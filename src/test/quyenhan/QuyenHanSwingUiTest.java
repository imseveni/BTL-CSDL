package quyenhan;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import swing.SwingUiTestSupport;

class QuyenHanSwingUiTest {
    static class FakeDao extends QuyenHanDao {
        int lookups;String grantMember,grantPermission,revokeMember,revokePermission;
        @Override public List<QuyenHan> danhSach(){return List.of(new QuyenHan("Q1","Đọc","Mô tả"),new QuyenHan("Q2","Ghi",null));}
        @Override public List<ThanhVienCoQuyen> thanhVienCoQuyen(String q){lookups++;return q.equals("Q1")?List.of(new ThanhVienCoQuyen("TV1","An","B1","Thành viên")):List.of();}
        @Override public void capQuyen(String m,String q){grantMember=m;grantPermission=q;}
        @Override public void thuHoiQuyen(String m,String q){revokeMember=m;revokePermission=q;}
    }
    @Test void loadsPermissionsAndAssignedMembersAndRefreshes() throws Exception {
        var dao=new FakeDao();var ui=new QuyenHanSwingUi(new QuyenHanService(dao));
        JTable permissions=(JTable)SwingUiTestSupport.find(ui,"quyenhan.table");JTable members=(JTable)SwingUiTestSupport.find(ui,"quyenhan.members");
        SwingUiTestSupport.waitFor(()->permissions.getRowCount()==2&&members.getRowCount()==1&&!ui.isBusy());
        assertEquals("Q1",permissions.getValueAt(0,0));assertEquals("TV1",members.getValueAt(0,0));
        SwingUtilities.invokeAndWait(ui::resetAndReload);SwingUiTestSupport.waitFor(()->dao.lookups>=2&&!ui.isBusy());
    }
    @Test void selectingPermissionRefreshesMemberList() throws Exception {
        var dao=new FakeDao();var ui=new QuyenHanSwingUi(new QuyenHanService(dao));JTable p=(JTable)SwingUiTestSupport.find(ui,"quyenhan.table");JTable m=(JTable)SwingUiTestSupport.find(ui,"quyenhan.members");
        SwingUiTestSupport.waitFor(()->m.getRowCount()==1&&!ui.isBusy());SwingUtilities.invokeAndWait(()->p.setRowSelectionInterval(1,1));
        SwingUiTestSupport.waitFor(()->dao.lookups>=2&&!ui.isBusy());assertEquals(0,m.getRowCount());
    }
}
