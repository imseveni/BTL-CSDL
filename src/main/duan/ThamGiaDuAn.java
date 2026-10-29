package duan;

import java.math.BigDecimal;

public class ThamGiaDuAn {
    private final String msv;
    private final String maDuAn;
    private final String vaiTro;
    private final BigDecimal tienDoRieng;

    public ThamGiaDuAn(String msv, String maDuAn, String vaiTro, BigDecimal tienDoRieng) {
        this.msv = msv;
        this.maDuAn = maDuAn;
        this.vaiTro = vaiTro;
        this.tienDoRieng = tienDoRieng;
    }

    public String getMsv() {
        return msv;
    }

    public String getMaDuAn() {
        return maDuAn;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public BigDecimal getTienDoRieng() {
        return tienDoRieng;
    }
}
