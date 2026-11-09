package duan;

import java.time.LocalDate;

/** Tiến độ chung là thuộc tính dẫn xuất, không phải cột của DuAn. */
public class DuAn {
    private final String maDuAn;
    private final String tenDuAn;
    private final String mucTieu;
    private final LocalDate ngayBatDau;
    private final LocalDate hanHoanThanh;

    public DuAn(String maDuAn, String tenDuAn, String mucTieu, LocalDate ngayBatDau, LocalDate hanHoanThanh) {
        this.maDuAn = maDuAn;
        this.tenDuAn = tenDuAn;
        this.mucTieu = mucTieu;
        this.ngayBatDau = ngayBatDau;
        this.hanHoanThanh = hanHoanThanh;
    }

    public String getMaDuAn() {
        return maDuAn;
    }

    public String getTenDuAn() {
        return tenDuAn;
    }

    public String getMucTieu() {
        return mucTieu;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public LocalDate getHanHoanThanh() {
        return hanHoanThanh;
    }
}
