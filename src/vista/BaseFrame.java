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

    public BaseFrame() {
        super("SuperCuricó");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 720));
        setSize(1180, 780);
        setLocationRelativeTo(null);

        vistaMenuPrincipal = new VistaMenuPrincipal(this);
        vistaProductos = new VistaProductos(this);
        vistaCarrito = new VistaCarrito(this);
        vistaLogin = new VistaLogin(this);
        vistaAdmin = new VistaAdmin(this);

        root.add(vistaMenuPrincipal, "INICIO");
        root.add(vistaProductos, "PRODUCTOS");
        root.add(vistaCarrito, "CARRO");
        root.add(vistaLogin, "LOGIN");
        root.add(vistaAdmin, "ADMIN");

        setContentPane(root);
        cardLayout.show(root, "INICIO");
    }

    public void mostrarVista(String vista) {
        if ("PRODUCTOS".equals(vista)) {
            vistaProductos.mostrarTodosLosProductos();
        } else if ("CARRO".equals(vista)) {
            vistaCarrito.actualizarCarrito();
        } else if ("ADMIN".equals(vista)) {
            vistaAdmin.poblarTabla();
        }
        cardLayout.show(root, vista);
    }

    public void mostrarProductosPorCategoria(Categorias cat) {
        vistaProductos.filtrarPorCategoria(cat);
        cardLayout.show(root, "PRODUCTOS");
    }
    public void mostrarMenuPrincipal() {    
        cardLayout.show(root, "INICIO");
    }

    public void buscarProductosPorTexto(String query) {
        vistaProductos.buscarPorTexto(query);
        vistaProductos.setTextoBuscador(query);
        cardLayout.show(root, "PRODUCTOS");
    }
}