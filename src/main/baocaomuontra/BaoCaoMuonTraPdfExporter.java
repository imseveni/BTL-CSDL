package baocaomuontra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.Deflater;

/**
 * Bộ xuất báo cáo mượn – trả cơ sở vật chất ra file PDF chuẩn (PDF 1.4).
 *
 * <p>Sử dụng cơ chế kết xuất trang độ phân giải cao (A4 150 DPI) bằng {@link Graphics2D} với
 * font tiếng Việt Unicode của hệ thống (Segoe UI / Arial / Tahoma / SansSerif) kết hợp nén
 * không mất dữ liệu {@code FlateDecode}, đảm bảo:
 * <ul>
 *   <li>Hiển thị tiếng Việt có dấu chính xác 100% trên mọi trình đọc PDF, không bao giờ lỗi font.</li>
 *   <li>Bảng dữ liệu có đường viền sắc nét, căn chỉnh hợp lý, tô màu phân biệt trạng thái.</li>
 *   <li>Tự động phân trang và đánh số trang ({@code Trang x / y}) khi có nhiều bản ghi.</li>
 *   <li>Không phụ thuộc thư viện bên ngoài (chạy hoàn toàn bằng JDK 17 chuẩn).</li>
 * </ul>
 */
public class BaoCaoMuonTraPdfExporter {

    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Kích thước chuẩn trang A4 theo điểm PDF (72 DPI) và điểm ảnh kết xuất (150 DPI)
    private static final int PDF_PAGE_WIDTH_PT = 595;
    private static final int PDF_PAGE_HEIGHT_PT = 842;
    private static final int PAGE_WIDTH_PX = 1240;
    private static final int PAGE_HEIGHT_PX = 1754;
    private static final int MARGIN_X = 50;

    // Số dòng tối đa trên trang đầu và các trang tiếp theo
    private static final int MAX_ROWS_FIRST_PAGE_WITH_SUMMARY = 10;
    private static final int MAX_ROWS_FIRST_PAGE_ONLY_TABLE = 15;
    private static final int MAX_ROWS_NEXT_PAGE_WITH_SUMMARY = 20;
    private static final int MAX_ROWS_NEXT_PAGE_ONLY_TABLE = 25;

    // Bảng màu chuyên nghiệp
    private static final Color MAU_CHU_DAO = new Color(24, 78, 119);
    private static final Color MAU_NEN_TIEU_DE_BANG = new Color(30, 96, 145);
    private static final Color MAU_VIEN_BANG = new Color(185, 200, 215);
    private static final Color MAU_DONG_CHAN = new Color(246, 250, 253);
    private static final Color MAU_CHU_CHINH = new Color(33, 37, 41);
    private static final Color MAU_CHU_PHU = new Color(90, 101, 119);

    private static final Color MAU_DA_TRA_NEN = new Color(219, 244, 228);
    private static final Color MAU_DA_TRA_CHU = new Color(21, 115, 71);
    private static final Color MAU_CHUA_TRA_NEN = new Color(255, 243, 205);
    private static final Color MAU_CHUA_TRA_CHU = new Color(153, 101, 0);
    private static final Color MAU_QUA_HAN_NEN = new Color(248, 215, 218);
    private static final Color MAU_QUA_HAN_CHU = new Color(176, 42, 55);

    private final String fontFamily;

    public BaoCaoMuonTraPdfExporter() {
        this.fontFamily = chonFontTiengViet();
    }

    /**
     * Gợi ý tên file PDF bao gồm khoảng thời gian báo cáo.
     */
    public static String tenFileGoiY(BoLocBaoCaoMuonTra boLoc) {
        if (boLoc == null || boLoc.tuNgay() == null || boLoc.denNgay() == null) {
            return "BaoCaoMuonTra_CSVC.pdf";
        }
        return String.format(
                "BaoCaoMuonTra_CSVC_%s_den_%s.pdf",
                boLoc.tuNgay(),
                boLoc.denNgay());
    }

    /**
     * Xuất kết quả báo cáo ra file PDF tại đường dẫn người dùng chọn.
     */
    public Path xuatFile(KetQuaBaoCaoMuonTra baoCao, Path duongDanFile) throws IOException {
        if (baoCao == null) {
            throw new IllegalArgumentException("Dữ liệu báo cáo không được để trống.");
        }
        if (duongDanFile == null) {
            throw new IllegalArgumentException("Đường dẫn lưu file PDF không được để trống.");
        }
        Path dich = chuanHoaDuoiPdf(duongDanFile);
        Path thuMucCha = dich.toAbsolutePath().getParent();
        if (thuMucCha != null && !Files.exists(thuMucCha)) {
            Files.createDirectories(thuMucCha);
        }

        List<BufferedImage> cacTrang = ketXuatCacTrang(baoCao);
        String tomTatNoiDung = taoVanBanDinhKem(baoCao, cacTrang.size());
        try (OutputStream out = Files.newOutputStream(dich)) {
            ghiTaiLieuPdf(cacTrang, tomTatNoiDung, out);
        }
        return dich;
    }

