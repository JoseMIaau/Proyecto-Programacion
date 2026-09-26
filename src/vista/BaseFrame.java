package vista;

import modelo.Categorias;

import javax.swing.*;
import java.awt.*;

// Ventana principal crea, registra y muestra las vistas
public class BaseFrame extends JFrame {

// CardLayout deja cambiar entre las distintas pantallas dentro de la misma ventana
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);

// Vistas principales
    private VistaMenuPrincipal vistaMenuPrincipal;
    private VistaProductos vistaProductos;
    private VistaCarrito vistaCarrito;
    private VistaLogin vistaLogin;
    private VistaAdmin vistaAdmin;
    private VistaModificarProducto vistaModificarProducto;

// Constructor de la ventana principal
    public BaseFrame() {

        super("SuperCuricó");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 720));
        setSize(1180, 780);
        setLocationRelativeTo(null);

        // Crear vistas
        vistaMenuPrincipal = new VistaMenuPrincipal(this);
        vistaProductos = new VistaProductos(this);
        vistaCarrito = new VistaCarrito(this);
        vistaLogin = new VistaLogin(this);
        vistaAdmin = new VistaAdmin(this);
        vistaModificarProducto = new VistaModificarProducto(this);

        // Registrar vistas
        root.add(vistaMenuPrincipal, "INICIO");
        root.add(vistaProductos, "PRODUCTOS");
        root.add(vistaCarrito, "CARRO");
        root.add(vistaLogin, "LOGIN");
        root.add(vistaAdmin, "ADMIN");
        root.add(vistaModificarProducto, "MODIFICAR");

        setContentPane(root);

        cardLayout.show(root, "INICIO");
    }

// Cambia la pantalla visible y actualiza su información cuando sea necesario
    public void mostrarVista(String vista) {

        if ("PRODUCTOS".equals(vista)) {

            vistaProductos.mostrarTodosLosProductos();

        } else if ("CARRO".equals(vista)) {

            vistaCarrito.actualizarCarrito();

        } else if ("ADMIN".equals(vista)) {

            // Refresca la tabla al volver al administrador
            vistaAdmin.poblarTabla();
        }

        cardLayout.show(root, vista);
    }

    // Muestra la vista de productos filtrada por la categoría seleccionada
    public void mostrarProductosPorCategoria(Categorias cat) {

        vistaProductos.filtrarPorCategoria(cat);
        cardLayout.show(root, "PRODUCTOS");
    }

   // Vuelve a la pantalla principal 
    public void mostrarMenuPrincipal() {

        cardLayout.show(root, "INICIO");
    }

    // Método que busca los productos según el texto ingresado
    public void buscarProductosPorTexto(String query) {
        vistaProductos.buscarPorTexto(query);
        vistaProductos.setTextoBuscador(query);
        cardLayout.show(root, "PRODUCTOS");
    }
}