package sukien;

import java.time.LocalDate;

/**
 * Thực thể Sự kiện ánh xạ bảng SuKien(MaSK, TenSK, NgayBatDau, NgayKetThuc).
 */
public class SuKien {

    private String maSK;
    private String tenSK;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;

    public SuKien() {
    }

    public SuKien(String maSK, String tenSK, LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        this.maSK = maSK;
        this.tenSK = tenSK;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getMaSK() {
        return maSK;
    }

    public void setMaSK(String maSK) {
        this.maSK = maSK;
    }

    public String getTenSK() {
        return tenSK;
    }

    public void setTenSK(String tenSK) {
        this.tenSK = tenSK;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }
}
