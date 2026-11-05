package banchuyenmon;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import swing.SwingUiTestSupport;

class BanChuyenMonSwingUiTest {
    static class FakeDao extends BanChuyenMonDao {
        int selectedCalls; String movedMember, movedBan, changedMember, changedRole;
        @Override public List<BanThongKe> danhSach(){return List.of(new BanThongKe("B1","Ban Một",1),new BanThongKe("B2","Ban Hai",0));}
        @Override public List<ThanhVienTrongBan> thanhVienTheoBan(String id){selectedCalls++;return id.equals("B1")?List.of(new ThanhVienTrongBan("TV1","An","Trưởng ban","Đang hoạt động")):List.of();}
        @Override public void chuyenBan(String m,String b){movedMember=m;movedBan=b;}
        @Override public void doiChucVu(String m,String r){changedMember=m;changedRole=r;}
    }
    @Test void loadsDepartmentAndMemberTablesAndRefreshes() throws Exception {
        var dao=new FakeDao();var ui=new BanChuyenMonSwingUi(new BanChuyenMonService(dao));
        JTable bans=(JTable)SwingUiTestSupport.find(ui,"banchuyenmon.table");JTable members=(JTable)SwingUiTestSupport.find(ui,"banchuyenmon.members");
        SwingUiTestSupport.waitFor(()->bans.getRowCount()==2&&members.getRowCount()==1&&!ui.isBusy());
        assertEquals("Ban Một",bans.getValueAt(0,1));assertEquals("TV1",members.getValueAt(0,0));
        SwingUtilities.invokeAndWait(ui::resetAndReload);SwingUiTestSupport.waitFor(()->dao.selectedCalls>=2&&!ui.isBusy());
    }
    @Test void selectingDepartmentLoadsItsMembers() throws Exception {
        var dao=new FakeDao();var ui=new BanChuyenMonSwingUi(new BanChuyenMonService(dao));
        JTable bans=(JTable)SwingUiTestSupport.find(ui,"banchuyenmon.table");JTable members=(JTable)SwingUiTestSupport.find(ui,"banchuyenmon.members");
        SwingUiTestSupport.waitFor(()->members.getRowCount()==1&&!ui.isBusy());
        SwingUtilities.invokeAndWait(()->bans.setRowSelectionInterval(1,1));
        SwingUiTestSupport.waitFor(()->dao.selectedCalls>=2&&!ui.isBusy());assertEquals(0,members.getRowCount());
    }
}
