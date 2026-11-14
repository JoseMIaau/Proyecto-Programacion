package red;

import java.io.*;
import java.net.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import modelo.Inventario;
import modelo.Producto;

//Maneja la conexion socket desde la aplicacion del Cliente hacia el Servidor Admin.
public class Cliente {
    private static Cliente instancia;
    private final String host;
    private final int puerto;
    private volatile Socket socket;
    private volatile PrintWriter out;
    private volatile boolean conectado = false;

    private Runnable onStockUpdateCallback;
    private Consumer<String> alRecibirMensaje;
    private Consumer<String> alCambiarConexion;           
    private BiConsumer<String, String> mostradorAvisos = Cliente::avisoPorDefecto; 

    public Cliente(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
        instancia = this;
    }

    public static Cliente getInstancia() { return instancia; }

    public void setOnStockUpdateCallback(Runnable callback) { this.onStockUpdateCallback = callback; }
    public void setAlRecibirMensaje(Consumer<String> notificador) { this.alRecibirMensaje = notificador; }
    public void setAlCambiarConexion(Consumer<String> listener) { this.alCambiarConexion = listener; }
    public void setMostradorAvisos(BiConsumer<String, String> mostrador) { this.mostradorAvisos = mostrador; }

    public boolean estaConectado() { return conectado; }

    //Inicia un hilo secundario para escuchar mensajes del servidor sin que se congele la ventana.
    public void conectar() {
        Thread hilo = new Thread(() -> {
            String motivoCierre = null;
            try {
                Socket s = new Socket();
                s.connect(new InetSocketAddress(host, puerto), 5000); // no se queda colgado si la ip no responde
                socket = s;
                out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
                conectado = true;
                notificarConexion("conectado");

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    try {
                        procesarLinea(line);
                    } catch (RuntimeException ex) {
                        System.err.println("[!] mensaje ignorado (" + ex + "): " + line);//casos que mataban el hilo
                    }
                }
                motivoCierre = "El servidor cerró la conexión.";
            } catch (SocketTimeoutException e) {
                motivoCierre = "Tiempo de espera agotado al conectar con " + host + ":" + puerto + ".";
            } catch (IOException e) {
                motivoCierre = "No se pudo mantener la conexión con " + host + ":" + puerto + " (" + e.getMessage() + ").";
            } finally {
                conectado = false;
                out = null;
                if (motivoCierre != null) notificarConexion(motivoCierre);
            }
        }, "cliente-socket");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void procesarLinea(String line) {
        String type = Protocolo.extractJsonField(line, "type");

        if (Protocolo.TYPE_DATA.equals(type)) {
            String sNumero = Protocolo.extractJsonField(line, "numero");
            String texto = Protocolo.extractJsonField(line, "texto");
            if (sNumero == null || texto == null) return;
            int idProd = Integer.parseInt(sNumero);

            if (texto.startsWith(Protocolo.STOCK)) {
                int nuevoStock = Integer.parseInt(texto.substring(Protocolo.STOCK.length()));
                enEdt(() -> {
                    Producto p = Inventario.getInstancia().buscarProducto(idProd);
                    if (p != null) { p.setStock(nuevoStock); refrescarVistas(); }
                });

            } else if (texto.startsWith(Protocolo.PRECIO)) {
                double nuevoPrecio = Double.parseDouble(texto.substring(Protocolo.PRECIO.length()));
                enEdt(() -> {
                    Producto p = Inventario.getInstancia().buscarProducto(idProd);
                    if (p != null) { p.setPrecio(nuevoPrecio); refrescarVistas(); }
                });

            } else if (texto.startsWith(Protocolo.RECHAZADO)) {
                // formato: RECHAZADO:<cantidad>:<motivo>
                String[] partes = texto.substring(Protocolo.RECHAZADO.length()).split(":", 2);
                int cant = Integer.parseInt(partes[0]);
                String motivo = partes.length > 1 ? partes[1] : "El servidor rechazó el producto.";
                enEdt(() -> {
                    Inventario.getInstancia().revertirAgregado(idProd, cant);
                    refrescarVistas();
                    mostradorAvisos.accept("No se pudo agregar", motivo);
                });

            } else if (texto.startsWith("ALERTA_CARRITO:")) {
                String mensaje = texto.substring(15);
                enEdt(() -> mostradorAvisos.accept("Aviso de Inactividad", mensaje));

            } else if (texto.startsWith("NOTIFICACION:")) {
                String mensaje = texto.substring(13);
                enEdt(() -> mostradorAvisos.accept("Nueva Notificación", mensaje));

            } else if (texto.startsWith("EXPIRADO:")) {
                String mensaje = texto.substring(9);
                enEdt(() -> {
                    Inventario.getInstancia().limpiarCarritoLocal();
                    refrescarVistas();
                    mostradorAvisos.accept("Carrito Expirado", mensaje);
                });
            }

        } else if (Protocolo.TYPE_MESSAGE.equals(type)) {
            String contenido = Protocolo.extractJsonField(line, "content");
            if (alRecibirMensaje != null && contenido != null) {
                enEdt(() -> alRecibirMensaje.accept(contenido));
            }
        }
    }

    private void refrescarVistas() {
        if (onStockUpdateCallback != null) onStockUpdateCallback.run();
    }

    private void notificarConexion(String estado) {
        if (alCambiarConexion != null) enEdt(() -> alCambiarConexion.accept(estado));
    }

    private static void enEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) r.run();
        else SwingUtilities.invokeLater(r);
    }

    private static void avisoPorDefecto(String titulo, String mensaje) {
        try {
            javax.swing.JOptionPane.showMessageDialog(null, mensaje, titulo, javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (java.awt.HeadlessException e) {
            System.out.println("[AVISO] " + titulo + ": " + mensaje);
        }
    }


    public boolean enviarDatos(int numero, String texto) {
        PrintWriter w = out;
        if (w == null) return false;
        w.println(Protocolo.createJsonData(numero, texto));
        return !w.checkError();
    }

    public boolean enviarMensaje(String msg) {
        PrintWriter w = out;
        if (w == null) return false;
        w.println(Protocolo.createJsonMessage(msg));
        return !w.checkError();
    }
}