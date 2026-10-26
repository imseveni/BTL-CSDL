package banchuyenmon;

import java.util.List;
import java.util.Objects;

/** Kiểm tra dữ liệu nhập; ràng buộc tham chiếu do MySQL đảm bảo. */
public class BanChuyenMonService {
    private final BanChuyenMonDao dao;

    public BanChuyenMonService() {
        this(new BanChuyenMonDao());
    }

    public BanChuyenMonService(BanChuyenMonDao dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    public void them(BanChuyenMon ban) {
        dao.them(validate(ban));
    }

    public void sua(BanChuyenMon ban) {
        dao.sua(validate(ban));
    }

    public void xoa(String maBan) {
        dao.xoa(required(maBan, "Mã ban", 20));
    }

    public List<BanThongKe> danhSach() {
        return dao.danhSach();
    }

    public List<ThanhVienTrongBan> thanhVienTheoBan(String maBan) {
        return dao.thanhVienTheoBan(required(maBan, "Mã ban", 20));
    }

    public void chuyenBan(String msv, String maBan) {
        dao.chuyenBan(required(msv, "MSV", 20), required(maBan, "Mã ban", 20));
    }

    public void doiChucVu(String msv, String chucVu) {
        dao.doiChucVu(required(msv, "MSV", 20), required(chucVu, "Chức vụ", 100));
    }

    private BanChuyenMon validate(BanChuyenMon ban) {
        if (ban == null) {
            throw new IllegalArgumentException("Thông tin ban không được trống.");
        }
        return new BanChuyenMon(required(ban.maBan(), "Mã ban", 20), required(ban.tenBan(), "Tên ban", 100));
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
