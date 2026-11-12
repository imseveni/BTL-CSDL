package thanhvien;

import common.BaoCaoPdf;
import common.NguoiLap;
import common.PhienLamViec;
import common.ReportSheet;
import common.Refreshable;
import common.SwingSupport;
import common.Validation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Tab Báo cáo của module Thành viên: chọn báo cáo và tham số, xem trước đúng như trang PDF sẽ in, rồi xuất PDF.
 * Báo cáo ghi rõ ai lập (Ban Chủ nhiệm đang đăng nhập) và gửi cho ai; cùng mẫu với báo cáo mượn – trả.
 */
public class ThanhVienBaoCaoUi extends JPanel implements Refreshable {

    private static final String BC_DANH_SACH = "1. Danh sách thành viên theo ban";
    private static final String BC_HOAT_DONG = "2. Tổng hợp hoạt động thành viên trong kỳ";
    private static final String ALL_BANS = "Tất cả ban";
    private static final String ALL_STATUS = "Tất cả trạng thái";

    private final ThanhVienService service;
    private final JComboBox<String> reportType = new JComboBox<>(new String[] { BC_DANH_SACH, BC_HOAT_DONG });
    private final JTextField author = new JTextField(tenNguoiDangNhap(), 20);
    private final JComboBox<Object> banFilter = new JComboBox<>(new Object[] { ALL_BANS });
    private final JComboBox<String> statusFilter = new JComboBox<>();
    private final JTextField from = new JTextField(LocalDate.now().withDayOfYear(1).toString(), 9);
    private final JTextField to = new JTextField(LocalDate.now().toString(), 9);
    private final CardLayout paramCards = new CardLayout();
    private final JPanel params = new JPanel(paramCards);
    private final JPanel pagesPanel = new JPanel();
    private final JLabel status = new JLabel("Chọn báo cáo, nhập tham số rồi bấm Xem báo cáo.");
    private final SwingSupport.Tasks tasks = new SwingSupport.Tasks(this, status);
    private ReportSheet current;
    private String currentName = "BaoCao";

