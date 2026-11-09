package sukien;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import common.DataAccessException;
import config.DBConnection;

/**
 * Tầng truy cập dữ liệu (DAO) cho Module 06 – Quản lý Sự kiện.
 */
public class SuKienDao {

    public List<SuKien> layTatCa() {
        String sql = "SELECT MaSK, TenSK, NgayBatDau, NgayKetThuc "
                   + "FROM SuKien ORDER BY NgayBatDau DESC, MaSK ASC";
        List<SuKien> danhSach = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(mapSuKien(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi lấy danh sách sự kiện", e);
        }
        return danhSach;
    }

    public SuKien timTheoMa(String maSK) {
        String sql = "SELECT MaSK, TenSK, NgayBatDau, NgayKetThuc FROM SuKien WHERE MaSK = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maSK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSuKien(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi tìm sự kiện theo mã: " + maSK, e);
        }
        return null;
    }

    public List<SuKien> timTheoTen(String tuKhoa) {
        String sql = "SELECT MaSK, TenSK, NgayBatDau, NgayKetThuc "
                   + "FROM SuKien WHERE TenSK LIKE ? ORDER BY NgayBatDau DESC, MaSK ASC";
        List<SuKien> ketQua = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + tuKhoa + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(mapSuKien(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi tìm sự kiện theo tên", e);
        }
        return ketQua;
    }

    public List<SuKien> traCuuTheoThoiGian(LocalDate tuNgay, LocalDate denNgay) {
        String sql = "SELECT MaSK, TenSK, NgayBatDau, NgayKetThuc "
                   + "FROM SuKien "
                   + "WHERE (? IS NULL OR COALESCE(NgayKetThuc, NgayBatDau) >= ?) "
                   + "  AND (? IS NULL OR NgayBatDau <= ?) "
                   + "ORDER BY NgayBatDau ASC, MaSK ASC";
        List<SuKien> ketQua = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (tuNgay != null) {
                Date dTu = Date.valueOf(tuNgay);
                ps.setDate(1, dTu);
                ps.setDate(2, dTu);
            } else {
                ps.setNull(1, Types.DATE);
                ps.setNull(2, Types.DATE);
            }
            if (denNgay != null) {
                Date dDen = Date.valueOf(denNgay);
                ps.setDate(3, dDen);
                ps.setDate(4, dDen);
            } else {
                ps.setNull(3, Types.DATE);
                ps.setNull(4, Types.DATE);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(mapSuKien(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi tra cứu sự kiện theo thời gian diễn ra", e);
        }
        return ketQua;
    }

    public void them(SuKien sk) {
        String sql = "INSERT INTO SuKien (MaSK, TenSK, NgayBatDau, NgayKetThuc) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sk.getMaSK());
            ps.setString(2, sk.getTenSK());
            ps.setDate(3, Date.valueOf(sk.getNgayBatDau()));
            if (sk.getNgayKetThuc() != null) {
                ps.setDate(4, Date.valueOf(sk.getNgayKetThuc()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi thêm sự kiện mới: " + e.getMessage(), e);
        }
    }

    public boolean capNhat(SuKien sk) {
        String sql = "UPDATE SuKien SET TenSK = ?, NgayBatDau = ?, NgayKetThuc = ? WHERE MaSK = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sk.getTenSK());
            ps.setDate(2, Date.valueOf(sk.getNgayBatDau()));
            if (sk.getNgayKetThuc() != null) {
                ps.setDate(3, Date.valueOf(sk.getNgayKetThuc()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setString(4, sk.getMaSK());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi cập nhật sự kiện: " + e.getMessage(), e);
        }
    }

    public boolean coGiaoDichThuChi(String maSK) {
        String sql = "SELECT 1 FROM ThuChi WHERE MaSK = ? LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maSK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi kiểm tra giao dịch thu chi của sự kiện", e);
        }
    }

    public boolean xoa(String maSK) {
        String sql = "DELETE FROM SuKien WHERE MaSK = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maSK);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi xóa sự kiện: " + e.getMessage(), e);
        }
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

    public boolean daThamGia(String msv, String maSK) {
        String sql = "SELECT 1 FROM ThamGiaSuKien WHERE MSV = ? AND MaSK = ? LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, msv);
            ps.setString(2, maSK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi kiểm tra thành viên tham gia sự kiện", e);
        }
    }

    public void themThanhVienThamGia(String msv, String maSK, String vaiTro) {
        String sql = "INSERT INTO ThamGiaSuKien (MSV, MaSK, VaiTro) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, msv);
            ps.setString(2, maSK);
            ps.setString(3, vaiTro);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi thêm thành viên tham gia sự kiện: " + e.getMessage(), e);
        }
    }

    public boolean capNhatVaiTroThamGia(String msv, String maSK, String vaiTroMoi) {
        String sql = "UPDATE ThamGiaSuKien SET VaiTro = ? WHERE MSV = ? AND MaSK = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, vaiTroMoi);
            ps.setString(2, msv);
            ps.setString(3, maSK);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi cập nhật vai trò tham gia sự kiện: " + e.getMessage(), e);
        }
    }

    public boolean xoaThanhVienThamGia(String msv, String maSK) {
        String sql = "DELETE FROM ThamGiaSuKien WHERE MSV = ? AND MaSK = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, msv);
            ps.setString(2, maSK);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi xóa thành viên khỏi sự kiện: " + e.getMessage(), e);
        }
    }

    public List<ThamGiaSuKien> layThanhVienTheoSuKien(String maSK) {
        String sql = "SELECT tg.MaSK, sk.TenSK, tg.MSV, tv.HoVaTen, tv.ChucVu, tv.MaBan, tg.VaiTro "
                   + "FROM ThamGiaSuKien tg "
                   + "INNER JOIN SuKien sk ON tg.MaSK = sk.MaSK "
                   + "INNER JOIN ThanhVien tv ON tg.MSV = tv.MSV "
                   + "WHERE tg.MaSK = ? "
                   + "ORDER BY tg.MSV ASC";
        List<ThamGiaSuKien> danhSach = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maSK);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThamGiaSuKien tg = new ThamGiaSuKien();
                    tg.setMaSK(rs.getString("MaSK"));
                    tg.setTenSK(rs.getString("TenSK"));
                    tg.setMsv(rs.getString("MSV"));
                    tg.setHoVaTen(rs.getString("HoVaTen"));
                    tg.setChucVu(rs.getString("ChucVu"));
                    tg.setMaBan(rs.getString("MaBan"));
                    tg.setVaiTro(rs.getString("VaiTro"));
                    danhSach.add(tg);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi lấy danh sách thành viên của sự kiện: " + maSK, e);
        }
        return danhSach;
    }

    public List<ThamGiaSuKien> layLichSuSuKienCuaThanhVien(String msv) {
        String sql = "SELECT tg.MSV, tv.HoVaTen, sk.MaSK, sk.TenSK, sk.NgayBatDau, sk.NgayKetThuc, tg.VaiTro "
                   + "FROM ThamGiaSuKien tg "
                   + "INNER JOIN SuKien sk ON tg.MaSK = sk.MaSK "
                   + "INNER JOIN ThanhVien tv ON tg.MSV = tv.MSV "
                   + "WHERE tg.MSV = ? "
                   + "ORDER BY sk.NgayBatDau DESC, sk.MaSK ASC";
        List<ThamGiaSuKien> danhSach = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, msv);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThamGiaSuKien tg = new ThamGiaSuKien();
                    tg.setMsv(rs.getString("MSV"));
                    tg.setHoVaTen(rs.getString("HoVaTen"));
                    tg.setMaSK(rs.getString("MaSK"));
                    tg.setTenSK(rs.getString("TenSK"));
                    Date bd = rs.getDate("NgayBatDau");
                    if (bd != null) {
                        tg.setNgayBatDau(bd.toLocalDate());
                    }
                    Date kt = rs.getDate("NgayKetThuc");
                    if (kt != null) {
                        tg.setNgayKetThuc(kt.toLocalDate());
                    }
                    tg.setVaiTro(rs.getString("VaiTro"));
                    danhSach.add(tg);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi khi lấy lịch sử sự kiện của thành viên: " + msv, e);
        }
        return danhSach;
    }

    private SuKien mapSuKien(ResultSet rs) throws SQLException {
        String maSK = rs.getString("MaSK");
        String tenSK = rs.getString("TenSK");
        Date bd = rs.getDate("NgayBatDau");
        LocalDate ngayBatDau = (bd != null) ? bd.toLocalDate() : null;
        Date kt = rs.getDate("NgayKetThuc");
        LocalDate ngayKetThuc = (kt != null) ? kt.toLocalDate() : null;
        return new SuKien(maSK, tenSK, ngayBatDau, ngayKetThuc);
    }
}
