package baocaomuontra;

import common.SwingModule;
import javax.swing.JComponent;

/**
 * Đăng ký tab giao diện đồ họa Swing cho module Báo cáo mượn – trả cơ sở vật chất.
 */
public class BaoCaoMuonTraSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Báo cáo Mượn – Trả";
    }

    @Override
    public JComponent createPanel() {
        return new BaoCaoMuonTraSwingUi();
    }
}

