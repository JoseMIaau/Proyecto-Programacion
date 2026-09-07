//VISTA PARA ADMINISTRAR LA TABLA Y PARA ELIMINAR 
package vista;

import modelo.Categorias;
import modelo.Inventario;
import modelo.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VistaAdmin extends JPanel {

    private final BaseFrame frame;
    private final Inventario inventario;

    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;

    private JButton btnEliminar;
    private JButton btnAgregar;
    private JButton btnVolver;

    public VistaAdmin(BaseFrame frame) {

        this.frame = frame;
        this.inventario = Inventario.getInstancia();

        inicializarComponentes();
        poblarTabla();
    }

    private void inicializarComponentes() {

        setLayout(new BorderLayout(5, 5));

        String[] columnas = {"ID", "Nombre", "Precio", "Stock", "Categoría"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProductos = new JTable(modeloTabla);

        add(new JScrollPane(tablaProductos), BorderLayout.CENTER);

        JPanel pnlSur = new JPanel(new FlowLayout());

        btnAgregar = new JButton("Agregar Producto");
        btnEliminar = new JButton("Eliminar Producto");
        btnVolver = new JButton("Volver al Menu Principal");
        //javi agreguemos aqui el btnModificar y que en el action listener que haga lo que estaba haceidno la clase de modificar producto
        pnlSur.add(btnAgregar);
        pnlSur.add(btnEliminar);
        pnlSur.add(btnVolver);

        add(pnlSur, BorderLayout.SOUTH);

        btnVolver.addActionListener(e -> {

            frame.mostrarVista("INICIO");

        });

        btnAgregar.addActionListener(e -> agregarProducto());

        btnEliminar.addActionListener(e -> eliminarProducto());
    }

    private void agregarProducto() {

        JTextField txtNombre = new JTextField();
        JTextField txtPrecio = new JTextField();
        JTextField txtStock = new JTextField();

        JComboBox<Categorias> cmbCategoria =
                new JComboBox<>(Categorias.values());

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);

        panel.add(new JLabel("Precio:"));
        panel.add(txtPrecio);

        panel.add(new JLabel("Stock:"));
        panel.add(txtStock);

        panel.add(new JLabel("Categoría:"));
        panel.add(cmbCategoria);

        int resultado = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Agregar Producto",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            String nombre = txtNombre.getText().trim();

            double precio = Double.parseDouble(
                    txtPrecio.getText().trim()
            );

            int stock = Integer.parseInt(
                    txtStock.getText().trim()
            );

            Categorias categoria =
                    (Categorias) cmbCategoria.getSelectedItem();

            if (nombre.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "El nombre del producto no puede estar vacío.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            Producto producto = new Producto(
                    0, nombre, precio, stock, categoria
            );

            boolean agregado =
                    inventario.crearProducto(producto);

            if (agregado) {

                poblarTabla();

                JOptionPane.showMessageDialog(
                        this,
                        "Producto agregado correctamente.",
                        "exito al agregar",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo agregar el producto.\n"
                        + "El ID puede estar repetido o el precio/stock ser inválido.",
                        "Error  ",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "El ID, precio y stock deben ser valores numéricos.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void eliminarProducto() {

        int fila = tablaProductos.getSelectedRow();

        if (fila == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un producto de la tabla.",
                    "AtenciOn",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int idProducto = Integer.parseInt(
                modeloTabla.getValueAt(fila, 0).toString()
        );

        String nombreProducto =
                modeloTabla.getValueAt(fila, 1).toString();

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de eliminar el producto \""
                        + nombreProducto + "\"?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        boolean eliminado =
                inventario.eliminarProducto(idProducto);

        if (eliminado) {

            poblarTabla();

            JOptionPane.showMessageDialog(
                    this,
                    "Producto eliminado correctamente.",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo eliminar el producto.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void poblarTabla() {

        modeloTabla.setRowCount(0);

        for (Producto p : inventario.leerProductos()) {

            modeloTabla.addRow(new Object[] {
                    p.getId(),
                    p.getNombre(),
                    p.getPrecio(),
                    p.getStock(),
                    p.getCategoria()
            });
        }
    }
}



//REVISAR EL INVENTARIO YA QUE ESTE CODIGO FUE EXTRAIDO DE UN PROYECTO ANTERIORMENTE HECHO, POR LO CUAL NECESITA LOS PRODUCTOS DE MANERA DIFERENTE