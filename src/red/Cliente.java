package red;

import modelo.Inventario;
import modelo.Producto;

import java.io.*;
import java.net.*;

public class Cliente {
    private static Cliente instancia;
    private final String host;
    private final int puerto;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Runnable onStockUpdateCallback;

    public Cliente(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
        instancia = this;
    }

    public static Cliente getInstancia() {
        return instancia;
    }

    public void setOnStockUpdateCallback(Runnable callback) {
        this.onStockUpdateCallback = callback;
    }

    public void conectar() {
        new Thread(() -> {
            try {
                socket = new Socket(host, puerto);
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String type = Protocolo.extractJsonField(line, "type");

                    if (Protocolo.TYPE_DATA.equals(type)) {
                        int idProd = Integer.parseInt(Protocolo.extractJsonField(line, "numero"));
                        String texto = Protocolo.extractJsonField(line, "texto");

                        if (texto.startsWith("STOCK:")) {
                            int nuevoStock = Integer.parseInt(texto.substring(6));
                            Producto p = Inventario.getInstancia().buscarProducto(idProd);
                            if (p != null) {
                                p.setStock(nuevoStock);
                                if (onStockUpdateCallback != null) {
                                    onStockUpdateCallback.run();
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("[!] error cliente socket: " + e.getMessage());
            }
        }).start();
    }

    public void enviarDatos(int numero, String texto) {
        if (out != null) {
            out.println(Protocolo.createJsonData(numero, texto));
        }
    }

    public void enviarMensaje(String msg) {
        if (out != null) {
            out.println(Protocolo.createJsonMessage(msg));
        }
    }
}