package baocaomuontra;

import baocaomuontra.BoLocBaoCaoMuonTra.LoaiThoiGian;
import baocaomuontra.BoLocBaoCaoMuonTra.SapXepTheo;
import baocaomuontra.BoLocBaoCaoMuonTra.TrangThaiLoc;
import common.Refreshable;
import common.SwingSupport;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện đồ họa Swing cho module Báo cáo mượn – trả cơ sở vật chất.
 */
public class BaoCaoMuonTraSwingUi extends JPanel implements Refreshable {

    private static final DateTimeFormatter DINH_DANG_ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DINH_DANG_VN = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BaoCaoMuonTraService service;
    private final BaoCaoMuonTraPdfExporter pdfExporter;

    private final JTextField tuNgayField = new JTextField(10);
    private final JTextField denNgayField = new JTextField(10);
    private final JComboBox<LoaiThoiGian> loaiThoiGianCombo = new JComboBox<>(LoaiThoiGian.values());
    private final JTextField tuKhoaField = new JTextField(16);
    private final JComboBox<TrangThaiLoc> trangThaiCombo = new JComboBox<>(TrangThaiLoc.values());
    private final JComboBox<SapXepTheo> sapXepCombo = new JComboBox<>(SapXepTheo.values());
    private final JComboBox<NguoiLapBaoCao> nguoiLapCombo = new JComboBox<>(
            new NguoiLapBaoCao[]{NguoiLapBaoCao.MAC_DINH});

    private final JLabel lblTongLuotMuon = new JLabel("0");
    private final JLabel lblTongLuotTra = new JLabel("0");
    private final JLabel lblSoCSVCDangMuon = new JLabel("0");
    private final JLabel lblSoCSVCDaTra = new JLabel("0");
    private final JLabel lblSoQuaHan = new JLabel("0");
    private final JLabel lblSoChuaTra = new JLabel("0");

    private final DefaultTableModel tableModel = SwingSupport.model(
            "STT",
            "Mã phiếu mượn",
            "Tên cơ sở vật chất",
            "Người mượn",
            "Số lượng mượn",
            "Ngày mượn",
            "Ngày dự kiến trả",
            "Ngày trả thực tế",
            "Trạng thái mượn – trả");
    private final JTable table = SwingSupport.table(tableModel, "baocaomuontra.table");
    private final JLabel statusLabel = new JLabel("Chọn khoảng thời gian và nhấn Xem báo cáo.");
    private final SwingSupport.Tasks tasks = new SwingSupport.Tasks(this, statusLabel);

    private KetQuaBaoCaoMuonTra ketQuaHienTai;

    public BaoCaoMuonTraSwingUi() {
        this(new BaoCaoMuonTraService(), new BaoCaoMuonTraPdfExporter());
    }

    public BaoCaoMuonTraSwingUi(BaoCaoMuonTraService service) {
        this(service, new BaoCaoMuonTraPdfExporter());
    }

