package vista;

import modelo.Inventario;
import modelo.Producto;

import javax.swing.*;
import java.awt.*;
//clase inutil
public class VistaModificarProducto extends JPanel {

    private final BaseFrame frame;
    private final Inventario inventario;

    private JTextField txtId;
    private JTextField txtPrecio;
    private JTextField txtStock;

    private JButton btnAtras;
    private JButton btnModificar;

    public VistaModificarProducto(BaseFrame frame) {

        this.frame = frame;
        this.inventario = Inventario.getInstancia();

        inicializarComponentes();
    }

    private void inicializarComponentes() {

        setLayout(new BorderLayout(15, 15));

        JLabel lblTitulo = new JLabel(
                "Modificar Producto",
                SwingConstants.CENTER
        );

        lblTitulo.setFont(
                new Font("Arial", Font.BOLD, 24)
        );

        add(lblTitulo, BorderLayout.NORTH);

        JPanel pnlFormulario = new JPanel(
                new GridLayout(3, 2, 10, 15)
        );

        pnlFormulario.setBorder(
                BorderFactory.createEmptyBorder(
                        60,
                        100,
                        60,
                        100
                )
        );

        txtId = new JTextField();
        txtPrecio = new JTextField();
        txtStock = new JTextField();

        pnlFormulario.add(
                new JLabel("ID del Producto:")
        );
        pnlFormulario.add(txtId);

        pnlFormulario.add(
                new JLabel("Nuevo Precio:")
        );
        pnlFormulario.add(txtPrecio);

        pnlFormulario.add(
                new JLabel("Nuevo Stock:")
        );
        pnlFormulario.add(txtStock);

        add(pnlFormulario, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(
                new FlowLayout()
        );

        btnAtras = new JButton("Volver");
        btnModificar = new JButton("Modificar Producto");

        pnlBotones.add(btnAtras);
        pnlBotones.add(btnModificar);

        add(pnlBotones, BorderLayout.SOUTH);

        btnAtras.addActionListener(e -> {
            frame.mostrarVista("ADMIN");
        });

        btnModificar.addActionListener(e -> {
            modificarProducto();
        });
    }

    private void modificarProducto() {

        try {

            String textoId = txtId.getText().trim();
            String textoPrecio = txtPrecio.getText().trim();
            String textoStock = txtStock.getText().trim();

            if (textoId.isEmpty()
                    || textoPrecio.isEmpty()
                    || textoStock.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Debe completar todos los campos.",
                        "Campos incompletos",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            int id = Integer.parseInt(textoId);
            double nuevoPrecio = Double.parseDouble(textoPrecio);
            int nuevoStock = Integer.parseInt(textoStock);

            if (nuevoPrecio < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "El precio no puede ser negativo.",
                        "Precio inválido",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            if (nuevoStock < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "El stock no puede ser negativo.",
                        "Stock inválido",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            Producto producto = inventario.buscarProducto(id);

            if (producto == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "No existe ningún producto con el ID " + id + ".",
                        "Producto no encontrado",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            int confirmacion = JOptionPane.showConfirmDialog(
                    this,
                    "¿Desea modificar el producto \""
                            + producto.getNombre()
                            + "\"?\n\n"
                            + "Precio actual: $"
                            + producto.getPrecio()
                            + "\n"
                            + "Nuevo precio: $"
                            + nuevoPrecio
                            + "\n\n"
                            + "Stock actual: "
                            + producto.getStock()
                            + "\n"
                            + "Nuevo stock: "
                            + nuevoStock,
                    "Confirmar modificación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }

            boolean modificado = inventario.actualizarProducto(
                    id,
                    nuevoPrecio,
                    nuevoStock
            );

            if (modificado) {

                JOptionPane.showMessageDialog(
                        this,
                        "Producto modificado correctamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE
                );

                limpiarFormulario();
                frame.mostrarVista("ADMINISTRAR");

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo modificar el producto.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "El ID y el stock deben ser números enteros.\n"
                            + "El precio debe ser un número válido.",
                    "Datos inválidos",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void limpiarFormulario() {

        txtId.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
    }
}

