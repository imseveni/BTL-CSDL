package thanhvien;

import static org.junit.jupiter.api.Assertions.*;

import common.ReportSheet;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/** Kiểm thử tab Báo cáo bằng Dao giả: xem báo cáo, thông tin người lập và nơi nhận, xuất file. */
class ThanhVienBaoCaoUiTest {

    private static class FakeDao extends ThanhVienDao {
        String banCuoi = "chưa gọi";
        String trangThaiCuoi = "chưa gọi";
        LocalDate tuCuoi;
        LocalDate denCuoi;

        @Override public List<BanLuaChon> danhSachBan() {
            return List.of(new BanLuaChon("BAN01", "Ban Chủ nhiệm"), new BanLuaChon("BAN02", "Ban Chuyên môn"));
        }
        @Override public List<ThanhVien> baoCaoDanhSach(String maBan, String trangThai) {
            banCuoi = maBan; trangThaiCuoi = trangThai;
            return List.of(new ThanhVien("TV1", "An", LocalDate.of(2004, 1, 1), "Nam", "an@example.com", "0912345678",
                            LocalDate.of(2025, 1, 1), "Đang hoạt động", "BAN01", "Ban Chủ nhiệm", "Chủ nhiệm"),
                    new ThanhVien("TV2", "Bình", null, null, null, null, LocalDate.of(2025, 2, 1), "Tạm nghỉ", "BAN02",
                            "Ban Chuyên môn", "Thành viên"));
        }
        @Override public List<TongHopHoatDong> baoCaoHoatDong(LocalDate tu, LocalDate den) {
            tuCuoi = tu; denCuoi = den;
            return List.of(new TongHopHoatDong("TV1", "An", "Ban Chủ nhiệm", "Chủ nhiệm", "Đang hoạt động", 1, 2, 3, 4, 5, 6, 21));
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

    @Test void xemBaoCaoDanhSachHienBangVaGhiRoNguoiLapNoiNhan() throws Exception {
        var dao = new FakeDao();
        var ui = new ThanhVienBaoCaoUi(new ThanhVienService(dao));
        JTable table = (JTable) find(ui, "thanhvien.baocao.table");
        JLabel info = (JLabel) find(ui, "thanhvien.baocao.thongtin");
        JLabel total = (JLabel) find(ui, "thanhvien.baocao.tonghop");
        JComboBox<?> ban = (JComboBox<?>) find(ui, "thanhvien.baocao.ban");
        SwingUtilities.invokeAndWait(ui::resetAndReload);
        waitFor(() -> ban.getItemCount() == 3 && !ui.isBusy());
        SwingUtilities.invokeAndWait(() -> ((JTextField) find(ui, "thanhvien.baocao.nguoilap")).setText("Nguyễn Thư Ký"));
        SwingUtilities.invokeAndWait(() -> ((JButton) find(ui, "thanhvien.baocao.xem")).doClick());
        waitFor(() -> table.getRowCount() == 2 && !ui.isBusy());
        assertEquals(11, table.getColumnCount());
        assertEquals("STT", table.getColumnName(0));
        assertEquals("TV1", table.getValueAt(0, 1));
        assertNull(dao.banCuoi);                      // "Tất cả ban" nghĩa là không lọc
        assertNull(dao.trangThaiCuoi);
        assertTrue(info.getText().contains("Người lập: Nguyễn Thư Ký"));
        assertTrue(info.getText().contains("Nơi nhận: " + BaoCaoThanhVien.NOI_NHAN_DANH_SACH));
        assertTrue(total.getText().contains("Tổng số thành viên: 2"));
        assertNotNull(ui.currentSheet());
        assertEquals("DANH SÁCH THÀNH VIÊN CÂU LẠC BỘ", ui.currentSheet().title());
    }

    @Test void locTheoBanVaTrangThaiDuocChuyenXuongService() throws Exception {
        var dao = new FakeDao();
        var ui = new ThanhVienBaoCaoUi(new ThanhVienService(dao));
        JComboBox<?> ban = (JComboBox<?>) find(ui, "thanhvien.baocao.ban");
        JComboBox<?> state = (JComboBox<?>) find(ui, "thanhvien.baocao.trangthai");
        SwingUtilities.invokeAndWait(ui::resetAndReload);
        waitFor(() -> ban.getItemCount() == 3 && !ui.isBusy());
        SwingUtilities.invokeAndWait(() -> {
            ban.setSelectedIndex(2);
            state.setSelectedItem("Tạm nghỉ");
            ((JButton) find(ui, "thanhvien.baocao.xem")).doClick();
        });
        waitFor(() -> "BAN02".equals(dao.banCuoi) && !ui.isBusy());
        assertEquals("Tạm nghỉ", dao.trangThaiCuoi);
    }

    @Test void baoCaoHoatDongTheoKyVaXuatFileExcel() throws Exception {
        var dao = new FakeDao();
        var ui = new ThanhVienBaoCaoUi(new ThanhVienService(dao));
        JTable table = (JTable) find(ui, "thanhvien.baocao.table");
        JComboBox<?> type = (JComboBox<?>) find(ui, "thanhvien.baocao.loai");
        SwingUtilities.invokeAndWait(() -> {
            type.setSelectedIndex(1);
            ((JTextField) find(ui, "thanhvien.baocao.tu")).setText("2026-01-01");
            ((JTextField) find(ui, "thanhvien.baocao.den")).setText("2026-06-30");
            ((JButton) find(ui, "thanhvien.baocao.xem")).doClick();
        });
        waitFor(() -> table.getRowCount() == 1 && !ui.isBusy());
        assertEquals(12, table.getColumnCount());
        assertEquals("21", table.getValueAt(0, 11));
        assertEquals(LocalDate.of(2026, 1, 1), dao.tuCuoi);
        assertEquals(LocalDate.of(2026, 6, 30), dao.denCuoi);
        ReportSheet sheet = ui.currentSheet();
        assertEquals("Kỳ báo cáo: từ 01/01/2026 đến 30/06/2026", sheet.subtitle());
        Path file = Files.createTempDirectory("bc").resolve("hoat-dong.xlsx");
        ThanhVienBaoCaoUi.exportTo(sheet, file);
        assertTrue(Files.size(file) > 1000);
    }

    @Test void doiLoaiBaoCaoXoaBaoCaoDangXem() throws Exception {
        var dao = new FakeDao();
        var ui = new ThanhVienBaoCaoUi(new ThanhVienService(dao));
        JTable table = (JTable) find(ui, "thanhvien.baocao.table");
        JComboBox<?> type = (JComboBox<?>) find(ui, "thanhvien.baocao.loai");
        SwingUtilities.invokeAndWait(() -> ((JButton) find(ui, "thanhvien.baocao.xem")).doClick());
        waitFor(() -> table.getRowCount() == 2 && !ui.isBusy());
        SwingUtilities.invokeAndWait(() -> type.setSelectedIndex(1));
        assertNull(ui.currentSheet());
        assertEquals(0, table.getRowCount());
        assertEquals(2, ThanhVienBaoCaoUi.reportNames().size());
    }
}
