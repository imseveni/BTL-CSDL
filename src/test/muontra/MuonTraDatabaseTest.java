package muontra;

import static org.junit.jupiter.api.Assertions.*;
import common.DataAccessException;
import config.DBConnection;
import cosovatchat.CoSoVatChat;
import cosovatchat.CoSoVatChatService;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Chỉ bật trên DB thử riêng đã chạy run_all.sql, bằng -Dclb.mysql.tests=true. */
@EnabledIfSystemProperty(named = "clb.mysql.tests", matches = "true")
class MuonTraDatabaseTest {
    private final String prefix = "ITT" + UUID.randomUUID().toString().substring(0, 8);
    private String b1() { return prefix + "B1"; }
    private String tv1() { return prefix + "TV1"; }
    private String tv2() { return prefix + "TV2"; }
    private String d1() { return prefix + "D1"; }
    private String d2() { return prefix + "D2"; }
    private String p1() { return prefix + "P1"; }
    private String p2() { return prefix + "P2"; }
    private String p3() { return prefix + "P3"; }

    private final CoSoVatChatService thietBi = new CoSoVatChatService();
    private final PhieuMuonService phieu = new PhieuMuonService();
    private final LocalDate muon = LocalDate.of(2026, 1, 10);
    private final LocalDate hen = LocalDate.of(2026, 1, 17);

