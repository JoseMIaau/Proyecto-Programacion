package modelo;

import java.util.ArrayList;
import java.util.List;

public class GestorPedidos {
    private static GestorPedidos instancia;
    private List<Pedido> pedidos;
    private int siguienteNumero;

    private GestorPedidos(){
        pedidos = new ArrayList<>();
        siguienteNumero = 1000;
    }
}
