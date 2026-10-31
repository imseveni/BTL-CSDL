package quyenhan;

import java.util.List;
import java.util.Objects;

/** Kiểm tra dữ liệu nhập; ràng buộc tham chiếu do MySQL đảm bảo. */
public class QuyenHanService {
    private final QuyenHanDao dao;

    public QuyenHanService() {
        this(new QuyenHanDao());
    }

    public QuyenHanService(QuyenHanDao dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    public void them(QuyenHan quyen) {
        dao.them(validate(quyen));
    }

    public void sua(QuyenHan quyen) {
        dao.sua(validate(quyen));
    }

    public void xoa(String maQuyenHan) {
        dao.xoa(required(maQuyenHan, "Mã quyền hạn", 20));
    }

    public List<QuyenHan> danhSach() {
        return dao.danhSach();
    }

    public void capQuyen(String msv, String maQuyenHan) {
        dao.capQuyen(required(msv, "MSV", 20), required(maQuyenHan, "Mã quyền hạn", 20));
    }

    public void thuHoiQuyen(String msv, String maQuyenHan) {
        dao.thuHoiQuyen(required(msv, "MSV", 20), required(maQuyenHan, "Mã quyền hạn", 20));
    }

    public List<QuyenHan> quyenCuaThanhVien(String msv) {
        return dao.quyenCuaThanhVien(required(msv, "MSV", 20));
    }

    public List<ThanhVienCoQuyen> thanhVienCoQuyen(String maQuyenHan) {
        return dao.thanhVienCoQuyen(required(maQuyenHan, "Mã quyền hạn", 20));
    }

    private QuyenHan validate(QuyenHan quyen) {
        if (quyen == null) {
            throw new IllegalArgumentException("Thông tin quyền hạn không được trống.");
        }
        String moTa = quyen.moTaChiTiet() == null || quyen.moTaChiTiet().isBlank()
                ? null : quyen.moTaChiTiet().strip();
        if (moTa != null && moTa.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 65535) {
            throw new IllegalArgumentException("Mô tả vượt dung lượng TEXT (65535 byte UTF-8).");
        }
        return new QuyenHan(required(quyen.maQuyenHan(), "Mã quyền hạn", 20),
                required(quyen.tenQuyenHan(), "Tên quyền hạn", 100), moTa);
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
