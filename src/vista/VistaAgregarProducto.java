
//SE ENTRA DESDE VISTA ADMIN A ESTA PANTALLA PARA AGREGAR UN PRODUCTO

package vista;

import modelo.Categorias;
import modelo.Inventario;
import modelo.Producto;

import javax.swing.*;
import java.awt.*;
//clase inutil
public class VistaAgregarProducto extends JPanel {

    private final BaseFrame frame;
    private final Inventario inventario;

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtPrecio;
    private JTextField txtStock;

    private JComboBox<Categorias> cbxCategoria;

    private JButton btnAtras;
    private JButton btnAgregar;

    public VistaAgregarProducto(BaseFrame frame) {

        this.frame = frame;
        this.inventario = Inventario.getInstancia();

        inicializarComponentes();
    }

    private void inicializarComponentes() {

        setLayout(new BorderLayout(15, 15));

        // =========================
        // TÍTULO
        // =========================

        JLabel lblTitulo = new JLabel(
                "Agregar Nuevo Producto",
                SwingConstants.CENTER
        );

        lblTitulo.setFont(
                new Font("Arial", Font.BOLD, 24)
        );

        add(lblTitulo, BorderLayout.NORTH);

        // =========================
        // FORMULARIO
        // =========================

        JPanel pnlFormulario = new JPanel(
                new GridLayout(5, 2, 10, 15)
        );

        pnlFormulario.setBorder(
                BorderFactory.createEmptyBorder(
                        40,
                        100,
                        40,
                        100
                )
        );

        txtId = new JTextField();
        txtNombre = new JTextField();
        txtPrecio = new JTextField();
        txtStock = new JTextField();

        cbxCategoria = new JComboBox<>(
                Categorias.values()
        );

        pnlFormulario.add(
                new JLabel("ID del Producto:")
        );
        pnlFormulario.add(txtId);

        pnlFormulario.add(
                new JLabel("Nombre del Producto:")
        );
        pnlFormulario.add(txtNombre);

        pnlFormulario.add(
                new JLabel("Precio:")
        );
        pnlFormulario.add(txtPrecio);

        pnlFormulario.add(
                new JLabel("Stock:")
        );
        pnlFormulario.add(txtStock);

        pnlFormulario.add(
                new JLabel("Categoría:")
        );
        pnlFormulario.add(cbxCategoria);

        add(pnlFormulario, BorderLayout.CENTER);


        JPanel pnlBotones = new JPanel(
                new FlowLayout()
        );

        btnAtras = new JButton(
                "Volver"
        );

        btnAgregar = new JButton(
                "Agregar Producto"
        );

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


    private void agregarProducto() {

        try {

            // Obtener ID
            String textoId = txtId.getText().trim();

            // Obtener nombre
            String nombre = txtNombre.getText().trim();

            // Obtener precio
            String textoPrecio = txtPrecio.getText().trim();

            // Obtener stock
            String textoStock = txtStock.getText().trim();

            // Validar campos vacíos
            if (textoId.isEmpty()
                    || nombre.isEmpty()
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

            double precio = Double.parseDouble(
                    textoPrecio
            );

            int stock = Integer.parseInt(
                    textoStock
            );

            Categorias categoria =
                    (Categorias) cbxCategoria.getSelectedItem();


            if (id < 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "El ID no puede ser negativo.",
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


            if (inventario.buscarProducto(id) != null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Ya existe un producto con el ID "
                                + id + ".",
                        "ID duplicado",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }


            Producto nuevoProducto = new Producto(
                    id,
                    nombre,
                    precio,
                    stock,
                    categoria
            );

            boolean agregado =
                    inventario.crearProducto(nuevoProducto);


            if (agregado) {

                JOptionPane.showMessageDialog(
                        this,
                        "Producto agregado correctamente.",
                        "Producto agregado",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // Limpiar formulario
                limpiarFormulario();

                // Volver a administración
                frame.mostrarVista("ADMINISTRAR");

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo agregar el producto.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "El ID, precio y stock deben contener valores numéricos válidos.",
                    "Datos inválidos",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void limpiarFormulario() {

        txtId.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtStock.setText("");

        if (cbxCategoria.getItemCount() > 0) {
            cbxCategoria.setSelectedIndex(0);
        }
    }
}
