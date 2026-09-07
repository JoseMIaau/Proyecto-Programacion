package vista;

import modelo.Categorias;

import javax.swing.*;
import java.awt.*;

public class BaseFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);

    private VistaMenuPrincipal vistaMenuPrincipal;
    private VistaProductos vistaProductos;
    private VistaCarrito vistaCarrito;
    private VistaLogin vistaLogin;
    private VistaAdmin vistaAdmin;
    private VistaModificarProducto vistaModificarProducto;

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

    public void mostrarVista(String vista) {

        if ("PRODUCTOS".equals(vista)) {

            vistaProductos.mostrarTodosLosProductos();

        } else if ("CARRO".equals(vista)) {

            vistaCarrito.actualizarCarrito();
<<<<<<< HEAD
        } else if ("ADMIN".equals(vista)) {
=======

        } else if ("ADMIN".equals(vista)) {

            // Refresca la tabla al volver al administrador
>>>>>>> Cambios-Antonia-Medina
            vistaAdmin.poblarTabla();
        }

        cardLayout.show(root, vista);
    }

    public void mostrarProductosPorCategoria(Categorias cat) {

        vistaProductos.filtrarPorCategoria(cat);
        cardLayout.show(root, "PRODUCTOS");
    }
<<<<<<< HEAD
    public void mostrarMenuPrincipal() {    
=======

    public void mostrarMenuPrincipal() {

>>>>>>> Cambios-Antonia-Medina
        cardLayout.show(root, "INICIO");
    }

    public void buscarProductosPorTexto(String query) {
<<<<<<< HEAD
        vistaProductos.buscarPorTexto(query);
        vistaProductos.setTextoBuscador(query);
        cardLayout.show(root, "PRODUCTOS");
=======

>>>>>>> Cambios-Antonia-Medina
    }
}