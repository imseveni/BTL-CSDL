package thanhvien;

import common.Refreshable;
import common.SwingSupport;
import quyenhan.QuyenHan;
import quyenhan.QuyenHanService;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình Quản lý Thành viên. Phía trên: bảng thành viên (tìm kiếm, thêm, sửa, đổi trạng thái, xóa).
 * Phía dưới, cho thành viên đang chọn: tab Lịch sử hoạt động và tab Quyền hạn (cấp, thu hồi quyền).
 * Đây là màn hình MẪU của nhóm (xem docs/SWING_HUONG_DAN.md).
 */
public class ThanhVienSwingUi extends JPanel implements Refreshable {

    private static final String ALL_BANS = "Tất cả ban";
    private static final String ALL_STATUS = "Tất cả trạng thái";

    private final ThanhVienService service;
    private final QuyenHanService quyenService;
    private final JTextField keyword = new JTextField(20);
    private final JComboBox<Object> banFilter = new JComboBox<>(new Object[] { ALL_BANS });
    private final JComboBox<String> statusFilter = new JComboBox<>();
    private final DefaultTableModel members = SwingSupport.model("MSV", "Họ và tên", "Ngày sinh", "Giới tính",
            "Email", "Số điện thoại", "Ngày gia nhập", "Trạng thái", "Ban", "Chức vụ");
    private final DefaultTableModel history = SwingSupport.model("Loại hoạt động", "Mã", "Tên", "Chi tiết", "Ngày");
    private final DefaultTableModel permissions = SwingSupport.model("Mã quyền", "Tên quyền", "Mô tả chi tiết");
    private final JTable memberTable = SwingSupport.table(members, "thanhvien.table");
    private final JTable historyTable = SwingSupport.table(history, "thanhvien.history");
    private final JTable permissionTable = SwingSupport.table(permissions, "thanhvien.quyen.table");
    private final JComboBox<QuyenItem> grantable = new JComboBox<>();
    private final JLabel historyTitle = new JLabel("Chọn một thành viên để xem lịch sử hoạt động và quyền hạn");
    private final JLabel summary = new JLabel(" ");
    private final JLabel status = new JLabel("Đang chờ nạp danh sách thành viên.");
    private final SwingSupport.Tasks tasks = new SwingSupport.Tasks(this, status);
    private List<ThanhVien> rows = new ArrayList<>();
    private List<BanLuaChon> bans = new ArrayList<>();
    private List<QuyenHan> granted = new ArrayList<>();
    private boolean rendering;

    /** Một quyền hạn trong ô chọn, hiển thị "QH02 – Duyệt phiếu mượn". */
    private record QuyenItem(QuyenHan quyen) {
        @Override
        public String toString() {
            return quyen.maQuyenHan() + " – " + quyen.tenQuyenHan();
        }
    }

    public ThanhVienSwingUi(ThanhVienService service) {
        this(service, new QuyenHanService());
    }

