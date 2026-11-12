package thanhvien;

import common.NguoiLap;
import common.ReportSheet;
import common.ReportSheet.Align;
import common.ReportSheet.Tone;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dựng nội dung hai báo cáo của module Thành viên theo mẫu chung của hệ thống ({@link ReportSheet}); việc vẽ PDF
 * ở {@code common.BaoCaoPdf}, việc đọc CSDL ở {@link ThanhVienService}.
 */
public final class BaoCaoThanhVien {

    /** Báo cáo 1: ai nhận và dùng để làm gì (hiện trên màn hình và in trong file). */
    public static final String NOI_NHAN_DANH_SACH = "Chủ nhiệm CLB; giảng viên phụ trách và Đoàn trường (đơn vị quản lý CLB)";
    public static final String MUC_DICH_DANH_SACH = "Đăng ký danh sách thành viên sinh hoạt với đơn vị quản lý";

    /** Báo cáo 2: ai nhận và dùng để làm gì. */
    public static final String NOI_NHAN_HOAT_DONG = "Chủ nhiệm CLB và giảng viên phụ trách";
    public static final String MUC_DICH_HOAT_DONG = "Xét khen thưởng, ghi nhận đóng góp của thành viên cuối kỳ";

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private BaoCaoThanhVien() {
    }

    public static ReportSheet danhSachTheoBan(NguoiLap nguoiLap, LocalDate ngayLap, List<ThanhVien> thanhViens) {
        List<List<String>> rows = new ArrayList<>();
        List<Tone> tones = new ArrayList<>();
        Map<String, Integer> theoBan = new LinkedHashMap<>();
        int hoatDong = 0;
        int tamNghi = 0;
        int daRoi = 0;
        int stt = 1;
        for (ThanhVien tv : thanhViens) {
            rows.add(List.of(String.valueOf(stt++), tv.msv(), tv.hoVaTen(), ngay(tv.ngaySinh()), tr(tv.gioiTinh()),
                    tr(tv.email()), tr(tv.sdt()), ngay(tv.ngayGiaNhap()), tr(tv.tenBan()), tr(tv.chucVu()),
                    tr(tv.trangThaiHD())));
            tones.add(tone(tv.trangThaiHD()));
            theoBan.merge(tr(tv.tenBan()), 1, Integer::sum);
            switch (tr(tv.trangThaiHD())) {
                case "Đang hoạt động" -> hoatDong++;
                case "Tạm nghỉ" -> tamNghi++;
                case "Đã rời CLB" -> daRoi++;
                default -> { }
            }
        }
        List<String> summary = new ArrayList<>();
        summary.add("Tổng số thành viên: " + thanhViens.size() + " (đang hoạt động " + hoatDong + ", tạm nghỉ " + tamNghi
                + ", đã rời CLB " + daRoi + ").");
        if (!theoBan.isEmpty()) {
            summary.add("Phân bố theo ban: " + join(theoBan) + ".");
        }
        return ReportSheet.builder()
                .title("DANH SÁCH THÀNH VIÊN CÂU LẠC BỘ")
                .subtitle("Tính đến ngày " + NGAY.format(ngayLap))
                .info("Kính gửi", NOI_NHAN_DANH_SACH)
                .info("Người lập", nguoiLap.hienThi())
                .info("Mục đích", MUC_DICH_DANH_SACH)
                .stat("Tổng số thành viên", thanhViens.size(), Tone.INFO)
                .stat("Đang hoạt động", hoatDong, Tone.GOOD)
                .stat("Tạm nghỉ", tamNghi, Tone.WARN)
                .stat("Đã rời CLB", daRoi, Tone.BAD)
                .column("STT", 5, Align.CENTER)
                .column("Mã sinh viên", 12, Align.LEFT)
                .column("Họ và tên", 17, Align.LEFT)
                .column("Ngày sinh", 10, Align.CENTER)
                .column("Giới tính", 7, Align.CENTER)
                .column("Email", 22, Align.LEFT)
                .column("Số điện thoại", 11, Align.LEFT)
                .column("Ngày gia nhập", 11, Align.CENTER)
                .column("Ban", 13, Align.LEFT)
                .column("Chức vụ", 12, Align.LEFT)
                .column("Trạng thái", 13, Align.CENTER)
                .rows(rows)
                .badge(10, tones)
                .summary("Tổng kết", summary)
                .issueDate(ngayLap)
                .signRight("NGƯỜI LẬP BÁO CÁO", "(Ký, ghi rõ họ tên)", nguoiLap.hoVaTen())
                .footerLabel("Danh sách thành viên")
                .build();
    }

