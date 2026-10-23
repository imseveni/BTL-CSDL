package sukien;

import common.AppModule;
import common.ConsoleUtil;

import java.util.Scanner;

/**
 * Module 06: Quản lý Sự kiện.
 * Người phụ trách: Minh.
 * Các file trong package này đặt tên theo hậu tố: X.java (thực thể), XDao.java (truy cập DB),
 * XService.java (nghiệp vụ), XUi.java (menu console).
 */
public class SuKienModule implements AppModule {

    @Override
    public String getName() {
        return "Quản lý Sự kiện";
    }

    @Override
    public void run(Scanner in) {
        // TODO(Minh): gọi menu (SuKienUi) khi đã cài đặt
        ConsoleUtil.todo(getName());
    }
}
