package thuchi;

import java.math.BigDecimal;

/**
 * Đối tượng kết quả thống kê Thu – Chi theo từng sự kiện.
 */
public class ThongKeThuChiSuKien {

    private String maSK;
    private String tenSK;
    private int soGiaoDich;
    private BigDecimal tongThu;
    private BigDecimal tongChi;
    private BigDecimal chenhLech;

    public ThongKeThuChiSuKien() {
    }

    public ThongKeThuChiSuKien(String maSK, String tenSK, int soGiaoDich,
                               BigDecimal tongThu, BigDecimal tongChi, BigDecimal chenhLech) {
        this.maSK = maSK;
        this.tenSK = tenSK;
        this.soGiaoDich = soGiaoDich;
        this.tongThu = tongThu;
        this.tongChi = tongChi;
        this.chenhLech = chenhLech;
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

    public int getSoGiaoDich() {
        return soGiaoDich;
    }

    public void setSoGiaoDich(int soGiaoDich) {
        this.soGiaoDich = soGiaoDich;
    }

    public BigDecimal getTongThu() {
        return tongThu;
    }

    public void setTongThu(BigDecimal tongThu) {
        this.tongThu = tongThu;
    }

    public BigDecimal getTongChi() {
        return tongChi;
    }

    public void setTongChi(BigDecimal tongChi) {
        this.tongChi = tongChi;
    }

    public BigDecimal getChenhLech() {
        return chenhLech;
    }

    public void setChenhLech(BigDecimal chenhLech) {
        this.chenhLech = chenhLech;
    }
}

