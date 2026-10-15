package modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Compra {

    private String runCliente;
    private LocalDateTime fecha;
    private List<DetalleCompra> detalles;
    private double subtotal;
    private double iva;
    private double total;

    public Compra(String runCliente, List<DetalleCompra> detalles,
                  double subtotal, double iva, double total) {

        this.runCliente = runCliente;
        this.fecha = LocalDateTime.now();
        this.detalles = new ArrayList<>(detalles);
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
    }

    public String getRunCliente() {
        return runCliente;
    }

    public void setRunCliente(String runCliente) {
        this.runCliente = runCliente;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getFechaFormateada() {
        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        return fecha.format(formato);
    }

    public List<DetalleCompra> getDetalles() {
        return new ArrayList<>(detalles);
    }

    public void setDetalles(List<DetalleCompra> detalles) {
        this.detalles = new ArrayList<>(detalles);
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
