package modelo;

public class TarifaEnvio {
    private int zona;
    private String descripcion;
    private double costo;
    private int diasHabiles;

    public TarifaEnvio(int zona, String descripcion, double costo, int diasHabiles) {
        this.zona = zona;

        if (descripcion == null) {
            this.descripcion = "Zona " + zona;
        } else {
            this.descripcion = descripcion.trim();
        }

        if (costo < 0) {
            this.costo = 0;
        } else {
            this.costo = costo;
        }

        if (diasHabiles < 0) {
            this.diasHabiles = 0;
        } else {
            this.diasHabiles = diasHabiles;
        }
    }

    public int getZona() {
        return zona;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getCosto() {
        return costo;
    }

    public int getDiasHabiles() {
        return diasHabiles;
    }

    public void setDescripcion(String descripcion) {
        if (descripcion != null && !descripcion.trim().isEmpty()) {
            this.descripcion = descripcion.trim();
        }
    }

    public void setCosto(double costo) {
        if (costo >= 0) {
            this.costo = costo;
        }
    }

    public void setDiasHabiles(int diasHabiles) {
        if (diasHabiles >= 0) {
            this.diasHabiles = diasHabiles;
        }
    }

    @Override
    public String toString() {
        String plazo;
        if (diasHabiles == 0) {
            plazo = "Mismo día";
        } else if (diasHabiles == 1) {
            plazo = "1 día hábil";
        } else {
            plazo = diasHabiles + " días hábiles";
        }
        return "Zona " + zona + " (" + descripcion + ") - $" + (int) costo + " [" + plazo + "]";
    }
}