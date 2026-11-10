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

    public int calcularCostoEnvio(int zona) {
        int costoEnvio = 0;
        double total = calcularTotal();

        if (total < 60000) {
            if (zona == 1) {
                costoEnvio = 790;
            } else if (zona == 2) {
                costoEnvio = 990;
            } else if (zona == 3) {
                costoEnvio = 1190;
            } else if (zona == 4) {
                costoEnvio = 1390;
            }
        }

        return costoEnvio;
    }

    public List<ItemCarrito> getItems() { return items; }
    public String getIdCliente() { return idCliente; }
}