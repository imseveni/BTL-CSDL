package app;

import common.PhienLamViec.NguoiDung;
import common.PhienLamViec.VaiTro;
import common.Validation;
import thanhvien.ThanhVien;
import thanhvien.ThanhVienDao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.function.Function;

/**
 * Xác định người dùng khi mở ứng dụng. Thành viên nhập MSV (phải là thành viên chưa rời CLB);
 * Ban Chủ nhiệm nhập MSV và mã xác nhận. Đây là cơ chế đơn giản cho bài tập lớn, không thay cho mật khẩu thật.
 *
 * <p>Mã xác nhận lấy từ biến môi trường {@code CLB_ADMIN_CODE}, hoặc thuộc tính JVM {@code -Dclb.admin.code=...},
 * nếu không có thì dùng {@link #MA_XAC_NHAN_MAC_DINH}. Đổi mã trước buổi demo thật.
 */
public class DangNhapService {

    public static final String MA_XAC_NHAN_MAC_DINH = "BCN2026";
    static final String TRANG_THAI_DA_ROI = "Đã rời CLB";

    private final Function<String, ThanhVien> tim;
    private final String maXacNhan;

    public DangNhapService() {
        this(msv -> new ThanhVienDao().chiTiet(msv), docMaXacNhan());
    }

    public DangNhapService(Function<String, ThanhVien> tim, String maXacNhan) {
        this.tim = tim;
        this.maXacNhan = maXacNhan;
    }

    public NguoiDung dangNhapThanhVien(String msv) {
        ThanhVien tv = timThanhVienDangHoatDong(msv);
        return new NguoiDung(VaiTro.THANH_VIEN, tv.msv(), tv.hoVaTen());
    }

    public NguoiDung dangNhapBanChuNhiem(String msv, String maNhap) {
        String ma = Validation.required(maNhap, "Mã xác nhận");
        if (!MessageDigest.isEqual(ma.getBytes(StandardCharsets.UTF_8), maXacNhan.getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Mã xác nhận Ban Chủ nhiệm không đúng.");
        }
        ThanhVien tv = timThanhVienDangHoatDong(msv);
        return new NguoiDung(VaiTro.BAN_CHU_NHIEM, tv.msv(), tv.hoVaTen());
    }

    private ThanhVien timThanhVienDangHoatDong(String msv) {
        String ma = Validation.required(msv, "Mã sinh viên", 20);
        ThanhVien tv;
        try {
            tv = tim.apply(ma);
        } catch (common.DataAccessException e) {
            // "Thành viên không tồn tại" và lỗi kết nối đều qua đây; chỉ nói không tồn tại khi DB trả lời được.
            if (e.getCause() == null) {
                throw new IllegalArgumentException("Không có thành viên có mã sinh viên " + ma + ".");
            }
            throw e;
        }
        if (tv == null) {
            throw new IllegalArgumentException("Không có thành viên có mã sinh viên " + ma + ".");
        }
        if (TRANG_THAI_DA_ROI.equals(tv.trangThaiHD())) {
            throw new IllegalArgumentException("Thành viên " + tv.msv() + " đã rời CLB nên không đăng nhập được.");
        }
        return tv;
    }

    private static String docMaXacNhan() {
        String env = System.getenv("CLB_ADMIN_CODE");
        if (env != null && !env.isBlank()) {
            return env.strip();
        }
        String prop = System.getProperty("clb.admin.code");
        return prop != null && !prop.isBlank() ? prop.strip() : MA_XAC_NHAN_MAC_DINH;
    }
}
