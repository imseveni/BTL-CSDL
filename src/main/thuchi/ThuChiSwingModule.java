package thuchi;

import common.PlaceholderPanel;
import common.SwingModule;
import javax.swing.JComponent;

/**
 * Module trong cửa sổ Swing. Người phụ trách: Minh.
 *
 * <p>VIỆC CẦN LÀM: viết lớp ThuChiSwingUi (extends JPanel, implements common.Refreshable) trong package này,
 * rồi thay dòng return trong createPanel() bên dưới bằng: return new ThuChiSwingUi(new ThuChiService());
 * Xem docs/SWING_HUONG_DAN.md và docs/PHAN_CONG_SWING.md.
 */
public class ThuChiSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Thu – Chi";
    }

    @Override
    public JComponent createPanel() {
        return new PlaceholderPanel("Thu – Chi", "Minh", "src/main/thuchi/ThuChiSwingModule.java");
    }
}
