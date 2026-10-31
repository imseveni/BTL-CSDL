package duan;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import common.ConnectionProvider;
import common.JdbcValues;

public class DuAnDao {
    private final ConnectionProvider connections;

    public DuAnDao(ConnectionProvider connections) {
        this.connections = connections;
    }

    public List<DuAn> search(String keyword) throws SQLException {
        String sql = "SELECT MaDuAn, TenDuAn, MucTieu, NgayBatDau, HanHoanThanh FROM DuAn "
                + "WHERE MaDuAn LIKE ? ESCAPE '!' OR TenDuAn LIKE ? ESCAPE '!' "
                + "OR MucTieu LIKE ? ESCAPE '!' ORDER BY MaDuAn";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = JdbcValues.contains(keyword);
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            try (ResultSet result = statement.executeQuery()) {
                List<DuAn> rows = new ArrayList<>();
                while (result.next()) {
                    rows.add(read(result));
                }
                return rows;
            }
        }
    }

    public Optional<DuAn> findById(String id) throws SQLException {
        String sql = "SELECT MaDuAn, TenDuAn, MucTieu, NgayBatDau, HanHoanThanh FROM DuAn WHERE MaDuAn = ?";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(read(result)) : Optional.empty();
            }
        }
    }

    public void insert(DuAn row) throws SQLException {
        String sql = "INSERT INTO DuAn (MaDuAn, TenDuAn, MucTieu, NgayBatDau, HanHoanThanh) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, row.getMaDuAn());
            statement.setString(2, row.getTenDuAn());
            statement.setString(3, row.getMucTieu());
            JdbcValues.date(statement, 4, row.getNgayBatDau());
            JdbcValues.date(statement, 5, row.getHanHoanThanh());
            statement.executeUpdate();
        }
    }

    public boolean update(DuAn row) throws SQLException {
        String sql = "UPDATE DuAn SET TenDuAn = ?, MucTieu = ?, NgayBatDau = ?, HanHoanThanh = ? WHERE MaDuAn = ?";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, row.getTenDuAn());
            statement.setString(2, row.getMucTieu());
            JdbcValues.date(statement, 3, row.getNgayBatDau());
            JdbcValues.date(statement, 4, row.getHanHoanThanh());
            statement.setString(5, row.getMaDuAn());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM DuAn WHERE MaDuAn = ? AND NOT EXISTS "
                + "(SELECT 1 FROM ThamGiaDuAn WHERE MaDuAn = ?)";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.setString(2, id);
            return statement.executeUpdate() > 0;
        }
    }

    public List<ThamGiaDuAn> participants(String id) throws SQLException {
        String sql = "SELECT MSV, MaDuAn, VaiTro, TienDoRieng FROM ThamGiaDuAn WHERE MaDuAn = ? ORDER BY MSV";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                List<ThamGiaDuAn> rows = new ArrayList<>();
                while (result.next()) {
                    rows.add(new ThamGiaDuAn(result.getString("MSV"), result.getString("MaDuAn"),
                            result.getString("VaiTro"), result.getBigDecimal("TienDoRieng")));
                }
                return rows;
            }
        }
    }

    public void addParticipant(ThamGiaDuAn row) throws SQLException {
        String sql = "INSERT INTO ThamGiaDuAn (MSV, MaDuAn, VaiTro, TienDoRieng) VALUES (?, ?, ?, ?)";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, row.getMsv());
            statement.setString(2, row.getMaDuAn());
            statement.setString(3, row.getVaiTro());
            progress(statement, 4, row.getTienDoRieng());
            statement.executeUpdate();
        }
    }

    public boolean updateParticipant(ThamGiaDuAn row) throws SQLException {
        String sql = "UPDATE ThamGiaDuAn SET VaiTro = ?, TienDoRieng = ? WHERE MSV = ? AND MaDuAn = ?";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, row.getVaiTro());
            progress(statement, 2, row.getTienDoRieng());
            statement.setString(3, row.getMsv());
            statement.setString(4, row.getMaDuAn());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean removeParticipant(String id, String msv) throws SQLException {
        String sql = "DELETE FROM ThamGiaDuAn WHERE MaDuAn = ? AND MSV = ?";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.setString(2, msv);
            return statement.executeUpdate() > 0;
        }
    }

    /** AVG bỏ qua NULL; chưa có tiến độ thì trả 0, thống nhất với SQL module 05. */
    public BigDecimal averageProgress(String id) throws SQLException {
        String sql = "SELECT COALESCE(AVG(TienDoRieng), 0) AS TienDoChung FROM ThamGiaDuAn WHERE MaDuAn = ?";
        try (Connection connection = connections.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getBigDecimal("TienDoChung");
            }
        }
    }

    private void progress(PreparedStatement statement, int index, BigDecimal value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DECIMAL);
        } else {
            statement.setBigDecimal(index, value);
        }
    }

    private DuAn read(ResultSet result) throws SQLException {
        return new DuAn(result.getString("MaDuAn"), result.getString("TenDuAn"), result.getString("MucTieu"),
                JdbcValues.date(result, "NgayBatDau"), JdbcValues.date(result, "HanHoanThanh"));
    }
}
