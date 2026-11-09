package thanhvien;

import common.ReportSheet;
import common.Refreshable;
import common.SwingSupport;
import common.Validation;
import common.XlsxWriter;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 * Tab Báo cáo của module Thành viên: chọn báo cáo và tham số, xem kết quả ngay trên màn hình, rồi xuất
 * file Excel đúng như đang xem. Màn hình ghi rõ ai lập báo cáo và báo cáo gửi cho ai.
 */
public class ThanhVienBaoCaoUi extends JPanel implements Refreshable {

    private static final String BC_DANH_SACH = "1. Danh sách thành viên theo ban";
    private static final String BC_HOAT_DONG = "2. Tổng hợp hoạt động thành viên trong kỳ";
    private static final String ALL_BANS = "Tất cả ban";
    private static final String ALL_STATUS = "Tất cả trạng thái";

    private final ThanhVienService service;
    private final JComboBox<String> reportType = new JComboBox<>(new String[] { BC_DANH_SACH, BC_HOAT_DONG });
    private final JTextField clubName = new JTextField("Câu lạc bộ sinh viên", 22);
    private final JTextField author = new JTextField("Thư ký CLB", 16);
    private final JComboBox<Object> banFilter = new JComboBox<>(new Object[] { ALL_BANS });
    private final JComboBox<String> statusFilter = new JComboBox<>();
    private final JTextField from = new JTextField(LocalDate.now().withDayOfYear(1).toString(), 9);
    private final JTextField to = new JTextField(LocalDate.now().toString(), 9);
    private final CardLayout paramCards = new CardLayout();
    private final JPanel params = new JPanel(paramCards);
    private final JLabel info = new JLabel(" ");
    private final DefaultTableModel preview = new DefaultTableModel();
    private final JTable previewTable = SwingSupport.table(preview, "thanhvien.baocao.table");
    private final JLabel summary = new JLabel(" ");
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
        clubName.setName("thanhvien.baocao.clb");
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
        line1.add(new JLabel("Tên CLB:"));
        line1.add(clubName);
        line1.add(new JLabel("Người lập:"));
        line1.add(author);
        JPanel line2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        line2.add(params);
        line2.add(SwingSupport.button("Xem báo cáo", "thanhvien.baocao.xem", this::view));
        line2.add(SwingSupport.button("Xuất Excel (.xlsx)", "thanhvien.baocao.xuat", this::export));
        JPanel controls = new JPanel(new BorderLayout(0, 6));
        controls.add(line1, BorderLayout.NORTH);
        controls.add(line2, BorderLayout.SOUTH);
        head.add(controls, BorderLayout.NORTH);
        info.setName("thanhvien.baocao.thongtin");
        info.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
        head.add(info, BorderLayout.SOUTH);
        add(head, BorderLayout.NORTH);

        add(SwingSupport.scroll(previewTable), BorderLayout.CENTER);
        summary.setName("thanhvien.baocao.tonghop");
        summary.setFont(summary.getFont().deriveFont(Font.BOLD));
        JPanel south = new JPanel(new BorderLayout(0, 4));
        south.add(summary, BorderLayout.NORTH);
        status.setName("thanhvien.baocao.status");
        south.add(status, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
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
        preview.setColumnCount(0);
        preview.setRowCount(0);
        info.setText(" ");
        summary.setText(" ");
    }

    /** Đọc tham số, lấy dữ liệu ở luồng nền rồi hiện báo cáo lên bảng. */
    private void view() {
        String club = Validation.required(clubName.getText(), "Tên câu lạc bộ", 100);
        String person = Validation.required(author.getText(), "Người lập", 100);
        LocalDate today = LocalDate.now();
        if (BC_DANH_SACH.equals(reportType.getSelectedItem())) {
            String ban = banFilter.getSelectedItem() instanceof BanLuaChon b ? b.maBan() : null;
            String banText = banFilter.getSelectedItem() instanceof BanLuaChon b ? b.toString() : "tất cả ban";
            String state = statusFilter.getSelectedIndex() <= 0 ? null : (String) statusFilter.getSelectedItem();
            String filter = banText + "; " + (state == null ? "mọi trạng thái" : state);
            currentName = BaoCaoThanhVien.tenFile("DanhSachThanhVien", today);
            tasks.run("Đang lập báo cáo...", () -> BaoCaoThanhVien.danhSachTheoBan(club, person, today, filter,
                    service.baoCaoDanhSach(ban, state)), this::show);
        } else {
            LocalDate tu = SwingSupport.date(from, "Từ ngày", true);
            LocalDate den = SwingSupport.date(to, "Đến ngày", true);
            currentName = BaoCaoThanhVien.tenFile("HoatDongThanhVien", today);
            tasks.run("Đang lập báo cáo...", () -> BaoCaoThanhVien.hoatDong(club, person, today, tu, den,
                    service.baoCaoHoatDong(tu, den)), this::show);
        }
    }

    private void show(ReportSheet sheet) {
        current = sheet;
        preview.setColumnCount(0);
        preview.setRowCount(0);
        sheet.columns().forEach(preview::addColumn);
        sheet.rows().forEach(row -> preview.addRow(row.toArray()));
        info.setText("<html><b>" + sheet.title() + "</b> — " + sheet.subtitle() + "<br>"
                + String.join("<br>", sheet.infoLines()) + "</html>");
        summary.setText(String.join("   |   ", sheet.summaryLines()));
        status.setText(sheet.rows().size() + " dòng. Bấm Xuất Excel để lưu file.");
    }

    /** Báo cáo đang hiển thị, hoặc null nếu chưa xem. */
    ReportSheet currentSheet() {
        return current;
    }

    /** Mở hộp thoại lưu file rồi xuất báo cáo đang xem ra Excel. */
    private void export() {
        if (current == null) {
            throw new IllegalArgumentException("Hãy bấm Xem báo cáo trước khi xuất file.");
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu báo cáo Excel");
        chooser.setFileFilter(new FileNameExtensionFilter("Bảng tính Excel (*.xlsx)", "xlsx"));
        chooser.setSelectedFile(new java.io.File(currentName));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path path = chooser.getSelectedFile().toPath();
        if (!path.getFileName().toString().toLowerCase().endsWith(".xlsx")) {
            path = path.resolveSibling(path.getFileName() + ".xlsx");
        }
        if (Files.exists(path) && !SwingSupport.confirm(this, "File đã tồn tại. Ghi đè " + path.getFileName() + "?")) {
            return;
        }
        Path target = path;
        ReportSheet sheet = current;
        tasks.run("Đang xuất file...", () -> {
            exportTo(sheet, target);
            return target;
        }, saved -> {
            status.setText("Đã xuất: " + saved);
            JOptionPane.showMessageDialog(this, "Đã lưu báo cáo:\n" + saved, "Xuất Excel", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /** Ghi báo cáo ra file; tách riêng để kiểm thử không cần hộp thoại. */
    static void exportTo(ReportSheet sheet, Path path) {
        try {
            XlsxWriter.write(path, sheet);
        } catch (IOException e) {
            throw new IllegalStateException("Không ghi được file " + path + ": " + e.getMessage(), e);
        }
    }

    static List<String> reportNames() {
        return List.of(BC_DANH_SACH, BC_HOAT_DONG);
    }
}
