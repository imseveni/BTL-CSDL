package thanhvien;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Component;
import java.awt.Container;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/** Kiểm thử màn hình Swing bằng Dao giả: không cần MySQL, không mở cửa sổ. */
class ThanhVienSwingUiTest {

    private static class FakeDao extends ThanhVienDao {
        int timLanGoi;
        String tuKhoaCuoi;
        String banCuoi;
        String trangThaiCuoi;

        @Override public List<ThanhVien> tim(String tuKhoa, String maBan, String trangThai) {
            timLanGoi++;
            tuKhoaCuoi = tuKhoa; banCuoi = maBan; trangThaiCuoi = trangThai;
            return List.of(
                    new ThanhVien("TV1", "An", LocalDate.of(2004, 1, 1), "Nam", "an@example.com", "0912345678",
                            LocalDate.of(2025, 1, 1), "Đang hoạt động", "BAN01", "Ban Chủ nhiệm", "Chủ nhiệm"),
                    new ThanhVien("TV2", "Bình", null, null, null, null, LocalDate.of(2025, 2, 1), "Tạm nghỉ",
                            "BAN02", "Ban Chuyên môn", "Thành viên"));
        }
        @Override public List<BanLuaChon> danhSachBan() {
            return List.of(new BanLuaChon("BAN01", "Ban Chủ nhiệm"), new BanLuaChon("BAN02", "Ban Chuyên môn"));
        }
        @Override public List<HoatDong> lichSu(String msv) {
            return List.of(new HoatDong("Khóa học", "KH01", "SQL cơ bản", "Học viên", LocalDate.of(2026, 1, 1)));
        }
        @Override public TongQuanThanhVien tongQuan(String msv) {
            return new TongQuanThanhVien(msv, "Tên", "Đang hoạt động", 1, 2, 3, 4, 5);
        }
    }

    private static Component find(Component root, String name) {
        if (name.equals(root.getName())) return root;
        if (root instanceof Container c)
            for (Component child : c.getComponents()) {
                Component found = find(child, name);
                if (found != null) return found;
            }
        return null;
    }

    private static void waitFor(BooleanSupplier condition) throws Exception {
        for (int i = 0; i < 200; i++) {
            SwingUtilities.invokeAndWait(() -> { });
            if (condition.getAsBoolean()) return;
            Thread.sleep(25);
        }
        fail("Hết thời gian chờ giao diện cập nhật.");
    }

    @Test void napBangThanhVienBanVaLichSuKhiMoLanDau() throws Exception {
        var dao = new FakeDao();
        var ui = new ThanhVienSwingUi(new ThanhVienService(dao));
        JTable table = (JTable) find(ui, "thanhvien.table");
        JTable history = (JTable) find(ui, "thanhvien.history");
        JLabel summary = (JLabel) find(ui, "thanhvien.summary");
        JComboBox<?> ban = (JComboBox<?>) find(ui, "thanhvien.ban");
        SwingUtilities.invokeAndWait(ui::resetAndReload);
        waitFor(() -> table.getRowCount() == 2 && history.getRowCount() == 1 && !ui.isBusy());
        assertEquals("TV1", table.getValueAt(0, 0));
        assertEquals("—", table.getValueAt(1, 2));              // ngày sinh trống hiển thị gạch
        assertEquals("Ban Chủ nhiệm", table.getValueAt(0, 8));
        assertEquals("Khóa học", history.getValueAt(0, 0));
        assertTrue(summary.getText().contains("Khóa học: 1"));
        assertTrue(summary.getText().contains("Phiếu mượn: 5"));
        assertEquals(3, ban.getItemCount());                     // "Tất cả ban" + 2 ban
    }

    @Test void boLocDuocChuyenXuongService() throws Exception {
        var dao = new FakeDao();
        var ui = new ThanhVienSwingUi(new ThanhVienService(dao));
        JComboBox<?> ban = (JComboBox<?>) find(ui, "thanhvien.ban");
        JComboBox<?> state = (JComboBox<?>) find(ui, "thanhvien.trangthai");
        SwingUtilities.invokeAndWait(ui::resetAndReload);
        waitFor(() -> dao.timLanGoi == 1 && !ui.isBusy());
        assertNull(dao.banCuoi);
        assertNull(dao.trangThaiCuoi);
        SwingUtilities.invokeAndWait(() -> {
            ban.setSelectedIndex(2);        // BAN02
            state.setSelectedItem("Tạm nghỉ");
            ((javax.swing.JButton) find(ui, "thanhvien.search")).doClick();
        });
        waitFor(() -> dao.timLanGoi == 2 && !ui.isBusy());
        assertEquals("BAN02", dao.banCuoi);
        assertEquals("Tạm nghỉ", dao.trangThaiCuoi);
    }

    @Test void chonDongKhacThiTaiLaiLichSu() throws Exception {
        var dao = new FakeDao() {
            String lichSuCuoi;
            @Override public List<HoatDong> lichSu(String msv) { lichSuCuoi = msv; return super.lichSu(msv); }
        };
        var ui = new ThanhVienSwingUi(new ThanhVienService(dao));
        JTable table = (JTable) find(ui, "thanhvien.table");
        SwingUtilities.invokeAndWait(ui::resetAndReload);
        waitFor(() -> "TV1".equals(dao.lichSuCuoi) && !ui.isBusy());
        SwingUtilities.invokeAndWait(() -> table.setRowSelectionInterval(1, 1));
        waitFor(() -> "TV2".equals(dao.lichSuCuoi) && !ui.isBusy());
    }
}
