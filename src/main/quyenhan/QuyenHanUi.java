package quyenhan;

import common.ConsoleUtil;
import common.DataAccessException;
import java.util.Objects;
import java.util.Scanner;

/** Menu console. Nhập 0 để quay lại menu chính. */
public class QuyenHanUi {
    private final QuyenHanService service;

    public QuyenHanUi() {
        this(new QuyenHanService());
    }

    public QuyenHanUi(QuyenHanService service) {
        this.service = Objects.requireNonNull(service);
    }

    public void run(Scanner in) {
        while (true) {
            System.out.println("\n===== QUYỀN HẠN =====");
            System.out.println("1. Danh sách quyền\n2. Thêm quyền\n3. Sửa quyền\n4. Xóa quyền");
            System.out.println("5. Cấp quyền\n6. Thu hồi quyền\n7. Quyền của thành viên\n8. Thành viên có quyền\n0. Quay lại");
            try {
                int choice = Integer.parseInt(read(in, "Chọn: "));
                switch (choice) {
                    case 0 -> { return; }
                    case 1 -> printQuyen(service.danhSach());
                    case 2 -> { service.them(readQuyen(in)); success(); }
                    case 3 -> { service.sua(readQuyen(in)); success(); }
                    case 4 -> {
                        String ma = read(in, "Mã quyền cần xóa: ");
                        if (confirm(in, "Xóa quyền " + ma + " và thu hồi quyền này ở mọi thành viên?")) {
                            service.xoa(ma); success();
                        }
                    }
                    case 5 -> { service.capQuyen(read(in, "MSV: "), read(in, "Mã quyền: ")); success(); }
                    case 6 -> { service.thuHoiQuyen(read(in, "MSV: "), read(in, "Mã quyền: ")); success(); }
                    case 7 -> printQuyen(service.quyenCuaThanhVien(read(in, "MSV: ")));
                    case 8 -> {
                        var rows = service.thanhVienCoQuyen(read(in, "Mã quyền: "));
                        rows.forEach(tv -> System.out.printf("%s | %s | %s | %s%n",
                                tv.msv(), tv.hoVaTen(), tv.maBan(), display(tv.chucVu())));
                        System.out.println("Số thành viên: " + rows.size());
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

    private QuyenHan readQuyen(Scanner in) {
        return new QuyenHan(read(in, "Mã quyền: "), read(in, "Tên quyền: "), read(in, "Mô tả (có thể trống): "));
    }

    private void printQuyen(java.util.List<QuyenHan> rows) {
        if (rows.isEmpty()) System.out.println("Chưa có quyền hạn.");
        rows.forEach(q -> System.out.printf("%s | %s | %s%n", q.maQuyenHan(), q.tenQuyenHan(), display(q.moTaChiTiet())));
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
