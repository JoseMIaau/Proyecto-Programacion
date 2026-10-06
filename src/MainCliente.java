import vista.BaseFrameCliente;
import red.Cliente;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainCliente {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            BaseFrameCliente frame = new BaseFrameCliente();
            frame.setVisible(true);

            //MODIFICAR AQUI PARA CONECTARSE CON LA IP
            Cliente cliente = new Cliente("192.168.1.109", 65432);
            cliente.setOnStockUpdateCallback(() -> {
                SwingUtilities.invokeLater(() -> frame.getVistaProductos().refrescarVistaActual());
            });
            cliente.conectar();
        });
    }
}