    public BaoCaoMuonTraSwingUi(BaoCaoMuonTraService service, BaoCaoMuonTraPdfExporter pdfExporter) {
        this.service = service;
        this.pdfExporter = pdfExporter;

        setName("baocaomuontra.panel");
        setLayout(new BorderLayout(10, 10));
        setBackground(SwingSupport.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        datKhoangThoiGianMacDinh();
        ganTenThanhPhan();

        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setOpaque(false);
        topContainer.add(taoThanhBoLoc(), BorderLayout.NORTH);
        topContainer.add(taoKhuVucThongKeTongQuan(), BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        add(taoKhuVucBangChiTiet(), BorderLayout.CENTER);

        statusLabel.setName("baocaomuontra.status");
        add(statusLabel, BorderLayout.SOUTH);

        cauHinhBang();
    }

    private void datKhoangThoiGianMacDinh() {
        tuNgayField.setText("2025-01-01");
        denNgayField.setText(LocalDate.now().toString());
    }

    private void ganTenThanhPhan() {
        tuNgayField.setName("baocaomuontra.tuNgay");
        denNgayField.setName("baocaomuontra.denNgay");
        loaiThoiGianCombo.setName("baocaomuontra.loaiThoiGian");
        tuKhoaField.setName("baocaomuontra.tuKhoa");
        trangThaiCombo.setName("baocaomuontra.trangThai");
        sapXepCombo.setName("baocaomuontra.sapXep");

        lblTongLuotMuon.setName("baocaomuontra.card.tongLuotMuon");
        lblTongLuotTra.setName("baocaomuontra.card.tongLuotTra");
        lblSoCSVCDangMuon.setName("baocaomuontra.card.csvcDangMuon");
        lblSoCSVCDaTra.setName("baocaomuontra.card.csvcDaTra");
        lblSoQuaHan.setName("baocaomuontra.card.quaHan");
        lblSoChuaTra.setName("baocaomuontra.card.chuaTra");

        tuNgayField.setToolTipText("Định dạng yyyy-MM-dd hoặc dd/MM/yyyy (ví dụ: 2025-09-01)");
        denNgayField.setToolTipText("Định dạng yyyy-MM-dd hoặc dd/MM/yyyy (ví dụ: 2025-12-31)");
        tuKhoaField.setToolTipText("Tìm theo mã phiếu, mã/tên cơ sở vật chất, MSV hoặc họ tên người mượn");

        loaiThoiGianCombo.setPreferredSize(new Dimension(155, 28));
        trangThaiCombo.setPreferredSize(new Dimension(155, 28));
        sapXepCombo.setPreferredSize(new Dimension(180, 28));
        nguoiLapCombo.setName("baocaomuontra.nguoiLap");
        nguoiLapCombo.setPreferredSize(new Dimension(240, 28));
        nguoiLapCombo.setToolTipText("Chọn thành viên trong CLB lập báo cáo");

        tuKhoaField.addActionListener(e -> xemBaoCao());
        tuNgayField.addActionListener(e -> xemBaoCao());
        denNgayField.addActionListener(e -> xemBaoCao());
    }

    private JPanel taoThanhBoLoc() {
        JPanel box = new JPanel(new GridLayout(2, 1, 0, 6));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 230)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        // Dòng 1: Thời gian + Loại thống kê + Nút chọn nhanh + Xem báo cáo + Xuất PDF
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row1.setOpaque(false);
        row1.add(new JLabel("Thống kê:"));
        row1.add(loaiThoiGianCombo);
        row1.add(new JLabel("Từ ngày:"));
        row1.add(tuNgayField);
        row1.add(new JLabel("Đến ngày:"));
        row1.add(denNgayField);
        row1.add(SwingSupport.button("Xem báo cáo", "baocaomuontra.xemBaoCao", this::xemBaoCao));
        row1.add(SwingSupport.button("Xuất PDF", "baocaomuontra.xuatPdf", this::moHopThoaiXuatPdf));
        row1.add(SwingSupport.button("Hôm nay", "baocaomuontra.homNay", () -> chonNhanhKhoangNgay(
                LocalDate.now(), LocalDate.now())));
        row1.add(SwingSupport.button("Tháng này", "baocaomuontra.thangNay", () -> {
            YearMonth ym = YearMonth.now();
            chonNhanhKhoangNgay(ym.atDay(1), ym.atEndOfMonth());
        }));

        // Dòng 2: Tìm kiếm + Lọc trạng thái + Sắp xếp + Người lập báo cáo + Làm mới
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        row2.setOpaque(false);
        row2.add(new JLabel("Tìm kiếm:"));
        row2.add(tuKhoaField);
        row2.add(new JLabel("Trạng thái:"));
        row2.add(trangThaiCombo);
        row2.add(new JLabel("Sắp xếp:"));
        row2.add(sapXepCombo);
        row2.add(new JLabel("Người lập:"));
        row2.add(nguoiLapCombo);
        row2.add(SwingSupport.button("Lọc / Sắp xếp", "baocaomuontra.loc", this::xemBaoCao));
        row2.add(SwingSupport.button("Đặt lại", "baocaomuontra.datLai", this::resetAndReload));

        box.add(row1);
        box.add(row2);
        return box;
    }

    private JPanel taoKhuVucThongKeTongQuan() {
        JPanel grid = new JPanel(new GridLayout(1, 6, 10, 0));
        grid.setOpaque(false);

        grid.add(taoTheChiSo("Tổng số lượt mượn", lblTongLuotMuon, new Color(30, 96, 145)));
        grid.add(taoTheChiSo("Tổng số lượt trả", lblTongLuotTra, new Color(25, 135, 84)));
        grid.add(taoTheChiSo("Số CSVC đang mượn", lblSoCSVCDangMuon, new Color(230, 126, 34)));
        grid.add(taoTheChiSo("Số CSVC đã trả", lblSoCSVCDaTra, new Color(13, 110, 253)));
        grid.add(taoTheChiSo("Trường hợp quá hạn", lblSoQuaHan, new Color(220, 53, 69)));
        grid.add(taoTheChiSo("Trường hợp chưa trả", lblSoChuaTra, new Color(214, 137, 16)));
        return grid;
    }

    private static JPanel taoTheChiSo(String tieuDe, JLabel giaTriLabel, Color mauNhan) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, mauNhan),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(218, 226, 235)),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12))));

        JLabel title = new JLabel(tieuDe);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 12f));
        title.setForeground(new Color(90, 101, 119));

        giaTriLabel.setFont(giaTriLabel.getFont().deriveFont(Font.BOLD, 20f));
        giaTriLabel.setForeground(mauNhan);

        card.add(title, BorderLayout.NORTH);
        card.add(giaTriLabel, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(150, 66));
        return card;
    }

    private JPanel taoKhuVucBangChiTiet() {
        JPanel section = new JPanel(new BorderLayout(6, 6));
        section.setOpaque(false);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel title = new JLabel("BẢNG CHI TIẾT CÁC GIAO DỊCH MƯỢN – TRẢ CƠ SỞ VẬT CHẤT");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        head.add(title, BorderLayout.WEST);

        JLabel legend = new JLabel("Chú thích trạng thái:  [Đã trả: Xanh lá]   [Chưa trả: Vàng cam]   [Quá hạn / Trả quá hạn: Đỏ]");
        legend.setForeground(new Color(90, 101, 119));
        head.add(legend, BorderLayout.EAST);

        section.add(head, BorderLayout.NORTH);
        section.add(SwingSupport.scroll(table), BorderLayout.CENTER);
        return section;
    }

    private void cauHinhBang() {
        // Tắt RowSorter mặc định của JTable để không ghi đè thứ tự sắp xếp của ô "Sắp xếp"
        // và không làm đảo lộn cột STT hoặc sắp xếp sai chuỗi "Chưa trả".
        table.setAutoCreateRowSorter(false);
        table.setRowSorter(null);

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                if (col == 5) { // Cột Ngày mượn
                    SapXepTheo hienTai = (SapXepTheo) sapXepCombo.getSelectedItem();
                    sapXepCombo.setSelectedItem(
                            hienTai == SapXepTheo.NGAY_MUON_TANG
                                    ? SapXepTheo.NGAY_MUON_GIAM
                                    : SapXepTheo.NGAY_MUON_TANG);
                    xemBaoCao();
                } else if (col == 7) { // Cột Ngày trả thực tế
                    SapXepTheo hienTai = (SapXepTheo) sapXepCombo.getSelectedItem();
                    sapXepCombo.setSelectedItem(
                            hienTai == SapXepTheo.NGAY_TRA_TANG
                                    ? SapXepTheo.NGAY_TRA_GIAM
                                    : SapXepTheo.NGAY_TRA_TANG);
                    xemBaoCao();
                }
            }
        });

        int[] widths = {55, 110, 220, 210, 110, 105, 120, 120, 160};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        // Renderer tô màu phân biệt rõ trạng thái Đã trả, Chưa trả và Quá hạn
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(getFont().deriveFont(Font.BOLD));
                String text = value == null ? "" : value.toString();
                if (!isSelected) {
                    if (text.contains("Quá hạn") || text.contains("quá hạn")) {
                        c.setBackground(new Color(248, 215, 218));
                        c.setForeground(new Color(176, 42, 55));
                    } else if (text.startsWith(BaoCaoMuonTraItem.DA_TRA)) {
                        c.setBackground(new Color(219, 244, 228));
                        c.setForeground(new Color(21, 115, 71));
                    } else {
                        c.setBackground(new Color(255, 243, 205));
                        c.setForeground(new Color(153, 101, 0));
                    }
                }
                return c;
            }
        });
    }

    private void chonNhanhKhoangNgay(LocalDate tuNgay, LocalDate denNgay) {
        tuNgayField.setText(tuNgay.toString());
        denNgayField.setText(denNgay.toString());
        xemBaoCao();
    }

    /**
     * Đọc điều kiện lọc hiện tại trên giao diện và kiểm tra hợp lệ ngày bắt đầu – ngày kết thúc.
     */
    public BoLocBaoCaoMuonTra docBoLocTuGiaoDien() {
        LocalDate tuNgay = phanTichNgay(tuNgayField.getText(), "Ngày bắt đầu");
        LocalDate denNgay = phanTichNgay(denNgayField.getText(), "Ngày kết thúc");
        BaoCaoMuonTraService.kiemTraKhoangThoiGian(tuNgay, denNgay);

        LoaiThoiGian loai = (LoaiThoiGian) loaiThoiGianCombo.getSelectedItem();
        TrangThaiLoc trangThai = (TrangThaiLoc) trangThaiCombo.getSelectedItem();
        SapXepTheo sapXep = (SapXepTheo) sapXepCombo.getSelectedItem();
        String tuKhoa = tuKhoaField.getText() == null ? "" : tuKhoaField.getText().trim();

        return new BoLocBaoCaoMuonTra(tuNgay, denNgay, loai, tuKhoa, trangThai, sapXep);
    }

    public static LocalDate phanTichNgay(String raw, String tenTruong) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(tenTruong + " không được để trống (định dạng yyyy-MM-dd hoặc dd/MM/yyyy).");
        }
        String s = raw.trim();
        try {
            return LocalDate.parse(s, DINH_DANG_ISO);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(s, DINH_DANG_VN);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        tenTruong + " không đúng định dạng (hãy nhập yyyy-MM-dd hoặc dd/MM/yyyy, ví dụ: 2025-09-01).");
            }
        }
    }

    /**
     * Thực hiện lọc và hiển thị báo cáo theo khoảng thời gian đã chọn.
     */
    public void xemBaoCao() {
        final BoLocBaoCaoMuonTra boLoc;
        try {
            boLoc = docBoLocTuGiaoDien();
        } catch (RuntimeException e) {
            statusLabel.setText("Lỗi: " + e.getMessage());
            SwingSupport.error(this, e);
            return;
        }

        tasks.run(
                "Đang tổng hợp báo cáo mượn – trả cơ sở vật chất...",
                () -> new DuLieuTaiVe(service.lapBaoCao(boLoc), service.danhSachThanhVienClb()),
                taiVe -> {
                    capNhatDanhSachNguoiLap(taiVe.thanhVienClb());
                    NguoiLapBaoCao chon = (NguoiLapBaoCao) nguoiLapCombo.getSelectedItem();
                    hienThiKetQua(taiVe.baoCao().voiNguoiLap(chon));
                });
    }

    private record DuLieuTaiVe(KetQuaBaoCaoMuonTra baoCao, List<NguoiLapBaoCao> thanhVienClb) {
    }

    private void capNhatDanhSachNguoiLap(List<NguoiLapBaoCao> danhSachMoi) {
        if (danhSachMoi == null || danhSachMoi.isEmpty()) {
            return;
        }
        NguoiLapBaoCao dangChon = (NguoiLapBaoCao) nguoiLapCombo.getSelectedItem();
        String msvDangChon = dangChon == null ? null : dangChon.msv();

        nguoiLapCombo.removeAllItems();
        int idxChon = 0;
        for (int i = 0; i < danhSachMoi.size(); i++) {
            NguoiLapBaoCao tv = danhSachMoi.get(i);
            nguoiLapCombo.addItem(tv);
            if (msvDangChon != null && msvDangChon.equals(tv.msv()) && !"TV_CLB".equals(msvDangChon)) {
                idxChon = i;
            }
        }
        if (nguoiLapCombo.getItemCount() > 0) {
            nguoiLapCombo.setSelectedIndex(idxChon);
        }
    }

    private void hienThiKetQua(KetQuaBaoCaoMuonTra ketQua) {
        this.ketQuaHienTai = ketQua;
        ThongKeMuonTra tk = ketQua.thongKe();

        lblTongLuotMuon.setText(String.valueOf(tk.tongSoLuotMuon()));
        lblTongLuotTra.setText(String.valueOf(tk.tongSoLuotTra()));
        lblSoCSVCDangMuon.setText(String.valueOf(tk.soCSVCDangMuon()));
        lblSoCSVCDaTra.setText(String.valueOf(tk.soCSVCDaTra()));
        lblSoQuaHan.setText(String.valueOf(tk.soTruongHopQuaHan()));
        lblSoChuaTra.setText(String.valueOf(tk.soTruongHopChuaTra()));

        tableModel.setRowCount(0);
        List<BaoCaoMuonTraItem> items = ketQua.danhSachChiTiet();
        for (int i = 0; i < items.size(); i++) {
            BaoCaoMuonTraItem it = items.get(i);
            tableModel.addRow(new Object[]{
                    i + 1,
                    it.maPhieuMuon(),
                    it.tenCoSoVatChatHienThi(),
                    it.nguoiMuonHienThi(),
                    it.soLuongMuon(),
                    SwingSupport.text(it.ngayMuon()),
                    SwingSupport.text(it.ngayHenTra()),
                    it.ngayTraThucTe() == null ? "Chưa trả" : SwingSupport.text(it.ngayTraThucTe()),
                    it.trangThaiHienThi()
            });
        }

        if (items.isEmpty()) {
            statusLabel.setText(String.format(
                    "Không có giao dịch mượn – trả nào trong khoảng %s đến %s (%s).",
                    ketQua.boLoc().tuNgay(),
                    ketQua.boLoc().denNgay(),
                    ketQua.boLoc().loaiThoiGian().nhan()));
        } else {
            statusLabel.setText(String.format(
                    "Đã tải %d giao dịch (%s: %s đến %s) — Đã trả: %d, Chưa trả: %d, Quá hạn: %d.",
                    items.size(),
                    ketQua.boLoc().loaiThoiGian().nhan(),
                    ketQua.boLoc().tuNgay(),
                    ketQua.boLoc().denNgay(),
                    tk.tongSoLuotTra(),
                    tk.soTruongHopChuaTra(),
                    tk.soTruongHopQuaHan()));
        }
    }

    /**
     * Mở hộp thoại cho phép người dùng chọn nơi lưu file PDF và xuất báo cáo đang lọc.
     */
    public void moHopThoaiXuatPdf() {
        final BoLocBaoCaoMuonTra boLoc;
        try {
            boLoc = docBoLocTuGiaoDien();
        } catch (RuntimeException e) {
            statusLabel.setText("Lỗi: " + e.getMessage());
            SwingSupport.error(this, e);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn nơi lưu file PDF Báo cáo mượn – trả cơ sở vật chất");
        chooser.setFileFilter(new FileNameExtensionFilter("Tập tin PDF (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File(BaoCaoMuonTraPdfExporter.tenFileGoiY(boLoc)));

        int luaChon = chooser.showSaveDialog(this);
        if (luaChon != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
            return;
        }

        Path duongDan = chooser.getSelectedFile().toPath();
        xuatPdfDenDuongDan(boLoc, duongDan, true);
    }

    /**
     * Xuất báo cáo ra đường dẫn chỉ định (phục vụ cả hộp thoại lưu file và kiểm thử tự động).
     */
    public void xuatPdfDenDuongDan(BoLocBaoCaoMuonTra boLoc, Path duongDan, boolean hienThongBao) {
        NguoiLapBaoCao nguoiLapChon = (NguoiLapBaoCao) nguoiLapCombo.getSelectedItem();
        tasks.run(
                "Đang tạo và xuất file PDF báo cáo...",
                () -> {
                    KetQuaBaoCaoMuonTra baoCao = service.lapBaoCao(boLoc);
                    if (nguoiLapChon != null && !"TV_CLB".equals(nguoiLapChon.msv())) {
                        baoCao = baoCao.voiNguoiLap(nguoiLapChon);
                    }
                    Path saved = pdfExporter.xuatFile(baoCao, duongDan);
                    return new KetQuaXuatPdf(baoCao, saved);
                },
                kq -> {
                    hienThiKetQua(kq.baoCao());
                    statusLabel.setText("Đã xuất báo cáo PDF thành công: " + kq.duongDan().toAbsolutePath());
                    if (hienThongBao) {
                        int chon = JOptionPane.showConfirmDialog(
                                this,
                                "Đã xuất báo cáo ra file PDF thành công!\nĐường dẫn: "
                                        + kq.duongDan().toAbsolutePath()
                                        + "\n\nBạn có muốn mở file PDF này ngay không?",
                                "Xuất PDF thành công",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE);
                        if (chon == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().open(kq.duongDan().toFile());
                            } catch (Exception ignored) {
                            }
                        }
                    }
                });
    }

    private record KetQuaXuatPdf(KetQuaBaoCaoMuonTra baoCao, Path duongDan) {
    }

    public KetQuaBaoCaoMuonTra getKetQuaHienTai() {
        return ketQuaHienTai;
    }

    @Override
    public boolean isBusy() {
        return tasks.isBusy();
    }

    @Override
    public void resetAndReload() {
        datKhoangThoiGianMacDinh();
        loaiThoiGianCombo.setSelectedItem(LoaiThoiGian.NGAY_MUON);
        tuKhoaField.setText("");
        trangThaiCombo.setSelectedItem(TrangThaiLoc.TAT_CA);
        sapXepCombo.setSelectedItem(SapXepTheo.NGAY_MUON_TANG);
        xemBaoCao();
    }
}

