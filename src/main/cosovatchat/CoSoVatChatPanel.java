package cosovatchat;

import common.PhienLamViec;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Nội dung của tab "Cơ sở vật chất": tìm, thêm, sửa thiết bị, gán người quản lý, kiểm tra khả dụng.
 * Mọi thao tác đi qua {@link CoSoVatChatService}. Trong CoSoVatChatSwingModule chỉ cần dùng:
 * new CoSoVatChatPanel(). Dữ liệu được nạp lần đầu khi màn hình hiện ra (luồng nền).
 */
public class CoSoVatChatPanel extends JPanel {
    private final CoSoVatChatService service;
    private final JTextField oTim = new JTextField(18);
    private final JButton nutTim = new JButton("Tìm kiếm");
    private final JButton nutTaiLai = new JButton("Tải lại");
    private final JButton nutThem = new JButton("Thêm thiết bị");
    private final JButton nutSua = new JButton("Sửa thiết bị");
    private final JButton nutGan = new JButton("Gán người quản lý");
    private final JButton nutKiemTra = new JButton("Kiểm tra khả dụng");
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã thiết bị", "Tên thiết bị", "Tổng", "Khả dụng", "Đang mượn", "Người quản lý"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bang = new JTable(model);
    private final JLabel trangThaiLabel = new JLabel(" ");

    private List<CoSoVatChat> dsThietBi = List.of();
    private boolean dangNap;
    private boolean daNapLanDau;

    public CoSoVatChatPanel() {
        this(new CoSoVatChatService());
    }

