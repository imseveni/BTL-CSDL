package sukien;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import common.ConsoleUtil;
import common.DataAccessException;

/**
 * Giao diện console (UI) cho Module 06 – Quản lý Sự kiện.
 * Chỉ làm nhiệm vụ nhập/xuất và gọi SuKienService.
 */
public class SuKienUi {

    private final SuKienService service;

    public SuKienUi() {
        this.service = new SuKienService();
    }

    public void run(Scanner in) {
        while (true) {
            System.out.println();
            System.out.println("===== MODULE 06: QUẢN LÝ SỰ KIỆN =====");
            System.out.println("1. Xem danh sách tất cả sự kiện");
            System.out.println("2. Thêm mới sự kiện");
            System.out.println("3. Cập nhật thông tin sự kiện");
            System.out.println("4. Xóa sự kiện");
            System.out.println("5. Tra cứu thời gian diễn ra sự kiện");
            System.out.println("6. Thêm thành viên tham gia sự kiện & ghi nhận vai trò");
            System.out.println("7. Cập nhật / Xóa thành viên tham gia sự kiện");
            System.out.println("8. Xem danh sách thành viên của từng sự kiện");
            System.out.println("9. Xem lịch sử sự kiện của một thành viên");
            System.out.println("0. Quay lại menu chính");

            int choice = ConsoleUtil.readInt(in, "Chọn chức năng: ");
            try {
                switch (choice) {
                    case 1 -> hienThiDanhSachSuKien();
                    case 2 -> themSuKien(in);
                    case 3 -> capNhatSuKien(in);
                    case 4 -> xoaSuKien(in);
                    case 5 -> traCuuThoiGianSuKien(in);
                    case 6 -> themThanhVienThamGia(in);
                    case 7 -> capNhatHoacXoaThanhVienThamGia(in);
                    case 8 -> xemThanhVienCuaSuKien(in);
                    case 9 -> xemLichSuSuKienThanhVien(in);
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Lựa chọn không hợp lệ. Vui lòng chọn từ 0 đến 9.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("[Cảnh báo nghiệp vụ] " + e.getMessage());
            } catch (DataAccessException e) {
                System.out.println("[Lỗi CSDL] " + e.getMessage());
            }
        }
    }

    private void hienThiDanhSachSuKien() {
        List<SuKien> danhSach = service.layTatCaSuKien();
        inBangSuKien(danhSach);
    }

    private void themSuKien(Scanner in) {
        System.out.println("--- THÊM MỚI SỰ KIỆN ---");
        String maSK = ConsoleUtil.readLine(in, "Nhập mã sự kiện (VD: SK06): ");
        String tenSK = ConsoleUtil.readLine(in, "Nhập tên sự kiện: ");
        LocalDate ngayBatDau = docNgayBatBuoc(in, "Nhập ngày bắt đầu (YYYY-MM-DD): ");
        LocalDate ngayKetThuc = docNgayTuyChon(in, "Nhập ngày kết thúc (YYYY-MM-DD, Enter để bỏ qua): ");

        SuKien sk = new SuKien(maSK, tenSK, ngayBatDau, ngayKetThuc);
        service.themSuKien(sk);
        System.out.println("Đã thêm sự kiện '" + sk.getMaSK() + "' thành công!");
    }

    private void capNhatSuKien(Scanner in) {
        System.out.println("--- CẬP NHẬT SỰ KIỆN ---");
        String maSK = ConsoleUtil.readLine(in, "Nhập mã sự kiện cần cập nhật: ");
        SuKien hienTai = service.timSuKienTheoMa(maSK);
        if (hienTai == null) {
            System.out.println("Không tìm thấy sự kiện có mã '" + maSK + "'.");
            return;
        }

        System.out.println("Thông tin hiện tại: " + hienTai.getTenSK()
                + " | Bắt đầu: " + hienTai.getNgayBatDau()
                + " | Kết thúc: " + (hienTai.getNgayKetThuc() != null ? hienTai.getNgayKetThuc() : "Chưa xác định"));

        String tenMoi = ConsoleUtil.readLine(in, "Tên sự kiện mới (Enter để giữ nguyên): ");
        if (tenMoi.isEmpty()) {
            tenMoi = hienTai.getTenSK();
        }

        String sNgayBD = ConsoleUtil.readLine(in, "Ngày bắt đầu mới (YYYY-MM-DD, Enter để giữ nguyên): ");
        LocalDate ngayBDMoi = sNgayBD.isEmpty() ? hienTai.getNgayBatDau() : parseNgay(sNgayBD);

        String sNgayKT = ConsoleUtil.readLine(in,
                "Ngày kết thúc mới (YYYY-MM-DD, nhập 'NULL' để xóa ngày kết thúc, Enter để giữ nguyên): ");
        LocalDate ngayKTMoi;
        if (sNgayKT.isEmpty()) {
            ngayKTMoi = hienTai.getNgayKetThuc();
        } else if (sNgayKT.equalsIgnoreCase("NULL")) {
            ngayKTMoi = null;
        } else {
            ngayKTMoi = parseNgay(sNgayKT);
        }

        SuKien capNhat = new SuKien(hienTai.getMaSK(), tenMoi, ngayBDMoi, ngayKTMoi);
        service.capNhatSuKien(capNhat);
        System.out.println("Đã cập nhật sự kiện '" + capNhat.getMaSK() + "' thành công!");
    }

    private void xoaSuKien(Scanner in) {
        System.out.println("--- XÓA SỰ KIỆN ---");
        String maSK = ConsoleUtil.readLine(in, "Nhập mã sự kiện cần xóa: ");
        String xacNhan = ConsoleUtil.readLine(in, "Bạn có chắc chắn muốn xóa sự kiện '" + maSK + "'? (y/n): ");
        if (!xacNhan.equalsIgnoreCase("y")) {
            System.out.println("Đã hủy thao tác xóa.");
            return;
        }
        service.xoaSuKien(maSK);
        System.out.println("Đã xóa sự kiện '" + maSK + "' thành công!");
    }

    private void traCuuThoiGianSuKien(Scanner in) {
        System.out.println("--- TRA CỨU THỜI GIAN DIỄN RA SỰ KIỆN ---");
        System.out.println("1. Tra cứu theo khoảng thời gian (Từ ngày - Đến ngày)");
        System.out.println("2. Tra cứu theo mã sự kiện");
        System.out.println("3. Tra cứu theo từ khóa tên sự kiện");
        int loai = ConsoleUtil.readInt(in, "Chọn cách tra cứu: ");
        switch (loai) {
            case 1 -> {
                LocalDate tuNgay = docNgayTuyChon(in, "Từ ngày (YYYY-MM-DD, Enter để không giới hạn đầu): ");
                LocalDate denNgay = docNgayTuyChon(in, "Đến ngày (YYYY-MM-DD, Enter để không giới hạn cuối): ");
                List<SuKien> ketQua = service.traCuuTheoThoiGian(tuNgay, denNgay);
                inBangSuKien(ketQua);
            }
            case 2 -> {
                String maSK = ConsoleUtil.readLine(in, "Nhập mã sự kiện: ");
                SuKien sk = service.timSuKienTheoMa(maSK);
                if (sk == null) {
                    System.out.println("Không tìm thấy sự kiện với mã: " + maSK);
                } else {
                    inBangSuKien(List.of(sk));
                }
            }
            case 3 -> {
                String tuKhoa = ConsoleUtil.readLine(in, "Nhập từ khóa tên sự kiện: ");
                List<SuKien> ketQua = service.timSuKienTheoTen(tuKhoa);
                inBangSuKien(ketQua);
            }
            default -> System.out.println("Lựa chọn không hợp lệ.");
        }
    }

    private void themThanhVienThamGia(Scanner in) {
        System.out.println("--- THÊM THÀNH VIÊN THAM GIA SỰ KIỆN ---");
        String maSK = ConsoleUtil.readLine(in, "Nhập mã sự kiện (MaSK): ");
        String msv = ConsoleUtil.readLine(in, "Nhập mã sinh viên (MSV): ");
        String vaiTro = ConsoleUtil.readLine(in, "Nhập vai trò trong sự kiện (VD: Ban tổ chức, Diễn giả, Tham dự): ");

        service.themThanhVienThamGia(msv, maSK, vaiTro);
        System.out.println("Đã thêm thành viên '" + msv + "' vào sự kiện '" + maSK + "' với vai trò '" + vaiTro + "'!");
    }

    private void capNhatHoacXoaThanhVienThamGia(Scanner in) {
        System.out.println("--- CẬP NHẬT VAI TRÒ / XÓA THÀNH VIÊN KHỎI SỰ KIỆN ---");
        System.out.println("1. Cập nhật vai trò của thành viên trong sự kiện");
        System.out.println("2. Xóa thành viên khỏi danh sách tham gia sự kiện");
        int loai = ConsoleUtil.readInt(in, "Chọn thao tác: ");
        String maSK = ConsoleUtil.readLine(in, "Nhập mã sự kiện (MaSK): ");
        String msv = ConsoleUtil.readLine(in, "Nhập mã sinh viên (MSV): ");

        if (loai == 1) {        
            String vaiTroMoi = ConsoleUtil.readLine(in, "Nhập vai trò mới: ");
            service.capNhatVaiTroThamGia(msv, maSK, vaiTroMoi);
            System.out.println("Đã cập nhật vai trò của '" + msv + "' tại sự kiện '" + maSK + "' thành công!");
        } else if (loai == 2) {
            service.xoaThanhVienKhoiSuKien(msv, maSK);
            System.out.println("Đã xóa thành viên '" + msv + "' khỏi sự kiện '" + maSK + "'!");
        } else {
            System.out.println("Lựa chọn không hợp lệ.");
        }
    }

    private void xemThanhVienCuaSuKien(Scanner in) {
        System.out.println("--- XEM THÀNH VIÊN CỦA TỪNG SỰ KIỆN ---");
        String maSK = ConsoleUtil.readLine(in, "Nhập mã sự kiện (MaSK): ");
        SuKien sk = service.timSuKienTheoMa(maSK);
        if (sk == null) {
            System.out.println("Không tìm thấy sự kiện có mã '" + maSK + "'.");
            return;
        }

        List<ThamGiaSuKien> danhSach = service.xemThanhVienCuaSuKien(maSK);
        System.out.println("Sự kiện: [" + sk.getMaSK() + "] " + sk.getTenSK()
                + " (" + sk.getNgayBatDau() + " -> "
                + (sk.getNgayKetThuc() != null ? sk.getNgayKetThuc() : "Chưa xác định") + ")");
        if (danhSach.isEmpty()) {
            System.out.println("Chưa có thành viên nào được ghi nhận tham gia sự kiện này.");
            return;
        }

        System.out.printf("%-15s | %-25s | %-12s | %-18s | %-20s%n",
                "MSV", "Họ và tên", "Mã ban", "Chức vụ CLB", "Vai trò tại sự kiện");
        System.out.println("-".repeat(98));
        for (ThamGiaSuKien tg : danhSach) {
            System.out.printf("%-15s | %-25s | %-12s | %-18s | %-20s%n",
                    tg.getMsv(),
                    tg.getHoVaTen() != null ? tg.getHoVaTen() : "",
                    tg.getMaBan() != null ? tg.getMaBan() : "",
                    tg.getChucVu() != null ? tg.getChucVu() : "",
                    tg.getVaiTro() != null ? tg.getVaiTro() : "");
        }
        System.out.println("Tổng số thành viên tham gia: " + danhSach.size());
    }

    private void xemLichSuSuKienThanhVien(Scanner in) {
        System.out.println("--- XEM LỊCH SỬ SỰ KIỆN CỦA MỘT THÀNH VIÊN ---");
        String msv = ConsoleUtil.readLine(in, "Nhập mã sinh viên (MSV): ");
        List<ThamGiaSuKien> lichSu = service.xemLichSuSuKienCuaThanhVien(msv);
        if (lichSu.isEmpty()) {
            System.out.println("Thành viên '" + msv + "' chưa tham gia sự kiện nào.");
            return;
        }

        System.out.println("Thành viên: [" + lichSu.get(0).getMsv() + "] " + lichSu.get(0).getHoVaTen());
        System.out.printf("%-10s | %-38s | %-12s | %-12s | %-20s%n",
                "Mã SK", "Tên sự kiện", "Ngày BĐ", "Ngày KT", "Vai trò");
        System.out.println("-".repeat(102));
        for (ThamGiaSuKien tg : lichSu) {
            System.out.printf("%-10s | %-38s | %-12s | %-12s | %-20s%n",
                    tg.getMaSK(),
                    catChuoi(tg.getTenSK(), 38),
                    tg.getNgayBatDau() != null ? tg.getNgayBatDau().toString() : "",
                    tg.getNgayKetThuc() != null ? tg.getNgayKetThuc().toString() : "Chưa XĐ",
                    tg.getVaiTro() != null ? tg.getVaiTro() : "");
        }
        System.out.println("Tổng số sự kiện đã tham gia: " + lichSu.size());
    }

    private void inBangSuKien(List<SuKien> danhSach) {
        if (danhSach.isEmpty()) {
            System.out.println("Không có sự kiện nào phù hợp.");
            return;
        }
        System.out.printf("%-10s | %-45s | %-12s | %-12s%n",
                "Mã SK", "Tên sự kiện", "Ngày bắt đầu", "Ngày kết thúc");
        System.out.println("-".repeat(88));
        for (SuKien sk : danhSach) {
            System.out.printf("%-10s | %-45s | %-12s | %-12s%n",
                    sk.getMaSK(),
                    catChuoi(sk.getTenSK(), 45),
                    sk.getNgayBatDau() != null ? sk.getNgayBatDau().toString() : "",
                    sk.getNgayKetThuc() != null ? sk.getNgayKetThuc().toString() : "Chưa xác định");
        }
        System.out.println("Tổng cộng: " + danhSach.size() + " sự kiện.");
    }

    private LocalDate docNgayBatBuoc(Scanner in, String prompt) {
        while (true) {
            String s = ConsoleUtil.readLine(in, prompt);
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                System.out.println("Định dạng ngày không hợp lệ. Vui lòng nhập theo chuẩn YYYY-MM-DD (VD: 2026-09-05).");
            }
        }
    }

    private LocalDate docNgayTuyChon(Scanner in, String prompt) {
        while (true) {
            String s = ConsoleUtil.readLine(in, prompt);
            if (s.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                System.out.println("Định dạng ngày không hợp lệ. Vui lòng nhập theo chuẩn YYYY-MM-DD hoặc nhấn Enter để bỏ qua.");
            }
        }
    }

    private LocalDate parseNgay(String s) {
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày '" + s + "' không đúng định dạng YYYY-MM-DD.");
        }
    }

    private String catChuoi(String s, int maxLen) {
        if (s == null) {
            return "";
        }
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }
}
