package vista;

import modelo.Categorias;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EstilosUI {
    public static final Color VERDE = new Color(46, 103, 57);
    public static final Color VERDE_CLARO = new Color(166, 211, 160);
    public static final Color FONDO = Color.WHITE;

    public static final Font FONT_NORMAL = new Font("SansSerif", Font.PLAIN, 15);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 26);

    public static JButton roundedButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(7, 18, 7, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JButton iconButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 24));
        b.setForeground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static Color getColorPorCategoria(Categorias cat) {
        if (cat == null) return new Color(220, 220, 220);
        switch (cat) {
            case FRUTAS_Y_VERDURAS: return new Color(115, 181, 77);
            case LACTEOS: return new Color(200, 220, 245);
            case PANADERIA: return new Color(210, 160, 110);
            case CARNICERIA: return new Color(235, 130, 130);
            case BEBIDAS: return new Color(110, 190, 240);
            case LIMPIEZA: return new Color(130, 215, 215);
            default: return new Color(245, 200, 70);
        }
    }

    public static String formatearCLP(double monto) {
        long valor = (long) monto;
        if (valor == 0) {
            return "$0";
        }

        String res = "";
        int cont = 0;
        long aux = valor;

        while (aux > 0) {
            if (cont == 3) {
                res = "." + res;
                cont = 0;
            }
            res = (aux % 10) + res;
            aux /= 10;
            cont++;
        }

        return "$" + res;
    }
}