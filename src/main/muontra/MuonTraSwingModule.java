package muontra;

import common.SwingModule;
import javax.swing.JPanel;

/**
 * Tab "Mượn – Trả" (module 10) trong cửa sổ chính. Người phụ trách: Trường.
 * Giao diện nằm ở {@link MuonTraPanel} (tab con Phiếu mượn và Báo cáo).
 */
public class MuonTraSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Mượn – Trả";
    }

    @Override
    public JPanel createPanel() {
        return new MuonTraPanel();
    }
}
