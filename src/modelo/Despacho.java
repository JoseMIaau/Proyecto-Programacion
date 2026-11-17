package modelo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Despacho {
    private boolean retiroEnTienda;
    private Direccion direccion;
    private double costoEnvio;
    private String fechaEstimada;

    //Retiro en tienda
    public Despacho() {
        this.retiroEnTienda = true;
        this.direccion = null;
        this.costoEnvio = 0.0;

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.fechaEstimada = LocalDate.now().format(formato) + " (Retiro en tienda)";
    }

    //Envio a domicilio
    public Despacho(Direccion direccion, double costoEnvio, int diasHabiles) {
        this.retiroEnTienda = false;
        this.direccion = direccion;

        if (costoEnvio < 0) {
            this.costoEnvio = 0.0;
        } else {
            this.costoEnvio = costoEnvio;
        }

        this.fechaEstimada = calcularFechaEntrega(diasHabiles);
    }

    public static String calcularFechaEntrega(int diasHabiles) {
        LocalDate fecha = LocalDate.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (diasHabiles == 0) {
            //saltarse sabado y domingo pasandolo a lunes
            while (fecha.getDayOfWeek() == DayOfWeek.SATURDAY || fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
                fecha = fecha.plusDays(1);
            }
            return fecha.format(formato);
        }

        int diasContados = 0;
        while (diasContados < diasHabiles) {
            fecha = fecha.plusDays(1);
            DayOfWeek dia = fecha.getDayOfWeek();

            if (dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY) {
                diasContados++;
            }
        }

        return fecha.format(formato);
    }

    public boolean esRetiroEnTienda() {
        return retiroEnTienda;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public double getCostoEnvio() {
        return costoEnvio;
    }

    public String getFechaEstimada() {
        return fechaEstimada;
    }

    public String getModalidadTexto() {
        if (retiroEnTienda) {
            return "Retiro en tienda";
        } else {
            return "Despacho a domicilio";
        }
    }
}