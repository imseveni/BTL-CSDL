package danhgia;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JTable;
import org.junit.jupiter.api.Test;
import swing.SwingUiTestSupport;

class DanhGiaSwingUiTest {
    static class FakeDao extends DanhGiaDao {
        int historyCalls;String lastMember;
        @Override public List<DanhGia> lichSu(String m){historyCalls++;lastMember=m;return List.of(new DanhGia("DG1","Khen thưởng","Giấy khen","Đóng góp",LocalDate.of(2026,1,1),m));}
        @Override public List<ThongKeDanhGia> thongKe(){return List.of(new ThongKeDanhGia("Khen thưởng",2,1));}
    }
    @Test void loadsStatisticsAndHistoryAfterMemberSearch() throws Exception {
        var dao=new FakeDao();var ui=new DanhGiaSwingUi(new DanhGiaService(dao));JTable stats=(JTable)SwingUiTestSupport.find(ui,"danhgia.stats");
        SwingUiTestSupport.waitFor(()->stats.getRowCount()==1&&!ui.isBusy());assertEquals("Khen thưởng",stats.getValueAt(0,0));
        javax.swing.JTextField id=(javax.swing.JTextField)SwingUiTestSupport.find(ui,"danhgia.msv");javax.swing.JTable history=(JTable)SwingUiTestSupport.find(ui,"danhgia.table");
        javax.swing.SwingUtilities.invokeAndWait(()->{id.setText("TV1");((javax.swing.JButton)SwingUiTestSupport.find(ui,"danhgia.search")).doClick();});
        SwingUiTestSupport.waitFor(()->history.getRowCount()==1&&!ui.isBusy());assertEquals("DG1",history.getValueAt(0,0));assertEquals("TV1",dao.lastMember);
    }
    @Test void refreshReloadsSelectedMemberHistory() throws Exception {
        var dao=new FakeDao();var ui=new DanhGiaSwingUi(new DanhGiaService(dao));javax.swing.JTextField id=(javax.swing.JTextField)SwingUiTestSupport.find(ui,"danhgia.msv");
        javax.swing.JTable history=(javax.swing.JTable)SwingUiTestSupport.find(ui,"danhgia.table");javax.swing.JTable stats=(javax.swing.JTable)SwingUiTestSupport.find(ui,"danhgia.stats");
        SwingUiTestSupport.waitFor(()->stats.getRowCount()==1&&!ui.isBusy());javax.swing.SwingUtilities.invokeAndWait(()->{id.setText("TV2");ui.resetAndReload();});
        SwingUiTestSupport.waitFor(()->"TV2".equals(dao.lastMember)&&history.getRowCount()==1&&!ui.isBusy());assertEquals("TV2",history.getValueAt(0,5));
    }
}
