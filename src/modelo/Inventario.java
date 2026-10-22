package modelo;

import persistencia.GestorArchivo;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Clase principal del modelo Singleton
 * Centraliza la gestion de productos, carrito actual, usuarios y el historial de ventas.
 */
public class Inventario {
    private static Inventario instancia;
    private List<Producto> productos;
    private GestorArchivo gestorArchivo;
    private GestorArchivo gestorUsuario;

    // Historial y Clientes Mauri y Javi
    private GestorArchivo gestorClientes;
    private GestorArchivo gestorCompras;
    private final LinkedHashMap<String, ClienteRegistrado> clientesRegistrados = new LinkedHashMap<>();
    private final List<Compra> historialCompras = new ArrayList<>();

    private HashMap<String, Admin> usuariosAdmin;
    private Admin adminActual;
    //Multicarrito
    private final HashMap<String, Carrito> carritosActivos = new HashMap<>();
    private String idClienteActual = "Cliente_" + java.util.UUID.randomUUID().toString().substring(0, 6);

    private Inventario() {
        this.gestorArchivo = new GestorArchivo("inventario.csv");
        this.gestorUsuario = new GestorArchivo("usuarios.csv");
        this.productos = gestorArchivo.cargarCatalogo();
        this.usuariosAdmin = gestorUsuario.cargarAdmins();
        
        this.gestorClientes = new GestorArchivo("clientes.csv");
        this.gestorCompras = new GestorArchivo("compras.csv");

        for (ClienteRegistrado cliente : gestorClientes.cargarClientes()) {
            clientesRegistrados.put(cliente.getRun(), cliente);
        }
        historialCompras.addAll(gestorCompras.cargarCompras());
        this.adminActual = null;
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
            productos.add(new Producto(1, "Plátano Granel (kg)", 1290.0, 45, Categorias.FRUTAS_Y_VERDURAS, "src/imagenes/ImagenesProductos/platano_granel.png"));
            productos.add(new Producto(2, "Manzana fancy (kg)", 1490.0, 30, Categorias.FRUTAS_Y_VERDURAS, "src/imagenes/ImagenesProductos/manzana_fancy.png"));
            productos.add(new Producto(3, "Papas (kg)", 990.0, 80, Categorias.FRUTAS_Y_VERDURAS, "src/imagenes/ImagenesProductos/papas.png"));

            // LACTEOS
            productos.add(new Producto(4, "Leche Entera 1L", 1050.0, 60, Categorias.LACTEOS, "src/imagenes/ImagenesProductos/leche_entera.png"));
            productos.add(new Producto(5, "Yogurt Batido Frutilla", 350.0, 100, Categorias.LACTEOS, "src/imagenes/ImagenesProductos/yogurt_batido_frutilla.png"));
            productos.add(new Producto(6, "Queso Laminado Mantecoso 250g", 2890.0, 25, Categorias.LACTEOS, "src/imagenes/ImagenesProductos/queso_laminado_250.png"));

            // PANADERIA
            productos.add(new Producto(7, "Marraqueta (kg)", 1990.0, 40, Categorias.PANADERIA, "src/imagenes/ImagenesProductos/marraqueta.png"));
            productos.add(new Producto(8, "Pan Hallulla (kg)", 1990.0, 35, Categorias.PANADERIA, "src/imagenes/ImagenesProductos/hallula.png"));
            productos.add(new Producto(9, "Pan de Molde Blanco", 2390.0, 20, Categorias.PANADERIA, "src/imagenes/ImagenesProductos/pan_molde_blanco.png"));

            // CARNICERIA
            productos.add(new Producto(10, "Pechuga de Pollo Deshuezada (kg)", 4990.0, 15, Categorias.CARNICERIA, "src/imagenes/ImagenesProductos/pechuga_pollo_deshuezada.png"));
            productos.add(new Producto(11, "Carne de Vacuno Filete (kg)", 16990.0, 10, Categorias.CARNICERIA, "src/imagenes/ImagenesProductos/filete_vacuno.png"));
            productos.add(new Producto(12, "Costillar de Cerdo (kg)", 6790.0, 12, Categorias.CARNICERIA, "src/imagenes/ImagenesProductos/costillar_cerdo.png"));

            // BEBIDAS
            productos.add(new Producto(13, "Pepsi zero 2.5L", 2000.0, 50, Categorias.BEBIDAS, "src/imagenes/ImagenesProductos/pepsi_zero.png"));
            productos.add(new Producto(14, "Jugo Néctar Durazno 1.5L", 1390.0, 30, Categorias.BEBIDAS, "src/imagenes/ImagenesProductos/jugo_durazno.png"));
            productos.add(new Producto(15, "Agua Mineral Sin Gas 1.6L", 890.0, 45, Categorias.BEBIDAS, "src/imagenes/ImagenesProductos/mineral_sin_gas.png"));

            // LIMPIEZA
            productos.add(new Producto(16, "Detergente Líquido 3L", 7490.0, 18, Categorias.LIMPIEZA, "src/imagenes/ImagenesProductos/detergente_liquido.png"));
            productos.add(new Producto(17, "Cloro Gel 900ml", 1690.0, 40, Categorias.LIMPIEZA, "src/imagenes/ImagenesProductos/clorogel.png"));
            productos.add(new Producto(18, "Lavaloza Limón 750ml", 2190.0, 28, Categorias.LIMPIEZA, "src/imagenes/ImagenesProductos/lavaloza.png"));

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

    public void setSesionActual(String idCliente) {
        this.idClienteActual = idCliente;
    }

    public Carrito getCarritoActual() {
        if (!carritosActivos.containsKey(idClienteActual)) {
            carritosActivos.put(idClienteActual, new Carrito(idClienteActual));
        }
        return carritosActivos.get(idClienteActual);
    }
    
    public List<ItemCarrito> getCarrito() {
        return getCarritoActual().getItems();
    }

    /**
     * Agrega un producto al carrito y actualiza el stock de forma temporal
     * Si el producto ya esta en el carrito, se suma a la cantidad existente.
     */
    public boolean agregarAlCarrito(Producto p, double cantidad) {
        if (p == null || cantidad <= 0 || p.getStock() < cantidad) return false;

        p.setStock((int) (p.getStock() - cantidad));
        Carrito c = getCarritoActual();
        boolean existe = false; 
        
        for (ItemCarrito item : c.getItems()) {
            if (item.getProducto().getId() == p.getId()) {
                item.setCantidad(item.getCantidad() + cantidad);
                existe = true;
                break;
            }
        }
        if (!existe) c.getItems().add(new ItemCarrito(p, cantidad));
        return true;
    }
    
    public void eliminarDelCarrito(int idProducto) {
        Carrito c = getCarritoActual();
        ItemCarrito aEliminar = null;
        for (ItemCarrito item : c.getItems()) {
            if (item.getProducto().getId() == idProducto) {
                aEliminar = item;
                break;
            }
        }
        if (aEliminar != null) {
            int nuevoStock = (int) (aEliminar.getProducto().getStock() + aEliminar.getCantidad());
            aEliminar.getProducto().setStock(nuevoStock);
            c.getItems().remove(aEliminar);
        }
    }

    public void vaciarCarrito() {
        Carrito c = getCarritoActual();
        for (ItemCarrito item : c.getItems()) {
            int nuevoStock = (int) (item.getProducto().getStock() + item.getCantidad());
            item.getProducto().setStock(nuevoStock);
        }
        c.limpiar();
    }

    public double calcularSubtotal() {
        double subtotal = 0.0;
        for (ItemCarrito item : getCarritoActual().getItems()) {
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
    public boolean procesarCompra(String run, String nombre) {
        Carrito c = getCarritoActual();
        if (c.estaVacio()) return false;

        run = run == null ? "" : run.trim().toUpperCase();
        nombre = nombre == null ? "" : nombre.trim();

        if (run.isEmpty() || nombre.isEmpty()) return false;

        ClienteRegistrado cliente = clientesRegistrados.get(run);
        if (cliente == null) {
            cliente = new ClienteRegistrado(run, nombre);
            clientesRegistrados.put(run, cliente);
        } else {
            cliente.setNombre(nombre);
        }
        gestorClientes.guardarClientes(clientesRegistrados.values());

        List<DetalleCompra> detalles = new ArrayList<>();
        for (ItemCarrito item : c.getItems()) {
            detalles.add(new DetalleCompra(
                item.getProducto().getId(), 
                item.getProducto().getNombre(), 
                item.getCantidad(), 
                item.getProducto().getPrecio(), 
                item.getSubtotal()
            ));
        }

        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        int nuevoIdCompra = historialCompras.size() + 1;
        Compra compra = new Compra(nuevoIdCompra, run, nombre, fecha, calcularSubtotal(), calcularIVA(), calcularTotalCarrito(), detalles);

        historialCompras.add(compra);
        gestorCompras.guardarCompras(historialCompras);

        gestorArchivo.guardarCatalogo(productos);
        c.limpiar(); 
        return true;
    }
    
    // --- METODOS DE HISTORIAL ---
    private void sincronizarHistoriales() {
        clientesRegistrados.clear();
        for (ClienteRegistrado cliente : gestorClientes.cargarClientes()) {
            clientesRegistrados.put(cliente.getRun(), cliente);
        }
        historialCompras.clear();
        historialCompras.addAll(gestorCompras.cargarCompras());
    }

    public List<ClienteRegistrado> obtenerClientesRegistrados() {
        sincronizarHistoriales();
        return new ArrayList<>(clientesRegistrados.values());
    }

    public List<Compra> obtenerHistorialCompras() {
        sincronizarHistoriales();
        return new ArrayList<>(historialCompras);
    }

    public Compra obtenerUltimaCompra(){
        sincronizarHistoriales();
        if(historialCompras.isEmpty()){
            return null;
        }
        return historialCompras.get(historialCompras.size() - 1);
    }

    // Busca un cliente registrado mediante su RUN
    public ClienteRegistrado buscarClientePorRun(String run) {
        sincronizarHistoriales();
        if (run == null) {
            return null;
        }

        return clientesRegistrados.get(run.trim().toUpperCase());
    }

    // Busca todas las compras realizadas por un cliente según su RUN
    public List<Compra> buscarComprasPorRun(String run) {
        sincronizarHistoriales();
        List<Compra> comprasCliente = new ArrayList<>();

        if (run == null || run.trim().isEmpty()) {
            return comprasCliente;
        }

        String runBuscado = run.trim().toUpperCase();

        for (Compra compra : historialCompras) {
            if (compra.getRunCliente().equalsIgnoreCase(runBuscado)) {
                comprasCliente.add(compra);
            }
        }

        return comprasCliente;
    }



}
