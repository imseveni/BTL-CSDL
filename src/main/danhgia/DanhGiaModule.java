package danhgia;

import common.AppModule;

import java.util.Scanner;

/**
 * Module 08: Quản lý Đánh giá thành viên.
 * Người phụ trách: Đông.
 * Các file trong package này đặt tên theo hậu tố: X.java (thực thể), XDao.java (truy cập DB),
 * XService.java (nghiệp vụ), XUi.java (menu console).
 */
public class DanhGiaModule implements AppModule {

    @Override
    public String getName() {
        return "Quản lý Đánh giá thành viên";
    }

    @Override
    public void run(Scanner in) {
        new DanhGiaUi().run(in);
    }
}
