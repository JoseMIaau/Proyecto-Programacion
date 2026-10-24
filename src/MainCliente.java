import vista.BaseFrameCliente;
import red.Cliente;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainCliente {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // Pregunta la ip al iniciar el programa
            String ipServidor = JOptionPane.showInputDialog(
                    null,
                    "Ingrese la IP del Servidor Administrador (Ej: 192.168.1.100):",
                    "Conexión de Red",
                    JOptionPane.QUESTION_MESSAGE
            );

            // Si el usuario cancela o deja vacío, cerramos o usamos localhost
            if (ipServidor == null || ipServidor.trim().isEmpty()) {
                ipServidor = "localhost";
            }

            BaseFrameCliente frame = new BaseFrameCliente();
            frame.setVisible(true);

            // Usar la ip ingresada
            Cliente cliente = new Cliente(ipServidor.trim(), 65432);
            cliente.setOnStockUpdateCallback(() -> {
                SwingUtilities.invokeLater(() -> frame.getVistaProductos().refrescarVistaActual());
            });
            cliente.conectar();
        });
    }
}