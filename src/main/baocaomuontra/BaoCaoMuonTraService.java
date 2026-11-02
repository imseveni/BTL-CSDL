package baocaomuontra;

import baocaomuontra.BoLocBaoCaoMuonTra.LoaiThoiGian;
import baocaomuontra.BoLocBaoCaoMuonTra.SapXepTheo;
import baocaomuontra.BoLocBaoCaoMuonTra.TrangThaiLoc;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Nghiệp vụ tổng hợp, kiểm tra hợp lệ, lọc, tìm kiếm và sắp xếp báo cáo mượn – trả cơ sở vật chất.
 */
public class BaoCaoMuonTraService {

    private final BaoCaoMuonTraDao dao;
    private final LocalDate ngayMocMacDinh;

    public BaoCaoMuonTraService() {
        this(new BaoCaoMuonTraDao(), null);
    }

    public BaoCaoMuonTraService(BaoCaoMuonTraDao dao) {
        this(dao, null);
    }

    public BaoCaoMuonTraService(BaoCaoMuonTraDao dao, LocalDate ngayMocMacDinh) {
        this.dao = dao;
        this.ngayMocMacDinh = ngayMocMacDinh;
    }

    /**
     * Kiểm tra khoảng thời gian hợp lệ (ngày bắt đầu không được lớn hơn ngày kết thúc).
     */
    public static void kiemTraKhoangThoiGian(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null) {
            throw new IllegalArgumentException("Ngày bắt đầu không được để trống.");
        }
        if (denNgay == null) {
            throw new IllegalArgumentException("Ngày kết thúc không được để trống.");
        }
        if (tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException(
                    "Ngày bắt đầu (" + tuNgay + ") không được lớn hơn ngày kết thúc (" + denNgay + ").");
        }
    }

    /**
     * Lập báo cáo mượn – trả cơ sở vật chất theo bộ lọc người dùng chọn.
     */
    public KetQuaBaoCaoMuonTra lapBaoCao(BoLocBaoCaoMuonTra boLoc) {
        if (boLoc == null) {
            throw new IllegalArgumentException("Điều kiện lọc báo cáo không được để trống.");
        }
        LocalDate tuNgay = boLoc.tuNgay();
        LocalDate denNgay = boLoc.denNgay();
        kiemTraKhoangThoiGian(tuNgay, denNgay);

        LoaiThoiGian loaiThoiGian = boLoc.loaiThoiGian() == null
                ? LoaiThoiGian.NGAY_MUON
                : boLoc.loaiThoiGian();
        TrangThaiLoc trangThaiLoc = boLoc.trangThaiLoc() == null
                ? TrangThaiLoc.TAT_CA
                : boLoc.trangThaiLoc();
        SapXepTheo sapXepTheo = boLoc.sapXepTheo() == null
                ? SapXepTheo.NGAY_MUON_TANG
                : boLoc.sapXepTheo();
        String tuKhoa = boLoc.tuKhoa() == null ? "" : boLoc.tuKhoa().trim();

        LocalDate ngayMoc = ngayMocMacDinh == null ? LocalDate.now() : ngayMocMacDinh;
        List<BaoCaoMuonTraItem> duLieuGoc = dao.truyVanTheoThoiGian(tuNgay, denNgay, loaiThoiGian, ngayMoc);

        List<BaoCaoMuonTraItem> danhSachLoc = new ArrayList<>();
        for (BaoCaoMuonTraItem item : duLieuGoc) {
            if (!thuocKhoangThoiGian(item, tuNgay, denNgay, loaiThoiGian)) {
                continue;
            }
            if (!khopTrangThai(item, trangThaiLoc)) {
                continue;
            }
            if (!khopTuKhoa(item, tuKhoa)) {
                continue;
            }
            danhSachLoc.add(item);
        }

        danhSachLoc.sort(boSoSanh(sapXepTheo));
        ThongKeMuonTra thongKe = ThongKeMuonTra.tuDanhSach(danhSachLoc);
        BoLocBaoCaoMuonTra boLocChuan = new BoLocBaoCaoMuonTra(
                tuNgay, denNgay, loaiThoiGian, tuKhoa, trangThaiLoc, sapXepTheo);

        return new KetQuaBaoCaoMuonTra(boLocChuan, ngayMoc, thongKe, List.copyOf(danhSachLoc));
    }

    private static boolean thuocKhoangThoiGian(
            BaoCaoMuonTraItem item,
            LocalDate tuNgay,
            LocalDate denNgay,
            LoaiThoiGian loaiThoiGian) {
        LocalDate ngayKiemTra = loaiThoiGian == LoaiThoiGian.NGAY_TRA
                ? item.ngayTraThucTe()
                : item.ngayMuon();
        if (ngayKiemTra == null) {
            return false;
        }
        return !ngayKiemTra.isBefore(tuNgay) && !ngayKiemTra.isAfter(denNgay);
    }

    private static boolean khopTrangThai(BaoCaoMuonTraItem item, TrangThaiLoc trangThaiLoc) {
        return switch (trangThaiLoc) {
            case TAT_CA -> true;
            case DA_TRA -> item.daTra();
            case CHUA_TRA -> item.chuaTra();
            case QUA_HAN -> item.laQuaHan();
        };
    }

    private static boolean khopTuKhoa(BaoCaoMuonTraItem item, String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) {
            return true;
        }
        String q = tuKhoa.toLowerCase(Locale.ROOT);
        return chua(item.maPhieuMuon(), q)
                || chua(item.maThietBi(), q)
                || chua(item.tenThietBi(), q)
                || chua(item.msvNguoiMuon(), q)
                || chua(item.tenNguoiMuon(), q)
                || chua(item.mucDichMuon(), q)
                || chua(item.trangThai(), q)
                || chua(item.trangThaiHienThi(), q);
    }

    private static boolean chua(String giaTri, String tuKhoaThuong) {
        return giaTri != null && giaTri.toLowerCase(Locale.ROOT).contains(tuKhoaThuong);
    }

    private static Comparator<BaoCaoMuonTraItem> boSoSanh(SapXepTheo sapXepTheo) {
        Comparator<String> maPhieu = Comparator.nullsLast(String::compareTo);
        Comparator<String> maThietBi = Comparator.nullsLast(String::compareTo);

        return switch (sapXepTheo) {
            case NGAY_MUON_TANG -> Comparator
                    .comparing(BaoCaoMuonTraItem::ngayMuon, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(BaoCaoMuonTraItem::maPhieuMuon, maPhieu)
                    .thenComparing(BaoCaoMuonTraItem::maThietBi, maThietBi);
            case NGAY_MUON_GIAM -> Comparator
                    .comparing(BaoCaoMuonTraItem::ngayMuon, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(BaoCaoMuonTraItem::maPhieuMuon, maPhieu)
                    .thenComparing(BaoCaoMuonTraItem::maThietBi, maThietBi);
            case NGAY_TRA_TANG -> Comparator
                    .comparing(BaoCaoMuonTraItem::ngayTraThucTe, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(BaoCaoMuonTraItem::ngayMuon, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(BaoCaoMuonTraItem::maPhieuMuon, maPhieu)
                    .thenComparing(BaoCaoMuonTraItem::maThietBi, maThietBi);
            case NGAY_TRA_GIAM -> Comparator
                    .comparing(BaoCaoMuonTraItem::ngayTraThucTe, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(BaoCaoMuonTraItem::ngayMuon, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(BaoCaoMuonTraItem::maPhieuMuon, maPhieu)
                    .thenComparing(BaoCaoMuonTraItem::maThietBi, maThietBi);
        };
    }
}