    public CoSoVatChatPanel(CoSoVatChatService service) {
        super(new BorderLayout(0, 6));
        this.service = Objects.requireNonNull(service);
        setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));

        JPanel bac = new JPanel();
        bac.setLayout(new BoxLayout(bac, BoxLayout.Y_AXIS));
        bac.add(taoThanhLoc());
        bac.add(taoThanhThaoTac());
        add(bac, BorderLayout.NORTH);
        add(cuon(taoBang()), BorderLayout.CENTER);
        trangThaiLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        add(trangThaiLabel, BorderLayout.SOUTH);

        nutTim.addActionListener(e -> taiLai(null));
        oTim.addActionListener(e -> taiLai(null));
        nutTaiLai.addActionListener(e -> {
            oTim.setText("");
            taiLai(null);
        });
        nutThem.addActionListener(e -> them());
        nutSua.addActionListener(e -> sua());
        nutGan.addActionListener(e -> gan());
        nutKiemTra.addActionListener(e -> kiemTra());
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !dangNap) {
                capNhatNut();
            }
        });
        capNhatNut();
        if (PhienLamViec.laThanhVien()) {
            // Thành viên chỉ tra cứu thiết bị và tình trạng khả dụng; thêm, sửa, gán người quản lý là việc của Ban Chủ nhiệm.
            nutThem.setVisible(false);
            nutSua.setVisible(false);
            nutGan.setVisible(false);
        }

        addHierarchyListener(e -> {
            if (!daNapLanDau && isShowing()) {
                daNapLanDau = true;
                taiLai(null);
            }
        });
    }

    // ------------------------------------------------------------------ dựng giao diện

    private JPanel taoThanhLoc() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.setBackground(new Color(0xE9ECEF));
        p.add(new JLabel("Mã / tên thiết bị:"));
        p.add(oTim);
        p.add(nutTim);
        p.add(nutTaiLai);
        p.add(nutThem);
        return canTrai(p);
    }

    private JPanel taoThanhThaoTac() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.setBackground(new Color(0xDDE1E6));
        JLabel tieuDe = new JLabel("DANH SÁCH THIẾT BỊ");
        tieuDe.setFont(tieuDe.getFont().deriveFont(Font.BOLD));
        p.add(tieuDe);
        p.add(nutSua);
        p.add(nutGan);
        p.add(nutKiemTra);
        return canTrai(p);
    }

    private static JPanel canTrai(JPanel p) {
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private JTable taoBang() {
        bang.setRowHeight(24);
        bang.setFillsViewportHeight(true);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.getTableHeader().setReorderingAllowed(false);
        bang.getTableHeader().setFont(bang.getTableHeader().getFont().deriveFont(Font.BOLD));
        DefaultTableCellRenderer giua = new DefaultTableCellRenderer();
        giua.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 2; i <= 4; i++) {
            bang.getColumnModel().getColumn(i).setCellRenderer(giua);
        }
        return bang;
    }

    // ------------------------------------------------------------------ nạp dữ liệu

    /** Nạp lại danh sách theo ô tìm; nếu maCanChon khác null thì chọn lại đúng thiết bị đó. */
    void taiLai(String maCanChon) {
        String tuKhoa = oTim.getText();
        datTrangThai("Đang tải...");
        ChayNen.chay(() -> service.timKiem(tuKhoa), ds -> hien(ds, maCanChon), loi -> {
            hien(List.of(), null);
            datTrangThai("Chưa thực hiện được. Kiểm tra dữ liệu hoặc kết nối MySQL. (" + loi + ")");
        });
    }

    private void hien(List<CoSoVatChat> ds, String maCanChon) {
        dangNap = true;
        dsThietBi = ds;
        model.setRowCount(0);
        int chon = -1;
        for (int i = 0; i < ds.size(); i++) {
            CoSoVatChat t = ds.get(i);
            model.addRow(new Object[]{t.maThietBi(), t.tenThietBi(), t.tongSoLuong(), t.soLuongKhaDung(),
                    t.soLuongDangMuon(), t.msvQuanLy() == null ? "(chưa có)" : t.msvQuanLy() + " - " + t.tenNguoiQuanLy()});
            if (t.maThietBi().equals(maCanChon)) {
                chon = i;
            }
        }
        if (chon >= 0) {
            bang.setRowSelectionInterval(chon, chon);
        } else {
            bang.clearSelection();
        }
        dangNap = false;
        datTrangThai("Có " + ds.size() + " thiết bị.");
        capNhatNut();
    }

    private CoSoVatChat dangChon() {
        int dong = bang.getSelectedRow();
        return dong >= 0 && dong < dsThietBi.size() ? dsThietBi.get(dong) : null;
    }

    private void capNhatNut() {
        boolean coChon = dangChon() != null;
        nutSua.setEnabled(coChon);
        nutGan.setEnabled(coChon);
        nutKiemTra.setEnabled(coChon);
    }

    // ------------------------------------------------------------------ thao tác

    private void them() {
        String[] v = hoiThongTin("Thêm thiết bị",
                new String[]{"Mã thiết bị", "Tên thiết bị", "Tổng số lượng", "MSV người quản lý (có thể để trống)"},
                new String[]{"", "", "1", ""});
        if (v == null) {
            return;
        }
        try {
            int tong = docSo(v[2]);
            String ma = v[0].strip();
            chayVaTaiLai(ma, () -> service.themMoi(v[0], v[1], tong, null, v[3]), "Đã thêm thiết bị " + ma + ".");
        } catch (IllegalArgumentException e) {
            canhBao(e.getMessage());
        }
    }

    private void sua() {
        CoSoVatChat t = dangChon();
        if (t == null) {
            return;
        }
        String[] v = hoiThongTin("Sửa thiết bị " + t.maThietBi(),
                new String[]{"Tên thiết bị", "Tổng số lượng"},
                new String[]{t.tenThietBi(), String.valueOf(t.tongSoLuong())});
        if (v == null) {
            return;
        }
        try {
            int tong = docSo(v[1]);
            chayVaTaiLai(t.maThietBi(), () -> service.capNhat(t.maThietBi(), v[0], tong),
                    "Đã cập nhật " + t.maThietBi() + ". Số lượng khả dụng được điều chỉnh theo số đang mượn.");
        } catch (IllegalArgumentException e) {
            canhBao(e.getMessage());
        }
    }

    private void gan() {
        CoSoVatChat t = dangChon();
        if (t == null) {
            return;
        }
        String[] v = hoiThongTin("Gán người quản lý - " + t.maThietBi(),
                new String[]{"MSV người quản lý (để trống = bỏ người quản lý)"},
                new String[]{t.msvQuanLy() == null ? "" : t.msvQuanLy()});
        if (v == null) {
            return;
        }
        chayVaTaiLai(t.maThietBi(), () -> service.ganNguoiQuanLy(t.maThietBi(), v[0]),
                "Đã cập nhật người quản lý của " + t.maThietBi() + ".");
    }

    private void kiemTra() {
        CoSoVatChat t = dangChon();
        if (t == null) {
            return;
        }
        String[] v = hoiThongTin("Kiểm tra khả dụng - " + t.maThietBi(),
                new String[]{"Số lượng muốn mượn"}, new String[]{"1"});
        if (v == null) {
            return;
        }
        try {
            int soLuong = docSo(v[0]);
            ChayNen.chay(() -> service.kiemTraKhaDung(t.maThietBi(), soLuong), du -> {
                String ketQua = du
                        ? "ĐỦ: còn " + t.soLuongKhaDung() + ", có thể cho mượn " + soLuong + "."
                        : "KHÔNG ĐỦ: chỉ còn " + t.soLuongKhaDung() + ", không thể cho mượn " + soLuong + ".";
                datTrangThai(t.maThietBi() + " - " + ketQua);
                if (!GraphicsEnvironment.isHeadless()) {
                    JOptionPane.showMessageDialog(this, ketQua, "Kiểm tra khả dụng - " + t.maThietBi(),
                            du ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                }
            }, this::canhBao);
        } catch (IllegalArgumentException e) {
            canhBao(e.getMessage());
        }
    }

    /** Chạy một thao tác ghi ở luồng nền; xong thì tải lại danh sách và chọn lại thiết bị maCanChon. */
    private void chayVaTaiLai(String maCanChon, Runnable viec, String thongBaoThanhCong) {
        ChayNen.chay(() -> {
            viec.run();
            return Boolean.TRUE;
        }, ok -> {
            taiLai(maCanChon);
            datTrangThai(thongBaoThanhCong);
        }, this::canhBao);
    }

    // ------------------------------------------------------------------ tiện ích

    private String[] hoiThongTin(String tieuDe, String[] nhan, String[] macDinh) {
        JPanel form = new JPanel(new GridLayout(nhan.length, 2, 8, 6));
        JTextField[] o = new JTextField[nhan.length];
        for (int i = 0; i < nhan.length; i++) {
            form.add(new JLabel(nhan[i]));
            o[i] = new JTextField(macDinh[i], 18);
            form.add(o[i]);
        }
        if (JOptionPane.showConfirmDialog(this, form, tieuDe, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        String[] kq = new String[nhan.length];
        for (int i = 0; i < kq.length; i++) {
            kq[i] = o[i].getText();
        }
        return kq;
    }

    private void canhBao(String noiDung) {
        datTrangThai("Không thực hiện được: " + noiDung);
        if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(this, noiDung, "Không thực hiện được", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void datTrangThai(String noiDung) {
        trangThaiLabel.setText(noiDung);
    }

    private static int docSo(String text) {
        try {
            return Integer.parseInt(text.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + text + "\" không phải số nguyên hợp lệ.");
        }
    }
    /** Bọc bảng vào khung cuộn và luôn hiện hàng tiêu đề cột. */
    static JScrollPane cuon(JTable b) {
        JScrollPane c = new JScrollPane(b);
        c.setColumnHeaderView(b.getTableHeader());
        return c;
    }
}
