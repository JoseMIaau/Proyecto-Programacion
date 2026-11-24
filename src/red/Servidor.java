package red;

import modelo.Inventario;
import modelo.Producto;
import IA.GeminiAPIClient;
import vista.BaseFrameAdmin;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class Servidor {
    private final int puerto;
    private final List<ManejadorCliente> clientes = new ArrayList<>();
    private ServerSocket serverSocket;
    private final BaseFrameAdmin frameAdmin;
    private int contadorClientes = 0;
    private java.util.function.Consumer<String> alRecibirMensajeAdmin;
    private boolean corriendo = false;
    //valores de prueba, estan en ms
    private static final long TIEMPO_AVISO_MS = 30000; //30 segundos
    private static final long TIEMPO_EXPIRACION_MS = 601000; //1 minuto

    public Servidor(int puerto, BaseFrameAdmin frameAdmin) {
        this.puerto = puerto;
        this.frameAdmin = frameAdmin;
    }

    public void iniciar() {
        corriendo = true;
        iniciarHiloVigia();

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(puerto);
                String ipLocal = InetAddress.getLocalHost().getHostAddress();
                System.out.println("==================================================");
                System.out.println("[+] Servidor Admin iniciado correctamente");
                System.out.println("[+] IP para conectar clientes: " + ipLocal);
                System.out.println("[+] Puerto: " + puerto);
                System.out.println("==================================================");

                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    System.out.println("[+] Nuevo cliente conectado desde: " + socket.getRemoteSocketAddress());
                    contadorClientes++;
                    ManejadorCliente cliente = new ManejadorCliente(socket, contadorClientes);
                    synchronized (clientes) {
                        clientes.add(cliente);
                    }
                    cliente.start();
                }
            } catch (IOException e) {
                System.err.println("[!] Servidor cerrado: " + e.getMessage());
            }
        }).start();
    }

    private void iniciarHiloVigia() {
        new Thread(() -> {
            while (corriendo) {
                try {
                    Thread.sleep(2000); 
                    long tiempoActual = System.currentTimeMillis();
                    
                    synchronized (clientes) {
                        for (ManejadorCliente c : clientes) {
                            if (c.getMiCarrito().isEmpty()) continue;

                            long tiempoInactivo = tiempoActual - c.ultimoAcceso;

                            if (tiempoInactivo >= TIEMPO_AVISO_MS && !c.avisoEnviado && tiempoInactivo < TIEMPO_EXPIRACION_MS) {
                                c.enviar(Protocolo.createJsonData(0, "ALERTA_CARRITO:Su carrito expirará por inactividad pronto."));
                                c.avisoEnviado = true;
                                System.out.println("[SERVIDOR] Aviso de expiración enviado a " + c.getIdCliente());
                            }

                            if (tiempoInactivo >= TIEMPO_EXPIRACION_MS) {
                                System.out.println("[SERVIDOR] Carrito de " + c.getIdCliente() + " expirado por inactividad.");
                                c.enviar(Protocolo.createJsonData(0, "EXPIRADO:Su carrito ha expirado y los productos fueron liberados."));
                                
                                for (java.util.Map.Entry<Integer, Integer> entry : c.getMiCarrito().entrySet()) {
                                    Producto p = Inventario.getInstancia().buscarProducto(entry.getKey());
                                    if (p != null) {
                                        p.setStock(p.getStock() + entry.getValue());
                                        p.setReservado(Math.max(0, p.getReservado() - entry.getValue()));
                                        difundir(Protocolo.createJsonData(p.getId(), "STOCK:" + p.getStock()));
                                    }
                                }
                                c.getMiCarrito().clear();
                                c.renovarReserva(); 
                                
                                if (frameAdmin != null) {
                                    SwingUtilities.invokeLater(() -> frameAdmin.refrescarTablaAdmin());
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public void difundir(String mensajeJson) {
        synchronized (clientes) {
            for (ManejadorCliente c : clientes) {
                c.enviar(mensajeJson);
            }
        }
    }

    public void enviarNotificacionACliente(String idClienteObjetivo, String mensaje) {
        synchronized (clientes) {
            for (ManejadorCliente c : clientes) {
                if (c.getIdCliente().equals(idClienteObjetivo)) {
                    c.enviar(Protocolo.createJsonData(0, "NOTIFICACION:" + mensaje));
                    System.out.println("[SERVIDOR] Notificación enviada a " + idClienteObjetivo);
                    break;
                }
            }
        }
    }

    public void difundirNotificacionReposicion(int idProducto, String nombreProducto) {
        synchronized (clientes) {
            for (ManejadorCliente c : clientes) {
                if (c.deseaProducto(idProducto)) {
                    enviarNotificacionACliente(c.getIdCliente(), nombreProducto + "' ya tiene stock disponible.");
                }
            }
        }
    }

    public void difundirNotificacionBajaPrecio(int idProducto, String nombreProducto, double precioAntiguo, double precioNuevo) {
        synchronized (clientes) {
            for (ManejadorCliente c : clientes) {
                if (c.deseaProducto(idProducto)) {
                    enviarNotificacionACliente(c.getIdCliente(), nombreProducto + "' bajó de precio!\nAntes: $" + (int)precioAntiguo + " | Ahora: $" + (int)precioNuevo);
                }
            }
        }
    }

    public List<ManejadorCliente> getClientesActivos() {
        synchronized (clientes) {
            return new ArrayList<>(clientes);
        }
    }

    public void setAlRecibirMensajeAdmin(java.util.function.Consumer<String> notificador) {
        this.alRecibirMensajeAdmin = notificador;
    }

    public class ManejadorCliente extends Thread {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private final String idCliente;
        private final java.util.Map<Integer, Integer> miCarrito = new java.util.HashMap<>();
        public long ultimoAcceso = System.currentTimeMillis();
        public boolean avisoEnviado = false;
        private final java.util.List<Integer> listaDeseos = new java.util.ArrayList<>();
        private final java.util.List<String[]> historialIA = new java.util.ArrayList<>();
        private static final int MAX_HISTORIAL_IA = 10;
        
        public ManejadorCliente(Socket socket, int numeroCliente) {
            this.socket = socket;
            this.idCliente = "Cliente-" + numeroCliente;
        }

        public String getIdCliente() {
            return idCliente;
        }

        public java.util.Map<Integer, Integer> getMiCarrito() {
            return miCarrito;
        }

        public void agregarListaDeseos(int idProducto) {
            if (!listaDeseos.contains(idProducto)) {
                listaDeseos.add(idProducto);
            }
        }

        public boolean deseaProducto(int idProducto) {
            return listaDeseos.contains(idProducto);
        }

        private String construirInstruccionSistema() {
            StringBuilder sb = new StringBuilder();
            sb.append("Eres el asistente virtual de SuperCuricó, una tienda online de productos. ");
            sb.append("Tu nombre es Asistente SuperCuricó. ");
            sb.append("Responde de forma amigable, breve y útil. ");
            sb.append("Si te preguntan por productos, usa SOLO los que están en el inventario actual. ");
            sb.append("Los precios están en pesos chilenos (CLP). ");
            sb.append("No inventes productos que no existan en el inventario. ");
            sb.append("Si preguntan por algo que no vendes, dilo y sugiere alternativas del catálogo. ");
            sb.append("Cuando recomiendes o menciones un producto, SIEMPRE incluye su ID en formato [ID:X] justo después del nombre. ");
            sb.append("Por ejemplo: Plátano Granel [ID:1] a $1.290. Esto es OBLIGATORIO para cada producto que menciones. ");
            sb.append("No uses comillas dobles en tus respuestas.\n\n");
            sb.append("=== INVENTARIO ACTUAL ===\n");

            java.util.List<Producto> productos = Inventario.getInstancia().leerProductos();
            if (productos.isEmpty()) {
                sb.append("(No hay productos disponibles)\n");
            } else {
                for (Producto p : productos) {
                    sb.append("- ID:").append(p.getId())
                    .append(" | ").append(p.getNombre())
                    .append(" | Precio: $").append((int) p.getPrecio())
                    .append(" | Stock disponible: ").append(p.getStock())
                    .append(" | Categoría: ").append(p.getCategoria().name().replace("_", " "))
                    .append("\n");
                }
            }
            return sb.toString();
        }

        public void renovarReserva() {
            this.ultimoAcceso = System.currentTimeMillis();
            this.avisoEnviado = false;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    renovarReserva();
                    System.out.println("[SERVIDOR RECIBIÓ] " + line);
                    String type = Protocolo.extractJsonField(line, "type");

                    if (Protocolo.TYPE_DATA.equals(type)) {
                        int numero = Integer.parseInt(Protocolo.extractJsonField(line, "numero"));
                        String texto = Protocolo.extractJsonField(line, "texto");

                        if (texto != null && texto.startsWith("AGREGAR_DESEO:")) {
                            agregarListaDeseos(numero);
                            System.out.println("[SERVIDOR] Cliente " + idCliente + " suscrito a producto ID: " + numero);
                            continue;
                        }
                        if (texto != null && texto.startsWith("CONSULTA_IA:")) {
                            String pregunta = texto.substring("CONSULTA_IA:".length());
                            System.out.println("[SERVIDOR] " + idCliente + " pregunta a IA: " + pregunta);
                            
                            new Thread(() -> {
                                try {
                                    String instruccion = construirInstruccionSistema();
                                    GeminiAPIClient gemini = new GeminiAPIClient();
                                    String respuesta;
                                    synchronized (historialIA) {
                                        respuesta = gemini.consultar(instruccion, historialIA, pregunta);
                                        historialIA.add(new String[]{"user", pregunta});
                                        historialIA.add(new String[]{"model", respuesta});
                                        while (historialIA.size() > MAX_HISTORIAL_IA * 2) {
                                            historialIA.remove(0);
                                            historialIA.remove(0);
                                        }
                                    }
                                    System.out.println("[SERVIDOR] Respuesta IA para " + idCliente + " lista.");
                                    enviar(Protocolo.createJsonData(0, "RESPUESTA_IA:" + respuesta));
                                } catch (Exception ex) {
                                    System.err.println("[SERVIDOR] Error IA: " + ex.getMessage());
                                    enviar(Protocolo.createJsonData(0, "ERROR_IA:" + ex.getMessage()));
                                }
                            }).start();
                            continue;
                        }

                        if (texto != null && texto.startsWith("AGREGAR:")) {
                            int cant = (int) Double.parseDouble(texto.substring(8));
                            Producto p = Inventario.getInstancia().buscarProducto(numero);

                            if (p != null) {
                                boolean modificado = false;

                                if (cant > 0 && p.getStock() >= cant) {
                                    p.setStock(p.getStock() - cant);
                                    p.setReservado(p.getReservado() + cant);
                                    miCarrito.put(numero, miCarrito.getOrDefault(numero, 0) + cant);
                                    modificado = true;
                                } else if (cant < 0) {
                                    int dev = Math.abs(cant);
                                    p.setStock(p.getStock() + dev);
                                    p.setReservado(Math.max(0, p.getReservado() - dev));

                                    int cantEnCarro = miCarrito.getOrDefault(numero, 0);
                                    if (cantEnCarro <= dev) {
                                        miCarrito.remove(numero);
                                    } else {
                                        miCarrito.put(numero, cantEnCarro - dev);
                                    }
                                    modificado = true;
                                }

                                if (modificado) {
                                    difundir(Protocolo.createJsonData(p.getId(), "STOCK:" + p.getStock()));

                                    if (frameAdmin != null) {
                                        SwingUtilities.invokeLater(() -> frameAdmin.refrescarTablaAdmin());
                                    }
                                }
                            }else if (texto != null && texto.equals("COMPRAR")) {
                                miCarrito.clear();
                                System.out.println("[SERVIDOR] Compra finalizada por " + idCliente + ". Stock confirmado.");
                            }else {
                                System.err.println("[SERVIDOR ERROR] producto id " + numero + " no encontrado");
                            }
                        }
                    } else if (Protocolo.TYPE_MESSAGE.equals(type)) {
                        String content = Protocolo.extractJsonField(line, "content");
                        
                        if (content.startsWith("!deseo ")) {
                                try {
                                    int idProd = Integer.parseInt(content.substring(7).trim());
                                    Producto p = Inventario.getInstancia().buscarProducto(idProd);
                                    
                                    if (p != null) {
                                        agregarListaDeseos(idProd);
                                        System.out.println("[SERVIDOR] " + idCliente + " añadió a lista deseados: " + p.getNombre());
                                        enviar(Protocolo.createJsonMessage("SISTEMA: Añadiste '" + p.getNombre() + "' a tu lista de deseados."));
                                    } else {
                                        enviar(Protocolo.createJsonMessage("SISTEMA: El ID " + idProd + " no existe."));
                                    }
                                } catch (NumberFormatException ex) {
                                    enviar(Protocolo.createJsonMessage("SISTEMA: Error. Usa el formato '!deseo ID' (Ej: !deseo 2)"));
                                }
                                continue; 
                            }
                        
                        if (alRecibirMensajeAdmin != null) {
                            SwingUtilities.invokeLater(() -> alRecibirMensajeAdmin.accept(idCliente + " : " + content));
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("[-] " + idCliente + " desconectado");
            } finally {
                for (java.util.Map.Entry<Integer, Integer> entry : miCarrito.entrySet()) {
                    Producto p = Inventario.getInstancia().buscarProducto(entry.getKey());
                    if (p != null) {
                        p.setStock(p.getStock() + entry.getValue());
                        p.setReservado(Math.max(0, p.getReservado() - entry.getValue()));
                        difundir(Protocolo.createJsonData(p.getId(), "STOCK:" + p.getStock()));
                    }
                }
                synchronized (clientes) {
                    clientes.remove(this);
                }
                if (frameAdmin != null) {
                    SwingUtilities.invokeLater(() -> frameAdmin.refrescarTablaAdmin());
                }
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        public void enviar(String msg) {
            if (out != null) out.println(msg);
        }
    }
}