package thanhvien;

import static org.junit.jupiter.api.Assertions.*;

import common.ReportSheet;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class BaoCaoThanhVienTest {

    private static final LocalDate NGAY_LAP = LocalDate.of(2026, 11, 6);

    private static ThanhVien tv(String msv, String ten, String ban, String chucVu, String trangThai) {
        return new ThanhVien(msv, ten, LocalDate.of(2004, 3, 1), "Nam", msv.toLowerCase() + "@example.com", "0900000001",
                LocalDate.of(2024, 9, 9), trangThai, "BAN01", ban, chucVu);
    }

    @Test void baoCaoDanhSachGhiRoNguoiLapNoiNhanVaTongHop() {
        ReportSheet r = BaoCaoThanhVien.danhSachTheoBan("câu lạc bộ sinh viên", "Thư ký An", NGAY_LAP, "tất cả ban; mọi trạng thái",
                List.of(tv("TV1", "An", "Ban Chủ nhiệm", "Chủ nhiệm", "Đang hoạt động"),
                        tv("TV2", "Bình", "Ban Chủ nhiệm", "Phó chủ nhiệm", "Tạm nghỉ"),
                        tv("TV3", "Chi", "Ban Chuyên môn", "Thành viên", "Đang hoạt động")));
        assertEquals("CÂU LẠC BỘ SINH VIÊN", r.organization());
        assertEquals("DANH SÁCH THÀNH VIÊN CÂU LẠC BỘ", r.title());
        assertTrue(r.infoLines().contains("Người lập: Thư ký An"));
        assertTrue(r.infoLines().stream().anyMatch(s -> s.startsWith("Nơi nhận: ") && s.contains("Đoàn trường")));
        assertTrue(r.infoLines().contains("Ngày lập: 06/11/2026"));
        assertEquals("Ngày 06 tháng 11 năm 2026", r.signDate());
        assertEquals(11, r.columns().size());
        assertEquals(3, r.rows().size());
        assertEquals(List.of("1", "TV1", "An", "01/03/2004", "Nam", "tv1@example.com", "0900000001", "09/09/2024",
                "Ban Chủ nhiệm", "Chủ nhiệm", "Đang hoạt động"), r.rows().get(0));
        assertEquals("Tổng số thành viên: 3", r.summaryLines().get(0));
        assertTrue(r.summaryLines().get(1).contains("Ban Chủ nhiệm: 2"));
        assertTrue(r.summaryLines().get(1).contains("Ban Chuyên môn: 1"));
        assertTrue(r.summaryLines().get(2).contains("Đang hoạt động: 2"));
        assertTrue(r.summaryLines().get(2).contains("Tạm nghỉ: 1"));
    }

    @Test void giaTriTrongKhongLamLoiBaoCao() {
        var rong = new ThanhVien("TV9", "Dũng", null, null, null, null, null, null, "BAN01", null, null);
        ReportSheet r = BaoCaoThanhVien.danhSachTheoBan("CLB", "An", NGAY_LAP, "", List.of(rong));
        assertEquals(List.of("1", "TV9", "Dũng", "", "", "", "", "", "", "", ""), r.rows().get(0));
    }

    @Test void baoCaoRongVanHopLe() {
        ReportSheet r = BaoCaoThanhVien.danhSachTheoBan("CLB", "An", NGAY_LAP, "", List.of());
        assertEquals(0, r.rows().size());
        assertEquals(List.of("Tổng số thành viên: 0"), r.summaryLines());
    }

    @Test void baoCaoHoatDongTinhTongVaKyBaoCao() {
        ReportSheet r = BaoCaoThanhVien.hoatDong("clb x", "Thư ký An", NGAY_LAP, LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 30),
                List.of(new TongHopHoatDong("TV1", "An", "Ban A", "Chủ nhiệm", "Đang hoạt động", 1, 2, 3, 0, 1, 0, 7),
                        new TongHopHoatDong("TV2", "Bình", "Ban A", "Thành viên", "Đang hoạt động", 0, 0, 0, 0, 0, 0, 0)));
        assertEquals("TỔNG HỢP HOẠT ĐỘNG CỦA THÀNH VIÊN", r.title());
        assertEquals("Kỳ báo cáo: từ 01/01/2026 đến 30/06/2026", r.subtitle());
        assertTrue(r.infoLines().stream().anyMatch(s -> s.startsWith("Nơi nhận: ") && s.contains("giảng viên phụ trách")));
        assertTrue(r.infoLines().stream().anyMatch(s -> s.startsWith("Mục đích: ") && s.contains("khen thưởng")));
        assertEquals(12, r.columns().size());
        assertEquals(List.of("1", "TV1", "An", "Ban A", "Chủ nhiệm", "1", "2", "3", "0", "1", "0", "7"), r.rows().get(0));
        assertTrue(r.numericColumns().contains(11));
        assertTrue(r.summaryLines().get(0).contains("Tổng số thành viên trong báo cáo: 2"));
        assertTrue(r.summaryLines().get(0).contains("Có hoạt động trong kỳ: 1"));
        assertTrue(r.summaryLines().get(0).contains("Tổng lượt hoạt động: 7"));
    }

    @Test void tenFileGoiY() {
        assertEquals("BaoCao_DanhSachThanhVien_20261106.xlsx", BaoCaoThanhVien.tenFile("DanhSachThanhVien", NGAY_LAP));
        assertEquals("BaoCao_X_20260102.xlsx", BaoCaoThanhVien.tenFile("X", LocalDate.of(2026, 1, 2)));
    }
}
