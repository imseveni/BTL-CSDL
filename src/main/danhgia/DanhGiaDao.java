package danhgia;

import common.DataAccessException;
import config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Truy cập MySQL qua các thủ tục của module; không ghép dữ liệu nhập vào SQL. */
public class DanhGiaDao {

    public void them(DanhGia dg) {
        execute("CALL sp_ThemDanhGia(?, ?, ?, ?, ?, ?)", dg.maGhiNhan(), dg.loaiDanhGia(),
                dg.hinhThuc(), dg.lyDo(), java.sql.Date.valueOf(dg.ngayQuyetDinh()), dg.msv());
    }

    public void sua(DanhGia dg) {
        execute("CALL sp_SuaDanhGia(?, ?, ?, ?, ?, ?)", dg.maGhiNhan(), dg.loaiDanhGia(),
                dg.hinhThuc(), dg.lyDo(), java.sql.Date.valueOf(dg.ngayQuyetDinh()), dg.msv());
    }

    public List<DanhGia> lichSu(String msv) {
        return query("CALL sp_LichSuDanhGia(?)",
                rs -> new DanhGia(rs.getString("MaGhiNhan"), rs.getString("LoaiDanhGia"),
                        rs.getString("HinhThuc"), rs.getString("LyDo"),
                        rs.getDate("NgayQuyetDinh").toLocalDate(), rs.getString("MSV")), msv);
    }

    public List<ThongKeDanhGia> thongKe() {
        return query("SELECT LoaiDanhGia, SoDanhGia, SoThanhVien FROM vw_ThongKeDanhGia ORDER BY LoaiDanhGia",
                rs -> new ThongKeDanhGia(rs.getString("LoaiDanhGia"), rs.getLong("SoDanhGia"), rs.getLong("SoThanhVien")));
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
