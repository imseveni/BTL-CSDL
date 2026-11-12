package thanhvien;

import static org.junit.jupiter.api.Assertions.*;

import common.NguoiLap;
import common.ReportSheet;
import common.ReportSheet.Tone;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class BaoCaoThanhVienTest {

    private static final LocalDate NGAY_LAP = LocalDate.of(2026, 11, 6);
    private static final NguoiLap AN = new NguoiLap("Nguyễn Văn An", "Chủ nhiệm");

    private static ThanhVien tv(String msv, String ten, String ban, String chucVu, String trangThai) {
        return new ThanhVien(msv, ten, LocalDate.of(2004, 3, 1), "Nam", msv.toLowerCase() + "@example.com", "0900000001",
                LocalDate.of(2024, 9, 9), trangThai, "BAN01", ban, chucVu);
    }

    @Test void baoCaoDanhSachGhiRoNguoiLapNoiNhanVaTongHop() {
        ReportSheet r = BaoCaoThanhVien.danhSachTheoBan(AN, NGAY_LAP,
                List.of(tv("TV1", "An", "Ban Chủ nhiệm", "Chủ nhiệm", "Đang hoạt động"),
                        tv("TV2", "Bình", "Ban Chủ nhiệm", "Phó chủ nhiệm", "Tạm nghỉ"),
                        tv("TV3", "Chi", "Ban Chuyên môn", "Thành viên", "Đã rời CLB")));
        assertEquals("CÂU LẠC BỘ SINH VIÊN", r.organization());
        assertEquals("BAN CHỦ NHIỆM", r.unit());
        assertEquals("DANH SÁCH THÀNH VIÊN CÂU LẠC BỘ", r.title());
        assertEquals("Tính đến ngày 06/11/2026", r.subtitle());
        assertEquals(List.of("Kính gửi", "Người lập", "Mục đích"), r.info().stream().map(ReportSheet.Info::label).toList());
        assertTrue(r.info().get(0).value().contains("Đoàn trường"));
        assertEquals("Nguyễn Văn An – Chủ nhiệm", r.info().get(1).value());
        assertEquals("Nguyễn Văn An", r.signRightName());
        assertEquals(NGAY_LAP, r.issueDate());
        assertEquals("Mã sinh viên", r.columns().get(1), "không viết tắt MSV");
        assertFalse(r.columns().contains("MSV"));
        assertEquals(11, r.columns().size());
        assertEquals(List.of("1", "TV1", "An", "01/03/2004", "Nam", "tv1@example.com", "0900000001", "09/09/2024",
                "Ban Chủ nhiệm", "Chủ nhiệm", "Đang hoạt động"), r.rows().get(0));
        assertEquals(List.of(Tone.GOOD, Tone.WARN, Tone.BAD), r.badgeTones());
        assertEquals(10, r.badgeColumn());
        assertEquals(List.of("3", "1", "1", "1"), r.stats().stream().map(ReportSheet.Stat::value).toList());
        assertTrue(r.summaryLines().get(0).contains("Tổng số thành viên: 3"));
        assertTrue(r.summaryLines().get(1).contains("Ban Chủ nhiệm 2"));
        assertTrue(r.summaryLines().get(1).contains("Ban Chuyên môn 1"));
    }

    @Test void baoCaoKhongCoDongBoLoc() {
        ReportSheet r = BaoCaoThanhVien.danhSachTheoBan(AN, NGAY_LAP, List.of());
        assertTrue(r.info().stream().noneMatch(i -> i.label().toLowerCase().contains("lọc")));
        assertFalse(r.subtitle().toLowerCase().contains("lọc"));
    }

    @Test void giaTriTrongKhongLamLoiBaoCao() {
        var rong = new ThanhVien("TV9", "Dũng", null, null, null, null, null, null, "BAN01", null, null);
        ReportSheet r = BaoCaoThanhVien.danhSachTheoBan(AN, NGAY_LAP, List.of(rong));
        assertEquals(List.of("1", "TV9", "Dũng", "", "", "", "", "", "", "", ""), r.rows().get(0));
    }

    @Test void baoCaoRongVanHopLe() {
        ReportSheet r = BaoCaoThanhVien.danhSachTheoBan(AN, NGAY_LAP, List.of());
        assertEquals(0, r.rows().size());
        assertTrue(r.summaryLines().get(0).contains("Tổng số thành viên: 0"));
    }

    @Test void baoCaoHoatDongTinhTongVaKyBaoCao() {
        ReportSheet r = BaoCaoThanhVien.hoatDong(AN, NGAY_LAP, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30),
                List.of(new TongHopHoatDong("TV1", "An", "Ban A", "Chủ nhiệm", "Đang hoạt động", 1, 2, 3, 0, 1, 0, 7),
                        new TongHopHoatDong("TV2", "Bình", "Ban A", "Thành viên", "Đang hoạt động", 0, 0, 0, 0, 0, 0, 0)));
        assertEquals("TỔNG HỢP HOẠT ĐỘNG CỦA THÀNH VIÊN", r.title());
        assertEquals("Kỳ báo cáo: từ 01/01/2026 đến 30/06/2026", r.subtitle());
        assertTrue(r.info().get(0).value().contains("giảng viên phụ trách"));
        assertTrue(r.info().get(2).value().contains("khen thưởng"));
        assertEquals("Mã sinh viên", r.columns().get(1));
        assertEquals(12, r.columns().size());
        assertEquals(List.of("1", "TV1", "An", "Ban A", "Chủ nhiệm", "1", "2", "3", "0", "1", "0", "7"), r.rows().get(0));
        assertEquals(List.of("2", "1", "1", "7"), r.stats().stream().map(ReportSheet.Stat::value).toList());
        assertTrue(r.summaryLines().get(0).contains("1/2 thành viên"));
        assertTrue(r.summaryLines().get(0).contains("7 lượt"));
    }

    @Test void tenFileGoiYLaPdf() {
        assertEquals("BaoCao_DanhSachThanhVien_20261106.pdf", BaoCaoThanhVien.tenFile("DanhSachThanhVien", NGAY_LAP));
        assertEquals("BaoCao_X_20260102.pdf", BaoCaoThanhVien.tenFile("X", LocalDate.of(2026, 1, 2)));
    }
}
