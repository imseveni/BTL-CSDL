package duan;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import common.Validation;

public class DuAnService {
    private final DuAnDao dao;

    public DuAnService(DuAnDao dao) {
        this.dao = dao;
    }

    public List<DuAn> search(String keyword) throws SQLException {
        return dao.search(keyword == null ? "" : keyword.trim());
    }

    public DuAn get(String id) throws SQLException {
        return dao.findById(Validation.required(id, "Mã dự án", 20))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dự án."));
    }

    public void create(DuAn row) throws SQLException {
        dao.insert(validate(row));
    }

    public void update(DuAn row) throws SQLException {
        if (!dao.update(validate(row))) {
            throw new IllegalArgumentException("Không tìm thấy dự án để cập nhật.");
        }
    }

    public void delete(String id) throws SQLException {
        if (!dao.delete(Validation.required(id, "Mã dự án", 20))) {
            throw new IllegalArgumentException(
                    "Không tìm thấy dự án hoặc còn thành viên tham gia. Hãy xóa lượt tham gia trước.");
        }
    }

    public List<ThamGiaDuAn> participants(String id) throws SQLException {
        return dao.participants(get(id).getMaDuAn());
    }

    public void addParticipant(ThamGiaDuAn row) throws SQLException {
        dao.addParticipant(validate(row));
    }

    public void updateParticipant(ThamGiaDuAn row) throws SQLException {
        if (!dao.updateParticipant(validate(row))) {
            throw new IllegalArgumentException("Không tìm thấy lượt tham gia để cập nhật.");
        }
    }

    public void removeParticipant(String id, String msv) throws SQLException {
        if (!dao.removeParticipant(Validation.required(id, "Mã dự án", 20), Validation.required(msv, "MSV", 20))) {
            throw new IllegalArgumentException("Không tìm thấy lượt tham gia.");
        }
    }

    public BigDecimal averageProgress(String id) throws SQLException {
        return dao.averageProgress(get(id).getMaDuAn());
    }

    private DuAn validate(DuAn row) {
        Validation.requiredDate(row.getNgayBatDau(), "Ngày bắt đầu");
        Validation.dates(row.getNgayBatDau(), row.getHanHoanThanh());
        return new DuAn(Validation.required(row.getMaDuAn(), "Mã dự án", 20),
                Validation.required(row.getTenDuAn(), "Tên dự án", 150), Validation.optional(row.getMucTieu()),
                row.getNgayBatDau(), row.getHanHoanThanh());
    }

    private ThamGiaDuAn validate(ThamGiaDuAn row) {
        Validation.progress(row.getTienDoRieng());
        return new ThamGiaDuAn(Validation.required(row.getMsv(), "MSV", 20),
                Validation.required(row.getMaDuAn(), "Mã dự án", 20),
                Validation.optional(row.getVaiTro(), "Vai trò", 100), row.getTienDoRieng());
    }
}
