package muontra;

import common.DataAccessException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.SwingWorker;

/** Chạy việc nặng (truy cập MySQL) ở luồng nền để cửa sổ không bị đơ, rồi trả kết quả về luồng giao diện. */
final class ChayNen {
    private ChayNen() {
    }

    static <T> void chay(Supplier<T> viec, Consumer<T> khiXong, Consumer<String> khiLoi) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return viec.get();
            }

            @Override
            protected void done() {
                try {
                    khiXong.accept(get());
                } catch (ExecutionException e) {
                    khiLoi.accept(thongBao(e.getCause()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    static String thongBao(Throwable t) {
        if (t instanceof IllegalArgumentException || t instanceof DataAccessException) {
            return String.valueOf(t.getMessage());
        }
        return "Lỗi không xác định: " + t;
    }
}
