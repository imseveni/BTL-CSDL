package muontra;

import cosovatchat.CoSoVatChatPanel;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Cửa sổ chạy thử hai tab của Trường (Cơ sở vật chất, Mượn – Trả) khi chưa ghép vào cửa sổ chính của nhóm.
 * Cần MySQL đã dựng bằng run_all.sql và file db.properties.
 * Chạy sau khi biên dịch:  java -cp target/classes muontra.MuonTraDemoFrame
 */
public class MuonTraDemoFrame extends JFrame {

    public MuonTraDemoFrame() {
        super("Cơ sở vật chất & Mượn – Trả (chạy thử)");
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Cơ sở vật chất", new CoSoVatChatPanel());
        tabs.addTab("Mượn – Trả", new MuonTraPanel());
        setContentPane(tabs);
        setSize(1100, 680);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // dùng giao diện mặc định của Swing
        }
        SwingUtilities.invokeLater(() -> {
            MuonTraDemoFrame frame = new MuonTraDemoFrame();
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
