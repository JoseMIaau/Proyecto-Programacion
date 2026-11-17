package modelo;

public class Direccion {
    private String nombreReceptor;
    private String calleNumero;
    private int zona;

    public Direccion(String nombreReceptor, String calleNumero, int zona) {
        if (nombreReceptor == null) {
            this.nombreReceptor = "";
        } else {
            this.nombreReceptor = nombreReceptor.trim();
        }

        if (calleNumero == null) {
            this.calleNumero = "";
        } else {
            this.calleNumero = calleNumero.trim();
        }

        this.zona = zona;
    }

    public boolean estaCompleta() {
        if (nombreReceptor.isEmpty()) {
            return false;
        }
        if (calleNumero.isEmpty()) {
            return false;
        }
        if (zona <= 0) {
            return false;
        }
        return true;
    }

    public String getNombreReceptor() {
        return nombreReceptor;
    }

    public String getCalleNumero() {
        return calleNumero;
    }

    public int getZona() {
        return zona;
    }

    @Override
    public String toString() {
        return calleNumero + " (Zona " + zona + ", Curicó) - Recibe: " + nombreReceptor;
    }
}