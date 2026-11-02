package common;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Màn hình tạm cho module chưa có giao diện Swing. Người phụ trách thay bằng panel thật trong
 * {@code createPanel()} của lớp {@code XxxSwingModule} của mình.
 */
public class PlaceholderPanel extends JPanel {

    public PlaceholderPanel(String title, String owner, String fileToEdit) {
        super(new GridBagLayout());
        setName("placeholder.panel");
        setBackground(SwingSupport.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        JLabel heading = new JLabel(title, SwingConstants.CENTER);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setForeground(SwingSupport.ACCENT);
        JLabel info = new JLabel("<html><div style='text-align:center'>Giao diện Swing của module này đang được phát triển.<br>"
                + "Người phụ trách: <b>" + owner + "</b><br><br>"
                + "<span style='color:#666666'>Sửa file: " + fileToEdit + "</span></div></html>", SwingConstants.CENTER);
        info.setForeground(new Color(70, 70, 70));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new java.awt.Insets(0, 0, 14, 0);
        add(heading, c);
        c.gridy = 1;
        c.insets = new java.awt.Insets(0, 0, 0, 0);
        add(info, c);
    }
}
