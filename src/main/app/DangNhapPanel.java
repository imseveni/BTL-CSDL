package app;

import common.PhienLamViec.NguoiDung;
import common.PhienLamViec.VaiTro;
import common.SwingSupport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * Màn hình đầu tiên: chọn đang dùng hệ thống với tư cách Thành viên hay Ban Chủ nhiệm / Người quản lý CLB,
 * rồi xác định bằng MSV (và mã xác nhận với Ban Chủ nhiệm). Sau khi thành công gọi {@code onLogin}.
 */
public class DangNhapPanel extends JPanel {

    private final DangNhapService service;
    private final Consumer<NguoiDung> onLogin;
    private final JRadioButton thanhVien = new JRadioButton("Thành viên Câu lạc bộ", true);
    private final JRadioButton banChuNhiem = new JRadioButton("Ban Chủ nhiệm / Người quản lý Câu lạc bộ");
    private final JTextField msv = new JTextField(22);
    private final JPasswordField ma = new JPasswordField(22);
    private final JLabel maLabel = new JLabel("Mã xác nhận *");
    private final JLabel moTa = new JLabel(" ");
    private final JLabel status = new JLabel(" ");
    private final JButton login;
    private boolean busy;

    public DangNhapPanel(DangNhapService service, Consumer<NguoiDung> onLogin) {
        super(new GridBagLayout());
        this.service = service;
        this.onLogin = onLogin;
        setName("dangnhap.panel");
        setBackground(SwingSupport.BACKGROUND);

        thanhVien.setName("dangnhap.thanhvien");
        banChuNhiem.setName("dangnhap.banchunhiem");
        msv.setName("dangnhap.msv");
        ma.setName("dangnhap.ma");
        moTa.setName("dangnhap.mota");
        status.setName("dangnhap.status");
        status.setForeground(new Color(150, 40, 40));
        ButtonGroup group = new ButtonGroup();
        group.add(thanhVien);
        group.add(banChuNhiem);
        thanhVien.addActionListener(e -> capNhatVaiTro());
        banChuNhiem.addActionListener(e -> capNhatVaiTro());
        msv.addActionListener(e -> dangNhap());
        ma.addActionListener(e -> dangNhap());

        login = SwingSupport.button("Đăng nhập", "dangnhap.login", this::dangNhap);
        JButton connect = SwingSupport.button("Kết nối MySQL", "dangnhap.ketnoi", this::ketNoi);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 216, 224)),
                BorderFactory.createEmptyBorder(26, 34, 26, 34)));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);

        JLabel title = new JLabel("QUẢN LÝ CÂU LẠC BỘ");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(SwingSupport.ACCENT);
        c.gridy = 0;
        card.add(title, c);
        c.gridy = 1;
        card.add(new JLabel("Chọn vai trò sử dụng hệ thống"), c);
        c.gridy = 2;
        card.add(thanhVien, c);
        c.gridy = 3;
        card.add(banChuNhiem, c);
        c.gridy = 4;
        moTa.setFont(moTa.getFont().deriveFont(Font.ITALIC, 12f));
        card.add(moTa, c);

        c.gridwidth = 1;
        c.gridy = 5;
        c.gridx = 0;
        c.insets = new Insets(14, 0, 4, 10);
        card.add(new JLabel("Mã sinh viên *"), c);
        c.gridx = 1;
        c.insets = new Insets(14, 0, 4, 0);
        card.add(msv, c);
        c.gridy = 6;
        c.gridx = 0;
        c.insets = new Insets(4, 0, 4, 10);
        card.add(maLabel, c);
        c.gridx = 1;
        c.insets = new Insets(4, 0, 4, 0);
        card.add(ma, c);

        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 7;
        c.insets = new Insets(14, 0, 4, 0);
        card.add(status, c);
        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(connect);
        buttons.add(login);
        c.gridy = 8;
        card.add(buttons, c);

        add(card);
        capNhatVaiTro();
    }

    private void capNhatVaiTro() {
        boolean bcn = banChuNhiem.isSelected();
        ma.setEnabled(bcn);
        maLabel.setEnabled(bcn);
        if (!bcn) {
            ma.setText("");
        }
        moTa.setText(bcn
                ? "Quản lý thành viên, cơ sở vật chất, duyệt phiếu mượn và lập báo cáo."
                : "Xem hồ sơ của mình, tra cứu thiết bị, lập và theo dõi phiếu mượn của mình.");
        status.setText(" ");
    }

    private void dangNhap() {
        if (busy) {
            return;
        }
        boolean bcn = banChuNhiem.isSelected();
        String id = msv.getText();
        String code = new String(ma.getPassword());
        busy = true;
        login.setEnabled(false);
        status.setForeground(Color.DARK_GRAY);
        status.setText("Đang kiểm tra...");
        new javax.swing.SwingWorker<NguoiDung, Void>() {
            @Override
            protected NguoiDung doInBackground() {
                return bcn ? service.dangNhapBanChuNhiem(id, code) : service.dangNhapThanhVien(id);
            }

            @Override
            protected void done() {
                busy = false;
                login.setEnabled(true);
                try {
                    NguoiDung nguoi = get();
                    ma.setText("");
                    onLogin.accept(nguoi);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (java.util.concurrent.ExecutionException e) {
                    status.setForeground(new Color(150, 40, 40));
                    Throwable cause = e.getCause();
                    status.setText("<html>" + (cause instanceof IllegalArgumentException
                            ? cause.getMessage()
                            : "Không kết nối được cơ sở dữ liệu. Bấm Kết nối MySQL để kiểm tra.") + "</html>");
                }
            }
        }.execute();
    }

    private void ketNoi() {
        try {
            ClubWorkspace.hopThoaiKetNoi(this, () -> status.setText("Đã kiểm tra kết nối MySQL. Hãy đăng nhập."));
        } catch (SQLException e) {
            SwingSupport.error(this, e);
        }
    }

    /** Vai trò đang chọn (dùng cho kiểm thử). */
    VaiTro vaiTroDangChon() {
        return banChuNhiem.isSelected() ? VaiTro.BAN_CHU_NHIEM : VaiTro.THANH_VIEN;
    }
}
