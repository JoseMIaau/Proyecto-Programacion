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

    private GestorArchivo gestorDeseos;
    private final java.util.Map<String, java.util.List<Integer>> deseosGuardados = new java.util.LinkedHashMap<>();
    private ListaDeseos listaDeseosActual = null;

    private HashMap<String, Admin> usuariosAdmin;
    private Admin adminActual;
    private Pedido ultimoPedidoCreado;

    private GestorArchivo gestorTarifas;
    private List<TarifaEnvio> tarifasEnvio = new ArrayList<>();;
    private double minimoEnvioGratis = 60000.0;
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
        this.gestorDeseos = new GestorArchivo("deseos.csv");
        this.deseosGuardados.putAll(gestorDeseos.cargarDeseos());

        this.gestorTarifas = new GestorArchivo("tarifas.csv");
        this.minimoEnvioGratis = gestorTarifas.cargarMinimoEnvioGratis(60000.0);
        this.tarifasEnvio.clear();
        this.tarifasEnvio.addAll(gestorTarifas.cargarTarifas());
        inicializarTarifasPorDefecto();

        for (ClienteRegistrado cliente : gestorClientes.cargarClientes()) {
            clientesRegistrados.put(cliente.getRun(), cliente);
        }
        historialCompras.addAll(gestorCompras.cargarCompras());
        this.adminActual = null;
        inicializarDatosDemoSiVacio();
        inicializarAdminPorDefecto();
    }

    public static synchronized Inventario getInstancia() {
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

    private void inicializarTarifasPorDefecto() {
        if (tarifasEnvio.isEmpty()) {
            tarifasEnvio.add(new TarifaEnvio(1, "Curicó Centro", 790.0, 0));
            tarifasEnvio.add(new TarifaEnvio(2, "Sector Norte / Sarmiento", 990.0, 1));
            tarifasEnvio.add(new TarifaEnvio(3, "Sector Sur / Rauquén", 1190.0, 1));
            tarifasEnvio.add(new TarifaEnvio(4, "Sector Oriente / Los Niches", 1390.0, 2));
            gestorTarifas.guardarTarifas(tarifasEnvio, minimoEnvioGratis);
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

    public synchronized int generarId() {
        int mayorId = 0;
        for (Producto producto : productos) {
            if (producto.getId() > mayorId) {
                mayorId = producto.getId();
            }
        }
        return mayorId + 1;
    }

    public synchronized boolean crearProducto(Producto producto) {
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

    public synchronized boolean productoExistente(String nombre){
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
    
    public synchronized Producto buscarProducto(int id) {
        for (Producto p : productos) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }
    
    public synchronized List<Producto> leerProductos() {
        return new ArrayList<>(productos);
    }

    public synchronized boolean actualizarProducto(int id, double nuevoPrecio, int nuevoStock) {
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

    public synchronized boolean eliminarProducto(int id) {
        Producto producto = buscarProducto(id);
        if (producto != null) {
            productos.remove(producto);
            gestorArchivo.guardarCatalogo(productos);
            return true;
        }
        return false;
    }

    public synchronized boolean vaciarInventario() {
        if (productos.isEmpty()) return false;
        productos.clear();
        gestorArchivo.guardarCatalogo(productos);
        return true;
    }

    // --- ACTUALIZACION INDIVIDUAL DE PRECIO Y STOCK ---

    public synchronized boolean actualizarPrecio(int id, double nuevoPrecio) {
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

    public synchronized boolean actualizarStock(int id, int nuevoStock) {
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
    public synchronized boolean agregarStock(int id, int cantidad) {
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

    public synchronized int obtenerStockDisponible(int idProducto) {
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

    public synchronized List<Producto> filtrarPorCategoria(Categorias cat) {
        if (cat == null) return leerProductos();
        List<Producto> filtrados = new ArrayList<>();
        for (Producto p : productos) {
            if (p.getCategoria() == cat) filtrados.add(p);
        }
        return filtrados;
    }

    public synchronized List<Producto> filtrarPorPrecio(double precioMinimo, double precioMaximo) {
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

    public synchronized List<Producto> buscarPorNombre(String texto) {
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

    public synchronized double calculoValorTotalInventario() {
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
    
    public String getIdClienteActual(){
        return idClienteActual;
    }

    public synchronized Carrito getCarritoActual() {
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
    public synchronized boolean agregarAlCarrito(Producto p, int cantidad) {
        if (p == null || cantidad <= 0 || p.getStock() < cantidad) return false;

        p.setStock(p.getStock() - cantidad);
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
    
    public synchronized void eliminarDelCarrito(int idProducto) {
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

    public synchronized void vaciarCarrito() {
        Carrito c = getCarritoActual();
        for (ItemCarrito item : c.getItems()) {
            int nuevoStock = (int) (item.getProducto().getStock() + item.getCantidad());
            item.getProducto().setStock(nuevoStock);
        }
        c.limpiar();
    }

    public double calcularIVA() { return calcularTotalCarrito() - calcularSubtotal();}

    public double calcularSubtotal() { return calcularTotalCarrito() / 1.19;}

    public synchronized double calcularTotalCarrito() {
        double total = 0.0;
        for (ItemCarrito item : getCarritoActual().getItems()) {
            total += item.getSubtotal();
        }
        return total;
    }

    /**
     * Finaliza la transaccion, crea la instancia de Venta para el historial
     * y limpia el carrito actual.
     */
    public synchronized boolean procesarCompra(String run, String nombre) {
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
        Pedido pedido = GestorPedidos.getInstancia().crearPedido(compra, idClienteActual);
        if (pedido == null){
            return false;
        }
        ultimoPedidoCreado = pedido;

        historialCompras.add(compra);
        gestorCompras.guardarCompras(historialCompras);

        gestorArchivo.guardarCatalogo(productos);
        c.limpiar(); 
        if (!tieneClienteDeseosIdentificado()) identificarClienteDeseos(run); //para que no vuelva a poner si ya compro
        return true;
    }
    
    public Pedido getUltimoPedidoCreado(){
        return ultimoPedidoCreado;
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

    public static final int RESERVA_INEXISTENTE = -1;
    public static final int RESERVA_INSUFICIENTE = -2;

    public synchronized int reservarStock(int idProducto, int cant) {
        Producto p = buscarProducto(idProducto);
        if (p == null) return RESERVA_INEXISTENTE;
        if (cant <= 0 || p.getStock() < cant) return RESERVA_INSUFICIENTE;
        p.setStock(p.getStock() - cant);
        p.setReservado(p.getReservado() + cant);
        return p.getStock();
    }

    public synchronized int liberarStock(int idProducto, int cant) {
        Producto p = buscarProducto(idProducto);
        if (p == null || cant <= 0) return RESERVA_INEXISTENTE;
        p.setStock(p.getStock() + cant);
        p.setReservado(Math.max(0, p.getReservado() - cant));
        return p.getStock();
    }

    public synchronized void confirmarVenta(int idProducto, int cant) {
        Producto p = buscarProducto(idProducto);
        if (p == null || cant <= 0) return;
        p.setReservado(Math.max(0, p.getReservado() - cant));
    }

    public synchronized void persistirCatalogo() {
        gestorArchivo.guardarCatalogo(productos);
    }

    public synchronized void revertirAgregado(int idProducto, int cant) {
        Carrito c = getCarritoActual();
        for (ItemCarrito item : c.getItems()) {
            if (item.getProducto().getId() == idProducto) {
                int restante = item.getCantidad() - cant;
                if (restante <= 0) c.getItems().remove(item);
                else item.setCantidad(restante);
                return;
            }
        }
    }

    public synchronized void limpiarCarritoLocal() {
        getCarritoActual().limpiar();
    }


    // --- METODOS LISTA DE DESEADOS) ---

    public ListaDeseos getListaDeseosActual() {
        return listaDeseosActual;
    }

    public boolean tieneClienteDeseosIdentificado() {
        return listaDeseosActual != null && !listaDeseosActual.getIdentificadorCliente().isEmpty();
    }

    public ListaDeseos identificarClienteDeseos(String identificador) {
        if (identificador == null || identificador.trim().isEmpty()) return null;
        String clave = identificador.trim().toUpperCase();

        ListaDeseos lista = new ListaDeseos(clave);
        List<Integer> idsPrevios = deseosGuardados.getOrDefault(clave, new ArrayList<>());
        for (int idProd : idsPrevios) {
            Producto p = buscarProducto(idProd);
            if (p != null) {
                lista.agregarProducto(p);
                if (red.Cliente.getInstancia() != null && red.Cliente.getInstancia().estaConectado()) {
                    red.Cliente.getInstancia().enviarDatos(idProd, red.Protocolo.AGREGAR_DESEO + "1");
                }
            }
        }
        this.listaDeseosActual = lista;
        return lista;
    }

    public boolean esFavorito(int idProducto) {
        return listaDeseosActual != null && listaDeseosActual.contieneProducto(idProducto);
    }

    public boolean agregarAFavoritos(Producto p) {
        if (listaDeseosActual == null || p == null) return false;
        boolean agregado = listaDeseosActual.agregarProducto(p);
        if (agregado) {
            persistirDeseosActuales();
            if (red.Cliente.getInstancia() != null && red.Cliente.getInstancia().estaConectado()) {
                red.Cliente.getInstancia().enviarDatos(p.getId(), red.Protocolo.AGREGAR_DESEO + "1");
            }
        }
        return agregado;
    }

    public boolean quitarDeFavoritos(int idProducto) {
        if (listaDeseosActual == null) return false;
        boolean eliminado = listaDeseosActual.eliminarProducto(idProducto);
        if (eliminado) {
            persistirDeseosActuales();
        }
        return eliminado;
    }

    private void persistirDeseosActuales() {
        if (listaDeseosActual == null) return;
        List<Integer> ids = new ArrayList<>();
        for (Producto prod : listaDeseosActual.getProductos()) {
            ids.add(prod.getId());
        }
        deseosGuardados.put(listaDeseosActual.getIdentificadorCliente(), ids);
        gestorDeseos.guardarDeseos(deseosGuardados);
    }

    // --- METODOS DE TARIFAS Y DESPACHO ---

    public synchronized List<TarifaEnvio> obtenerTarifasEnvio() {
        return new ArrayList<>(tarifasEnvio);
    }

    public synchronized TarifaEnvio buscarTarifaPorZona(int zona) {
        for (TarifaEnvio t : tarifasEnvio) {
            if (t.getZona() == zona) {
                return t;
            }
        }
        return null;
    }

    public synchronized boolean actualizarTarifaEnvio(int zona, String descripcion, double nuevoCosto, int nuevosDias) {
        if (nuevoCosto < 0 || nuevosDias < 0) {
            return false;
        }
        TarifaEnvio tarifa = buscarTarifaPorZona(zona);
        if (tarifa == null) {
            return false;
        }
        tarifa.setDescripcion(descripcion);
        tarifa.setCosto(nuevoCosto);
        tarifa.setDiasHabiles(nuevosDias);
        gestorTarifas.guardarTarifas(tarifasEnvio, minimoEnvioGratis);
        return true;
    }

    public synchronized double getMinimoEnvioGratis() {
        return minimoEnvioGratis;
    }

    public synchronized boolean setMinimoEnvioGratis(double nuevoMinimo) {
        if (nuevoMinimo < 0) {
            return false;
        }
        this.minimoEnvioGratis = nuevoMinimo;
        gestorTarifas.guardarTarifas(tarifasEnvio, minimoEnvioGratis);
        return true;
    }

    public synchronized double calcularSubtotalProductos() {
        double suma = 0.0;
        for (ItemCarrito item : getCarritoActual().getItems()) {
            suma += item.getSubtotal();
        }
        return suma;
    }

    public synchronized double calcularCostoEnvio(boolean retiroEnTienda, int zona) {
        if (retiroEnTienda) {
            return 0.0;
        }
        double subtotal = calcularSubtotalProductos();
        if (subtotal >= minimoEnvioGratis) {
            return 0.0;
        }
        TarifaEnvio tarifa = buscarTarifaPorZona(zona);
        if (tarifa == null) {
            return 0.0;
        }
        return tarifa.getCosto();
    }

    public synchronized double calcularIVAConEnvio(double costoEnvio) {
        double baseImponible = calcularSubtotalProductos() + costoEnvio;
        return baseImponible * 0.19;
    }

    public synchronized double calcularTotalConEnvio(double costoEnvio) {
        double baseImponible = calcularSubtotalProductos() + costoEnvio;
        double iva = calcularIVAConEnvio(costoEnvio);
        return baseImponible + iva;
    }


}
