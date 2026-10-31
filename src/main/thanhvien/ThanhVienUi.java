package thanhvien;

import common.ConsoleUtil;
import common.DataAccessException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

/** Menu console của module Thành viên. Nhập 0 để quay lại menu chính. */
public class ThanhVienUi {

    private final ThanhVienService service;

    public ThanhVienUi() {
        this(new ThanhVienService());
    }

    public ThanhVienUi(ThanhVienService service) {
        this.service = Objects.requireNonNull(service);
    }

    public void run(Scanner in) {
        while (true) {
            System.out.println("\n===== QUẢN LÝ THÀNH VIÊN =====");
            System.out.println("1. Tìm kiếm / danh sách thành viên\n2. Xem chi tiết một thành viên\n3. Thêm thành viên");
            System.out.println("4. Cập nhật thông tin cá nhân\n5. Đổi trạng thái hoạt động\n6. Xóa thành viên");
            System.out.println("7. Thành viên gia nhập trong khoảng ngày\n8. Thống kê theo trạng thái");
            System.out.println("9. Lịch sử hoạt động của thành viên\n10. Tổng quan hoạt động của thành viên\n0. Quay lại");
            try {
                int choice = Integer.parseInt(read(in, "Chọn: "));
                switch (choice) {
                    case 0 -> { return; }
                    case 1 -> timKiem(in);
                    case 2 -> inChiTiet(service.chiTiet(read(in, "MSV: ")));
                    case 3 -> { service.them(nhapMoi(in)); success(); }
                    case 4 -> { capNhat(in); }
                    case 5 -> {
                        String msv = read(in, "MSV: ");
                        System.out.println("Trạng thái: " + String.join(" / ", ThanhVienService.TRANG_THAI));
                        service.doiTrangThai(msv, read(in, "Trạng thái mới: "));
                        success();
                    }
                    case 6 -> {
                        String msv = read(in, "MSV cần xóa: ");
                        if (confirm(in, "Xóa thành viên " + msv + "?")) { service.xoa(msv); success(); }
                    }
                    case 7 -> {
                        LocalDate tu = readDate(in, "Từ ngày (yyyy-MM-dd, Enter = không giới hạn): ", true);
                        LocalDate den = readDate(in, "Đến ngày (yyyy-MM-dd, Enter = không giới hạn): ", true);
                        List<ThanhVien> rows = service.giaNhap(tu, den);
                        rows.forEach(tv -> System.out.printf("%s | %s | gia nhập %s | %s | %s | %s%n", tv.msv(),
                                tv.hoVaTen(), tv.ngayGiaNhap(), display(tv.trangThaiHD()), display(tv.tenBan()),
                                display(tv.chucVu())));
                        System.out.println("Số thành viên: " + rows.size());
                    }
                    case 8 -> service.thongKeTrangThai().forEach(r ->
                            System.out.printf("%s: %d thành viên%n", r.trangThai(), r.soThanhVien()));
                    case 9 -> {
                        List<HoatDong> rows = service.lichSu(read(in, "MSV: "));
                        if (rows.isEmpty()) System.out.println("Thành viên chưa có hoạt động nào được ghi nhận.");
                        rows.forEach(h -> System.out.printf("%s | %s | %s | %s | %s%n", display(h.ngay() == null ? null : h.ngay().toString()),
                                h.loai(), h.ma(), display(h.ten()), display(h.chiTiet())));
                    }
                    case 10 -> {
                        TongQuanThanhVien t = service.tongQuan(read(in, "MSV: "));
                        System.out.printf("%s | %s | %s%n", t.msv(), t.hoVaTen(), display(t.trangThaiHD()));
                        System.out.printf("Khóa học: %d | Dự án: %d | Sự kiện: %d | Đánh giá: %d | Phiếu mượn: %d%n",
                                t.soKhoaHoc(), t.soDuAn(), t.soSuKien(), t.soDanhGia(), t.soPhieuMuon());
                    }
                    default -> System.out.println("Lựa chọn không hợp lệ.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập số tương ứng với chức năng.");
            } catch (IllegalArgumentException | DataAccessException e) {
                System.out.println("Không thực hiện được: " + e.getMessage());
            } catch (NoSuchElementException e) {
                return;
            }
        }
    }

    private void timKiem(Scanner in) {
        String tuKhoa = read(in, "Từ khóa trong MSV hoặc họ tên (Enter = bỏ qua): ");
        String maBan = read(in, "Mã ban (Enter = bỏ qua): ");
        System.out.println("Trạng thái: " + String.join(" / ", ThanhVienService.TRANG_THAI));
        String trangThai = read(in, "Lọc theo trạng thái (Enter = bỏ qua): ");
        List<ThanhVien> rows = service.tim(tuKhoa, maBan, trangThai);
        if (rows.isEmpty()) System.out.println("Không có thành viên phù hợp.");
        rows.forEach(tv -> System.out.printf("%s | %s | %s | %s | %s%n", tv.msv(), tv.hoVaTen(),
                display(tv.tenBan()), display(tv.chucVu()), display(tv.trangThaiHD())));
        System.out.println("Số thành viên: " + rows.size());
    }

    private void inChiTiet(ThanhVien tv) {
        System.out.println("MSV: " + tv.msv());
        System.out.println("Họ và tên: " + tv.hoVaTen());
        System.out.println("Ngày sinh: " + display(tv.ngaySinh() == null ? null : tv.ngaySinh().toString()));
        System.out.println("Giới tính: " + display(tv.gioiTinh()));
        System.out.println("Email: " + display(tv.email()));
        System.out.println("Số điện thoại: " + display(tv.sdt()));
        System.out.println("Ngày gia nhập: " + tv.ngayGiaNhap());
        System.out.println("Trạng thái: " + display(tv.trangThaiHD()));
        System.out.println("Ban: " + display(tv.tenBan()) + " (" + tv.maBan() + ")");
        System.out.println("Chức vụ: " + display(tv.chucVu()));
    }

    private ThanhVien nhapMoi(Scanner in) {
        String msv = read(in, "MSV: ");
        String hoVaTen = read(in, "Họ và tên: ");
        LocalDate ngaySinh = readDate(in, "Ngày sinh (yyyy-MM-dd, Enter = bỏ qua): ", true);
        String gioiTinh = read(in, "Giới tính (" + String.join("/", ThanhVienService.GIOI_TINH) + ", Enter = bỏ qua): ");
        String email = read(in, "Email (Enter = bỏ qua): ");
        String sdt = read(in, "Số điện thoại (Enter = bỏ qua): ");
        LocalDate ngayGiaNhap = readDate(in, "Ngày gia nhập (yyyy-MM-dd): ", false);
        String trangThai = read(in, "Trạng thái (Enter = Đang hoạt động): ");
        String maBan = read(in, "Mã ban: ");
        String chucVu = read(in, "Chức vụ (Enter = Thành viên): ");
        return new ThanhVien(msv, hoVaTen, ngaySinh, gioiTinh, email, sdt, ngayGiaNhap, trangThai, maBan, null, chucVu);
    }

    /** Hiện giá trị hiện tại; bỏ trống nghĩa là giữ nguyên, nhập "-" để xóa giá trị tùy chọn. */
    private void capNhat(Scanner in) {
        ThanhVien cu = service.chiTiet(read(in, "MSV cần cập nhật: "));
        System.out.println("Enter = giữ nguyên giá trị hiện tại; nhập - để xóa giá trị (với mục tùy chọn).");
        String hoVaTen = keep(read(in, "Họ và tên [" + cu.hoVaTen() + "]: "), cu.hoVaTen());
        String ngaySinhText = read(in, "Ngày sinh [" + display(cu.ngaySinh() == null ? null : cu.ngaySinh().toString()) + "]: ");
        LocalDate ngaySinh = ngaySinhText.isEmpty() ? cu.ngaySinh() : "-".equals(ngaySinhText) ? null : parseDate(ngaySinhText);
        String gioiTinh = keepOptional(read(in, "Giới tính [" + display(cu.gioiTinh()) + "]: "), cu.gioiTinh());
        String email = keepOptional(read(in, "Email [" + display(cu.email()) + "]: "), cu.email());
        String sdt = keepOptional(read(in, "Số điện thoại [" + display(cu.sdt()) + "]: "), cu.sdt());
        service.capNhat(new ThanhVien(cu.msv(), hoVaTen, ngaySinh, gioiTinh, email, sdt, null, null, null, null, null));
        success();
    }

    private static String keep(String input, String current) {
        return input.isEmpty() ? current : input;
    }

    private static String keepOptional(String input, String current) {
        if (input.isEmpty()) return current;
        return "-".equals(input) ? null : input;
    }

    private LocalDate readDate(Scanner in, String prompt, boolean optional) {
        String text = read(in, prompt);
        if (text.isEmpty()) {
            if (optional) return null;
            throw new IllegalArgumentException("Ngày không được để trống.");
        }
        return parseDate(text);
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày không hợp lệ, cần đúng dạng yyyy-MM-dd và có thật.");
        }
    }

    private String read(Scanner in, String prompt) {
        return ConsoleUtil.readLine(in, prompt);
    }

    private void success() {
        System.out.println("Thực hiện thành công.");
    }

    private String display(String value) {
        return value == null || value.isBlank() ? "(chưa có)" : value;
    }

    private boolean confirm(Scanner in, String prompt) {
        return "y".equalsIgnoreCase(read(in, prompt + " [y/N]: "));
    }
}
