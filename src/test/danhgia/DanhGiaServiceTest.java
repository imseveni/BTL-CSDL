package danhgia;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DanhGiaServiceTest {
    private static class FakeDao extends DanhGiaDao {
        DanhGia saved;
        @Override public void them(DanhGia value) { saved = value; }
        @Override public void sua(DanhGia value) { saved = value; }
    }

    @Test void requiresDateAndValidMySqlDateRange() {
        var dao = new FakeDao();
        var service = new DanhGiaService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.them(null));
        assertThrows(IllegalArgumentException.class, () -> service.them(dg(null, "Lý do")));
        assertThrows(IllegalArgumentException.class, () -> service.them(dg(LocalDate.of(999, 1, 1), "Lý do")));
        assertThrows(IllegalArgumentException.class, () -> service.them(dg(LocalDate.of(10000, 1, 1), "Lý do")));
        assertNull(dao.saved);
    }

    @Test void requiresCompleteDecisionAndChecksTextBytes() {
        var dao = new FakeDao();
        var service = new DanhGiaService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.them(dg(LocalDate.of(2026, 1, 1), " ")));
        assertThrows(IllegalArgumentException.class, () -> service.them(dg(LocalDate.of(2026, 1, 1), "đ".repeat(32768))));
        assertThrows(IllegalArgumentException.class, () -> service.them(new DanhGia("D1", "", "Giấy khen", "Lý do", LocalDate.of(2026, 1, 1), "TV1")));
        assertThrows(IllegalArgumentException.class, () -> service.sua(new DanhGia("D1", "Khen thưởng", " ", "Lý do", LocalDate.of(2026, 1, 1), "TV1")));
        assertNull(dao.saved);
    }

    @Test void keepsUnfinalizedCategoryListOpenAndTrimsInput() {
        var dao = new FakeDao();
        var service = new DanhGiaService(dao);
        service.sua(new DanhGia(" D1 ", " Ghi nhận đóng góp ", " Thư cảm ơn ", " Hỗ trợ sự kiện ", LocalDate.of(2026, 10, 25), " TV1 "));
        assertEquals("D1", dao.saved.maGhiNhan());
        assertEquals("TV1", dao.saved.msv());
        assertEquals("Ghi nhận đóng góp", dao.saved.loaiDanhGia());
        assertEquals("Hỗ trợ sự kiện", dao.saved.lyDo());
    }

    private DanhGia dg(LocalDate date, String reason) {
        return new DanhGia("D1", "Khen thưởng", "Giấy khen", reason, date, "TV1");
    }
}