    /**
     * Kết xuất danh sách các trang báo cáo thành ảnh {@link BufferedImage} (phục vụ xuất PDF và kiểm thử).
     */
    public List<BufferedImage> ketXuatCacTrang(KetQuaBaoCaoMuonTra baoCao) {
        List<BaoCaoMuonTraItem> danhSach = baoCao.danhSachChiTiet() == null
                ? List.of()
                : baoCao.danhSachChiTiet();
        List<TrangDuLieu> phanTrang = chiaTrang(danhSach);
        int tongSoTrang = phanTrang.size();

        List<BufferedImage> ketQua = new ArrayList<>(tongSoTrang);
        for (int i = 0; i < tongSoTrang; i++) {
            TrangDuLieu trang = phanTrang.get(i);
            BufferedImage anhTrang = veMotTrang(
                    baoCao,
                    trang.cacDong(),
                    trang.sttBatDau(),
                    i + 1,
                    tongSoTrang,
                    i == 0,
                    trang.coPhanTongKetCuoi());
            ketQua.add(anhTrang);
        }
        return ketQua;
    }

    private record TrangDuLieu(
            List<BaoCaoMuonTraItem> cacDong,
            int sttBatDau,
            boolean coPhanTongKetCuoi) {
    }

    private static List<TrangDuLieu> chiaTrang(List<BaoCaoMuonTraItem> danhSach) {
        List<TrangDuLieu> cacTrang = new ArrayList<>();
        int tongDong = danhSach.size();

        if (tongDong <= MAX_ROWS_FIRST_PAGE_WITH_SUMMARY) {
            cacTrang.add(new TrangDuLieu(danhSach, 1, true));
            return cacTrang;
        }

        int viTri = 0;
        boolean laTrangDau = true;

        while (viTri < tongDong) {
            int conLai = tongDong - viTri;
            int sucChuaKemTongKet = laTrangDau
                    ? MAX_ROWS_FIRST_PAGE_WITH_SUMMARY
                    : MAX_ROWS_NEXT_PAGE_WITH_SUMMARY;
            int sucChuaToiDa = laTrangDau
                    ? MAX_ROWS_FIRST_PAGE_ONLY_TABLE
                    : MAX_ROWS_NEXT_PAGE_ONLY_TABLE;

            if (conLai <= sucChuaKemTongKet) {
                cacTrang.add(new TrangDuLieu(
                        danhSach.subList(viTri, tongDong),
                        viTri + 1,
                        true));
                viTri = tongDong;
            } else if (conLai <= sucChuaToiDa) {
                // Vừa đủ bảng trên trang này, phần tổng kết chuyển sang trang mới
                cacTrang.add(new TrangDuLieu(
                        danhSach.subList(viTri, tongDong),
                        viTri + 1,
                        false));
                cacTrang.add(new TrangDuLieu(
                        List.of(),
                        tongDong + 1,
                        true));
                viTri = tongDong;
            } else {
                int ketThuc = viTri + sucChuaToiDa;
                cacTrang.add(new TrangDuLieu(
                        danhSach.subList(viTri, ketThuc),
                        viTri + 1,
                        false));
                viTri = ketThuc;
            }
            laTrangDau = false;
        }

        return cacTrang;
    }