    public static ReportSheet hoatDong(NguoiLap nguoiLap, LocalDate ngayLap, LocalDate tuNgay, LocalDate denNgay,
                                       List<TongHopHoatDong> dong) {
        List<List<String>> rows = new ArrayList<>();
        int stt = 1;
        long tong = 0;
        long khoaHoc = 0;
        long duAn = 0;
        long suKien = 0;
        long danhGia = 0;
        long phieuMuon = 0;
        long giaoDich = 0;
        int coHoatDong = 0;
        for (TongHopHoatDong d : dong) {
            rows.add(List.of(String.valueOf(stt++), d.msv(), d.hoVaTen(), tr(d.tenBan()), tr(d.chucVu()),
                    String.valueOf(d.soKhoaHoc()), String.valueOf(d.soDuAn()), String.valueOf(d.soSuKien()),
                    String.valueOf(d.soDanhGia()), String.valueOf(d.soPhieuMuon()), String.valueOf(d.soGiaoDich()),
                    String.valueOf(d.tong())));
            tong += d.tong();
            khoaHoc += d.soKhoaHoc();
            duAn += d.soDuAn();
            suKien += d.soSuKien();
            danhGia += d.soDanhGia();
            phieuMuon += d.soPhieuMuon();
            giaoDich += d.soGiaoDich();
            if (d.tong() > 0) {
                coHoatDong++;
            }
        }
        List<String> summary = List.of(
                "Có " + coHoatDong + "/" + dong.size() + " thành viên có hoạt động trong kỳ; tổng cộng " + tong
                        + " lượt hoạt động.",
                "Theo loại: khóa học " + khoaHoc + ", dự án " + duAn + ", sự kiện " + suKien + ", đánh giá " + danhGia
                        + ", phiếu mượn " + phieuMuon + ", giao dịch thu chi " + giaoDich + ".");
        return ReportSheet.builder()
                .title("TỔNG HỢP HOẠT ĐỘNG CỦA THÀNH VIÊN")
                .subtitle("Kỳ báo cáo: từ " + NGAY.format(tuNgay) + " đến " + NGAY.format(denNgay))
                .info("Kính gửi", NOI_NHAN_HOAT_DONG)
                .info("Người lập", nguoiLap.hienThi())
                .info("Mục đích", MUC_DICH_HOAT_DONG)
                .stat("Tổng số thành viên", dong.size(), Tone.INFO)
                .stat("Có hoạt động trong kỳ", coHoatDong, Tone.GOOD)
                .stat("Chưa có hoạt động", dong.size() - coHoatDong, Tone.WARN)
                .stat("Tổng lượt hoạt động", tong, Tone.INFO)
                .column("STT", 5, Align.CENTER)
                .column("Mã sinh viên", 12, Align.LEFT)
                .column("Họ và tên", 17, Align.LEFT)
                .column("Ban", 14, Align.LEFT)
                .column("Chức vụ", 12, Align.LEFT)
                .column("Khóa học", 8, Align.CENTER)
                .column("Dự án", 7, Align.CENTER)
                .column("Sự kiện", 8, Align.CENTER)
                .column("Đánh giá", 8, Align.CENTER)
                .column("Phiếu mượn", 9, Align.CENTER)
                .column("Thu chi", 7, Align.CENTER)
                .column("Tổng", 7, Align.CENTER)
                .rows(rows)
                .summary("Tổng kết", summary)
                .issueDate(ngayLap)
                .signRight("NGƯỜI LẬP BÁO CÁO", "(Ký, ghi rõ họ tên)", nguoiLap.hoVaTen())
                .footerLabel("Hoạt động của thành viên")
                .build();
    }

    /** Tên file gợi ý khi xuất, ví dụ BaoCao_DanhSachThanhVien_20261106.pdf. */
    public static String tenFile(String loai, LocalDate ngay) {
        return "BaoCao_" + loai + "_" + String.format("%04d%02d%02d", ngay.getYear(), ngay.getMonthValue(), ngay.getDayOfMonth()) + ".pdf";
    }

    private static Tone tone(String trangThai) {
        return switch (tr(trangThai)) {
            case "Đang hoạt động" -> Tone.GOOD;
            case "Tạm nghỉ" -> Tone.WARN;
            case "Đã rời CLB" -> Tone.BAD;
            default -> Tone.NONE;
        };
    }

    private static String ngay(LocalDate d) {
        return d == null ? "" : NGAY.format(d);
    }

    private static String tr(String s) {
        return s == null ? "" : s;
    }

    private static String join(Map<String, Integer> counts) {
        List<String> parts = new ArrayList<>();
        counts.forEach((k, v) -> parts.add(k + " " + v));
        return String.join(", ", parts);
    }
}
