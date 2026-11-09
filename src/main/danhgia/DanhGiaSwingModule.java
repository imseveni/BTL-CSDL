package danhgia;

import common.PlaceholderPanel;
import common.SwingModule;
import javax.swing.JComponent;

/**
 * Module trong cửa sổ Swing. Người phụ trách: Đông.
 *
 * <p>VIỆC CẦN LÀM: viết lớp DanhGiaSwingUi (extends JPanel, implements common.Refreshable) trong package này,
 * rồi thay dòng return trong createPanel() bên dưới bằng: return new DanhGiaSwingUi(new DanhGiaService());
 * Xem docs/SWING_HUONG_DAN.md và docs/PHAN_CONG_SWING.md.
 */
public class DanhGiaSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Đánh giá";
    }

    @Override
    public JComponent createPanel() {
        return new PlaceholderPanel("Đánh giá", "Đông", "src/main/danhgia/DanhGiaSwingModule.java");
    }
}
