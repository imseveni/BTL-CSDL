package common;

import java.util.Objects;

/**
 * Người đang dùng hệ thống: Thành viên hoặc Ban Chủ nhiệm / Người quản lý CLB (theo bản mô tả đề tài,
 * mục "Các tác nhân tương tác hệ thống"). Màn hình đăng nhập đặt phiên làm việc; các Service gọi
 * {@code yeuCau...} trước khi làm việc, nên quyền được chặn ở tầng nghiệp vụ chứ không chỉ ẩn nút.
 *
 * <p>Mặc định (khi không qua màn hình đăng nhập: menu dòng lệnh, kiểm thử) phiên là quản trị đầy đủ.
 * Ứng dụng Swing luôn đặt lại phiên khi đăng nhập và khi đăng xuất.
 */
public final class PhienLamViec {

    public enum VaiTro {
        THANH_VIEN("Thành viên Câu lạc bộ"),
        BAN_CHU_NHIEM("Ban Chủ nhiệm / Người quản lý Câu lạc bộ");

        private final String ten;

        VaiTro(String ten) {
            this.ten = ten;
        }

        public String ten() {
            return ten;
        }
    }

    /** msv có thể null với phiên quản trị dòng lệnh. */
    public record NguoiDung(VaiTro vaiTro, String msv, String hoVaTen) {
        public NguoiDung {
            Objects.requireNonNull(vaiTro);
        }
    }

    private static final NguoiDung QUAN_TRI_DONG_LENH =
            new NguoiDung(VaiTro.BAN_CHU_NHIEM, null, "Quản trị (dòng lệnh)");

    private static volatile NguoiDung hienTai = QUAN_TRI_DONG_LENH;

    private PhienLamViec() {
    }

    public static NguoiDung hienTai() {
        return hienTai;
    }

    public static void batDau(NguoiDung nguoiDung) {
        hienTai = Objects.requireNonNull(nguoiDung);
    }

    /** Về phiên mặc định (quản trị dòng lệnh). Dùng khi kiểm thử hoặc sau khi đăng xuất. */
    public static void datLai() {
        hienTai = QUAN_TRI_DONG_LENH;
    }

    public static boolean laBanChuNhiem() {
        return hienTai.vaiTro() == VaiTro.BAN_CHU_NHIEM;
    }

    public static boolean laThanhVien() {
        return hienTai.vaiTro() == VaiTro.THANH_VIEN;
    }

    /** MSV của thành viên đang đăng nhập; null nếu là phiên quản trị không gắn với thành viên nào. */
    public static String msvHienTai() {
        return hienTai.msv();
    }

    /** Chỉ Ban Chủ nhiệm được làm {@code hanhDong}. */
    public static void yeuCauBanChuNhiem(String hanhDong) {
        if (!laBanChuNhiem()) {
            throw new QuyenException("Bạn đang đăng nhập với vai trò Thành viên nên không được " + hanhDong
                    + ". Chức năng này chỉ dành cho Ban Chủ nhiệm.");
        }
    }

    /** Ban Chủ nhiệm được làm với mọi MSV; Thành viên chỉ được làm với chính mình. */
    public static void yeuCauChinhMinhHoacBanChuNhiem(String msv, String hanhDong) {
        if (laBanChuNhiem()) {
            return;
        }
        if (msv == null || !msv.strip().equalsIgnoreCase(String.valueOf(hienTai.msv()))) {
            throw new QuyenException("Thành viên chỉ được " + hanhDong + " của chính mình.");
        }
    }
}
