package app;

import common.AppModule;
import common.ConsoleUtil;
import thanhvien.ThanhVienModule;
import banchuyenmon.BanChuyenMonModule;
import quyenhan.QuyenHanModule;
// import khoahoc.KhoaHocModule;
// import duan.DuAnModule;
import sukien.SuKienModule;
import thuchi.ThuChiModule;
import danhgia.DanhGiaModule;
import cosovatchat.CoSoVatChatModule;
import muontra.MuonTraModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Menu chính. Chỉ Tú sửa file này (đăng ký module). */
public class MainMenu {

    private final List<AppModule> modules = new ArrayList<>();

    public MainMenu() {
        modules.add(new ThanhVienModule());
        modules.add(new BanChuyenMonModule());
        modules.add(new QuyenHanModule());
        // modules.add(new KhoaHocModule());
        // modules.add(new DuAnModule());
        modules.add(new SuKienModule());
        modules.add(new ThuChiModule());
        modules.add(new DanhGiaModule());
        modules.add(new CoSoVatChatModule());
        modules.add(new MuonTraModule());
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
            modules.get(choice - 1).run(in);
        }
    }
}
