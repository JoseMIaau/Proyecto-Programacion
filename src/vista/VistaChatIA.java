package vista;

import modelo.Inventario;
import modelo.Producto;
import red.Cliente;
import red.Protocolo;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VistaChatIA extends JPanel {

    private static final String PLACEHOLDER = "Pregunta sobre productos, precios, stock o recomendaciones...";
    private static final int TIMEOUT_UI_MS = 85_000;

    private final BaseFrameCliente frame;
    private final JPanel panelMensajes;
    private final JScrollPane scrollMensajes;
    private final JTextField campoMensaje;
    private final JButton btnEnviar;

    private boolean esperandoRespuesta = false;
    private boolean callbackRegistrado = false;
    private Timer timerTimeout;

    public VistaChatIA(BaseFrameCliente frame) {
        this.frame = frame;

        setLayout(new BorderLayout());
        setBackground(EstilosUI.FONDO);

        add(crearHeader(), BorderLayout.NORTH);

        panelMensajes = new JPanel();
        panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
        panelMensajes.setBackground(new Color(248, 249, 250));
        panelMensajes.setBorder(new EmptyBorder(15, 25, 15, 25));

        scrollMensajes = new JScrollPane(panelMensajes);
        scrollMensajes.setBorder(null);
        scrollMensajes.setBackground(EstilosUI.FONDO);
        scrollMensajes.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollMensajes, BorderLayout.CENTER);

        agregarMensajeBienvenida();

        JPanel panelInferior = new JPanel(new BorderLayout(10, 0));
        panelInferior.setBackground(EstilosUI.FONDO);
        panelInferior.setBorder(new EmptyBorder(10, 30, 15, 30));

        campoMensaje = new JTextField();
        campoMensaje.setFont(new Font("SansSerif", Font.PLAIN, 14));
        campoMensaje.setBorder(new CompoundBorder(
                new LineBorder(new Color(210, 210, 210), 1, true),
                new EmptyBorder(9, 14, 9, 14)
        ));
        campoMensaje.setText(PLACEHOLDER);
        campoMensaje.setForeground(Color.GRAY);

        campoMensaje.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (campoMensaje.getText().equals(PLACEHOLDER)) {
                    campoMensaje.setText("");
                    campoMensaje.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (campoMensaje.getText().trim().isEmpty()) {
                    campoMensaje.setText(PLACEHOLDER);
                    campoMensaje.setForeground(Color.GRAY);
                }
            }
        });

        campoMensaje.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    enviarPregunta();
                }
            }
        });

        btnEnviar = EstilosUI.roundedButton("Enviar", EstilosUI.VERDE_CLARO, EstilosUI.VERDE);
        btnEnviar.setPreferredSize(new Dimension(105, 40));
        btnEnviar.addActionListener(e -> enviarPregunta());

        JPanel inputPanel = new JPanel(new BorderLayout(10, 6));
        inputPanel.setOpaque(false);
        inputPanel.add(crearPanelSugerencias(), BorderLayout.NORTH);

        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.setOpaque(false);
        inputRow.add(campoMensaje, BorderLayout.CENTER);
        inputRow.add(btnEnviar, BorderLayout.EAST);
        inputPanel.add(inputRow, BorderLayout.CENTER);

        panelInferior.add(inputPanel, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(EstilosUI.VERDE);
        header.setBorder(new EmptyBorder(10, 25, 10, 25));

        JButton btnVolver = EstilosUI.iconButton("☰");
        btnVolver.setToolTipText("Volver al Inicio");
        btnVolver.addActionListener(e -> frame.mostrarVista("INICIO"));
        header.add(btnVolver, BorderLayout.WEST);

        JPanel centroTitulo = new JPanel();
        centroTitulo.setLayout(new BoxLayout(centroTitulo, BoxLayout.Y_AXIS));
        centroTitulo.setOpaque(false);

        JLabel title = new JLabel("🤖 Asistente SuperCuricó", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        centroTitulo.add(title);
        header.add(centroTitulo, BorderLayout.CENTER);

        JPanel accionesDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accionesDerecha.setOpaque(false);

        JButton btnCarrito = EstilosUI.iconButton("🛒");
        btnCarrito.setToolTipText("Ir al carrito");
        btnCarrito.addActionListener(e -> frame.mostrarVista("CARRO"));

        JButton btnLimpiar = EstilosUI.iconButton("🗑");
        btnLimpiar.setToolTipText("Reiniciar conversación");
        btnLimpiar.addActionListener(e -> limpiarChat());

        accionesDerecha.add(btnCarrito);
        accionesDerecha.add(btnLimpiar);
        header.add(accionesDerecha, BorderLayout.EAST);

        return header;
    }

    private JPanel crearPanelSugerencias() {
        JPanel sugerencias = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        sugerencias.setOpaque(false);

        String[] prompts = {
            "¿Qué productos tienen disponibles?",
            "Necesito algo para un asado por menos de $10.000",
            "¿Cuál es el producto más barato?"
        };

        for (String sug : prompts) {
            JButton btn = new JButton(sug);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
            btn.setForeground(EstilosUI.VERDE);
            btn.setBackground(new Color(232, 245, 233));
            btn.setBorder(new CompoundBorder(
                    new LineBorder(EstilosUI.VERDE_CLARO, 1, true),
                    new EmptyBorder(5, 10, 5, 10)
            ));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                if (esperandoRespuesta) return;
                campoMensaje.setText(sug);
                campoMensaje.setForeground(Color.BLACK);
                enviarPregunta();
            });
            sugerencias.add(btn);
        }
        return sugerencias;
    }

    private void registrarCallback() {
        if (callbackRegistrado) return;
        Cliente cliente = Cliente.getInstancia();
        if (cliente != null) {
            cliente.setAlRecibirRespuestaIA(respuesta -> SwingUtilities.invokeLater(() -> recibirRespuesta(respuesta)));
            callbackRegistrado = true;
        }
    }

    private void enviarPregunta() {
        String pregunta = campoMensaje.getText().trim();
        if (pregunta.isEmpty() || pregunta.equals(PLACEHOLDER) || esperandoRespuesta) {
            return;
        }

        Cliente cliente = Cliente.getInstancia();
        if (cliente == null || !cliente.estaConectado()) {
            agregarMensajeError("No hay conexión con el servidor Administrador. Verifica que el servidor esté encendido y conectado.");
            return;
        }

        registrarCallback();

        agregarMensajeUsuario(pregunta);
        campoMensaje.setText("");

        setEstadoEsperando(true);
        agregarIndicadorCarga();

        boolean enviado = cliente.enviarDatos(0, Protocolo.CONSULTA_IA + pregunta);
        if (!enviado) {
            detenerTimerTimeout();
            quitarIndicadorCarga();
            setEstadoEsperando(false);
            agregarMensajeError("No se pudo enviar la consulta al servidor (conexión perdida).");
            return;
        }

        iniciarTimerTimeout();
    }

    private void iniciarTimerTimeout() {
        detenerTimerTimeout();
        timerTimeout = new Timer(TIMEOUT_UI_MS, e -> {
            if (esperandoRespuesta) {
                quitarIndicadorCarga();
                setEstadoEsperando(false);
                agregarMensajeError("Tiempo de espera agotado al esperar respuesta del asistente IA. Intenta nuevamente.");
            }
        });
        timerTimeout.setRepeats(false);
        timerTimeout.start();
    }

    private void detenerTimerTimeout() {
        if (timerTimeout != null) {
            timerTimeout.stop();
            timerTimeout = null;
        }
    }

    private void setEstadoEsperando(boolean esperando) {
        this.esperandoRespuesta = esperando;
        campoMensaje.setEnabled(!esperando);
        btnEnviar.setEnabled(!esperando);
        if (!esperando) {
            campoMensaje.requestFocusInWindow();
        }
    }

    private void recibirRespuesta(String respuesta) {
        detenerTimerTimeout();
        quitarIndicadorCarga();

        if (respuesta.startsWith("ERROR:")) {
            agregarMensajeError(respuesta.substring(6));
        } else {
            List<Integer> idsProductos = extraerIdsProductos(respuesta);
            String textoLimpio = respuesta.replaceAll("\\[ID:\\d+\\]", "").replaceAll(" +", " ").trim();

            agregarMensajeIA(textoLimpio);

            if (!idsProductos.isEmpty()) {
                agregarBotonesCarrito(idsProductos);
            }
        }

        setEstadoEsperando(false);
    }

    private void agregarMensajeBienvenida() {
        JPanel panel = crearPanelBurbuja(
                new Color(235, 247, 238),
                "🤖 Asistente SuperCuricó ",
                "¡Hola! Soy el asistente virtual de SuperCuricó.\n\n"
                + "Puedo ayudarte con nuestro catálogo real en tiempo real:\n"
                + " • Recomendaciones y comparaciones de productos\n"
                + " • Precios, categorías y disponibilidad de stock\n"
                + " • Preguntas de seguimiento sobre lo que conversamos\n\n"
                + " Asistente SuperCuricó es una IA y puede cometer errores."
        );
        panelMensajes.add(panel);
        panelMensajes.add(Box.createVerticalStrut(12));
    }

    private void agregarMensajeUsuario(String texto) {
        JPanel panel = crearPanelBurbuja(new Color(225, 238, 255), "🧑 Tú", texto);
        panelMensajes.add(panel);
        panelMensajes.add(Box.createVerticalStrut(12));
        scrollAlFinal();
    }

    private void agregarMensajeIA(String texto) {
        JPanel panel = crearPanelBurbuja(new Color(240, 248, 240), "🤖 Asistente IA (respuesta generada por IA)", texto);
        panelMensajes.add(panel);
        panelMensajes.add(Box.createVerticalStrut(8));
        scrollAlFinal();
    }

    private void agregarMensajeError(String error) {
        JPanel panel = crearPanelBurbuja(new Color(255, 235, 235), "❌ Error del Asistente IA", error);
        panelMensajes.add(panel);
        panelMensajes.add(Box.createVerticalStrut(12));
        scrollAlFinal();
    }

    private void agregarIndicadorCarga() {
        JPanel panel = crearPanelBurbuja(
                new Color(250, 250, 235),
                "🤖 Asistente IA",
                "⏳ Consultando a la IA con el stock actual..."
        );
        panel.setName("indicador_carga");
        panelMensajes.add(panel);
        panelMensajes.add(Box.createVerticalStrut(8));
        scrollAlFinal();
    }

    private void quitarIndicadorCarga() {
        for (int i = panelMensajes.getComponentCount() - 1; i >= 0; i--) {
            Component comp = panelMensajes.getComponent(i);
            if (comp instanceof JPanel && "indicador_carga".equals(comp.getName())) {
                panelMensajes.remove(i);
                if (i < panelMensajes.getComponentCount()) {
                    panelMensajes.remove(i);
                }
                break;
            }
        }
        panelMensajes.revalidate();
        panelMensajes.repaint();
    }

    private void agregarBotonesCarrito(List<Integer> idsProductos) {
        JPanel panelContenedor = new JPanel();
        panelContenedor.setLayout(new BoxLayout(panelContenedor, BoxLayout.Y_AXIS));
        panelContenedor.setOpaque(false);
        panelContenedor.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel("✨ Productos recomendados (agregar al carrito):");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(EstilosUI.VERDE);
        lbl.setBorder(new EmptyBorder(0, 5, 5, 0));
        panelContenedor.add(lbl);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panelBotones.setOpaque(false);

        boolean hayBotones = false;
        for (int id : idsProductos) {
            Producto p = Inventario.getInstancia().buscarProducto(id);
            if (p == null) continue;
            hayBotones = true;

            JButton btn = EstilosUI.roundedButton(
                    "🛒 Agregar " + p.getNombre() + " (" + EstilosUI.formatearCLP(p.getPrecio()) + ")",
                    EstilosUI.VERDE_CLARO,
                    EstilosUI.VERDE
            );
            btn.setFont(new Font("SansSerif", Font.BOLD, 12));

            btn.addActionListener(e -> {
                Cliente cl = Cliente.getInstancia();
                if (cl == null || !cl.estaConectado()) {
                    JOptionPane.showMessageDialog(frame,
                            "Sin conexión con el servidor. No se puede reservar stock.",
                            "Sin conexión", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (Inventario.getInstancia().agregarAlCarrito(p, 1)) {
                    cl.enviarDatos(p.getId(), Protocolo.AGREGAR + 1);
                    frame.getVistaProductos().refrescarVistaActual();
                    frame.getVistaCarrito().actualizarCarrito();
                    JOptionPane.showMessageDialog(frame,
                            "¡'" + p.getNombre() + "' agregado al carrito!",
                            "Producto agregado", JOptionPane.INFORMATION_MESSAGE);
                    if (p.getStock() <= 0) {
                        btn.setEnabled(false);
                        btn.setText("Sin más stock (" + p.getNombre() + ")");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "No se puede agregar '" + p.getNombre() + "': sin stock disponible (" + p.getStock() + ").",
                            "Stock Insuficiente", JOptionPane.WARNING_MESSAGE);
                }
            });
            panelBotones.add(btn);
        }

        if (!hayBotones) return;

        panelContenedor.add(panelBotones);
        panelMensajes.add(panelContenedor);
        panelMensajes.add(Box.createVerticalStrut(12));
        scrollAlFinal();
    }

    private JPanel crearPanelBurbuja(Color fondo, String emisor, String texto) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(fondo);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(215, 220, 215), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblEmisor = new JLabel(emisor);
        lblEmisor.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblEmisor.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblEmisor);
        panel.add(Box.createVerticalStrut(6));

        JTextArea areaTexto = new JTextArea(texto);
        areaTexto.setEditable(false);
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        areaTexto.setFont(new Font("SansSerif", Font.PLAIN, 13));
        areaTexto.setBackground(fondo);
        areaTexto.setBorder(null);
        areaTexto.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(areaTexto);

        return panel;
    }

    private List<Integer> extraerIdsProductos(String texto) {
        List<Integer> ids = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[ID:(\\d+)\\]");
        Matcher matcher = pattern.matcher(texto);
        while (matcher.find()) {
            try {
                int id = Integer.parseInt(matcher.group(1));
                if (!ids.contains(id)) {
                    ids.add(id);
                }
            } catch (NumberFormatException ignored) {}
        }
        return ids;
    }

    private void scrollAlFinal() {
        panelMensajes.revalidate();
        panelMensajes.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollMensajes.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void limpiarChat() {
        if (esperandoRespuesta) return;
        Cliente cl = Cliente.getInstancia();
        if (cl != null && cl.estaConectado()) {
            cl.enviarDatos(0, Protocolo.LIMPIAR_IA);
        }
        panelMensajes.removeAll();
        agregarMensajeBienvenida();
        panelMensajes.revalidate();
        panelMensajes.repaint();
    }
}
