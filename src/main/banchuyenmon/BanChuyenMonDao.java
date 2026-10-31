package banchuyenmon;

import common.DataAccessException;
import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Truy cập MySQL qua các thủ tục của module; không ghép dữ liệu nhập vào SQL. */
public class BanChuyenMonDao {

    public void them(BanChuyenMon ban) {
        execute("CALL sp_ThemBan(?, ?)", ban.maBan(), ban.tenBan());
    }

    public void sua(BanChuyenMon ban) {
        execute("CALL sp_SuaBan(?, ?)", ban.maBan(), ban.tenBan());
    }

    public void xoa(String maBan) {
        execute("CALL sp_XoaBan(?)", maBan);
    }

    public List<BanThongKe> danhSach() {
        return query("SELECT MaBan, TenBan, SoThanhVien FROM vw_BanChuyenMonThongKe ORDER BY MaBan",
                rs -> new BanThongKe(rs.getString("MaBan"), rs.getString("TenBan"), rs.getLong("SoThanhVien")));
    }

    public List<ThanhVienTrongBan> thanhVienTheoBan(String maBan) {
        return query("CALL sp_ThanhVienTheoBan(?)",
                rs -> new ThanhVienTrongBan(rs.getString("MSV"), rs.getString("HoVaTen"),
                        rs.getString("ChucVu"), rs.getString("TrangThaiHD")), maBan);
    }

    public void chuyenBan(String msv, String maBan) {
        execute("CALL sp_ChuyenBan(?, ?)", msv, maBan);
    }

    public void doiChucVu(String msv, String chucVu) {
        execute("CALL sp_DoiChucVu(?, ?)", msv, chucVu);
    }


    @FunctionalInterface
    private interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
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
            ps.setObject(i + 1, parameters[i]);
        }
    }

    private DataAccessException databaseError(SQLException e) {
        String message;
        if ("45000".equals(e.getSQLState())) {
            message = e.getMessage();
        } else {
            message = switch (e.getErrorCode()) {
                case 1062 -> "Mã đã tồn tại hoặc thành viên đã có quyền này.";
                case 1451 -> "Không thể xóa vì còn dữ liệu đang tham chiếu.";
                case 1452 -> "Thành viên, ban hoặc quyền được tham chiếu không tồn tại.";
                default -> "Không thể truy cập cơ sở dữ liệu. Kiểm tra kết nối và chạy sql/run_all.sql trước.";
            };
        }
        return new DataAccessException(message, e);
    }

}
