package thanhvien;

import common.PhienLamViec;
import common.Refreshable;
import common.SwingSupport;
import quyenhan.QuyenHan;
import quyenhan.QuyenHanService;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Tab "Hồ sơ của tôi" dành cho vai trò Thành viên (theo mô tả đề tài): xem ban chuyên môn, chức vụ,
 * trạng thái; cập nhật một số thông tin cá nhân của chính mình; xem lịch sử hoạt động và quyền hạn.
 * Ban, chức vụ và trạng thái chỉ đọc: do Ban Chủ nhiệm thay đổi. Quyền được kiểm tra lại ở Service.
 */
public class HoSoCaNhanPanel extends JPanel implements Refreshable {

    private final ThanhVienService service;
    private final QuyenHanService quyenService;
    private final JLabel msv = new JLabel("—");
    private final JLabel ban = new JLabel("—");
    private final JLabel chucVu = new JLabel("—");
    private final JLabel trangThai = new JLabel("—");
    private final JLabel ngayGiaNhap = new JLabel("—");
    private final JTextField hoTen = new JTextField(28);
    private final JTextField ngaySinh = new JTextField(12);
    private final JComboBox<String> gioiTinh = new JComboBox<>(new String[] { "", "Nam", "Nữ", "Khác" });
    private final JTextField email = new JTextField(28);
    private final JTextField sdt = new JTextField(16);
    private final JLabel summary = new JLabel(" ");
    private final DefaultTableModel history = SwingSupport.model("Loại", "Mã", "Tên", "Chi tiết", "Ngày");
    private final DefaultTableModel permissions = SwingSupport.model("Mã quyền", "Tên quyền", "Mô tả");
    private final JLabel status = new JLabel("Đang tải hồ sơ...");
    private final SwingSupport.Tasks tasks = new SwingSupport.Tasks(this, status);
    private final JButton save;
    private ThanhVien current;

    public HoSoCaNhanPanel(ThanhVienService service, QuyenHanService quyenService) {
        this.service = service;
        this.quyenService = quyenService;
        setName("hoso.panel");
        setLayout(new BorderLayout(10, 10));
        setBackground(SwingSupport.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        hoTen.setName("hoso.hoten");
        ngaySinh.setName("hoso.ngaysinh");
        ngaySinh.setToolTipText("Định dạng yyyy-MM-dd");
        gioiTinh.setName("hoso.gioitinh");
        email.setName("hoso.email");
        sdt.setName("hoso.sdt");
        sdt.setToolTipText("9 đến 15 chữ số, có thể bắt đầu bằng +.");
        msv.setName("hoso.msv");
        ban.setName("hoso.ban");
        chucVu.setName("hoso.chucvu");
        trangThai.setName("hoso.trangthai");
        summary.setName("hoso.summary");
        status.setName("hoso.status");
        save = SwingSupport.button("Lưu thay đổi", "hoso.save", this::save);

        JPanel form = SwingSupport.form(
                new String[] { "Mã sinh viên", "Ban chuyên môn", "Chức vụ", "Trạng thái", "Ngày gia nhập",
                        "Họ và tên *", "Ngày sinh", "Giới tính", "Email", "Số điện thoại" },
                msv, ban, chucVu, trangThai, ngayGiaNhap, hoTen, ngaySinh, gioiTinh, email, sdt);
        JPanel top = new JPanel(new BorderLayout(6, 6));
        JLabel title = new JLabel("HỒ SƠ CỦA TÔI");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        top.add(title, BorderLayout.NORTH);
        top.add(form, BorderLayout.CENTER);
        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 7, 0));
        actions.add(save);
        actions.add(new JLabel("Ban, chức vụ, trạng thái do Ban Chủ nhiệm thay đổi."));
        top.add(actions, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        JPanel historyTab = new JPanel(new BorderLayout(6, 6));
        historyTab.add(summary, BorderLayout.NORTH);
        historyTab.add(SwingSupport.scroll(SwingSupport.table(history, "hoso.history")), BorderLayout.CENTER);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Lịch sử hoạt động", historyTab);
        tabs.addTab("Quyền hạn của tôi", SwingSupport.scroll(SwingSupport.table(permissions, "hoso.quyen")));
        add(tabs, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
    }

    private record Loaded(ThanhVien member, List<HoatDong> history, TongQuanThanhVien overview, List<QuyenHan> granted) {
    }

    @Override
    public boolean isBusy() {
        return tasks.isBusy();
    }

    @Override
    public void resetAndReload() {
        String me = PhienLamViec.msvHienTai();
        if (me == null) {
            status.setText("Không có thành viên đăng nhập.");
            return;
        }
        tasks.run("Đang tải hồ sơ...", () -> new Loaded(service.chiTiet(me), service.lichSu(me),
                service.tongQuan(me), quyenService.quyenCuaThanhVien(me)), this::show);
    }

    private void show(Loaded loaded) {
        current = loaded.member();
        ThanhVien tv = current;
        msv.setText(tv.msv());
        ban.setText(SwingSupport.text(tv.tenBan()));
        chucVu.setText(SwingSupport.text(tv.chucVu()));
        trangThai.setText(SwingSupport.text(tv.trangThaiHD()));
        ngayGiaNhap.setText(SwingSupport.text(tv.ngayGiaNhap()));
        hoTen.setText(tv.hoVaTen());
        ngaySinh.setText(tv.ngaySinh() == null ? "" : tv.ngaySinh().toString());
        gioiTinh.setSelectedItem(tv.gioiTinh() == null ? "" : tv.gioiTinh());
        email.setText(tv.email() == null ? "" : tv.email());
        sdt.setText(tv.sdt() == null ? "" : tv.sdt());
        history.setRowCount(0);
        for (HoatDong h : loaded.history()) {
            history.addRow(new Object[] { h.loai(), h.ma(), SwingSupport.text(h.ten()), SwingSupport.text(h.chiTiet()),
                    SwingSupport.text(h.ngay()) });
        }
        TongQuanThanhVien t = loaded.overview();
        summary.setText("Khóa học: " + t.soKhoaHoc() + "   |   Dự án: " + t.soDuAn() + "   |   Sự kiện: " + t.soSuKien()
                + "   |   Đánh giá: " + t.soDanhGia() + "   |   Phiếu mượn: " + t.soPhieuMuon());
        permissions.setRowCount(0);
        for (QuyenHan q : loaded.granted()) {
            permissions.addRow(new Object[] { q.maQuyenHan(), q.tenQuyenHan(), SwingSupport.text(q.moTaChiTiet()) });
        }
        status.setText("Hồ sơ của " + tv.hoVaTen() + ".");
    }

    private void save() {
        if (current == null) {
            throw new IllegalArgumentException("Chưa tải được hồ sơ của bạn.");
        }
        ThanhVien edited = new ThanhVien(current.msv(), hoTen.getText(), SwingSupport.date(ngaySinh, "Ngày sinh", false),
                emptyToNull((String) gioiTinh.getSelectedItem()), emptyToNull(email.getText()), emptyToNull(sdt.getText()),
                null, null, null, null, null);
        tasks.run("Đang lưu...", () -> {
            service.capNhat(edited);
            return true;
        }, ok -> resetAndReload());
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
