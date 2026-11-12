package app;

import common.AppModule;
import common.ConsoleUtil;
import thanhvien.ThanhVienModule;
import banchuyenmon.BanChuyenMonModule;
import quyenhan.QuyenHanModule;
import sukien.SuKienModule;
import thuchi.ThuChiModule;
import danhgia.DanhGiaModule;
import cosovatchat.CoSoVatChatModule;
import muontra.MuonTraModule;
import baocaomuontra.BaoCaoMuonTraModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Menu chính. Chỉ Tú sửa file này (đăng ký module).
 * Menu dòng lệnh dành cho người phát triển, chạy với quyền quản trị (không qua màn hình đăng nhập).
 * Giao diện chính của ứng dụng là Swing (xem SwingApp); module 04 và 05 chỉ còn mã Dao/Service, không có menu.
 */
public class MainMenu {

    private final List<AppModule> modules = new ArrayList<>();

    public MainMenu() {
        modules.add(new ThanhVienModule());                                              // 01
        modules.add(new BanChuyenMonModule());                                           // 02
        modules.add(new QuyenHanModule());                                               // 03
        modules.add(new SuKienModule());                                                 // 06
        modules.add(new ThuChiModule());                                                 // 07
        modules.add(new DanhGiaModule());                                                // 08
        modules.add(new CoSoVatChatModule());                                            // 09
        modules.add(new MuonTraModule());                                                // 10
        modules.add(new BaoCaoMuonTraModule());                                          // 11
    }

    public List<AppModule> getModules() {
        return modules;
    }

    public void run(Scanner in) {
        while (true) {
            System.out.println();
            System.out.println("===== QUẢN LÝ CÂU LẠC BỘ =====");
            for (int i = 0; i < modules.size(); i++) {
                System.out.println((i + 1) + ". " + modules.get(i).getName());
            }
            System.out.println("0. Thoát");
            int choice = ConsoleUtil.readInt(in, "Chọn: ");
            if (choice == 0) {
                System.out.println("Tạm biệt!");
                return;
            }
            if (choice < 1 || choice > modules.size()) {
                System.out.println("Lựa chọn không hợp lệ.");
                continue;
            }
            try {
                modules.get(choice - 1).run(in);
            } catch (RuntimeException e) {
                // Một module lỗi (vd mất kết nối MySQL) không được làm sập cả chương trình.
                System.out.println("Module gặp lỗi: " + e.getMessage());
            }
        }
    }
}
