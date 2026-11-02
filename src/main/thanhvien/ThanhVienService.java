package thanhvien;

import common.Validation;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Kiểm tra dữ liệu nhập trước khi xuống CSDL (cùng quy tắc với thủ tục trong sql/modules/01_thanhvien).
 * Các ràng buộc tham chiếu (ban tồn tại, MSV trùng...) do MySQL đảm bảo.
 */
public class ThanhVienService {

    public static final List<String> TRANG_THAI = List.of("Đang hoạt động", "Tạm nghỉ", "Đã rời CLB");
    public static final List<String> GIOI_TINH = List.of("Nam", "Nữ", "Khác");

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern SDT = Pattern.compile("^\\+?[0-9]{9,15}$");

    private final ThanhVienDao dao;

    public ThanhVienService() {
        this(new ThanhVienDao());
    }

    public ThanhVienService(ThanhVienDao dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    public void them(ThanhVien tv) {
        if (tv == null) {
            throw new IllegalArgumentException("Thông tin thành viên không được trống.");
        }
        String msv = Validation.required(tv.msv(), "Mã sinh viên", 20);
        PersonalInfo info = personalInfo(tv);
        Validation.requiredDate(tv.ngayGiaNhap(), "Ngày gia nhập");
        if (tv.ngayGiaNhap().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày gia nhập không được ở tương lai.");
        }
        if (info.ngaySinh() != null && tv.ngayGiaNhap().isBefore(info.ngaySinh())) {
            throw new IllegalArgumentException("Ngày gia nhập không được trước ngày sinh.");
        }
        String trangThai = Validation.optional(tv.trangThaiHD());
        if (trangThai == null) {
            trangThai = TRANG_THAI.get(0);
        }
        oneOf(trangThai, TRANG_THAI, "Trạng thái");
        String maBan = Validation.required(tv.maBan(), "Mã ban", 20);
        String chucVu = Validation.optional(tv.chucVu(), "Chức vụ", 100);
        dao.them(new ThanhVien(msv, info.hoVaTen(), info.ngaySinh(), info.gioiTinh(), info.email(), info.sdt(),
                tv.ngayGiaNhap(), trangThai, maBan, null, chucVu));
    }

    /** Cập nhật thông tin cá nhân. Ban và chức vụ đổi ở module 02; trạng thái dùng {@link #doiTrangThai}. */
    public void capNhat(ThanhVien tv) {
        if (tv == null) {
            throw new IllegalArgumentException("Thông tin thành viên không được trống.");
        }
        String msv = Validation.required(tv.msv(), "Mã sinh viên", 20);
        PersonalInfo info = personalInfo(tv);
        dao.capNhat(new ThanhVien(msv, info.hoVaTen(), info.ngaySinh(), info.gioiTinh(), info.email(), info.sdt(),
                null, null, null, null, null));
    }

    public void doiTrangThai(String msv, String trangThai) {
        String ma = Validation.required(msv, "Mã sinh viên", 20);
        String tt = Validation.required(trangThai, "Trạng thái");
        oneOf(tt, TRANG_THAI, "Trạng thái");
        dao.doiTrangThai(ma, tt);
    }

    public void xoa(String msv) {
        dao.xoa(Validation.required(msv, "Mã sinh viên", 20));
    }

    /** Từ khóa, mã ban, trạng thái đều tùy chọn; để trống nghĩa là không lọc theo tiêu chí đó. */
    public List<ThanhVien> tim(String tuKhoa, String maBan, String trangThai) {
        String tt = Validation.optional(trangThai);
        if (tt != null) {
            oneOf(tt, TRANG_THAI, "Trạng thái");
        }
        return dao.tim(Validation.optional(tuKhoa, "Từ khóa", 100), Validation.optional(maBan, "Mã ban", 20), tt);
    }

    public ThanhVien chiTiet(String msv) {
        return dao.chiTiet(Validation.required(msv, "Mã sinh viên", 20));
    }

    public List<ThanhVien> giaNhap(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay != null && denNgay != null && denNgay.isBefore(tuNgay)) {
            throw new IllegalArgumentException("Đến ngày không được trước từ ngày.");
        }
        return dao.giaNhap(tuNgay, denNgay);
    }

    public List<BanLuaChon> danhSachBan() {
        return dao.danhSachBan();
    }

    public List<ThongKeTrangThai> thongKeTrangThai() {
        return dao.thongKeTrangThai();
    }

    public List<HoatDong> lichSu(String msv) {
        return dao.lichSu(Validation.required(msv, "Mã sinh viên", 20));
    }

    public TongQuanThanhVien tongQuan(String msv) {
        return dao.tongQuan(Validation.required(msv, "Mã sinh viên", 20));
    }

    // ---------------------------------------------------------------------------------------

    private record PersonalInfo(String hoVaTen, LocalDate ngaySinh, String gioiTinh, String email, String sdt) {
    }

    private PersonalInfo personalInfo(ThanhVien tv) {
        String hoVaTen = Validation.required(tv.hoVaTen(), "Họ và tên", 100);
        if (tv.ngaySinh() != null && tv.ngaySinh().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày sinh không được ở tương lai.");
        }
        String gioiTinh = Validation.optional(tv.gioiTinh());
        if (gioiTinh != null) {
            oneOf(gioiTinh, GIOI_TINH, "Giới tính");
        }
        String email = Validation.optional(tv.email(), "Email", 100);
        if (email != null && !EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
        String sdt = Validation.optional(tv.sdt());
        if (sdt != null && !SDT.matcher(sdt).matches()) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (9 đến 15 chữ số, có thể bắt đầu bằng +).");
        }
        return new PersonalInfo(hoVaTen, tv.ngaySinh(), gioiTinh, email, sdt);
    }

    private static void oneOf(String value, List<String> allowed, String label) {
        if (!allowed.contains(value)) {
            throw new IllegalArgumentException(label + " phải là một trong: " + String.join(", ", allowed) + ".");
        }
    }
}
