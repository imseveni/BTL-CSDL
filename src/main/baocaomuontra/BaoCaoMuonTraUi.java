package baocaomuontra;

import baocaomuontra.BoLocBaoCaoMuonTra.LoaiThoiGian;
import baocaomuontra.BoLocBaoCaoMuonTra.SapXepTheo;
import baocaomuontra.BoLocBaoCaoMuonTra.TrangThaiLoc;
import common.ConsoleUtil;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Giao diện Console và khởi chạy giao diện đồ họa cho module Báo cáo mượn – trả cơ sở vật chất.
 */
public class BaoCaoMuonTraUi {

    private final BaoCaoMuonTraService service;
    private final BaoCaoMuonTraPdfExporter pdfExporter;
    private KetQuaBaoCaoMuonTra baoCaoGanNhat;

    public BaoCaoMuonTraUi() {
        this(new BaoCaoMuonTraService(), new BaoCaoMuonTraPdfExporter());
    }

    public BaoCaoMuonTraUi(BaoCaoMuonTraService service, BaoCaoMuonTraPdfExporter pdfExporter) {
        this.service = service;
        this.pdfExporter = pdfExporter;
    }

    public void run(Scanner in) {
        while (true) {
            System.out.println("\n--- BÁO CÁO MƯỢN – TRẢ CƠ SỞ VẬT CHẤT ---");
            System.out.println("1. Mở giao diện đồ họa Báo cáo mượn – trả (Swing + Xuất PDF)");
            System.out.println("2. Xem báo cáo mượn – trả theo khoảng thời gian (Console)");
            System.out.println("3. Xuất báo cáo ra file PDF");
            System.out.println("0. Quay lại");
            int chon = ConsoleUtil.readInt(in, "Chọn: ");
            try {
                switch (chon) {
                    case 1 -> moGiaoDienDoHoa();
                    case 2 -> xemBaoCaoConsole(in);
                    case 3 -> xuatBaoCaoPdfConsole(in);
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Lựa chọn không hợp lệ.");
                }
            } catch (RuntimeException | IOException e) {
                System.out.println("  [!] " + e.getMessage());
            }
        }
    }

