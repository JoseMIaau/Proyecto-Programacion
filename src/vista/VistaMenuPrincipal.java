package vista;

import modelo.Categorias;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class VistaMenuPrincipal extends JPanel {

    private final BaseFrameCliente frame;
    private int indicePromo = 0;

    private JDialog ventanaChat;
    private JTextArea areaChat;

    public VistaMenuPrincipal(BaseFrameCliente frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(EstilosUI.FONDO);

        add(createHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setBackground(EstilosUI.FONDO);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(25, 60, 30, 60));

        iniciarBannerRotativo(center);
        generarTarjetasCategorias(center);
        agregarBotonesInferiores(center);

        add(center, BorderLayout.CENTER);
    }

    // --- HEADER Y BUSCADOR ---
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(EstilosUI.VERDE);
        header.setBorder(new EmptyBorder(12, 28, 12, 28));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        left.setOpaque(false);

        JLabel logo = new JLabel("◉  SuperCuricó");
        logo.setFont(new Font("SansSerif", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        left.add(logo);
        header.add(left, BorderLayout.WEST);

        // Buscador
        String placeholder = "Buscar producto y presionar Enter...";
        JTextField search = new JTextField(placeholder);
        search.setFont(EstilosUI.FONT_NORMAL);
        search.setForeground(Color.GRAY);
        search.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(7, 15, 7, 15)));

        search.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (search.getText().equals(placeholder)) {
                    search.setText("");
                    search.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (search.getText().trim().isEmpty()) {
                    search.setText(placeholder);
                    search.setForeground(Color.GRAY);
                }
            }
        });

        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String texto = search.getText().trim();
                    if (!texto.isEmpty() && !texto.equals(placeholder)) {
                        frame.buscarProductosPorTexto(texto);
                    }
                }
            }
        });

        header.add(search, BorderLayout.CENTER);

        // Carrito
        JButton carro = EstilosUI.iconButton("🛒");
        carro.addActionListener(e -> frame.mostrarVista("CARRO"));

        JPanel contenedor = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        contenedor.setOpaque(false);
        contenedor.add(carro);
        header.add(contenedor, BorderLayout.EAST);

        return header;
    }

    // --- COMPONENTES VISUALES ---

    private void iniciarBannerRotativo(JPanel center) {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setMaximumSize(new Dimension(800, 288));
        banner.setPreferredSize(new Dimension(800, 288));
        banner.setOpaque(false);
        banner.setBorder(null);

        String[] imagenesBanner = {
                "/imagenes/Banners/banner_verduras.png",
                "/imagenes/Banners/banner_carnes_y_lacteos.png",
                "/imagenes/Banners/banner_panaderia.png"
        };

        JLabel bannerImagen = new JLabel();
        bannerImagen.setHorizontalAlignment(SwingConstants.CENTER);

        ImageIcon iconoOriginal = new ImageIcon(getClass().getResource(imagenesBanner[0]));
        Image imagenEscalada = iconoOriginal.getImage().getScaledInstance(800, 288, Image.SCALE_SMOOTH);
        bannerImagen.setIcon(new ImageIcon(imagenEscalada));
        banner.add(bannerImagen, BorderLayout.CENTER);

        Timer timerBanner = new Timer(3000, e -> {
            indicePromo = (indicePromo + 1) % imagenesBanner.length;
            ImageIcon nuevoIcono = new ImageIcon(getClass().getResource(imagenesBanner[indicePromo]));
            Image nuevaImagen = nuevoIcono.getImage().getScaledInstance(800, 288, Image.SCALE_SMOOTH);
            bannerImagen.setIcon(new ImageIcon(nuevaImagen));
        });
        timerBanner.start();

        center.add(banner);
        center.add(Box.createVerticalStrut(25));
    }

    private void generarTarjetasCategorias(JPanel center) {
        JPanel categoriesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
        categoriesPanel.setOpaque(false);

        for (Categorias cat : Categorias.values()) {
            categoriesPanel.add(createCategoryCard(cat));
        }

        JScrollPane scrollCat = new JScrollPane(categoriesPanel);
        scrollCat.setOpaque(false);
        scrollCat.getViewport().setOpaque(false);
        scrollCat.setBorder(null);

        center.add(scrollCat);
        center.add(Box.createVerticalStrut(20));
    }

    private JPanel createCategoryCard(Categorias cat) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        String ruta = "/imagenes/categorias/" + cat.name().toLowerCase() + ".png";
        ImageIcon iconoOriginal = new ImageIcon(getClass().getResource(ruta));
        Image imagenOriginal = iconoOriginal.getImage();

        int maximo = 210;
        double escala = Math.min((double) maximo / iconoOriginal.getIconWidth(),
                (double) maximo / iconoOriginal.getIconHeight());
        Image imagenEscalada = imagenOriginal.getScaledInstance((int) (iconoOriginal.getIconWidth() * escala),
                (int) (iconoOriginal.getIconHeight() * escala), Image.SCALE_SMOOTH);

        JButton boton = new JButton(new ImageIcon(imagenEscalada));
        boton.setPreferredSize(new Dimension(120, 120));
        boton.setMaximumSize(new Dimension(120, 120));
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setFocusPainted(false);
        boton.setOpaque(false);
        boton.setMargin(new Insets(0, 0, 0, 0));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.addActionListener(e -> frame.mostrarProductosPorCategoria(cat));

        JLabel label = new JLabel(cat.name().replace("_", " "));
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(boton);
        p.add(Box.createVerticalStrut(6));
        p.add(label);

        return p;
    }

    private void agregarBotonesInferiores(JPanel center) {
        JButton enter = EstilosUI.roundedButton("VER CATÁLOGO COMPLETO", EstilosUI.VERDE_CLARO, EstilosUI.VERDE);
        enter.setAlignmentX(Component.CENTER_ALIGNMENT);
        enter.setMaximumSize(new Dimension(250, 45));
        enter.addActionListener(e -> frame.mostrarVista("PRODUCTOS"));

        JButton chat = EstilosUI.roundedButton("🗪", EstilosUI.VERDE_CLARO, EstilosUI.VERDE);
        chat.addActionListener(e -> abrirChat());

        JPanel botonesAbajo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 180, 0));
        botonesAbajo.setOpaque(false);
        botonesAbajo.add(enter);
        botonesAbajo.add(chat);
        botonesAbajo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        center.add(botonesAbajo);
    }

    // --- CHAT GLOBAL ---

    private void abrirChat() {
        if (ventanaChat != null && ventanaChat.isVisible()) {
            ventanaChat.toFront();
            return;
        }
        ventanaChat = new JDialog();
        ventanaChat.setTitle("Chat Global");
        ventanaChat.setSize(350, 450);
        ventanaChat.setLayout(new BorderLayout());

        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setLineWrap(true);
        areaChat.setWrapStyleWord(true);

        JTextField campoMensaje = new JTextField();
        JButton btnEnviar = new JButton("Enviar");

        JPanel panelAbajo = new JPanel(new BorderLayout());
        panelAbajo.add(campoMensaje, BorderLayout.CENTER);
        panelAbajo.add(btnEnviar, BorderLayout.EAST);

        ventanaChat.add(new JScrollPane(areaChat), BorderLayout.CENTER);
        ventanaChat.add(panelAbajo, BorderLayout.SOUTH);
        ventanaChat.setLocationRelativeTo(frame);

        btnEnviar.addActionListener(e -> {
            String texto = campoMensaje.getText().trim();
            if (!texto.isEmpty() && red.Cliente.getInstancia() != null) {
                areaChat.append("Tú: " + texto + "\n");
                red.Cliente.getInstancia().enviarMensaje(texto);
                campoMensaje.setText("");
            }
        });

        campoMensaje.addActionListener(e -> btnEnviar.doClick());

        if (red.Cliente.getInstancia() != null) {
            red.Cliente.getInstancia().setAlRecibirMensaje(mensajeRecibido -> {
                if (areaChat != null) {
                    areaChat.append(mensajeRecibido + "\n");
                    areaChat.setCaretPosition(areaChat.getDocument().getLength());
                }
            });
        }
        ventanaChat.setVisible(true);
        campoMensaje.requestFocusInWindow();
    }
}