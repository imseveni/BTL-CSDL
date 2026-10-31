package muontra;

import common.DataAccessException;
import config.DBConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy cập MySQL qua thủ tục/view của module 10; không ghép dữ liệu nhập vào SQL.
 * Quy tắc mượn/trả (đủ số lượng, trừ/cộng khả dụng, ngày trả hợp lệ) nằm trong trigger và thủ tục.
 */
public class PhieuMuonDao {

    private static final RowMapper<PhieuMuon> PHIEU = rs -> new PhieuMuon(
            rs.getString("MaPhieuMuon"), ngay(rs, "NgayMuon"), ngay(rs, "NgayHenTra"),
            rs.getString("MucDichMuon"), rs.getString("TrangThaiDuyet"), rs.getString("MSVNguoiMuon"));

    private static final RowMapper<DongTheoDoi> THEO_DOI = rs -> new DongTheoDoi(
            rs.getString("MaPhieuMuon"), rs.getString("MSVNguoiMuon"), rs.getString("HoVaTen"),
            rs.getString("MaThietBi"), rs.getString("TenThietBi"), rs.getInt("SoLuongMuon"),
            ngay(rs, "NgayMuon"), ngay(rs, "NgayHenTra"), ngay(rs, "NgayTraThucTe"),
            rs.getInt("SoNgayTre"), rs.getString("TinhTrang"));

    public void taoPhieu(String ma, LocalDate ngayMuon, LocalDate ngayHenTra, String mucDich, String msv) {
        execute("CALL sp_TaoPhieuMuon(?, ?, ?, ?, ?)", ma, ngayMuon, ngayHenTra, mucDich, msv);
    }

    public void themChiTiet(String maPhieu, String maThietBi, int soLuong) {
        execute("CALL sp_ThemChiTietMuon(?, ?, ?)", maPhieu, maThietBi, soLuong);
    }

    public void duyet(String maPhieu) {
        execute("CALL sp_DuyetPhieuMuon(?)", maPhieu);
    }

    public void tuChoi(String maPhieu) {
        execute("CALL sp_TuChoiPhieuMuon(?)", maPhieu);
    }

    /** ngayTra null -> thủ tục lấy ngày hôm nay. */
    public void traThietBi(String maPhieu, String maThietBi, LocalDate ngayTra) {
        execute("CALL sp_TraThietBi(?, ?, ?)", maPhieu, maThietBi, ngayTra);
    }

    public void traTatCa(String maPhieu, LocalDate ngayTra) {
        execute("CALL sp_TraTatCaThietBi(?, ?)", maPhieu, ngayTra);
    }

    /** Trả về null nếu không có phiếu mang mã này. */
    public PhieuMuon timPhieu(String maPhieu) {
        List<PhieuMuon> rows = query("SELECT MaPhieuMuon, NgayMuon, NgayHenTra, MucDichMuon, TrangThaiDuyet, "
                + "MSVNguoiMuon FROM PhieuMuon WHERE MaPhieuMuon = ?", PHIEU, maPhieu);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** trangThai null -> tất cả phiếu. */
    public List<PhieuMuon> danhSachPhieu(String trangThai) {
        return query("SELECT MaPhieuMuon, NgayMuon, NgayHenTra, MucDichMuon, TrangThaiDuyet, MSVNguoiMuon "
                + "FROM PhieuMuon WHERE (? IS NULL OR TrangThaiDuyet = ?) ORDER BY NgayMuon DESC, MaPhieuMuon",
                PHIEU, trangThai, trangThai);
    }

    public List<ChiTietMuon> chiTietCuaPhieu(String maPhieu) {
        return query("SELECT c.MaPhieuMuon, c.MaThietBi, t.TenThietBi, c.SoLuongMuon, c.NgayTraThucTe "
                + "FROM ChiTietMuon c JOIN CoSoVatChat t ON t.MaThietBi = c.MaThietBi "
                + "WHERE c.MaPhieuMuon = ? ORDER BY c.MaThietBi",
                rs -> new ChiTietMuon(rs.getString("MaPhieuMuon"), rs.getString("MaThietBi"),
                        rs.getString("TenThietBi"), rs.getInt("SoLuongMuon"), ngay(rs, "NgayTraThucTe")),
                maPhieu);
    }

    public List<DongTheoDoi> thietBiChuaTra() {
        return query("SELECT * FROM vw_ThietBiChuaTra ORDER BY SoNgayTre DESC, MaPhieuMuon, MaThietBi", THEO_DOI);
    }

    public List<DongTheoDoi> thietBiTraMuon() {
        return query("SELECT * FROM vw_ThietBiTraMuon ORDER BY SoNgayTre DESC, MaPhieuMuon, MaThietBi", THEO_DOI);
    }

    public List<LichSuMuon> lichSuMuon(String msv) {
        return query("CALL sp_LichSuMuon(?)", rs -> new LichSuMuon(
                rs.getString("MaPhieuMuon"), ngay(rs, "NgayMuon"), ngay(rs, "NgayHenTra"),
                rs.getString("MucDichMuon"), rs.getString("TrangThaiDuyet"), rs.getString("MaThietBi"),
                rs.getString("TenThietBi"), rs.getInt("SoLuongMuon"), ngay(rs, "NgayTraThucTe")), msv);
    }

    @FunctionalInterface
    private interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private static LocalDate ngay(ResultSet rs, String column) throws SQLException {
        Date value = rs.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    private Connection openConnection() throws SQLException {
        try {
            return DBConnection.getConnection();
        } catch (LinkageError e) {
            throw new DataAccessException("Chưa đọc được cấu hình DB. Kiểm tra db.properties trên classpath.", e);
        }
    }

    private <T> List<T> query(String sql, RowMapper<T> mapper, Object... parameters) {
        try (Connection c = openConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, parameters);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    private void execute(String sql, Object... parameters) {
        try (Connection c = openConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, parameters);
            ps.execute();
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    private void bind(PreparedStatement ps, Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            Object value = parameters[i];
            if (value == null) {
                ps.setNull(i + 1, Types.NULL);
            } else if (value instanceof LocalDate date) {
                ps.setDate(i + 1, Date.valueOf(date));
            } else {
                ps.setObject(i + 1, value);
            }
        }
    }

    private DataAccessException databaseError(SQLException e) {
        String message;
        if ("45000".equals(e.getSQLState())) {
            message = e.getMessage();
        } else {
            message = switch (e.getErrorCode()) {
                case 1062 -> "Mã phiếu mượn đã tồn tại.";
                case 1451 -> "Không thể xóa vì còn dữ liệu đang tham chiếu.";
                case 1452 -> "Thành viên, phiếu hoặc thiết bị được tham chiếu không tồn tại.";
                case 3819 -> "Dữ liệu vi phạm ràng buộc kiểm tra của cơ sở dữ liệu.";
                default -> "Không thể truy cập cơ sở dữ liệu. Kiểm tra kết nối và chạy sql/run_all.sql trước.";
            };
        }
        return new DataAccessException(message, e);
    }
}
