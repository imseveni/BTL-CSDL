package muontra;

import common.AppModule;
import common.ConsoleUtil;

import java.util.Scanner;

/**
 * Module 10: Quản lý Mượn – Trả cơ sở vật chất.
 * Người phụ trách: Trường.
 * Các file trong package này đặt tên theo hậu tố: X.java (thực thể), XDao.java (truy cập DB),
 * XService.java (nghiệp vụ), XUi.java (menu console).
 */
public class MuonTraModule implements AppModule {

    @Override
    public String getName() {
        return "Quản lý Mượn – Trả cơ sở vật chất";
    }

    @Override
    public void run(Scanner in) {
        // TODO(Trường): gọi menu (MuonTraUi) khi đã cài đặt
        ConsoleUtil.todo(getName());
    }
}
