package vista;

import modelo.Inventario;
import modelo.ItemCarrito;
import modelo.Venta;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;

public class VistaCarrito extends JPanel {
    
    private final BaseFrameCliente frame;
    private final JPanel leftPanel;
    private final JPanel rightPanel;
    private final JLabel totalLabel;

    public VistaCarrito(BaseFrameCliente frame) {
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
        rightPanel.setBorder(new CompoundBorder(new MatteBorder(0, 2, 0, 0, Color.LIGHT_GRAY), new EmptyBorder(30, 40, 30, 40)));

        totalLabel = new JLabel("Total: $0");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(scrollLeft);
        content.add(rightPanel);
        add(content, BorderLayout.CENTER);

        actualizarCarrito();
    }

    // --- LOGICA DEL CARRITO ---

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

            leftPanel.add(Box.createVerticalStrut(20));
            JButton btnVaciar = EstilosUI.roundedButton("vaciar carrito", new Color(255, 230, 230), new Color(180, 40, 40));
            btnVaciar.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnVaciar.setMaximumSize(new Dimension(160, 44));
            
            btnVaciar.addActionListener(e -> {
                int confirmacion = JOptionPane.showConfirmDialog(
                        this, "¿Estás seguro de que deseas eliminar todos los productos del carrito?",
                        "Confirmar vaciado", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
                );

                if (confirmacion == JOptionPane.YES_OPTION) {
                    for (ItemCarrito it : Inventario.getInstancia().getCarrito()) {
                        if (red.Cliente.getInstancia() != null) {
                            red.Cliente.getInstancia().enviarDatos(it.getProducto().getId(), "AGREGAR:-" + it.getCantidad());
                        }
                    }
                    Inventario.getInstancia().vaciarCarrito();
                    actualizarCarrito();
                }
            });
            leftPanel.add(btnVaciar);
        }

        rightPanel.add(Box.createVerticalStrut(20));
        
        double subtotal = Inventario.getInstancia().calcularSubtotal();
        double iva = Inventario.getInstancia().calcularIVA();
        double total = Inventario.getInstancia().calcularTotalCarrito();
        
        rightPanel.add(summaryLine("Subtotal", "$" + (int) subtotal));
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(summaryLine("IVA (19%)", "$" + (int) iva));
        rightPanel.add(Box.createVerticalStrut(8));

        totalLabel.setText("Total: $" + (int) total);
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

    // --- CONSTRUCCION UI ---

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
        row.setBorder(new CompoundBorder(new LineBorder(new Color(230, 230, 230), 1, true), new EmptyBorder(8, 12, 8, 12)));

        JLabel name = new JLabel(item.getProducto().getNombre());
        name.setFont(EstilosUI.FONT_BOLD);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JLabel price = new JLabel("$" + (int) item.getSubtotal());
        price.setFont(EstilosUI.FONT_BOLD);

        JButton botonMenos = new JButton("-");
        botonMenos.setFont(new Font("SansSerif", Font.BOLD, 15));
        botonMenos.setPreferredSize(new Dimension(38, 28));
        botonMenos.setMargin(new Insets(0, 0, 0, 0));
        botonMenos.setFocusPainted(false);
        botonMenos.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JTextField txtUnits = new JTextField(String.valueOf((int) item.getCantidad()), 3);
        txtUnits.setHorizontalAlignment(JTextField.CENTER);
        txtUnits.setFont(EstilosUI.FONT_BOLD);
        txtUnits.setEditable(false);

        JButton botonMas = new JButton("+");
        botonMas.setFont(new Font("SansSerif", Font.BOLD, 15));
        botonMas.setPreferredSize(new Dimension(38, 28));
        botonMas.setMargin(new Insets(0, 0, 0, 0));
        botonMas.setFocusPainted(false);
        botonMas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton trash = new JButton("🗑");
        trash.setFont(new Font("SansSerif", Font.PLAIN, 18));
        trash.setForeground(Color.RED);
        trash.setBorderPainted(false);
        trash.setContentAreaFilled(false);
        trash.setFocusPainted(false);
        trash.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Listeners
        botonMenos.addActionListener(click -> {
            if (item.getCantidad() > 1) {
                item.getProducto().setStock(item.getProducto().getStock() + 1);
                item.setCantidad(item.getCantidad() - 1);
                if (red.Cliente.getInstancia() != null) red.Cliente.getInstancia().enviarDatos(item.getProducto().getId(), "AGREGAR:-1");
                actualizarCarrito();
            } else {
                int confirmacion = JOptionPane.showConfirmDialog(
                        this, "¿Desea quitar " + item.getProducto().getNombre() + " del carrito?",
                        "Quitar producto", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
                );
                if (confirmacion == JOptionPane.YES_OPTION) {
                    double cant = item.getCantidad();
                    Inventario.getInstancia().eliminarDelCarrito(item.getProducto().getId());
                    if (red.Cliente.getInstancia() != null) red.Cliente.getInstancia().enviarDatos(item.getProducto().getId(), "AGREGAR:-" + cant);
                    actualizarCarrito();
                }
            }
        });

        botonMas.addActionListener(click -> {
            if (item.getProducto().getStock() >= 1) {
                item.getProducto().setStock(item.getProducto().getStock() - 1);
                item.setCantidad(item.getCantidad() + 1);
                if (red.Cliente.getInstancia() != null) red.Cliente.getInstancia().enviarDatos(item.getProducto().getId(), "AGREGAR:1");
                actualizarCarrito();
            } else {
                JOptionPane.showMessageDialog(this, "No hay mas stock disponible de este producto.", "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
            }
        });

        trash.addActionListener(e -> {
            double cant = item.getCantidad();
            Inventario.getInstancia().eliminarDelCarrito(item.getProducto().getId());
            if (red.Cliente.getInstancia() != null) red.Cliente.getInstancia().enviarDatos(item.getProducto().getId(), "AGREGAR:-" + cant);
            actualizarCarrito();
        });

        actions.add(price);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(botonMenos);
        actions.add(txtUnits);
        actions.add(botonMas);
        actions.add(Box.createHorizontalStrut(8));
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
        dialog.setSize(480, 420);
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
        JTextField txtRun = new JTextField();

        body.add(totalTxt);
        body.add(Box.createVerticalStrut(15));
        body.add(new JLabel("Nombre Completo:"));
        body.add(txtNombre);
        body.add(Box.createVerticalStrut(8));
        body.add(new JLabel("RUN del cliente:"));
        body.add(txtRun);
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
            String nombre = txtNombre.getText().trim();
            String run = txtRun.getText().trim();
            String direccion = txtDireccion.getText().trim();

            if (nombre.isEmpty() || run.isEmpty() || direccion.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Complete todos los campos obligatorios.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean exito = Inventario.getInstancia().procesarCompra(run, nombre);

            if (exito) {
                if (red.Cliente.getInstancia() != null) {
                    red.Cliente.getInstancia().enviarDatos(0, "COMPRAR"); 
                }
                JOptionPane.showMessageDialog(dialog, "¡Pago procesado con éxito! Gracias por su compra en SuperCuricó.");
                actualizarCarrito(); 
                dialog.dispose();
                frame.mostrarVista("INICIO"); 
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al procesar el pago. Por favor revise su carrito.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        body.add(btnConfirmar);
        dialog.add(body, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}
