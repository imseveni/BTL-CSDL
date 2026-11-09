package thanhvien;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ThanhVienServiceTest {

    /** Dao giả: ghi lại thứ được gọi, không đụng tới MySQL. */
    private static class FakeDao extends ThanhVienDao {
        ThanhVien themVao;
        ThanhVien capNhatVao;
        String msvDoi;
        String trangThaiDoi;
        String msvXoa;
        String tuKhoa;
        String maBan;
        String trangThaiLoc;
        String banBaoCao;
        String trangThaiBaoCao;
        LocalDate tuBaoCao;
        LocalDate denBaoCao;

        @Override public void them(ThanhVien tv) { themVao = tv; }
        @Override public void capNhat(ThanhVien tv) { capNhatVao = tv; }
        @Override public void doiTrangThai(String msv, String tt) { msvDoi = msv; trangThaiDoi = tt; }
        @Override public void xoa(String msv) { msvXoa = msv; }
        @Override public List<ThanhVien> baoCaoDanhSach(String ban, String tt) {
            banBaoCao = ban; trangThaiBaoCao = tt;
            return List.of();
        }
        @Override public List<TongHopHoatDong> baoCaoHoatDong(LocalDate tu, LocalDate den) {
            tuBaoCao = tu; denBaoCao = den;
            return List.of();
        }
        @Override public List<ThanhVien> giaNhap(LocalDate tu, LocalDate den) { return List.of(); }
        @Override public List<ThanhVien> tim(String tk, String ban, String tt) {
            tuKhoa = tk; maBan = ban; trangThaiLoc = tt;
            return List.of();
        }
    }

    private static ThanhVien hopLe() {
        return new ThanhVien("B21DCCN100", "Nguyễn Văn Test", LocalDate.of(2004, 1, 1), "Nam",
                "test@example.com", "0912345678", LocalDate.of(2025, 1, 1), "Đang hoạt động", "BAN01",
                null, "Thành viên");
    }

    private static ThanhVien voi(java.util.function.UnaryOperator<ThanhVien> doi) {
        return doi.apply(hopLe());
    }

    @Test void thanhVienHopLeDuocChuyenXuongDao() {
        var dao = new FakeDao();
        new ThanhVienService(dao).them(hopLe());
        assertEquals("B21DCCN100", dao.themVao.msv());
        assertEquals("Nguyễn Văn Test", dao.themVao.hoVaTen());
        assertEquals("BAN01", dao.themVao.maBan());
    }

    @Test void chuanHoaKhoangTrangVaGiaTriMacDinh() {
        var dao = new FakeDao();
        var tv = new ThanhVien(" B21DCCN100 ", "  Trần Thị Bình  ", null, " ", " ", "",
                LocalDate.of(2025, 1, 1), " ", " BAN01 ", null, "");
        new ThanhVienService(dao).them(tv);
        assertEquals("B21DCCN100", dao.themVao.msv());
        assertEquals("Trần Thị Bình", dao.themVao.hoVaTen());
        assertNull(dao.themVao.gioiTinh());
        assertNull(dao.themVao.email());
        assertNull(dao.themVao.sdt());
        assertEquals("Đang hoạt động", dao.themVao.trangThaiHD());
        assertEquals("BAN01", dao.themVao.maBan());
        assertNull(dao.themVao.chucVu());
    }

    @Test void duLieuSaiKhongBaoGioXuongDao() {
        var dao = new FakeDao();
        var service = new ThanhVienService(dao);
        assertThrows(IllegalArgumentException.class, () -> service.them(null));
        assertThrows(IllegalArgumentException.class, () -> service.them(voi(t -> new ThanhVien(" ", t.hoVaTen(), t.ngaySinh(), t.gioiTinh(), t.email(), t.sdt(), t.ngayGiaNhap(), t.trangThaiHD(), t.maBan(), null, t.chucVu()))));
        assertThrows(IllegalArgumentException.class, () -> service.them(voi(t -> new ThanhVien("M".repeat(21), t.hoVaTen(), t.ngaySinh(), t.gioiTinh(), t.email(), t.sdt(), t.ngayGiaNhap(), t.trangThaiHD(), t.maBan(), null, t.chucVu()))));
        assertThrows(IllegalArgumentException.class, () -> service.them(voi(t -> new ThanhVien(t.msv(), " ", t.ngaySinh(), t.gioiTinh(), t.email(), t.sdt(), t.ngayGiaNhap(), t.trangThaiHD(), t.maBan(), null, t.chucVu()))));
        assertThrows(IllegalArgumentException.class, () -> service.them(voi(t -> new ThanhVien(t.msv(), "a".repeat(101), t.ngaySinh(), t.gioiTinh(), t.email(), t.sdt(), t.ngayGiaNhap(), t.trangThaiHD(), t.maBan(), null, t.chucVu()))));
        assertThrows(IllegalArgumentException.class, () -> service.them(voi(t -> new ThanhVien(t.msv(), t.hoVaTen(), LocalDate.now().plusDays(1), t.gioiTinh(), t.email(), t.sdt(), t.ngayGiaNhap(), t.trangThaiHD(), t.maBan(), null, t.chucVu()))));
        assertThrows(IllegalArgumentException.class, () -> service.them(voi(t -> new ThanhVien(t.msv(), t.hoVaTen(), t.ngaySinh(), "Robot", t.email(), t.sdt(), t.ngayGiaNhap(), t.trangThaiHD(), t.maBan(), null, t.chucVu()))));
        assertThrows(IllegalArgumentException.class, () -> service.them(voi(t -> new ThanhVien(t.msv(), t.hoVaTen(), t.ngaySinh(), t.gioiTinh(), "khong-co-a-cong", t.sdt(), t.ngayGiaNhap(), t.trangThaiHD(), t.maBan(), null, t.chucVu()))));
        assertThrows(IllegalArgumentException.class, () -> service.them(voi(t -> new ThanhVien(t.msv(), t.hoVaTen(), t.ngaySinh(), t.gioiTinh(), t.email(), "12ab", t.ngayGiaNhap(), t.trangThaiHD(), t.maBan(), null, t.chucVu()))));
        assertThrows(IllegalArgumentException.class, () -> service.them(voi(t -> new ThanhVien(t.msv(), t.hoVaTen(), t.ngaySinh(), t.gioiTinh(), t.email(), t.sdt(), null, t.trangThaiHD(), t.maBan(), null, t.chucVu()))));
        assertThrows(IllegalArgumentException.class, () -> service.them(voi(t -> new ThanhVien(t.msv(), t.hoVaTen(), t.ngaySinh(), t.gioiTinh(), t.email(), t.sdt(), LocalDate.now().plusDays(1), t.trangThaiHD(), t.maBan(), null, t.chucVu()))));
        assertThrows(IllegalArgumentException.class, () -> service.them(voi(t -> new ThanhVien(t.msv(), t.hoVaTen(), t.ngaySinh(), t.gioiTinh(), t.email(), t.sdt(), LocalDate.of(2003, 12, 31), t.trangThaiHD(), t.maBan(), null, t.chucVu()))));
        assertThrows(IllegalArgumentException.class, () -> service.them(voi(t -> new ThanhVien(t.msv(), t.hoVaTen(), t.ngaySinh(), t.gioiTinh(), t.email(), t.sdt(), t.ngayGiaNhap(), "Mất tích", t.maBan(), null, t.chucVu()))));
        assertThrows(IllegalArgumentException.class, () -> service.them(voi(t -> new ThanhVien(t.msv(), t.hoVaTen(), t.ngaySinh(), t.gioiTinh(), t.email(), t.sdt(), t.ngayGiaNhap(), t.trangThaiHD(), " ", null, t.chucVu()))));
        assertThrows(IllegalArgumentException.class, () -> service.them(voi(t -> new ThanhVien(t.msv(), t.hoVaTen(), t.ngaySinh(), t.gioiTinh(), t.email(), t.sdt(), t.ngayGiaNhap(), t.trangThaiHD(), t.maBan(), null, "c".repeat(101)))));
        assertNull(dao.themVao);
    }

    @Test void capNhatChiGuiThongTinCaNhan() {
        var dao = new FakeDao();
        new ThanhVienService(dao).capNhat(hopLe());
        assertEquals("B21DCCN100", dao.capNhatVao.msv());
        assertEquals("test@example.com", dao.capNhatVao.email());
        assertNull(dao.capNhatVao.maBan());
        assertNull(dao.capNhatVao.trangThaiHD());
        assertThrows(IllegalArgumentException.class,
                () -> new ThanhVienService(dao).capNhat(voi(t -> new ThanhVien(t.msv(), "", null, null, null, null, null, null, null, null, null))));
    }

    @Test void doiTrangThaiChiNhanGiaTriHopLe() {
        var dao = new FakeDao();
        var service = new ThanhVienService(dao);
        service.doiTrangThai(" B21DCCN100 ", " Tạm nghỉ ");
        assertEquals("B21DCCN100", dao.msvDoi);
        assertEquals("Tạm nghỉ", dao.trangThaiDoi);
        dao.msvDoi = null;
        assertThrows(IllegalArgumentException.class, () -> service.doiTrangThai("B21DCCN100", "Bay mất"));
        assertThrows(IllegalArgumentException.class, () -> service.doiTrangThai(" ", "Tạm nghỉ"));
        assertThrows(IllegalArgumentException.class, () -> service.doiTrangThai("B21DCCN100", null));
        assertNull(dao.msvDoi);
    }

    @Test void xoaVaTimKiemChuanHoaThamSo() {
        var dao = new FakeDao();
        var service = new ThanhVienService(dao);
        service.xoa(" B21DCCN100 ");
        assertEquals("B21DCCN100", dao.msvXoa);
        assertThrows(IllegalArgumentException.class, () -> service.xoa(""));
        service.tim("  An ", " ", "");
        assertEquals("An", dao.tuKhoa);
        assertNull(dao.maBan);
        assertNull(dao.trangThaiLoc);
        service.tim(null, "BAN01", "Đã rời CLB");
        assertNull(dao.tuKhoa);
        assertEquals("BAN01", dao.maBan);
        assertEquals("Đã rời CLB", dao.trangThaiLoc);
        assertThrows(IllegalArgumentException.class, () -> service.tim("", "", "Không có"));
    }

    @Test void khoangNgayGiaNhapKhongDuocDaoNguoc() {
        var service = new ThanhVienService(new FakeDao());
        assertThrows(IllegalArgumentException.class,
                () -> service.giaNhap(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)));
        assertDoesNotThrow(() -> service.giaNhap(null, LocalDate.of(2026, 1, 1)));
        assertDoesNotThrow(() -> service.giaNhap(LocalDate.of(2026, 1, 1), null));
    }

    @Test void baoCaoDanhSachChuanHoaThamSoVaChanTrangThaiLa() {
        var dao = new FakeDao();
        var service = new ThanhVienService(dao);
        service.baoCaoDanhSach(" BAN01 ", " Tạm nghỉ ");
        assertEquals("BAN01", dao.banBaoCao);
        assertEquals("Tạm nghỉ", dao.trangThaiBaoCao);
        service.baoCaoDanhSach("", " ");
        assertNull(dao.banBaoCao);
        assertNull(dao.trangThaiBaoCao);
        assertThrows(IllegalArgumentException.class, () -> service.baoCaoDanhSach(null, "Bay mất"));
        assertThrows(IllegalArgumentException.class, () -> service.baoCaoDanhSach("b".repeat(21), null));
    }

    @Test void baoCaoHoatDongCanDuHaiNgayVaKhongDaoNguoc() {
        var dao = new FakeDao();
        var service = new ThanhVienService(dao);
        service.baoCaoHoatDong(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
        assertEquals(LocalDate.of(2026, 1, 1), dao.tuBaoCao);
        assertEquals(LocalDate.of(2026, 6, 30), dao.denBaoCao);
        dao.tuBaoCao = null;
        assertDoesNotThrow(() -> service.baoCaoHoatDong(LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 3)));
        assertThrows(IllegalArgumentException.class, () -> service.baoCaoHoatDong(null, LocalDate.of(2026, 6, 30)));
        assertThrows(IllegalArgumentException.class, () -> service.baoCaoHoatDong(LocalDate.of(2026, 1, 1), null));
        assertThrows(IllegalArgumentException.class,
                () -> service.baoCaoHoatDong(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 6, 30)));
        assertEquals(LocalDate.of(2026, 3, 3), dao.tuBaoCao);    // các ca sai không chạm tới Dao
    }
}
