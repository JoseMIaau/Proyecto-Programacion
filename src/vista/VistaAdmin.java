package vista;

import modelo.Categorias;
import modelo.ClienteRegistrado;
import modelo.Compra;
import modelo.DetalleCompra;
import modelo.Inventario;
import modelo.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VistaAdmin extends JPanel {

    private final BaseFrameAdmin frame;
    private final Inventario inventario;

    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;

    private JButton btnEliminar, btnAgregar, btnModificar, btnEstadisticas, btnVaciarInventario, btnChat, btnHistorial, btnCarritosActivos;

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

        String[] columnas = { "ID", "Nombre", "Precio", "Stock Total", "Stock Reservado", "Categoría" };
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
        btnHistorial = new JButton("Gestion de Historial");
        btnCarritosActivos = new JButton("Carritos Activos");
        pnlSur.add(btnAgregar);
        pnlSur.add(btnModificar);
        pnlSur.add(btnEliminar);
        pnlSur.add(btnVaciarInventario);
        pnlSur.add(btnEstadisticas);
        pnlSur.add(btnChat);
        pnlSur.add(btnHistorial);
        pnlSur.add(btnCarritosActivos);

        add(pnlSur, BorderLayout.SOUTH);

        // Listeners de botones
        btnAgregar.addActionListener(e -> frame.mostrarVista("AGREGAR"));
        btnModificar.addActionListener(e -> frame.mostrarVista("MODIFICAR"));
        btnEliminar.addActionListener(e -> eliminarProducto());
        btnVaciarInventario.addActionListener(e -> vaciarInventario());
        btnEstadisticas.addActionListener(e -> mostrarEstadisticas());
        btnChat.addActionListener(e -> abrirChatAdmin());
        btnHistorial.addActionListener(e -> { mostrarHistorialClientes(); });
        btnCarritosActivos.addActionListener(e -> {
            if (servidor != null) {
                frame.mostrarVista("CARROS");
            } else {
                JOptionPane.showMessageDialog(this, "El servidor no está activo. No se pueden mostrar los carritos activos.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        });
    }

    // --- ACCIONES DE TABLA E INVENTARIO ---

    public void poblarTabla() {
        modeloTabla.setRowCount(0);
        for (Producto p : inventario.leerProductos()) {
            modeloTabla.addRow(new Object[] {
                    p.getId(), p.getNombre(), p.getPrecio(), p.getStock(), p.getReservado(), p.getCategoria()
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
    
    //Mostrar Clientes
    private void mostrarHistorialClientes() {
        List<ClienteRegistrado> clientes = inventario.obtenerClientesRegistrados();

        if (clientes.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Todavía no existen clientes registrados.",
                    "Historial de Clientes",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String[] columnas = {"RUN", "Nombre Completo"};

        DefaultTableModel modeloClientes = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        for (ClienteRegistrado cliente : clientes) {
            modeloClientes.addRow(new Object[]{
                    cliente.getRun(),
                    cliente.getNombre()
            });
        }

        JTable tablaClientes = new JTable(modeloClientes);
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(tablaClientes);
        scroll.setPreferredSize(new Dimension(620, 300));

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JTextField txtBuscarRun = new JTextField();

        JPanel panelBusqueda = new JPanel(new BorderLayout(8, 0));
        panelBusqueda.add(new JLabel("Buscar por RUN:"), BorderLayout.WEST);
        panelBusqueda.add(txtBuscarRun, BorderLayout.CENTER);

        panel.add(panelBusqueda, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        Object[] opciones = {"Ver Historial", "Cerrar"};

        while (true) {
            int opcion = JOptionPane.showOptionDialog(
                    this,
                    panel,
                    "Clientes Registrados",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    opciones,
                    opciones[0]
            );

            if (opcion != 0) {
                break;
            }

            String run = txtBuscarRun.getText().trim();

            if (run.isEmpty()) {
                int fila = tablaClientes.getSelectedRow();

                if (fila == -1) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Seleccione un cliente o ingrese su RUN.",
                            "Aviso",
                            JOptionPane.WARNING_MESSAGE
                    );
                    continue;
                }

                run = tablaClientes.getValueAt(fila, 0).toString();
            }

            ClienteRegistrado cliente = inventario.buscarClientePorRun(run);

            if (cliente == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "El Run ingresado no se encuentra ingresado",
                        "Cliente no encontrado",
                        JOptionPane.WARNING_MESSAGE
                );
                continue;
            }

            mostrarComprasCliente(cliente);
        }
    }

    //PARTE NUEVA - MOSTRAR COMPRAS DE UN CLIENTE
    private void mostrarComprasCliente(ClienteRegistrado cliente) {
        List<Compra> compras = inventario.buscarComprasPorRun(cliente.getRun());

        if (compras.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "El cliente no tiene compras registradas.",
                    "Historial de Compras",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        StringBuilder historial = new StringBuilder();

        historial.append("CLIENTE: ").append(cliente.getNombre()).append("\n");
        historial.append("RUN: ").append(cliente.getRun()).append("\n");
        historial.append("====================================\n\n");

        int numeroCompra = 1;

        for (Compra compra : compras) {
            historial.append("COMPRA N° ").append(numeroCompra++).append("\n");
            historial.append("Fecha: ").append(compra.getFecha()).append("\n\n");

            for (DetalleCompra detalle : compra.getDetalles()) {
                historial.append("Producto: ")
                        .append(detalle.getNombreProducto()).append("\n");

                historial.append("Cantidad: ")
                        .append(detalle.getCantidad()).append("\n");

                historial.append("Precio unitario: ")
                        .append(EstilosUI.formatearCLP(detalle.getPrecioUnitario()))
                        .append("\n");

                historial.append("Subtotal producto: ")
                        .append(EstilosUI.formatearCLP(detalle.getSubtotal()))
                        .append("\n\n");
            }

            historial.append("Subtotal: ")
                    .append(EstilosUI.formatearCLP(compra.getSubtotal()))
                    .append("\n");

            historial.append("IVA (19%): ")
                    .append(EstilosUI.formatearCLP(compra.getIva()))
                    .append("\n");

            historial.append("TOTAL: ")
                    .append(EstilosUI.formatearCLP(compra.getTotal()))
                    .append("\n");

            historial.append("------------------------------------\n\n");
        }

        JTextArea areaHistorial = new JTextArea(historial.toString());
        areaHistorial.setEditable(false);
        areaHistorial.setFont(new Font("Monospaced", Font.PLAIN, 13));
        areaHistorial.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(areaHistorial);
        scroll.setPreferredSize(new Dimension(650, 450));

        JOptionPane.showMessageDialog(
                this,
                scroll,
                "Historial de Compras - " + cliente.getNombre(),
                JOptionPane.INFORMATION_MESSAGE
        );
    }

}