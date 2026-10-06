package vista;


import modelo.Categorias;
import modelo.Inventario;
import modelo.Producto;

import javax.swing.*;
import java.awt.*;

public class VistaAgregarProducto extends JPanel{
        private final BaseFrameAdmin frame;
        private final Inventario inventario;

        private JTextField txtId;
        private JTextField txtPrecio;
        private JTextField txtStock;

        private JButton btnAtras;
        private JButton btnAgregar;

    // Constructor
    public VistaAgregarProducto(BaseFrameAdmin frame) {

        this.frame = frame;
        this.inventario = Inventario.getInstancia();

        inicializarComponentes();
        
    }

    private void inicializarComponentes() {

        setLayout(new BorderLayout(15, 15));

        JLabel lblTitulo = new JLabel(
                "Agregar Producto",
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

        txtPrecio = new JTextField();
        txtStock = new JTextField();

        pnlFormulario.add(txtId);

        pnlFormulario.add(
                new JLabel("Precio:")
        );
        pnlFormulario.add(txtPrecio);

        pnlFormulario.add(
                new JLabel("Stock:")
        );
        pnlFormulario.add(txtStock);

        add(pnlFormulario, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(
                new FlowLayout()
        );

        btnAtras = new JButton("Volver");
        btnAgregar = new JButton("Agregar Producto");

        pnlBotones.add(btnAtras);
        pnlBotones.add(btnAgregar);

        add(pnlBotones, BorderLayout.SOUTH);

        btnAtras.addActionListener(e -> {
            frame.mostrarVista("ADMIN");
        });

        btnAgregar.addActionListener(e -> {
            agregarProducto();
        });
    }


    // Solicita los datos necesarios y crea un nuevo producto en el inventario
    private void agregarProducto() {
        // Campos para ingresar los datos
        JTextField txtNombre = new JTextField();
        JTextField txtPrecio = new JTextField();     //AQUIIIIIIIII
        JTextField txtStock = new JTextField();

        JComboBox<Categorias> cmbCategoria =
                new JComboBox<>(Categorias.values());

        JPanel panel = new JPanel();

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        panel.add(
                new JLabel("Nombre:")
        );
        panel.add(txtNombre);

        panel.add(
                new JLabel("Precio:")
        );
        panel.add(txtPrecio);

        panel.add(
                new JLabel("Stock:")
        );
        panel.add(txtStock);

        panel.add(
                new JLabel("Categoría:")
        );
        panel.add(cmbCategoria);

        // Mostrar formulario para ingresar los datos
        int resultado =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Agregar Producto",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try { //convertimos los datos en los tipos que correspondan

            String nombre =
                    txtNombre.getText().trim();

            double precio =
                    Double.parseDouble(
                            txtPrecio.getText().trim()
                    );

            int stock =
                    Integer.parseInt(
                            txtStock.getText().trim()
                    );

            Categorias categoria =
                    (Categorias)
                            cmbCategoria.getSelectedItem();

        //Validamos que no hayan campos vacios o erroneos
            if (nombre.isEmpty()) { 

                JOptionPane.showMessageDialog(
                        this,
                        "El nombre del producto no puede estar vacío.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            if (precio < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "El precio no puede ser negativo.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            if (stock < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "El stock no puede ser negativo.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            Producto producto =
                    new Producto(
                                0,
                            nombre,
                            precio,
                            stock,
                            categoria
                    );

        // Intentar registrar el producto en el inventario y confirmar cual sea el caso
            boolean agregado =
                    inventario.crearProducto(producto);

            if (agregado) {


                JOptionPane.showMessageDialog(
                        this,
                        "Producto agregado correctamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo agregar el producto.\n"
                                + "El ID puede estar repetido.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "El ID y stock deben ser números enteros.\n"
                            + "El precio debe ser un número válido.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

        // Vacía los campos después de agregar
    private void limpiarFormulario() {

        txtId.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
    }

}
