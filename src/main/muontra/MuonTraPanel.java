package muontra;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Nội dung của tab "Mượn – Trả" trong cửa sổ chính: hai tab con "Phiếu mượn" và "Báo cáo".
 * Trong MuonTraSwingModule chỉ cần dùng: new MuonTraPanel().
 */
public class MuonTraPanel extends JPanel {

    private final PhieuMuonPanel phieuMuon;
    private final BaoCaoMuonTraPanel baoCao;

    public MuonTraPanel() {
        this(new PhieuMuonPanel(), new BaoCaoMuonTraPanel());
    }

    public MuonTraPanel(PhieuMuonPanel phieuMuon, BaoCaoMuonTraPanel baoCao) {
        super(new BorderLayout());
        this.phieuMuon = phieuMuon;
        this.baoCao = baoCao;
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Phiếu mượn", phieuMuon);
        tabs.addTab("Báo cáo", baoCao);
        add(tabs, BorderLayout.CENTER);
    }

    public PhieuMuonPanel getPhieuMuon() {
        return phieuMuon;
    }

    /** Tab Báo cáo: hiện chỉ có giao diện, người làm chức năng báo cáo nối dữ liệu qua panel này. */
    public BaoCaoMuonTraPanel getBaoCao() {
        return baoCao;
    }
}
