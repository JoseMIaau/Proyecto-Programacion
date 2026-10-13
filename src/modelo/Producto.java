package modelo;


public class Producto {
    private int id;
    private String nombre;
    private double precio;
    private int stock;
    private int reservado;
    private Categorias categoria;

    public Producto(int id, String nombre, double precio, int stock, Categorias categoria) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.reservado = 0;
        this.categoria = categoria;
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

    public int getReservado(){ return reservado; }
    public void setReservado(int reservado){ this.reservado = reservado; }

    public int getStockTotalReal(){
        return stock + reservado;
    }
    
    @Override
    public String toString() {
        return id + " - " + nombre + " ($" + (int) precio + ") | Stock: " + stock + " | Cat: " + categoria;
    }
}