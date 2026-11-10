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

    public static GestorPedidos getInstancia() {
        if(instancia == null){
            instancia = new GestorPedidos();
        }

        return instancia;
    }
    private int generarNumeroPedido(){
        int numero = siguienteNumero;
        siguienteNumero++;
        return numero;
    }

    public Pedido crearPedido(Compra compra, String idCliente){
        if (compra == null || idCliente == null || idCliente.trim().isEmpty()){
            return null;
        }

        Pedido pedido = new Pedido(generarNumeroPedido(), compra, idCliente.trim());
        pedidos.add(pedido);
        return pedido;
    }
    
    public Pedido buscarPedido(int numeroPedido){
        for (Pedido pedido:pedidos){
            if (pedido.getNumeroPedido() == numeroPedido){
                return pedido;
            }
        }
        return null;
    }
    
    public List<Pedido> obtenerPedidos(){
        return new ArrayList<>(pedidos);
    }

    public List<Pedido> filtrarPorEstado(EstadoPedido estado){
        List<Pedido> filtrados = new ArrayList<>();

        if (estado == null){
            return filtrados;
        }
        for (Pedido pedido:pedidos){
            if (pedido.getEstado() == estado){
                filtrados.add(pedido);
            }
        }

        return filtrados;
    }

    public boolean cambiarEstado(int numeroPedido, EstadoPedido nuevoEstado){
        Pedido pedido = buscarPedido(numeroPedido);

        if(pedido == null){
            return false;
        }
        return pedido.cambiarEstado(nuevoEstado);
    }

    public boolean cancelarPedido(int numeroPedido){
        Pedido pedido = buscarPedido(numeroPedido);

        if (pedido==null || !pedido.puedeCancelarse()){
            return false;
        }

        Compra compra = pedido.getCompra();
        Inventario inventario = Inventario.getInstancia();

        for (DetalleCompra detalle : compra.getDetalles()){
            Producto producto = inventario.buscarProducto(detalle.getIdProducto());

            if (producto == null){
                return false;
            }
        }

        for (DetalleCompra detalle : compra.getDetalles()){
            boolean restaurado = inventario.agregarStock(detalle.getIdProducto(), (int) detalle.getCantidad());

            if (!restaurado){
                return false;
            }
        }
        return pedido.marcarComoCancelado();
    }

    public Pedido obtenerUltimoPedidoPorCliente(String idCliente){
        if (idCliente == null){
            return null;
        }
        for (int i = pedidos.size() - 1; i>=0; i--){
            Pedido pedido = pedidos.get(i);
            if(pedido.getIdCliente().equals(idCliente)){
                return pedido;
            }
        }
        return null;
    }
    
}
