package muontra;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PhieuMuonServiceTest {
    private static final LocalDate MUON = LocalDate.of(2026, 10, 29);

    private static class FakeDao extends PhieuMuonDao {
        final List<String> calls = new ArrayList<>();
        @Override public void taoPhieu(String ma, LocalDate muon, LocalDate hen, String mucDich, String msv) {
            calls.add("tao:" + ma + ":" + muon + ":" + hen + ":" + mucDich + ":" + msv);
        }
        @Override public void themChiTiet(String phieu, String thietBi, int soLuong) { calls.add("them:" + phieu + ":" + thietBi + ":" + soLuong); }
        @Override public void duyet(String phieu) { calls.add("duyet:" + phieu); }
        @Override public void tuChoi(String phieu) { calls.add("tuchoi:" + phieu); }
        @Override public void traThietBi(String phieu, String thietBi, LocalDate ngay) { calls.add("tra:" + phieu + ":" + thietBi + ":" + ngay); }
        @Override public void traTatCa(String phieu, LocalDate ngay) { calls.add("traTatCa:" + phieu + ":" + ngay); }
        @Override public List<PhieuMuon> danhSachPhieu(String trangThai) { calls.add("ds:" + trangThai); return List.of(); }
        @Override public PhieuMuon timPhieu(String ma) {
            return "PM1".equals(ma) ? new PhieuMuon("PM1", MUON, MUON, null, PhieuMuon.CHO_DUYET, "TV1") : null;
        }
    }

    @Test void invalidLoanNeverReachesPersistence() {
        var dao = new FakeDao();
        var service = new PhieuMuonService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.taoPhieu("", MUON, MUON, "x", "TV1"));
        assertThrows(IllegalArgumentException.class, () -> service.taoPhieu("P".repeat(21), MUON, MUON, "x", "TV1"));
        assertThrows(IllegalArgumentException.class, () -> service.taoPhieu("PM1", MUON, MUON, "x", " "));
        assertThrows(IllegalArgumentException.class, () -> service.taoPhieu("PM1", null, MUON, "x", "TV1"));
        assertThrows(IllegalArgumentException.class, () -> service.taoPhieu("PM1", MUON, null, "x", "TV1"));
        var ex = assertThrows(IllegalArgumentException.class, () -> service.taoPhieu("PM1", MUON, MUON.minusDays(1), "x", "TV1"));
        assertTrue(ex.getMessage().contains("Ngày hẹn trả"));
        assertTrue(dao.calls.isEmpty());
    }

    @Test void trimsInputAndKeepsBlankPurposeAsNull() {
        var dao = new FakeDao();
        var service = new PhieuMuonService(dao);
        service.taoPhieu(" PM1 ", MUON, MUON.plusDays(3), "  Họp ban  ", " TV1 ");
        service.taoPhieu("PM2", MUON, MUON, "   ", "TV1");
        assertEquals(List.of("tao:PM1:2026-10-29:2026-11-01:Họp ban:TV1", "tao:PM2:2026-10-29:2026-10-29:null:TV1"), dao.calls);
    }

    @Test void invalidQuantityOrCodeNeverReachesPersistence() {
        var dao = new FakeDao();
        var service = new PhieuMuonService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.themThietBi("PM1", "TB1", 0));
        assertThrows(IllegalArgumentException.class, () -> service.themThietBi("PM1", "TB1", -2));
        assertThrows(IllegalArgumentException.class, () -> service.themThietBi(" ", "TB1", 1));
        assertThrows(IllegalArgumentException.class, () -> service.themThietBi("PM1", null, 1));
        assertThrows(IllegalArgumentException.class, () -> service.duyet(""));
        assertThrows(IllegalArgumentException.class, () -> service.tuChoi(null));
        assertThrows(IllegalArgumentException.class, () -> service.traThietBi("PM1", " ", MUON));
        assertThrows(IllegalArgumentException.class, () -> service.traTatCa("", MUON));
        assertThrows(IllegalArgumentException.class, () -> service.lichSuMuon(" "));
        assertTrue(dao.calls.isEmpty());
    }

    @Test void validOperationsReachDaoWithCleanIdentifiers() {
        var dao = new FakeDao();
        var service = new PhieuMuonService(dao);
        service.themThietBi(" PM1 ", " TB1 ", 2);
        service.duyet(" PM1 ");
        service.tuChoi(" PM2 ");
        service.traThietBi("PM1", "TB1", null);
        service.traTatCa("PM1", MUON);
        assertEquals(List.of("them:PM1:TB1:2", "duyet:PM1", "tuchoi:PM2", "tra:PM1:TB1:null", "traTatCa:PM1:2026-10-29"), dao.calls);
    }

    @Test void statusFilterAcceptsOnlyKnownStatuses() {
        var dao = new FakeDao();
        var service = new PhieuMuonService(dao);
        service.danhSachPhieu(null);
        service.danhSachPhieu("  ");
        service.danhSachPhieu(" DaDuyet ");
        assertThrows(IllegalArgumentException.class, () -> service.danhSachPhieu("DaXoa"));
        assertEquals(List.of("ds:null", "ds:null", "ds:DaDuyet"), dao.calls);
    }

    @Test void missingLoanIsReported() {
        var service = new PhieuMuonService(new FakeDao());
        assertEquals("TV1", service.xemPhieu(" PM1 ").msvNguoiMuon());
        assertThrows(IllegalArgumentException.class, () -> service.xemPhieu("KHONG_CO"));
    }
}
