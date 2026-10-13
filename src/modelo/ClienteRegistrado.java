package modelo;

public class ClienteRegistrado {

private String run;
private String nombreCompleto;

public ClienteRegistrado(String run, String nombreCompleto) {
this.run = run;
this.nombreCompleto = nombreCompleto;
}

public String getRun() {
return run;
}

public void setRun(String run) {
this.run = run;
}

public String getNombreCompleto() {
return nombreCompleto;
}

public void setNombreCompleto(String nombreCompleto) {
this.nombreCompleto = nombreCompleto;
}
}