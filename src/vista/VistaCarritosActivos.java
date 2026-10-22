package vista;

import red.Servidor;
import red.Servidor.ManejadorCliente;
import modelo.Inventario;
import modelo.Producto;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class VistaCarritosActivos extends JPanel {

    private final Servidor servidor;
    private final JPanel panelCarritos;
    private final Timer timer;
    private JButton btnAtras;

    public VistaCarritosActivos(Servidor servidor, BaseFrameAdmin frame) {
        this.servidor = servidor;

        setLayout(new BorderLayout(10, 10));
        JLabel titulo = new JLabel("Carritos Activos:");
        titulo.setFont(new Font("Arial", Font.BOLD, 22));

        add(titulo, BorderLayout.NORTH);

        panelCarritos = new JPanel();
        panelCarritos.setLayout(
            new BoxLayout(panelCarritos, BoxLayout.Y_AXIS)
        );
        add(new JScrollPane(panelCarritos), BorderLayout.CENTER);

        // Botón para volver
        JPanel pnlSur = new JPanel(new FlowLayout());
        btnAtras = new JButton("Volver");
        pnlSur.add(btnAtras);
        btnAtras.addActionListener(e -> frame.mostrarVista("ADMIN"));
        add(pnlSur, BorderLayout.SOUTH);

        timer = new Timer(1000, e -> actualizarCarritos());
        timer.start();
        actualizarCarritos();
    }

    private void actualizarCarritos() {

        panelCarritos.removeAll();

        for (ManejadorCliente cliente : servidor.getClientesActivos()) {
            Map<Integer, Integer> carrito = cliente.getMiCarrito();
            if (carrito.isEmpty()) {
                continue;
            }
            JLabel nombreCliente = new JLabel(
                cliente.getIdCliente() + ":"
            );
            nombreCliente.setFont(
                new Font("Arial", Font.BOLD, 16)
            );
            panelCarritos.add(nombreCliente);

            for (Map.Entry<Integer, Integer> item : carrito.entrySet()) {
                int idProducto = item.getKey();
                int cantidad = item.getValue();
                Producto producto = Inventario.getInstancia()
                        .buscarProducto(idProducto);

                if (producto != null) {
                    JLabel label = new JLabel(
                        producto.getNombre()
                        + "    |    "
                        + cantidad + " unidades"
                    );
                    panelCarritos.add(label);
                }
            }
            panelCarritos.add(Box.createVerticalStrut(15));
            panelCarritos.add(new JSeparator());
        }
        panelCarritos.revalidate();
        panelCarritos.repaint();
    }
}