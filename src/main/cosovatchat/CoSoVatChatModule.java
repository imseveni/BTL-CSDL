package cosovatchat;

import common.AppModule;
import common.ConsoleUtil;

import java.util.Scanner;

/**
 * Module 09: Quản lý Cơ sở vật chất.
 * Người phụ trách: Trường.
 * Các file trong package này đặt tên theo hậu tố: X.java (thực thể), XDao.java (truy cập DB),
 * XService.java (nghiệp vụ), XUi.java (menu console).
 */
public class CoSoVatChatModule implements AppModule {

    @Override
    public String getName() {
        return "Quản lý Cơ sở vật chất";
    }

    @Override
    public void run(Scanner in) {
        // TODO(Trường): gọi menu (CoSoVatChatUi) khi đã cài đặt
        ConsoleUtil.todo(getName());
    }
}
