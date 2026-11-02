package baocaomuontra;

import app.SwingApp;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Điểm khởi chạy độc lập cho giao diện Báo cáo mượn – trả cơ sở vật chất.
 */
public final class BaoCaoMuonTraApp {

    private BaoCaoMuonTraApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = createStandaloneFrame(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    public static JFrame createStandaloneFrame(int closeOperation) {
        SwingApp.configureLookAndFeel();
        JFrame frame = new JFrame("Báo cáo Mượn – Trả Cơ sở vật chất - Quản lý Câu lạc bộ");
        frame.setDefaultCloseOperation(closeOperation);

        BaoCaoMuonTraSwingUi panel = new BaoCaoMuonTraSwingUi();
        frame.setLayout(new BorderLayout());
        frame.setContentPane(panel);
        frame.setMinimumSize(new Dimension(1120, 720));
        frame.setSize(1260, 800);
        frame.setLocationRelativeTo(null);

        panel.resetAndReload();
        return frame;
    }
}

