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
    private int indicePromo = 0;

    public VistaMenuPrincipal(BaseFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(EstilosUI.FONDO);

        add(createHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setBackground(EstilosUI.FONDO);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(25, 60, 30, 60));


// BANNER ROTATIVO
JPanel banner = new JPanel(new BorderLayout());

banner.setMaximumSize(new Dimension(800, 288));
banner.setPreferredSize(new Dimension(800, 288));

banner.setOpaque(false);
banner.setBorder(null);


//Imagenes para el banner, pre cortadas y con el texto integrado
String[] imagenesBanner = {
        "/imagenes/Banners/Banner_verduras.png",
        "/imagenes/Banners/Banner_carnes_y_lacteos.png",
        "/imagenes/Banners/Banner_panaderia.png"
};

// Label que muestra las imagenes
JLabel bannerImagen = new JLabel();
bannerImagen.setHorizontalAlignment(SwingConstants.CENTER);

// Primera imagen
ImageIcon iconoOriginal = new ImageIcon(
        getClass().getResource(imagenesBanner[0])
);

Image imagenEscalada = iconoOriginal
        .getImage()
        .getScaledInstance(
                800,
                288,
                Image.SCALE_SMOOTH
        );

bannerImagen.setIcon(
        new ImageIcon(imagenEscalada)
);

banner.add(
        bannerImagen,
        BorderLayout.CENTER
);

// Timer con delay de 3 segundos
Timer timerBanner = new Timer(3000, e -> {

    indicePromo =
            (indicePromo + 1)
            % imagenesBanner.length;

    ImageIcon nuevoIcono = new ImageIcon(
            getClass().getResource(
                    imagenesBanner[indicePromo]
            )
    );

    Image nuevaImagen = nuevoIcono
            .getImage()
            .getScaledInstance(
                    800,
                    288,
                    Image.SCALE_SMOOTH
            );

    bannerImagen.setIcon(
            new ImageIcon(nuevaImagen)
    );
});

timerBanner.start();

// Agregar banner en el centro
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
        JTextField search = new JTextField("Buscar producto y presionar Enter...");
        search.setFont(EstilosUI.FONT_NORMAL);
        search.setForeground(Color.GRAY);
        search.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(7, 15, 7, 15)
        ));

        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    frame.buscarProductosPorTexto(search.getText().trim());
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

    // Ruta de la imagen
    String ruta = "/imagenes/categorias/"
            + cat.name().toLowerCase()
            + ".png";

        ImageIcon iconoOriginal = new ImageIcon(
        getClass().getResource(ruta)
);
        Image imagenOriginal = iconoOriginal.getImage();

        int anchoOriginal = iconoOriginal.getIconWidth();
        int altoOriginal = iconoOriginal.getIconHeight();

        int maximo = 210;

        double escala = Math.min(
                (double) maximo / anchoOriginal,
                (double) maximo / altoOriginal
        );

        int nuevoAncho = (int) (anchoOriginal * escala);
        int nuevoAlto = (int) (altoOriginal * escala);

        Image imagenEscalada = imagenOriginal.getScaledInstance(
                nuevoAncho,
                nuevoAlto,
                Image.SCALE_SMOOTH
        );

        ImageIcon icono = new ImageIcon(imagenEscalada);

    // Botón normal
    JButton boton = new JButton(icono);

    boton.setPreferredSize(new Dimension(120, 120));
    boton.setPreferredSize(new Dimension(120, 120));
    boton.setMaximumSize(new Dimension(120, 120));

    // Quitar apariencia visual del botón
    boton.setBorderPainted(false);
    boton.setContentAreaFilled(false);
    boton.setFocusPainted(false);
    boton.setOpaque(false);
    boton.setMargin(new Insets(0, 0, 0, 0));

    boton.setCursor(
            Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    );

    boton.setAlignmentX(Component.CENTER_ALIGNMENT);

    // Acción
    boton.addActionListener(
            e -> frame.mostrarProductosPorCategoria(cat)
    );

    // Nombre de la categoría
    JLabel label =
            new JLabel(cat.name().replace("_", " "));

    label.setFont(
            new Font("SansSerif", Font.BOLD, 12)
    );

    label.setAlignmentX(Component.CENTER_ALIGNMENT);

    // Agregar elementos
    p.add(boton);
    p.add(Box.createVerticalStrut(6));
    p.add(label);

    return p;
}
}