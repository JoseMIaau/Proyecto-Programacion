package persistencia;
import modelo.Producto;
import modelo.Categorias;
import modelo.Admin;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class GestorArchivo {
    private String rutaArchivo;
    private static final String separador = ",";

    public GestorArchivo(String rutaArchivo){
        this.rutaArchivo = rutaArchivo;
    }
    //guarda la lista de productos sobreescribiendo el archivo csv, con el try se asegura que se cierre automaticamente la lectura del archivo
    public void guardarCatalogo(List<Producto> productos){
        //buffered writer por optimizacion
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))){
            for (Producto p : productos){//formato: id,nombre,precio,stock,categoria
                String linea = p.getId()+separador+
                                p.getNombre()+separador+
                                p.getPrecio()+separador+
                                p.getStock()+separador+
                                p.getCategoria().name();;//convierte a string
                bw.write(linea);
                bw.newLine();//salto de linea       
            }


        } catch (IOException e){
            System.err.println("Error al guardar en el archivo: " + e.getMessage());
        }
    }
    //lee el archivo csv de inventario y retorna la lista con los productos que leyó
    public List<Producto> cargarCatalogo(){
        List<Producto> productos = new ArrayList<>();
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {//en caso de que no haya archivo se retorna la lista vacia
            return productos; 
        }
        //de nuevo buffered se utiliza por optimizacion
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))){
            String linea;
            while ((linea = br.readLine()) != null) {//se lee cada fila del archivo hasta llegar al final
                if (linea.trim().isEmpty()) continue;// ignora alguna linea vacia que se haya colado
                //separa la fila por comas en el arreglo
                String[] p = linea.split(separador);
                if (p.length == 5) {//si estan los 5 datos, convierte cada texto en su tipo correspondiente
                    int id = Integer.parseInt(p[0].trim());
                    String nombre = p[1].trim();
                    double precio = Double.parseDouble(p[2].trim());
                    int stock = Integer.parseInt(p[3].trim());
                    Categorias categoria = Categorias.valueOf(p[4].trim().toUpperCase());
                    //crea el objeto y se guarda en la lista
                    productos.add(new Producto(id, nombre, precio, stock, categoria));
                }
            }
        }   catch (IOException | NumberFormatException e) {
            System.err.println("Error al cargar el archivo: " + e.getMessage());
        }

        return productos;
    }
    //guarda en el archivo de usuarios a las cuentas de administradores, se usa collection porque hashmap retorna eso
    public void guardarAdmin(Collection<Admin> admins) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            for (Admin a : admins) {//formato: usuario, contraseña
                String linea = a.getUsuario() + separador + a.getContrasena();
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
                String[] p = linea.split(separador);
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
