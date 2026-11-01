package thanhvien;

import static org.junit.jupiter.api.Assertions.*;

import common.DataAccessException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

class ThanhVienMenuTest {

    @Test void menuPhucHoiSauLuaChonSaiVaQuayLaiKhongCanDatabase() {
        String output = capture(() -> new ThanhVienUi().run(new Scanner("abc\n99\n0\n")));
        assertTrue(output.contains("Vui lòng nhập số"));
        assertTrue(output.contains("Lựa chọn không hợp lệ"));
        assertTrue(output.contains("QUẢN LÝ THÀNH VIÊN"));
    }

    @Test void xoaChiThucHienKhiXacNhanY() {
        int[] xoa = {0};
        var dao = new ThanhVienDao() {
            @Override public void xoa(String msv) { xoa[0]++; }
        };
        capture(() -> new ThanhVienUi(new ThanhVienService(dao)).run(new Scanner("6\nTV1\nn\n6\nTV1\ny\n0\n")));
        assertEquals(1, xoa[0]);
    }

    @Test void loiTuDatabaseDuocHienThiVaMenuTiepTuc() {
        var dao = new ThanhVienDao() {
            @Override public void xoa(String msv) {
                throw new DataAccessException("Thành viên đã có lịch sử hoạt động", null);
            }
        };
        String output = capture(() -> new ThanhVienUi(new ThanhVienService(dao)).run(new Scanner("6\nTV1\ny\n0\n")));
        assertTrue(output.contains("Thành viên đã có lịch sử hoạt động"));
    }

    @Test void ngayKhongTonTaiBiChanTruocKhiXuongDao() {
        int[] ghi = {0};
        var dao = new ThanhVienDao() {
            @Override public void them(ThanhVien tv) { ghi[0]++; }
        };
        // MSV, tên, ngày sinh sai (30/02)
        String output = capture(() -> new ThanhVienUi(new ThanhVienService(dao)).run(
                new Scanner("3\nTV1\nNguyễn Văn A\n2026-02-30\n0\n")));
        assertEquals(0, ghi[0]);
        assertTrue(output.contains("Ngày không hợp lệ"));
    }

    @Test void themThanhVienDayDuQuaMenu() {
        ThanhVien[] nhan = {null};
        var dao = new ThanhVienDao() {
            @Override public void them(ThanhVien tv) { nhan[0] = tv; }
        };
        String input = "3\nTV1\nLê Văn B\n2004-05-06\nNam\nb@example.com\n0912345678\n2025-01-01\n\nBAN01\n\n0\n";
        String output = capture(() -> new ThanhVienUi(new ThanhVienService(dao)).run(new Scanner(input)));
        assertTrue(output.contains("Thực hiện thành công"));
        assertEquals("TV1", nhan[0].msv());
        assertEquals(LocalDate.of(2004, 5, 6), nhan[0].ngaySinh());
        assertEquals("Đang hoạt động", nhan[0].trangThaiHD());
        assertEquals("BAN01", nhan[0].maBan());
    }

    @Test void capNhatGiuNguyenGiaTriKhiBoTrongVaXoaKhiNhapGachNgang() {
        ThanhVien[] nhan = {null};
        var dao = new ThanhVienDao() {
            @Override public ThanhVien chiTiet(String msv) {
                return new ThanhVien("TV1", "Tên cũ", LocalDate.of(2004, 1, 1), "Nam", "cu@example.com",
                        "0912345678", LocalDate.of(2025, 1, 1), "Đang hoạt động", "BAN01", "Ban Chủ nhiệm", "Thành viên");
            }
            @Override public void capNhat(ThanhVien tv) { nhan[0] = tv; }
        };
        // họ tên đổi, ngày sinh giữ, giới tính giữ, email xóa (-), SĐT giữ
        String output = capture(() -> new ThanhVienUi(new ThanhVienService(dao)).run(
                new Scanner("4\nTV1\nTên mới\n\n\n-\n\n0\n")));
        assertTrue(output.contains("Thực hiện thành công"));
        assertEquals("Tên mới", nhan[0].hoVaTen());
        assertEquals(LocalDate.of(2004, 1, 1), nhan[0].ngaySinh());
        assertEquals("Nam", nhan[0].gioiTinh());
        assertNull(nhan[0].email());
        assertEquals("0912345678", nhan[0].sdt());
    }

    @Test void lichSuHoatDongDuocIn() {
        var dao = new ThanhVienDao() {
            @Override public List<HoatDong> lichSu(String msv) {
                return List.of(new HoatDong("Khóa học", "KH01", "SQL cơ bản", "Học viên", LocalDate.of(2026, 10, 1)));
            }
        };
        String output = capture(() -> new ThanhVienUi(new ThanhVienService(dao)).run(new Scanner("9\nTV1\n0\n")));
        assertTrue(output.contains("Khóa học | KH01 | SQL cơ bản | Học viên"));
    }

    private static String capture(Runnable action) {
        PrintStream old = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            action.run();
        } finally {
            System.setOut(old);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
