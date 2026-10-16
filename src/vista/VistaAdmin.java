package vista;

import modelo.Categorias;
import modelo.Inventario;
import modelo.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VistaAdmin extends JPanel {

    private final BaseFrameAdmin frame;
    private final Inventario inventario;

    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;

    private JButton btnEliminar, btnAgregar, btnModificar, btnEstadisticas, btnVaciarInventario, btnChat;

    private red.Servidor servidor;
    private JDialog ventanaChat;
    private JTextArea areaChat;

    public VistaAdmin(BaseFrameAdmin frame) {
        this.frame = frame;
        this.inventario = Inventario.getInstancia();
        inicializarComponentes();
        poblarTabla();
    }

    public void setServidor(red.Servidor servidor) {
        this.servidor = servidor;
        this.servidor.setAlRecibirMensajeAdmin(mensaje -> {
            if (areaChat != null) {
                areaChat.append(mensaje + "\n");
                areaChat.setCaretPosition(areaChat.getDocument().getLength());
            }
        });
    }

    // --- INICIALIZACION UI ---
    private void inicializarComponentes() {
        setLayout(new BorderLayout(5, 5));

        String[] columnas = { "ID", "Nombre", "Precio", "Stock", "Categoría", "Imagen" };
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
        btnVaciarInventario = new JButton("Vaciar Inventario");
        btnEstadisticas = new JButton("Estadísticas");
        btnChat = new JButton("Chat de Soporte");

        pnlSur.add(btnAgregar);
        pnlSur.add(btnModificar);
        pnlSur.add(btnEliminar);
        pnlSur.add(btnVaciarInventario);
        pnlSur.add(btnEstadisticas);
        pnlSur.add(btnChat);

        add(pnlSur, BorderLayout.SOUTH);

        // Listeners de botones
        btnAgregar.addActionListener(e -> frame.mostrarVista("AGREGAR"));
        btnModificar.addActionListener(e -> frame.mostrarVista("MODIFICAR"));
        btnEliminar.addActionListener(e -> eliminarProducto());
        btnVaciarInventario.addActionListener(e -> vaciarInventario());
        btnEstadisticas.addActionListener(e -> mostrarEstadisticas());
        btnChat.addActionListener(e -> abrirChatAdmin());
    }

    // --- ACCIONES DE TABLA E INVENTARIO ---

    public void poblarTabla() {
        modeloTabla.setRowCount(0);
        for (Producto p : inventario.leerProductos()) {
            modeloTabla.addRow(new Object[] {
                    p.getId(), p.getNombre(), p.getPrecio(), p.getStock(), p.getCategoria()
            });
        }
    }

    private void eliminarProducto() {
        int fila = tablaProductos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la tabla.", "Atención",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idProducto = Integer.parseInt(modeloTabla.getValueAt(fila, 0).toString());
        String nombreProducto = modeloTabla.getValueAt(fila, 1).toString();

        int confirmacion = JOptionPane.showConfirmDialog(
                this, "¿Está seguro de eliminar el producto \"" + nombreProducto + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (inventario.eliminarProducto(idProducto)) {
                poblarTabla();
                JOptionPane.showMessageDialog(this, "Producto eliminado correctamente.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el producto.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void vaciarInventario() {
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Estas seguro de que quieres vaciar todo el inventario?\nEsta acción no se puede deshacer",
                "Confirmacion de vaciado", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (respuesta == JOptionPane.YES_OPTION) {
            if (inventario.vaciarInventario()) {
                poblarTabla();
                JOptionPane.showMessageDialog(this, "Inventario Vaciado Correctamente", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void mostrarEstadisticas() {
        JComboBox<String> cmbCategoria = new JComboBox<>();
        cmbCategoria.addItem("INVENTARIO");
        for (Categorias categoria : Categorias.values()) {
            cmbCategoria.addItem(categoria.toString());
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Seleccione una categoría:"));
        panel.add(cmbCategoria);

        int resultado = JOptionPane.showConfirmDialog(
                this, panel, "Estadísticas de Productos",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado != JOptionPane.OK_OPTION)
            return;

        String seleccion = (String) cmbCategoria.getSelectedItem();

        if ("INVENTARIO".equals(seleccion)) {
            java.util.List<Producto> productos = inventario.leerProductos();
            int unidadesTotales = 0;
            for (Producto producto : productos) {
                unidadesTotales += producto.getStock();
            }

            double valorTotal = inventario.calculoValorTotalInventario();
            Producto menorStock = inventario.obtenerProductoPorMenorStockInventario();

            String mensaje = "RESUMEN DEL INVENTARIO\n------------------------------\n\n"
                    + "Cantidad de productos: " + productos.size() + "\n\n"
                    + "Unidades totales en stock: " + unidadesTotales + "\n\n"
                    + "Valor total del inventario: " + EstilosUI.formatearCLP(valorTotal) + "\n\n";

            if (menorStock != null) {
                mensaje += "Producto con menor stock:\n" + menorStock.getNombre() + "\nStock: "
                        + menorStock.getStock() + " unidades";
            } else {
                mensaje += "Producto con menor stock:\nNo existen productos en el inventario.";
            }

            JOptionPane.showMessageDialog(this, mensaje, "Resumen del Inventario",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Categorias categoriaSeleccionada = Categorias.valueOf(seleccion);
        double promedio = inventario.calcularPromedioPorCategoria(categoriaSeleccionada);
        Producto menorStock = inventario.obtenerProductoPorMenorStock(categoriaSeleccionada);

        String mensaje;
        if (menorStock == null) {
            mensaje = "No existen productos en la categoría " + categoriaSeleccionada + ".";
        } else {
            mensaje = "Categoría: " + categoriaSeleccionada + "\n\n"
                    + "Promedio de precios: " + EstilosUI.formatearCLP(promedio) + "\n\n"
                    + "Producto con menor stock: " + menorStock.getNombre() + "\n"
                    + "Stock: " + menorStock.getStock() + " unidades";
        }

        JOptionPane.showMessageDialog(this, mensaje, "Estadísticas", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- CHAT ---

    private void abrirChatAdmin() {
        if (ventanaChat != null && ventanaChat.isVisible()) {
            ventanaChat.toFront();
            return;
        }

        ventanaChat = new JDialog();
        ventanaChat.setTitle("Soporte a Clientes (Admin)");
        ventanaChat.setSize(350, 450);
        ventanaChat.setLayout(new BorderLayout());

        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setLineWrap(true);

        JTextField campoMensaje = new JTextField();
        JButton btnEnviar = new JButton("Enviar");

        btnEnviar.addActionListener(e -> {
            String texto = campoMensaje.getText().trim();
            if (!texto.isEmpty() && servidor != null) {
                areaChat.append("Tú (Admin): " + texto + "\n");
                servidor.difundir(red.Protocolo.createJsonMessage("Admin: " + texto));
                campoMensaje.setText("");
            }
        });
        campoMensaje.addActionListener(e -> btnEnviar.doClick());

        JPanel panelAbajo = new JPanel(new BorderLayout());
        panelAbajo.add(campoMensaje, BorderLayout.CENTER);
        panelAbajo.add(btnEnviar, BorderLayout.EAST);

        ventanaChat.add(new JScrollPane(areaChat), BorderLayout.CENTER);
        ventanaChat.add(panelAbajo, BorderLayout.SOUTH);
        ventanaChat.setLocationRelativeTo(this);

        ventanaChat.setVisible(true);
        campoMensaje.requestFocusInWindow();
    }
}