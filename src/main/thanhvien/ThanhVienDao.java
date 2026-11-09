package thanhvien;

import common.ConnectionProvider;
import common.DataAccessException;
import common.Database;
import common.JdbcValues;
import common.SqlErrors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Truy cập MySQL qua các thủ tục và view của module 01; không ghép dữ liệu nhập vào câu SQL. */
public class ThanhVienDao {

    private final ConnectionProvider connections;

    public ThanhVienDao() {
        this(Database::getConnection);
    }

    public ThanhVienDao(ConnectionProvider connections) {
        this.connections = connections;
    }

    public void them(ThanhVien tv) {
        execute("CALL sp_ThemThanhVien(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                tv.msv(), tv.hoVaTen(), tv.ngaySinh(), tv.gioiTinh(), tv.email(), tv.sdt(),
                tv.ngayGiaNhap(), tv.trangThaiHD(), tv.maBan(), tv.chucVu());
    }

    public void capNhat(ThanhVien tv) {
        execute("CALL sp_CapNhatThanhVien(?, ?, ?, ?, ?, ?)",
                tv.msv(), tv.hoVaTen(), tv.ngaySinh(), tv.gioiTinh(), tv.email(), tv.sdt());
    }

    public void doiTrangThai(String msv, String trangThai) {
        execute("CALL sp_DoiTrangThaiThanhVien(?, ?)", msv, trangThai);
    }

    public void xoa(String msv) {
        execute("CALL sp_XoaThanhVien(?)", msv);
    }

    public List<ThanhVien> tim(String tuKhoa, String maBan, String trangThai) {
        return query("CALL sp_TimThanhVien(?, ?, ?)", ThanhVienDao::mapThanhVien, tuKhoa, maBan, trangThai);
    }

    /** Trả về thành viên cần xem, hoặc ném DataAccessException nếu không tồn tại. */
    public ThanhVien chiTiet(String msv) {
        List<ThanhVien> rows = query("CALL sp_ChiTietThanhVien(?)", ThanhVienDao::mapThanhVien, msv);
        if (rows.isEmpty()) {
            throw new DataAccessException("Thành viên không tồn tại", null);
        }
        return rows.get(0);
    }

    public List<ThanhVien> giaNhap(LocalDate tuNgay, LocalDate denNgay) {
        return query("CALL sp_ThanhVienGiaNhap(?, ?)", rs -> new ThanhVien(
                rs.getString("MSV"), rs.getString("HoVaTen"), null, null, null, null,
                JdbcValues.date(rs, "NgayGiaNhap"), rs.getString("TrangThaiHD"),
                rs.getString("MaBan"), rs.getString("TenBan"), rs.getString("ChucVu")), tuNgay, denNgay);
    }

    /** Danh sách ban để chọn khi thêm thành viên hoặc lọc; chỉ đọc bảng BanChuyenMon. */
    public List<BanLuaChon> danhSachBan() {
        return query("SELECT MaBan, TenBan FROM BanChuyenMon ORDER BY MaBan",
                rs -> new BanLuaChon(rs.getString("MaBan"), rs.getString("TenBan")));
    }

    /** Báo cáo 1: danh sách thành viên theo ban (đã sắp theo ban, chức vụ, MSV). Ban và trạng thái tùy chọn. */
    public List<ThanhVien> baoCaoDanhSach(String maBan, String trangThai) {
        return query("CALL sp_BaoCaoDanhSachThanhVien(?, ?)", ThanhVienDao::mapThanhVien, maBan, trangThai);
    }

    /** Báo cáo 2: số hoạt động của từng thành viên trong khoảng ngày [tuNgay, denNgay]. */
    public List<TongHopHoatDong> baoCaoHoatDong(LocalDate tuNgay, LocalDate denNgay) {
        return query("CALL sp_BaoCaoHoatDongThanhVien(?, ?)", rs -> new TongHopHoatDong(
                rs.getString("MSV"), rs.getString("HoVaTen"), rs.getString("TenBan"), rs.getString("ChucVu"),
                rs.getString("TrangThaiHD"), rs.getLong("SoKhoaHoc"), rs.getLong("SoDuAn"), rs.getLong("SoSuKien"),
                rs.getLong("SoDanhGia"), rs.getLong("SoPhieuMuon"), rs.getLong("SoGiaoDich"),
                rs.getLong("TongHoatDong")), tuNgay, denNgay);
    }

    public List<ThongKeTrangThai> thongKeTrangThai() {
        return query("SELECT TrangThaiHD, SoThanhVien FROM vw_ThongKeTrangThaiThanhVien ORDER BY TrangThaiHD",
                rs -> new ThongKeTrangThai(rs.getString("TrangThaiHD"), rs.getLong("SoThanhVien")));
    }

    public List<HoatDong> lichSu(String msv) {
        return query("CALL sp_LichSuHoatDongThanhVien(?)", rs -> new HoatDong(
                rs.getString("LoaiHoatDong"), rs.getString("MaHoatDong"), rs.getString("TenHoatDong"),
                rs.getString("ChiTiet"), JdbcValues.date(rs, "Ngay")), msv);
    }

    public TongQuanThanhVien tongQuan(String msv) {
        List<TongQuanThanhVien> rows = query("CALL sp_TongQuanThanhVien(?)", rs -> new TongQuanThanhVien(
                rs.getString("MSV"), rs.getString("HoVaTen"), rs.getString("TrangThaiHD"),
                rs.getLong("SoKhoaHoc"), rs.getLong("SoDuAn"), rs.getLong("SoSuKien"),
                rs.getLong("SoDanhGia"), rs.getLong("SoPhieuMuon")), msv);
        if (rows.isEmpty()) {
            throw new DataAccessException("Thành viên không tồn tại", null);
        }
        return rows.get(0);
    }

    // ---------------------------------------------------------------------------------------

    @FunctionalInterface
    private interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private static ThanhVien mapThanhVien(ResultSet rs) throws SQLException {
        return new ThanhVien(rs.getString("MSV"), rs.getString("HoVaTen"),
                JdbcValues.date(rs, "NgaySinh"), rs.getString("GioiTinh"), rs.getString("Email"),
                rs.getString("SDT"), JdbcValues.date(rs, "NgayGiaNhap"), rs.getString("TrangThaiHD"),
                rs.getString("MaBan"), rs.getString("TenBan"), rs.getString("ChucVu"));
    }

    private <T> List<T> query(String sql, RowMapper<T> mapper, Object... parameters) {
        try (Connection c = connections.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
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
        try (Connection c = connections.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
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
                ps.setNull(i + 1, Types.VARCHAR);
            } else if (value instanceof LocalDate date) {
                JdbcValues.date(ps, i + 1, date);
            } else {
                ps.setObject(i + 1, value);
            }
        }
    }

    private DataAccessException databaseError(SQLException e) {
        // Thông báo do thủ tục tự đặt (SIGNAL SQLSTATE 45000) đã là tiếng Việt, dùng nguyên văn.
        String message = "45000".equals(e.getSQLState()) ? e.getMessage() : SqlErrors.message(e);
        return new DataAccessException(message, e);
    }
}
