package vista;

import modelo.Producto;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VistaListaDeseados extends JPanel {
    private final BaseFrameCliente frame;
    private final JPanel panelLista;
    private final List<Producto> misDeseosLocales = new ArrayList<>();

    public VistaListaDeseados(BaseFrameCliente frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(EstilosUI.FONDO);

        // encabezado
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(EstilosUI.VERDE);
        header.setBorder(new EmptyBorder(10, 25, 10, 25));

        JButton btnVolver = EstilosUI.iconButton("☰");
        btnVolver.addActionListener(e -> frame.mostrarVista("PRODUCTOS"));
        header.add(btnVolver, BorderLayout.WEST);

        JLabel title = new JLabel("Lista de Deseados", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.add(title, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        panelLista = new JPanel();
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setBackground(EstilosUI.FONDO);
        
        JScrollPane scroll = new JScrollPane(panelLista);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
        
        actualizarVista();
    }

    public void agregarDeseoLocal(Producto p) {
        if (!misDeseosLocales.contains(p)) {
            misDeseosLocales.add(p);
            actualizarVista();
        }
    }

    public void actualizarVista() {
        panelLista.removeAll();

        if (misDeseosLocales.isEmpty()) {
            JLabel lblVacio = new JLabel("Tu lista está vacía. ¡Busca productos sin stock en el catálogo!");
            lblVacio.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblVacio.setBorder(new EmptyBorder(50, 0, 0, 0));
            panelLista.add(lblVacio);
        } else {
            for (Producto p : misDeseosLocales) {
                JPanel item = new JPanel(new BorderLayout());
                item.setMaximumSize(new Dimension(800, 50));
                item.setBorder(BorderFactory.createCompoundBorder(
                    new EmptyBorder(5, 10, 5, 10),
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true)
                ));
                
                JLabel lblNombre = new JLabel(" " + p.getNombre() + " - " + EstilosUI.formatearCLP(p.getPrecio()));
                item.add(lblNombre, BorderLayout.CENTER);
                
                //Anto termina esto mcuhas gracias
                JButton btnMover = new JButton("Mover al Carro");
                btnMover.setEnabled(p.getStock() > 0); 
                item.add(btnMover, BorderLayout.EAST);

                panelLista.add(item);
            }
        }
        
        this.revalidate();
        this.repaint();
    }
}
