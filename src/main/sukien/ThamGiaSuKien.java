package sukien;

import java.time.LocalDate;

/**
 * Thực thể ThamGiaSuKien ánh xạ bảng ThamGiaSuKien(MSV, MaSK, VaiTro)
 * kèm các thông tin tra cứu từ bảng ThanhVien và SuKien.
 */
public class ThamGiaSuKien {

    private String msv;
    private String hoVaTen;
    private String chucVu;
    private String maBan;
    private String maSK;
    private String tenSK;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private String vaiTro;

    public ThamGiaSuKien() {
    }

    public ThamGiaSuKien(String msv, String maSK, String vaiTro) {
        this.msv = msv;
        this.maSK = maSK;
        this.vaiTro = vaiTro;
    }

    public String getMsv() {
        return msv;
    }

    public void setMsv(String msv) {
        this.msv = msv;
    }

    public String getHoVaTen() {
        return hoVaTen;
    }

    public void setHoVaTen(String hoVaTen) {
        this.hoVaTen = hoVaTen;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public String getMaBan() {
        return maBan;
    }

    public void setMaBan(String maBan) {
        this.maBan = maBan;
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

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }
}

