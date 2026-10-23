package thanhvien;

import common.AppModule;
import common.ConsoleUtil;

import java.util.Scanner;

/**
 * Module 01: Quản lý Thành viên.
 * Người phụ trách: Tú.
 * Các file trong package này đặt tên theo hậu tố: X.java (thực thể), XDao.java (truy cập DB),
 * XService.java (nghiệp vụ), XUi.java (menu console).
 */
public class ThanhVienModule implements AppModule {

    @Override
    public String getName() {
        return "Quản lý Thành viên";
    }

    @Override
    public void run(Scanner in) {
        // TODO(Tú): gọi menu (ThanhVienUi) khi đã cài đặt
        ConsoleUtil.todo(getName());
    }
}
