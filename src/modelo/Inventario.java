package modelo;
import java.util.HashMap;
import java.util.ArrayList;
import persistencia.GestorArchivo;
import java.util.List;

public class Inventario {
    private static Inventario instancia;
    private List<Producto> productos;
    private List<ItemCarrito> carrito;
    private GestorArchivo gestorArchivo;
    private GestorArchivo gestorUsuario;
    private HashMap<String, Admin> usuariosAdmin;
    private Admin adminActual;

    private Inventario() {
        this.gestorArchivo = new GestorArchivo("inventario.csv");
        this.gestorUsuario = new GestorArchivo("usuarios.csv");
        this.productos = gestorArchivo.cargarCatalogo();
        this.carrito = new ArrayList<>();
        this.usuariosAdmin = gestorUsuario.cargarAdmins();
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

    private void inicializarDatosDemoSiVacio() {
        if (productos.isEmpty()) {
            // FRUTAS Y VERDURAS
            productos.add(new Producto(1, "Plátano Granel (kg)", 1290.0, 45, Categorias.FRUTAS_Y_VERDURAS));
            productos.add(new Producto(2, "Manzana fancy (kg)", 1490.0, 30, Categorias.FRUTAS_Y_VERDURAS));
            productos.add(new Producto(3, "Papas (kg)", 990.0, 80, Categorias.FRUTAS_Y_VERDURAS));

            // LACTEOS
            productos.add(new Producto(4, "Leche Entera 1L", 1050.0, 60, Categorias.LACTEOS));
            productos.add(new Producto(5, "Yogurt Batido Frutilla", 350.0, 100, Categorias.LACTEOS));
            productos.add(new Producto(6, "Queso Laminado Mantecoso 250g", 2890.0, 25, Categorias.LACTEOS));

            // PANADERIA
            productos.add(new Producto(7, "Marraqueta (kg)", 1990.0, 40, Categorias.PANADERIA));
            productos.add(new Producto(8, "Pan Hallulla (kg)", 1990.0, 35, Categorias.PANADERIA));
            productos.add(new Producto(9, "Pan de Molde Blanco", 2390.0, 20, Categorias.PANADERIA));

            // CARNICERIA
            productos.add(new Producto(10, "Pechuga de Pollo Deshuezada (kg)", 4990.0, 15, Categorias.CARNICERIA));
            productos.add(new Producto(11, "Carnde de Vacuno Filete (kg)", 16990.0, 10, Categorias.CARNICERIA));
            productos.add(new Producto(12, "Costillar de Cerdo (kg)", 6790.0, 12, Categorias.CARNICERIA));

            // BEBIDAS
            productos.add(new Producto(13, "Pepsi zero 2.5L", 2000.0, 50, Categorias.BEBIDAS));
            productos.add(new Producto(14, "Jugo Néctar Durazno 1.5L", 1390.0, 30, Categorias.BEBIDAS));
            productos.add(new Producto(15, "Agua Mineral Sin Gas 1.5L", 890.0, 45, Categorias.BEBIDAS));

            // LIMPIEZA
            productos.add(new Producto(16, "Detergente Líquido 3L", 7490.0, 18, Categorias.LIMPIEZA));
            productos.add(new Producto(17, "Cloro Gel 900ml", 1690.0, 40, Categorias.LIMPIEZA));
            productos.add(new Producto(18, "Lavaloza Limón 750ml", 2190.0, 28, Categorias.LIMPIEZA));
                

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

    public boolean registrarAdmin(String correo, String contrasena) {
        if (correo == null || contrasena == null) return false;
        String correoLimpio = correo.trim().toLowerCase();
        String passLimpia = contrasena.trim();
        if (correoLimpio.isEmpty() || passLimpia.isEmpty()) return false;

        if (!correoLimpio.endsWith("@supercurico.cl")) {//le puse ese sufijo por mientras porque no me acuerdo que nombre le tenemos al super
            return false;
        }

        if (usuariosAdmin.containsKey(correoLimpio)) {//no crea el usuario si el correo ya esta enlazado a alguien
            return false;
        }

        Admin nuevo = new Admin(correoLimpio, passLimpia);
        usuariosAdmin.put(correoLimpio, nuevo);
        gestorUsuario.guardarAdmin(new ArrayList<>(usuariosAdmin.values()));
        return true;
    }

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

    public void cerrarSesion() {
        this.adminActual = null;
    }

    public boolean hayAdminLogueado() {
        return adminActual != null;
    }

    public Admin getAdminActual() {
        return adminActual;
    }

    //Metodos CRUD

    //Generador de ID
    public int generarId(){
        int mayorId = 0;

        for(Producto producto : productos){
            if(producto.getId() > mayorId){
                mayorId = producto.getId();
            }
        }
        return mayorId + 1;
    }

    //  Crear producto 
    public boolean crearProducto(Producto producto){
        if(producto == null){
            return false;
        }

        // verifica que el precio y el stock no sean negativos (como va a valer -100 pesos jajalolxd :V)
        if(producto.getPrecio() < 0 || producto.getStock() < 0){
            return false;
        }

        if (producto.getId() <= 0 || buscarProducto(producto.getId()) != null) {
            producto.setId(generarId());
        }

        productos.add(producto); // agrega el producto
        gestorArchivo.guardarCatalogo(productos); // guarda el producto 
        return true;
    }
    
    // Busca el producto
    public Producto buscarProducto(int id){
        for(Producto p:productos){
            if(p.getId() == id){
                return p;
            }
        }
        return null;
    }
    
    //  Leer productos
    public List<Producto> leerProductos(){
        return new ArrayList<>(productos);
    }

    //Actualizar producto
    public boolean actualizarProducto(int id, double nuevoPrecio, int nuevoStock){

        // valida que el nuevo precio y stock no sean negativos dnvo xdxdloljaja
        if(nuevoPrecio < 0 || nuevoStock < 0){
            return false;
        }

        Producto producto= buscarProducto(id);

        if(producto!=null){
            producto.setPrecio(nuevoPrecio); // cambia el precio 
            producto.setStock(nuevoStock); // cambia el stock
            gestorArchivo.guardarCatalogo(productos); // lo guarda 
            return true;
        }
        return false;
    }

    //Eliminar producto
    public boolean eliminarProducto(int id){

        Producto producto=buscarProducto(id);

        if(producto!=null){
            productos.remove(producto);
            gestorArchivo.guardarCatalogo(productos);
            return true;
        }
        return false;
    }
    
    public List<Producto> filtrarPorCategoria(Categorias cat) {
        if (cat == null) return leerProductos();
        List<Producto> filtrados = new ArrayList<>();
        for (Producto p : productos) {
            if (p.getCategoria() == cat) filtrados.add(p);
        }
        return filtrados;
    }

    public List<Producto> buscarPorNombre(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return leerProductos();
        }
        
        List<Producto> lista = new ArrayList<>();
        String busqueda = texto.trim().toLowerCase();

        for (Producto p : productos) {
            if (p.getNombre().toLowerCase().contains(busqueda)) {
                lista.add(p);
            }
        }
        return lista;
    }

    // Metodos matematicos.

    // Calculo del promedio segun la categoria
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

    //Obtener el producto con menor stock (asi cuando hay poquito :O)
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

    public double calculoValorTotalInventario(){

        double valorTotal = 0.0;

        for (Producto producto : productos){
            valorTotal += (producto.getPrecio() * producto.getStock());
        }

        return valorTotal;
    }
    
    //---Carrito---
    public List<ItemCarrito> getCarrito() {
        return carrito;
    }

    public boolean agregarAlCarrito(Producto p, double cantidad) {
        if (p == null || cantidad <= 0) {
            return false;
        }
        //Verificacion de si ya existe el prodcuto en el carrito
        for (ItemCarrito item : carrito) {//Busca si el producto esta en el carrito
            if (item.getProducto().getId() == p.getId()) {//si lo encuentra le suma la nueva cantidad en lugar de agregarlo por separado al carrito
                if (item.getCantidad() + cantidad > p.getStock()) {
                    return false; // No hay suficiente stock para sumar esa cantidad
                }
                
                item.setCantidad(item.getCantidad() + cantidad);
                return true;//termina la ejecucion
            }
        }
        //primera vez que se agrega
        if (cantidad > p.getStock()) {
            return false;
        }

        carrito.add(new ItemCarrito(p, cantidad)); // Agrega el producto al carrito
        return true;
    }

    public void eliminarDelCarrito(int idProducto) {
        carrito.removeIf(item -> item.getProducto().getId() == idProducto);
    }

    public void vaciarCarrito() {
        carrito.clear();
    }

    public double calcularTotalCarrito() {
        double total = 0.0;
        for (ItemCarrito item : carrito) {
            total += item.getSubtotal();
        }
        return total;
    }

    public boolean procesarCompra() {
        if (carrito.isEmpty()) {
            return false;
        }

        // verificacion por si acaso de stock
        for (ItemCarrito item : carrito) {
            Producto p = item.getProducto();
            if (p.getStock() < item.getCantidad()) {
                return false;
            }
        }

        for (ItemCarrito item : carrito) {
            Producto p = item.getProducto();
            p.setStock((int) (p.getStock() - item.getCantidad()));
        }

        gestorArchivo.guardarCatalogo(productos);
        vaciarCarrito();
        return true;
    }
}
