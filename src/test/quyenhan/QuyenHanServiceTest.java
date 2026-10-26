package quyenhan;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class QuyenHanServiceTest {
    private static class FakeDao extends QuyenHanDao {
        QuyenHan saved;
        String msv;
        String quyen;
        @Override public void them(QuyenHan value) { saved = value; }
        @Override public void capQuyen(String tv, String q) { msv = tv; quyen = q; }
        @Override public void thuHoiQuyen(String tv, String q) { msv = tv; quyen = q; }
    }

    @Test void invalidPermissionNeverReachesPersistence() {
        var dao = new FakeDao();
        var service = new QuyenHanService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.them(null));
        assertThrows(IllegalArgumentException.class, () -> service.them(new QuyenHan("", "Tên", null)));
        assertThrows(IllegalArgumentException.class, () -> service.them(new QuyenHan("Q1", " ", null)));
        assertThrows(IllegalArgumentException.class, () -> service.them(new QuyenHan("Q".repeat(21), "Tên", null)));
        assertThrows(IllegalArgumentException.class, () -> service.them(new QuyenHan("Q1", "Tên", "đ".repeat(32768))));
        assertNull(dao.saved);
    }

    @Test void trimsIdentifiersAndAllowsOptionalDescription() {
        var dao = new FakeDao();
        var service = new QuyenHanService(dao);
        service.them(new QuyenHan(" Q1 ", " Quyền đọc ", " "));
        assertEquals(new QuyenHan("Q1", "Quyền đọc", null), dao.saved);
        service.capQuyen(" TV1 ", " Q1 ");
        assertEquals("TV1", dao.msv);
        assertEquals("Q1", dao.quyen);
        service.thuHoiQuyen(" TV2 ", " Q2 ");
        assertEquals("TV2", dao.msv);
        assertEquals("Q2", dao.quyen);
    }

    @Test void invalidGrantAndRevokeDoNotReachPersistence() {
        var dao = new FakeDao();
        var service = new QuyenHanService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.capQuyen("", "Q1"));
        assertThrows(IllegalArgumentException.class, () -> service.thuHoiQuyen("TV1", null));
        assertNull(dao.msv);
    }
}
