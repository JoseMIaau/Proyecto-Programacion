package vista;

import modelo.Inventario;
import modelo.ItemCarrito;
import modelo.Compra;
import modelo.GestorPedidos;
import modelo.Pedido;
import modelo.Despacho;
import modelo.Direccion;
import modelo.TarifaEnvio;
import modelo.Cupon;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;

public class VistaCarrito extends JPanel {

    private final BaseFrameCliente frame;
    private final JPanel leftPanel;
    private final JPanel rightPanel;
    private final JLabel totalLabel;

    public VistaCarrito(BaseFrameCliente frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(EstilosUI.FONDO);

        add(createCartHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2));
        content.setBackground(EstilosUI.FONDO);

        leftPanel = new JPanel();
        leftPanel.setBackground(EstilosUI.FONDO);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(25, 40, 25, 30));
        JScrollPane scrollLeft = new JScrollPane(leftPanel);
        scrollLeft.setBorder(null);

        rightPanel = new JPanel();
        rightPanel.setBackground(EstilosUI.FONDO);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new CompoundBorder(new MatteBorder(0, 2, 0, 0, Color.LIGHT_GRAY), new EmptyBorder(30, 40, 30, 40)));

        totalLabel = new JLabel("Total: $0");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(scrollLeft);
        content.add(rightPanel);
        add(content, BorderLayout.CENTER);

        actualizarCarrito();
    }

    // --- LOGICA DEL CARRITO ---

    public void actualizarCarrito() {
        leftPanel.removeAll();
        rightPanel.removeAll();

        List<ItemCarrito> items = Inventario.getInstancia().getCarrito();

        if (items.isEmpty()) {
            JLabel lblVacio = new JLabel("El carrito está vacío.");
            lblVacio.setFont(EstilosUI.FONT_BOLD);
            leftPanel.add(lblVacio);
        } else {
            for (ItemCarrito item : items) {
                leftPanel.add(createCartRow(item));
                leftPanel.add(Box.createVerticalStrut(12));

                rightPanel.add(summaryLine(item.getProducto().getNombre() + " (x" + item.getCantidad() + ")", "$" + (int) item.getSubtotal()));
                rightPanel.add(Box.createVerticalStrut(8));
            }

            leftPanel.add(Box.createVerticalStrut(20));
            JButton btnVaciar = EstilosUI.roundedButton("vaciar carrito", new Color(255, 230, 230), new Color(180, 40, 40));
            btnVaciar.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnVaciar.setMaximumSize(new Dimension(160, 44));

            btnVaciar.addActionListener(e -> {
                int confirmacion = JOptionPane.showConfirmDialog(
                        this, "¿Estás seguro de que deseas eliminar todos los productos del carrito?",
                        "Confirmar vaciado", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
                );

                if (confirmacion == JOptionPane.YES_OPTION) {
                    for (ItemCarrito it : Inventario.getInstancia().getCarrito()) {
                        if (red.Cliente.getInstancia() != null) {
                            red.Cliente.getInstancia().enviarDatos(it.getProducto().getId(), "AGREGAR:-" + it.getCantidad());
                        }
                    }
                    Inventario.getInstancia().vaciarCarrito();
                    actualizarCarrito();
                }
            });
            leftPanel.add(btnVaciar);
        }

        rightPanel.add(Box.createVerticalStrut(20));

        double subtotal = Inventario.getInstancia().calcularSubtotal();
        double iva = Inventario.getInstancia().calcularIVA();
        double total = Inventario.getInstancia().calcularTotalCarrito();

        rightPanel.add(summaryLine("Subtotal", "$" + (int) subtotal));
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(summaryLine("IVA (19%)", "$" + (int) iva));
        rightPanel.add(Box.createVerticalStrut(8));

        totalLabel.setText("Total: $" + (int) total);
        rightPanel.add(totalLabel);
        rightPanel.add(Box.createVerticalStrut(25));

        JButton btnPagar = EstilosUI.roundedButton("PAGAR", EstilosUI.VERDE_CLARO, EstilosUI.VERDE);
        btnPagar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPagar.setMaximumSize(new Dimension(160, 44));
        btnPagar.addActionListener(e -> {
            if (items.isEmpty()) { 
                JOptionPane.showMessageDialog(this, "Debe agregar productos antes de pagar", "Carro Vacío", JOptionPane.WARNING_MESSAGE);
            } else {
                showCheckoutDialog();
            }
        });
        rightPanel.add(btnPagar);

        this.revalidate();
        this.repaint();
    }

    // --- CONSTRUCCION UI ---

    private JPanel createCartHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(EstilosUI.VERDE);
        header.setBorder(new EmptyBorder(10, 25, 10, 25));

        JButton back = EstilosUI.iconButton("☰");
        back.addActionListener(e -> frame.mostrarVista("PRODUCTOS"));
        header.add(back, BorderLayout.WEST);

        JLabel title = new JLabel("Mi carrito de compras", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.add(title, BorderLayout.CENTER);

        return header;
    }

    private JPanel createCartRow(ItemCarrito item) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setBackground(EstilosUI.FONDO);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        row.setBorder(new CompoundBorder(new LineBorder(new Color(230, 230, 230), 1, true), new EmptyBorder(8, 12, 8, 12)));

        JLabel name = new JLabel(item.getProducto().getNombre());
        name.setFont(EstilosUI.FONT_BOLD);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JLabel price = new JLabel("$" + (int) item.getSubtotal());
        price.setFont(EstilosUI.FONT_BOLD);

        JButton botonMenos = new JButton("-");
        botonMenos.setFont(new Font("SansSerif", Font.BOLD, 15));
        botonMenos.setPreferredSize(new Dimension(38, 28));
        botonMenos.setMargin(new Insets(0, 0, 0, 0));
        botonMenos.setFocusPainted(false);
        botonMenos.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JTextField txtUnits = new JTextField(String.valueOf((int) item.getCantidad()), 3);
        txtUnits.setHorizontalAlignment(JTextField.CENTER);
        txtUnits.setFont(EstilosUI.FONT_BOLD);
        txtUnits.setEditable(false);

        JButton botonMas = new JButton("+");
        botonMas.setFont(new Font("SansSerif", Font.BOLD, 15));
        botonMas.setPreferredSize(new Dimension(38, 28));
        botonMas.setMargin(new Insets(0, 0, 0, 0));
        botonMas.setFocusPainted(false);
        botonMas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton trash = new JButton("🗑");
        trash.setFont(new Font("SansSerif", Font.PLAIN, 18));
        trash.setForeground(Color.RED);
        trash.setBorderPainted(false);
        trash.setContentAreaFilled(false);
        trash.setFocusPainted(false);
        trash.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Listeners
        botonMenos.addActionListener(click -> {
            if (item.getCantidad() > 1) {
                item.getProducto().setStock(item.getProducto().getStock() + 1);
                item.setCantidad(item.getCantidad() - 1);
                if (red.Cliente.getInstancia() != null) red.Cliente.getInstancia().enviarDatos(item.getProducto().getId(), "AGREGAR:-1");
                actualizarCarrito();
            } else {
                int confirmacion = JOptionPane.showConfirmDialog(
                        this, "¿Desea quitar " + item.getProducto().getNombre() + " del carrito?",
                        "Quitar producto", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
                );
                if (confirmacion == JOptionPane.YES_OPTION) {
                    double cant = item.getCantidad();
                    Inventario.getInstancia().eliminarDelCarrito(item.getProducto().getId());
                    if (red.Cliente.getInstancia() != null) red.Cliente.getInstancia().enviarDatos(item.getProducto().getId(), "AGREGAR:-" + cant);
                    actualizarCarrito();
                }
            }
        });

        botonMas.addActionListener(click -> {
            if (item.getProducto().getStock() >= 1) {
                item.getProducto().setStock(item.getProducto().getStock() - 1);
                item.setCantidad(item.getCantidad() + 1);
                if (red.Cliente.getInstancia() != null) red.Cliente.getInstancia().enviarDatos(item.getProducto().getId(), "AGREGAR:1");
                actualizarCarrito();
            } else {
                JOptionPane.showMessageDialog(this, "No hay mas stock disponible de este producto.", "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
            }
        });

        trash.addActionListener(e -> {
            double cant = item.getCantidad();
            Inventario.getInstancia().eliminarDelCarrito(item.getProducto().getId());
            if (red.Cliente.getInstancia() != null) red.Cliente.getInstancia().enviarDatos(item.getProducto().getId(), "AGREGAR:-" + cant);
            actualizarCarrito();
        });

        actions.add(price);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(botonMenos);
        actions.add(txtUnits);
        actions.add(botonMas);
        actions.add(Box.createHorizontalStrut(8));
        actions.add(trash);

        row.add(name, BorderLayout.WEST);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private JPanel summaryLine(String name, String price) {
        JPanel line = new JPanel(new BorderLayout());
        line.setOpaque(false);

        JLabel a = new JLabel(name);
        a.setFont(new Font("SansSerif", Font.PLAIN, 15));
        JLabel b = new JLabel(price);
        b.setFont(new Font("SansSerif", Font.BOLD, 15));

        line.add(a, BorderLayout.WEST);
        line.add(b, BorderLayout.EAST);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return line;
    }

    private void showCheckoutDialog() {
        JDialog dialog = new JDialog(frame, "Finalizar Pago", true);
        dialog.setSize(480, 680);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout());

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Datos del cliente
        JTextField txtNombre = new JTextField();
        JTextField txtRun = new JTextField();

        Inventario inv = Inventario.getInstancia();
        if (inv.tieneClienteDeseosIdentificado()) {
            String runGuardado = inv.getListaDeseosActual().getIdentificadorCliente();
            if (!runGuardado.equalsIgnoreCase("INVITADO")) {
                txtRun.setText(runGuardado);
                modelo.ClienteRegistrado clientePrevio = inv.buscarClientePorRun(runGuardado);
                if (clientePrevio != null) {
                    txtNombre.setText(clientePrevio.getNombre());
                }
            }
        }

        // Cupón de descuento
        JTextField txtCupon = new JTextField();
        Cupon[] cuponAplicado = new Cupon[1];

        JButton btnAplicarCupon = EstilosUI.roundedButton(
                "Aplicar cupón",
                EstilosUI.VERDE_CLARO,
                EstilosUI.VERDE
        );
        btnAplicarCupon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Despacho a domicilio
        String[] modalidades = {"Retiro en tienda", "Despacho a domicilio"};
        JComboBox<String> cmbModalidad = new JComboBox<>(modalidades);

        JTextField txtReceptor = new JTextField();
        JTextField txtDireccion = new JTextField();
        JComboBox<TarifaEnvio> cmbZona = new JComboBox<>();

        List<TarifaEnvio> tarifas = inv.obtenerTarifasEnvio();
        for (TarifaEnvio t : tarifas) {
            cmbZona.addItem(t);
        }

        JTextField txtTarjeta = new JTextField();

        // Resumen
        JLabel lblSubtotal = new JLabel("Subtotal productos: $0");
        lblSubtotal.setFont(EstilosUI.FONT_NORMAL);

        JLabel lblDescuento = new JLabel("Descuento cupón: $0");
        lblDescuento.setFont(EstilosUI.FONT_NORMAL);

        JLabel lblEnvio = new JLabel("Costo de envío: $0");
        lblEnvio.setFont(EstilosUI.FONT_NORMAL);

        JLabel lblIva = new JLabel("IVA (19%): $0");
        lblIva.setFont(EstilosUI.FONT_NORMAL);

        JLabel lblFechaEstimada = new JLabel("Entrega estimada: -");
        lblFechaEstimada.setFont(EstilosUI.FONT_BOLD);
        lblFechaEstimada.setForeground(EstilosUI.VERDE);

        JLabel totalTxt = new JLabel("Total a pagar: $0");
        totalTxt.setFont(new Font("SansSerif", Font.BOLD, 20));

        Runnable actualizarResumen = () -> {
            String modalidad = (String) cmbModalidad.getSelectedItem();
            boolean esRetiro = "Retiro en tienda".equals(modalidad);

            txtReceptor.setEnabled(!esRetiro);
            txtDireccion.setEnabled(!esRetiro);
            cmbZona.setEnabled(!esRetiro);

            double subtotal = inv.calcularSubtotalProductos();
            double descuento = 0.0;

            if (cuponAplicado[0] != null && cuponAplicado[0].esValido()) {
                descuento = cuponAplicado[0].calcularDescuento(subtotal);
                lblDescuento.setText("Descuento cupón (" + cuponAplicado[0].getCodigo() + "): -$" + (int) descuento);
            } else {
                lblDescuento.setText("Descuento cupón: $0");
            }

            double costoEnvio = 0.0;
            String fechaTexto = "";

            if (esRetiro) {
                Despacho retiro = new Despacho();
                costoEnvio = 0.0;
                fechaTexto = retiro.getFechaEstimada();
                lblEnvio.setText("Costo de envío: $0 (Retiro en tienda)");
            } else {
                TarifaEnvio tarifa = (TarifaEnvio) cmbZona.getSelectedItem();
                if (tarifa != null) {
                    costoEnvio = inv.calcularCostoEnvio(false, tarifa.getZona());
                    fechaTexto = Despacho.calcularFechaEntrega(tarifa.getDiasHabiles());

                    if (costoEnvio == 0.0 && subtotal >= inv.getMinimoEnvioGratis()) {
                        lblEnvio.setText("Costo de envío: $0 (Envío gratis)");
                    } else {
                        lblEnvio.setText("Costo de envío: $" + (int) costoEnvio);
                    }
                } else {
                    lblEnvio.setText("Costo de envío: Zona no válida");
                    fechaTexto = "No disponible";
                }
            }

            double baseImponible = (subtotal - descuento) + costoEnvio;
            double iva = baseImponible * 0.19;
            double total = baseImponible + iva;

            lblSubtotal.setText("Subtotal productos: $" + (int) subtotal);
            lblIva.setText("IVA (19%): $" + (int) iva);
            totalTxt.setText("Total a pagar: $" + (int) total);
            lblFechaEstimada.setText("Entrega estimada: " + fechaTexto);
        };

        // Listeners
        btnAplicarCupon.addActionListener(e -> {
            String codigo = txtCupon.getText().trim();
            if (codigo.isEmpty()) {
                cuponAplicado[0] = null;
                actualizarResumen.run();
                JOptionPane.showMessageDialog(dialog, "Ingrese un código de cupón.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Cupon encontrado = inv.buscarCupon(codigo);
            if (encontrado == null || !encontrado.esValido()) {
                cuponAplicado[0] = null;
                actualizarResumen.run();
                JOptionPane.showMessageDialog(dialog, "El cupón no existe, está vencido o no tiene usos disponibles.", "Cupón inválido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            cuponAplicado[0] = encontrado;
            actualizarResumen.run();
            JOptionPane.showMessageDialog(dialog, "Cupón '" + encontrado.getCodigo() + "' aplicado.", "Cupón válido", JOptionPane.INFORMATION_MESSAGE);
        });

        cmbModalidad.addActionListener(e -> actualizarResumen.run());
        cmbZona.addActionListener(e -> actualizarResumen.run());

        body.add(new JLabel("Nombre Completo:"));
        body.add(txtNombre);
        body.add(Box.createVerticalStrut(6));

        body.add(new JLabel("RUN del cliente:"));
        body.add(txtRun);
        body.add(Box.createVerticalStrut(6));

        body.add(new JLabel("Código de cupón (opcional):"));
        body.add(txtCupon);
        body.add(Box.createVerticalStrut(6));
        body.add(btnAplicarCupon);
        body.add(Box.createVerticalStrut(10));

        body.add(new JLabel("Modalidad de entrega:"));
        body.add(cmbModalidad);
        body.add(Box.createVerticalStrut(6));

        body.add(new JLabel("Nombre de quien recibe:"));
        body.add(txtReceptor);
        body.add(Box.createVerticalStrut(6));

        body.add(new JLabel("Dirección de entrega (Curicó):"));
        body.add(txtDireccion);
        body.add(Box.createVerticalStrut(6));

        body.add(new JLabel("Zona de reparto:"));
        body.add(cmbZona);
        body.add(Box.createVerticalStrut(6));

        body.add(new JLabel("N° Tarjeta:"));
        body.add(txtTarjeta);
        body.add(Box.createVerticalStrut(14));

        body.add(lblSubtotal);
        body.add(lblDescuento);
        body.add(lblEnvio);
        body.add(lblIva);
        body.add(Box.createVerticalStrut(4));
        body.add(totalTxt);
        body.add(Box.createVerticalStrut(6));
        body.add(lblFechaEstimada);
        body.add(Box.createVerticalStrut(16));

        actualizarResumen.run();

        JButton btnConfirmar = EstilosUI.roundedButton("PAGAR AHORA", EstilosUI.VERDE_CLARO, EstilosUI.VERDE);
        btnConfirmar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConfirmar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String run = txtRun.getText().trim();
            String modalidad = (String) cmbModalidad.getSelectedItem();
            boolean esRetiro = "Retiro en tienda".equals(modalidad);

            if (nombre.isEmpty() || run.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Complete su nombre y RUN.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Despacho despachoElegido;

            if (esRetiro) {
                despachoElegido = new Despacho();
            } else {
                TarifaEnvio tarifa = (TarifaEnvio) cmbZona.getSelectedItem();
                if (tarifa == null || inv.buscarTarifaPorZona(tarifa.getZona()) == null) {
                    JOptionPane.showMessageDialog(dialog, "La zona seleccionada no existe en la tabla de tarifas.", "Zona inválida", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String receptor = txtReceptor.getText().trim();
                String calle = txtDireccion.getText().trim();
                Direccion dir = new Direccion(receptor, calle, tarifa.getZona());

                if (!dir.estaCompleta()) {
                    JOptionPane.showMessageDialog(dialog, "Complete el nombre de quien recibe y la dirección en Curicó.", "Datos de despacho incompletos", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double costoEnvio = inv.calcularCostoEnvio(false, tarifa.getZona());
                despachoElegido = new Despacho(dir, costoEnvio, tarifa.getDiasHabiles());
            }

            boolean exito = inv.procesarCompra(run, nombre, cuponAplicado[0]);

            if (exito) {
                String idCliente = inv.getIdClienteActual();
                Pedido pedido = GestorPedidos.getInstancia().obtenerUltimoPedidoPorCliente(idCliente);
                Compra ultimaCompra = inv.obtenerUltimaCompra();

                if (ultimaCompra != null) {
                    if (red.Cliente.getInstancia() != null) {
                        red.Cliente.getInstancia().enviarDatos(0, "COMPRAR");
                    }

                    String textoComprobante = ultimaCompra.generarComprobante();
                    if (cuponAplicado[0] != null) {
                        textoComprobante += "\nCupón aplicado: " + cuponAplicado[0].getCodigo();
                    }
                    textoComprobante += "\n\n==== DATOS DE ENTREGA ====";
                    textoComprobante += "\nModalidad: " + despachoElegido.getModalidadTexto();
                    if (!despachoElegido.esRetiroEnTienda()) {
                        textoComprobante += "\nDirección: " + despachoElegido.getDireccion().toString();
                    }
                    textoComprobante += "\nCosto envío: $" + (int) despachoElegido.getCostoEnvio();
                    textoComprobante += "\nFecha estimada: " + despachoElegido.getFechaEstimada();

                    if (pedido != null) {
                        textoComprobante += "\n\n==== SEGUIMIENTO DEL PEDIDO ===="
                                + "\nPedido N° " + pedido.getNumeroPedido()
                                + "\nEstado: " + pedido.getEstado();
                    }

                    JTextArea areaComprobante = new JTextArea(textoComprobante);
                    areaComprobante.setEditable(false);
                    areaComprobante.setFont(new Font("Monospaced", Font.PLAIN, 14));
                    areaComprobante.setBackground(Color.WHITE);
                    areaComprobante.setCaretPosition(0);

                    JScrollPane scrollComprobante = new JScrollPane(areaComprobante);
                    scrollComprobante.setPreferredSize(new Dimension(450, 380));

                    JOptionPane.showMessageDialog(dialog, scrollComprobante, "Comprobante de compra", JOptionPane.INFORMATION_MESSAGE);
                }

                actualizarCarrito();
                dialog.dispose();
                frame.mostrarVista("INICIO");
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al procesar el pago. Por favor revise su carrito o el cupón.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        body.add(btnConfirmar);
        dialog.add(new JScrollPane(body), BorderLayout.CENTER);
        dialog.setVisible(true);
                
    }
}