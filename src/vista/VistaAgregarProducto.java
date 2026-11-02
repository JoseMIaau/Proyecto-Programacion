package vista;

import modelo.Categorias;
import modelo.Inventario;
import modelo.Producto;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.IOException;


public class VistaAgregarProducto extends JPanel {
    private final BaseFrameAdmin frame;
    private final Inventario inventario;

    private JTextField txtNombre, txtPrecio, txtStock;
    private JComboBox<Categorias> cmbCategoria;
    private String rutaImagen;

    private JButton btnAtras, btnAgregar, btnSeleccionarImagen;

    public VistaAgregarProducto(BaseFrameAdmin frame) {
        this.frame = frame;
        this.inventario = Inventario.getInstancia();
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(15, 15));

        JLabel lblTitulo = new JLabel("Agregar Producto", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel pnlFormulario = new JPanel(new GridLayout(5, 2, 10, 15));
        pnlFormulario.setBorder(BorderFactory.createEmptyBorder(60, 100, 60, 100));

        txtNombre = new JTextField();
        txtPrecio = new JTextField();
        txtStock = new JTextField();
        cmbCategoria = new JComboBox<>(Categorias.values());
        btnSeleccionarImagen = new JButton("Seleccionar Imagen");

        pnlFormulario.add(new JLabel("Nombre:"));
        pnlFormulario.add(txtNombre);
        pnlFormulario.add(new JLabel("Precio:"));
        pnlFormulario.add(txtPrecio);
        pnlFormulario.add(new JLabel("Stock:"));
        pnlFormulario.add(txtStock);
        pnlFormulario.add(new JLabel("Categoría:"));
        pnlFormulario.add(cmbCategoria);
        pnlFormulario.add(new JLabel("Imagen:"));
        pnlFormulario.add(btnSeleccionarImagen);

        add(pnlFormulario, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout());
        btnAtras = new JButton("Volver");
        btnAgregar = new JButton("Agregar Producto");

        pnlBotones.add(btnAtras);
        pnlBotones.add(btnAgregar);
        add(pnlBotones, BorderLayout.SOUTH);

        // Listeners
        btnAtras.addActionListener(e -> frame.mostrarVista("ADMIN"));
        btnAgregar.addActionListener(e -> agregarProducto());
        btnSeleccionarImagen.addActionListener(this::btnSeleccionarImagenActionPerformed);
    }

    // --- ACCIONES Y VALIDACION ---

private void btnSeleccionarImagenActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Imagen producto:");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JPG & JPEG & PNG", "imagen", "jpg", "jpeg", "png", "gif"));
        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            try{
            java.io.File archivoOriginal = fileChooser.getSelectedFile();
            String subCarpeta = "src/imagenes/ImagenesProductos";
            java.io.File directorioDestino = new java.io.File(subCarpeta);
            if (!directorioDestino.exists()) directorioDestino.mkdirs();
            //me dio error con nombres iguales asi que nunca mas
            String nombreUnico = System.currentTimeMillis() + "_" + archivoOriginal.getName();
            java.io.File archivoDestino = new java.io.File(directorioDestino, nombreUnico);
            
            java.nio.file.Files.copy(archivoOriginal.toPath(), archivoDestino.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            rutaImagen = "src/imagenes/ImagenesProductos/" + nombreUnico;
            
            JOptionPane.showMessageDialog(this, "Imagen cargada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar la imagen: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                rutaImagen = null;
            }
        }
    }


    private void agregarProducto() {
        try {
            String nombre = txtNombre.getText().trim();
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());
            Categorias categoria = (Categorias) cmbCategoria.getSelectedItem();

            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre del producto no puede estar vacío.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (precio < 0) {
                JOptionPane.showMessageDialog(this, "El precio no puede ser negativo.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "El stock no puede ser negativo.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Producto producto = new Producto(0, nombre, precio, stock, categoria, rutaImagen);

            if (inventario.crearProducto(producto)) {
                JOptionPane.showMessageDialog(this, "Producto agregado correctamente.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo agregar el producto.\nEl ID puede estar repetido.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "El ID y stock deben ser números enteros.\nEl precio debe ser un número válido.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtPrecio.setText("");
        txtStock.setText("");
        cmbCategoria.setSelectedIndex(0);
        rutaImagen = null;
    }
}