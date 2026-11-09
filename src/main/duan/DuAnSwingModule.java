package duan;

import common.SwingModule;
import javax.swing.JComponent;

/** Module 05 trong cửa sổ Swing. Người phụ trách: Chiến. */
public class DuAnSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Dự án";
    }

    @Override
    public JComponent createPanel() {
        return DuAnModule.createSwingUi();
    }
}
