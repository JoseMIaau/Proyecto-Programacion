package modelo;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import persistencia.GestorArchivo;

/**
 * Clase principal del modelo Singleton
 * Centraliza la gestion de productos, carrito actual, usuarios y el historial de ventas.
 */
public class Inventario {
    private static Inventario instancia;
    
    private GestorArchivo gestorArchivo;
    private GestorArchivo gestorUsuario;
    
    private List<Producto> productos;
    private HashMap<String, Admin> usuariosAdmin;
    private List<Venta> historialVentas;
    
    private Admin adminActual;
    private Carrito carritoActual;

    private Inventario() {
        this.gestorArchivo = new GestorArchivo("inventario.csv");
        this.gestorUsuario = new GestorArchivo("usuarios.csv");
        
        this.productos = gestorArchivo.cargarCatalogo();
        this.usuariosAdmin = gestorUsuario.cargarAdmins();
        this.historialVentas = new ArrayList<>(); // TEMPORAL | HAY QUE CARGARLO DESDE UN CSV
        
        this.adminActual = null;
        this.carritoActual = new Carrito();     
        
        inicializarDatosDemoSiVacio();
        inicializarAdminPorDefecto();
    }

    public static Inventario getInstancia() {
        if (instancia == null) {
            instancia = new Inventario();
        }
        return instancia;
    }

    // --- METODOS DE CARGA DE DATOS DEMOS ---

    private void inicializarDatosDemoSiVacio() {
        if (productos.isEmpty()) {
            // FRUTAS Y VERDURAS
            productos.add(new Producto(1, "Plátano Granel (kg)", 1290.0, 45, Categorias.FRUTAS_Y_VERDURAS, null));
            productos.add(new Producto(2, "Manzana fancy (kg)", 1490.0, 30, Categorias.FRUTAS_Y_VERDURAS, null));
            productos.add(new Producto(3, "Papas (kg)", 990.0, 80, Categorias.FRUTAS_Y_VERDURAS, null));

            // LACTEOS
            productos.add(new Producto(4, "Leche Entera 1L", 1050.0, 60, Categorias.LACTEOS, null));
            productos.add(new Producto(5, "Yogurt Batido Frutilla", 350.0, 100, Categorias.LACTEOS, null));
            productos.add(new Producto(6, "Queso Laminado Mantecoso 250g", 2890.0, 25, Categorias.LACTEOS, null));

            // PANADERIA
            productos.add(new Producto(7, "Marraqueta (kg)", 1990.0, 40, Categorias.PANADERIA, null));
            productos.add(new Producto(8, "Pan Hallulla (kg)", 1990.0, 35, Categorias.PANADERIA, null));
            productos.add(new Producto(9, "Pan de Molde Blanco", 2390.0, 20, Categorias.PANADERIA, null));

            // CARNICERIA
            productos.add(new Producto(10, "Pechuga de Pollo Deshuezada (kg)", 4990.0, 15, Categorias.CARNICERIA, null));
            productos.add(new Producto(11, "Carnde de Vacuno Filete (kg)", 16990.0, 10, Categorias.CARNICERIA, null));
            productos.add(new Producto(12, "Costillar de Cerdo (kg)", 6790.0, 12, Categorias.CARNICERIA, null));

            // BEBIDAS
            productos.add(new Producto(13, "Pepsi zero 2.5L", 2000.0, 50, Categorias.BEBIDAS, null));
            productos.add(new Producto(14, "Jugo Néctar Durazno 1.5L", 1390.0, 30, Categorias.BEBIDAS, null));
            productos.add(new Producto(15, "Agua Mineral Sin Gas 1.5L", 890.0, 45, Categorias.BEBIDAS, null));

            // LIMPIEZA
            productos.add(new Producto(16, "Detergente Líquido 3L", 7490.0, 18, Categorias.LIMPIEZA, null));
            productos.add(new Producto(17, "Cloro Gel 900ml", 1690.0, 40, Categorias.LIMPIEZA, null));
            productos.add(new Producto(18, "Lavaloza Limón 750ml", 2190.0, 28, Categorias.LIMPIEZA, null));

            gestorArchivo.guardarCatalogo(productos);
        }
    }

    private void inicializarAdminPorDefecto() {
        if (usuariosAdmin.isEmpty()) {
            Admin prueba = new Admin("admin@supercurico.cl", "admin123");
            usuariosAdmin.put(prueba.getUsuario(), prueba);
            gestorUsuario.guardarAdmin(new ArrayList<>(usuariosAdmin.values()));
        }
    }

    // --- METODOS DE AUTENTICACION ---

    public Admin iniciarSesion(String usuario, String contrasena) {
        if (usuario == null || contrasena == null) return null;
        String userTrim = usuario.trim();
        String pass = contrasena.trim();

        if (usuariosAdmin.containsKey(userTrim)) {
            Admin admin = usuariosAdmin.get(userTrim);
            if (admin.getContrasena().equals(pass)) {
                this.adminActual = admin;
                return admin;
            }
        }
        return null;
    }

