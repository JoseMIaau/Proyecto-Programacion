package modelo;

import java.util.ArrayList;
import java.util.List;
import persistencia.GestorArchivo;

public class GestorPedidos {
    private static GestorPedidos instancia;
    private List<Pedido> pedidos;
    private int siguienteNumero;
    private GestorArchivo GestorPedidos;

    private GestorPedidos(){
        gestorPedidos = new GestorArchivo("pedidos.csv");

        List<Compra> compras = Inventario.getInstancia().obtenerHistorialCompras();
        pedidos = gestorPedidos.cargarPedidos(compras);
        
        actualizarSiguienteNumero();
    }

    private void actualizarSiguienteNumero(){
        int mayorNumero = 999;

        for (Pedido pedido : pedidos){
            if (pedido.getNumeroPedido() > mayorNumero){
                mayorNumero = pedido.getNumeroPedido();
            }
        }
        siguienteNumero = mayorNumero + 1;
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
        gestorPedidos.guardarPedidos(pedidos);
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
        
        boolean cambiado = pedido.cambiarEstado(nuevoEstado);
        if(cambiado){
            gestorPedidos.guardarPedidos(pedidos);
        }
        return cambiado;
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
        boolean cancelado = pedido.marcarComoCancelado();
        if (cancelado){
            gestorPedidos.guardarPedidos(pedidos);
        }

        return cancelado;
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
    
    public boolean avanzarEstado(int numeroPedido){
        Pedido pedido = buscarPedido(numeroPedido);
        if (pedido == null){
            return false;
        }

        EstadoPedido siguienteEstado;

        switch (pedido.getEstado()){
            case PENDIENTE:
                siguienteEstado = EstadoPedido.EN_PREPARACION;
                break;
            case EN_PREPARACION:
                siguienteEstado = EstadoPedido.DESPACHADO;
                break;
            case DESPACHADO:
                siguienteEstado = EstadoPedido.ENTREGADO;
                break;
            default:
                return false;
        }
        return cambiarEstado(numeroPedido, siguienteEstado);
    }

}
