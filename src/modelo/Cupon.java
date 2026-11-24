package modelo;

import java.time.LocalDate;
import java.io.Serializable;


public class Cupon implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public enum TipoDescuento{
        PORCENTAJE,
        MONTO_FIJO
    }


private String codigo;
private TipoDescuento tipo;
private double valor;
private LocalDate fechaVencimiento;
private int usosMaximos;
private int usosRealizados;

public Cupon(String codigo, TipoDescuento tipo, double valor, LocalDate fechaVencimiento, int usosMaximos) {
    if (codigo == null || codigo.trim().isEmpty()) {
        throw new IllegalArgumentException("El codigo no puede estar vacio");
    }

    if (tipo == null || fechaVencimiento == null) {
    throw new IllegalArgumentException("Porfavor ingresar tipo y fecha");
        }

    if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException("El descuento debe ser positivo");
       
        }

     if (tipo == TipoDescuento.PORCENTAJE && valor > 100) {
    throw new IllegalArgumentException("El porcentaje no puede superar 100");
        }

    if (usosMaximos <= 0) {
    throw new IllegalArgumentException("Los usos maximos deben ser mayores que cero");
        }
    

    this.codigo = codigo.trim().toUpperCase();
    this.tipo = tipo;
    this.valor = valor;
    this.fechaVencimiento= fechaVencimiento;
    this.usosMaximos= usosMaximos;
    this.usosRealizados= 0;
}     
    
    public boolean estaVigente(){
        return !LocalDate.now().isAfter(fechaVencimiento);

    }
    
    public boolean tieneUsosDisponibles(){
        return usosRealizados < usosMaximos;
    }

    public boolean esValido(){
        return estaVigente() && tieneUsosDisponibles();
    }
 
    public double calcularDescuento(double subtotal) {
        if (!esValido() || !Double.isFinite(subtotal) || subtotal <= 0) {
            return 0;
        }

    double descuento;

        if (tipo == TipoDescuento.PORCENTAJE) {
            descuento = subtotal * (valor / 100.0);
        } else {
            descuento = valor;
        }

        return Math.min(descuento, subtotal);
    }
    

    public boolean registrarUso(){
        if (!esValido()) {
            return false;
        }
         usosRealizados++;
         return true;
     }
    
     public String getCodigo() {
        return codigo;
    }

    public TipoDescuento getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public int getUsosMaximos() {
        return usosMaximos;
    }

    public int getUsosRealizados() {
        return usosRealizados;
    }

    public int getUsosRestantes() {
        return usosMaximos - usosRealizados;
    }

    

    
    public void setUsosRealizados(int usosRealizados) {
        if (usosRealizados < 0 || usosRealizados > usosMaximos) {
            throw new IllegalArgumentException("Cantidad de usos invalida");
        }

        this.usosRealizados = usosRealizados;
    }
}


    




