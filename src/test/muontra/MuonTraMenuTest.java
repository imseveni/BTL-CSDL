package muontra;

import static org.junit.jupiter.api.Assertions.*;
import common.DataAccessException;
import cosovatchat.CoSoVatChatDao;
import cosovatchat.CoSoVatChatService;
import cosovatchat.CoSoVatChatUi;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

class MuonTraMenuTest {
    @Test void menusRecoverFromInvalidChoiceAndReturnWithoutDatabase() {
        String output = capture(() -> {
            new PhieuMuonUi().run(new Scanner("abc\n99\n0\n"));
            new CoSoVatChatUi().run(new Scanner("abc\n99\n0\n"));
        });
        assertTrue(output.contains("MƯỢN - TRẢ THIẾT BỊ"));
        assertTrue(output.contains("CƠ SỞ VẬT CHẤT"));
        assertTrue(output.contains("Vui lòng nhập số"));
        assertTrue(output.contains("Lựa chọn không hợp lệ"));
    }

    @Test void endOfInputDuringOperationReturnsCleanly() {
        assertDoesNotThrow(() -> capture(() -> new PhieuMuonUi().run(new Scanner("1\n"))));
        assertDoesNotThrow(() -> capture(() -> new CoSoVatChatUi().run(new Scanner("3\n"))));
    }

    @Test void impossibleDateIsRejectedBeforePersistence() {
        int[] writes = {0};
        var dao = new PhieuMuonDao() {
            @Override public void taoPhieu(String ma, LocalDate muon, LocalDate hen, String mucDich, String msv) { writes[0]++; }
        };
        String output = capture(() -> new PhieuMuonUi(new PhieuMuonService(dao)).run(
                new Scanner("1\nPM1\nTV1\n2026-02-30\n0\n")));
        assertEquals(0, writes[0]);
        assertTrue(output.contains("Ngày không hợp lệ"));
    }

    @Test void returnDateDefaultsToTodayWhenLeftBlank() {
        LocalDate[] seen = {null};
        var dao = new PhieuMuonDao() {
            @Override public void traThietBi(String phieu, String thietBi, LocalDate ngay) { seen[0] = ngay; }
        };
        capture(() -> new PhieuMuonUi(new PhieuMuonService(dao)).run(new Scanner("7\nPM1\nTB1\n\n0\n")));
        assertEquals(LocalDate.now(), seen[0]);
    }

    @Test void approvingAndRejectingRequireAffirmativeConfirmation() {
        int[] approved = {0};
        int[] rejected = {0};
        var dao = new PhieuMuonDao() {
            @Override public void duyet(String phieu) { approved[0]++; }
            @Override public void tuChoi(String phieu) { rejected[0]++; }
        };
        capture(() -> new PhieuMuonUi(new PhieuMuonService(dao)).run(
                new Scanner("5\nPM1\nn\n5\nPM1\ny\n6\nPM2\nn\n6\nPM2\ny\n0\n")));
        assertEquals(1, approved[0]);
        assertEquals(1, rejected[0]);
    }

    @Test void databaseFailureIsShownAndMenuContinues() {
        var dao = new PhieuMuonDao() {
            @Override public void duyet(String phieu) {
                throw new DataAccessException("Số lượng mượn vượt quá số lượng khả dụng, không thể duyệt", null);
            }
        };
        String output = capture(() -> new PhieuMuonUi(new PhieuMuonService(dao)).run(new Scanner("5\nPM1\ny\n0\n")));
        assertTrue(output.contains("vượt quá số lượng khả dụng"));
    }

    @Test void addingSeveralDevicesContinuesAfterOneFailure() {
        int[] added = {0};
        var dao = new PhieuMuonDao() {
            @Override public void themChiTiet(String phieu, String thietBi, int soLuong) {
                if (thietBi.equals("TB_HET")) throw new DataAccessException("Số lượng mượn vượt quá số lượng khả dụng", null);
                added[0]++;
            }
        };
        String output = capture(() -> new PhieuMuonUi(new PhieuMuonService(dao)).run(
                new Scanner("2\nPM1\nTB1\n1\nTB_HET\n9\nTB2\n2\n\n0\n")));
        assertEquals(2, added[0]);
        assertTrue(output.contains("vượt quá số lượng khả dụng"));
    }

    @Test void nonNumericQuantityIsReportedWithoutCrash() {
        int[] writes = {0};
        var dao = new CoSoVatChatDao() {
            @Override public void themMoi(String ma, String ten, int tong, Integer khaDung, String msv) { writes[0]++; }
        };
        String output = capture(() -> new CoSoVatChatUi(new CoSoVatChatService(dao)).run(
                new Scanner("3\nTB1\nLoa\nabc\n0\n")));
        assertEquals(0, writes[0]);
        assertTrue(output.contains("không phải số nguyên"));
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
