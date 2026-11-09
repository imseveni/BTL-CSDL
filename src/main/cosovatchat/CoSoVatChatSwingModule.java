package cosovatchat;

import common.SwingModule;
import javax.swing.JPanel;

/**
 * Tab "Cơ sở vật chất" (module 09) trong cửa sổ chính. Người phụ trách: Trường.
 * Giao diện nằm ở {@link CoSoVatChatPanel}; panel tự nạp dữ liệu khi tab hiện ra lần đầu.
 */
public class CoSoVatChatSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Cơ sở vật chất";
    }

    @Override
    public JPanel createPanel() {
        return new CoSoVatChatPanel();
    }
}
