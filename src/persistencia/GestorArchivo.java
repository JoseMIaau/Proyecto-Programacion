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
import modelo.ClienteRegistrado;
import modelo.Compra;
import modelo.DetalleCompra;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

    
    //HISTORIAL DE COMPRAS

    private String codificarTexto(String texto) {
        return Base64.getEncoder().encodeToString(
                (texto == null ? "" : texto).getBytes(StandardCharsets.UTF_8)
        );
    }

    private String decodificarTexto(String texto) {
        return new String(
                Base64.getDecoder().decode(texto),
                StandardCharsets.UTF_8
        );
    }

    //Cargar los clientes
    public List<ClienteRegistrado> cargarClientes() {
        List<ClienteRegistrado> clientes = new ArrayList<>();
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            return clientes;
        }

        try (BufferedReader br = new BufferedReader(
                new FileReader(rutaArchivo))) {

            String linea;

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] p = linea.split("\\|", -1);

                if (p.length == 2) {
                    clientes.add(new ClienteRegistrado(
                            decodificarTexto(p[0]),
                            decodificarTexto(p[1])
                    ));
                }
            }

        } catch (IOException | IllegalArgumentException e) {
            System.err.println(
                    "Error al cargar clientes: " + e.getMessage()
            );
        }

        return clientes;
    }

    //guardarlos clientes en el archivo
    public void guardarClientes(Collection<ClienteRegistrado> clientes) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(rutaArchivo))) {

            for (ClienteRegistrado cliente : clientes) {
                String linea =
                        codificarTexto(cliente.getRun()) + "|" +
                        codificarTexto(cliente.getNombre());

                bw.write(linea);
                bw.newLine();
            }

        } catch (IOException e) {
            System.err.println(
                    "Error al guardar clientes: " + e.getMessage()
            );
        }
    }

    //cargar el historial de las compras
    public List<Compra> cargarCompras() {
        List<Compra> compras = new ArrayList<>();
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) {
            return compras;
        }

        try (BufferedReader br = new BufferedReader(
                new FileReader(rutaArchivo))) {

            String linea;

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] p = linea.split("\\|", -1);

                if (p.length < 7) continue;

                String run = decodificarTexto(p[0]);
                String nombre = decodificarTexto(p[1]);
                String fecha = decodificarTexto(p[2]);

                double subtotal = Double.parseDouble(p[3]);
                double iva = Double.parseDouble(p[4]);
                double total = Double.parseDouble(p[5]);

                List<DetalleCompra> detalles = new ArrayList<>();

                for (int i = 6; i < p.length; i++) {
                    if (p[i].isEmpty()) continue;

                    String[] d = p[i].split(",", -1);

                    if (d.length != 5) continue;

                    detalles.add(new DetalleCompra(
                            Integer.parseInt(d[0]),
                            decodificarTexto(d[1]),
                            Double.parseDouble(d[2]),
                            Double.parseDouble(d[3]),
                            Double.parseDouble(d[4])
                    ));
                }

                compras.add(new Compra(
                        run, nombre, fecha,
                        subtotal, iva, total, detalles
                ));
            }

        } catch (IOException | IllegalArgumentException e) {
            System.err.println(
                    "Error al cargar compras: " + e.getMessage()
            );
        }

        return compras;
    }

    //guardar historial
    public void guardarCompras(Collection<Compra> compras) {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter(rutaArchivo))) {

            for (Compra compra : compras) {
                StringBuilder linea = new StringBuilder();

                linea.append(codificarTexto(compra.getRunCliente())).append("|")
                     .append(codificarTexto(compra.getNombreCliente())).append("|")
                     .append(codificarTexto(compra.getFecha())).append("|")
                     .append(compra.getSubtotal()).append("|")
                     .append(compra.getIva()).append("|")
                     .append(compra.getTotal()).append("|");

                boolean primero = true;

                for (DetalleCompra detalle : compra.getDetalles()) {
                    if (!primero) {
                        linea.append("|");
                    }

                    linea.append(detalle.getIdProducto()).append(",")
                         .append(codificarTexto(detalle.getNombreProducto())).append(",")
                         .append(detalle.getCantidad()).append(",")
                         .append(detalle.getPrecioUnitario()).append(",")
                         .append(detalle.getSubtotal());

                    primero = false;
                }

                bw.write(linea.toString());
                bw.newLine();
            }

        } catch (IOException e) {
            System.err.println(
                    "Error al guardar compras: " + e.getMessage()
            );
        }
    }


}
