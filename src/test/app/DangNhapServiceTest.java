package app;

import static org.junit.jupiter.api.Assertions.*;

import common.DataAccessException;
import common.PhienLamViec.VaiTro;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import thanhvien.ThanhVien;

class DangNhapServiceTest {

    private static ThanhVien tv(String msv, String trangThai) {
        return new ThanhVien(msv, "Nguyễn Văn " + msv, null, null, null, null, LocalDate.of(2024, 9, 1), trangThai,
                "BAN01", null, "Thành viên");
    }

    private final DangNhapService service = new DangNhapService(msv -> switch (msv) {
        case "B21DCCN001" -> tv(msv, "Đang hoạt động");
        case "B21DCCN009" -> tv(msv, "Đã rời CLB");
        case "LOI_KET_NOI" -> throw new DataAccessException("Không kết nối được", new RuntimeException("boom"));
        default -> throw new DataAccessException("Thành viên không tồn tại", null);
    }, "MA-DEMO");

    @Test void thanhVienDangNhapBangMsv() {
        var nguoi = service.dangNhapThanhVien(" B21DCCN001 ");
        assertEquals(VaiTro.THANH_VIEN, nguoi.vaiTro());
        assertEquals("B21DCCN001", nguoi.msv());
    }

    @Test void banChuNhiemCanDungMaXacNhan() {
        assertEquals(VaiTro.BAN_CHU_NHIEM, service.dangNhapBanChuNhiem("B21DCCN001", "MA-DEMO").vaiTro());
        assertThrows(IllegalArgumentException.class, () -> service.dangNhapBanChuNhiem("B21DCCN001", "sai"));
        assertThrows(IllegalArgumentException.class, () -> service.dangNhapBanChuNhiem("B21DCCN001", " "));
    }

    @Test void tuChoiMsvKhongTonTaiHoacDaRoiCLB() {
        assertTrue(assertThrows(IllegalArgumentException.class, () -> service.dangNhapThanhVien("KHONG_CO"))
                .getMessage().contains("KHONG_CO"));
        assertTrue(assertThrows(IllegalArgumentException.class, () -> service.dangNhapThanhVien("B21DCCN009"))
                .getMessage().contains("đã rời"));
        assertThrows(IllegalArgumentException.class, () -> service.dangNhapThanhVien(""));
        // Mã đúng nhưng MSV không có trong CLB vẫn bị từ chối.
        assertThrows(IllegalArgumentException.class, () -> service.dangNhapBanChuNhiem("KHONG_CO", "MA-DEMO"));
    }

    @Test void loiKetNoiKhongBiBaoThanhKhongTonTai() {
        assertThrows(DataAccessException.class, () -> service.dangNhapThanhVien("LOI_KET_NOI"));
    }
}
