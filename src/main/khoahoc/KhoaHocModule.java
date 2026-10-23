package khoahoc;

import common.AppModule;
import common.ConsoleUtil;

import java.util.Scanner;

/**
 * Module 04: Quản lý Khóa học.
 * Người phụ trách: Chiến.
 * Các file trong package này đặt tên theo hậu tố: X.java (thực thể), XDao.java (truy cập DB),
 * XService.java (nghiệp vụ), XUi.java (menu console).
 */
public class KhoaHocModule implements AppModule {

    @Override
    public String getName() {
        return "Quản lý Khóa học";
    }

    @Override
    public void run(Scanner in) {
        // TODO(Chiến): gọi menu (KhoaHocUi) khi đã cài đặt
        ConsoleUtil.todo(getName());
    }
}
