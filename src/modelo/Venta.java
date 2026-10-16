package modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Venta {
    private int idVenta;
    private LocalDateTime fecha;
    private String idCliente;
    private String detalleProductos;
    private double subtotal;
    private double iva;
    private double total;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Venta(int idVenta, LocalDateTime fecha, String idCliente, List<ItemCarrito> items, double subtotal, double iva, double total) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.idCliente = idCliente;
        this.detalleProductos = "";
        for (ItemCarrito item : items) {
            this.detalleProductos += (int)item.getCantidad() + "x " + item.getProducto().getNombre() + " / ";
        }
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public String getFecha() {
        return fecha.format(FORMATTER);
    }

    public String getIdCliente() {
        return idCliente;
    }

    public String getDetalleProductos() {
        return detalleProductos;
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

    public void imprimirVenta() {
        System.out.println("\n----- NUEVA VENTA REGISTRADA -----");
        System.out.println("ID Venta: " + idVenta);
        System.out.println("Fecha: " + getFecha());
        System.out.println("Cliente: " + idCliente);
        System.out.println("Productos: " + detalleProductos);
        System.out.println("Total: $" + (int)total);
        System.out.println("----------------------------------\n");
    }

}
