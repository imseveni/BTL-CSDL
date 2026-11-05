package swing;

import static org.junit.jupiter.api.Assertions.fail;
import java.awt.Component;
import java.awt.Container;
import java.util.function.BooleanSupplier;
import javax.swing.SwingUtilities;

public final class SwingUiTestSupport {
    private SwingUiTestSupport() { }
    public static Component find(Component root, String name) {
        if (name.equals(root.getName())) return root;
        if (root instanceof Container container)
            for (Component child : container.getComponents()) {
                Component found = find(child, name);
                if (found != null) return found;
            }
        return null;
    }
    public static void waitFor(BooleanSupplier condition) throws Exception {
        for (int i=0;i<200;i++) {
            SwingUtilities.invokeAndWait(() -> { });
            if (condition.getAsBoolean()) return;
            Thread.sleep(20);
        }
        fail("Hết thời gian chờ giao diện Swing cập nhật.");
    }
}
