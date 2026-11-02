package baocaomuontra;

import baocaomuontra.BoLocBaoCaoMuonTra.LoaiThoiGian;
import common.ConnectionProvider;
import common.DataAccessException;
import config.DBConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Truy vấn dữ liệu báo cáo mượn – trả cơ sở vật chất trực tiếp từ cơ sở dữ liệu MySQL.
 * Sử dụng lại các bảng hiện có: {@code PhieuMuon}, {@code ChiTietMuon}, {@code CoSoVatChat}, {@code ThanhVien}.
 */
public class BaoCaoMuonTraDao {

    private static final String BASE_SELECT = """
            SELECT pm.MaPhieuMuon,
                   ct.MaThietBi,
                   tb.TenThietBi,
                   pm.MSVNguoiMuon,
                   tv.HoVaTen AS TenNguoiMuon,
                   ct.SoLuongMuon,
                   pm.NgayMuon,
                   pm.NgayHenTra,
                   ct.NgayTraThucTe,
                   pm.MucDichMuon
            FROM ChiTietMuon ct
            JOIN PhieuMuon pm ON pm.MaPhieuMuon = ct.MaPhieuMuon
            JOIN CoSoVatChat tb ON tb.MaThietBi = ct.MaThietBi
            LEFT JOIN ThanhVien tv ON tv.MSV = pm.MSVNguoiMuon
            WHERE pm.TrangThaiDuyet = 'DaDuyet'
            """;

    private static final String SQL_THEO_NGAY_MUON = BASE_SELECT + """
              AND pm.NgayMuon BETWEEN ? AND ?
            ORDER BY pm.NgayMuon ASC, pm.MaPhieuMuon ASC, ct.MaThietBi ASC
            """;

    private static final String SQL_THEO_NGAY_TRA = BASE_SELECT + """
              AND ct.NgayTraThucTe IS NOT NULL
              AND ct.NgayTraThucTe BETWEEN ? AND ?
            ORDER BY ct.NgayTraThucTe ASC, pm.MaPhieuMuon ASC, ct.MaThietBi ASC
            """;

    private final ConnectionProvider connections;

    public BaoCaoMuonTraDao() {
        this(DBConnection::getConnection);
    }

    public BaoCaoMuonTraDao(ConnectionProvider connections) {
        this.connections = connections;
    }

    /**
     * Truy vấn các giao dịch mượn – trả thuộc khoảng thời gian {@code [tuNgay, denNgay]}
     * theo tiêu chí Ngày mượn hoặc Ngày trả thực tế.
     */
    public List<BaoCaoMuonTraItem> truyVanTheoThoiGian(
            LocalDate tuNgay,
            LocalDate denNgay,
            LoaiThoiGian loaiThoiGian,
            LocalDate ngayMoc) {
        String sql = loaiThoiGian == LoaiThoiGian.NGAY_TRA ? SQL_THEO_NGAY_TRA : SQL_THEO_NGAY_MUON;
        LocalDate moc = ngayMoc == null ? LocalDate.now() : ngayMoc;
        List<BaoCaoMuonTraItem> items = new ArrayList<>();

        try (Connection conn = connections.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(docDong(rs, moc));
                }
            }
            return items;
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Không thể truy vấn dữ liệu báo cáo mượn – trả cơ sở vật chất: " + e.getMessage(), e);
        }
    }

    private static BaoCaoMuonTraItem docDong(ResultSet rs, LocalDate ngayMoc) throws SQLException {
        return BaoCaoMuonTraItem.of(
                rs.getString("MaPhieuMuon"),
                rs.getString("MaThietBi"),
                rs.getString("TenThietBi"),
                rs.getString("MSVNguoiMuon"),
                rs.getString("TenNguoiMuon"),
                rs.getInt("SoLuongMuon"),
                ngay(rs, "NgayMuon"),
                ngay(rs, "NgayHenTra"),
                ngay(rs, "NgayTraThucTe"),
                rs.getString("MucDichMuon"),
                ngayMoc);
    }

    private static LocalDate ngay(ResultSet rs, String column) throws SQLException {
        Date d = rs.getDate(column);
        return d == null ? null : d.toLocalDate();
    }
}
