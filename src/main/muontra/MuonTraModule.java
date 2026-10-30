package muontra;

import common.AppModule;

import java.util.Scanner;

/**
 * Module 10: Quản lý Mượn - Trả thiết bị.
 * Người phụ trách: Trường.
 * Các file trong package này đặt tên theo hậu tố: X.java (thực thể), XDao.java (truy cập DB),
 * XService.java (nghiệp vụ), XUi.java (menu console).
 */
public class MuonTraModule implements AppModule {

    @Override
    public String getName() {
        return "Quản lý Mượn - Trả thiết bị";
    }

    @Override
    public void run(Scanner in) {
        new PhieuMuonUi().run(in);
    }
}
