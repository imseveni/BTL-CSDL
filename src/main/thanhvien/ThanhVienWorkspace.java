package thanhvien;

import common.Refreshable;
import quyenhan.QuyenHanService;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/** Nội dung tab Thành viên: tab "Quản lý thành viên" và tab "Báo cáo". */
public class ThanhVienWorkspace extends JPanel implements Refreshable {

    private final ThanhVienSwingUi members;
    private final ThanhVienBaoCaoUi reports;

    public ThanhVienWorkspace(ThanhVienService service, QuyenHanService quyenService) {
        super(new BorderLayout());
        members = new ThanhVienSwingUi(service, quyenService);
        reports = new ThanhVienBaoCaoUi(service);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setName("thanhvien.tabs");
        tabs.addTab("Quản lý thành viên", members);
        tabs.addTab("Báo cáo", reports);
        add(tabs, BorderLayout.CENTER);
    }

    @Override
    public boolean isBusy() {
        return members.isBusy() || reports.isBusy();
    }

    @Override
    public void resetAndReload() {
        members.resetAndReload();
        reports.resetAndReload();
    }
}
