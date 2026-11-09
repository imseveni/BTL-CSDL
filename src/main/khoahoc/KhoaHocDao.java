package khoahoc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import common.ConnectionProvider;
import common.JdbcValues;

// Lớp thực hiện các thao tác database của module khóa học: 
// tìm, thêm, sửa, xóa khóa học và quản lý người tham gia.
public class KhoaHocDao {

    private final ConnectionProvider connections;

    public KhoaHocDao(ConnectionProvider connections) {

        this.connections = connections;
    }

    // Tìm khóa học theo mã hoặc tên
    public List<KhoaHoc> search(String keyword) throws SQLException {

        // SQL của hàm
        String sql = "SELECT MaKhoaHoc, TenKhoaHoc, NgayKhaiGiang, NgayBeGiang FROM KhoaHoc "
                + "WHERE MaKhoaHoc LIKE ? ESCAPE '!' "
                + "OR TenKhoaHoc LIKE ? ESCAPE '!' "
                + "ORDER BY MaKhoaHoc";

        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            String pattern = JdbcValues.contains(keyword);

            statement.setString(1, pattern);
            statement.setString(2, pattern);

            try (ResultSet result = statement.executeQuery()) {

                List<KhoaHoc> rows = new ArrayList<>();

                while (result.next()) {

                    rows.add(read(result));
                }

                return rows;
            }
        }
    }

    public Optional<KhoaHoc> findById(String id) throws SQLException {
        String sql = "SELECT MaKhoaHoc, TenKhoaHoc, NgayKhaiGiang, NgayBeGiang FROM KhoaHoc WHERE MaKhoaHoc = ?";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(read(result)) : Optional.empty();
            }
        }
    }

    public void insert(KhoaHoc row) throws SQLException {
        String sql = "INSERT INTO KhoaHoc (MaKhoaHoc, TenKhoaHoc, NgayKhaiGiang, NgayBeGiang) VALUES (?, ?, ?, ?)";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, row.getMaKhoaHoc());
            statement.setString(2, row.getTenKhoaHoc());
            JdbcValues.date(statement, 3, row.getNgayKhaiGiang());
            JdbcValues.date(statement, 4, row.getNgayBeGiang());
            statement.executeUpdate();
        }
    }

    public boolean update(KhoaHoc row) throws SQLException {
        String sql = "UPDATE KhoaHoc SET TenKhoaHoc = ?, NgayKhaiGiang = ?, NgayBeGiang = ? WHERE MaKhoaHoc = ?";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, row.getTenKhoaHoc());
            JdbcValues.date(statement, 2, row.getNgayKhaiGiang());
            JdbcValues.date(statement, 3, row.getNgayBeGiang());
            statement.setString(4, row.getMaKhoaHoc());
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Chỉ xóa khóa học chưa có người tham gia, kể cả khi FK gốc có ON DELETE
     * CASCADE.
     */
    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM KhoaHoc WHERE MaKhoaHoc = ? AND NOT EXISTS "
                + "(SELECT 1 FROM ThamGiaKhoaHoc WHERE MaKhoaHoc = ?)";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.setString(2, id);
            return statement.executeUpdate() > 0;
        }
    }

    public List<ThamGiaKhoaHoc> participants(String id) throws SQLException {
        String sql = "SELECT MSV, MaKhoaHoc, VaiTro, KetQuaDanhGia FROM ThamGiaKhoaHoc WHERE MaKhoaHoc = ? ORDER BY MSV";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                List<ThamGiaKhoaHoc> rows = new ArrayList<>();
                while (result.next()) {
                    rows.add(new ThamGiaKhoaHoc(result.getString("MSV"), result.getString("MaKhoaHoc"),
                            result.getString("VaiTro"), result.getString("KetQuaDanhGia")));
                }
                return rows;
            }
        }
    }

    public void addParticipant(ThamGiaKhoaHoc row) throws SQLException {
        String sql = "INSERT INTO ThamGiaKhoaHoc (MSV, MaKhoaHoc, VaiTro, KetQuaDanhGia) VALUES (?, ?, ?, ?)";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, row.getMsv());
            statement.setString(2, row.getMaKhoaHoc());
            statement.setString(3, row.getVaiTro());
            statement.setString(4, row.getKetQuaDanhGia());
            statement.executeUpdate();
        }
    }

    public boolean updateParticipant(ThamGiaKhoaHoc row) throws SQLException {
        String sql = "UPDATE ThamGiaKhoaHoc SET VaiTro = ?, KetQuaDanhGia = ? WHERE MSV = ? AND MaKhoaHoc = ?";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, row.getVaiTro());
            statement.setString(2, row.getKetQuaDanhGia());
            statement.setString(3, row.getMsv());
            statement.setString(4, row.getMaKhoaHoc());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean removeParticipant(String id, String msv) throws SQLException {
        String sql = "DELETE FROM ThamGiaKhoaHoc WHERE MaKhoaHoc = ? AND MSV = ?";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.setString(2, msv);
            return statement.executeUpdate() > 0;
        }
    }

    private KhoaHoc read(ResultSet result) throws SQLException {
        return new KhoaHoc(result.getString("MaKhoaHoc"), result.getString("TenKhoaHoc"),
                JdbcValues.date(result, "NgayKhaiGiang"), JdbcValues.date(result, "NgayBeGiang"));
    }
}
