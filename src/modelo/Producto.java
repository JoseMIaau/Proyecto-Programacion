package modelo;


public class Producto {
    private int id;
    private String nombre;
    private double precio;
    private int stock;
    private Categorias categoria;
    private String rutaImagen;

    public Producto(int id, String nombre, double precio, int stock, Categorias categoria, String rutaImagen) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.rutaImagen = rutaImagen;
    }
    
    //Getters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public int getStock() {
        return stock;
    }

    public Categorias getCategoria() {
        return categoria;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    //Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public void setCategoria(Categorias categoria) {
        this.categoria = categoria;
    }
    
    public void setRutaImagen(String rutaImagen) {
        this.rutaImagen = rutaImagen;
    }
    
    @Override
    public String toString() {
        return id + " - " + nombre + " ($" + (int) precio + ") | Stock: " + stock + " | Cat: " + categoria;
    }
}