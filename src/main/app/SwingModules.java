package app;

import common.SwingModule;
import thanhvien.ThanhVienSwingModule;
import banchuyenmon.BanChuyenMonSwingModule;
import quyenhan.QuyenHanSwingModule;
import khoahoc.KhoaHocSwingModule;
import duan.DuAnSwingModule;
import sukien.SuKienSwingModule;
import thuchi.ThuChiSwingModule;
import danhgia.DanhGiaSwingModule;
import cosovatchat.CoSoVatChatSwingModule;
import muontra.MuonTraSwingModule;
import baocaomuontra.BaoCaoMuonTraSwingModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Danh sách module hiển thị thành tab trong cửa sổ chính, theo đúng số thứ tự 01 đến 10 của
 * docs/PHAN_CONG.md. Chỉ Tú sửa file này; các thành viên chỉ sửa lớp XxxSwingModule trong package của mình.
 */
public final class SwingModules {

    private SwingModules() {
    }

    public static List<SwingModule> all() {
        List<SwingModule> list = new ArrayList<>();
        list.add(new ThanhVienSwingModule());                                     // 01
        list.add(new BanChuyenMonSwingModule());                                     // 02
        list.add(new QuyenHanSwingModule());                                     // 03
        list.add(new KhoaHocSwingModule());                                     // 04
        list.add(new DuAnSwingModule());                                     // 05
        list.add(new SuKienSwingModule());                                     // 06
        list.add(new ThuChiSwingModule());                                     // 07
        list.add(new DanhGiaSwingModule());                                     // 08
        list.add(new CoSoVatChatSwingModule());                                     // 09
        list.add(new MuonTraSwingModule());                                     // 10
        list.add(new BaoCaoMuonTraSwingModule());                               // 11
        return list;
    }
}
