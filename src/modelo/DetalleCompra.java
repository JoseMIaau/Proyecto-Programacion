
package modelo;

public class DetalleCompra {
    private final int idProducto;
    private final String nombreProducto;
    private final double cantidad;
    private final double precioUnitario;
    private final double subtotal;

    public DetalleCompra(int idProducto, String nombreProducto,
                         double cantidad, double precioUnitario,
                         double subtotal) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto == null ? "" : nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public double getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
