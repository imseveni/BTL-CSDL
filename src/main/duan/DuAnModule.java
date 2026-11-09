package duan;

import common.Database;

/** Adapter cho menu chung List<AppModule> của repo BTL-CSDL. */
public class DuAnModule {

    public static DuAnSwingUi createSwingUi() {

        return new DuAnSwingUi(new DuAnService(new DuAnDao(Database::getConnection)));
    }
}
