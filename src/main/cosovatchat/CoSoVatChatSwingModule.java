package cosovatchat;

import common.PlaceholderPanel;
import common.SwingModule;
import javax.swing.JComponent;

/**
 * Module trong cửa sổ Swing. Người phụ trách: Trường.
 *
 * <p>VIỆC CẦN LÀM: viết lớp CoSoVatChatSwingUi (extends JPanel, implements common.Refreshable) trong package này,
 * rồi thay dòng return trong createPanel() bên dưới bằng: return new CoSoVatChatSwingUi(new CoSoVatChatService());
 * Xem docs/SWING_HUONG_DAN.md và docs/PHAN_CONG_SWING.md.
 */
public class CoSoVatChatSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Cơ sở vật chất";
    }

    @Override
    public JComponent createPanel() {
        return new PlaceholderPanel("Cơ sở vật chất", "Trường", "src/main/cosovatchat/CoSoVatChatSwingModule.java");
    }
}
