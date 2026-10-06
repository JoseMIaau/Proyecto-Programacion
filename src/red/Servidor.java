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
                    ManejadorCliente cliente = new ManejadorCliente(socket);
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

    private class ManejadorCliente extends Thread {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ManejadorCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    System.out.println("[SERVIDOR RECIBIO] " + line);
                    String type = Protocolo.extractJsonField(line, "type");
                    System.out.println("[SERVIDOR] tipo detectado: " + type);

                    if (Protocolo.TYPE_DATA.equals(type)) {
                        int numero = Integer.parseInt(Protocolo.extractJsonField(line, "numero"));
                        String texto = Protocolo.extractJsonField(line, "texto");
                        System.out.println("[SERVIDOR] numero=" + numero + ", texto=" + texto);

                        if (texto.startsWith("AGREGAR:")) {
                            double cant = Double.parseDouble(texto.substring(8));
                            Producto p = Inventario.getInstancia().buscarProducto(numero);
                            if (p != null) {
                                boolean ok = Inventario.getInstancia().agregarAlCarrito(p, cant);
                                System.out.println("[SERVIDOR] descuento en inventario: " + ok + ", nuevo stock=" + p.getStock());
                                if (ok) {
                                    difundir(Protocolo.createJsonData(p.getId(), "STOCK:" + p.getStock()));

                                    if (frameAdmin != null) {
                                        System.out.println("[SERVIDOR] refrescar tabla admin");
                                        SwingUtilities.invokeLater(() -> {
                                            frameAdmin.refrescarTablaAdmin();
                                        });
                                    }else {
                                        System.err.println("[SERVIDOR ERROR] frameAdmin es null");
                                    }
                                }
                            }else {
                                System.err.println("[SERVIDOR ERROR] producto id " + numero + " no encontrado");
                            }
                        }
                    } else if (Protocolo.TYPE_MESSAGE.equals(type)) {
                        String content = Protocolo.extractJsonField(line, "content");
                        difundir(Protocolo.createJsonMessage(content));
                    }
                }
            } catch (IOException e) {
                System.out.println("[-] Cliente desconectado");
            } finally {
                try {
                    synchronized (clientes) {
                        clientes.remove(this);
                    }
                    socket.close();
                } catch (IOException ignored) {}
            }
        }

        public void enviar(String msg) {
            if (out != null) out.println(msg);
        }
    }
}
