package muontra;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Cửa sổ để chạy thử giao diện báo cáo Mượn - Trả (chưa có chức năng).
 * Chạy sau khi biên dịch:  java -cp target/classes muontra.BaoCaoMuonTraFrame
 */
public class BaoCaoMuonTraFrame extends JFrame {

    public BaoCaoMuonTraFrame() {
        super("Báo cáo Mượn - Trả Cơ sở vật chất");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(new BaoCaoMuonTraPanel());
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // dùng giao diện mặc định của Swing
        }
        SwingUtilities.invokeLater(() -> {
            BaoCaoMuonTraFrame frame = new BaoCaoMuonTraFrame();
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
