package vista;

import modelo.Inventario;
import modelo.ListaDeseos;
import modelo.Producto;
import red.Cliente;
import red.Protocolo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class VistaListaDeseados extends JPanel {
    private final BaseFrameCliente frame;
    private final JPanel panelLista;
    private final JLabel lblUsuario;

    public VistaListaDeseados(BaseFrameCliente frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(EstilosUI.FONDO);

        // Encabezado
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(EstilosUI.VERDE);
        header.setBorder(new EmptyBorder(10, 25, 10, 25));

        JButton btnVolver = EstilosUI.iconButton("☰");
        btnVolver.addActionListener(e -> frame.mostrarVista("PRODUCTOS"));
        header.add(btnVolver, BorderLayout.WEST);

        JLabel title = new JLabel("Lista de Deseados", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.add(title, BorderLayout.CENTER);

        JButton btnIdentificar = EstilosUI.roundedButton("Identificar RUN", EstilosUI.VERDE_CLARO, EstilosUI.VERDE);
        btnIdentificar.addActionListener(e -> pedirIdentificacion());
        header.add(btnIdentificar, BorderLayout.EAST);

        JButton carro = EstilosUI.iconButton("🛒");
        carro.addActionListener(e -> frame.mostrarVista("CARRO"));
        header.add(carro, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Barra sub-header con el RUN/usuario activo
        JPanel subHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        subHeader.setBackground(new Color(245, 248, 245));
        lblUsuario = new JLabel("Cliente: No identificado");
        lblUsuario.setFont(EstilosUI.FONT_BOLD);
        subHeader.add(lblUsuario);

        panelLista = new JPanel();
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setBackground(EstilosUI.FONDO);

        JPanel contenedorCentro = new JPanel(new BorderLayout());
        contenedorCentro.add(subHeader, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(panelLista);
        scroll.setBorder(null);
        contenedorCentro.add(scroll, BorderLayout.CENTER);

        add(contenedorCentro, BorderLayout.CENTER);

        actualizarVista();
    }

    private void pedirIdentificacion() {
        String id = JOptionPane.showInputDialog(
                frame,
                "Ingrese su RUN o nombre de usuario:",
                "Identificación de Cliente",
                JOptionPane.QUESTION_MESSAGE
        );
        if (id != null && !id.trim().isEmpty()) {
            Inventario.getInstancia().identificarClienteDeseos(id);
            actualizarVista();
            frame.getVistaProductos().refrescarVistaActual();
        }
    }

    public void actualizarVista() {
        panelLista.removeAll();
        Inventario inv = Inventario.getInstancia();
        ListaDeseos lista = inv.getListaDeseosActual();

        if (lista == null) {
            lblUsuario.setText("Cliente: No identificado (presiona 'Identificar RUN' o marca un favorito)");
            JLabel lblVacio = new JLabel("Identifícate con tu RUN o usuario para ver y guardar tus productos favoritos.");
            lblVacio.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblVacio.setBorder(new EmptyBorder(50, 0, 0, 0));
            panelLista.add(lblVacio);
        } else {
            lblUsuario.setText("Lista de deseos de: " + lista.getIdentificadorCliente());
            List<Producto> productos = lista.getProductos();

            if (productos.isEmpty()) {
                JLabel lblVacio = new JLabel("Tu lista está vacía. Marca productos con ♡ en el catálogo.");
                lblVacio.setAlignmentX(Component.CENTER_ALIGNMENT);
                lblVacio.setBorder(new EmptyBorder(50, 0, 0, 0));
                panelLista.add(lblVacio);
            } else {
                for (Producto p : productos) {
                    JPanel item = new JPanel(new BorderLayout(10, 0));
                    item.setMaximumSize(new Dimension(850, 55));
                    item.setBackground(Color.WHITE);
                    item.setBorder(BorderFactory.createCompoundBorder(
                            new EmptyBorder(5, 10, 5, 10),
                            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true)
                    ));

                    String estadoStock = p.getStock() > 0 ? "Stock: " + p.getStock() : "Sin stock";
                    JLabel lblNombre = new JLabel("  " + p.getNombre() + "  |  "
                            + EstilosUI.formatearCLP(p.getPrecio()) + "  |  " + estadoStock);
                    lblNombre.setFont(EstilosUI.FONT_NORMAL);
                    item.add(lblNombre, BorderLayout.CENTER);

                    JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
                    botones.setOpaque(false);

                    JButton btnMover = new JButton("Mover al Carro");
                    btnMover.setEnabled(p.getStock() > 0);
                    btnMover.addActionListener(e -> moverAlCarrito(p));

                    JButton btnQuitar = new JButton("Quitar");
                    btnQuitar.addActionListener(e -> {
                        inv.quitarDeFavoritos(p.getId());
                        actualizarVista();
                        frame.getVistaProductos().refrescarVistaActual();
                    });

                    botones.add(btnMover);
                    botones.add(btnQuitar);
                    item.add(botones, BorderLayout.EAST);

                    panelLista.add(item);
                }
            }
        }

        panelLista.revalidate();
        panelLista.repaint();
    }

    private void moverAlCarrito(Producto p) {
        Cliente cl = Cliente.getInstancia();
        if (cl == null || !cl.estaConectado()) {
            JOptionPane.showMessageDialog(frame,
                    "Sin conexión con el servidor. No se puede reservar stock.",
                    "Sin conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (Inventario.getInstancia().agregarAlCarrito(p, 1)) {
            cl.enviarDatos(p.getId(), Protocolo.AGREGAR + 1);
            Inventario.getInstancia().quitarDeFavoritos(p.getId());
            actualizarVista();
            frame.getVistaCarrito().actualizarCarrito();
            frame.getVistaProductos().refrescarVistaActual();

            JOptionPane.showMessageDialog(frame,
                    "¡" + p.getNombre() + " movido al carrito!",
                    "Producto agregado", JOptionPane.INFORMATION_MESSAGE);
        } else {
            actualizarVista();
            JOptionPane.showMessageDialog(frame,
                    "No hay stock disponible para este producto.",
                    "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
        }
    }
}