package banchuyenmon;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class BanChuyenMonServiceTest {
    private static class FakeDao extends BanChuyenMonDao {
        BanChuyenMon saved;
        String msv;
        String ban;
        String chucVu;
        @Override public void them(BanChuyenMon value) { saved = value; }
        @Override public void chuyenBan(String tv, String ma) { msv = tv; ban = ma; }
        @Override public void doiChucVu(String tv, String cv) { msv = tv; chucVu = cv; }
    }

    @Test void invalidDepartmentNeverReachesPersistence() {
        var dao = new FakeDao();
        var service = new BanChuyenMonService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.them(new BanChuyenMon(" ", "Tên")));
        assertThrows(IllegalArgumentException.class, () -> service.them(new BanChuyenMon("B1", " ")));
        assertThrows(IllegalArgumentException.class, () -> service.them(new BanChuyenMon("B".repeat(21), "Tên")));
        assertThrows(IllegalArgumentException.class, () -> service.them(new BanChuyenMon("B1", "a".repeat(101))));
        assertThrows(IllegalArgumentException.class, () -> service.them(null));
        assertNull(dao.saved);
    }

    @Test void trimsIdentifiersAndSupportsVietnamese() {
        var dao = new FakeDao();
        var service = new BanChuyenMonService(dao);
        service.them(new BanChuyenMon(" B1 ", " Ban Nhân sự "));
        assertEquals(new BanChuyenMon("B1", "Ban Nhân sự"), dao.saved);
        service.chuyenBan(" TV1 ", " B1 ");
        assertEquals("TV1", dao.msv);
        assertEquals("B1", dao.ban);
        service.doiChucVu("TV1", " Trưởng ban ");
        assertEquals("Trưởng ban", dao.chucVu);
    }

    @Test void invalidAssignmentDoesNotReachPersistence() {
        var dao = new FakeDao();
        var service = new BanChuyenMonService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.chuyenBan(null, "B1"));
        assertThrows(IllegalArgumentException.class, () -> service.chuyenBan("TV1", ""));
        assertThrows(IllegalArgumentException.class, () -> service.doiChucVu("TV1", " "));
        assertNull(dao.msv);
    }
}
