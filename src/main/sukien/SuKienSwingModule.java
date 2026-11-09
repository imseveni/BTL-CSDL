package sukien;

import common.PlaceholderPanel;
import common.SwingModule;
import javax.swing.JComponent;

/**
 * Module trong cửa sổ Swing. Người phụ trách: Minh.
 *
 * <p>VIỆC CẦN LÀM: viết lớp SuKienSwingUi (extends JPanel, implements common.Refreshable) trong package này,
 * rồi thay dòng return trong createPanel() bên dưới bằng: return new SuKienSwingUi(new SuKienService());
 * Xem docs/SWING_HUONG_DAN.md và docs/PHAN_CONG_SWING.md.
 */
public class SuKienSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Sự kiện";
    }

    @Override
    public JComponent createPanel() {
        return new PlaceholderPanel("Sự kiện", "Minh", "src/main/sukien/SuKienSwingModule.java");
    }
}
