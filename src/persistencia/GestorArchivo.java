package persistencia;

import modelo.Producto;
import modelo.Categorias;
import modelo.Admin;
import java.io.*;
import java.util.*;

//Se encarga de la lectura y escritura en archivos CSV.
public class GestorArchivo {
    private String rutaArchivo;
    private static final String SEPARADOR = ",";

    public GestorArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public void guardarCatalogo(List<Producto> productos) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            for (Producto p : productos) {
                String linea = p.getId() + SEPARADOR +
                            p.getNombre() + SEPARADOR +
                            p.getPrecio() + SEPARADOR +
                            p.getStock() + SEPARADOR +
                            p.getCategoria().name() + SEPARADOR +  
                            p.getRutaImagen();
                bw.write(linea);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al guardar catálogo: " + e.getMessage());
        }
    }

    public List<Producto> cargarCatalogo() {
        List<Producto> productos = new ArrayList<>();
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) return productos; 

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] p = linea.split(SEPARADOR);
                
                //separa la fila por comas en el arreglo
                if (p.length == 6) {
                    int id = Integer.parseInt(p[0].trim());
                    String nombre = p[1].trim();
                    double precio = Double.parseDouble(p[2].trim());
                    int stock = Integer.parseInt(p[3].trim());
                    Categorias categoria = Categorias.valueOf(p[4].trim().toUpperCase());
                    String rutaImagen = p[5].trim();
                    
                    productos.add(new Producto(id, nombre, precio, stock, categoria, rutaImagen));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al cargar catálogo: " + e.getMessage());
        }

        return productos;
    }

    //guarda en el archivo de usuarios a las cuentas de administradores, se usa collection porque hashmap retorna eso
    public void guardarAdmin(Collection<Admin> admins) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            for (Admin a : admins) {//formato: usuario, contraseña
                String linea = a.getUsuario() + SEPARADOR + a.getContrasena();
                bw.write(linea);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al guardar admins: " + e.getMessage());
        }
    }
    //carga las credenciales de admin, se usa hashmap por su eficiencia
    public HashMap<String, Admin> cargarAdmins() {
        HashMap<String, Admin> admins = new HashMap<>();
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            return admins;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] p = linea.split(SEPARADOR);
                if (p.length >= 2) {
                    String user = p[0].trim();
                    String pass = p[1].trim();
                    admins.put(user, new Admin(user, pass));
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar admins: " + e.getMessage());
        }

        return admins;
    }
}