    private void moGiaoDienDoHoa() {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Môi trường hiện tại không hỗ trợ màn hình đồ họa. Vui lòng dùng mục 2 và 3.");
            return;
        }
        CountDownLatch closed = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = BaoCaoMuonTraApp.createStandaloneFrame(JFrame.DISPOSE_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        closed.countDown();
                    }
                });
                frame.setVisible(true);
            } catch (RuntimeException e) {
                System.out.println("Không mở được cửa sổ đồ họa: " + e.getMessage());
                closed.countDown();
            }
        });
        System.out.println("Đã mở cửa sổ Báo cáo mượn – trả cơ sở vật chất. Đóng cửa sổ để quay lại menu.");
        try {
            closed.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void xemBaoCaoConsole(Scanner in) {
        BoLocBaoCaoMuonTra boLoc = nhapBoLoc(in);
        KetQuaBaoCaoMuonTra ketQua = service.lapBaoCao(boLoc);
        this.baoCaoGanNhat = ketQua;
        inKetQuaBaoCao(ketQua);
    }

    private void xuatBaoCaoPdfConsole(Scanner in) throws IOException {
        KetQuaBaoCaoMuonTra ketQua = this.baoCaoGanNhat;
        if (ketQua == null) {
            System.out.println("Nhập khoảng thời gian báo cáo cần xuất PDF:");
            BoLocBaoCaoMuonTra boLoc = nhapBoLoc(in);
            ketQua = service.lapBaoCao(boLoc);
            this.baoCaoGanNhat = ketQua;
            inKetQuaBaoCao(ketQua);
        }

        String macDinh = BaoCaoMuonTraPdfExporter.tenFileGoiY(ketQua.boLoc());
        System.out.print("Đường dẫn lưu file PDF (Enter = " + macDinh + "): ");
        String duongDanNhap = in.nextLine().trim();
        Path dich = Path.of(duongDanNhap.isEmpty() ? macDinh : duongDanNhap);
        Path daLuu = pdfExporter.xuatFile(ketQua, dich);
        System.out.println("  -> Đã xuất file PDF thành công tại: " + daLuu.toAbsolutePath());
    }

    private BoLocBaoCaoMuonTra nhapBoLoc(Scanner in) {
        LocalDate tuNgay = BaoCaoMuonTraSwingUi.phanTichNgay(
                ConsoleUtil.readLine(in, "Ngày bắt đầu (yyyy-MM-dd hoặc dd/MM/yyyy): "), "Ngày bắt đầu");
        LocalDate denNgay = BaoCaoMuonTraSwingUi.phanTichNgay(
                ConsoleUtil.readLine(in, "Ngày kết thúc (yyyy-MM-dd hoặc dd/MM/yyyy): "), "Ngày kết thúc");
        BaoCaoMuonTraService.kiemTraKhoangThoiGian(tuNgay, denNgay);

        System.out.println("Thống kê theo: 1. Ngày mượn (mặc định) | 2. Ngày trả");
        String chonLoai = in.nextLine().trim();
        LoaiThoiGian loai = "2".equals(chonLoai) ? LoaiThoiGian.NGAY_TRA : LoaiThoiGian.NGAY_MUON;

        System.out.print("Từ khóa tìm kiếm (Enter = tất cả): ");
        String tuKhoa = in.nextLine().trim();

        System.out.println("Lọc trạng thái: 1. Tất cả | 2. Đã trả | 3. Chưa trả | 4. Quá hạn");
        String chonTrangThai = in.nextLine().trim();
        TrangThaiLoc trangThai = switch (chonTrangThai) {
            case "2" -> TrangThaiLoc.DA_TRA;
            case "3" -> TrangThaiLoc.CHUA_TRA;
            case "4" -> TrangThaiLoc.QUA_HAN;
            default -> TrangThaiLoc.TAT_CA;
        };

        System.out.println("Sắp xếp: 1. Ngày mượn tăng | 2. Ngày mượn giảm | 3. Ngày trả tăng | 4. Ngày trả giảm");
        String chonSapXep = in.nextLine().trim();
        SapXepTheo sapXep = switch (chonSapXep) {
            case "2" -> SapXepTheo.NGAY_MUON_GIAM;
            case "3" -> SapXepTheo.NGAY_TRA_TANG;
            case "4" -> SapXepTheo.NGAY_TRA_GIAM;
            default -> SapXepTheo.NGAY_MUON_TANG;
        };

        return new BoLocBaoCaoMuonTra(tuNgay, denNgay, loai, tuKhoa, trangThai, sapXep);
    }

    public static void inKetQuaBaoCao(KetQuaBaoCaoMuonTra kq) {
        BoLocBaoCaoMuonTra loc = kq.boLoc();
        ThongKeMuonTra tk = kq.thongKe();
        System.out.println("\n==========================================================================================================");
        System.out.println("                             BÁO CÁO MƯỢN – TRẢ CƠ SỞ VẬT CHẤT");
        System.out.printf("  Khoảng thời gian (%s): %s -> %s | Ngày lập: %s%n",
                loc.loaiThoiGian().nhan(), loc.tuNgay(), loc.denNgay(), kq.ngayLapBaoCao());
        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.printf("  • Tổng số lượt mượn          : %d lượt%n", tk.tongSoLuotMuon());
        System.out.printf("  • Tổng số lượt trả           : %d lượt%n", tk.tongSoLuotTra());
        System.out.printf("  • Số CSVC đang được mượn     : %d thiết bị%n", tk.soCSVCDangMuon());
        System.out.printf("  • Số CSVC đã trả             : %d thiết bị%n", tk.soCSVCDaTra());
        System.out.printf("  • Số trường hợp trả quá hạn  : %d trường hợp%n", tk.soTruongHopQuaHan());
        System.out.printf("  • Số trường hợp chưa trả     : %d trường hợp%n", tk.soTruongHopChuaTra());
        System.out.println("----------------------------------------------------------------------------------------------------------");

        List<BaoCaoMuonTraItem> ds = kq.danhSachChiTiet();
        if (ds.isEmpty()) {
            System.out.println("  (Không có dữ liệu giao dịch mượn – trả trong khoảng thời gian đã chọn)");
            System.out.println("==========================================================================================================");
            return;
        }

        System.out.printf("  %-4s %-10s %-24s %-22s %4s %-11s %-11s %-11s %-18s%n",
                "STT", "Mã phiếu", "Tên cơ sở vật chất", "Người mượn", "SL",
                "Ngày mượn", "Dự kiến trả", "Ngày trả TT", "Trạng thái");
        System.out.println("  " + "-".repeat(122));
        for (int i = 0; i < ds.size(); i++) {
            BaoCaoMuonTraItem it = ds.get(i);
            System.out.printf("  %-4d %-10s %-24s %-22s %4d %-11s %-11s %-11s %-18s%n",
                    i + 1,
                    it.maPhieuMuon(),
                    cat(it.tenCoSoVatChatHienThi(), 24),
                    cat(it.nguoiMuonHienThi(), 22),
                    it.soLuongMuon(),
                    it.ngayMuon(),
                    it.ngayHenTra(),
                    it.ngayTraThucTe() == null ? "Chưa trả" : it.ngayTraThucTe(),
                    it.trangThaiHienThi());
        }
        System.out.println("==========================================================================================================");
    }

    private static String cat(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
