package banchuyenmon;

import common.PlaceholderPanel;
import common.SwingModule;
import javax.swing.JComponent;

/**
 * Module trong cửa sổ Swing. Người phụ trách: Đông.
 *
 * <p>VIỆC CẦN LÀM: viết lớp BanChuyenMonSwingUi (extends JPanel, implements common.Refreshable) trong package này,
 * rồi thay dòng return trong createPanel() bên dưới bằng: return new BanChuyenMonSwingUi(new BanChuyenMonService());
 * Xem docs/SWING_HUONG_DAN.md và docs/PHAN_CONG_SWING.md.
 */
public class BanChuyenMonSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Ban chuyên môn & Chức vụ";
    }

    @Override
    public JComponent createPanel() {
        return new PlaceholderPanel("Ban chuyên môn & Chức vụ", "Đông", "src/main/banchuyenmon/BanChuyenMonSwingModule.java");
    }
}
