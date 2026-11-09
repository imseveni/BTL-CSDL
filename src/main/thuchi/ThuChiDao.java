package thuchi;

import common.DataAccessException;
import config.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Tầng truy cập dữ liệu (DAO) cho Module 07 – Quản lý Thu – Chi.
 * Sử dụng Transaction khi ghi đồng thời bảng ThuChi và ThuChi_PhuongThucThanhToan.
 */
public class ThuChiDao {

    private static final String BASE_SELECT =
            "SELECT tc.MaGD, tc.LoaiGD, tc.MucDich, tc.SoTien, tc.ThoiGian, tc.MinhChung, "
            + "tc.MSV, tv.HoVaTen AS HoTenThanhVien, tc.MaSK, sk.TenSK "
            + "FROM ThuChi tc "
            + "LEFT JOIN ThanhVien tv ON tc.MSV = tv.MSV "
            + "LEFT JOIN SuKien sk ON tc.MaSK = sk.MaSK ";

    public List<ThuChi> layTatCa() {
        String sql = BASE_SELECT + "ORDER BY tc.ThoiGian DESC, tc.MaGD ASC";
        List<ThuChi> danhSach = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ThuChi gd = mapThuChi(rs);
                gd.setPhuongThucThanhToan(layPhuongThucThanhToan(c, gd.getMaGD()));
                danhSach.add(gd);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi lấy danh sách giao dịch thu – chi", e);
        }
        return danhSach;
    }

    public ThuChi timTheoMa(String maGD) {
        String sql = BASE_SELECT + "WHERE tc.MaGD = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maGD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ThuChi gd = mapThuChi(rs);
                    gd.setPhuongThucThanhToan(layPhuongThucThanhToan(c, gd.getMaGD()));
                    return gd;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi tìm giao dịch theo mã: " + maGD, e);
        }
        return null;
    }

    public List<ThuChi> traCuuTheoThanhVien(String msv) {
        String sql = BASE_SELECT + "WHERE tc.MSV = ? ORDER BY tc.ThoiGian DESC, tc.MaGD ASC";
        List<ThuChi> ketQua = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, msv);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThuChi gd = mapThuChi(rs);
                    gd.setPhuongThucThanhToan(layPhuongThucThanhToan(c, gd.getMaGD()));
                    ketQua.add(gd);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi tra cứu giao dịch theo thành viên: " + msv, e);
        }
        return ketQua;
    }

    public List<ThuChi> traCuuTheoSuKien(String maSK) {
        String sql = BASE_SELECT + "WHERE tc.MaSK = ? ORDER BY tc.ThoiGian DESC, tc.MaGD ASC";
        List<ThuChi> ketQua = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maSK);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThuChi gd = mapThuChi(rs);
                    gd.setPhuongThucThanhToan(layPhuongThucThanhToan(c, gd.getMaGD()));
                    ketQua.add(gd);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi tra cứu giao dịch theo sự kiện: " + maSK, e);
        }
        return ketQua;
    }

    public List<ThuChi> traCuuTheoLoai(String loaiGD) {
        String sql = BASE_SELECT + "WHERE tc.LoaiGD = ? ORDER BY tc.ThoiGian DESC, tc.MaGD ASC";
        List<ThuChi> ketQua = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, loaiGD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThuChi gd = mapThuChi(rs);
                    gd.setPhuongThucThanhToan(layPhuongThucThanhToan(c, gd.getMaGD()));
                    ketQua.add(gd);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi lọc giao dịch theo loại: " + loaiGD, e);
        }
        return ketQua;
    }

    /**
     * Thêm giao dịch mới và danh sách phương thức thanh toán trong cùng một Transaction.
     */
    public void them(ThuChi gd) {
        String sqlInsertThuChi = "INSERT INTO ThuChi "
                + "(MaGD, LoaiGD, MucDich, SoTien, ThoiGian, MinhChung, MSV, MaSK) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlInsertPTTT = "INSERT INTO ThuChi_PhuongThucThanhToan "
                + "(MaGD, PhuongThucThanhToan) VALUES (?, ?)";

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement psTC = c.prepareStatement(sqlInsertThuChi);
                 PreparedStatement psPT = c.prepareStatement(sqlInsertPTTT)) {

                psTC.setString(1, gd.getMaGD());
                psTC.setString(2, gd.getLoaiGD());
                psTC.setString(3, gd.getMucDich());
                psTC.setBigDecimal(4, gd.getSoTien());
                psTC.setTimestamp(5, Timestamp.valueOf(gd.getThoiGian()));
                setNullableString(psTC, 6, gd.getMinhChung());
                setNullableString(psTC, 7, gd.getMsv());
                setNullableString(psTC, 8, gd.getMaSK());
                psTC.executeUpdate();

                for (String pt : gd.getPhuongThucThanhToan()) {
                    psPT.setString(1, gd.getMaGD());
                    psPT.setString(2, pt);
                    psPT.addBatch();
                }
                psPT.executeBatch();

                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi thêm mới giao dịch thu – chi: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin giao dịch và danh sách phương thức thanh toán trong cùng một Transaction.
     */
    public void capNhat(ThuChi gd) {
        String sqlUpdateThuChi = "UPDATE ThuChi "
                + "SET LoaiGD = ?, MucDich = ?, SoTien = ?, ThoiGian = ?, MinhChung = ?, MSV = ?, MaSK = ? "
                + "WHERE MaGD = ?";
        String sqlDeletePTTT = "DELETE FROM ThuChi_PhuongThucThanhToan WHERE MaGD = ?";
        String sqlInsertPTTT = "INSERT INTO ThuChi_PhuongThucThanhToan (MaGD, PhuongThucThanhToan) VALUES (?, ?)";

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement psTC = c.prepareStatement(sqlUpdateThuChi);
                 PreparedStatement psDelPT = c.prepareStatement(sqlDeletePTTT);
                 PreparedStatement psInsPT = c.prepareStatement(sqlInsertPTTT)) {

                psTC.setString(1, gd.getLoaiGD());
                psTC.setString(2, gd.getMucDich());
                psTC.setBigDecimal(3, gd.getSoTien());
                psTC.setTimestamp(4, Timestamp.valueOf(gd.getThoiGian()));
                setNullableString(psTC, 5, gd.getMinhChung());
                setNullableString(psTC, 6, gd.getMsv());
                setNullableString(psTC, 7, gd.getMaSK());
                psTC.setString(8, gd.getMaGD());
                psTC.executeUpdate();

                psDelPT.setString(1, gd.getMaGD());
                psDelPT.executeUpdate();

                for (String pt : gd.getPhuongThucThanhToan()) {
                    psInsPT.setString(1, gd.getMaGD());
                    psInsPT.setString(2, pt);
                    psInsPT.addBatch();
                }
                psInsPT.executeBatch();

                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi cập nhật giao dịch thu – chi: " + e.getMessage(), e);
        }
    }

    public boolean xoa(String maGD) {
        String sql = "DELETE FROM ThuChi WHERE MaGD = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maGD);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi xóa giao dịch thu – chi: " + e.getMessage(), e);
        }
    }

    public List<ThongKeThuChiSuKien> thongKeTheoTungSuKien() {
        String sql = "SELECT "
                + "    sk.MaSK, "
                + "    sk.TenSK, "
                + "    COUNT(tc.MaGD) AS SoGiaoDich, "
                + "    COALESCE(SUM(CASE WHEN tc.LoaiGD = 'Thu' THEN tc.SoTien ELSE 0 END), 0) AS TongThu, "
                + "    COALESCE(SUM(CASE WHEN tc.LoaiGD = 'Chi' THEN tc.SoTien ELSE 0 END), 0) AS TongChi, "
                + "    COALESCE(SUM(CASE WHEN tc.LoaiGD = 'Thu' THEN tc.SoTien ELSE 0 END), 0) "
                + "      - COALESCE(SUM(CASE WHEN tc.LoaiGD = 'Chi' THEN tc.SoTien ELSE 0 END), 0) AS ChenhLech "
                + "FROM SuKien sk "
                + "LEFT JOIN ThuChi tc ON sk.MaSK = tc.MaSK "
                + "GROUP BY sk.MaSK, sk.TenSK "
                + "ORDER BY sk.MaSK ASC";
        List<ThongKeThuChiSuKien> danhSach = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ThongKeThuChiSuKien tk = new ThongKeThuChiSuKien(
                        rs.getString("MaSK"),
                        rs.getString("TenSK"),
                        rs.getInt("SoGiaoDich"),
                        rs.getBigDecimal("TongThu"),
                        rs.getBigDecimal("TongChi"),
                        rs.getBigDecimal("ChenhLech")
                );
                danhSach.add(tk);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi thống kê thu – chi theo từng sự kiện", e);
        }
        return danhSach;
    }

    public boolean tonTaiThanhVien(String msv) {
        String sql = "SELECT 1 FROM ThanhVien WHERE MSV = ? LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, msv);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi kiểm tra mã sinh viên: " + msv, e);
        }
    }

    public boolean tonTaiSuKien(String maSK) {
        String sql = "SELECT 1 FROM SuKien WHERE MaSK = ? LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maSK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi kiểm tra mã sự kiện: " + maSK, e);
        }
    }

    private List<String> layPhuongThucThanhToan(Connection c, String maGD) throws SQLException {
        String sql = "SELECT PhuongThucThanhToan FROM ThuChi_PhuongThucThanhToan "
                   + "WHERE MaGD = ? ORDER BY PhuongThucThanhToan ASC";
        List<String> ds = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maGD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(rs.getString("PhuongThucThanhToan"));
                }
            }
        }
        return ds;
    }

    private ThuChi mapThuChi(ResultSet rs) throws SQLException {
        ThuChi gd = new ThuChi();
        gd.setMaGD(rs.getString("MaGD"));
        gd.setLoaiGD(rs.getString("LoaiGD"));
        gd.setMucDich(rs.getString("MucDich"));
        gd.setSoTien(rs.getBigDecimal("SoTien"));
        Timestamp ts = rs.getTimestamp("ThoiGian");
        if (ts != null) {
            gd.setThoiGian(ts.toLocalDateTime());
        }
        gd.setMinhChung(rs.getString("MinhChung"));
        gd.setMsv(rs.getString("MSV"));
        gd.setHoTenThanhVien(rs.getString("HoTenThanhVien"));
        gd.setMaSK(rs.getString("MaSK"));
        gd.setTenSuKien(rs.getString("TenSK"));
        return gd;
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }
}
