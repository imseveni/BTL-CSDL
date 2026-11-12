package app;

import static org.junit.jupiter.api.Assertions.*;

import baocaomuontra.BaoCaoMuonTraDao;
import baocaomuontra.BaoCaoMuonTraService;
import baocaomuontra.BoLocBaoCaoMuonTra;
import common.PhienLamViec;
import common.PhienLamViec.NguoiDung;
import common.PhienLamViec.VaiTro;
import common.QuyenException;
import cosovatchat.CoSoVatChatDao;
import cosovatchat.CoSoVatChatService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import muontra.LichSuMuon;
import muontra.PhieuMuon;
import muontra.PhieuMuonDao;
import muontra.PhieuMuonService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import quyenhan.QuyenHanDao;
import quyenhan.QuyenHanService;
import thanhvien.ThanhVien;
import thanhvien.ThanhVienDao;
import thanhvien.ThanhVienService;

/** Quyền được chặn ở tầng Service, nên không cần MySQL: DAO giả ghi lại những gì được gọi. */
class PhanQuyenTest {

    private static final LocalDate NGAY = LocalDate.of(2026, 11, 1);
    private final List<String> calls = new ArrayList<>();

    private static void thanhVien(String msv) {
        PhienLamViec.batDau(new NguoiDung(VaiTro.THANH_VIEN, msv, "Thành viên " + msv));
    }

    private static void banChuNhiem() {
        PhienLamViec.batDau(new NguoiDung(VaiTro.BAN_CHU_NHIEM, "B21DCCN001", "Chủ nhiệm"));
    }

    @AfterEach void reset() {
        PhienLamViec.datLai();
    }

    // ---- Thành viên

    private ThanhVienService thanhVienService() {
        return new ThanhVienService(new ThanhVienDao() {
            @Override public void them(ThanhVien tv) { calls.add("them " + tv.msv()); }
            @Override public void capNhat(ThanhVien tv) { calls.add("capNhat " + tv.msv()); }
            @Override public void doiTrangThai(String msv, String tt) { calls.add("doiTrangThai"); }
            @Override public void xoa(String msv) { calls.add("xoa"); }
            @Override public List<ThanhVien> tim(String k, String b, String t) { calls.add("tim"); return List.of(); }
            @Override public ThanhVien chiTiet(String msv) { calls.add("chiTiet " + msv); return null; }
            @Override public List<ThanhVien> baoCaoDanhSach(String b, String t) { calls.add("baoCao"); return List.of(); }
        });
    }

    private static ThanhVien tv(String msv) {
        return new ThanhVien(msv, "Nguyễn Văn A", LocalDate.of(2004, 1, 1), "Nam", "a@example.com", "0912345678",
                NGAY, "Đang hoạt động", "BAN01", null, "Thành viên");
    }

    @Test void thanhVienChiXemVaSuaHoSoCuaMinh() {
        thanhVien("B21DCCN010");
        var service = thanhVienService();
        service.chiTiet("B21DCCN010");
        service.capNhat(tv("B21DCCN010"));
        assertEquals(List.of("chiTiet B21DCCN010", "capNhat B21DCCN010"), calls);
        assertThrows(QuyenException.class, () -> service.chiTiet("B21DCCN011"));
        assertThrows(QuyenException.class, () -> service.capNhat(tv("B21DCCN011")));
        assertEquals(2, calls.size(), "không được gọi xuống CSDL khi bị từ chối");
    }

    @Test void thanhVienKhongQuanLyDuocDanhSachThanhVien() {
        thanhVien("B21DCCN010");
        var service = thanhVienService();
        assertThrows(QuyenException.class, () -> service.them(tv("B21DCCN099")));
        assertThrows(QuyenException.class, () -> service.xoa("B21DCCN011"));
        assertThrows(QuyenException.class, () -> service.doiTrangThai("B21DCCN011", "Tạm nghỉ"));
        assertThrows(QuyenException.class, () -> service.tim("", null, null));
        assertThrows(QuyenException.class, () -> service.baoCaoDanhSach(null, null));
        assertThrows(QuyenException.class, () -> service.baoCaoHoatDong(NGAY, NGAY));
        assertTrue(calls.isEmpty());
    }

