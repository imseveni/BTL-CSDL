package khoahoc;

import java.sql.SQLException;
import java.util.List;
import common.Validation;

public class KhoaHocService {
    private final KhoaHocDao dao;

    public KhoaHocService(KhoaHocDao dao) {
        this.dao = dao;
    }

    public List<KhoaHoc> search(String keyword) throws SQLException {
        return dao.search(keyword == null ? "" : keyword.trim());
    }

    public KhoaHoc get(String id) throws SQLException {
        return dao.findById(Validation.required(id, "Mã khóa học", 20))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khóa học."));
    }

    public void create(KhoaHoc row) throws SQLException {
        dao.insert(validate(row));
    }

    public void update(KhoaHoc row) throws SQLException {
        if (!dao.update(validate(row))) {
            throw new IllegalArgumentException("Không tìm thấy khóa học để cập nhật.");
        }
    }

    public void delete(String id) throws SQLException {
        if (!dao.delete(Validation.required(id, "Mã khóa học", 20))) {
            throw new IllegalArgumentException(
                    "Không tìm thấy khóa học hoặc còn thành viên tham gia. Hãy xóa lượt tham gia trước.");
        }
    }

    public List<ThamGiaKhoaHoc> participants(String id) throws SQLException {
        return dao.participants(get(id).getMaKhoaHoc());
    }

    public void addParticipant(ThamGiaKhoaHoc row) throws SQLException {
        dao.addParticipant(validate(row));
    }

    public void updateParticipant(ThamGiaKhoaHoc row) throws SQLException {
        if (!dao.updateParticipant(validate(row))) {
            throw new IllegalArgumentException("Không tìm thấy lượt tham gia để cập nhật.");
        }
    }

    public void removeParticipant(String id, String msv) throws SQLException {
        if (!dao.removeParticipant(Validation.required(id, "Mã khóa học", 20), Validation.required(msv, "MSV", 20))) {
            throw new IllegalArgumentException("Không tìm thấy lượt tham gia.");
        }
    }

    private KhoaHoc validate(KhoaHoc row) {
        Validation.requiredDate(row.getNgayKhaiGiang(), "Ngày khai giảng");
        Validation.dates(row.getNgayKhaiGiang(), row.getNgayBeGiang());
        return new KhoaHoc(Validation.required(row.getMaKhoaHoc(), "Mã khóa học", 20),
                Validation.required(row.getTenKhoaHoc(), "Tên khóa học", 150), row.getNgayKhaiGiang(),
                row.getNgayBeGiang());
    }

    private ThamGiaKhoaHoc validate(ThamGiaKhoaHoc row) {
        return new ThamGiaKhoaHoc(Validation.required(row.getMsv(), "MSV", 20),
                Validation.required(row.getMaKhoaHoc(), "Mã khóa học", 20),
                Validation.optional(row.getVaiTro(), "Vai trò", 100),
                Validation.optional(row.getKetQuaDanhGia(), "Kết quả đánh giá", 255));
    }
}
