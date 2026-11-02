
package modelo;

import java.util.ArrayList;
import java.util.List;

public class Compra {
    private final int idCompra;
    private final String runCliente;
    private final String nombreCliente;
    private final String fecha;
    private final double subtotal;
    private final double iva;
    private final double total;
    private final List<DetalleCompra> detalles;

    public Compra(int idCompra, String runCliente, String nombreCliente, String fecha,
                double subtotal, double iva, double total, List<DetalleCompra> detalles) {
        this.idCompra = idCompra;
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

    public int getidCompra() {
        return idCompra;
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
}
