package baocaomuontra;

import common.AppModule;
import java.util.Scanner;

/**
 * Module Báo cáo mượn – trả cơ sở vật chất (Borrowing and Returning Reports).
 */
public class BaoCaoMuonTraModule implements AppModule {

    private final BaoCaoMuonTraUi ui = new BaoCaoMuonTraUi();

    @Override
    public String getName() {
        return "Báo cáo mượn – trả cơ sở vật chất (Thống kê & Xuất PDF)";
    }

    @Override
    public void run(Scanner in) {
        ui.run(in);
    }
}

