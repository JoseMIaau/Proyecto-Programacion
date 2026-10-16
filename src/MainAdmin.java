import vista.BaseFrameAdmin;
import red.Servidor;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainAdmin {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            
            BaseFrameAdmin frame = new BaseFrameAdmin();
            Servidor servidor = new Servidor(65432, frame);
            servidor.iniciar();
            frame.setServidor(servidor);
            frame.setVisible(true);
        });
    }
}