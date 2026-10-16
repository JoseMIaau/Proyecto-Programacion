package modelo;

public class ItemCarrito {
    private Producto producto;
    private double cantidad;

    public ItemCarrito(Producto producto, double cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() {
        return producto;
    }
    public double getCantidad() {
        return cantidad;
    }
    public double getSubtotal() { 
        return producto.getPrecio() * cantidad; 
    }
    public void setCantidad(double cantidad) { 
        this.cantidad = cantidad; 
    }
    
}