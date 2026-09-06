package modelo;

public class Admin {
    private String usuario;
    private String contrasena;

    public Admin(String usuario, String contrasena) {
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    // Validar el inicio de sesion en el login.
    public boolean validarCredenciales(String usuarioIngresado, String contrasenaIngresada){
        if (usuarioIngresado == null || contrasenaIngresada == null){
            return false;
        }
        return usuario.equals(usuarioIngresado) && contrasena.equals(contrasenaIngresada);
    }
}