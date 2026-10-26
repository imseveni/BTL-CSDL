package banchuyenmon;

import static org.junit.jupiter.api.Assertions.*;
import common.DataAccessException;
import config.DBConnection;
import danhgia.*;
import quyenhan.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Chỉ bật trên DB thử riêng đã chạy run_all.sql, bằng -Dclb.mysql.tests=true. */
@EnabledIfSystemProperty(named = "clb.mysql.tests", matches = "true")
class NhanSuDatabaseTest {
    private final String prefix = "ITD" + UUID.randomUUID().toString().substring(0, 8);
    private String b1() { return prefix + "B1"; }
    private String b2() { return prefix + "B2"; }
    private String tv1() { return prefix + "TV1"; }
    private String tv2() { return prefix + "TV2"; }
    private String q1() { return prefix + "Q1"; }
    private String d1() { return prefix + "D1"; }
    private String d2() { return prefix + "D2"; }

    @BeforeEach void fixture() throws SQLException {
        var service = new BanChuyenMonService();
        service.them(new BanChuyenMon(b1(), "Ban JDBC 1"));
        service.them(new BanChuyenMon(b2(), "Ban JDBC 2"));
        try (Connection c = DBConnection.getConnection(); var ps = c.prepareStatement(
                "INSERT INTO ThanhVien(MSV, HoVaTen, NgayGiaNhap, MaBan) VALUES (?, ?, '2026-01-01', ?)")) {
            for (String msv : new String[]{tv1(), tv2()}) {
                ps.setString(1, msv); ps.setString(2, "Thành viên JDBC"); ps.setString(3, b1()); ps.executeUpdate();
            }
        }
    }

    @AfterEach void cleanup() throws SQLException {
        // Xóa đúng mã fixture, không reset DB hoặc chạm dữ liệu nền.
        try (Connection c = DBConnection.getConnection()) {
            execute(c, "DELETE FROM DanhGia WHERE MaGhiNhan IN (?, ?)", d1(), d2());
            execute(c, "DELETE FROM QuyenHan WHERE MaQuyenHan = ?", q1());
            execute(c, "DELETE FROM ThanhVien WHERE MSV IN (?, ?)", tv1(), tv2());
            execute(c, "DELETE FROM BanChuyenMon WHERE MaBan IN (?, ?)", b1(), b2());
        }
    }

    @Test void departmentsSupportCrudAssignmentAndGuardedDeletion() {
        var service = new BanChuyenMonService();
        assertThrows(DataAccessException.class, () -> service.xoa(b1()));
        assertThrows(DataAccessException.class, () -> service.them(new BanChuyenMon(b1(), "Trùng")));
        assertEquals(2, service.thanhVienTheoBan(b1()).size());
        service.chuyenBan(tv1(), b2());
        service.doiChucVu(tv1(), "Trưởng ban");
        assertEquals("Trưởng ban", service.thanhVienTheoBan(b2()).get(0).chucVu());
        service.chuyenBan(tv2(), b2());
        service.sua(new BanChuyenMon(b2(), "Ban đã đổi tên"));
        service.sua(new BanChuyenMon(b2(), "Ban đã đổi tên"));
        var row = service.danhSach().stream().filter(b -> b.maBan().equals(b2())).findFirst().orElseThrow();
        assertEquals(2, row.soThanhVien());
        assertEquals("Ban đã đổi tên", row.tenBan());
        assertThrows(DataAccessException.class, () -> service.chuyenBan(tv1(), prefix + "NO"));
        assertThrows(DataAccessException.class, () -> service.doiChucVu(prefix + "NO", "Thành viên"));
        assertThrows(DataAccessException.class, () -> service.thanhVienTheoBan(prefix + "NO"));
        service.xoa(b1());
        assertThrows(DataAccessException.class, () -> service.xoa(b1()));
    }

    @Test void permissionsSupportGrantRevokeLookupsAndCascade() {
        var service = new QuyenHanService();
        service.them(new QuyenHan(q1(), "Quyền JDBC", null));
        service.sua(new QuyenHan(q1(), "Quyền đã sửa", "Mô tả tiếng Việt"));
        service.sua(new QuyenHan(q1(), "Quyền đã sửa", "Mô tả tiếng Việt"));
        service.capQuyen(tv1(), q1());
        service.capQuyen(tv2(), q1());
        assertThrows(DataAccessException.class, () -> service.capQuyen(tv1(), q1()));
        assertEquals(2, service.thanhVienCoQuyen(q1()).size());
        assertEquals("Mô tả tiếng Việt", service.quyenCuaThanhVien(tv1()).get(0).moTaChiTiet());
        assertTrue(service.danhSach().stream().anyMatch(q -> q.maQuyenHan().equals(q1())));
        service.thuHoiQuyen(tv1(), q1());
        assertTrue(service.quyenCuaThanhVien(tv1()).isEmpty());
        assertThrows(DataAccessException.class, () -> service.thuHoiQuyen(tv1(), q1()));
        service.xoa(q1());
        assertTrue(service.quyenCuaThanhVien(tv2()).isEmpty());
        assertThrows(DataAccessException.class, () -> service.thanhVienCoQuyen(q1()));
        assertThrows(DataAccessException.class, () -> service.capQuyen(prefix + "NO", q1()));
    }

    @Test void evaluationsSupportUpdateChronologicalHistoryAndDistinctMemberCounts() {
        var service = new DanhGiaService();
        LocalDate date = LocalDate.of(2026, 10, 1);
        service.them(new DanhGia(d1(), prefix, "Biểu dương", "Đóng góp", date, tv1()));
        service.them(new DanhGia(d2(), prefix, "Giấy khen", "Hỗ trợ", date.plusDays(1), tv1()));
        assertEquals(d2(), service.lichSu(tv1()).get(0).maGhiNhan());
        var summary = service.thongKe().stream().filter(t -> t.loaiDanhGia().equals(prefix)).findFirst().orElseThrow();
        assertEquals(2, summary.soDanhGia());
        assertEquals(1, summary.soThanhVien());
        service.sua(new DanhGia(d1(), prefix, "Thư cảm ơn", "Hỗ trợ sự kiện", date.plusDays(2), tv2()));
        assertEquals("Hỗ trợ sự kiện", service.lichSu(tv2()).get(0).lyDo());
        summary = service.thongKe().stream().filter(t -> t.loaiDanhGia().equals(prefix)).findFirst().orElseThrow();
        assertEquals(2, summary.soThanhVien());
        assertThrows(DataAccessException.class, () -> service.them(new DanhGia(d1(), prefix, "A", "B", date, tv1())));
        assertThrows(DataAccessException.class, () -> service.them(new DanhGia(prefix + "NO", prefix, "A", "B", date, prefix + "NO")));
        assertThrows(DataAccessException.class, () -> service.lichSu(prefix + "NO"));
    }

    private void execute(Connection c, String sql, String... args) throws SQLException {
        try (var ps = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setString(i + 1, args[i]);
            ps.executeUpdate();
        }
    }
}
