package danhgia;

import common.SwingModule;
import javax.swing.JComponent;

/**
 * Module trong cửa sổ Swing. Người phụ trách: Đông.
 *
 * Màn hình Swing cho đánh giá thành viên.
 */
public class DanhGiaSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Đánh giá";
    }

    @Override
    public JComponent createPanel() {
        return new DanhGiaSwingUi(new DanhGiaService());
    }
}
