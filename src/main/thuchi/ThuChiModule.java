package thuchi;

import common.AppModule;
import common.ConsoleUtil;

import java.util.Scanner;

/**
 * Module 07: Quản lý Thu – Chi.
 * Người phụ trách: Minh.
 * Các file trong package này đặt tên theo hậu tố: X.java (thực thể), XDao.java (truy cập DB),
 * XService.java (nghiệp vụ), XUi.java (menu console).
 */
public class ThuChiModule implements AppModule {

    @Override
    public String getName() {
        return "Quản lý Thu – Chi";
    }

    @Override
    public void run(Scanner in) {
        // TODO(Minh): gọi menu (ThuChiUi) khi đã cài đặt
        ConsoleUtil.todo(getName());
    }
}
