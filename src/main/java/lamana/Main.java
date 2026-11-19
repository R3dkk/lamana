package lamana;

import lamana.view.GameFrame;
import javax.swing.SwingUtilities;

/**
 * Entry point murni aplikasi Animal War.
 * Memisahkan entry point dari UI layout dan logika domain (Separation of Concerns).
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}
