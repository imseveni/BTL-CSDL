package khoahoc;

import common.SwingModule;
import javax.swing.JComponent;

/** Module 04 trong cửa sổ Swing. Người phụ trách: Chiến. */
public class KhoaHocSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Khóa học";
    }

    @Override
    public JComponent createPanel() {
        return KhoaHocModule.createSwingUi();
    }
}
