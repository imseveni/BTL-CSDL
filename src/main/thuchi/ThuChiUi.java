package thuchi;

import common.ConsoleUtil;
import common.DataAccessException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Giao diện console (UI) cho Module 07 – Quản lý Thu – Chi.
 * Chỉ làm nhiệm vụ nhập/xuất và gọi ThuChiService.
 */
public class ThuChiUi {

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    private final ThuChiService service;

    public ThuChiUi() {
        this.service = new ThuChiService();
    }

    public void run(Scanner in) {
        while (true) {
            System.out.println();
            System.out.println("===== MODULE 07: QUẢN LÝ THU – CHI =====");
            System.out.println("1. Xem danh sách tất cả giao dịch Thu – Chi");
            System.out.println("2. Thêm mới giao dịch (Thu / Chi)");
            System.out.println("3. Cập nhật thông tin giao dịch");
            System.out.println("4. Xóa giao dịch");
            System.out.println("5. Tra cứu giao dịch theo thành viên thực hiện (MSV)");
            System.out.println("6. Tra cứu giao dịch theo sự kiện (MaSK)");
            System.out.println("7. Lọc giao dịch theo phân loại (Thu / Chi)");
            System.out.println("8. Thống kê Thu – Chi theo từng sự kiện");
            System.out.println("9. Xem chi tiết một giao dịch theo mã");
            System.out.println("0. Quay lại menu chính");

            int choice = ConsoleUtil.readInt(in, "Chọn chức năng: ");
            try {
                switch (choice) {
                    case 1 -> hienThiTatCaGiaoDich();
                    case 2 -> themGiaoDich(in);
                    case 3 -> capNhatGiaoDich(in);
                    case 4 -> xoaGiaoDich(in);
                    case 5 -> traCuuTheoThanhVien(in);
                    case 6 -> traCuuTheoSuKien(in);
                    case 7 -> locTheoLoaiGiaoDich(in);
                    case 8 -> thongKeTheoTungSuKien();
                    case 9 -> xemChiTietGiaoDich(in);
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

    private void hienThiTatCaGiaoDich() {
        List<ThuChi> danhSach = service.layTatCaGiaoDich();
        inBangGiaoDich(danhSach);
    }

    private void themGiaoDich(Scanner in) {
        System.out.println("--- THÊM MỚI GIAO DỊCH THU – CHI ---");
        String maGD = ConsoleUtil.readLine(in, "Nhập mã giao dịch (VD: GD07): ");
        String loaiGD = docLoaiGiaoDich(in, "Nhập loại giao dịch (Thu / Chi): ");
        String mucDich = ConsoleUtil.readLine(in, "Nhập mục đích giao dịch: ");
        BigDecimal soTien = docSoTien(in, "Nhập số tiền (VNĐ, > 0): ");
        LocalDateTime thoiGian = docThoiGian(in,
                "Nhập thời gian (YYYY-MM-DD HH:mm, Enter để lấy thời điểm hiện tại): ",
                LocalDateTime.now());
        List<String> phuongThuc = docDanhSachPhuongThuc(in,
                "Nhập phương thức thanh toán (ngăn cách bởi dấu phẩy nếu có nhiều, VD: Tiền mặt, Chuyển khoản): ");
        String minhChung = ConsoleUtil.readLine(in, "Nhập minh chứng (tên file/link hóa đơn, Enter để bỏ qua): ");
        String msv = ConsoleUtil.readLine(in, "Nhập MSV thành viên thực hiện (Enter nếu không gắn thành viên): ");
        String maSK = ConsoleUtil.readLine(in, "Nhập mã sự kiện liên kết (MaSK, Enter nếu thu/chi chung CLB): ");

        ThuChi gd = new ThuChi(
                maGD,
                loaiGD,
                mucDich,
                soTien,
                thoiGian,
                minhChung.isEmpty() ? null : minhChung,
                msv.isEmpty() ? null : msv,
                maSK.isEmpty() ? null : maSK,
                phuongThuc
        );
        service.themGiaoDich(gd);
        System.out.println("Đã thêm giao dịch '" + gd.getMaGD() + "' thành công!");
    }

    private void capNhatGiaoDich(Scanner in) {
        System.out.println("--- CẬP NHẬT GIAO DỊCH THU – CHI ---");
        String maGD = ConsoleUtil.readLine(in, "Nhập mã giao dịch cần cập nhật: ");
        ThuChi hienTai = service.timGiaoDichTheoMa(maGD);
        if (hienTai == null) {
            System.out.println("Không tìm thấy giao dịch có mã '" + maGD + "'.");
            return;
        }
        inChiTietMotGiaoDich(hienTai);

        String loaiMoi = ConsoleUtil.readLine(in,
                "Loại giao dịch mới (Thu/Chi, Enter để giữ '" + hienTai.getLoaiGD() + "'): ");
        if (loaiMoi.isEmpty()) {
            loaiMoi = hienTai.getLoaiGD();
        }

        String mucDichMoi = ConsoleUtil.readLine(in, "Mục đích mới (Enter để giữ nguyên): ");
        if (mucDichMoi.isEmpty()) {
            mucDichMoi = hienTai.getMucDich();
        }

        String sSoTien = ConsoleUtil.readLine(in,
                "Số tiền mới (Enter để giữ " + formatTien(hienTai.getSoTien()) + "): ");
        BigDecimal soTienMoi = sSoTien.isEmpty() ? hienTai.getSoTien() : parseSoTien(sSoTien);

        LocalDateTime thoiGianMoi = docThoiGian(in,
                "Thời gian mới (YYYY-MM-DD HH:mm, Enter để giữ nguyên): ",
                hienTai.getThoiGian());

        String sPhuongThuc = ConsoleUtil.readLine(in,
                "Phương thức thanh toán mới (ngăn cách dấu phẩy, Enter để giữ ["
                + hienTai.getChuoiPhuongThucThanhToan() + "]): ");
        List<String> phuongThucMoi = sPhuongThuc.isEmpty()
                ? hienTai.getPhuongThucThanhToan()
                : tachPhuongThuc(sPhuongThuc);

        String minhChungMoi = docTruongTuyChonCapNhat(in,
                "Minh chứng mới (nhập 'NULL' để xóa, Enter để giữ nguyên): ",
                hienTai.getMinhChung());

        String msvMoi = docTruongTuyChonCapNhat(in,
                "MSV thành viên thực hiện mới (nhập 'NULL' để bỏ gắn, Enter để giữ nguyên): ",
                hienTai.getMsv());

        String maSKMoi = docTruongTuyChonCapNhat(in,
                "Mã sự kiện liên kết mới (nhập 'NULL' để bỏ liên kết, Enter để giữ nguyên): ",
                hienTai.getMaSK());

        ThuChi capNhat = new ThuChi(
                hienTai.getMaGD(),
                loaiMoi,
                mucDichMoi,
                soTienMoi,
                thoiGianMoi,
                minhChungMoi,
                msvMoi,
                maSKMoi,
                phuongThucMoi
        );
        service.capNhatGiaoDich(capNhat);
        System.out.println("Đã cập nhật giao dịch '" + capNhat.getMaGD() + "' thành công!");
    }

    private void xoaGiaoDich(Scanner in) {
        System.out.println("--- XÓA GIAO DỊCH THU – CHI ---");
        String maGD = ConsoleUtil.readLine(in, "Nhập mã giao dịch cần xóa: ");
        String xacNhan = ConsoleUtil.readLine(in, "Bạn có chắc chắn muốn xóa giao dịch '" + maGD + "'? (y/n): ");
        if (!xacNhan.equalsIgnoreCase("y")) {
            System.out.println("Đã hủy thao tác xóa.");
            return;
        }
        service.xoaGiaoDich(maGD);
        System.out.println("Đã xóa giao dịch '" + maGD + "' thành công!");
    }

    private void traCuuTheoThanhVien(Scanner in) {
        System.out.println("--- TRA CỨU GIAO DỊCH THEO THÀNH VIÊN ---");
        String msv = ConsoleUtil.readLine(in, "Nhập MSV thành viên thực hiện: ");
        List<ThuChi> ketQua = service.traCuuTheoThanhVien(msv);
        inBangGiaoDich(ketQua);
    }

    private void traCuuTheoSuKien(Scanner in) {
        System.out.println("--- TRA CỨU GIAO DỊCH THEO SỰ KIỆN ---");
        String maSK = ConsoleUtil.readLine(in, "Nhập mã sự kiện (MaSK): ");
        List<ThuChi> ketQua = service.traCuuTheoSuKien(maSK);
        inBangGiaoDich(ketQua);
    }

    private void locTheoLoaiGiaoDich(Scanner in) {
        System.out.println("--- LỌC GIAO DỊCH THEO PHÂN LOẠI THU / CHI ---");
        String loai = docLoaiGiaoDich(in, "Nhập loại giao dịch cần lọc (Thu / Chi): ");
        List<ThuChi> ketQua = service.locTheoLoai(loai);
        inBangGiaoDich(ketQua);
    }

    private void thongKeTheoTungSuKien() {
        System.out.println("--- THỐNG KÊ THU – CHI THEO TỪNG SỰ KIỆN ---");
        List<ThongKeThuChiSuKien> danhSach = service.thongKeTheoTungSuKien();
        if (danhSach.isEmpty()) {
            System.out.println("Chưa có dữ liệu sự kiện để thống kê.");
            return;
        }

        System.out.printf("%-10s | %-38s | %8s | %16s | %16s | %16s%n",
                "Mã SK", "Tên sự kiện", "Số GD", "Tổng Thu (VNĐ)", "Tổng Chi (VNĐ)", "Chênh lệch (VNĐ)");
        System.out.println("-".repeat(118));

        BigDecimal tongThuAll = BigDecimal.ZERO;
        BigDecimal tongChiAll = BigDecimal.ZERO;
        int tongGDAll = 0;

        for (ThongKeThuChiSuKien tk : danhSach) {
            System.out.printf("%-10s | %-38s | %8d | %16s | %16s | %16s%n",
                    tk.getMaSK(),
                    catChuoi(tk.getTenSK(), 38),
                    tk.getSoGiaoDich(),
                    formatTien(tk.getTongThu()),
                    formatTien(tk.getTongChi()),
                    formatTien(tk.getChenhLech()));
            tongGDAll += tk.getSoGiaoDich();
            tongThuAll = tongThuAll.add(tk.getTongThu());
            tongChiAll = tongChiAll.add(tk.getTongChi());
        }
        System.out.println("-".repeat(118));
        System.out.printf("%-10s | %-38s | %8d | %16s | %16s | %16s%n",
                "TỔNG", "Tất cả sự kiện",
                tongGDAll,
                formatTien(tongThuAll),
                formatTien(tongChiAll),
                formatTien(tongThuAll.subtract(tongChiAll)));
    }

    private void xemChiTietGiaoDich(Scanner in) {
        String maGD = ConsoleUtil.readLine(in, "Nhập mã giao dịch cần xem chi tiết: ");
        ThuChi gd = service.timGiaoDichTheoMa(maGD);
        if (gd == null) {
            System.out.println("Không tìm thấy giao dịch có mã '" + maGD + "'.");
            return;
        }
        inChiTietMotGiaoDich(gd);
    }

    private void inBangGiaoDich(List<ThuChi> danhSach) {
        if (danhSach.isEmpty()) {
            System.out.println("Không có giao dịch nào phù hợp.");
            return;
        }
        System.out.printf("%-8s | %-5s | %15s | %-16s | %-8s | %-12s | %-24s | %-28s%n",
                "Mã GD", "Loại", "Số tiền (VNĐ)", "Thời gian", "Mã SK", "MSV", "Phương thức TT", "Mục đích");
        System.out.println("-".repeat(132));
        for (ThuChi gd : danhSach) {
            String thoiGianStr = gd.getThoiGian() != null ? gd.getThoiGian().format(DT_FORMATTER) : "";
            System.out.printf("%-8s | %-5s | %15s | %-16s | %-8s | %-12s | %-24s | %-28s%n",
                    gd.getMaGD(),
                    gd.getLoaiGD(),
                    formatTien(gd.getSoTien()),
                    thoiGianStr,
                    gd.getMaSK() != null ? gd.getMaSK() : "(Chung)",
                    gd.getMsv() != null ? gd.getMsv() : "(Trống)",
                    catChuoi(gd.getChuoiPhuongThucThanhToan(), 24),
                    catChuoi(gd.getMucDich(), 28));
        }
        System.out.println("Tổng số: " + danhSach.size() + " giao dịch.");
    }

    private void inChiTietMotGiaoDich(ThuChi gd) {
        System.out.println("--------------------------------------------------");
        System.out.println("Mã giao dịch       : " + gd.getMaGD());
        System.out.println("Phân loại          : " + gd.getLoaiGD());
        System.out.println("Số tiền            : " + formatTien(gd.getSoTien()) + " VNĐ");
        System.out.println("Mục đích           : " + gd.getMucDich());
        System.out.println("Thời gian          : "
                + (gd.getThoiGian() != null ? gd.getThoiGian().format(DT_FORMATTER) : ""));
        System.out.println("Phương thức TT     : " + gd.getChuoiPhuongThucThanhToan());
        System.out.println("Minh chứng         : " + (gd.getMinhChung() != null ? gd.getMinhChung() : "(Không có)"));
        System.out.println("Thành viên thực hiện: "
                + (gd.getMsv() != null
                        ? gd.getMsv() + (gd.getHoTenThanhVien() != null ? " - " + gd.getHoTenThanhVien() : "")
                        : "(Chưa ghi nhận)"));
        System.out.println("Sự kiện liên kết   : "
                + (gd.getMaSK() != null
                        ? gd.getMaSK() + (gd.getTenSuKien() != null ? " - " + gd.getTenSuKien() : "")
                        : "(Hoạt động chung CLB)"));
        System.out.println("--------------------------------------------------");
    }

    private String docLoaiGiaoDich(Scanner in, String prompt) {
        while (true) {
            String s = ConsoleUtil.readLine(in, prompt);
            if (s.equalsIgnoreCase("Thu")) {
                return "Thu";
            }
            if (s.equalsIgnoreCase("Chi")) {
                return "Chi";
            }
            System.out.println("Vui lòng chỉ nhập 'Thu' hoặc 'Chi'.");
        }
    }

    private BigDecimal docSoTien(Scanner in, String prompt) {
        while (true) {
            String s = ConsoleUtil.readLine(in, prompt);
            try {
                BigDecimal val = parseSoTien(s);
                if (val.compareTo(BigDecimal.ZERO) > 0) {
                    return val;
                }
                System.out.println("Số tiền phải lớn hơn 0.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private BigDecimal parseSoTien(String s) {
        try {
            String clean = s.replace(",", "").replace("_", "").trim();
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Số tiền '" + s + "' không hợp lệ.");
        }
    }

    private LocalDateTime docThoiGian(Scanner in, String prompt, LocalDateTime macDinh) {
        while (true) {
            String s = ConsoleUtil.readLine(in, prompt);
            if (s.isEmpty()) {
                return macDinh;
            }
            try {
                if (s.length() == 10) {
                    return LocalDateTime.parse(s + " 00:00", DT_FORMATTER);
                }
                return LocalDateTime.parse(s.replace("T", " "), DT_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Định dạng thời gian không hợp lệ. Vui lòng nhập YYYY-MM-DD HH:mm (VD: 2026-09-05 14:30).");
            }
        }
    }

    private List<String> docDanhSachPhuongThuc(Scanner in, String prompt) {
        while (true) {
            String s = ConsoleUtil.readLine(in, prompt);
            List<String> ds = tachPhuongThuc(s);
            if (!ds.isEmpty()) {
                return ds;
            }
            System.out.println("Vui lòng nhập ít nhất 1 phương thức thanh toán.");
        }
    }

    private List<String> tachPhuongThuc(String input) {
        List<String> ds = new ArrayList<>();
        if (input == null || input.isBlank()) {
            return ds;
        }
        for (String part : input.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                ds.add(trimmed);
            }
        }
        return ds;
    }

    private String docTruongTuyChonCapNhat(Scanner in, String prompt, String giaTriCu) {
        String s = ConsoleUtil.readLine(in, prompt);
        if (s.isEmpty()) {
            return giaTriCu;
        }
        if (s.equalsIgnoreCase("NULL")) {
            return null;
        }
        return s;
    }

    private String formatTien(BigDecimal soTien) {
        if (soTien == null) {
            return "0";
        }
        return MONEY_FORMAT.format(soTien);
    }

    private String catChuoi(String s, int maxLen) {
        if (s == null) {
            return "";
        }
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }
}
