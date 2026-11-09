package sukien;

import java.time.LocalDate;
import java.util.List;

/**
 * Tầng nghiệp vụ (Service) cho Module 06 – Quản lý Sự kiện.
 * Kiểm tra tính hợp lệ của dữ liệu và quy tắc nghiệp vụ trước khi gọi SuKienDao.
 */
public class SuKienService {

    private final SuKienDao suKienDao;

    public SuKienService() {
        this.suKienDao = new SuKienDao();
    }

    public SuKienService(SuKienDao suKienDao) {
        this.suKienDao = suKienDao;
    }

    public List<SuKien> layTatCaSuKien() {
        return suKienDao.layTatCa();
    }

    public SuKien timSuKienTheoMa(String maSK) {
        if (maSK == null || maSK.isBlank()) {
            throw new IllegalArgumentException("Mã sự kiện không được để trống.");
        }
        return suKienDao.timTheoMa(maSK.trim());
    }

    public List<SuKien> timSuKienTheoTen(String tuKhoa) {
        if (tuKhoa == null) {
            tuKhoa = "";
        }
        return suKienDao.timTheoTen(tuKhoa.trim());
    }

    public List<SuKien> traCuuTheoThoiGian(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay != null && denNgay != null && denNgay.isBefore(tuNgay)) {
            throw new IllegalArgumentException("Ngày kết thúc khoảng tra cứu phải lớn hơn hoặc bằng ngày bắt đầu.");
        }
        return suKienDao.traCuuTheoThoiGian(tuNgay, denNgay);
    }

    public void themSuKien(SuKien sk) {
        kiemTraThongTinSuKien(sk);
        String maSK = sk.getMaSK().trim();
        if (suKienDao.timTheoMa(maSK) != null) {
            throw new IllegalArgumentException("Mã sự kiện '" + maSK + "' đã tồn tại.");
        }
        sk.setMaSK(maSK);
        sk.setTenSK(sk.getTenSK().trim());
        suKienDao.them(sk);
    }

    public void capNhatSuKien(SuKien sk) {
        kiemTraThongTinSuKien(sk);
        String maSK = sk.getMaSK().trim();
        if (suKienDao.timTheoMa(maSK) == null) {
            throw new IllegalArgumentException("Không tìm thấy sự kiện có mã '" + maSK + "'.");
        }
        sk.setMaSK(maSK);
        sk.setTenSK(sk.getTenSK().trim());
        suKienDao.capNhat(sk);
    }

    public void xoaSuKien(String maSK) {
        if (maSK == null || maSK.isBlank()) {
            throw new IllegalArgumentException("Mã sự kiện không được để trống.");
        }
        String ma = maSK.trim();
        if (suKienDao.timTheoMa(ma) == null) {
            throw new IllegalArgumentException("Không tìm thấy sự kiện có mã '" + ma + "'.");
        }
        if (suKienDao.coGiaoDichThuChi(ma)) {
            throw new IllegalStateException(
                    "Không thể xóa sự kiện '" + ma + "' vì đang có giao dịch thu – chi liên kết với sự kiện này.");
        }
        suKienDao.xoa(ma);
    }

    public void themThanhVienThamGia(String msv, String maSK, String vaiTro) {
        if (msv == null || msv.isBlank()) {
            throw new IllegalArgumentException("Mã sinh viên (MSV) không được để trống.");
        }
        if (maSK == null || maSK.isBlank()) {
            throw new IllegalArgumentException("Mã sự kiện (MaSK) không được để trống.");
        }
        if (vaiTro == null || vaiTro.isBlank()) {
            throw new IllegalArgumentException("Vai trò tham gia không được để trống.");
        }

        String msvChuan = msv.trim();
        String maSKChuan = maSK.trim();
        String vaiTroChuan = vaiTro.trim();

        if (suKienDao.timTheoMa(maSKChuan) == null) {
            throw new IllegalArgumentException("Không tìm thấy sự kiện có mã '" + maSKChuan + "'.");
        }
        if (!suKienDao.tonTaiThanhVien(msvChuan)) {
            throw new IllegalArgumentException("Không tìm thấy thành viên có MSV '" + msvChuan + "' trong hệ thống.");
        }
        if (suKienDao.daThamGia(msvChuan, maSKChuan)) {
            throw new IllegalStateException(
                    "Thành viên '" + msvChuan + "' đã có trong danh sách tham gia sự kiện '" + maSKChuan + "'.");
        }

        suKienDao.themThanhVienThamGia(msvChuan, maSKChuan, vaiTroChuan);
    }

    public void capNhatVaiTroThamGia(String msv, String maSK, String vaiTroMoi) {
        if (msv == null || msv.isBlank()) {
            throw new IllegalArgumentException("Mã sinh viên (MSV) không được để trống.");
        }
        if (maSK == null || maSK.isBlank()) {
            throw new IllegalArgumentException("Mã sự kiện (MaSK) không được để trống.");
        }
        if (vaiTroMoi == null || vaiTroMoi.isBlank()) {
            throw new IllegalArgumentException("Vai trò mới không được để trống.");
        }

        String msvChuan = msv.trim();
        String maSKChuan = maSK.trim();
        if (!suKienDao.daThamGia(msvChuan, maSKChuan)) {
            throw new IllegalArgumentException(
                    "Thành viên '" + msvChuan + "' chưa tham gia sự kiện '" + maSKChuan + "'.");
        }
        suKienDao.capNhatVaiTroThamGia(msvChuan, maSKChuan, vaiTroMoi.trim());
    }

    public void xoaThanhVienKhoiSuKien(String msv, String maSK) {
        if (msv == null || msv.isBlank() || maSK == null || maSK.isBlank()) {
            throw new IllegalArgumentException("MSV và Mã sự kiện không được để trống.");
        }
        String msvChuan = msv.trim();
        String maSKChuan = maSK.trim();
        if (!suKienDao.xoaThanhVienThamGia(msvChuan, maSKChuan)) {
            throw new IllegalArgumentException(
                    "Không tìm thấy bản ghi tham gia của thành viên '" + msvChuan + "' tại sự kiện '" + maSKChuan + "'.");
        }
    }

    public List<ThamGiaSuKien> xemThanhVienCuaSuKien(String maSK) {
        if (maSK == null || maSK.isBlank()) {
            throw new IllegalArgumentException("Mã sự kiện không được để trống.");
        }
        String maSKChuan = maSK.trim();
        if (suKienDao.timTheoMa(maSKChuan) == null) {
            throw new IllegalArgumentException("Không tìm thấy sự kiện có mã '" + maSKChuan + "'.");
        }
        return suKienDao.layThanhVienTheoSuKien(maSKChuan);
    }

    public List<ThamGiaSuKien> xemLichSuSuKienCuaThanhVien(String msv) {
        if (msv == null || msv.isBlank()) {
            throw new IllegalArgumentException("Mã sinh viên (MSV) không được để trống.");
        }
        String msvChuan = msv.trim();
        if (!suKienDao.tonTaiThanhVien(msvChuan)) {
            throw new IllegalArgumentException("Không tìm thấy thành viên có MSV '" + msvChuan + "' trong hệ thống.");
        }
        return suKienDao.layLichSuSuKienCuaThanhVien(msvChuan);
    }

    private void kiemTraThongTinSuKien(SuKien sk) {
        if (sk == null) {
            throw new IllegalArgumentException("Thông tin sự kiện không hợp lệ.");
        }
        if (sk.getMaSK() == null || sk.getMaSK().isBlank()) {
            throw new IllegalArgumentException("Mã sự kiện không được để trống.");
        }
        if (sk.getMaSK().trim().length() > 20) {
            throw new IllegalArgumentException("Mã sự kiện không được vượt quá 20 ký tự.");
        }
        if (sk.getTenSK() == null || sk.getTenSK().isBlank()) {
            throw new IllegalArgumentException("Tên sự kiện không được để trống.");
        }
        if (sk.getTenSK().trim().length() > 150) {
            throw new IllegalArgumentException("Tên sự kiện không được vượt quá 150 ký tự.");
        }
        if (sk.getNgayBatDau() == null) {
            throw new IllegalArgumentException("Ngày bắt đầu sự kiện không được để trống.");
        }
        if (sk.getNgayKetThuc() != null && sk.getNgayKetThuc().isBefore(sk.getNgayBatDau())) {
            throw new IllegalArgumentException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.");
        }
    }
}
