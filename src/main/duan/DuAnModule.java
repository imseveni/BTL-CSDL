package duan;

import common.AppModule;
import common.ConsoleUtil;

import java.util.Scanner;

/**
 * Module 05: Quản lý Dự án.
 * Người phụ trách: Chiến.
 * Các file trong package này đặt tên theo hậu tố: X.java (thực thể), XDao.java (truy cập DB),
 * XService.java (nghiệp vụ), XUi.java (menu console).
 */
public class DuAnModule implements AppModule {

    @Override
    public String getName() {
        return "Quản lý Dự án";
    }

    @Override
    public void run(Scanner in) {
        // TODO(Chiến): gọi menu (DuAnUi) khi đã cài đặt
        ConsoleUtil.todo(getName());
    }
}
