package danhgia;

import java.util.List;
import java.util.Objects;

/** Kiểm tra dữ liệu nhập; ràng buộc tham chiếu do MySQL đảm bảo. */
public class DanhGiaService {
    private final DanhGiaDao dao;

    public DanhGiaService() {
        this(new DanhGiaDao());
    }

    public DanhGiaService(DanhGiaDao dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    public void them(DanhGia dg) {
        dao.them(validate(dg));
    }

    public void sua(DanhGia dg) {
        dao.sua(validate(dg));
    }

    public List<DanhGia> lichSu(String msv) {
        return dao.lichSu(required(msv, "MSV", 20));
    }

    public List<ThongKeDanhGia> thongKe() {
        return dao.thongKe();
    }

    private DanhGia validate(DanhGia dg) {
        if (dg == null || dg.ngayQuyetDinh() == null) {
            throw new IllegalArgumentException("Ngày quyết định không được để trống.");
        }
        if (dg.ngayQuyetDinh().getYear() < 1000 || dg.ngayQuyetDinh().getYear() > 9999) {
            throw new IllegalArgumentException("Ngày quyết định phải trong khoảng 1000-01-01 đến 9999-12-31.");
        }
        String lyDo = required(dg.lyDo(), "Lý do", 65535);
        if (lyDo.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 65535) {
            throw new IllegalArgumentException("Lý do vượt dung lượng TEXT (65535 byte UTF-8).");
        }
        return new DanhGia(required(dg.maGhiNhan(), "Mã ghi nhận", 20),
                required(dg.loaiDanhGia(), "Loại đánh giá", 100), required(dg.hinhThuc(), "Hình thức", 100),
                lyDo, dg.ngayQuyetDinh(), required(dg.msv(), "MSV", 20));
    }


    private String required(String value, String label, int max) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " không được để trống.");
        }
        String result = value.strip();
        if (result.codePointCount(0, result.length()) > max) {
            throw new IllegalArgumentException(label + " tối đa " + max + " ký tự.");
        }
        return result;
    }

}
