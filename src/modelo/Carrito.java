package modelo;

import java.util.ArrayList;
import java.util.List;

public class Carrito {
    private final String idCliente;
    private final List<ItemCarrito> items;

    public Carrito() {
        this("Invitado");
    }

    public Carrito(String idCliente) {
        this.idCliente = idCliente;
        this.items = new ArrayList<>();
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public void limpiar() {
        items.clear();
    }

    public double calcularTotal() {
        double total = 0;
        for (ItemCarrito item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public List<ItemCarrito> getItems() { return items; }
    public String getIdCliente() { return idCliente; }
}