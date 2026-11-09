package khoahoc;

import java.time.LocalDate;

public class KhoaHoc {

    private final String maKhoaHoc;
    private final String tenKhoaHoc;
    private final LocalDate ngayKhaiGiang;
    private final LocalDate ngayBeGiang;

    public KhoaHoc(
            String maKhoaHoc,
            String tenKhoaHoc,
            LocalDate ngayKhaiGiang,
            LocalDate ngayBeGiang) {

        this.maKhoaHoc = maKhoaHoc;
        this.tenKhoaHoc = tenKhoaHoc;
        this.ngayKhaiGiang = ngayKhaiGiang;
        this.ngayBeGiang = ngayBeGiang;
    }

    public String getMaKhoaHoc() {

        return maKhoaHoc;
    }

    public String getTenKhoaHoc() {

        return tenKhoaHoc;
    }

    public LocalDate getNgayKhaiGiang() {

        return ngayKhaiGiang;
    }

    public LocalDate getNgayBeGiang() {

        return ngayBeGiang;
    }
}
