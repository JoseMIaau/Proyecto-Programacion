package vista;


import modelo.Categorias;
import modelo.Inventario;
import modelo.Producto;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

//Imports para manejo de imagenes
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.awt.*;

public class VistaAgregarProducto extends JPanel{
        private final BaseFrameAdmin frame;
        private final Inventario inventario;

        private JTextField txtNombre;
        private JTextField txtPrecio;
        private JTextField txtStock;
        private JComboBox<Categorias> cmbCategoria;
        private String rutaImagen;

        private JButton btnAtras;
        private JButton btnAgregar;
        private JButton btnSeleccionarImagen;

    // Constructor
    public VistaAgregarProducto(BaseFrameAdmin frame) {

        this.frame = frame;
        this.inventario = Inventario.getInstancia();

        inicializarComponentes();
        
    }
    private void btnSeleccionarImagenActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setDialogTitle("Seleccionar Imagen producto:");
        
        fileChooser.setFileFilter(new FileNameExtensionFilter("JPG & JPEG & PNG", "imagen", "jpg", "jpeg", "png", "gif"));
        
        int resultado = fileChooser.showOpenDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            try{
            File archivo = fileChooser.getSelectedFile();

            Path carpeta = Paths.get( "src", "imagenes", "ImagenesProductos");

            String nombreNuevo = archivo.getName();

            //Definir la ruta
            Path destino = carpeta.resolve(nombreNuevo);

            Files.copy(archivo.toPath(),destino);

            rutaImagen = destino.toString();

            System.out.println("Imagen guardada en: " + rutaImagen);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Error al guardar la imagen: " + ex.getMessage()
            );
            }
        }
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
                new GridLayout(5, 2, 10, 15)
        );

        pnlFormulario.setBorder(
                BorderFactory.createEmptyBorder(
                        60,
                        100,
                        60,
                        100
                )
        );

        txtNombre = new JTextField();
        txtPrecio = new JTextField();
        txtStock = new JTextField();
        cmbCategoria = new JComboBox<>(Categorias.values());

        pnlFormulario.add(
                new JLabel("Nombre:")
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
        pnlFormulario.add(cmbCategoria);

        btnSeleccionarImagen = new JButton("Seleccionar Imagen");

        pnlFormulario.add(new JLabel("Imagen:"));
        pnlFormulario.add(btnSeleccionarImagen);

        btnSeleccionarImagen.addActionListener(e-> {btnSeleccionarImagenActionPerformed(e);});

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
                            categoria,
                            rutaImagen
                    );
        producto.setRutaImagen(rutaImagen); // Asignamos la ruta de la imagen al producto

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
                limpiarFormulario();

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

        txtNombre.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        cmbCategoria.setSelectedIndex(0);
    }

}