    private BufferedImage veMotTrang(
            KetQuaBaoCaoMuonTra baoCao,
            List<BaoCaoMuonTraItem> cacDongTrang,
            int sttBatDau,
            int soTrang,
            int tongSoTrang,
            boolean laTrangDau,
            boolean veTongKetCuoi) {
        BufferedImage image = new BufferedImage(PAGE_WIDTH_PX, PAGE_HEIGHT_PX, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Nền trang trắng
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, PAGE_WIDTH_PX, PAGE_HEIGHT_PX);

            int y = 56;
            if (laTrangDau) {
                y = veTieuDeVaThongKeTrangDau(g, baoCao, y);
            } else {
                y = veTieuDeTrangTiepTheo(g, baoCao, soTrang, tongSoTrang, y);
            }

            if (!cacDongTrang.isEmpty() || laTrangDau) {
                y = veBangChiTiet(g, cacDongTrang, sttBatDau, y);
            }

            if (veTongKetCuoi) {
                vePhanTongKetCuoiBaoCao(g, baoCao, y + 22);
            }

            veChanTrang(g, baoCao, soTrang, tongSoTrang);
        } finally {
            g.dispose();
        }
        return image;
    }

    private int veTieuDeVaThongKeTrangDau(Graphics2D g, KetQuaBaoCaoMuonTra baoCao, int startY) {
        int contentWidth = PAGE_WIDTH_PX - 2 * MARGIN_X;
        int y = startY;

        // Phần đầu trang theo bố cục hành chính chuẩn (Trái: Tên đơn vị CLB | Phải: Quốc hiệu & Tiêu ngữ)
        int leftColW = (int) (contentWidth * 0.44);
        int rightColX = MARGIN_X + (int) (contentWidth * 0.46);
        int rightColW = contentWidth - (int) (contentWidth * 0.46);

        g.setFont(font(Font.BOLD, 16));
        g.setColor(MAU_CHU_CHINH);
        veCanGiua(g, "CÂU LẠC BỘ SINH VIÊN", MARGIN_X, leftColW, y);
        veCanGiua(g, "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM", rightColX, rightColW, y);
        y += 24;

        g.setFont(font(Font.PLAIN, 14));
        veCanGiua(g, "CƠ SỞ VẬT CHẤT – HẬU CẦN", MARGIN_X, leftColW, y);
        g.setFont(font(Font.BOLD, 15));
        veCanGiua(g, "Độc lập - Tự do - Hạnh phúc", rightColX, rightColW, y);
        y += 10;

        // Gạch chân dưới tên đơn vị và tiêu ngữ
        g.setColor(MAU_CHU_CHINH);
        g.setStroke(new BasicStroke(1.2f));
        int leftLineW = 160;
        g.drawLine(
                MARGIN_X + (leftColW - leftLineW) / 2,
                y,
                MARGIN_X + (leftColW + leftLineW) / 2,
                y);
        int rightLineW = 200;
        g.drawLine(
                rightColX + (rightColW - rightLineW) / 2,
                y,
                rightColX + (rightColW + rightLineW) / 2,
                y);
        y += 28;

        // Ngày tháng năm lập báo cáo (in nghiêng, căn phải dưới Quốc hiệu)
        LocalDate ngayLap = baoCao.ngayLapBaoCao() == null ? LocalDate.now() : baoCao.ngayLapBaoCao();
        String dongNgayThang = String.format(
                "Hà Nội, ngày %02d tháng %02d năm %04d",
                ngayLap.getDayOfMonth(),
                ngayLap.getMonthValue(),
                ngayLap.getYear());
        g.setFont(font(Font.ITALIC, 15));
        g.setColor(MAU_CHU_CHINH);
        veCanGiua(g, dongNgayThang, rightColX, rightColW, y);
        y += 42;

        // 1. Tiêu đề chính: BÁO CÁO MƯỢN – TRẢ CƠ SỞ VẬT CHẤT
        g.setFont(font(Font.BOLD, 28));
        g.setColor(MAU_CHU_DAO);
        veCanGiua(g, "BÁO CÁO MƯỢN – TRẢ CƠ SỞ VẬT CHẤT", MARGIN_X, contentWidth, y);
        y += 36;

        // 2. Khối thông tin Người lập báo cáo (TV trong CLB), Chức vụ, Bộ phận, Người nhận (Ban Chủ nhiệm) & Thời gian
        NguoiLapBaoCao nguoiLap = baoCao.nguoiLapBaoCao() == null
                ? NguoiLapBaoCao.MAC_DINH
                : baoCao.nguoiLapBaoCao();
        BoLocBaoCaoMuonTra loc = baoCao.boLoc();
        int infoX = MARGIN_X + 70;
        int lineGap = 25;

        veDongThongTinDauBaoCao(g, "Họ và tên người lập:", nguoiLap.hoTenHienThi(), infoX, y);
        y += lineGap;
        veDongThongTinDauBaoCao(g, "Mã sinh viên:", nguoiLap.msvHienThi(), infoX, y);
        y += lineGap;
        veDongThongTinDauBaoCao(g, "Chức vụ:", nguoiLap.chucVuHienThi(), infoX, y);
        y += lineGap;
        veDongThongTinDauBaoCao(g, "Bộ phận công tác:", nguoiLap.boPhanHienThi(), infoX, y);
        y += lineGap;
        veDongThongTinDauBaoCao(g, "Người nhận báo cáo:", "Ban Chủ nhiệm Câu lạc bộ", infoX, y);
        y += lineGap;
        String khoangThoiGian = String.format(
                "Từ ngày %s đến ngày %s (%s)",
                dinhDangNgay(loc.tuNgay()),
                dinhDangNgay(loc.denNgay()),
                loc.loaiThoiGian().nhan());
        veDongThongTinDauBaoCao(g, "Thời gian thực hiện:", khoangThoiGian, infoX, y);
        y += 28;

        // 4. Các số liệu tổng quan (6 thẻ chỉ số chia làm 2 hàng x 3 cột)
        g.setFont(font(Font.BOLD, 17));
        g.setColor(MAU_CHU_DAO);
        g.drawString("I. SỐ LIỆU THỐNG KÊ TỔNG QUAN", MARGIN_X, y);
        y += 12;

        ThongKeMuonTra tk = baoCao.thongKe();
        String[][] theChiSo = {
                {"Tổng số lượt mượn", tk.tongSoLuotMuon() + " lượt", "Số giao dịch mượn thiết bị"},
                {"Tổng số lượt trả", tk.tongSoLuotTra() + " lượt", "Số giao dịch đã hoàn trả"},
                {"Số CSVC đang được mượn", tk.soCSVCDangMuon() + " thiết bị", "Tổng số lượng chưa trả"},
                {"Số CSVC đã trả", tk.soCSVCDaTra() + " thiết bị", "Tổng số lượng đã hoàn trả"},
                {"Số trường hợp trả quá hạn", tk.soTruongHopQuaHan() + " trường hợp", "Trả muộn hoặc quá hạn chưa trả"},
                {"Số trường hợp chưa trả", tk.soTruongHopChuaTra() + " trường hợp", "Giao dịch chưa có ngày trả"}
        };
        Color[] mauVienThe = {
                new Color(30, 96, 145),
                new Color(25, 135, 84),
                new Color(230, 126, 34),
                new Color(13, 110, 253),
                new Color(220, 53, 69),
                new Color(214, 137, 16)
        };

        int gapX = 18;
        int gapY = 12;
        int cardW = (contentWidth - 2 * gapX) / 3;
        int cardH = 74;

        for (int i = 0; i < theChiSo.length; i++) {
            int row = i / 3;
            int col = i % 3;
            int cx = MARGIN_X + col * (cardW + gapX);
            int cy = y + row * (cardH + gapY);

            g.setColor(new Color(248, 251, 254));
            g.fillRoundRect(cx, cy, cardW, cardH, 10, 10);
            g.setColor(MAU_VIEN_BANG);
            g.setStroke(new BasicStroke(1.2f));
            g.drawRoundRect(cx, cy, cardW, cardH, 10, 10);

            g.setColor(mauVienThe[i]);
            g.fillRoundRect(cx, cy, 7, cardH, 6, 6);

            g.setFont(font(Font.BOLD, 14));
            g.setColor(MAU_CHU_PHU);
            g.drawString(theChiSo[i][0], cx + 18, cy + 22);

            g.setFont(font(Font.BOLD, 21));
            g.setColor(mauVienThe[i]);
            g.drawString(theChiSo[i][1], cx + 18, cy + 48);

            g.setFont(font(Font.PLAIN, 12));
            g.setColor(MAU_CHU_PHU);
            g.drawString(theChiSo[i][2], cx + 18, cy + 66);
        }

        y += 2 * cardH + gapY + 24;
        g.setFont(font(Font.BOLD, 17));
        g.setColor(MAU_CHU_DAO);
        g.drawString("II. BẢNG CHI TIẾT CÁC GIAO DỊCH MƯỢN – TRẢ", MARGIN_X, y);
        return y + 12;
    }

    private void veDongThongTinDauBaoCao(Graphics2D g, String nhan, String giaTri, int x, int y) {
        g.setFont(font(Font.BOLD, 15));
        g.setColor(MAU_CHU_CHINH);
        g.drawString(nhan, x, y);
        int labelW = g.getFontMetrics().stringWidth(nhan);
        g.setFont(font(Font.PLAIN, 15));
        g.drawString(giaTri == null ? "" : giaTri, x + labelW + 10, y);
    }

    private int veTieuDeTrangTiepTheo(
            Graphics2D g,
            KetQuaBaoCaoMuonTra baoCao,
            int soTrang,
            int tongSoTrang,
            int startY) {
        int contentWidth = PAGE_WIDTH_PX - 2 * MARGIN_X;
        int y = startY;

        g.setFont(font(Font.BOLD, 18));
        g.setColor(MAU_CHU_DAO);
        g.drawString("BÁO CÁO MƯỢN – TRẢ CƠ SỞ VẬT CHẤT (Tiếp theo)", MARGIN_X, y);

        BoLocBaoCaoMuonTra loc = baoCao.boLoc();
        String phanPhu = String.format(
                "Kỳ báo cáo: %s - %s (%s)",
                dinhDangNgay(loc.tuNgay()),
                dinhDangNgay(loc.denNgay()),
                loc.loaiThoiGian().nhan());
        g.setFont(font(Font.PLAIN, 14));
        g.setColor(MAU_CHU_PHU);
        veCanPhai(g, phanPhu, MARGIN_X + contentWidth, y);

        y += 12;
        g.setColor(MAU_VIEN_BANG);
        g.setStroke(new BasicStroke(1.2f));
        g.drawLine(MARGIN_X, y, MARGIN_X + contentWidth, y);
        return y + 18;
    }

    private int veBangChiTiet(
            Graphics2D g,
            List<BaoCaoMuonTraItem> cacDong,
            int sttBatDau,
            int startY) {
        int contentWidth = PAGE_WIDTH_PX - 2 * MARGIN_X;
        // 9 cột: STT, Mã phiếu, Tên CSVC, Người mượn, Số lượng, Ngày mượn, Dự kiến trả, Ngày trả thực tế, Trạng thái (tổng = 1140)
        int[] doRongCot = {46, 88, 236, 206, 86, 106, 108, 144, 120};
        String[] tieuDeCot = {
                "STT",
                "Mã phiếu",
                "Tên cơ sở vật chất",
                "Người mượn",
                "Số lượng",
                "Ngày mượn",
                "Dự kiến trả",
                "Ngày trả thực tế",
                "Trạng thái"
        };

        int headerH = 44;
        int minRowH = 50;
        int y = startY;

        // Vẽ nền header bảng
        g.setColor(MAU_NEN_TIEU_DE_BANG);
        g.fillRect(MARGIN_X, y, contentWidth, headerH);

        g.setFont(font(Font.BOLD, 14));
        g.setColor(Color.WHITE);
        int x = MARGIN_X;
        for (int c = 0; c < tieuDeCot.length; c++) {
            veChuTrongO(g, tieuDeCot[c], x, y, doRongCot[c], headerH, true);
            x += doRongCot[c];
        }

        // Đường viền header
        g.setColor(MAU_VIEN_BANG);
        g.setStroke(new BasicStroke(1.2f));
        g.drawRect(MARGIN_X, y, contentWidth, headerH);
        x = MARGIN_X;
        for (int c = 0; c < doRongCot.length - 1; c++) {
            x += doRongCot[c];
            g.drawLine(x, y, x, y + headerH);
        }
        y += headerH;

        if (cacDong.isEmpty()) {
            int emptyH = 76;
            g.setColor(MAU_DONG_CHAN);
            g.fillRect(MARGIN_X, y, contentWidth, emptyH);
            g.setColor(MAU_VIEN_BANG);
            g.drawRect(MARGIN_X, y, contentWidth, emptyH);
            g.setFont(font(Font.ITALIC, 15));
            g.setColor(MAU_CHU_PHU);
            veCanGiua(
                    g,
                    "Không có dữ liệu giao dịch mượn – trả nào trong khoảng thời gian đã chọn.",
                    MARGIN_X,
                    contentWidth,
                    y + 44);
            return y + emptyH;
        }

        for (int i = 0; i < cacDong.size(); i++) {
            BaoCaoMuonTraItem item = cacDong.get(i);
            int stt = sttBatDau + i;

            g.setFont(font(Font.PLAIN, 13));
            FontMetrics fm = g.getFontMetrics();
            List<String> dongTenCsvt = tachTenVaMa(
                    item.tenThietBi(),
                    item.maThietBi(),
                    fm,
                    doRongCot[2] - 14);
            List<String> dongNguoiMuon = tachTenVaMa(
                    item.tenNguoiMuon(),
                    item.msvNguoiMuon(),
                    fm,
                    doRongCot[3] - 14);

            int soDongToiDa = Math.max(dongTenCsvt.size(), dongNguoiMuon.size());
            int rowH = Math.max(minRowH, 14 + soDongToiDa * 18);

            // Nền dòng chẵn / lẻ
            g.setColor(i % 2 == 1 ? MAU_DONG_CHAN : Color.WHITE);
            g.fillRect(MARGIN_X, y, contentWidth, rowH);

            String[] giaTri = {
                    String.valueOf(stt),
                    item.maPhieuMuon(),
                    "",
                    "",
                    String.valueOf(item.soLuongMuon()),
                    dinhDangNgay(item.ngayMuon()),
                    dinhDangNgay(item.ngayHenTra()),
                    item.ngayTraThucTe() == null ? "Chưa trả" : dinhDangNgay(item.ngayTraThucTe()),
                    item.trangThai()
            };

            int cx = MARGIN_X;
            for (int c = 0; c < giaTri.length; c++) {
                if (c == 8) {
                    veBadgeTrangThai(g, item, cx, y, doRongCot[c], rowH);
                } else if (c == 2) {
                    veTenKemMaPhiaDuoi(g, dongTenCsvt, cx, y, doRongCot[c], rowH);
                } else if (c == 3) {
                    veTenKemMaPhiaDuoi(g, dongNguoiMuon, cx, y, doRongCot[c], rowH);
                } else {
                    g.setFont(font(c == 1 ? Font.BOLD : Font.PLAIN, 13));
                    g.setColor(c == 7 && item.ngayTraThucTe() == null ? MAU_QUA_HAN_CHU : MAU_CHU_CHINH);
                    boolean canGiua = (c == 0 || c == 1 || c == 4 || c == 5 || c == 6 || c == 7);
                    veChuTrongO(g, giaTri[c], cx, y, doRongCot[c], rowH, canGiua);
                }
                cx += doRongCot[c];
            }

            // Viền ô
            g.setColor(MAU_VIEN_BANG);
            g.drawRect(MARGIN_X, y, contentWidth, rowH);
            cx = MARGIN_X;
            for (int c = 0; c < doRongCot.length - 1; c++) {
                cx += doRongCot[c];
                g.drawLine(cx, y, cx, y + rowH);
            }

            y += rowH;
        }

        return y;
    }

    private void veBadgeTrangThai(
            Graphics2D g,
            BaoCaoMuonTraItem item,
            int x,
            int y,
            int w,
            int h) {
        Color nen;
        Color chu;
        if (item.laQuaHan()) {
            nen = MAU_QUA_HAN_NEN;
            chu = MAU_QUA_HAN_CHU;
        } else if (item.daTra()) {
            nen = MAU_DA_TRA_NEN;
            chu = MAU_DA_TRA_CHU;
        } else {
            nen = MAU_CHUA_TRA_NEN;
            chu = MAU_CHUA_TRA_CHU;
        }

        int padX = 6;
        int badgeH = 28;
        int badgeY = y + (h - badgeH) / 2;
        g.setColor(nen);
        g.fillRoundRect(x + padX, badgeY, w - 2 * padX, badgeH, 8, 8);
        g.setFont(font(Font.BOLD, 12));
        g.setColor(chu);
        veChuTrongO(g, item.trangThai(), x, y, w, h, true);
    }

    private void vePhanTongKetCuoiBaoCao(Graphics2D g, KetQuaBaoCaoMuonTra baoCao, int startY) {
        int contentWidth = PAGE_WIDTH_PX - 2 * MARGIN_X;
        int y = startY;
        ThongKeMuonTra tk = baoCao.thongKe();

        // Hộp tổng kết cuối báo cáo
        int boxH = 98;
        g.setColor(new Color(243, 248, 252));
        g.fillRoundRect(MARGIN_X, y, contentWidth, boxH, 10, 10);
        g.setColor(MAU_VIEN_BANG);
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(MARGIN_X, y, contentWidth, boxH, 10, 10);

        g.setFont(font(Font.BOLD, 16));
        g.setColor(MAU_CHU_DAO);
        g.drawString("III. TỔNG KẾT VÀ NHẬN XÉT CUỐI BÁO CÁO", MARGIN_X + 18, y + 26);

        g.setFont(font(Font.PLAIN, 14));
        g.setColor(MAU_CHU_CHINH);
        String dong1 = String.format(
                "• Tổng số giao dịch mượn trong kỳ: %d lượt (Tổng số lượng: %d thiết bị) — Đã trả: %d lượt (%d thiết bị), Chưa trả: %d trường hợp (%d thiết bị).",
                tk.tongSoLuotMuon(),
                tk.tongSoLuongMuon(),
                tk.tongSoLuotTra(),
                tk.soCSVCDaTra(),
                tk.soTruongHopChuaTra(),
                tk.soCSVCDangMuon());
        String dong2 = String.format(
                "• Tình hình tuân thủ hạn trả: Có %d trường hợp quá hạn (bao gồm các lượt trả muộn sau ngày hẹn trả hoặc quá hạn chưa trả).",
                tk.soTruongHopQuaHan());
        g.drawString(dong1, MARGIN_X + 18, y + 54);
        g.drawString(dong2, MARGIN_X + 18, y + 80);

        // Chữ ký cuối báo cáo theo bố cục hình mẫu:
        // Bên trái: Người nhận báo cáo (BAN CHỦ NHIỆM CÂU LẠC BỘ) | Bên phải: NGƯỜI LẬP BÁO CÁO (Thành viên CLB)
        int sigY = y + boxH + 32;
        if (sigY + 95 < PAGE_HEIGHT_PX - 60) {
            int colW = contentWidth / 2;
            NguoiLapBaoCao nguoiLap = baoCao.nguoiLapBaoCao() == null
                    ? NguoiLapBaoCao.MAC_DINH
                    : baoCao.nguoiLapBaoCao();

            g.setFont(font(Font.BOLD, 15));
            g.setColor(MAU_CHU_CHINH);
            veCanGiua(g, "BAN CHỦ NHIỆM CÂU LẠC BỘ", MARGIN_X, colW, sigY);
            veCanGiua(g, "NGƯỜI LẬP BÁO CÁO", MARGIN_X + colW, colW, sigY);

            g.setFont(font(Font.ITALIC, 13));
            g.setColor(MAU_CHU_PHU);
            veCanGiua(g, "(Người nhận báo cáo - Ký và xác nhận)", MARGIN_X, colW, sigY + 22);
            veCanGiua(g, "(Ký và ghi rõ họ tên)", MARGIN_X + colW, colW, sigY + 22);

            if (sigY + 95 < PAGE_HEIGHT_PX - 55 && nguoiLap.hoVaTen() != null && !nguoiLap.hoVaTen().isBlank()) {
                g.setFont(font(Font.BOLD, 14));
                g.setColor(MAU_CHU_CHINH);
                veCanGiua(g, nguoiLap.hoVaTen(), MARGIN_X + colW, colW, sigY + 90);
            }
        }
    }

    private void veChanTrang(Graphics2D g, KetQuaBaoCaoMuonTra baoCao, int soTrang, int tongSoTrang) {
        int contentWidth = PAGE_WIDTH_PX - 2 * MARGIN_X;
        int y = PAGE_HEIGHT_PX - 46;

        g.setColor(MAU_VIEN_BANG);
        g.setStroke(new BasicStroke(1.0f));
        g.drawLine(MARGIN_X, y - 16, MARGIN_X + contentWidth, y - 16);

        g.setFont(font(Font.PLAIN, 13));
        g.setColor(MAU_CHU_PHU);
        g.drawString(
                "Báo cáo mượn – trả cơ sở vật chất | Ngày lập: " + dinhDangNgay(baoCao.ngayLapBaoCao()),
                MARGIN_X,
                y);
        veCanPhai(g, "Trang " + soTrang + " / " + tongSoTrang, MARGIN_X + contentWidth, y);
    }

    private void veChuTrongO(
            Graphics2D g,
            String text,
            int x,
            int y,
            int width,
            int height,
            boolean canGiua) {
        String safe = text == null ? "" : text;
        FontMetrics fm = g.getFontMetrics();
        int maxTextWidth = width - 14;
        String rutGon = catChuoiVuaO(safe, fm, maxTextWidth);
        int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent();
        if (canGiua) {
            int textW = fm.stringWidth(rutGon);
            g.drawString(rutGon, x + Math.max(4, (width - textW) / 2), textY);
        } else {
            g.drawString(rutGon, x + 8, textY);
        }
    }

    /**
     * Vẽ ô gồm tên ở trên và mã (mã thiết bị hoặc mã sinh viên) căn chính giữa ở phía dưới.
     */
    private void veTenKemMaPhiaDuoi(
            Graphics2D g,
            List<String> cacDong,
            int x,
            int y,
            int width,
            int height) {
        if (cacDong == null || cacDong.isEmpty()) {
            return;
        }
        g.setFont(font(Font.PLAIN, 13));
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();
        int totalH = cacDong.size() * lineHeight;
        int startY = y + (height - totalH) / 2 + fm.getAscent();

        for (int i = 0; i < cacDong.size(); i++) {
            String dong = cacDong.get(i);
            boolean laDongMaCuoi = (cacDong.size() > 1 && i == cacDong.size() - 1 && dong.startsWith("(") && dong.endsWith(")"));
            if (laDongMaCuoi) {
                g.setFont(font(Font.PLAIN, 12));
                g.setColor(MAU_CHU_PHU);
            } else {
                g.setFont(font(Font.PLAIN, 13));
                g.setColor(MAU_CHU_CHINH);
            }
            FontMetrics curFm = g.getFontMetrics();
            int textW = curFm.stringWidth(dong);
            int drawX = x + Math.max(4, (width - textW) / 2);
            g.drawString(dong, drawX, startY + i * lineHeight);
        }
    }

    private static List<String> tachTenVaMa(String ten, String ma, FontMetrics fm, int maxWidth) {
        String safeTen = ten == null ? "" : ten.trim();
        String safeMa = ma == null ? "" : ma.trim();
        List<String> ketQua = new ArrayList<>();
        if (!safeTen.isEmpty()) {
            ketQua.addAll(tachDongVuaO(safeTen, fm, maxWidth));
        }
        if (!safeMa.isEmpty()) {
            ketQua.add("(" + safeMa + ")");
        }
        if (ketQua.isEmpty()) {
            ketQua.add("");
        }
        return ketQua;
    }

    private static List<String> tachDongVuaO(String text, FontMetrics fm, int maxWidth) {
        String safe = text == null ? "" : text.trim();
        if (safe.isEmpty() || fm.stringWidth(safe) <= maxWidth) {
            return List.of(safe);
        }

        List<String> ketQua = new ArrayList<>();
        String[] tu = safe.split("\\s+");
        StringBuilder dongHienTai = new StringBuilder();

        for (String w : tu) {
            if (dongHienTai.length() == 0) {
                dongHienTai.append(w);
            } else {
                String thuGhep = dongHienTai + " " + w;
                if (fm.stringWidth(thuGhep) <= maxWidth) {
                    dongHienTai.append(" ").append(w);
                } else {
                    ketQua.add(dongHienTai.toString());
                    dongHienTai.setLength(0);
                    dongHienTai.append(w);
                }
            }
        }
        if (dongHienTai.length() > 0) {
            ketQua.add(dongHienTai.toString());
        }
        return ketQua;
    }

    private static String catChuoiVuaO(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth || text.length() <= 4) {
            return text;
        }
        String ellipsis = "...";
        int low = 0;
        int high = text.length();
        while (low < high) {
            int mid = (low + high + 1) / 2;
            String candidate = text.substring(0, mid) + ellipsis;
            if (fm.stringWidth(candidate) <= maxWidth) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return text.substring(0, Math.max(1, low)) + ellipsis;
    }

    private static void veCanGiua(Graphics2D g, String text, int x, int width, int y) {
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g.drawString(text, x + (width - textWidth) / 2, y);
    }

    private static void veCanPhai(Graphics2D g, String text, int rightX, int y) {
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g.drawString(text, rightX - textWidth, y);
    }

    private Font font(int style, int size) {
        return new Font(fontFamily, style, size);
    }

    private static String chonFontTiengViet() {
        try {
            Set<String> available = Arrays.stream(
                            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.ROOT))
                    .map(s -> s.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());
            String[] uuTien = {"Segoe UI", "Arial", "Tahoma", "Times New Roman", "DejaVu Sans"};
            for (String f : uuTien) {
                if (available.contains(f.toLowerCase(Locale.ROOT))) {
                    return f;
                }
            }
        } catch (RuntimeException ignored) {
        }
        return Font.SANS_SERIF;
    }

    private static String dinhDangNgay(LocalDate date) {
        return date == null ? "—" : date.format(DINH_DANG_NGAY);
    }

    private static Path chuanHoaDuoiPdf(Path path) {
        String fileName = path.getFileName().toString();
        if (fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            return path;
        }
        return path.resolveSibling(fileName + ".pdf");
    }

    private static String taoVanBanDinhKem(KetQuaBaoCaoMuonTra baoCao, int soTrang) {
        StringBuilder sb = new StringBuilder();
        BoLocBaoCaoMuonTra loc = baoCao.boLoc();
        ThongKeMuonTra tk = baoCao.thongKe();
        NguoiLapBaoCao nguoiLap = baoCao.nguoiLapBaoCao() == null
                ? NguoiLapBaoCao.MAC_DINH
                : baoCao.nguoiLapBaoCao();
        sb.append("BÁO CÁO MƯỢN – TRẢ CƠ SỞ VẬT CHẤT\n");
        sb.append("Người lập báo cáo: ").append(nguoiLap.hoTenKemMsv())
                .append(" | Chức vụ: ").append(nguoiLap.chucVuHienThi())
                .append(" | Bộ phận: ").append(nguoiLap.boPhanHienThi()).append("\n");
        sb.append("Người nhận báo cáo: Ban Chủ nhiệm Câu lạc bộ\n");
        sb.append("Khoảng thời gian thống kê: ")
                .append(dinhDangNgay(loc.tuNgay()))
                .append(" - ")
                .append(dinhDangNgay(loc.denNgay()))
                .append(" (")
                .append(loc.loaiThoiGian().nhan())
                .append(")\n");
        sb.append("Ngày lập báo cáo: ").append(dinhDangNgay(baoCao.ngayLapBaoCao())).append("\n");
        sb.append("Tổng số trang: ").append(soTrang).append("\n");
        sb.append("Tổng số lượt mượn: ").append(tk.tongSoLuotMuon()).append("\n");
        sb.append("Tổng số lượt trả: ").append(tk.tongSoLuotTra()).append("\n");
        sb.append("Số cơ sở vật chất đang được mượn: ").append(tk.soCSVCDangMuon()).append("\n");
        sb.append("Số cơ sở vật chất đã trả: ").append(tk.soCSVCDaTra()).append("\n");
        sb.append("Số trường hợp trả quá hạn: ").append(tk.soTruongHopQuaHan()).append("\n");
        sb.append("Số trường hợp chưa trả: ").append(tk.soTruongHopChuaTra()).append("\n");
        List<BaoCaoMuonTraItem> items = baoCao.danhSachChiTiet();
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                BaoCaoMuonTraItem it = items.get(i);
                sb.append(i + 1)
                        .append(" | ")
                        .append(it.maPhieuMuon())
                        .append(" | ")
                        .append(it.tenCoSoVatChatHienThi())
                        .append(" | ")
                        .append(it.nguoiMuonHienThi())
                        .append(" | SL: ")
                        .append(it.soLuongMuon())
                        .append(" | Mượn: ")
                        .append(dinhDangNgay(it.ngayMuon()))
                        .append(" | Hẹn trả: ")
                        .append(dinhDangNgay(it.ngayHenTra()))
                        .append(" | Trả TT: ")
                        .append(it.ngayTraThucTe() == null ? "Chưa trả" : dinhDangNgay(it.ngayTraThucTe()))
                        .append(" | ")
                        .append(it.trangThaiHienThi())
                        .append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Ghi cấu trúc file PDF 1.4 chuẩn (Catalog, Pages, từng Page + Image XObject FlateDecode + Metadata UTF-8).
     */
    private static void ghiTaiLieuPdf(
            List<BufferedImage> pages,
            String utf8SummaryText,
            OutputStream out) throws IOException {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream(256 * 1024);
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0); // Object 0

        writeAscii(pdf, "%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n");

        int pageCount = pages.size();
        // Object 1: Catalog
        // Object 2: Pages
        // Với mỗi trang i (0..pageCount-1):
        //   pageObj = 3 + i * 3
        //   contentObj = 4 + i * 3
        //   imageObj = 5 + i * 3
        // Object cuối: Info/Metadata stream chứa văn bản tiếng Việt UTF-8 đầy đủ
        int metadataObjId = 3 + pageCount * 3;

        // Obj 1: Catalog
        offsets.add(pdf.size());
        writeAscii(pdf, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R /Metadata " + metadataObjId + " 0 R >>\nendobj\n");

        // Obj 2: Pages
        offsets.add(pdf.size());
        StringBuilder kids = new StringBuilder("[");
        for (int i = 0; i < pageCount; i++) {
            if (i > 0) {
                kids.append(' ');
            }
            kids.append(3 + i * 3).append(" 0 R");
        }
        kids.append(']');
        writeAscii(
                pdf,
                "2 0 obj\n<< /Type /Pages /Kids " + kids + " /Count " + pageCount + " >>\nendobj\n");

        for (int i = 0; i < pageCount; i++) {
            BufferedImage img = pages.get(i);
            int pageObj = 3 + i * 3;
            int contentObj = 4 + i * 3;
            int imageObj = 5 + i * 3;
            String imgName = "/Im" + (i + 1);

            // Page object
            offsets.add(pdf.size());
            writeAscii(
                    pdf,
                    pageObj + " 0 obj\n"
                            + "<< /Type /Page /Parent 2 0 R "
                            + "/MediaBox [0 0 " + PDF_PAGE_WIDTH_PT + " " + PDF_PAGE_HEIGHT_PT + "] "
                            + "/Resources << /XObject << " + imgName + " " + imageObj + " 0 R >> >> "
                            + "/Contents " + contentObj + " 0 R >>\n"
                            + "endobj\n");

            // Content stream vẽ ảnh phủ kín trang A4
            String contentCmd = "q\n"
                    + PDF_PAGE_WIDTH_PT + " 0 0 " + PDF_PAGE_HEIGHT_PT + " 0 0 cm\n"
                    + imgName + " Do\n"
                    + "Q\n";
            byte[] contentBytes = contentCmd.getBytes(StandardCharsets.US_ASCII);
            offsets.add(pdf.size());
            writeAscii(pdf, contentObj + " 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n");
            pdf.write(contentBytes);
            writeAscii(pdf, "\nendstream\nendobj\n");

            // Image XObject (RGB nén FlateDecode không suy hao)
            byte[] compressedRgb = nenAnhRgbFlate(img);
            offsets.add(pdf.size());
            writeAscii(
                    pdf,
                    imageObj + " 0 obj\n"
                            + "<< /Type /XObject /Subtype /Image "
                            + "/Width " + img.getWidth() + " "
                            + "/Height " + img.getHeight() + " "
                            + "/ColorSpace /DeviceRGB "
                            + "/BitsPerComponent 8 "
                            + "/Filter /FlateDecode "
                            + "/Length " + compressedRgb.length + " >>\n"
                            + "stream\n");
            pdf.write(compressedRgb);
            writeAscii(pdf, "\nendstream\nendobj\n");
        }

        // Metadata object lưu nội dung UTF-8 đầy đủ của báo cáo
        byte[] metaBytes = utf8SummaryText.getBytes(StandardCharsets.UTF_8);
        offsets.add(pdf.size());
        writeAscii(
                pdf,
                metadataObjId + " 0 obj\n<< /Type /Metadata /Subtype /XML /Length " + metaBytes.length + " >>\nstream\n");
        pdf.write(metaBytes);
        writeAscii(pdf, "\nendstream\nendobj\n");

        // Bảng xref và trailer
        int xrefOffset = pdf.size();
        int totalObjects = offsets.size();
        writeAscii(pdf, "xref\n0 " + totalObjects + "\n");
        writeAscii(pdf, "0000000000 65535 f \n");
        for (int i = 1; i < totalObjects; i++) {
            writeAscii(pdf, String.format(Locale.ROOT, "%010d 00000 n \n", offsets.get(i)));
        }

        writeAscii(
                pdf,
                "trailer\n<< /Size " + totalObjects + " /Root 1 0 R >>\n"
                        + "startxref\n" + xrefOffset + "\n%%EOF\n");

        pdf.writeTo(out);
    }

    private static byte[] nenAnhRgbFlate(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] argb = img.getRGB(0, 0, w, h, null, 0, w);
        byte[] raw = new byte[w * h * 3];
        int idx = 0;
        for (int pixel : argb) {
            raw[idx++] = (byte) ((pixel >> 16) & 0xFF);
            raw[idx++] = (byte) ((pixel >> 8) & 0xFF);
            raw[idx++] = (byte) (pixel & 0xFF);
        }

        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        deflater.setInput(raw);
        deflater.finish();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(raw.length / 8);
        byte[] buf = new byte[16384];
        while (!deflater.finished()) {
            int count = deflater.deflate(buf);
            baos.write(buf, 0, count);
        }
        deflater.end();
        return baos.toByteArray();
    }

    private static void writeAscii(ByteArrayOutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.ISO_8859_1));
    }
}
