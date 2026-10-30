package muontra;

import common.ConsoleUtil;
import common.DataAccessException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/** Menu console. Nhập 0 để quay lại menu chính. */
public class PhieuMuonUi {
    private static final DateTimeFormatter NGAY =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final PhieuMuonService service;

    public PhieuMuonUi() {
        this(new PhieuMuonService());
    }

    public PhieuMuonUi(PhieuMuonService service) {
        this.service = Objects.requireNonNull(service);
    }

    public void run(Scanner in) {
        while (true) {
            System.out.println("\n===== MƯỢN - TRẢ THIẾT BỊ =====");
            System.out.println("1. Tạo phiếu mượn\n2. Thêm thiết bị vào phiếu\n3. Xem chi tiết phiếu");
            System.out.println("4. Danh sách phiếu (lọc theo trạng thái)\n5. Duyệt phiếu\n6. Từ chối phiếu");
            System.out.println("7. Ghi nhận trả một thiết bị\n8. Ghi nhận trả tất cả thiết bị của phiếu");
            System.out.println("9. Thiết bị chưa trả / quá hạn\n10. Thiết bị đã trả muộn");
            System.out.println("11. Lịch sử mượn của một thành viên\n0. Quay lại");
            try {
                int choice = Integer.parseInt(read(in, "Chọn: "));
                switch (choice) {
                    case 0 -> { return; }
                    case 1 -> {
                        String ma = read(in, "Mã phiếu mượn: ");
                        String msv = read(in, "MSV người mượn: ");
                        LocalDate muon = readDate(in, "Ngày mượn (yyyy-MM-dd, Enter = hôm nay): ", true);
                        LocalDate hen = readDate(in, "Ngày hẹn trả (yyyy-MM-dd): ", false);
                        service.taoPhieu(ma, muon, hen, read(in, "Mục đích mượn: "), msv);
                        System.out.println("Đã tạo phiếu ở trạng thái ChoDuyet. Hãy thêm thiết bị bằng mục 2.");
                    }
                    case 2 -> themThietBi(in);
                    case 3 -> xemChiTiet(in);
                    case 4 -> {
                        List<PhieuMuon> rows = service.danhSachPhieu(
                                read(in, "Trạng thái (ChoDuyet/DaDuyet/TuChoi, Enter = tất cả): "));
                        if (rows.isEmpty()) System.out.println("Không có phiếu nào.");
                        rows.forEach(p -> System.out.printf("%s | %s | mượn %s | hẹn trả %s | %s | %s%n",
                                p.maPhieuMuon(), p.msvNguoiMuon(), p.ngayMuon(), p.ngayHenTra(),
                                p.trangThaiDuyet(), display(p.mucDichMuon())));
                    }
                    case 5 -> {
                        String ma = read(in, "Mã phiếu cần duyệt: ");
                        if (confirm(in, "Duyệt phiếu " + ma + " (sẽ trừ số lượng khả dụng)?")) {
                            service.duyet(ma);
                            System.out.println("Đã duyệt phiếu, số lượng khả dụng đã được trừ.");
                        }
                    }
                    case 6 -> {
                        String ma = read(in, "Mã phiếu cần từ chối: ");
                        if (confirm(in, "Từ chối phiếu " + ma + "?")) {
                            service.tuChoi(ma);
                            System.out.println("Đã từ chối phiếu.");
                        }
                    }
                    case 7 -> {
                        String phieu = read(in, "Mã phiếu: ");
                        String thietBi = read(in, "Mã thiết bị trả: ");
                        service.traThietBi(phieu, thietBi, readDate(in, "Ngày trả (yyyy-MM-dd, Enter = hôm nay): ", true));
                        System.out.println("Đã ghi nhận trả, số lượng khả dụng đã được cộng lại.");
                    }
                    case 8 -> {
                        String phieu = read(in, "Mã phiếu: ");
                        service.traTatCa(phieu, readDate(in, "Ngày trả (yyyy-MM-dd, Enter = hôm nay): ", true));
                        System.out.println("Đã ghi nhận trả toàn bộ thiết bị còn lại của phiếu.");
                    }
                    case 9 -> showTheoDoi(service.thietBiChuaTra(), "Không có thiết bị nào đang mượn.");
                    case 10 -> showTheoDoi(service.thietBiTraMuon(), "Chưa có thiết bị nào trả muộn.");
                    case 11 -> {
                        List<LichSuMuon> rows = service.lichSuMuon(read(in, "MSV: "));
                        if (rows.isEmpty()) System.out.println("Thành viên này chưa có phiếu mượn nào.");
                        rows.forEach(l -> System.out.printf("%s | mượn %s | hẹn trả %s | %s | %s | %s | ngày trả: %s%n",
                                l.maPhieuMuon(), l.ngayMuon(), l.ngayHenTra(), l.trangThaiDuyet(),
                                l.maThietBi() == null ? "(chưa có thiết bị)"
                                        : l.maThietBi() + " - " + l.tenThietBi(),
                                l.maThietBi() == null ? "-" : "x" + l.soLuongMuon(),
                                l.ngayTraThucTe() == null ? "(chưa trả)" : l.ngayTraThucTe()));
                    }
                    default -> System.out.println("Lựa chọn không hợp lệ.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập số tương ứng với chức năng.");
            } catch (IllegalArgumentException | DataAccessException e) {
                System.out.println("Không thực hiện được: " + e.getMessage());
            } catch (java.util.NoSuchElementException e) {
                return;
            }
        }
    }

    /** Thêm nhiều thiết bị vào một phiếu; lỗi ở một dòng thì báo rồi cho nhập tiếp dòng khác. */
    private void themThietBi(Scanner in) {
        String phieu = read(in, "Mã phiếu: ");
        while (true) {
            String thietBi = read(in, "Mã thiết bị (Enter để kết thúc): ");
            if (thietBi.isBlank()) {
                return;
            }
            try {
                service.themThietBi(phieu, thietBi, readInt(in, "Số lượng mượn: "));
                System.out.println("Đã thêm " + thietBi + " vào phiếu.");
            } catch (IllegalArgumentException | DataAccessException e) {
                System.out.println("Không thực hiện được: " + e.getMessage());
            }
        }
    }

    private void xemChiTiet(Scanner in) {
        String ma = read(in, "Mã phiếu: ");
        PhieuMuon p = service.xemPhieu(ma);
        System.out.printf("Phiếu %s | người mượn %s | %s | mượn %s | hẹn trả %s | mục đích: %s%n",
                p.maPhieuMuon(), p.msvNguoiMuon(), p.trangThaiDuyet(), p.ngayMuon(), p.ngayHenTra(),
                display(p.mucDichMuon()));
        List<ChiTietMuon> rows = service.chiTiet(ma);
        if (rows.isEmpty()) System.out.println("Phiếu chưa có thiết bị nào.");
        rows.forEach(c -> System.out.printf("  %s | %s | x%d | ngày trả: %s%n", c.maThietBi(), c.tenThietBi(),
                c.soLuongMuon(), c.daTra() ? c.ngayTraThucTe() : "(chưa trả)"));
    }

    private void showTheoDoi(List<DongTheoDoi> rows, String khiRong) {
        if (rows.isEmpty()) {
            System.out.println(khiRong);
            return;
        }
        rows.forEach(d -> System.out.printf("%s | %s (%s) | %s - %s x%d | hẹn trả %s | trả %s | trễ %d ngày | %s%n",
                d.maPhieuMuon(), d.hoVaTen(), d.msvNguoiMuon(), d.maThietBi(), d.tenThietBi(), d.soLuongMuon(),
                d.ngayHenTra(), d.ngayTraThucTe() == null ? "-" : d.ngayTraThucTe(), d.soNgayTre(), d.tinhTrang()));
        System.out.println("Số dòng: " + rows.size());
    }

    private String read(Scanner in, String prompt) {
        return ConsoleUtil.readLine(in, prompt);
    }

    /** Đọc số nguyên; báo lỗi riêng để không lẫn với lỗi nhập lựa chọn menu. */
    private int readInt(Scanner in, String prompt) {
        String text = read(in, prompt);
        try {
            return Integer.parseInt(text.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + text + "\" không phải số nguyên hợp lệ.");
        }
    }

    /** Ngày ISO nghiêm ngặt (loại 2026-02-30). macDinhHomNay = true: Enter lấy hôm nay; false: bắt buộc nhập. */
    private LocalDate readDate(Scanner in, String prompt, boolean macDinhHomNay) {
        String text = read(in, prompt);
        if (text.isBlank()) {
            if (macDinhHomNay) {
                return LocalDate.now();
            }
            throw new IllegalArgumentException("Phải nhập ngày theo dạng yyyy-MM-dd.");
        }
        try {
            return LocalDate.parse(text.strip(), NGAY);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày không hợp lệ. Nhập yyyy-MM-dd, ví dụ 2026-10-25.");
        }
    }

    private String display(String value) {
        return value == null || value.isBlank() ? "(không ghi)" : value;
    }

    private boolean confirm(Scanner in, String prompt) {
        return "y".equalsIgnoreCase(read(in, prompt + " [y/N]: "));
    }
}
