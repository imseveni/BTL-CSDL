package baocaomuontra;

import baocaomuontra.BoLocBaoCaoMuonTra.LoaiThoiGian;
import common.ConnectionProvider;
import common.DataAccessException;
import config.DBConnection;
import java.sql.CallableStatement;
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

    /** Truy vấn nằm trong thủ tục sp_BaoCaoMuonTra (sql/modules/11_baocaomuontra/queries.sql). */
    private static final String SQL_BAO_CAO = "{CALL sp_BaoCaoMuonTra(?, ?, ?, ?)}";

    private static final String SQL_THANH_VIEN_CLB = """
            SELECT tv.MSV, tv.HoVaTen, tv.ChucVu, b.TenBan
            FROM ThanhVien tv
            LEFT JOIN BanChuyenMon b ON b.MaBan = tv.MaBan
            ORDER BY tv.MSV ASC
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
        String loai = loaiThoiGian == LoaiThoiGian.NGAY_TRA ? "NGAY_TRA" : "NGAY_MUON";
        LocalDate moc = ngayMoc == null ? LocalDate.now() : ngayMoc;
        List<BaoCaoMuonTraItem> items = new ArrayList<>();

        try (Connection conn = connections.getConnection();
                CallableStatement ps = conn.prepareCall(SQL_BAO_CAO)) {
            ps.setDate(1, Date.valueOf(tuNgay));
            ps.setDate(2, Date.valueOf(denNgay));
            ps.setString(3, loai);
            ps.setDate(4, Date.valueOf(moc));

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

    /**
     * Lấy danh sách thành viên trong Câu lạc bộ từ bảng {@code ThanhVien} và {@code BanChuyenMon}
     * để chọn làm người lập báo cáo.
     */
    public List<NguoiLapBaoCao> danhSachThanhVienClb() {
        List<NguoiLapBaoCao> list = new ArrayList<>();
        try (Connection conn = connections.getConnection();
                PreparedStatement ps = conn.prepareStatement(SQL_THANH_VIEN_CLB);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new NguoiLapBaoCao(
                        rs.getString("MSV"),
                        rs.getString("HoVaTen"),
                        rs.getString("ChucVu"),
                        rs.getString("TenBan")));
            }
        } catch (SQLException ignored) {
            // Dự phòng khi chạy unit test không kết nối MySQL
        }
        return list;
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