    @BeforeEach void fixture() throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            execute(c, "INSERT INTO BanChuyenMon(MaBan, TenBan) VALUES (?, ?)", b1(), "Ban JDBC");
            for (String msv : new String[]{tv1(), tv2()}) {
                execute(c, "INSERT INTO ThanhVien(MSV, HoVaTen, NgayGiaNhap, MaBan) VALUES (?, ?, '2026-01-01', ?)",
                        msv, "Thành viên JDBC", b1());
            }
        }
    }

    @AfterEach void cleanup() throws SQLException {
        // Trả hết thiết bị còn mượn (trigger chặn xóa dòng đang mượn), rồi xóa đúng mã fixture.
        for (String code : new String[]{p1(), p2(), p3()}) {
            try { phieu.traTatCa(code, LocalDate.of(2026, 12, 31)); } catch (RuntimeException ignored) { }
        }
        try (Connection c = DBConnection.getConnection()) {
            execute(c, "DELETE FROM ChiTietMuon WHERE MaPhieuMuon LIKE ?", prefix + "%");
            execute(c, "DELETE FROM PhieuMuon WHERE MaPhieuMuon LIKE ?", prefix + "%");
            execute(c, "DELETE FROM CoSoVatChat WHERE MaThietBi LIKE ?", prefix + "%");
            execute(c, "DELETE FROM ThanhVien WHERE MSV LIKE ?", prefix + "%");
            execute(c, "DELETE FROM BanChuyenMon WHERE MaBan = ?", b1());
        }
    }

    @Test void devicesSupportCrudManagerAssignmentAndAvailabilityCheck() {
        thietBi.themMoi(d1(), " Máy chiếu JDBC ", 4, null, null);
        assertThrows(DataAccessException.class, () -> thietBi.themMoi(d1(), "Trùng", 1, null, null));
        assertThrows(DataAccessException.class, () -> thietBi.themMoi(d2(), "Sai người quản lý", 1, null, prefix + "NO"));
        CoSoVatChat tb = thietBi.xemChiTiet(d1());
        assertEquals("Máy chiếu JDBC", tb.tenThietBi());
        assertEquals(4, tb.soLuongKhaDung());
        assertNull(tb.msvQuanLy());

        thietBi.ganNguoiQuanLy(d1(), tv1());
        assertEquals("Thành viên JDBC", thietBi.xemChiTiet(d1()).tenNguoiQuanLy());
        thietBi.ganNguoiQuanLy(d1(), tv2());
        assertEquals(tv2(), thietBi.xemChiTiet(d1()).msvQuanLy());
        thietBi.ganNguoiQuanLy(d1(), "");
        assertNull(thietBi.xemChiTiet(d1()).msvQuanLy());
        assertThrows(DataAccessException.class, () -> thietBi.ganNguoiQuanLy(d1(), prefix + "NO"));

        thietBi.capNhat(d1(), "Máy chiếu đã sửa", 6);
        assertEquals(6, thietBi.xemChiTiet(d1()).soLuongKhaDung());
        assertTrue(thietBi.kiemTraKhaDung(d1(), 6));
        assertFalse(thietBi.kiemTraKhaDung(d1(), 7));
        assertTrue(thietBi.timKiem("đã sửa").stream().anyMatch(t -> t.maThietBi().equals(d1())));
        assertThrows(IllegalArgumentException.class, () -> thietBi.xemChiTiet(prefix + "NO"));
    }

    @Test void loanLifecycleKeepsAvailabilityConsistent() {
        thietBi.themMoi(d1(), "Thiết bị 1", 5, null, null);
        thietBi.themMoi(d2(), "Thiết bị 2", 3, null, null);

        phieu.taoPhieu(p1(), muon, hen, "Họp thử", tv1());
        phieu.themThietBi(p1(), d1(), 3);
        phieu.themThietBi(p1(), d2(), 1);
        assertEquals(5, thietBi.xemChiTiet(d1()).soLuongKhaDung(), "chưa duyệt thì chưa trừ");
        assertThrows(DataAccessException.class, () -> phieu.themThietBi(p1(), d1(), 1), "trùng thiết bị trong phiếu");
        assertThrows(DataAccessException.class, () -> phieu.taoPhieu(prefix + "X", muon, hen, "x", prefix + "NO"));

        phieu.duyet(p1());
        assertEquals(2, thietBi.xemChiTiet(d1()).soLuongKhaDung());
        assertEquals(2, thietBi.xemChiTiet(d2()).soLuongKhaDung());
        assertThrows(DataAccessException.class, () -> phieu.duyet(p1()));
        assertThrows(DataAccessException.class, () -> phieu.tuChoi(p1()));
        assertThrows(DataAccessException.class, () -> phieu.themThietBi(p1(), d1(), 1));

        // Hai phiếu chờ cùng mượn vừa đủ; phiếu thứ hai bị chặn lúc duyệt.
        phieu.taoPhieu(p2(), muon, hen, null, tv2());
        phieu.themThietBi(p2(), d1(), 2);
        phieu.taoPhieu(p3(), muon, hen, "Phiếu 3", tv2());
        phieu.themThietBi(p3(), d1(), 2);
        phieu.duyet(p2());
        assertEquals(0, thietBi.xemChiTiet(d1()).soLuongKhaDung());
        assertThrows(DataAccessException.class, () -> phieu.duyet(p3()));
        assertEquals(PhieuMuon.CHO_DUYET, phieu.xemPhieu(p3()).trangThaiDuyet());
        assertEquals(0, thietBi.xemChiTiet(d1()).soLuongKhaDung());
        phieu.tuChoi(p3());
        assertEquals(PhieuMuon.TU_CHOI, phieu.xemPhieu(p3()).trangThaiDuyet());

        // Trả: sai ngày bị chặn, đúng thì cộng lại; trả hai lần bị chặn; muộn xuất hiện trong view.
        assertThrows(DataAccessException.class, () -> phieu.traThietBi(p1(), d1(), muon.minusDays(1)));
        phieu.traThietBi(p1(), d1(), hen.plusDays(3));
        assertEquals(3, thietBi.xemChiTiet(d1()).soLuongKhaDung());
        assertThrows(DataAccessException.class, () -> phieu.traThietBi(p1(), d1(), hen.plusDays(4)));
        assertEquals(3, thietBi.xemChiTiet(d1()).soLuongKhaDung());
        DongTheoDoi muonTra = phieu.thietBiTraMuon().stream()
                .filter(d -> d.maPhieuMuon().equals(p1()) && d.maThietBi().equals(d1())).findFirst().orElseThrow();
        assertEquals(3, muonTra.soNgayTre());
        assertTrue(phieu.thietBiChuaTra().stream().anyMatch(d -> d.maPhieuMuon().equals(p1()) && d.maThietBi().equals(d2())));
        assertTrue(phieu.thietBiChuaTra().stream().anyMatch(d -> d.maPhieuMuon().equals(p2())
                && d.tinhTrang().equals("QuaHan")));

        phieu.traTatCa(p1(), hen);
        assertEquals(3, thietBi.xemChiTiet(d2()).soLuongKhaDung());
        assertThrows(DataAccessException.class, () -> phieu.traTatCa(p1(), hen));
        phieu.traTatCa(p2(), hen);
        assertEquals(5, thietBi.xemChiTiet(d1()).soLuongKhaDung(), "trả hết thì khả dụng về như cũ");

        assertEquals(2, phieu.lichSuMuon(tv2()).stream().map(LichSuMuon::maPhieuMuon).distinct().count());
        assertEquals(2, phieu.chiTiet(p1()).size());
        assertEquals(3, phieu.danhSachPhieu(null).stream().filter(p -> p.maPhieuMuon().startsWith(prefix)).count());
        assertThrows(DataAccessException.class, () -> phieu.lichSuMuon(prefix + "NO"));
    }

    private void execute(Connection c, String sql, String... args) throws SQLException {
        try (var ps = c.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setString(i + 1, args[i]);
            ps.executeUpdate();
        }
    }
}
