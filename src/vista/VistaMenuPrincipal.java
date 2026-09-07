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
    private final BaseFrame frame;
    private JLabel bannerText;
    private int indicePromo = 0;

//Banner de promociones 
    private final String[] promociones = {
            "<html><div style='text-align:center;'>Verduras frescas<br><b>TODOS LOS DÍAS</b></div></html>",
            "<html><div style='text-align:center;'>Ofertas en Carnes y Lácteos<br><b>HASTA 30% DCTO</b></div></html>",
            "<html><div style='text-align:center;'>Panadería y Abarrotes<br><b>CALIDAD GARANTIZADA</b></div></html>"
    };

    public VistaMenuPrincipal(BaseFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(EstilosUI.FONDO);

        add(createHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setBackground(EstilosUI.FONDO);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(25, 60, 30, 60));

        // Banner rotativo
        JPanel banner = new JPanel(new BorderLayout());
        banner.setMaximumSize(new Dimension(750, 130));
        banner.setPreferredSize(new Dimension(750, 130));
        banner.setBackground(EstilosUI.VERDE_CLARO);
        banner.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 20, 15, 20)
        ));

        bannerText = new JLabel(promociones[0], SwingConstants.CENTER);
        bannerText.setFont(EstilosUI.FONT_TITLE);
        bannerText.setForeground(new Color(255, 240, 70));
        banner.add(bannerText, BorderLayout.CENTER);

        // Timer Swing para rotar las ofertas cada 3 segundos
        Timer timerBanner = new Timer(3000, e -> {
            indicePromo = (indicePromo + 1) % promociones.length;
            bannerText.setText(promociones[indicePromo]);
        });
        timerBanner.start();

        center.add(banner);
        center.add(Box.createVerticalStrut(25));

        // Generación dinámica de categorías interactivas
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

        JButton enter = EstilosUI.roundedButton("VER CATÁLOGO COMPLETO", EstilosUI.VERDE_CLARO, EstilosUI.VERDE);
        enter.setAlignmentX(Component.CENTER_ALIGNMENT);
        enter.setMaximumSize(new Dimension(250, 45));
        enter.addActionListener(e -> frame.mostrarVista("PRODUCTOS"));

        center.add(enter);
        add(center, BorderLayout.CENTER);
    }

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

        //Placeholder
        String placeholder = "Buscar producto y presionar Enter...";
        JTextField search = new JTextField(placeholder);
        search.setFont(EstilosUI.FONT_NORMAL);
        search.setForeground(Color.GRAY);
        search.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(7, 15, 7, 15)
        ));

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

        //Botones lado derecho del encabezado
        
        JButton carro = EstilosUI.iconButton("🛒");
        carro.addActionListener(e -> frame.mostrarVista("CARRO"));

        JButton login = EstilosUI.iconButton("👤");
        login.addActionListener(e -> frame.mostrarVista("LOGIN"));

        JPanel contenedor = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        contenedor.setOpaque(false);

        contenedor.add(carro);
        contenedor.add(login);

        header.add(contenedor, BorderLayout.EAST);


        /*Tambíen estaba la opción de usar una GridBag */

        return header;
    }

  private JPanel createCategoryCard(Categorias cat) {

    JPanel p = new JPanel();

    p.setOpaque(false);
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));


    JButton circleBtn = new JButton() {
        //PPT 14: pag 21(Programación Avanzada)  
        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create(); 

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int ancho = getWidth();
            int alto = getHeight();

            // Forma circular  
              
            Shape circulo =
                    new java.awt.geom.Ellipse2D.Double(
                            0, 0, ancho, alto
                    );

            // Todo lo que se dibuje después
            // quedará dentro del círculo
            g2.setClip(circulo);

            // Color de fondo
            g2.setColor(
                    EstilosUI.getColorPorCategoria(cat)
            );

            g2.fillOval(
                    0,
                    0,
                    ancho,
                    alto
            );

           

            g2.dispose();
        }
    };

    circleBtn.setPreferredSize(
            new Dimension(85, 85)
    );

    circleBtn.setMaximumSize(
            new Dimension(85, 85)
    );

    circleBtn.setFocusPainted(false);
    circleBtn.setBorderPainted(false);
    circleBtn.setContentAreaFilled(false);

    circleBtn.setCursor(
            Cursor.getPredefinedCursor(
                    Cursor.HAND_CURSOR
            )
    );

    circleBtn.setAlignmentX(
            Component.CENTER_ALIGNMENT
    );

    circleBtn.addActionListener(
            e -> frame.mostrarProductosPorCategoria(cat)
    );

    JLabel label =
            new JLabel(cat.name().replace("_", " "));

    label.setFont(
            new Font(
                    "SansSerif",
                    Font.BOLD,
                    12
            )
    );

    label.setAlignmentX(
            Component.CENTER_ALIGNMENT
    );

    p.add(circleBtn);
    p.add(Box.createVerticalStrut(6));
    p.add(label);

    return p;
}
}