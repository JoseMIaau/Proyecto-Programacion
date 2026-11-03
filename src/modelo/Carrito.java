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

    public int CostoEnvio() {
        int calcularEnvio = 0;
        double total = calcularTotal();
        for(;total < 60000;) {
            if (zona == 1) {
                calcularEnvio = 990;
            } else if (zona == 2) {
                calcularEnvio = 1990;
            } else if (zona == 3) {
                calcularEnvio = 2990;
            }
            else if (zona == 4) {
                calcularEnvio = 3990;
            }
            return calcularEnvio ;
        }
        return calcularEnvio;   //se adjudica un número y un costo de envío según la zona del cliente, si el total es mayor a 60000 el costo de envío es 0
    }

    public List<ItemCarrito> getItems() { return items; }
    public String getIdCliente() { return idCliente; }
}