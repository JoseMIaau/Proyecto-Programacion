package vista;

import modelo.Inventario;
import modelo.ItemCarrito;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;

public class VistaCarrito extends JPanel {
    private final BaseFrame frame;
    private final JPanel leftPanel;
    private final JPanel rightPanel;
    private final JLabel totalLabel;

    public VistaCarrito(BaseFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(EstilosUI.FONDO);

        add(createCartHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2));
        content.setBackground(EstilosUI.FONDO);

        leftPanel = new JPanel();
        leftPanel.setBackground(EstilosUI.FONDO);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(25, 40, 25, 30));

        JScrollPane scrollLeft = new JScrollPane(leftPanel);
        scrollLeft.setBorder(null);

        rightPanel = new JPanel();
        rightPanel.setBackground(EstilosUI.FONDO);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 2, 0, 0, Color.LIGHT_GRAY),
                new EmptyBorder(30, 40, 30, 40)
        ));

        totalLabel = new JLabel("Total: $0");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(scrollLeft);
        content.add(rightPanel);
        add(content, BorderLayout.CENTER);

        actualizarCarrito();
    }

    public void actualizarCarrito() {
        leftPanel.removeAll();
        rightPanel.removeAll();

        List<ItemCarrito> items = Inventario.getInstancia().getCarrito();

        if (items.isEmpty()) {
            JLabel lblVacio = new JLabel("El carrito está vacío.");
            lblVacio.setFont(EstilosUI.FONT_BOLD);
            leftPanel.add(lblVacio);
        } else {
            for (ItemCarrito item : items) {
                leftPanel.add(createCartRow(item));
                leftPanel.add(Box.createVerticalStrut(12));

                rightPanel.add(summaryLine(item.getProducto().getNombre() + " (x" + item.getCantidad() + ")", "$" + (int) item.getSubtotal()));
                rightPanel.add(Box.createVerticalStrut(8));
            }
        }

        rightPanel.add(Box.createVerticalStrut(20));
        totalLabel.setText("Total: $" + (int) Inventario.getInstancia().calcularTotalCarrito());
        rightPanel.add(totalLabel);
        rightPanel.add(Box.createVerticalStrut(25));

        JButton btnPagar = EstilosUI.roundedButton("PAGAR", EstilosUI.VERDE_CLARO, EstilosUI.VERDE);
        btnPagar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPagar.setMaximumSize(new Dimension(160, 44));
        btnPagar.addActionListener(e -> {
            if (items.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe agregar productos antes de pagar", "Carro Vacío", JOptionPane.WARNING_MESSAGE);
            } else {
                showCheckoutDialog();
            }
        });
        rightPanel.add(btnPagar);

        revalidate();
        repaint();
    }

    private JPanel createCartHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(EstilosUI.VERDE);
        header.setBorder(new EmptyBorder(10, 25, 10, 25));

        JButton back = EstilosUI.iconButton("☰");
        back.addActionListener(e -> frame.mostrarVista("PRODUCTOS"));
        header.add(back, BorderLayout.WEST);

        JLabel title = new JLabel("Mi carrito de compras", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.add(title, BorderLayout.CENTER);

        return header;
    }

    private JPanel createCartRow(ItemCarrito item) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setBackground(EstilosUI.FONDO);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        row.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel name = new JLabel(item.getProducto().getNombre() + " (x" + item.getCantidad() + ")");
        name.setFont(EstilosUI.FONT_BOLD);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actions.setOpaque(false);

        JLabel price = new JLabel("$" + (int) item.getSubtotal());
        price.setFont(EstilosUI.FONT_BOLD);

        JButton trash = new JButton("🗑");
        trash.setFont(new Font("SansSerif", Font.PLAIN, 18));
        trash.setForeground(Color.RED);
        trash.setBorderPainted(false);
        trash.setContentAreaFilled(false);
        trash.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        trash.addActionListener(e -> {
            Inventario.getInstancia().eliminarDelCarrito(item.getProducto().getId());
            actualizarCarrito();
        });

        actions.add(price);
        actions.add(trash);

        row.add(name, BorderLayout.WEST);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private JPanel summaryLine(String name, String price) {
        JPanel line = new JPanel(new BorderLayout());
        line.setOpaque(false);

        JLabel a = new JLabel(name);
        a.setFont(new Font("SansSerif", Font.PLAIN, 15));
        JLabel b = new JLabel(price);
        b.setFont(new Font("SansSerif", Font.BOLD, 15));

        line.add(a, BorderLayout.WEST);
        line.add(b, BorderLayout.EAST);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return line;
    }

    private void showCheckoutDialog() {
        JDialog dialog = new JDialog(frame, "Finalizar Pago", true);
        dialog.setSize(480, 360);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout());

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel totalTxt = new JLabel("Total a pagar: $" + (int) Inventario.getInstancia().calcularTotalCarrito());
        totalTxt.setFont(EstilosUI.FONT_TITLE);
        totalTxt.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtNombre = new JTextField();
        JTextField txtDireccion = new JTextField();
        JTextField txtTarjeta = new JTextField();

        body.add(totalTxt);
        body.add(Box.createVerticalStrut(15));
        body.add(new JLabel("Nombre Completo:"));
        body.add(txtNombre);
        body.add(Box.createVerticalStrut(8));
        body.add(new JLabel("Direccion de entrega:"));
        body.add(txtDireccion);
        body.add(Box.createVerticalStrut(8));
        body.add(new JLabel("N° Tarjeta:"));
        body.add(txtTarjeta);
        body.add(Box.createVerticalStrut(20));

        JButton btnConfirmar = EstilosUI.roundedButton("PAGAR AHORA", EstilosUI.VERDE_CLARO, EstilosUI.VERDE);
        btnConfirmar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConfirmar.addActionListener(e -> {
            if (txtNombre.getText().trim().isEmpty() || txtDireccion.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Complete todos los campos obligatorios.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean exito = Inventario.getInstancia().procesarCompra();
            if (exito) {
                JOptionPane.showMessageDialog(dialog, "¡Pago procesado con éxito! Gracias por su compra en SuperCuricó.");
                actualizarCarrito(); // Deja la tabla visual vacía y en $0
                dialog.dispose();
                frame.mostrarVista("INICIO"); // Redirige al menú principal
            }else {
                JOptionPane.showMessageDialog(dialog, 
                    "Error al procesar el pago: uno o más productos ya no cuentan con stock suficiente.", 
                    "Error de Stock", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        body.add(btnConfirmar);
        dialog.add(body, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}