    public ThanhVienSwingUi(ThanhVienService service, QuyenHanService quyenService) {
        this.service = service;
        this.quyenService = quyenService;
        setName("thanhvien.panel");
        setLayout(new BorderLayout(10, 10));
        setBackground(SwingSupport.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        keyword.setName("thanhvien.keyword");
        keyword.setToolTipText("Tìm chuỗi con trong MSV hoặc họ tên. Trống = tất cả.");
        keyword.addActionListener(event -> reload(null));
        banFilter.setName("thanhvien.ban");
        statusFilter.setName("thanhvien.trangthai");
        statusFilter.addItem(ALL_STATUS);
        ThanhVienService.TRANG_THAI.forEach(statusFilter::addItem);
        grantable.setName("thanhvien.quyen.combo");
        // Cố định độ rộng các ô chọn để không bị co nhỏ khi danh sách trống.
        banFilter.setPrototypeDisplayValue("BAN00 – Ban Truyền thông dài");
        statusFilter.setPrototypeDisplayValue(ALL_STATUS + "  ");
        grantable.setPrototypeDisplayValue(new QuyenItem(new QuyenHan("QH00", "Quản lý hoạt động của CLB", null)));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        toolbar.add(new JLabel("MSV / họ tên:"));
        toolbar.add(keyword);
        toolbar.add(banFilter);
        toolbar.add(statusFilter);
        toolbar.add(SwingSupport.button("Tìm kiếm", "thanhvien.search", () -> reload(null)));
        toolbar.add(SwingSupport.button("Tải lại", "thanhvien.reload", () -> reload(selectedMsv())));
        toolbar.add(SwingSupport.button("Thêm thành viên", "thanhvien.add", () -> editMember(null)));
        add(toolbar, BorderLayout.NORTH);

        JPanel memberSection = new JPanel(new BorderLayout(6, 6));
        JPanel memberActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        JLabel title = new JLabel("DANH SÁCH THÀNH VIÊN");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        memberActions.add(title);
        memberActions.add(SwingSupport.button("Sửa thông tin cá nhân", "thanhvien.edit", () -> editMember(selected())));
        memberActions.add(SwingSupport.button("Đổi trạng thái", "thanhvien.status.change", this::changeStatus));
        memberActions.add(SwingSupport.button("Xóa thành viên", "thanhvien.delete", this::deleteMember));
        memberSection.add(memberActions, BorderLayout.NORTH);
        memberSection.add(SwingSupport.scroll(memberTable), BorderLayout.CENTER);

        // Phần dưới: hai tab cho thành viên đang chọn.
        JPanel historyTab = new JPanel(new BorderLayout(6, 6));
        summary.setName("thanhvien.summary");
        historyTab.add(summary, BorderLayout.NORTH);
        historyTab.add(SwingSupport.scroll(historyTable), BorderLayout.CENTER);

        JPanel permissionTab = new JPanel(new BorderLayout(6, 6));
        JPanel grantBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        grantBar.add(new JLabel("Cấp thêm quyền:"));
        grantBar.add(grantable);
        grantBar.add(SwingSupport.button("Cấp quyền", "thanhvien.quyen.grant", this::grant));
        grantBar.add(SwingSupport.button("Thu hồi quyền đang chọn", "thanhvien.quyen.revoke", this::revoke));
        permissionTab.add(grantBar, BorderLayout.NORTH);
        permissionTab.add(SwingSupport.scroll(permissionTable), BorderLayout.CENTER);

        JTabbedPane detailTabs = new JTabbedPane();
        detailTabs.setName("thanhvien.detail.tabs");
        detailTabs.addTab("Lịch sử hoạt động", historyTab);
        detailTabs.addTab("Quyền hạn", permissionTab);

        JPanel detailSection = new JPanel(new BorderLayout(6, 6));
        historyTitle.setFont(historyTitle.getFont().deriveFont(Font.BOLD));
        detailSection.add(historyTitle, BorderLayout.NORTH);
        detailSection.add(detailTabs, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, memberSection, detailSection);
        split.setResizeWeight(0.55);
        split.setDividerLocation(280);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
        status.setName("thanhvien.status");
        add(status, BorderLayout.SOUTH);

        // Độ rộng gợi ý cho từng cột của bảng thành viên (cột Email cần rộng nhất).
        int[] widths = { 110, 170, 90, 75, 220, 110, 105, 115, 140, 120 };
        for (int i = 0; i < widths.length; i++)
            memberTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        memberTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && !rendering && !tasks.isBusy())
                loadDetail();
        });
    }

    @Override
    public boolean isBusy() {
        return tasks.isBusy();
    }

    @Override
    public void resetAndReload() {
        keyword.setText("");
        banFilter.setSelectedIndex(0);
        statusFilter.setSelectedIndex(0);
        rendering = true;
        try {
            rows = new ArrayList<>();
            members.setRowCount(0);
            clearDetail();
        } finally {
            rendering = false;
        }
        reload(null);
    }

    /** Kết quả một lần tải: danh sách thành viên theo bộ lọc và danh sách ban hiện có. */
    private record Loaded(List<ThanhVien> rows, List<BanLuaChon> bans) {
    }

    /** Dữ liệu của thành viên đang chọn: lịch sử, tổng quan, quyền đang có và toàn bộ quyền để chọn cấp. */
    private record Detail(String msv, List<HoatDong> history, TongQuanThanhVien overview,
                          List<QuyenHan> granted, List<QuyenHan> allPermissions) {
    }

    private void reload(String keepMsv) {
        String query = keyword.getText();
        String ban = banFilter.getSelectedItem() instanceof BanLuaChon b ? b.maBan() : null;
        String trangThai = statusFilter.getSelectedIndex() <= 0 ? null : (String) statusFilter.getSelectedItem();
        tasks.run("Đang tải thành viên...", () -> new Loaded(service.tim(query, ban, trangThai), service.danhSachBan()),
                loaded -> {
                    rendering = true;
                    try {
                        showBans(loaded.bans());
                        rows = loaded.rows();
                        members.setRowCount(0);
                        for (ThanhVien tv : rows)
                            members.addRow(new Object[] { tv.msv(), tv.hoVaTen(), SwingSupport.text(tv.ngaySinh()),
                                    SwingSupport.text(tv.gioiTinh()), SwingSupport.text(tv.email()),
                                    SwingSupport.text(tv.sdt()), SwingSupport.text(tv.ngayGiaNhap()),
                                    SwingSupport.text(tv.trangThaiHD()), SwingSupport.text(tv.tenBan()),
                                    SwingSupport.text(tv.chucVu()) });
                        int index = 0;
                        for (int i = 0; i < rows.size(); i++) {
                            if (rows.get(i).msv().equals(keepMsv)) {
                                index = i;
                                break;
                            }
                        }
                        if (!rows.isEmpty()) {
                            int view = memberTable.convertRowIndexToView(index);
                            memberTable.setRowSelectionInterval(view, view);
                        }
                    } finally {
                        rendering = false;
                    }
                    loadDetail();
                });
    }

    /** Cập nhật danh sách ban trong ô lọc, giữ nguyên ban đang chọn nếu còn tồn tại. */
    private void showBans(List<BanLuaChon> latest) {
        bans = new ArrayList<>(latest);
        BanLuaChon current = banFilter.getSelectedItem() instanceof BanLuaChon b ? b : null;
        banFilter.removeAllItems();
        banFilter.addItem(ALL_BANS);
        BanLuaChon restore = null;
        for (BanLuaChon ban : latest) {
            banFilter.addItem(ban);
            if (current != null && current.maBan().equals(ban.maBan()))
                restore = ban;
        }
        banFilter.setSelectedItem(restore == null ? ALL_BANS : restore);
    }

    private void clearDetail() {
        history.setRowCount(0);
        permissions.setRowCount(0);
        grantable.removeAllItems();
        granted = new ArrayList<>();
        summary.setText(" ");
    }

    private void loadDetail() {
        clearDetail();
        String msv = selectedMsv();
        if (msv == null) {
            historyTitle.setText("Chọn một thành viên để xem lịch sử hoạt động và quyền hạn");
            status.setText("Tìm thấy " + rows.size() + " thành viên.");
            return;
        }
        historyTitle.setText("Thành viên đang chọn — " + msv + " · " + selected().hoVaTen());
        tasks.run("Đang tải chi tiết thành viên...",
                () -> new Detail(msv, service.lichSu(msv), service.tongQuan(msv), quyenService.quyenCuaThanhVien(msv),
                        quyenService.danhSach()),
                detail -> {
                    if (!detail.msv().equals(selectedMsv())) {
                        loadDetail();
                        return;
                    }
                    for (HoatDong h : detail.history())
                        history.addRow(new Object[] { h.loai(), h.ma(), SwingSupport.text(h.ten()),
                                SwingSupport.text(h.chiTiet()), SwingSupport.text(h.ngay()) });
                    TongQuanThanhVien t = detail.overview();
                    summary.setText("Khóa học: " + t.soKhoaHoc() + "   |   Dự án: " + t.soDuAn() + "   |   Sự kiện: "
                            + t.soSuKien() + "   |   Đánh giá: " + t.soDanhGia() + "   |   Phiếu mượn: " + t.soPhieuMuon());
                    granted = new ArrayList<>(detail.granted());
                    for (QuyenHan q : granted)
                        permissions.addRow(new Object[] { q.maQuyenHan(), q.tenQuyenHan(), SwingSupport.text(q.moTaChiTiet()) });
                    for (QuyenHan q : detail.allPermissions()) {
                        boolean already = granted.stream().anyMatch(g -> g.maQuyenHan().equals(q.maQuyenHan()));
                        if (!already)
                            grantable.addItem(new QuyenItem(q));
                    }
                    status.setText(rows.size() + " thành viên  |  " + detail.history().size() + " hoạt động, "
                            + granted.size() + " quyền của thành viên đang chọn.");
                });
    }

    /** Cấp quyền đang chọn trong ô cho thành viên đang chọn (ghi vào bảng ThanhVien_QuyenHan). */
    private void grant() {
        String msv = selected().msv();
        QuyenItem item = (QuyenItem) grantable.getSelectedItem();
        if (item == null)
            throw new IllegalArgumentException("Không còn quyền nào để cấp cho thành viên này.");
        tasks.run("Đang cấp quyền...", () -> {
            quyenService.capQuyen(msv, item.quyen().maQuyenHan());
            return true;
        }, ok -> loadDetail());
    }

    /** Thu hồi quyền đang chọn trong bảng quyền của thành viên. */
    private void revoke() {
        String msv = selected().msv();
        int view = permissionTable.getSelectedRow();
        if (view < 0)
            throw new IllegalArgumentException("Hãy chọn một quyền trong bảng Quyền hạn để thu hồi.");
        QuyenHan quyen = granted.get(permissionTable.convertRowIndexToModel(view));
        if (SwingSupport.confirm(this, "Thu hồi quyền " + quyen.maQuyenHan() + " – " + quyen.tenQuyenHan()
                + " của " + msv + "?")) {
            tasks.run("Đang thu hồi quyền...", () -> {
                quyenService.thuHoiQuyen(msv, quyen.maQuyenHan());
                return true;
            }, ok -> loadDetail());
        }
    }

    private String selectedMsv() {
        int view = memberTable.getSelectedRow();
        return view < 0 ? null : rows.get(memberTable.convertRowIndexToModel(view)).msv();
    }

    private ThanhVien selected() {
        int view = memberTable.getSelectedRow();
        if (view < 0)
            throw new IllegalArgumentException("Hãy chọn một thành viên trong bảng.");
        return rows.get(memberTable.convertRowIndexToModel(view));
    }

    private static String optional(Object value) {
        return value == null || value.toString().isBlank() ? null : value.toString();
    }

    /** Thêm mới khi current == null; sửa thông tin cá nhân khi có current. */
    private void editMember(ThanhVien current) {
        if (current == null && bans.isEmpty())
            throw new IllegalArgumentException(
                    "Chưa có ban chuyên môn nào. Hãy tạo ban ở tab Ban chuyên môn trước khi thêm thành viên.");
        boolean adding = current == null;
        JTextField msv = new JTextField(adding ? "" : current.msv(), 28);
        msv.setEditable(adding);
        JTextField name = new JTextField(adding ? "" : current.hoVaTen());
        JTextField birth = new JTextField(adding || current.ngaySinh() == null ? "" : current.ngaySinh().toString());
        JComboBox<String> gender = new JComboBox<>(new String[] { "", "Nam", "Nữ", "Khác" });
        if (!adding && current.gioiTinh() != null)
            gender.setSelectedItem(current.gioiTinh());
        JTextField email = new JTextField(adding || current.email() == null ? "" : current.email());
        JTextField phone = new JTextField(adding || current.sdt() == null ? "" : current.sdt());
        phone.setToolTipText("9 đến 15 chữ số, có thể bắt đầu bằng +.");

        if (adding) {
            JTextField join = new JTextField(LocalDate.now().toString());
            JComboBox<String> state = new JComboBox<>(ThanhVienService.TRANG_THAI.toArray(new String[0]));
            JComboBox<BanLuaChon> ban = new JComboBox<>(bans.toArray(new BanLuaChon[0]));
            JTextField position = new JTextField("Thành viên");
            JPanel form = SwingSupport.form(new String[] { "MSV *", "Họ và tên *", "Ngày sinh", "Giới tính", "Email",
                    "Số điện thoại", "Ngày gia nhập *", "Trạng thái", "Ban *", "Chức vụ" },
                    msv, name, birth, gender, email, phone, join, state, ban, position);
            SwingSupport.editor(this, "Thêm thành viên", form,
                    () -> new ThanhVien(msv.getText(), name.getText(), SwingSupport.date(birth, "Ngày sinh", false),
                            optional(gender.getSelectedItem()), email.getText(), phone.getText(),
                            SwingSupport.date(join, "Ngày gia nhập", true), (String) state.getSelectedItem(),
                            ((BanLuaChon) ban.getSelectedItem()).maBan(), null, position.getText()),
                    service::them, () -> reload(msv.getText().trim()));
        } else {
            JPanel form = SwingSupport.form(new String[] { "MSV", "Họ và tên *", "Ngày sinh", "Giới tính", "Email",
                    "Số điện thoại" }, msv, name, birth, gender, email, phone);
            SwingSupport.editor(this, "Sửa thông tin cá nhân",
                    "* Bắt buộc. Ban và chức vụ đổi ở tab Ban chuyên môn; trạng thái dùng nút Đổi trạng thái.", "Lưu",
                    form,
                    () -> new ThanhVien(current.msv(), name.getText(), SwingSupport.date(birth, "Ngày sinh", false),
                            optional(gender.getSelectedItem()), email.getText(), phone.getText(), null, null, null,
                            null, null),
                    service::capNhat, () -> reload(current.msv()));
        }
    }

    private void changeStatus() {
        ThanhVien current = selected();
        JTextField msv = new JTextField(current.msv() + " · " + current.hoVaTen(), 28);
        msv.setEditable(false);
        JComboBox<String> state = new JComboBox<>(ThanhVienService.TRANG_THAI.toArray(new String[0]));
        if (current.trangThaiHD() != null)
            state.setSelectedItem(current.trangThaiHD());
        JPanel form = SwingSupport.form(new String[] { "Thành viên", "Trạng thái mới *" }, msv, state);
        SwingSupport.editor(this, "Đổi trạng thái hoạt động",
                "Nên dùng \"Đã rời CLB\" thay cho xóa để giữ lịch sử hoạt động.", "Đổi", form,
                () -> (String) state.getSelectedItem(),
                value -> service.doiTrangThai(current.msv(), value), () -> reload(current.msv()));
    }

    private void deleteMember() {
        ThanhVien current = selected();
        if (SwingSupport.confirm(this, "Xóa thành viên " + current.msv() + " · " + current.hoVaTen()
                + "?\nChỉ xóa được khi chưa có lịch sử hoạt động; nếu không hãy đổi trạng thái thành Đã rời CLB.")) {
            tasks.run("Đang xóa thành viên...", () -> {
                service.xoa(current.msv());
                return true;
            }, result -> reload(null));
        }
    }
}
