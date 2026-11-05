package quyenhan;

import common.SwingModule;
import javax.swing.JComponent;

/**
 * Module trong cửa sổ Swing. Người phụ trách: Đông.
 *
 * Màn hình Swing cho quyền hạn.
 */
public class QuyenHanSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Quyền hạn";
    }

    @Override
    public JComponent createPanel() {
        return new QuyenHanSwingUi(new QuyenHanService());
    }
}
