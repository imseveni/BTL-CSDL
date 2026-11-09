package banchuyenmon;

import common.AppModule;

import java.util.Scanner;

/**
 * Module 02: Quản lý Ban chuyên môn & Chức vụ.
 * Người phụ trách: Đông.
 * Các file trong package này đặt tên theo hậu tố: X.java (thực thể), XDao.java (truy cập DB),
 * XService.java (nghiệp vụ), XUi.java (menu console).
 */
public class BanChuyenMonModule implements AppModule {

    @Override
    public String getName() {
        return "Quản lý Ban chuyên môn & Chức vụ";
    }

    @Override
    public void run(Scanner in) {
        new BanChuyenMonUi().run(in);
    }
}
