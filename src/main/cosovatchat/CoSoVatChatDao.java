package cosovatchat;

import common.DataAccessException;
import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Truy cập MySQL qua view/thủ tục/hàm của module 09; không ghép dữ liệu nhập vào SQL. */
public class CoSoVatChatDao {

    private static final RowMapper<CoSoVatChat> THIET_BI = rs -> new CoSoVatChat(
            rs.getString("MaThietBi"), rs.getString("TenThietBi"), rs.getInt("TongSoLuong"),
            rs.getInt("SoLuongKhaDung"), rs.getString("MSVQuanLy"), rs.getString("TenNguoiQuanLy"));

    public List<CoSoVatChat> danhSach() {
        return query("SELECT * FROM vw_DanhSachThietBi ORDER BY MaThietBi", THIET_BI);
    }

    /** Tra cứu theo mã hoặc tên; từ khóa trống = tất cả. */
    public List<CoSoVatChat> timKiem(String tuKhoa) {
        return query("CALL sp_TimThietBi(?)", THIET_BI, tuKhoa);
    }

    /** Trả về null nếu không có thiết bị mang mã này. */
    public CoSoVatChat timTheoMa(String maThietBi) {
        List<CoSoVatChat> rows = query("SELECT * FROM vw_DanhSachThietBi WHERE MaThietBi = ?", THIET_BI, maThietBi);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** soLuongKhaDung null -> bằng tổng; msvQuanLy null -> chưa có người quản lý. */
    public void themMoi(String ma, String ten, int tong, Integer soLuongKhaDung, String msvQuanLy) {
        execute("CALL sp_ThemThietBi(?, ?, ?, ?, ?)", ma, ten, tong, soLuongKhaDung, msvQuanLy);
    }

    /** Sửa tên và tổng số lượng; khả dụng được thủ tục điều chỉnh theo số đang mượn. */
    public void capNhat(String ma, String ten, int tong) {
        execute("CALL sp_CapNhatThietBi(?, ?, ?)", ma, ten, tong);
    }

    /** msv null -> bỏ người quản lý. */
    public void ganNguoiQuanLy(String ma, String msv) {
        execute("CALL sp_GanNguoiQuanLy(?, ?)", ma, msv);
    }

    /** Gọi hàm fn_KiemTraKhaDung: true nếu còn đủ số lượng để cho mượn. */
    public boolean kiemTraKhaDung(String ma, int soLuong) {
        List<Boolean> rows = query("SELECT fn_KiemTraKhaDung(?, ?) AS DuDieuKien",
                rs -> rs.getInt("DuDieuKien") == 1, ma, soLuong);
        return !rows.isEmpty() && rows.get(0);
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
            if (parameters[i] == null) {
                ps.setNull(i + 1, Types.NULL);
            } else {
                ps.setObject(i + 1, parameters[i]);
            }
        }
    }

    private DataAccessException databaseError(SQLException e) {
        String message;
        if ("45000".equals(e.getSQLState())) {
            message = e.getMessage();
        } else {
            message = switch (e.getErrorCode()) {
                case 1062 -> "Mã thiết bị đã tồn tại.";
                case 1451 -> "Không thể xóa vì còn dữ liệu đang tham chiếu.";
                case 1452 -> "Thành viên hoặc thiết bị được tham chiếu không tồn tại.";
                case 3819 -> "Số lượng vi phạm ràng buộc kiểm tra của cơ sở dữ liệu.";
                default -> "Không thể truy cập cơ sở dữ liệu. Kiểm tra kết nối và chạy sql/run_all.sql trước.";
            };
        }
        return new DataAccessException(message, e);
    }
}
