package app;

import common.AppModule;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Mục menu mở giao diện đồ họa (Swing) của Khóa học hoặc Dự án từ menu chính dạng console.
 * Cửa sổ chạy trên luồng giao diện; menu console chờ đến khi cửa sổ được đóng rồi mới quay lại.
 */
public class SwingLauncherModule implements AppModule {

    private final String name;
    private final int tab;

    /** @param tab tab mở sẵn: 0 = Khóa học, 1 = Dự án */
    public SwingLauncherModule(String name, int tab) {
        this.name = name;
        this.tab = tab;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void run(Scanner in) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Máy này không có màn hình đồ họa nên không mở được cửa sổ. "
                    + "Hãy chạy trên máy có giao diện.");
            return;
        }
        CountDownLatch closed = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = SwingApp.createFrame(JFrame.DISPOSE_ON_CLOSE, tab);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        closed.countDown();
                    }
                });
                frame.setVisible(true);
            } catch (RuntimeException e) {
                System.out.println("Không mở được cửa sổ: " + e.getMessage());
                closed.countDown();
            }
        });
        System.out.println("Đã mở cửa sổ đồ họa. Đóng cửa sổ để quay lại menu.");
        try {
            closed.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