    public ThanhVienBaoCaoUi(ThanhVienService service) {
        this.service = service;
        setName("thanhvien.baocao.panel");
        setLayout(new BorderLayout(10, 10));
        setBackground(SwingSupport.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        reportType.setName("thanhvien.baocao.loai");
        author.setName("thanhvien.baocao.nguoilap");
        banFilter.setName("thanhvien.baocao.ban");
        statusFilter.setName("thanhvien.baocao.trangthai");
        from.setName("thanhvien.baocao.tu");
        to.setName("thanhvien.baocao.den");
        statusFilter.addItem(ALL_STATUS);
        ThanhVienService.TRANG_THAI.forEach(statusFilter::addItem);
        reportType.setPrototypeDisplayValue(BC_HOAT_DONG + "  ");
        banFilter.setPrototypeDisplayValue("BAN00 – Ban Truyền thông dài");
        statusFilter.setPrototypeDisplayValue(ALL_STATUS + "  ");
        from.setToolTipText("Định dạng yyyy-MM-dd");
        to.setToolTipText("Định dạng yyyy-MM-dd");

        JPanel listParams = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        listParams.add(new JLabel("Ban:"));
        listParams.add(banFilter);
        listParams.add(new JLabel("Trạng thái:"));
        listParams.add(statusFilter);
        JPanel periodParams = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        periodParams.add(new JLabel("Từ ngày:"));
        periodParams.add(from);
        periodParams.add(new JLabel("Đến ngày:"));
        periodParams.add(to);
        params.add(listParams, BC_DANH_SACH);
        params.add(periodParams, BC_HOAT_DONG);
        reportType.addActionListener(event -> {
            paramCards.show(params, (String) reportType.getSelectedItem());
            clearPreview();
        });

        JPanel head = new JPanel(new BorderLayout(0, 8));
        JPanel line1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        line1.add(new JLabel("Báo cáo:"));
        line1.add(reportType);
        line1.add(new JLabel("Người lập:"));
        line1.add(author);
        JPanel line2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        line2.add(params);
        line2.add(SwingSupport.button("Xem báo cáo", "thanhvien.baocao.xem", this::view));
        line2.add(SwingSupport.button("Xuất PDF", "thanhvien.baocao.xuat", this::export));
        JPanel controls = new JPanel(new BorderLayout(0, 6));
        controls.add(line1, BorderLayout.NORTH);
        controls.add(line2, BorderLayout.SOUTH);
        head.add(controls, BorderLayout.NORTH);
        add(head, BorderLayout.NORTH);

        pagesPanel.setName("thanhvien.baocao.trang");
        pagesPanel.setLayout(new BoxLayout(pagesPanel, BoxLayout.Y_AXIS));
        pagesPanel.setBackground(new Color(120, 126, 134));
        add(SwingSupport.scroll(pagesPanel), BorderLayout.CENTER);
        status.setName("thanhvien.baocao.status");
        add(status, BorderLayout.SOUTH);
    }

    @Override
    public boolean isBusy() {
        return tasks.isBusy();
    }

    /** Nạp danh sách ban cho ô lọc và xóa báo cáo đang xem. */
    @Override
    public void resetAndReload() {
        clearPreview();
        tasks.run("Đang tải danh sách ban...", service::danhSachBan, bans -> {
            banFilter.removeAllItems();
            banFilter.addItem(ALL_BANS);
            bans.forEach(banFilter::addItem);
            status.setText("Chọn báo cáo, nhập tham số rồi bấm Xem báo cáo.");
        });
    }

    private void clearPreview() {
        current = null;
        pagesPanel.removeAll();
        pagesPanel.revalidate();
        pagesPanel.repaint();
    }

    /** Họ tên người đang đăng nhập; null nếu chạy không qua màn hình đăng nhập. */
    private static String tenNguoiDangNhap() {
        String ten = PhienLamViec.hienTai().hoVaTen();
        return PhienLamViec.msvHienTai() == null ? "" : ten;
    }

    /** Người lập: tên trong ô nhập; chức vụ lấy từ hồ sơ nếu đúng là người đang đăng nhập. */
    private NguoiLap nguoiLap(String ten) {
        String msv = PhienLamViec.msvHienTai();
        String chucVu = "";
        if (msv != null && ten.equals(PhienLamViec.hienTai().hoVaTen())) {
            ThanhVien tv = service.chiTiet(msv);
            chucVu = tv == null || tv.chucVu() == null ? "" : tv.chucVu();
        }
        return new NguoiLap(ten, chucVu);
    }

    private record Rendered(ReportSheet sheet, List<BufferedImage> pages) {
    }

    /** Đọc tham số, lấy dữ liệu và dựng trang PDF ở luồng nền rồi hiện báo cáo lên màn hình. */
    private void view() {
        String person = Validation.required(author.getText(), "Người lập", 100);
        LocalDate today = LocalDate.now();
        if (BC_DANH_SACH.equals(reportType.getSelectedItem())) {
            String ban = banFilter.getSelectedItem() instanceof BanLuaChon b ? b.maBan() : null;
            String state = statusFilter.getSelectedIndex() <= 0 ? null : (String) statusFilter.getSelectedItem();
            currentName = BaoCaoThanhVien.tenFile("DanhSachThanhVien", today);
            tasks.run("Đang lập báo cáo...", () -> render(BaoCaoThanhVien.danhSachTheoBan(nguoiLap(person), today,
                    service.baoCaoDanhSach(ban, state))), this::show);
        } else {
            LocalDate tu = SwingSupport.date(from, "Từ ngày", true);
            LocalDate den = SwingSupport.date(to, "Đến ngày", true);
            currentName = BaoCaoThanhVien.tenFile("HoatDongThanhVien", today);
            tasks.run("Đang lập báo cáo...", () -> render(BaoCaoThanhVien.hoatDong(nguoiLap(person), today, tu, den,
                    service.baoCaoHoatDong(tu, den))), this::show);
        }
    }

    private static Rendered render(ReportSheet sheet) throws IOException {
        return new Rendered(sheet, BaoCaoPdf.render(sheet));
    }

    private void show(Rendered rendered) {
        current = rendered.sheet();
        pagesPanel.removeAll();
        for (BufferedImage page : rendered.pages()) {
            int width = 980;
            int height = page.getHeight() * width / page.getWidth();
            JLabel label = new JLabel(new ImageIcon(page.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
            label.setAlignmentX(0.5f);
            label.setBorder(BorderFactory.createLineBorder(new Color(90, 96, 104), 1));
            pagesPanel.add(Box.createVerticalStrut(10));
            pagesPanel.add(label);
        }
        pagesPanel.add(Box.createVerticalStrut(10));
        pagesPanel.revalidate();
        pagesPanel.repaint();
        status.setText(current.rows().size() + " dòng, " + rendered.pages().size() + " trang. Bấm Xuất PDF để lưu file.");
    }

    /** Báo cáo đang hiển thị, hoặc null nếu chưa xem. */
    ReportSheet currentSheet() {
        return current;
    }

    /** Mở hộp thoại lưu file rồi xuất báo cáo đang xem ra PDF. */
    private void export() {
        if (current == null) {
            throw new IllegalArgumentException("Hãy bấm Xem báo cáo trước khi xuất file.");
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu báo cáo PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("Tệp PDF (*.pdf)", "pdf"));
        chooser.setSelectedFile(new java.io.File(currentName));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path path = BaoCaoPdf.withPdfExtension(chooser.getSelectedFile().toPath());
        if (Files.exists(path) && !SwingSupport.confirm(this, "File đã tồn tại. Ghi đè " + path.getFileName() + "?")) {
            return;
        }
        Path target = path;
        ReportSheet sheet = current;
        tasks.run("Đang xuất file...", () -> exportTo(sheet, target), saved -> {
            status.setText("Đã xuất: " + saved);
            JOptionPane.showMessageDialog(this, "Đã lưu báo cáo:\n" + saved, "Xuất PDF", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /** Ghi báo cáo ra file PDF; tách riêng để kiểm thử không cần hộp thoại. */
    static Path exportTo(ReportSheet sheet, Path path) {
        try {
            return BaoCaoPdf.write(sheet, path);
        } catch (IOException e) {
            throw new IllegalStateException("Không ghi được file " + path + ": " + e.getMessage(), e);
        }
    }

    static List<String> reportNames() {
        return List.of(BC_DANH_SACH, BC_HOAT_DONG);
    }
}
