package cosovatchat;

import common.ConsoleUtil;
import common.DataAccessException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/** Menu console. Nhập 0 để quay lại menu chính. */
public class CoSoVatChatUi {
    private final CoSoVatChatService service;

    public CoSoVatChatUi() {
        this(new CoSoVatChatService());
    }

    public CoSoVatChatUi(CoSoVatChatService service) {
        this.service = Objects.requireNonNull(service);
    }

    public void run(Scanner in) {
        while (true) {
            System.out.println("\n===== CƠ SỞ VẬT CHẤT =====");
            System.out.println("1. Danh sách thiết bị và số lượng khả dụng\n2. Tìm thiết bị (mã hoặc tên)");
            System.out.println("3. Thêm thiết bị\n4. Sửa thiết bị (tên, tổng số lượng)");
            System.out.println("5. Gán/đổi người quản lý\n6. Kiểm tra số lượng khả dụng trước khi cho mượn\n0. Quay lại");
            try {
                int choice = Integer.parseInt(read(in, "Chọn: "));
                switch (choice) {
                    case 0 -> { return; }
                    case 1 -> show(service.danhSach());
                    case 2 -> show(service.timKiem(read(in, "Từ khóa (Enter = tất cả): ")));
                    case 3 -> {
                        String ma = read(in, "Mã thiết bị: ");
                        String ten = read(in, "Tên thiết bị: ");
                        int tong = readInt(in, "Tổng số lượng: ");
                        String msv = read(in, "MSV người quản lý (Enter nếu chưa có): ");
                        service.themMoi(ma, ten, tong, null, msv);
                        success();
                    }
                    case 4 -> {
                        String ma = read(in, "Mã thiết bị cần sửa: ");
                        CoSoVatChat cu = service.xemChiTiet(ma);
                        String ten = read(in, "Tên mới (Enter giữ \"" + cu.tenThietBi() + "\"): ");
                        String tongText = read(in, "Tổng số lượng mới (Enter giữ " + cu.tongSoLuong() + "): ");
                        service.capNhat(ma, ten.isBlank() ? cu.tenThietBi() : ten,
                                tongText.isBlank() ? cu.tongSoLuong() : toInt(tongText));
                        System.out.println("Thực hiện thành công. Số lượng khả dụng được điều chỉnh theo số đang mượn.");
                    }
                    case 5 -> {
                        service.ganNguoiQuanLy(read(in, "Mã thiết bị: "),
                                read(in, "MSV người quản lý mới (Enter để bỏ người quản lý): "));
                        success();
                    }
                    case 6 -> {
                        String ma = read(in, "Mã thiết bị: ");
                        int soLuong = readInt(in, "Số lượng muốn mượn: ");
                        boolean du = service.kiemTraKhaDung(ma, soLuong);
                        int conLai = service.xemChiTiet(ma).soLuongKhaDung();
                        System.out.println(du
                                ? "ĐỦ: còn " + conLai + ", có thể cho mượn " + soLuong + "."
                                : "KHÔNG ĐỦ: chỉ còn " + conLai + ", không thể cho mượn " + soLuong + ".");
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

    private void show(List<CoSoVatChat> rows) {
        if (rows.isEmpty()) {
            System.out.println("Không có thiết bị nào.");
            return;
        }
        rows.forEach(t -> System.out.printf("%s | %s | tổng %d | khả dụng %d | đang mượn %d | quản lý: %s%n",
                t.maThietBi(), t.tenThietBi(), t.tongSoLuong(), t.soLuongKhaDung(), t.soLuongDangMuon(),
                t.msvQuanLy() == null ? "(chưa có)" : t.msvQuanLy() + " - " + t.tenNguoiQuanLy()));
        System.out.println("Số thiết bị: " + rows.size());
    }

    private String read(Scanner in, String prompt) {
        return ConsoleUtil.readLine(in, prompt);
    }

    /** Đọc số nguyên; báo lỗi riêng để không lẫn với lỗi nhập lựa chọn menu. */
    private int readInt(Scanner in, String prompt) {
        return toInt(read(in, prompt));
    }

    private int toInt(String text) {
        try {
            return Integer.parseInt(text.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + text + "\" không phải số nguyên hợp lệ.");
        }
    }

    private void success() {
        System.out.println("Thực hiện thành công.");
    }
}
