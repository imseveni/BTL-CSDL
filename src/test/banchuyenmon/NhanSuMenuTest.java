package banchuyenmon;

import static org.junit.jupiter.api.Assertions.*;
import common.DataAccessException;
import danhgia.DanhGia;
import danhgia.DanhGiaDao;
import danhgia.DanhGiaService;
import danhgia.DanhGiaUi;
import quyenhan.QuyenHanDao;
import quyenhan.QuyenHanService;
import quyenhan.QuyenHanUi;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

class NhanSuMenuTest {
    @Test void menusRecoverFromInvalidChoiceAndReturnWithoutDatabase() {
        String output = capture(() -> {
            new BanChuyenMonUi().run(new Scanner("abc\n99\n0\n"));
            new QuyenHanUi().run(new Scanner("0\n"));
            new DanhGiaUi().run(new Scanner("0\n"));
        });
        assertTrue(output.contains("Vui lòng nhập số"));
        assertTrue(output.contains("Lựa chọn không hợp lệ"));
        assertTrue(output.contains("QUYỀN HẠN"));
        assertTrue(output.contains("ĐÁNH GIÁ THÀNH VIÊN"));
    }

    @Test void deleteRequiresAffirmativeConfirmation() {
        int[] deletes = {0};
        var dao = new QuyenHanDao() {
            @Override public void xoa(String code) { deletes[0]++; }
        };
        capture(() -> new QuyenHanUi(new QuyenHanService(dao)).run(new Scanner("4\nQ1\nn\n4\nQ1\ny\n0\n")));
        assertEquals(1, deletes[0]);
    }

    @Test void databaseFailureIsShownAndMenuContinues() {
        var dao = new BanChuyenMonDao() {
            @Override public void xoa(String code) {
                throw new DataAccessException("Không thể xóa ban còn thành viên", null);
            }
        };
        String output = capture(() -> new BanChuyenMonUi(new BanChuyenMonService(dao)).run(new Scanner("4\nB1\ny\n0\n")));
        assertTrue(output.contains("Không thể xóa ban còn thành viên"));
    }

    @Test void impossibleDateIsRejectedBeforePersistence() {
        int[] writes = {0};
        var dao = new DanhGiaDao() {
            @Override public void them(DanhGia value) { writes[0]++; }
        };
        String output = capture(() -> new DanhGiaUi(new DanhGiaService(dao)).run(
                new Scanner("1\nD1\nTV1\nKhen thưởng\nGiấy khen\nĐóng góp\n2026-02-30\n0\n")));
        assertEquals(0, writes[0]);
        assertTrue(output.contains("Ngày không hợp lệ"));
    }

    @Test void endOfInputDuringOperationReturnsCleanly() {
        assertDoesNotThrow(() -> capture(() -> new BanChuyenMonUi().run(new Scanner("2\n"))));
        assertDoesNotThrow(() -> capture(() -> new QuyenHanUi().run(new Scanner("5\n"))));
        assertDoesNotThrow(() -> capture(() -> new DanhGiaUi().run(new Scanner("1\n"))));
    }

    private String capture(Runnable action) {
        PrintStream old = System.out;
        var bytes = new ByteArrayOutputStream();
        try (var out = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            System.setOut(out);
            action.run();
        } finally {
            System.setOut(old);
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }
}
