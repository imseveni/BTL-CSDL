package banchuyenmon;

import common.ConsoleUtil;
import common.DataAccessException;
import java.util.Objects;
import java.util.Scanner;

/** Menu console. Nhập 0 để quay lại menu chính. */
public class BanChuyenMonUi {
    private final BanChuyenMonService service;

    public BanChuyenMonUi() {
        this(new BanChuyenMonService());
    }

    public BanChuyenMonUi(BanChuyenMonService service) {
        this.service = Objects.requireNonNull(service);
    }

    public void run(Scanner in) {
        while (true) {
            System.out.println("\n===== BAN CHUYÊN MÔN & CHỨC VỤ =====");
            System.out.println("1. Danh sách ban và số thành viên\n2. Thêm ban\n3. Sửa tên ban\n4. Xóa ban");
            System.out.println("5. Thành viên theo ban\n6. Phân/chuyển ban\n7. Gán/đổi chức vụ\n0. Quay lại");
            try {
                int choice = Integer.parseInt(read(in, "Chọn: "));
                switch (choice) {
                    case 0 -> { return; }
                    case 1 -> {
                        var rows = service.danhSach();
                        if (rows.isEmpty()) System.out.println("Chưa có ban chuyên môn.");
                        rows.forEach(b -> System.out.printf("%s | %s | %d thành viên%n", b.maBan(), b.tenBan(), b.soThanhVien()));
                    }
                    case 2 -> { service.them(readBan(in)); success(); }
                    case 3 -> { service.sua(readBan(in)); success(); }
                    case 4 -> {
                        String ma = read(in, "Mã ban cần xóa: ");
                        if (confirm(in, "Xóa ban " + ma + "?")) { service.xoa(ma); success(); }
                    }
                    case 5 -> {
                        var rows = service.thanhVienTheoBan(read(in, "Mã ban: "));
                        rows.forEach(tv -> System.out.printf("%s | %s | %s | %s%n",
                                tv.msv(), tv.hoVaTen(), display(tv.chucVu()), display(tv.trangThaiHD())));
                        System.out.println("Số thành viên: " + rows.size());
                    }
                    case 6 -> {
                        service.chuyenBan(read(in, "MSV: "), read(in, "Mã ban mới: "));
                        System.out.println("Đã chuyển ban. Có thể đổi chức vụ bằng mục 7.");
                    }
                    case 7 -> { service.doiChucVu(read(in, "MSV: "), read(in, "Chức vụ mới: ")); success(); }
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

    private BanChuyenMon readBan(Scanner in) {
        return new BanChuyenMon(read(in, "Mã ban: "), read(in, "Tên ban: "));
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
