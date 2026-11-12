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

    @Test void xemBaoCaoDanhSachHienTrangInVaGhiRoNguoiLapNoiNhan() throws Exception {
        var dao = new FakeDao();
        var ui = new ThanhVienBaoCaoUi(new ThanhVienService(dao));
        JComboBox<?> ban = (JComboBox<?>) find(ui, "thanhvien.baocao.ban");
        SwingUtilities.invokeAndWait(ui::resetAndReload);
        waitFor(() -> ban.getItemCount() == 3 && !ui.isBusy());
        SwingUtilities.invokeAndWait(() -> ((JTextField) find(ui, "thanhvien.baocao.nguoilap")).setText("Nguyễn Thư Ký"));
        SwingUtilities.invokeAndWait(() -> ((JButton) find(ui, "thanhvien.baocao.xem")).doClick());
        waitFor(() -> ui.currentSheet() != null && !ui.isBusy());
        ReportSheet sheet = ui.currentSheet();
        assertEquals(2, sheet.rows().size());
        assertEquals(11, sheet.columns().size());
        assertEquals("Mã sinh viên", sheet.columns().get(1));
        assertEquals("TV1", sheet.rows().get(0).get(1));
        assertNull(dao.banCuoi);                      // "Tất cả ban" nghĩa là không lọc
        assertNull(dao.trangThaiCuoi);
        assertEquals("Nguyễn Thư Ký", sheet.info().get(1).value().split(" – ")[0]);
        assertTrue(sheet.info().get(0).value().contains("Đoàn trường"));
        assertTrue(sheet.summaryLines().get(0).contains("Tổng số thành viên: 2"));
        assertEquals("DANH SÁCH THÀNH VIÊN CÂU LẠC BỘ", sheet.title());
        assertNull(find(ui, "thanhvien.baocao.clb"), "tên CLB cố định, không nhập tay");
        assertNotNull(find(ui, "thanhvien.baocao.trang"));
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
            ((JTextField) find(ui, "thanhvien.baocao.nguoilap")).setText("Nguyễn Thư Ký");
            ((JButton) find(ui, "thanhvien.baocao.xem")).doClick();
        });
        waitFor(() -> "BAN02".equals(dao.banCuoi) && !ui.isBusy());
        assertEquals("Tạm nghỉ", dao.trangThaiCuoi);
    }

    @Test void baoCaoHoatDongTheoKyVaXuatFilePdf() throws Exception {
        var dao = new FakeDao();
        var ui = new ThanhVienBaoCaoUi(new ThanhVienService(dao));
        JComboBox<?> type = (JComboBox<?>) find(ui, "thanhvien.baocao.loai");
        SwingUtilities.invokeAndWait(() -> {
            type.setSelectedIndex(1);
            ((JTextField) find(ui, "thanhvien.baocao.tu")).setText("2026-01-01");
            ((JTextField) find(ui, "thanhvien.baocao.den")).setText("2026-06-30");
            ((JTextField) find(ui, "thanhvien.baocao.nguoilap")).setText("Nguyễn Thư Ký");
            ((JButton) find(ui, "thanhvien.baocao.xem")).doClick();
        });
        waitFor(() -> ui.currentSheet() != null && !ui.isBusy());
        ReportSheet sheet = ui.currentSheet();
        assertEquals(1, sheet.rows().size());
        assertEquals("21", sheet.rows().get(0).get(11));
        assertEquals(LocalDate.of(2026, 1, 1), dao.tuCuoi);
        assertEquals(LocalDate.of(2026, 6, 30), dao.denCuoi);
        assertEquals("Kỳ báo cáo: từ 01/01/2026 đến 30/06/2026", sheet.subtitle());
        Path file = Files.createTempDirectory("bc").resolve("hoat-dong");
        Path saved = ThanhVienBaoCaoUi.exportTo(sheet, file);
        assertTrue(saved.getFileName().toString().endsWith(".pdf"));
        assertTrue(Files.size(saved) > 20_000);
        assertTrue(new String(Files.readAllBytes(saved), java.nio.charset.StandardCharsets.ISO_8859_1).startsWith("%PDF-1.4"));
    }

    @Test void doiLoaiBaoCaoXoaBaoCaoDangXem() throws Exception {
        var dao = new FakeDao();
        var ui = new ThanhVienBaoCaoUi(new ThanhVienService(dao));
        JComboBox<?> type = (JComboBox<?>) find(ui, "thanhvien.baocao.loai");
        SwingUtilities.invokeAndWait(() -> {
            ((JTextField) find(ui, "thanhvien.baocao.nguoilap")).setText("Nguyễn Thư Ký");
            ((JButton) find(ui, "thanhvien.baocao.xem")).doClick();
        });
        waitFor(() -> ui.currentSheet() != null && !ui.isBusy());
        assertEquals(2, ui.currentSheet().rows().size());
        SwingUtilities.invokeAndWait(() -> type.setSelectedIndex(1));
        assertNull(ui.currentSheet());
        assertEquals(2, ThanhVienBaoCaoUi.reportNames().size());
    }
}
