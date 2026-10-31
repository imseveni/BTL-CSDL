package cosovatchat;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CoSoVatChatServiceTest {
    private static class FakeDao extends CoSoVatChatDao {
        final List<String> calls = new ArrayList<>();
        @Override public void themMoi(String ma, String ten, int tong, Integer khaDung, String msv) {
            calls.add("them:" + ma + ":" + ten + ":" + tong + ":" + khaDung + ":" + msv);
        }
        @Override public void capNhat(String ma, String ten, int tong) { calls.add("sua:" + ma + ":" + ten + ":" + tong); }
        @Override public void ganNguoiQuanLy(String ma, String msv) { calls.add("gan:" + ma + ":" + msv); }
        @Override public boolean kiemTraKhaDung(String ma, int soLuong) { calls.add("kiemTra:" + ma + ":" + soLuong); return soLuong <= 2; }
        @Override public CoSoVatChat timTheoMa(String ma) {
            return "TB1".equals(ma) ? new CoSoVatChat("TB1", "Máy chiếu", 3, 2, null, null) : null;
        }
        @Override public List<CoSoVatChat> timKiem(String tuKhoa) { calls.add("tim:[" + tuKhoa + "]"); return List.of(); }
    }

    @Test void invalidDeviceNeverReachesPersistence() {
        var dao = new FakeDao();
        var service = new CoSoVatChatService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.themMoi("", "Loa", 1, null, null));
        assertThrows(IllegalArgumentException.class, () -> service.themMoi("TB1", " ", 1, null, null));
        assertThrows(IllegalArgumentException.class, () -> service.themMoi("T".repeat(21), "Loa", 1, null, null));
        assertThrows(IllegalArgumentException.class, () -> service.themMoi("TB1", "L".repeat(151), 1, null, null));
        assertThrows(IllegalArgumentException.class, () -> service.themMoi("TB1", "Loa", 0, null, null));
        assertThrows(IllegalArgumentException.class, () -> service.themMoi("TB1", "Loa", 3, 4, null));
        assertThrows(IllegalArgumentException.class, () -> service.themMoi("TB1", "Loa", 3, -1, null));
        assertThrows(IllegalArgumentException.class, () -> service.themMoi("TB1", "Loa", 3, null, "M".repeat(21)));
        assertThrows(IllegalArgumentException.class, () -> service.capNhat("TB1", "Loa", -1));
        assertTrue(dao.calls.isEmpty());
    }

    @Test void trimsInputAndTreatsBlankManagerAsNone() {
        var dao = new FakeDao();
        var service = new CoSoVatChatService(dao);
        service.themMoi(" TB2 ", " Loa ", 4, null, "  ");
        service.themMoi("TB3", "Micro", 2, 1, " TV1 ");
        service.ganNguoiQuanLy(" TB2 ", "");
        service.capNhat(" TB2 ", " Loa mới ", 5);
        assertEquals(List.of("them:TB2:Loa:4:null:null", "them:TB3:Micro:2:1:TV1", "gan:TB2:null", "sua:TB2:Loa mới:5"), dao.calls);
    }

    @Test void availabilityCheckRejectsNonPositiveQuantityBeforePersistence() {
        var dao = new FakeDao();
        var service = new CoSoVatChatService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.kiemTraKhaDung("TB1", 0));
        assertThrows(IllegalArgumentException.class, () -> service.kiemTraKhaDung(" ", 1));
        assertTrue(dao.calls.isEmpty());
        assertTrue(service.kiemTraKhaDung(" TB1 ", 2));
        assertFalse(service.kiemTraKhaDung("TB1", 3));
    }

    @Test void detailLookupReportsMissingDeviceAndNullSearchMeansAll() {
        var dao = new FakeDao();
        var service = new CoSoVatChatService(dao);
        assertEquals(1, service.xemChiTiet(" TB1 ").soLuongDangMuon());
        assertThrows(IllegalArgumentException.class, () -> service.xemChiTiet("KHONG_CO"));
        service.timKiem(null);
        service.timKiem("  loa ");
        assertEquals(List.of("tim:[]", "tim:[loa]"), dao.calls);
    }
}
