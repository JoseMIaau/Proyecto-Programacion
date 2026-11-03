package red;

import modelo.Inventario;
import modelo.Producto;
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

    public Servidor(int puerto, BaseFrameAdmin frameAdmin) {
        this.puerto = puerto;
        this.frameAdmin = frameAdmin;
    }

    public void iniciar() {
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

    public void difundir(String mensajeJson) {
        synchronized (clientes) {
            for (ManejadorCliente c : clientes) {
                c.enviar(mensajeJson);
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

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    System.out.println("[SERVIDOR RECIBIÓ] " + line);
                    String type = Protocolo.extractJsonField(line, "type");

                    if (Protocolo.TYPE_DATA.equals(type)) {
                        int numero = Integer.parseInt(Protocolo.extractJsonField(line, "numero"));
                        String texto = Protocolo.extractJsonField(line, "texto");

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
                        if (alRecibirMensajeAdmin != null && content != null) {
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