package muontra;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Giao diện "Báo cáo Mượn - Trả Cơ sở vật chất" (Ban Chủ nhiệm / Người quản lý xem).
 *
 * CHỈ LÀ GIAO DIỆN: các nút chưa làm gì, bảng và số thống kê đang là DỮ LIỆU MẪU.
 * Người làm chức năng nối vào qua các hàm công khai bên dưới, không cần sửa bố cục:
 *   - getTuNgay(), getDenNgay(), getTrangThaiDangChon(): đọc bộ lọc.
 *   - setRows(...), setThongKe(...): đổ dữ liệu thật lên bảng và khung thống kê.
 *   - setOnThongKe(...), setOnXuatBaoCao(...): gán việc cần làm khi bấm nút.
 */
public class BaoCaoMuonTraPanel extends JPanel {

    /** Một dòng của bảng báo cáo. tra: "Chưa", "Đã trả" hoặc "-" (phiếu chưa duyệt/bị từ chối). */
    public record DongBaoCao(String maPhieu, String nguoiMuon, String thietBi, int soLuong,
                             String trangThai, String tra) {
    }

    public static final String TAT_CA = "Tất cả";
    private static final String[] TRANG_THAI = {TAT_CA, "Chờ duyệt", "Đã duyệt", "Từ chối"};
    private static final String[] COT = {"Mã phiếu", "Người mượn", "Thiết bị", "SL", "Trạng thái", "Trả"};

    private final JSpinner tuNgay = taoOChonNgay(LocalDate.of(2026, 11, 1));
    private final JSpinner denNgay = taoOChonNgay(LocalDate.of(2026, 11, 30));
    private final JComboBox<String> trangThai = new JComboBox<>(new DefaultComboBoxModel<>(TRANG_THAI));
    private final JButton nutThongKe = new JButton("Thống kê");
    private final JButton nutXuat = new JButton("Xuất báo cáo");
    private final DefaultTableModel model = new DefaultTableModel(COT, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = new JTable(model);
    private final JLabel tongPhieu = taoSo();
    private final JLabel choDuyet = taoSo();
    private final JLabel daDuyet = taoSo();
    private final JLabel tuChoi = taoSo();
    private final JLabel chuaTra = taoSo();

    private Runnable onThongKe = () -> { };      // TODO (người làm chức năng): nạp dữ liệu theo bộ lọc
    private Runnable onXuatBaoCao = () -> { };   // TODO (người làm chức năng): xuất báo cáo ra file

    public BaoCaoMuonTraPanel() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(taoTieuDe(), BorderLayout.NORTH);
        JPanel giua = new JPanel(new BorderLayout(0, 8));
        giua.add(taoBoLoc(), BorderLayout.NORTH);
        giua.add(taoBang(), BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);
        add(taoChanTrang(), BorderLayout.SOUTH);

        nutThongKe.addActionListener(e -> onThongKe.run());
        nutXuat.addActionListener(e -> onXuatBaoCao.run());

        napDuLieuMau();
        setPreferredSize(new Dimension(820, 560));
    }

    // ------------------------------------------------------------------ API cho người nối chức năng

    public LocalDate getTuNgay() {
        return layNgay(tuNgay);
    }

    public LocalDate getDenNgay() {
        return layNgay(denNgay);
    }

    /** Trả về "Tất cả", "Chờ duyệt", "Đã duyệt" hoặc "Từ chối". */
    public String getTrangThaiDangChon() {
        return (String) trangThai.getSelectedItem();
    }

    public void setRows(List<DongBaoCao> rows) {
        model.setRowCount(0);
        for (DongBaoCao r : rows) {
            model.addRow(new Object[]{r.maPhieu(), r.nguoiMuon(), r.thietBi(), r.soLuong(), r.trangThai(), r.tra()});
        }
    }

    public void setThongKe(int tong, int soChoDuyet, int soDaDuyet, int soTuChoi, int soChuaTra) {
        tongPhieu.setText(String.valueOf(tong));
        choDuyet.setText(String.valueOf(soChoDuyet));
        daDuyet.setText(String.valueOf(soDaDuyet));
        tuChoi.setText(String.valueOf(soTuChoi));
        chuaTra.setText(String.valueOf(soChuaTra));
    }

    public void setOnThongKe(Runnable action) {
        this.onThongKe = action == null ? () -> { } : action;
    }

    public void setOnXuatBaoCao(Runnable action) {
        this.onXuatBaoCao = action == null ? () -> { } : action;
    }

    // ------------------------------------------------------------------ dựng giao diện

