package muontra;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình "Phiếu mượn" của module Mượn – Trả: tìm phiếu, tạo phiếu, thêm thiết bị, duyệt/từ chối,
 * ghi nhận trả, xem chi tiết và lịch sử mượn. Mọi thao tác đi qua {@link PhieuMuonService}.
 * Dữ liệu được nạp lần đầu khi màn hình hiện ra; truy cập MySQL chạy ở luồng nền.
 */
public class PhieuMuonPanel extends JPanel {
    private static final String[] LOC = {"Tất cả trạng thái", "Chờ duyệt", "Đã duyệt", "Từ chối"};
    private static final String[] MA_LOC = {null, PhieuMuon.CHO_DUYET, PhieuMuon.DA_DUYET, PhieuMuon.TU_CHOI};
    private static final DateTimeFormatter NGAY_NHAP =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter NGAY_HIEN = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PhieuMuonService service;
    private final JTextField oTim = new JTextField(16);
    private final JComboBox<String> locTrangThai = new JComboBox<>(LOC);
    private final JButton nutTim = new JButton("Tìm kiếm");
    private final JButton nutTaiLai = new JButton("Tải lại");
    private final JButton nutTao = new JButton("Tạo phiếu");
    private final JButton nutThemTb = new JButton("Thêm thiết bị vào phiếu");
    private final JButton nutDuyet = new JButton("Duyệt");
    private final JButton nutTuChoi = new JButton("Từ chối");
    private final JButton nutTra = new JButton("Ghi nhận trả");
    private final JButton nutTraTatCa = new JButton("Trả tất cả");
    private final DefaultTableModel modelPhieu = taoModel("Mã phiếu", "Người mượn (MSV)", "Ngày mượn", "Hẹn trả",
            "Trạng thái", "Mục đích");
    private final DefaultTableModel modelChiTiet = taoModel("Mã thiết bị", "Tên thiết bị", "Số lượng", "Ngày trả thực tế");
    private final DefaultTableModel modelLichSu = taoModel("Mã phiếu", "Ngày mượn", "Hẹn trả", "Trạng thái", "Thiết bị",
            "SL", "Ngày trả");
    private final JTable bangPhieu = taoBang(modelPhieu);
    private final JLabel trangThaiLabel = new JLabel(" ");

    private List<PhieuMuon> dsPhieu = List.of();
    private List<ChiTietMuon> dsChiTiet = List.of();
    private boolean dangNap;
    private boolean daNapLanDau;

    public PhieuMuonPanel() {
        this(new PhieuMuonService());
    }

