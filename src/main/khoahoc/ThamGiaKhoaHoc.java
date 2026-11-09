package khoahoc;

public class ThamGiaKhoaHoc {

    private final String msv;
    private final String maKhoaHoc;
    private final String vaiTro;
    private final String ketQuaDanhGia;

    public ThamGiaKhoaHoc(
            String msv,
            String maKhoaHoc,
            String vaiTro,
            String ketQuaDanhGia) {

        this.msv = msv;
        this.maKhoaHoc = maKhoaHoc;
        this.vaiTro = vaiTro;
        this.ketQuaDanhGia = ketQuaDanhGia;
    }

    public String getMsv() {

        return msv;
    }

    public String getMaKhoaHoc() {

        return maKhoaHoc;
    }

    public String getVaiTro() {

        return vaiTro;
    }

    public String getKetQuaDanhGia() {

        return ketQuaDanhGia;
    }
}