    public Admin getAdminActual() { return adminActual; }
    public List<Venta> getHistorialVentas() { return new ArrayList<>(historialVentas);}
    public Venta getUltimaVenta() {
    if (historialVentas.isEmpty()) {
        return null;
    }
    return historialVentas.get(historialVentas.size() - 1);
}

    // --- METODOS CRUD ---

    public int generarId() {
        int mayorId = 0;
        for (Producto producto : productos) {
            if (producto.getId() > mayorId) {
                mayorId = producto.getId();
            }
        }
        return mayorId + 1;
    }

    public boolean crearProducto(Producto producto) {
        if (producto == null || producto.getPrecio() < 0 || producto.getStock() < 0) {
            return false;
        }

         if(productoExistente(producto.getNombre())){
            return false;
        }

        if (producto.getId() <= 0 || buscarProducto(producto.getId()) != null) {
            producto.setId(generarId());
        }

        productos.add(producto);
        gestorArchivo.guardarCatalogo(productos);
        return true;
    }

        // Verifica que no exista otro producto con el mismo nombre
    public boolean productoExistente(String nombre){
        if(nombre == null){
            return false;
        }
        for(Producto producto : productos){
            if(producto.getNombre().trim().equalsIgnoreCase(nombre.trim())){
                return true;
            }
        }
        return false;
}
    
    public Producto buscarProducto(int id) {
        for (Producto p : productos) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }
    
    public List<Producto> leerProductos() {
        return new ArrayList<>(productos);
    }

    public boolean actualizarProducto(int id, double nuevoPrecio, int nuevoStock) {
        if (nuevoPrecio < 0 || nuevoStock < 0) return false;

        Producto producto = buscarProducto(id);
        if (producto != null) {
            producto.setPrecio(nuevoPrecio);
            producto.setStock(nuevoStock);
            gestorArchivo.guardarCatalogo(productos);
            return true;
        }
        return false;
    }

    public boolean eliminarProducto(int id) {
        Producto producto = buscarProducto(id);
        if (producto != null) {
            productos.remove(producto);
            gestorArchivo.guardarCatalogo(productos);
            return true;
        }
        return false;
    }

    public boolean vaciarInventario() {
        if (productos.isEmpty()) return false;
        productos.clear();
        gestorArchivo.guardarCatalogo(productos);
        return true;
    }

    // --- ACTUALIZACION INDIVIDUAL DE PRECIO Y STOCK ---

    public boolean actualizarPrecio(int id, double nuevoPrecio) {
        if (nuevoPrecio < 0) {
            return false;
        }

        Producto producto = buscarProducto(id);

        if (producto != null) {
            producto.setPrecio(nuevoPrecio);
            gestorArchivo.guardarCatalogo(productos);
            return true;
        }
        return false;
    }

    public boolean actualizarStock(int id, int nuevoStock) {
        if (nuevoStock < 0) {
            return false;
        }

        Producto producto = buscarProducto(id);

        if (producto != null) {
            producto.setStock(nuevoStock);
            gestorArchivo.guardarCatalogo(productos);
            return true;
        }
        return false;
    }

    //Aumenta el stock de un producto que ya existe
    public boolean agregarStock(int id, int cantidad) {
        if (cantidad <= 0) {
            return false;
        }

        Producto producto = buscarProducto(id);

        if (producto == null) {
            return false;
        }

        int nuevoStock = producto.getStock() + cantidad;
        producto.setStock(nuevoStock);

        gestorArchivo.guardarCatalogo(productos);
        return true;
    }

    // --- METODOS FRILTACION Y ESTADISTICAS ---

    public int obtenerStockDisponible(int idProducto) {
        Producto producto = buscarProducto(idProducto);
        if (producto == null) {
            return -1;
        }

        return producto.getStock();
    }

    public Producto obtenerProductoPorMenorStockInventario(){
        Producto productoMenor = null;
        
        //Recorre y filtra los productos
        for (Producto producto : productos){
            if(productoMenor== null || producto.getStock()<productoMenor.getStock()){
                    productoMenor = producto;
            }
        }
        return productoMenor;
    }

    public Producto obtenerProductoPorMenorStock(Categorias categoria){
        if (categoria == null){
            return null;
        }
        
        Producto productoMenor = null;
        
        //Recorre y filtra los productos
        for (Producto producto : productos){
            if(producto.getCategoria() == categoria){
                if(productoMenor== null || producto.getStock()<productoMenor.getStock()){
                    productoMenor = producto;
                }
            }
        }
        return productoMenor;
    }