    public PhieuMuonPanel(PhieuMuonService service) {
        super(new BorderLayout(0, 6));
        this.service = Objects.requireNonNull(service);
        setBorder(BorderFactory.createEmptyBorder(8, 10, 6, 10));

        JPanel bac = new JPanel();
        bac.setLayout(new BoxLayout(bac, BoxLayout.Y_AXIS));
        bac.add(taoThanhLoc());
        bac.add(taoThanhThaoTac());
        add(bac, BorderLayout.NORTH);
        add(taoVungGiua(), BorderLayout.CENTER);
        trangThaiLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        add(trangThaiLabel, BorderLayout.SOUTH);

        nutTim.addActionListener(e -> taiLai(null));
        nutTaiLai.addActionListener(e -> {
            oTim.setText("");
            locTrangThai.setSelectedIndex(0);
            taiLai(null);
        });
        oTim.addActionListener(e -> taiLai(null));
        nutTao.addActionListener(e -> tao());
        nutThemTb.addActionListener(e -> themThietBi());
        nutDuyet.addActionListener(e -> duyet());
        nutTuChoi.addActionListener(e -> tuChoi());
        nutTra.addActionListener(e -> tra());
        nutTraTatCa.addActionListener(e -> traTatCa());
        bangPhieu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !dangNap) {
                khiChonPhieu();
            }
        });
        capNhatNut(null);

        // Chỉ chạm vào MySQL khi màn hình thật sự hiện ra lần đầu.
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
        p.add(new JLabel("Mã phiếu / MSV:"));
        p.add(oTim);
        p.add(locTrangThai);
        p.add(nutTim);
        p.add(nutTaiLai);
        p.add(nutTao);
        return canTrai(p);
    }

    private JPanel taoThanhThaoTac() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.setBackground(new Color(0xDDE1E6));
        JLabel tieuDe = new JLabel("DANH SÁCH PHIẾU MƯỢN");
        tieuDe.setFont(tieuDe.getFont().deriveFont(Font.BOLD));
        p.add(tieuDe);
        p.add(nutThemTb);
        p.add(nutDuyet);
        p.add(nutTuChoi);
        p.add(nutTra);
        p.add(nutTraTatCa);
        return canTrai(p);
    }

    private static JPanel canTrai(JPanel p) {
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private JSplitPane taoVungGiua() {
        JTabbedPane duoi = new JTabbedPane();
        duoi.addTab("Chi tiết phiếu", cuon(taoBang(modelChiTiet)));
        duoi.addTab("Lịch sử mượn của thành viên", cuon(taoBang(modelLichSu)));
        JLabel goiY = new JLabel("Chọn một phiếu để xem chi tiết thiết bị và lịch sử mượn của người mượn");
        goiY.setFont(goiY.getFont().deriveFont(Font.BOLD));
        JPanel duoiVoiGoiY = new JPanel(new BorderLayout(0, 4));
        duoiVoiGoiY.add(goiY, BorderLayout.NORTH);
        duoiVoiGoiY.add(duoi, BorderLayout.CENTER);

        JSplitPane chia = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cuon(bangPhieu), duoiVoiGoiY);
        chia.setResizeWeight(0.55);
        chia.setBorder(null);
        return chia;
    }

    static DefaultTableModel taoModel(String... cot) {
        return new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    static JTable taoBang(DefaultTableModel model) {
        JTable b = new JTable(model);
        b.setRowHeight(24);
        b.setFillsViewportHeight(true);
        b.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        b.getTableHeader().setReorderingAllowed(false);
        b.getTableHeader().setFont(b.getTableHeader().getFont().deriveFont(Font.BOLD));
        DefaultTableCellRenderer giua = new DefaultTableCellRenderer();
        giua.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < model.getColumnCount(); i++) {
            String ten = model.getColumnName(i);
            if (ten.equals("SL") || ten.equals("Số lượng")) {
                b.getColumnModel().getColumn(i).setCellRenderer(giua);
            }
        }
        return b;
    }

    // ------------------------------------------------------------------ nạp dữ liệu

    /** Nạp lại danh sách theo ô lọc; nếu maCanChon khác null thì chọn lại đúng phiếu đó. */
    void taiLai(String maCanChon) {
        String trangThai = MA_LOC[locTrangThai.getSelectedIndex()];
        String tuKhoa = oTim.getText().strip().toLowerCase();
        datTrangThai("Đang tải...");
        ChayNen.chay(() -> service.danhSachPhieu(trangThai), ds -> hienPhieu(ds.stream()
                        .filter(p -> tuKhoa.isEmpty() || p.maPhieuMuon().toLowerCase().contains(tuKhoa)
                                || p.msvNguoiMuon().toLowerCase().contains(tuKhoa))
                        .toList(), maCanChon),
                loi -> {
                    hienPhieu(List.of(), null);
                    datTrangThai("Chưa thực hiện được. Kiểm tra dữ liệu hoặc kết nối MySQL. (" + loi + ")");
                });
    }

    private void hienPhieu(List<PhieuMuon> ds, String maCanChon) {
        dangNap = true;
        dsPhieu = ds;
        modelPhieu.setRowCount(0);
        int chon = -1;
        for (int i = 0; i < ds.size(); i++) {
            PhieuMuon p = ds.get(i);
            modelPhieu.addRow(new Object[]{p.maPhieuMuon(), p.msvNguoiMuon(), ngay(p.ngayMuon()), ngay(p.ngayHenTra()),
                    tenTrangThai(p.trangThaiDuyet()), p.mucDichMuon() == null ? "" : p.mucDichMuon()});
            if (p.maPhieuMuon().equals(maCanChon)) {
                chon = i;
            }
        }
        if (chon >= 0) {
            bangPhieu.setRowSelectionInterval(chon, chon);
        } else {
            bangPhieu.clearSelection();
        }
        dangNap = false;
        if (!ds.isEmpty() || maCanChon == null) {
            datTrangThai("Có " + ds.size() + " phiếu.");
        }
        khiChonPhieu();
    }

    private void khiChonPhieu() {
        PhieuMuon p = phieuDangChon();
        capNhatNut(p);
        if (p == null) {
            dsChiTiet = List.of();
            modelChiTiet.setRowCount(0);
            modelLichSu.setRowCount(0);
            return;
        }
        ChayNen.chay(() -> new ChiTietVaLichSu(service.chiTiet(p.maPhieuMuon()), service.lichSuMuon(p.msvNguoiMuon())),
                kq -> {
                    PhieuMuon dangChon = phieuDangChon();
                    if (dangChon == null || !dangChon.maPhieuMuon().equals(p.maPhieuMuon())) {
                        return;     // người dùng đã chọn phiếu khác trong lúc chờ
                    }
                    hienChiTiet(kq.chiTiet());
                    hienLichSu(kq.lichSu());
                }, this::datTrangThai);
    }

    private record ChiTietVaLichSu(List<ChiTietMuon> chiTiet, List<LichSuMuon> lichSu) {
    }

    private void hienChiTiet(List<ChiTietMuon> ds) {
        dsChiTiet = ds;
        modelChiTiet.setRowCount(0);
        for (ChiTietMuon c : ds) {
            modelChiTiet.addRow(new Object[]{c.maThietBi(), c.tenThietBi(), c.soLuongMuon(),
                    c.daTra() ? ngay(c.ngayTraThucTe()) : "(chưa trả)"});
        }
    }

    private void hienLichSu(List<LichSuMuon> ds) {
        modelLichSu.setRowCount(0);
        for (LichSuMuon l : ds) {
            boolean coThietBi = l.maThietBi() != null;
            modelLichSu.addRow(new Object[]{l.maPhieuMuon(), ngay(l.ngayMuon()), ngay(l.ngayHenTra()),
                    tenTrangThai(l.trangThaiDuyet()),
                    coThietBi ? l.maThietBi() + " - " + l.tenThietBi() : "(chưa có thiết bị)",
                    coThietBi ? l.soLuongMuon() : "-",
                    !coThietBi ? "-" : l.ngayTraThucTe() == null ? "(chưa trả)" : ngay(l.ngayTraThucTe())});
        }
    }

    private PhieuMuon phieuDangChon() {
        int dong = bangPhieu.getSelectedRow();
        return dong >= 0 && dong < dsPhieu.size() ? dsPhieu.get(dong) : null;
    }

    private void capNhatNut(PhieuMuon p) {
        boolean cho = p != null && PhieuMuon.CHO_DUYET.equals(p.trangThaiDuyet());
        boolean daDuyet = p != null && PhieuMuon.DA_DUYET.equals(p.trangThaiDuyet());
        nutThemTb.setEnabled(cho);
        nutDuyet.setEnabled(cho);
        nutTuChoi.setEnabled(cho);
        nutTra.setEnabled(daDuyet);
        nutTraTatCa.setEnabled(daDuyet);
    }

    // ------------------------------------------------------------------ thao tác

    private void tao() {
        LocalDate homNay = LocalDate.now();
        String[] v = hoiThongTin("Tạo phiếu mượn",
                new String[]{"Mã phiếu", "MSV người mượn", "Ngày mượn (yyyy-MM-dd)", "Ngày hẹn trả (yyyy-MM-dd)", "Mục đích"},
                new String[]{"", "", homNay.toString(), homNay.plusDays(7).toString(), ""});
        if (v == null) {
            return;
        }
        try {
            LocalDate muon = docNgay(v[2]);
            LocalDate hen = docNgay(v[3]);
            String ma = v[0].strip();
            chayVaTaiLai(ma, () -> service.taoPhieu(v[0], muon, hen, v[4], v[1]),
                    "Đã tạo phiếu " + ma + ". Bấm \"Thêm thiết bị vào phiếu\" để chọn thiết bị cần mượn.");
        } catch (IllegalArgumentException e) {
            canhBao(e.getMessage());
        }
    }

    private void themThietBi() {
        PhieuMuon p = phieuDangChon();
        if (p == null) {
            return;
        }
        String[] v = hoiThongTin("Thêm thiết bị vào phiếu " + p.maPhieuMuon(),
                new String[]{"Mã thiết bị", "Số lượng"}, new String[]{"", "1"});
        if (v == null) {
            return;
        }
        try {
            int soLuong = docSo(v[1]);
            chayVaTaiLai(p.maPhieuMuon(), () -> service.themThietBi(p.maPhieuMuon(), v[0], soLuong),
                    "Đã thêm thiết bị " + v[0].strip() + " vào phiếu " + p.maPhieuMuon() + ".");
        } catch (IllegalArgumentException e) {
            canhBao(e.getMessage());
        }
    }

    private void duyet() {
        PhieuMuon p = phieuDangChon();
        if (p != null && xacNhan("Duyệt phiếu " + p.maPhieuMuon() + "?\nSố lượng khả dụng của các thiết bị trong phiếu sẽ bị trừ.")) {
            guiDuyet(p);
        }
    }

    void guiDuyet(PhieuMuon p) {
        chayVaTaiLai(p.maPhieuMuon(), () -> service.duyet(p.maPhieuMuon()),
                "Đã duyệt phiếu " + p.maPhieuMuon() + ", số lượng khả dụng đã được trừ.");
    }

    private void tuChoi() {
        PhieuMuon p = phieuDangChon();
        if (p != null && xacNhan("Từ chối phiếu " + p.maPhieuMuon() + "?")) {
            chayVaTaiLai(p.maPhieuMuon(), () -> service.tuChoi(p.maPhieuMuon()), "Đã từ chối phiếu " + p.maPhieuMuon() + ".");
        }
    }

    private void tra() {
        PhieuMuon p = phieuDangChon();
        if (p == null) {
            return;
        }
        List<ChiTietMuon> conLai = dsChiTiet.stream().filter(c -> !c.daTra()).toList();
        if (conLai.isEmpty()) {
            canhBao("Phiếu không còn thiết bị nào chưa trả.");
            return;
        }
        JComboBox<String> chon = new JComboBox<>(conLai.stream()
                .map(c -> c.maThietBi() + " - " + c.tenThietBi() + " (x" + c.soLuongMuon() + ")").toArray(String[]::new));
        JTextField oNgay = new JTextField(LocalDate.now().toString(), 12);
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 6));
        form.add(new JLabel("Thiết bị trả"));
        form.add(chon);
        form.add(new JLabel("Ngày trả (yyyy-MM-dd)"));
        form.add(oNgay);
        if (JOptionPane.showConfirmDialog(this, form, "Ghi nhận trả - phiếu " + p.maPhieuMuon(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        ChiTietMuon c = conLai.get(chon.getSelectedIndex());
        try {
            LocalDate ngayTra = oNgay.getText().isBlank() ? LocalDate.now() : docNgay(oNgay.getText());
            chayVaTaiLai(p.maPhieuMuon(), () -> service.traThietBi(p.maPhieuMuon(), c.maThietBi(), ngayTra),
                    "Đã ghi nhận trả " + c.maThietBi() + ", số lượng khả dụng đã được cộng lại.");
        } catch (IllegalArgumentException e) {
            canhBao(e.getMessage());
        }
    }

    private void traTatCa() {
        PhieuMuon p = phieuDangChon();
        if (p == null) {
            return;
        }
        String[] v = hoiThongTin("Trả tất cả - phiếu " + p.maPhieuMuon(),
                new String[]{"Ngày trả (yyyy-MM-dd)"}, new String[]{LocalDate.now().toString()});
        if (v == null) {
            return;
        }
        try {
            LocalDate ngayTra = v[0].isBlank() ? LocalDate.now() : docNgay(v[0]);
            chayVaTaiLai(p.maPhieuMuon(), () -> service.traTatCa(p.maPhieuMuon(), ngayTra),
                    "Đã ghi nhận trả toàn bộ thiết bị còn lại của phiếu " + p.maPhieuMuon() + ".");
        } catch (IllegalArgumentException e) {
            canhBao(e.getMessage());
        }
    }

    /** Chạy một thao tác ghi ở luồng nền; xong thì tải lại danh sách và chọn lại phiếu maCanChon. */
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

    private boolean xacNhan(String cauHoi) {
        return JOptionPane.showConfirmDialog(this, cauHoi, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
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

    static LocalDate docNgay(String text) {
        try {
            return LocalDate.parse(text.strip(), NGAY_NHAP);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày không hợp lệ. Nhập yyyy-MM-dd, ví dụ 2026-10-25.");
        }
    }

    static int docSo(String text) {
        try {
            return Integer.parseInt(text.strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + text + "\" không phải số nguyên hợp lệ.");
        }
    }

    static String ngay(LocalDate d) {
        return d == null ? "" : d.format(NGAY_HIEN);
    }

    static String tenTrangThai(String ma) {
        if (PhieuMuon.CHO_DUYET.equals(ma)) {
            return "Chờ duyệt";
        }
        if (PhieuMuon.DA_DUYET.equals(ma)) {
            return "Đã duyệt";
        }
        if (PhieuMuon.TU_CHOI.equals(ma)) {
            return "Từ chối";
        }
        return ma == null ? "" : ma;
    }
    /** Bọc bảng vào khung cuộn và luôn hiện hàng tiêu đề cột. */
    static JScrollPane cuon(JTable b) {
        JScrollPane c = new JScrollPane(b);
        c.setColumnHeaderView(b.getTableHeader());
        return c;
    }
}
