package quyenhan;

import common.DataAccessException;
import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Truy cập MySQL qua các thủ tục của module; không ghép dữ liệu nhập vào SQL. */
public class QuyenHanDao {

    public void them(QuyenHan quyen) {
        execute("CALL sp_ThemQuyenHan(?, ?, ?)", quyen.maQuyenHan(), quyen.tenQuyenHan(), quyen.moTaChiTiet());
    }

    public void sua(QuyenHan quyen) {
        execute("CALL sp_SuaQuyenHan(?, ?, ?)", quyen.maQuyenHan(), quyen.tenQuyenHan(), quyen.moTaChiTiet());
    }

    public void xoa(String maQuyenHan) {
        execute("CALL sp_XoaQuyenHan(?)", maQuyenHan);
    }

    public List<QuyenHan> danhSach() {
        return query("SELECT MaQuyenHan, TenQuyenHan, MoTaChiTiet FROM QuyenHan ORDER BY MaQuyenHan", this::mapQuyen);
    }

    public void capQuyen(String msv, String maQuyenHan) {
        execute("CALL sp_CapQuyen(?, ?)", msv, maQuyenHan);
    }

    public void thuHoiQuyen(String msv, String maQuyenHan) {
        execute("CALL sp_ThuHoiQuyen(?, ?)", msv, maQuyenHan);
    }

    public List<QuyenHan> quyenCuaThanhVien(String msv) {
        return query("CALL sp_QuyenCuaThanhVien(?)", this::mapQuyen, msv);
    }

    public List<ThanhVienCoQuyen> thanhVienCoQuyen(String maQuyenHan) {
        return query("CALL sp_ThanhVienCoQuyen(?)",
                rs -> new ThanhVienCoQuyen(rs.getString("MSV"), rs.getString("HoVaTen"),
                        rs.getString("MaBan"), rs.getString("ChucVu")), maQuyenHan);
    }

    private QuyenHan mapQuyen(ResultSet rs) throws SQLException {
        return new QuyenHan(rs.getString("MaQuyenHan"), rs.getString("TenQuyenHan"), rs.getString("MoTaChiTiet"));
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
