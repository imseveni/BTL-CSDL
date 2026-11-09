package danhgia;

import common.ConsoleUtil;
import common.DataAccessException;
import java.util.Objects;
import java.util.Scanner;

/** Menu console. Nhập 0 để quay lại menu chính. */
public class DanhGiaUi {
    private final DanhGiaService service;

    public DanhGiaUi() {
        this(new DanhGiaService());
    }

    public DanhGiaUi(DanhGiaService service) {
        this.service = Objects.requireNonNull(service);
    }

    public void run(Scanner in) {
        while (true) {
            System.out.println("\n===== ĐÁNH GIÁ THÀNH VIÊN =====");
            System.out.println("1. Tạo đánh giá\n2. Cập nhật đánh giá\n3. Lịch sử của thành viên\n4. Thống kê theo loại\n0. Quay lại");
            try {
                int choice = Integer.parseInt(read(in, "Chọn: "));
                switch (choice) {
                    case 0 -> { return; }
                    case 1 -> { service.them(readDanhGia(in)); success(); }
                    case 2 -> { service.sua(readDanhGia(in)); success(); }
                    case 3 -> {
                        var rows = service.lichSu(read(in, "MSV: "));
                        if (rows.isEmpty()) System.out.println("Thành viên chưa có đánh giá.");
                        rows.forEach(dg -> System.out.printf("%s | %s | %s | %s | %s | %s%n", dg.maGhiNhan(),
                                dg.msv(), dg.ngayQuyetDinh(), display(dg.loaiDanhGia()), display(dg.hinhThuc()), display(dg.lyDo())));
                    }
                    case 4 -> {
                        var rows = service.thongKe();
                        if (rows.isEmpty()) System.out.println("Chưa có đánh giá.");
                        rows.forEach(tk -> System.out.printf("%s | %d đánh giá | %d thành viên%n",
                                tk.loaiDanhGia(), tk.soDanhGia(), tk.soThanhVien()));
                    }
                    default -> System.out.println("Lựa chọn không hợp lệ.");
                }
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println("Ngày không hợp lệ. Nhập yyyy-MM-dd, ví dụ 2026-10-25.");
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập số tương ứng với chức năng.");
            } catch (IllegalArgumentException | DataAccessException e) {
                System.out.println("Không thực hiện được: " + e.getMessage());
            } catch (java.util.NoSuchElementException e) {
                return;
            }
        }
    }

    private DanhGia readDanhGia(Scanner in) {
        String ma = read(in, "Mã ghi nhận: ");
        String msv = read(in, "MSV: ");
        String loai = read(in, "Loại đánh giá (gợi ý: Khen thưởng, Kỷ luật): ");
        String hinhThuc = read(in, "Hình thức: ");
        String lyDo = read(in, "Lý do: ");
        var ngay = java.time.LocalDate.parse(read(in, "Ngày quyết định (yyyy-MM-dd): "),
                java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        return new DanhGia(ma, loai, hinhThuc, lyDo, ngay, msv);
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

}
