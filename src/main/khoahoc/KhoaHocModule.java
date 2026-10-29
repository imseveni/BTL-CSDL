package khoahoc;

import common.Database;

/** Adapter cho menu chung List<AppModule> của repo BTL-CSDL. */
public class KhoaHocModule {

    public static KhoaHocSwingUi createSwingUi() {

        return new KhoaHocSwingUi(new KhoaHocService(new KhoaHocDao(Database::getConnection)));
    }
}
