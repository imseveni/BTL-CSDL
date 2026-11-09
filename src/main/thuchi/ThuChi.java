package thuchi;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể Giao dịch Thu – Chi ánh xạ bảng ThuChi và bảng đa trị ThuChi_PhuongThucThanhToan.
 */
public class ThuChi {

    private String maGD;
    private String loaiGD;
    private String mucDich;
    private BigDecimal soTien;
    private LocalDateTime thoiGian;
    private String minhChung;
    private String msv;
    private String hoTenThanhVien;
    private String maSK;
    private String tenSuKien;
    private List<String> phuongThucThanhToan = new ArrayList<>();

    public ThuChi() {
    }

    public ThuChi(String maGD, String loaiGD, String mucDich, BigDecimal soTien,
                  LocalDateTime thoiGian, String minhChung, String msv, String maSK,
                  List<String> phuongThucThanhToan) {
        this.maGD = maGD;
        this.loaiGD = loaiGD;
        this.mucDich = mucDich;
        this.soTien = soTien;
        this.thoiGian = thoiGian;
        this.minhChung = minhChung;
        this.msv = msv;
        this.maSK = maSK;
        if (phuongThucThanhToan != null) {
            this.phuongThucThanhToan = new ArrayList<>(phuongThucThanhToan);
        }
    }

    public String getMaGD() {
        return maGD;
    }

    public void setMaGD(String maGD) {
        this.maGD = maGD;
    }

    public String getLoaiGD() {
        return loaiGD;
    }

    public void setLoaiGD(String loaiGD) {
        this.loaiGD = loaiGD;
    }

    public String getMucDich() {
        return mucDich;
    }

    public void setMucDich(String mucDich) {
        this.mucDich = mucDich;
    }

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }

    public String getMinhChung() {
        return minhChung;
    }

    public void setMinhChung(String minhChung) {
        this.minhChung = minhChung;
    }

    public String getMsv() {
        return msv;
    }

    public void setMsv(String msv) {
        this.msv = msv;
    }

    public String getHoTenThanhVien() {
        return hoTenThanhVien;
    }

    public void setHoTenThanhVien(String hoTenThanhVien) {
        this.hoTenThanhVien = hoTenThanhVien;
    }

    public String getMaSK() {
        return maSK;
    }

    public void setMaSK(String maSK) {
        this.maSK = maSK;
    }

    public String getTenSuKien() {
        return tenSuKien;
    }

    public void setTenSuKien(String tenSuKien) {
        this.tenSuKien = tenSuKien;
    }

    public List<String> getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(List<String> phuongThucThanhToan) {
        this.phuongThucThanhToan = (phuongThucThanhToan != null)
                ? new ArrayList<>(phuongThucThanhToan)
                : new ArrayList<>();
    }

    public String getChuoiPhuongThucThanhToan() {
        return String.join(", ", phuongThucThanhToan);
    }
}

