package modelo;

import java.time.LocalDateTime;

public class Pedido {
    private int numeroPedido;
    private Compra compra;
    private EstadoPedido estado;
    private String idCliente;
    private LocalDateTime fechaCreacion;

    public Pedido(int numeroPedido, Compra compra, String idCliente){
        this.numeroPedido = numeroPedido;
        this.compra = compra;
        this.idCliente = idCliente;
        this.estado = EstadoPedido.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    public int getNumeroPedido(){
        return numeroPedido;
    }

    public Compra getCompra(){
        return compra;
    }

    public EstadoPedido getEstado(){
        return estado;
    }

    public String getIdCliente(){
        return idCliente;
    }
    
    public LocalDateTime getfechaCreacion(){
        return fechaCreacion;
    }

    public boolean cambiarEstado (EstadoPedido nuevoEstado){
        if (nuevoEstado == null){
            return false;
        }

        if (estado == EstadoPedido.PENDIENTE && nuevoEstado == EstadoPedido.EN_PREPARACION){
            estado = nuevoEstado;
            return true;
        }

        if (estado == EstadoPedido.EN_PREPARACION && nuevoEstado == EstadoPedido.DESPACHADO){
            estado = nuevoEstado;
            return true;
        }

        if (estado == EstadoPedido.DESPACHADO && nuevoEstado == EstadoPedido.ENTREGADO){
            estado = nuevoEstado;
            return true;
        }
        return false;
    }
    
    public boolean puedeCancelarse(){
        return estado == EstadoPedido.PENDIENTE || estado == EstadoPedido.EN_PREPARACION;
    }

    public boolean marcarComoCancelado(){
        if(!puedeCancelarse()){
            return false;
        }

        estado=EstadoPedido.CANCELADO;
        return true;
    }
}
