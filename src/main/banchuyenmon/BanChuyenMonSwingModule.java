package banchuyenmon;

import common.SwingModule;
import javax.swing.JComponent;

/**
 * Module trong cửa sổ Swing. Người phụ trách: Đông.
 *
 * Màn hình Swing cho Ban chuyên môn và Chức vụ.
 */
public class BanChuyenMonSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Ban chuyên môn & Chức vụ";
    }

    @Override
    public JComponent createPanel() {
        return new BanChuyenMonSwingUi(new BanChuyenMonService());
    }
}
