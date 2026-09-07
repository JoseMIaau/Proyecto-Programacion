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
    private JButton btnModificar;
    private JButton btnEstadisticas;
    private JButton btnVolver;

    public VistaAdmin(BaseFrame frame) {

        this.frame = frame;
        this.inventario = Inventario.getInstancia();

        inicializarComponentes();
        poblarTabla();
    }

    private void inicializarComponentes() {

        setLayout(new BorderLayout(5, 5));

        String[] columnas = {
            "ID",
            "Nombre",
            "Precio",
            "Stock",
            "Categoría"
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
        btnModificar = new JButton("Modificar Producto");
        btnEliminar = new JButton("Eliminar Producto");
        btnEstadisticas = new JButton("Estadísticas");
        btnVolver = new JButton("Volver al Menu Principal");

        pnlSur.add(btnAgregar);
        pnlSur.add(btnModificar);
        pnlSur.add(btnEliminar);
        pnlSur.add(btnEstadisticas);
        pnlSur.add(btnVolver);

        add(pnlSur, BorderLayout.SOUTH);

        // Volver al menú principal
        btnVolver.addActionListener(e -> {
            frame.mostrarVista("INICIO");
        });

        // Agregar producto
        btnAgregar.addActionListener(e -> agregarProducto());

        // Modificar producto
        btnModificar.addActionListener(e -> {
            frame.mostrarVista("MODIFICAR_PRODUCTO");
        });

        // Eliminar producto
        btnEliminar.addActionListener(e -> eliminarProducto());

        // Estadísticas
        btnEstadisticas.addActionListener(e -> mostrarEstadisticas());
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
                    0,
                    nombre,
                    precio,
                    stock,
                    categoria
            );

            boolean agregado =
                    inventario.crearProducto(producto);

            if (agregado) {

                poblarTabla();

                JOptionPane.showMessageDialog(
                        this,
                        "Producto agregado correctamente.",
                        "Éxito al agregar",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo agregar el producto.\n"
                        + "El ID puede estar repetido o el precio/stock ser inválido.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "El precio y stock deben ser valores numéricos.",
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
                    "Atencion",
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
                "¿Esta seguro de eliminar el producto \""
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
                    "Exito",
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

    private void mostrarEstadisticas() {


        JComboBox<String> cmbCategoria = new JComboBox<>();
//AGREGAR LA CATEGORIA DE INVENTARIO PARA MOSTRAR LAS STATS DEL INVENTARIO COMPLETO
        cmbCategoria.addItem("INVENTARIO");

// categorias normales
        for (Categorias categoria : Categorias.values()) {
            cmbCategoria.addItem(categoria.toString());
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Seleccione una categoría:"));
        panel.add(cmbCategoria);

        int resultado = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Estadísticas de Productos",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        String seleccion =
                (String) cmbCategoria.getSelectedItem();


        if ("INVENTARIO".equals(seleccion)) {

            
            java.util.List<Producto> productos =
                    inventario.leerProductos();


            int cantidadProductos = productos.size();

            int unidadesTotales = 0;

            for (Producto producto : productos) {
                unidadesTotales += producto.getStock();
            }
            double valorTotal =
                    inventario.calculoValorTotalInventario();

            Producto menorStock =
                    inventario.obtenerProductoPorMenorStockInventario();

            String mensaje =
                    "RESUMEN DEL INVENTARIO\n"
                    + "------------------------------\n\n"
                    + "Cantidad de productos: "
                    + cantidadProductos
                    + "\n\n"
                    + "Unidades totales en stock: "
                    + unidadesTotales
                    + "\n\n"
                    + "Valor total del inventario: $"
                    + String.format("%.0f", valorTotal)
                    + "\n\n";

            if (menorStock != null) {

                mensaje +=
                        "Producto con menor stock:\n"
                        + menorStock.getNombre()
                        + "\nStock: "
                        + menorStock.getStock()
                        + " unidades";

            } else {

                mensaje +=
                        "Producto con menor stock:\n"
                        + "No existen productos en el inventario.";
            }

            JOptionPane.showMessageDialog(
                    this,
                    mensaje,
                    "Resumen del Inventario",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }


        Categorias categoriaSeleccionada = null;

        for (Categorias categoria : Categorias.values()) {

            if (categoria.toString().equals(seleccion)) {
                categoriaSeleccionada = categoria;
                break;
            }
        }

        double promedio =
                inventario.calcularPromedioPorCategoria(
                        categoriaSeleccionada
                );

        Producto menorStock =
                inventario.obtenerProductoPorMenorStock(
                        categoriaSeleccionada
                );

        String mensaje;

        if (menorStock == null) {

            mensaje =
                    "No existen productos en la categoría "
                    + categoriaSeleccionada
                    + ".";

        } else {

            mensaje =
                    "Categoría: "
                    + categoriaSeleccionada
                    + "\n\n"
                    + "Promedio de precios: $"
                    + String.format("%.0f", promedio)
                    + "\n\n"
                    + "Producto con menor stock: "
                    + menorStock.getNombre()
                    + "\n"
                    + "Stock: "
                    + menorStock.getStock()
                    + " unidades";
        }

        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Estadísticas",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void poblarTabla() {

        modeloTabla.setRowCount(0);

        for (Producto p : inventario.leerProductos()) {

            modeloTabla.addRow(new Object[]{
                    p.getId(),
                    p.getNombre(),
                    p.getPrecio(),
                    p.getStock(),
                    p.getCategoria()
            });
        }
    }
}