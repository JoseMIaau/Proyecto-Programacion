package vista;

import javax.swing.*;
import java.awt.*;

public class BaseFrameAdmin extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);

    private VistaLogin vistaLogin;
    private VistaAdmin vistaAdmin;
    private VistaModificarProducto vistaModificarProducto;

    public BaseFrameAdmin() {
        super("SuperCuricó - Servidor Administrador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 720));
        setSize(1180, 780);
        setLocationRelativeTo(null);

        vistaLogin = new VistaLogin(this);
        vistaAdmin = new VistaAdmin(this);
        vistaModificarProducto = new VistaModificarProducto(this);

        root.add(vistaLogin, "LOGIN");
        root.add(vistaAdmin, "ADMIN");
        root.add(vistaModificarProducto, "MODIFICAR");

        setContentPane(root);
        cardLayout.show(root, "LOGIN");
    }

    public void mostrarVista(String vista) {
        if ("ADMIN".equals(vista)) {
            vistaAdmin.poblarTabla();
        }
        cardLayout.show(root, vista);
    }

    public void refrescarTablaAdmin() {
        System.out.println("[BASEFRAME ADMIN] ejecutando refresco de tabla...");
        if (vistaAdmin != null) {
            vistaAdmin.poblarTabla();
        }
    }

}