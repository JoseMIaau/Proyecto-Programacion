
package modelo;

public class ClienteRegistrado {
    private final String run;
    private String nombre;

    public ClienteRegistrado(String run, String nombre) {
        this.run = run == null ? "" : run.trim();
        this.nombre = nombre == null ? "" : nombre.trim();
    }

    public String getRun() {
        return run;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre == null ? "" : nombre.trim();
    }
}
