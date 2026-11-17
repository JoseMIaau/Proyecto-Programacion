package modelo;

import java.util.ArrayList;
import java.util.List;

public class ListaDeseos {
    private String identificadorCliente;
    private final List<Producto> productos;

    public ListaDeseos(String identificadorCliente) {
        this.identificadorCliente = identificadorCliente == null ? "" : identificadorCliente.trim();
        this.productos = new ArrayList<>();
    }

    public String getIdentificadorCliente() {
        return identificadorCliente;
    }

    public void setIdentificadorCliente(String identificadorCliente) {
        this.identificadorCliente = identificadorCliente == null ? "" : identificadorCliente.trim();
    }

    public boolean agregarProducto(Producto p) {
        if (p == null || contieneProducto(p.getId())) {
            return false;
        }
        productos.add(p);
        return true;
    }

    public boolean eliminarProducto(int idProducto) {
        return productos.removeIf(p -> p.getId() == idProducto);
    }

    public boolean contieneProducto(int idProducto) {
        for (Producto p : productos) {
            if (p.getId() == idProducto) {
                return true;
            }
        }
        return false;
    }

    public List<Producto> getProductos() {
        return new ArrayList<>(productos);
    }
}