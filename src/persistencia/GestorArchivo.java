package persistencia;

import modelo.Producto;
import modelo.Categorias;
import modelo.ClienteRegistrado;
import modelo.Compra;
import modelo.DetalleCompra;
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

//Se encarga de la lectura y escritura en archivos CSV.
public class GestorArchivo {
    private String rutaArchivo;
    private static final String separador = ",";

    public GestorArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    // ---  PRODUCTOS ---
    // Formato: id,nombre,precio,stock,categoria,rutaImagen
    public void guardarCatalogo(List<Producto> productos) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            for (Producto p : productos) {
                String linea = p.getId() + separador +
                        p.getNombre() + separador +
                        p.getPrecio() + separador +
                        p.getStock() + separador +
                        p.getCategoria().name() + separador +
                        (p.getRutaImagen() != null ? p.getRutaImagen() : "");
                bw.write(linea);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al guardar inventario: " + e.getMessage());
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
                String[] p = linea.split(separador, -1);
                if (p.length >= 5) {
                    int id = Integer.parseInt(p[0].trim());
                    String nombre = p[1].trim();
                    double precio = Double.parseDouble(p[2].trim());
                    int stock = Integer.parseInt(p[3].trim());
                    Categorias categoria = Categorias.valueOf(p[4].trim().toUpperCase());
                    String rutaImg = (p.length > 5) ? p[5].trim() : "";
                    
                    productos.add(new Producto(id, nombre, precio, stock, categoria, rutaImg));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al cargar inventario: " + e.getMessage());
        }
        return productos;
    }

    // ---  ADMINS ---
    //guarda en el archivo de usuarios a las cuentas de administradores, se usa collection porque hashmap retorna eso
    public void guardarAdmin(Collection<Admin> admins) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            for (Admin a : admins) {
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

        if (!archivo.exists()) return admins;

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

    // ---  HISTORIAL CLIENTES ---
    public void guardarClientes(Collection<ClienteRegistrado> clientes) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            for (ClienteRegistrado cliente : clientes) {
                String linea = cliente.getRun() + "|" + cliente.getNombre();
                bw.write(linea);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al guardar clientes: " + e.getMessage());
        }
    }

    public List<ClienteRegistrado> cargarClientes() {
        List<ClienteRegistrado> clientes = new ArrayList<>();
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) return clientes;

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] p = linea.split("\\|", -1);
                if (p.length == 2) {
                    clientes.add(new ClienteRegistrado(p[0].trim(), p[1].trim()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar clientes: " + e.getMessage());
        }
        return clientes;
    }

    // --- HISTORIAL DE COMPRAS ---
    public void guardarCompras(Collection<Compra> compras) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            for (Compra compra : compras) {
                StringBuilder linea = new StringBuilder();

                linea.append(compra.getidCompra()).append("|")
                    .append(compra.getRunCliente()).append("|")
                    .append(compra.getNombreCliente()).append("|")
                    .append(compra.getFecha()).append("|")
                    .append(compra.getSubtotal()).append("|")
                    .append(compra.getIva()).append("|")
                    .append(compra.getTotal()).append("|");

                boolean primero = true;
                for (DetalleCompra detalle : compra.getDetalles()) {
                    if (!primero) linea.append("|");
                    linea.append(detalle.getIdProducto()).append(",")
                        .append(detalle.getNombreProducto()).append(",")
                        .append(detalle.getCantidad()).append(",")
                        .append(detalle.getPrecioUnitario()).append(",")
                        .append(detalle.getSubtotal());
                    primero = false;
                }

                bw.write(linea.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al guardar compras: " + e.getMessage());
        }
    }

    public List<Compra> cargarCompras() {
        List<Compra> compras = new ArrayList<>();
        File archivo = new File(rutaArchivo);

        if (!archivo.exists()) return compras;

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] p = linea.split("\\|", -1);
                if (p.length < 8) continue; //si no tiene totales y al menos 1 producto, saltar

                int idCompra = Integer.parseInt(p[0].trim());
                String run = p[1].trim();
                String nombre = p[2].trim();
                String fecha = p[3].trim();
                double subtotal = Double.parseDouble(p[4]);
                double iva = Double.parseDouble(p[5]);
                double total = Double.parseDouble(p[6]);

                List<DetalleCompra> detalles = new ArrayList<>();

                for (int i = 7; i < p.length; i++) {
                    if (p[i].isEmpty()) continue;
                    String[] d = p[i].split(",", -1);
                    if (d.length != 5) continue;

                    detalles.add(new DetalleCompra(
                            Integer.parseInt(d[0]),
                            d[1].trim(),
                            Double.parseDouble(d[2]),
                            Double.parseDouble(d[3]),
                            Double.parseDouble(d[4])
                    ));
                }

                compras.add(new Compra(idCompra, run, nombre, fecha, subtotal, iva, total, detalles));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al cargar compras: " + e.getMessage());
        }
        return compras;
    }
}