    private JLabel taoTieuDe() {
        JLabel tieuDe = new JLabel("BÁO CÁO MƯỢN - TRẢ CƠ SỞ VẬT CHẤT", SwingConstants.CENTER);
        tieuDe.setFont(tieuDe.getFont().deriveFont(Font.BOLD, 20f));
        return tieuDe;
    }

    private JPanel taoBoLoc() {
        JPanel boLoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        boLoc.setBorder(BorderFactory.createTitledBorder("Bộ lọc"));
        boLoc.add(new JLabel("Từ ngày:"));
        boLoc.add(tuNgay);
        boLoc.add(new JLabel("Đến ngày:"));
        boLoc.add(denNgay);
        boLoc.add(new JLabel("Trạng thái:"));
        boLoc.add(trangThai);
        boLoc.add(nutThongKe);
        return boLoc;
    }

    private JScrollPane taoBang() {
        bang.setRowHeight(26);
        bang.setFillsViewportHeight(true);
        bang.getTableHeader().setReorderingAllowed(false);
        bang.getTableHeader().setFont(bang.getTableHeader().getFont().deriveFont(Font.BOLD));

        DefaultTableCellRenderer giuaO = new DefaultTableCellRenderer();
        giuaO.setHorizontalAlignment(SwingConstants.CENTER);
        bang.getColumnModel().getColumn(0).setPreferredWidth(90);
        bang.getColumnModel().getColumn(1).setPreferredWidth(180);
        bang.getColumnModel().getColumn(2).setPreferredWidth(180);
        bang.getColumnModel().getColumn(3).setPreferredWidth(50);
        bang.getColumnModel().getColumn(3).setCellRenderer(giuaO);
        bang.getColumnModel().getColumn(4).setPreferredWidth(110);
        bang.getColumnModel().getColumn(5).setPreferredWidth(90);
        bang.getColumnModel().getColumn(5).setCellRenderer(giuaO);
        JScrollPane cuon = new JScrollPane(bang);
        cuon.setColumnHeaderView(bang.getTableHeader());   // luôn hiện hàng tiêu đề cột
        return cuon;
    }

    private JPanel taoChanTrang() {
        JPanel chan = new JPanel(new BorderLayout());
        chan.add(taoKhungThongKe(), BorderLayout.WEST);

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        nutXuat.setPreferredSize(new Dimension(150, 34));
        phai.add(nutXuat);
        JPanel duoi = new JPanel(new BorderLayout());
        duoi.add(phai, BorderLayout.SOUTH);
        chan.add(duoi, BorderLayout.CENTER);
        return chan;
    }

    private JPanel taoKhungThongKe() {
        JPanel khung = new JPanel(new GridBagLayout());
        khung.setBorder(BorderFactory.createTitledBorder("Thống kê"));
        String[] nhan = {"Tổng phiếu:", "Chờ duyệt:", "Đã duyệt:", "Từ chối:", "Thiết bị chưa trả:"};
        JLabel[] so = {tongPhieu, choDuyet, daDuyet, tuChoi, chuaTra};
        for (int i = 0; i < nhan.length; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = i;
            c.insets = new Insets(2, 8, 2, 8);
            c.anchor = GridBagConstraints.WEST;
            c.gridx = 0;
            khung.add(new JLabel(nhan[i]), c);
            c.gridx = 1;
            c.anchor = GridBagConstraints.EAST;
            c.weightx = 1;
            khung.add(so[i], c);
        }
        return khung;
    }

    private static JLabel taoSo() {
        JLabel l = new JLabel("0", SwingConstants.RIGHT);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        l.setPreferredSize(new Dimension(40, l.getPreferredSize().height));
        return l;
    }

    private static JSpinner taoOChonNgay(LocalDate ngay) {
        Date d = Date.from(ngay.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner s = new JSpinner(new SpinnerDateModel(d, null, null, java.util.Calendar.DAY_OF_MONTH));
        s.setEditor(new JSpinner.DateEditor(s, "dd/MM/yyyy"));
        s.setPreferredSize(new Dimension(110, s.getPreferredSize().height));
        return s;
    }

    private static LocalDate layNgay(JSpinner s) {
        return ((Date) s.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** Dữ liệu mẫu để xem giao diện; thay bằng setRows/setThongKe khi nối dữ liệu thật. */
    private void napDuLieuMau() {
        setRows(List.of(
                new DongBaoCao("PM001", "Nguyễn A", "Máy chiếu", 1, "Đã duyệt", "Chưa"),
                new DongBaoCao("PM002", "Trần B", "Micro", 2, "Đã duyệt", "Đã trả"),
                new DongBaoCao("PM003", "Lê C", "Loa", 1, "Chờ duyệt", "-")));
        setThongKe(10, 2, 7, 1, 3);
    }
}