    @Test void banChuNhiemLamDuocMoiThuVoiThanhVien() {
        banChuNhiem();
        var service = thanhVienService();
        service.them(tv("B21DCCN099"));
        service.capNhat(tv("B21DCCN011"));
        service.tim("", null, null);
        service.xoa("B21DCCN011");
        assertEquals(4, calls.size());
    }

    @Test void thanhVienKhongCapQuyenChoMinh() {
        thanhVien("B21DCCN010");
        var service = new QuyenHanService(new QuyenHanDao() {
            @Override public void capQuyen(String msv, String ma) { calls.add("capQuyen"); }
            @Override public List<quyenhan.QuyenHan> quyenCuaThanhVien(String msv) { calls.add("xem"); return List.of(); }
        });
        assertThrows(QuyenException.class, () -> service.capQuyen("B21DCCN010", "QH01"));
        assertThrows(QuyenException.class, () -> service.danhSach());
        service.quyenCuaThanhVien("B21DCCN010");
        assertEquals(List.of("xem"), calls);
    }

    // ---- Cơ sở vật chất

    @Test void thanhVienChiTraCuuThietBi() {
        thanhVien("B21DCCN010");
        var service = new CoSoVatChatService(new CoSoVatChatDao() {
            @Override public List<cosovatchat.CoSoVatChat> timKiem(String k) { calls.add("tim"); return List.of(); }
            @Override public void themMoi(String a, String b, int c, Integer d, String e) { calls.add("them"); }
            @Override public void capNhat(String a, String b, int c) { calls.add("sua"); }
            @Override public void ganNguoiQuanLy(String a, String b) { calls.add("gan"); }
        });
        service.timKiem("loa");
        assertThrows(QuyenException.class, () -> service.themMoi("TB1", "Loa", 3, null, null));
        assertThrows(QuyenException.class, () -> service.capNhat("TB1", "Loa", 3));
        assertThrows(QuyenException.class, () -> service.ganNguoiQuanLy("TB1", "B21DCCN010"));
        assertEquals(List.of("tim"), calls);
        banChuNhiem();
        service.themMoi("TB1", "Loa", 3, null, null);
        assertEquals(List.of("tim", "them"), calls);
    }

    // ---- Mượn – trả

    private PhieuMuonService phieuService() {
        return new PhieuMuonService(new PhieuMuonDao() {
            @Override public void taoPhieu(String ma, LocalDate m, LocalDate h, String d, String msv) { calls.add("tao " + msv); }
            @Override public void themChiTiet(String p, String t, int n) { calls.add("them " + p); }
            @Override public void duyet(String p) { calls.add("duyet"); }
            @Override public void tuChoi(String p) { calls.add("tuChoi"); }
            @Override public void traThietBi(String p, String t, LocalDate n) { calls.add("tra"); }
            @Override public void traTatCa(String p, LocalDate n) { calls.add("traTatCa"); }
            @Override public PhieuMuon timPhieu(String p) {
                return new PhieuMuon(p, NGAY, NGAY.plusDays(3), null, PhieuMuon.CHO_DUYET,
                        p.startsWith("CUA_TOI") ? "B21DCCN010" : "B21DCCN011");
            }
            @Override public List<PhieuMuon> danhSachPhieu(String tt) {
                return List.of(new PhieuMuon("CUA_TOI", NGAY, NGAY, null, PhieuMuon.DA_DUYET, "B21DCCN010"),
                        new PhieuMuon("CUA_NGUOI_KHAC", NGAY, NGAY, null, PhieuMuon.DA_DUYET, "B21DCCN011"));
            }
            @Override public List<LichSuMuon> lichSuMuon(String msv) { calls.add("lichSu " + msv); return List.of(); }
        });
    }

