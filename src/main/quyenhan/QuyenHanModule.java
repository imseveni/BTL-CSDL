package quyenhan;

import common.AppModule;

import java.util.Scanner;

/**
 * Module 03: Quản lý Quyền hạn.
 * Người phụ trách: Đông.
 * Các file trong package này đặt tên theo hậu tố: X.java (thực thể), XDao.java (truy cập DB),
 * XService.java (nghiệp vụ), XUi.java (menu console).
 */
public class QuyenHanModule implements AppModule {

    @Override
    public String getName() {
        return "Quản lý Quyền hạn";
    }

    @Override
    public void run(Scanner in) {
        new QuyenHanUi().run(in);
    }
}
