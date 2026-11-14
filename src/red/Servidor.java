package red;

import modelo.Inventario;
import modelo.Producto;
import vista.BaseFrameAdmin;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;

public class Servidor {
    private final int puerto;
    private final List<ManejadorCliente> clientes = new CopyOnWriteArrayList<>();
    private ServerSocket serverSocket;
    private final BaseFrameAdmin frameAdmin;
    private int contadorClientes = 0;
    private java.util.function.Consumer<String> alRecibirMensajeAdmin;
    private volatile boolean corriendo = false;

    // Reserva de carritos (configurables). Por defecto: expira a los 15 min y avisa 1 min antes.
    private volatile long tiempoExpiracionMs = 15 * 60_000L;
    private volatile long avisoAntesMs = 60_000L;

    public Servidor(int puerto, BaseFrameAdmin frameAdmin) {
        this.puerto = puerto;
        this.frameAdmin = frameAdmin;
    }

    public void configurarReserva(long expiracionMs, long avisoAntesMs) {
        this.tiempoExpiracionMs = expiracionMs;
        this.avisoAntesMs = Math.min(avisoAntesMs, expiracionMs);
    }

    public void iniciar() {
        corriendo = true;
        iniciarHiloVigia();

        Thread aceptador = new Thread(() -> {
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
                    clientes.add(cliente);
                    cliente.setDaemon(true);
                    cliente.start();
                }
            } catch (IOException e) {
                if (corriendo) System.err.println("[!] Servidor cerrado: " + e.getMessage());
            }
        }, "servidor-aceptador");
        aceptador.setDaemon(true);
        aceptador.start();
    }

    public void detener() {
        corriendo = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
        for (ManejadorCliente c : clientes) c.cerrar();
    }

    private void iniciarHiloVigia() {
        Thread vigia = new Thread(() -> {
            while (corriendo) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    long ahora = System.currentTimeMillis();
                    for (ManejadorCliente c : clientes) {
                        if (c.getMiCarrito().isEmpty()) continue;

                        long inactivo = ahora - c.ultimoAcceso;
                        long limiteAviso = tiempoExpiracionMs - avisoAntesMs;

                        if (inactivo >= limiteAviso && inactivo < tiempoExpiracionMs && !c.avisoEnviado) {
                            c.avisoEnviado = true;
                            long seg = Math.max(1, (tiempoExpiracionMs - inactivo) / 1000);
                            c.enviar(Protocolo.createJsonData(0, "ALERTA_CARRITO:Su carrito expirará en " + seg
                                    + " segundos por inactividad. Modifique el carrito para renovar la reserva."));
                            System.out.println("[SERVIDOR] Aviso de expiracion enviado a " + c.getIdCliente());
                        }

                        if (inactivo >= tiempoExpiracionMs) {
                            System.out.println("[SERVIDOR] Carrito de " + c.getIdCliente() + " expirado por inactividad.");
                            c.enviar(Protocolo.createJsonData(0, "EXPIRADO:Su carrito ha expirado y los productos fueron liberados."));
                            c.liberarTodo();
                            c.renovarReserva();
                        }
                    }
                } catch (RuntimeException e) {
                    System.err.println("[SERVIDOR] Error en vigia: " + e);
                }
            }
        }, "servidor-vigia");
        vigia.setDaemon(true);
        vigia.start();
    }

    public void difundir(String mensajeJson) {
        for (ManejadorCliente c : clientes) {
            c.enviar(mensajeJson);
        }
    }

    public void enviarNotificacionACliente(String idClienteObjetivo, String mensaje) {
        for (ManejadorCliente c : clientes) {
            if (c.getIdCliente().equals(idClienteObjetivo)) {
                c.enviar(Protocolo.createJsonData(0, "NOTIFICACION:" + mensaje));
                System.out.println("[SERVIDOR] Notificacion enviada a " + idClienteObjetivo);
                break;
            }
        }
    }

    public void difundirNotificacionReposicion(int idProducto, String nombreProducto) {
        for (ManejadorCliente c : clientes) {
            if (c.deseaProducto(idProducto)) {
                c.enviar(Protocolo.createJsonData(0, "NOTIFICACION:'" + nombreProducto + "' ya tiene stock disponible."));
            }
        }
    }

    public void difundirNotificacionBajaPrecio(int idProducto, String nombreProducto, double precioAntiguo, double precioNuevo) {
        for (ManejadorCliente c : clientes) {
            if (c.deseaProducto(idProducto)) {
                c.enviar(Protocolo.createJsonData(0, "NOTIFICACION:'" + nombreProducto + "' bajó de precio!\nAntes: $"
                        + (int) precioAntiguo + " | Ahora: $" + (int) precioNuevo));
            }
        }
    }

    public void publicarCambioProducto(int id, String nombre, int stockAntiguo, int stockNuevo,
                                        double precioAntiguo, double precioNuevo) {
        difundir(Protocolo.createJsonData(id, Protocolo.STOCK + stockNuevo));
        if (precioNuevo != precioAntiguo) {
            difundir(Protocolo.createJsonData(id, Protocolo.PRECIO + precioNuevo));
        }
        if (stockAntiguo == 0 && stockNuevo > 0) {
            difundirNotificacionReposicion(id, nombre);
        }
        if (precioNuevo < precioAntiguo) {
            difundirNotificacionBajaPrecio(id, nombre, precioAntiguo, precioNuevo);
        }
    }

    public List<ManejadorCliente> getClientesActivos() {
        return new ArrayList<>(clientes);
    }

    public void setAlRecibirMensajeAdmin(java.util.function.Consumer<String> notificador) {
        this.alRecibirMensajeAdmin = notificador;
    }

    private void refrescarAdmin() {
        if (frameAdmin != null) {
            SwingUtilities.invokeLater(() -> frameAdmin.refrescarTablaAdmin());
        }
    }

    public class ManejadorCliente extends Thread {
        private final Socket socket;
        private BufferedReader in;
        private volatile PrintWriter out;
        private final String idCliente;
        private final Map<Integer, Integer> miCarrito = new HashMap<>();
        private final Set<Integer> listaDeseos = ConcurrentHashMap.newKeySet();
        public volatile long ultimoAcceso = System.currentTimeMillis();
        public volatile boolean avisoEnviado = false;

        public ManejadorCliente(Socket socket, int numeroCliente) {
            this.socket = socket;
            this.idCliente = "Cliente-" + numeroCliente;
        }

        public String getIdCliente() { return idCliente; }

        public Map<Integer, Integer> getMiCarrito() {
            synchronized (miCarrito) {
                return new HashMap<>(miCarrito);
            }
        }

        public void agregarListaDeseos(int idProducto) { listaDeseos.add(idProducto); }
        public boolean deseaProducto(int idProducto) { return listaDeseos.contains(idProducto); }

        public void renovarReserva() {
            this.ultimoAcceso = System.currentTimeMillis();
            this.avisoEnviado = false;
        }

        public void liberarTodo() {
            Map<Integer, Integer> copia;
            synchronized (miCarrito) {
                copia = new HashMap<>(miCarrito);
                miCarrito.clear();
            }
            Inventario inv = Inventario.getInstancia();
            for (Map.Entry<Integer, Integer> e : copia.entrySet()) {
                int nuevoStock = inv.liberarStock(e.getKey(), e.getValue());
                if (nuevoStock >= 0) {
                    difundir(Protocolo.createJsonData(e.getKey(), Protocolo.STOCK + nuevoStock));
                }
            }
            if (!copia.isEmpty()) refrescarAdmin();
        }

        public void cerrar() {
            try { socket.close(); } catch (IOException ignored) {}
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    try {
                        procesarLinea(line);
                    } catch (RuntimeException ex) {
                        System.err.println("[SERVIDOR] Mensaje invalido de " + idCliente + ": " + ex);
                    }
                }
            } catch (IOException e) {
                System.out.println("[-] " + idCliente + " desconectado");
            } finally {
                liberarTodo();
                clientes.remove(this);
                refrescarAdmin();
                cerrar();
            }
        }

        private void procesarLinea(String line) {
            System.out.println("[SERVIDOR RECIBIO] " + line);
            String type = Protocolo.extractJsonField(line, "type");

            if (Protocolo.TYPE_DATA.equals(type)) {
                String sNumero = Protocolo.extractJsonField(line, "numero");
                String texto = Protocolo.extractJsonField(line, "texto");
                if (sNumero == null || texto == null) return;
                int numero = Integer.parseInt(sNumero);

                if (texto.startsWith(Protocolo.AGREGAR_DESEO)) {
                    agregarListaDeseos(numero);
                    System.out.println("[SERVIDOR] " + idCliente + " suscrito a producto ID: " + numero);

                } else if (texto.startsWith(Protocolo.AGREGAR)) {
                    renovarReserva(); 
                    int cant = (int) Double.parseDouble(texto.substring(Protocolo.AGREGAR.length()));
                    if (cant > 0) reservar(numero, cant);
                    else if (cant < 0) devolver(numero, -cant);

                } else if (texto.equals(Protocolo.COMPRAR)) {
                    confirmarCompra();
                }

            } else if (Protocolo.TYPE_MESSAGE.equals(type)) {
                String content = Protocolo.extractJsonField(line, "content");
                if (content == null) return;

                if (content.startsWith("!deseo ")) {
                    try {
                        int idProd = Integer.parseInt(content.substring(7).trim());
                        Producto p = Inventario.getInstancia().buscarProducto(idProd);
                        if (p != null) {
                            agregarListaDeseos(idProd);
                            enviar(Protocolo.createJsonMessage("SISTEMA: Añadiste '" + p.getNombre() + "' a tu lista de deseados."));
                        } else {
                            enviar(Protocolo.createJsonMessage("SISTEMA: El ID " + idProd + " no existe."));
                        }
                    } catch (NumberFormatException ex) {
                        enviar(Protocolo.createJsonMessage("SISTEMA: Error. Usa el formato '!deseo ID' (Ej: !deseo 2)"));
                    }
                    return;
                }

                if (alRecibirMensajeAdmin != null) {
                    SwingUtilities.invokeLater(() -> alRecibirMensajeAdmin.accept(idCliente + " : " + content));
                }
            }
        }

        private void reservar(int idProducto, int cant) {
            int res = Inventario.getInstancia().reservarStock(idProducto, cant);
            if (res >= 0) {
                synchronized (miCarrito) {
                    miCarrito.merge(idProducto, cant, Integer::sum);
                }
                difundir(Protocolo.createJsonData(idProducto, Protocolo.STOCK + res));
                refrescarAdmin();
            } else {
                String motivo = (res == Inventario.RESERVA_INEXISTENTE)
                        ? "El producto ya no existe."
                        : "No hay stock suficiente (otro cliente se lo llevo primero).";
                enviar(Protocolo.createJsonData(idProducto, Protocolo.RECHAZADO + cant + ":" + motivo));
                int stockReal = Inventario.getInstancia().obtenerStockDisponible(idProducto);
                if (stockReal >= 0) enviar(Protocolo.createJsonData(idProducto, Protocolo.STOCK + stockReal));
            }
        }

        private void devolver(int idProducto, int pedido) {
            int dev;
            synchronized (miCarrito) {
                int enCarro = miCarrito.getOrDefault(idProducto, 0);
                dev = Math.min(pedido, enCarro);
                if (dev > 0) {
                    if (enCarro == dev) miCarrito.remove(idProducto);
                    else miCarrito.put(idProducto, enCarro - dev);
                }
            }
            if (dev > 0) {
                int nuevoStock = Inventario.getInstancia().liberarStock(idProducto, dev);
                if (nuevoStock >= 0) difundir(Protocolo.createJsonData(idProducto, Protocolo.STOCK + nuevoStock));
                refrescarAdmin();
            } else {
                System.err.println("[SERVIDOR] " + idCliente + " intento devolver producto " + idProducto + " que no tenia reservado.");
                int stockReal = Inventario.getInstancia().obtenerStockDisponible(idProducto);
                if (stockReal >= 0) enviar(Protocolo.createJsonData(idProducto, Protocolo.STOCK + stockReal)); 
            }
        }

        private void confirmarCompra() {
            Map<Integer, Integer> comprado;
            synchronized (miCarrito) {
                comprado = new HashMap<>(miCarrito);
                miCarrito.clear();
            }
            Inventario inv = Inventario.getInstancia();
            for (Map.Entry<Integer, Integer> e : comprado.entrySet()) {
                inv.confirmarVenta(e.getKey(), e.getValue());
            }
            inv.persistirCatalogo();
            System.out.println("[SERVIDOR] Compra finalizada por " + idCliente + ". Stock confirmado.");
            refrescarAdmin();
        }

        public void enviar(String msg) {
            PrintWriter w = out;
            if (w != null) w.println(msg);
        }
    }
}