    @Test void thanhVienLapVaTheoDoiPhieuCuaMinh() {
        thanhVien("B21DCCN010");
        var service = phieuService();
        service.taoPhieu("CUA_TOI_1", NGAY, NGAY.plusDays(2), "Họp", "B21DCCN010");
        service.themThietBi("CUA_TOI_1", "TB1", 1);
        service.lichSuMuon("B21DCCN010");
        assertEquals(List.of("tao B21DCCN010", "them CUA_TOI_1", "lichSu B21DCCN010"), calls);
        assertEquals(List.of("CUA_TOI"), service.danhSachPhieu(null).stream().map(PhieuMuon::maPhieuMuon).toList());
        assertThrows(QuyenException.class, () -> service.taoPhieu("X", NGAY, NGAY, null, "B21DCCN011"));
        assertThrows(QuyenException.class, () -> service.themThietBi("PHIEU_KHAC", "TB1", 1));
        assertThrows(QuyenException.class, () -> service.chiTiet("PHIEU_KHAC"));
        assertThrows(QuyenException.class, () -> service.lichSuMuon("B21DCCN011"));
    }

    @Test void thanhVienKhongDuocDuyetTuChoiHayGhiNhanTra() {
        thanhVien("B21DCCN010");
        var service = phieuService();
        assertThrows(QuyenException.class, () -> service.duyet("CUA_TOI"));
        assertThrows(QuyenException.class, () -> service.tuChoi("CUA_TOI"));
        assertThrows(QuyenException.class, () -> service.traThietBi("CUA_TOI", "TB1", NGAY));
        assertThrows(QuyenException.class, () -> service.traTatCa("CUA_TOI", NGAY));
        assertThrows(QuyenException.class, () -> service.thietBiChuaTra());
        assertTrue(calls.isEmpty());
    }

    @Test void banChuNhiemThayMoiPhieuVaDuyetDuoc() {
        banChuNhiem();
        var service = phieuService();
        assertEquals(2, service.danhSachPhieu(null).size());
        service.duyet("CUA_TOI");
        service.traTatCa("CUA_TOI", NGAY);
        assertEquals(List.of("duyet", "traTatCa"), calls);
    }

    // ---- Báo cáo mượn – trả

    @Test void thanhVienKhongLapDuocBaoCaoMuonTra() {
        thanhVien("B21DCCN010");
        var service = new BaoCaoMuonTraService(new BaoCaoMuonTraDao() {
            @Override public List<baocaomuontra.BaoCaoMuonTraItem> truyVanTheoThoiGian(LocalDate a, LocalDate b,
                    BoLocBaoCaoMuonTra.LoaiThoiGian l, LocalDate m) {
                calls.add("truyVan");
                return List.of();
            }
        }, NGAY);
        var boLoc = new BoLocBaoCaoMuonTra(NGAY, NGAY, BoLocBaoCaoMuonTra.LoaiThoiGian.NGAY_MUON, "",
                BoLocBaoCaoMuonTra.TrangThaiLoc.TAT_CA, BoLocBaoCaoMuonTra.SapXepTheo.NGAY_MUON_TANG);
        assertThrows(QuyenException.class, () -> service.lapBaoCao(boLoc));
        assertThrows(QuyenException.class, service::danhSachThanhVienClb);
        assertTrue(calls.isEmpty());
        banChuNhiem();
        assertNotNull(service.lapBaoCao(boLoc));
        assertEquals(List.of("truyVan"), calls);
    }

    @Test void phienMacDinhLaQuanTriChoDongLenhVaDatLaiSauKhiDangXuat() {
        assertTrue(PhienLamViec.laBanChuNhiem());
        thanhVien("B21DCCN010");
        assertTrue(PhienLamViec.laThanhVien());
        PhienLamViec.datLai();
        assertTrue(PhienLamViec.laBanChuNhiem());
        assertNull(PhienLamViec.msvHienTai());
    }
}