    public List<Producto> filtrarPorCategoria(Categorias cat) {
        if (cat == null) return leerProductos();
        List<Producto> filtrados = new ArrayList<>();
        for (Producto p : productos) {
            if (p.getCategoria() == cat) filtrados.add(p);
        }
        return filtrados;
    }

    public List<Producto> filtrarPorPrecio(double precioMinimo, double precioMaximo) {
        List<Producto> productosFiltrados = new ArrayList<>();
        //validacion de negativos y que el minimo no sea mayor al maximo
        if (precioMinimo < 0 || precioMaximo < 0 || precioMinimo > precioMaximo) {
            return productosFiltrados;
        }

        for (Producto producto : productos) {
            if (producto.getPrecio() >= precioMinimo && producto.getPrecio() <= precioMaximo) {
                productosFiltrados.add(producto);
            }
        }

        return productosFiltrados;
    }

    public List<Producto> buscarPorNombre(String texto) {
        if (texto == null || texto.trim().isEmpty()) return leerProductos();
        
        List<Producto> lista = new ArrayList<>();
        String busqueda = texto.trim().toLowerCase();

        for (Producto p : productos) {
            if (p.getNombre().toLowerCase().contains(busqueda)) {
                lista.add(p);
            }
        }
        return lista;
    }

    public double calcularPromedioPorCategoria(Categorias categoria){
        if (categoria == null){
            return 0.0;
        }

        double sumaPrecio = 0.0;
        int cantidadProductos = 0;

        //Recorre los productos
        for (Producto producto : productos){
            if (producto.getCategoria() == categoria){
                sumaPrecio+=producto.getPrecio();
                cantidadProductos++;
            }
        }

        //Comprueba si es que no se encontró ningun producto
        if (cantidadProductos == 0){
            return 0.0;
        }

        return sumaPrecio/cantidadProductos; //Retorna el promedio
    }

    public double calculoValorTotalInventario() {
        double valorTotal = 0.0;
        for (Producto p : productos) {
            valorTotal += (p.getPrecio() * p.getStock());
        }
        return valorTotal;
    }

    // --- METODOS DE COMPRA Y CARRITO ---

    public Carrito getCarritoActual() { return carritoActual; }
    public List<ItemCarrito> getCarrito() { return carritoActual.getItems(); }

    /**
     * Agrega un producto al carrito y actualiza el stock de forma temporal
     * Si el producto ya esta en el carrito, se suma a la cantidad existente.
     */
    public boolean agregarAlCarrito(Producto p, double cantidad) {
        if (p == null || cantidad <= 0 || p.getStock() < cantidad) return false;

        p.setStock((int) (p.getStock() - cantidad));
        boolean existe = false;
        
        for (ItemCarrito item : carritoActual.getItems()) {
            if (item.getProducto().getId() == p.getId()) {
                item.setCantidad(item.getCantidad() + cantidad);
                existe = true;
                break;
            }
        }
        if (!existe) {
            carritoActual.getItems().add(new ItemCarrito(p, cantidad));
        }
        return true;
    }
    
    public void eliminarDelCarrito(int idProducto) {
        ItemCarrito aEliminar = null;
        for (ItemCarrito item : carritoActual.getItems()) {
            if (item.getProducto().getId() == idProducto) {
                aEliminar = item;
                break;
            }
        }
        if (aEliminar != null) {
            int nuevoStock = (int) (aEliminar.getProducto().getStock() + aEliminar.getCantidad());
            aEliminar.getProducto().setStock(nuevoStock);
            carritoActual.getItems().remove(aEliminar);
        }
    }

    public void vaciarCarrito() {
        for (ItemCarrito item : carritoActual.getItems()) {
            int nuevoStock = (int) (item.getProducto().getStock() + item.getCantidad());
            item.getProducto().setStock(nuevoStock);
        }
        carritoActual.limpiar();
    }

    public double calcularSubtotal() {
        double subtotal = 0.0;
        for (ItemCarrito item : carritoActual.getItems()) {
            subtotal += item.getSubtotal();
        }
        return subtotal;
    }

    public double calcularIVA() { return calcularSubtotal() * 0.19; }
    public double calcularTotalCarrito() { return calcularSubtotal() + calcularIVA(); }

    /**
     * Finaliza la transaccion, crea la instancia de Venta para el historial
     * y limpia el carrito actual.
     */
    public boolean procesarCompra() {
        if (carritoActual.estaVacio()) return false;

        Venta nuevaVenta = new Venta(
            historialVentas.size() + 1,
            java.time.LocalDateTime.now(),
            "Invitado",
            carritoActual.getItems(),
            calcularSubtotal(),
            calcularIVA(),
            calcularTotalCarrito()
        );
        
        historialVentas.add(nuevaVenta);
        nuevaVenta.imprimirVenta(); // TEMPORAL | impresion en consola
        carritoActual.limpiar();
        return true;
    }
}