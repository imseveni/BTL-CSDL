package app;

import static org.junit.jupiter.api.Assertions.*;

import common.PhienLamViec.NguoiDung;
import common.PhienLamViec.VaiTro;
import java.awt.Component;
import java.awt.Container;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import thanhvien.ThanhVien;

/** Màn hình chọn vai trò, chạy không cần màn hình thật và không cần MySQL. */
class DangNhapPanelTest {

    private final DangNhapService service = new DangNhapService(msv -> {
        if (!msv.equals("B21DCCN001")) {
            throw new common.DataAccessException("Thành viên không tồn tại", null);
        }
        return new ThanhVien(msv, "Nguyễn Văn An", null, null, null, null, LocalDate.of(2024, 9, 1),
                "Đang hoạt động", "BAN01", null, "Chủ nhiệm");
    }, "MA-DEMO");

    private static Component find(Container root, String name) {
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName())) return c;
            if (c instanceof Container child) {
                Component found = find(child, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static void waitFor(BooleanSupplier condition) throws Exception {
        for (int i = 0; i < 200; i++) {
            SwingUtilities.invokeAndWait(() -> { });
            if (condition.getAsBoolean()) return;
            Thread.sleep(25);
        }
        fail("Hết thời gian chờ giao diện cập nhật.");
    }

    @Test void thanhVienDangNhapKhongCanMaXacNhan() throws Exception {
        AtomicReference<NguoiDung> got = new AtomicReference<>();
        var panel = new DangNhapPanel(service, got::set);
        JPasswordField ma = (JPasswordField) find(panel, "dangnhap.ma");
        assertFalse(ma.isEnabled(), "vai trò Thành viên không cần mã xác nhận");
        SwingUtilities.invokeAndWait(() -> {
            ((JTextField) find(panel, "dangnhap.msv")).setText("B21DCCN001");
            ((JButton) find(panel, "dangnhap.login")).doClick();
        });
        waitFor(() -> got.get() != null);
        assertEquals(VaiTro.THANH_VIEN, got.get().vaiTro());
        assertEquals("B21DCCN001", got.get().msv());
    }

    @Test void banChuNhiemSaiMaBiTuChoiRoiDungMaThiVao() throws Exception {
        AtomicReference<NguoiDung> got = new AtomicReference<>();
        var panel = new DangNhapPanel(service, got::set);
        JPasswordField ma = (JPasswordField) find(panel, "dangnhap.ma");
        JLabel status = (JLabel) find(panel, "dangnhap.status");
        SwingUtilities.invokeAndWait(() -> {
            ((JRadioButton) find(panel, "dangnhap.banchunhiem")).doClick();
            ((JTextField) find(panel, "dangnhap.msv")).setText("B21DCCN001");
            ma.setText("sai");
            ((JButton) find(panel, "dangnhap.login")).doClick();
        });
        waitFor(() -> status.getText().contains("không đúng"));
        assertNull(got.get());
        SwingUtilities.invokeAndWait(() -> {
            ma.setText("MA-DEMO");
            ((JButton) find(panel, "dangnhap.login")).doClick();
        });
        waitFor(() -> got.get() != null);
        assertEquals(VaiTro.BAN_CHU_NHIEM, got.get().vaiTro());
    }

    @Test void msvKhongTonTaiHienThongBaoTrenManHinh() throws Exception {
        var panel = new DangNhapPanel(service, n -> fail("không được đăng nhập"));
        JLabel status = (JLabel) find(panel, "dangnhap.status");
        SwingUtilities.invokeAndWait(() -> {
            ((JTextField) find(panel, "dangnhap.msv")).setText("KHONG_CO");
            ((JButton) find(panel, "dangnhap.login")).doClick();
        });
        waitFor(() -> status.getText().contains("KHONG_CO"));
    }
}
