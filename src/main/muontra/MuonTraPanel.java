package muontra;

import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 * Nội dung của tab "Mượn – Trả" trong cửa sổ chính. Ban Chủ nhiệm xem, duyệt, ghi nhận trả mọi phiếu;
 * Thành viên chỉ lập và theo dõi phiếu của mình (xem {@link PhieuMuonPanel}).
 * Báo cáo mượn – trả nằm ở tab riêng "Báo cáo Mượn – Trả" (module 11), không còn tab báo cáo mẫu ở đây.
 */
public class MuonTraPanel extends JPanel {

    private final PhieuMuonPanel phieuMuon;

    public MuonTraPanel() {
        this(new PhieuMuonPanel());
    }

    public MuonTraPanel(PhieuMuonPanel phieuMuon) {
        super(new BorderLayout());
        this.phieuMuon = phieuMuon;
        add(phieuMuon, BorderLayout.CENTER);
    }

    public PhieuMuonPanel getPhieuMuon() {
        return phieuMuon;
    }
}
