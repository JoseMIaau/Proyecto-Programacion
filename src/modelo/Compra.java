
package modelo;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Compra {
    private final String runCliente;
    private final String nombreCliente;
    private final String fecha;
    private final double subtotal;
    private final double iva;
    private final double total;
    private final List<DetalleCompra> detalles;

    public Compra(String runCliente, String nombreCliente, String fecha,
                double subtotal, double iva, double total,
                List<DetalleCompra> detalles) {
        this.runCliente = runCliente == null ? "" : runCliente.trim();
        this.nombreCliente = nombreCliente == null ? "" : nombreCliente.trim();
        this.fecha = fecha == null ? "" : fecha;
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
        this.detalles = new ArrayList<>(
                detalles == null ? List.of() : detalles
        );
    }

    public String getRunCliente() {
        return runCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String getFecha() {
        return fecha;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getIva() {
        return iva;
    }

    public double getTotal() {
        return total;
    }

    public List<DetalleCompra> getDetalles() {
        return new ArrayList<>(detalles);
    }

    public String generarComprobante(){
        StringBuilder comprobante = new StringBuilder();
        comprobante.append("===== COMPROBANTE DE COMPRA =====\n\n");
        comprobante.append("Fecha: ").append(fecha).append("\n");
        comprobante.append("RUN: ").append(runCliente).append("\n");
        comprobante.append("Cliente: ").append(nombreCliente).append("\n\n");

        comprobante.append("Productos:\n");
        comprobante.append("---------------------------------\n");

        for (DetalleCompra detalle : detalles) {
            comprobante.append((int) detalle.getCantidad())
                    .append("x ")
                    .append(detalle.getNombreProducto())
                    .append("\n");

            comprobante.append("   Precio unitario: $")
                    .append((int) detalle.getPrecioUnitario())
                    .append("\n");

            comprobante.append("   Subtotal: $")
                    .append((int) detalle.getSubtotal())
                    .append("\n\n");
        }

        comprobante.append("---------------------------------\n");
        comprobante.append("Subtotal: $").append((int) subtotal).append("\n");
        comprobante.append("IVA (19%): $").append((int) iva).append("\n");
        comprobante.append("TOTAL: $").append((int) total).append("\n");
        comprobante.append("---------------------------------\n");
        comprobante.append("Gracias por comprar en SuperCuricó");

        return comprobante.toString();
    }
    

    private static String codificar(String valor) {
        return Base64.getEncoder().encodeToString(
                valor.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String decodificar(String valor) {
        return new String(
                Base64.getDecoder().decode(valor),
                StandardCharsets.UTF_8
        );
    }

    // SOCKETSS
    public String serializarParaRed() {
        StringBuilder datos = new StringBuilder();

        datos.append(codificar(runCliente)).append('|')
             .append(codificar(nombreCliente)).append('|')
             .append(codificar(fecha)).append('|')
             .append(subtotal).append('|')
             .append(iva).append('|')
             .append(total);

        for (DetalleCompra d : detalles) {
            datos.append('|')
                 .append(d.getIdProducto()).append(',')
                 .append(codificar(d.getNombreProducto())).append(',')
                 .append(d.getCantidad()).append(',')
                 .append(d.getPrecioUnitario()).append(',')
                 .append(d.getSubtotal());
        }

        return "COMPRA:" + Base64.getEncoder().encodeToString(
                datos.toString().getBytes(StandardCharsets.UTF_8)
        );
    }

    // Método existente para reconstruir una compra recibida por socket.
    public static Compra desdeMensajeRed(String mensaje) {
        try {
            String contenido = mensaje.substring("COMPRA:".length());

            String datos = new String(
                    Base64.getDecoder().decode(contenido),
                    StandardCharsets.UTF_8
            );

            String[] partes = datos.split("\\|", -1);

            if (partes.length < 6) {
                return null;
            }

            List<DetalleCompra> detalles = new ArrayList<>();

            for (int i = 6; i < partes.length; i++) {
                String[] d = partes[i].split(",", -1);

                if (d.length != 5) {
                    return null;
                }

                detalles.add(new DetalleCompra(
                        Integer.parseInt(d[0]),
                        decodificar(d[1]),
                        Double.parseDouble(d[2]),
                        Double.parseDouble(d[3]),
                        Double.parseDouble(d[4])
                ));
            }

            return new Compra(
                    decodificar(partes[0]),
                    decodificar(partes[1]),
                    decodificar(partes[2]),
                    Double.parseDouble(partes[3]),
                    Double.parseDouble(partes[4]),
                    Double.parseDouble(partes[5]),
                    detalles
            );

        } catch (RuntimeException e) {
            System.err.println(
                    "No se pudo interpretar una compra recibida: "
                    + e.getMessage()
            );
            return null;
        }
    }
}
