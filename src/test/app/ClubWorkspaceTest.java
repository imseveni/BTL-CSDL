package app;

import static org.junit.jupiter.api.Assertions.*;

import common.Refreshable;
import common.SwingModule;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

/** Không cần MySQL và không cần màn hình: chỉ dựng các thành phần Swing nhẹ, không mở cửa sổ. */
class ClubWorkspaceTest {

    private static class CountingPanel extends JPanel implements Refreshable {
        final AtomicInteger reloads = new AtomicInteger();
        @Override public boolean isBusy() { return false; }
        @Override public void resetAndReload() { reloads.incrementAndGet(); }
    }

    private static SwingModule module(String title, java.util.function.Supplier<JComponent> factory,
            AtomicInteger created) {
        return new SwingModule() {
            @Override public String getTitle() { return title; }
            @Override public JComponent createPanel() { created.incrementAndGet(); return factory.get(); }
        };
    }

    @Test void chiTaoTabKhiMoLanDauVaTuNapDuLieu() {
        AtomicInteger createdA = new AtomicInteger();
        AtomicInteger createdB = new AtomicInteger();
        CountingPanel a = new CountingPanel();
        CountingPanel b = new CountingPanel();
        var workspace = new ClubWorkspace(List.of(module("A", () -> a, createdA), module("B", () -> b, createdB)));
        // Tab đầu được tạo ngay và nạp dữ liệu một lần; tab thứ hai chưa tạo.
        assertEquals(1, createdA.get());
        assertEquals(1, a.reloads.get());
        assertEquals(0, createdB.get());
        assertNull(workspace.panelAt(1));
        workspace.showTab(1);
        assertEquals(1, createdB.get());
        assertEquals(1, b.reloads.get());
        // Mở lại tab đã tạo thì không tạo lại, không nạp lại.
        workspace.showTab(0);
        workspace.showTab(1);
        assertEquals(1, createdA.get());
        assertEquals(1, createdB.get());
        assertEquals(1, a.reloads.get());
        assertEquals(1, b.reloads.get());
    }

    @Test void moduleLoiKhongLamHongCuaSo() {
        AtomicInteger created = new AtomicInteger();
        var workspace = new ClubWorkspace(List.of(
                module("Tốt", JPanel::new, created),
                module("Lỗi", () -> { throw new IllegalStateException("hỏng khi dựng"); }, created)));
        workspace.showTab(1);
        assertNotNull(workspace.panelAt(1));
        assertTrue(workspace.panelAt(1) instanceof JPanel);
        // Panel lỗi vẫn là một thành phần hiển thị được, có chứa thông báo lỗi.
        JLabel label = (JLabel) ((JPanel) workspace.panelAt(1)).getComponent(0);
        assertTrue(label.getText().contains("hỏng khi dựng"));
    }

    @Test void tabKhongTonTaiBiTuChoi() {
        var workspace = new ClubWorkspace(List.of(module("A", JPanel::new, new AtomicInteger())));
        assertThrows(IllegalArgumentException.class, () -> workspace.showTab(5));
        assertThrows(IllegalArgumentException.class, () -> workspace.showTab(-1));
    }

    @Test void banChuNhiemCoBonTabVaThanhVienCoBaTabRieng() {
        List<String> bcn = SwingModules.choVaiTro(common.PhienLamViec.VaiTro.BAN_CHU_NHIEM).stream()
                .map(SwingModule::getTitle).toList();
        assertEquals(List.of("Thành viên", "Cơ sở vật chất", "Mượn – Trả", "Báo cáo Mượn – Trả"), bcn);
        assertEquals(bcn, SwingModules.all().stream().map(SwingModule::getTitle).toList());
        List<String> tv = SwingModules.choVaiTro(common.PhienLamViec.VaiTro.THANH_VIEN).stream()
                .map(SwingModule::getTitle).toList();
        assertEquals(List.of("Hồ sơ của tôi", "Cơ sở vật chất", "Mượn – Trả"), tv);
        assertFalse(tv.contains("Báo cáo Mượn – Trả"), "Thành viên không có tab báo cáo");
    }
}
