package muontra;

import common.PlaceholderPanel;
import common.SwingModule;
import javax.swing.JComponent;

/**
 * Module trong cửa sổ Swing. Người phụ trách: Trường.
 *
 * <p>VIỆC CẦN LÀM: viết lớp MuonTraSwingUi (extends JPanel, implements common.Refreshable) trong package này,
 * rồi thay dòng return trong createPanel() bên dưới bằng: return new MuonTraSwingUi(new PhieuMuonService());
 * Xem docs/SWING_HUONG_DAN.md và docs/PHAN_CONG_SWING.md.
 */
public class MuonTraSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Mượn – Trả";
    }

    @Override
    public JComponent createPanel() {
        return new PlaceholderPanel("Mượn – Trả", "Trường", "src/main/muontra/MuonTraSwingModule.java");
    }
}
