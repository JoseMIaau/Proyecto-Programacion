package vista;

import modelo.Categorias;
import modelo.Inventario;
import modelo.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Vista del admin, permite ver, agregar, modificar y eliminar productos del inventario
public class VistaAdmin extends JPanel {

// Inventario y componentes de la tabla
    private final BaseFrameAdmin frame;
    private final Inventario inventario;

    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;

    private JButton btnEliminar;
    private JButton btnAgregar;
    private JButton btnModificar;
    private JButton btnEstadisticas;
    private JButton btnVaciarInventario;
    private JButton btnChat;
    private red.Servidor servidor;


// Constructor
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

    // Crea y configura los componentes gráficos de la vista
    private void inicializarComponentes() {

        setLayout(new BorderLayout(5, 5));

        // Definimos las columnas que tendra la tabla del inventario
        String[] columnas = {
                "ID",
                "Nombre",
                "Precio",
                "Stock",
                "Categoría",
                "Imagen"
        };

        // Crea el modelo de la tabla, tambien impedir la edición directa de las celdas
        modeloTabla = new DefaultTableModel(columnas, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaProductos = new JTable(modeloTabla);

        add(
                new JScrollPane(tablaProductos),
                BorderLayout.CENTER
        );

        JPanel pnlSur = new JPanel(
                new FlowLayout()
        );

        // Creamos los botones de administración de productos
        btnAgregar = new JButton("Agregar Producto");
        btnModificar = new JButton("Modificar Producto");
        btnEliminar = new JButton("Eliminar Producto");
        btnVaciarInventario = new JButton("Vaciar Inventario");
        btnEstadisticas = new JButton("Estadísticas");
        
        

        pnlSur.add(btnAgregar);
        pnlSur.add(btnModificar);
        pnlSur.add(btnEliminar);
        pnlSur.add(btnVaciarInventario);
        pnlSur.add(btnEstadisticas);


        btnChat = new JButton("Chat de Soporte");
        pnlSur.add(btnChat);
        btnChat.addActionListener(e -> abrirChatAdmin());

        add(pnlSur, BorderLayout.SOUTH);

        // IR A LA VISTA AGREGAR PRODUCTO
        btnAgregar.addActionListener(e -> {
            frame.mostrarVista("AGREGAR");
        });

        // IR A LA VISTA DE MODIFICAR PRODUCTO
        btnModificar.addActionListener(a -> {
            frame.mostrarVista("MODIFICAR");
        });

        // ELIMINAR PRODUCTO
        btnEliminar.addActionListener(a -> {
            eliminarProducto();
        });

        // VACIAR INVENTARIO COMPLETO
        btnVaciarInventario.addActionListener(e -> {
                vaciarInventario();

        });

        //MOSTRAR LAS STATS DEL SUPERMERCADO (SE PERDIO ESTE ACTIONLISTENER AL RESOLVER EL MERGE)
        btnEstadisticas.addActionListener(a -> {
        mostrarEstadisticas();
});
    }


    // Elimina el producto seleccionado en la tabla
    private void eliminarProducto() {

        int fila = //Obtenemos la fila seleccionada
                tablaProductos.getSelectedRow();

        if (fila == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Seleccione un producto de la tabla.",
                    "Atención",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int idProducto =
                Integer.parseInt(
                        modeloTabla
                                .getValueAt(fila, 0)
                                .toString()
                );

        String nombreProducto =
                modeloTabla
                        .getValueAt(fila, 1)
                        .toString();

        int confirmacion = // Solicitar confirmación antes de eliminar el producto
                JOptionPane.showConfirmDialog(
                        this,
                        "¿Está seguro de eliminar el producto \""
                                + nombreProducto
                                + "\"?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (confirmacion != JOptionPane.YES_OPTION) { 
            return;
        }

        // Eliminar el producto del inventario
        boolean eliminado =
                inventario.eliminarProducto(
                        idProducto
                );

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


    //Vaciar Inventario
   private void vaciarInventario() {
    int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Estas seguro de que quieres vaciar todo el inventario?\nEsta acción no se puede deshacer",
            "Confirmacion de vaciado",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
    );

    if (respuesta == JOptionPane.YES_OPTION) {

        boolean exito = inventario.vaciarInventario();

        if (exito) {
            // SOUTS DE SEGUIMIENTO PARA EL VACIADO DEL INVENTARIO
            System.out.println("Vaciado");

            poblarTabla();

            System.out.println("Esta poblando la tabla");

            JOptionPane.showMessageDialog(
                    this,
                    "Inventario Vaciado Correctamente",
                    "Exito",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
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
                    + "Valor total del inventario: "
                    + EstilosUI.formatearCLP(valorTotal)
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
                    + "Promedio de precios: "
                    + EstilosUI.formatearCLP(promedio)
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

    // Carga en la tabla todos los productos actualmente registrados
    public void poblarTabla() {

        modeloTabla.setRowCount(0);

        for (Producto p :
                inventario.leerProductos()) {

            modeloTabla.addRow(
                    new Object[]{
                        
                            p.getId(),
                            p.getNombre(),
                            p.getPrecio(),
                            p.getStock(),
                            p.getCategoria()
                    }
            );
        }
    }

    private JDialog ventanaChat;
    private JTextArea areaChat;
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