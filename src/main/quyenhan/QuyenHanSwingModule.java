package quyenhan;

import common.PlaceholderPanel;
import common.SwingModule;
import javax.swing.JComponent;

/**
 * Module trong cửa sổ Swing. Người phụ trách: Đông.
 *
 * <p>VIỆC CẦN LÀM: viết lớp QuyenHanSwingUi (extends JPanel, implements common.Refreshable) trong package này,
 * rồi thay dòng return trong createPanel() bên dưới bằng: return new QuyenHanSwingUi(new QuyenHanService());
 * Xem docs/SWING_HUONG_DAN.md và docs/PHAN_CONG_SWING.md.
 */
public class QuyenHanSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Quyền hạn";
    }

    @Override
    public JComponent createPanel() {
        return new PlaceholderPanel("Quyền hạn", "Đông", "src/main/quyenhan/QuyenHanSwingModule.java");
    }
}
