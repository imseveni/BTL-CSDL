package app;

import baocaomuontra.BaoCaoMuonTraSwingModule;
import common.PhienLamViec.VaiTro;
import common.SwingModule;
import cosovatchat.CoSoVatChatSwingModule;
import muontra.MuonTraSwingModule;
import thanhvien.HoSoCaNhanSwingModule;
import thanhvien.ThanhVienSwingModule;

import java.util.List;

/**
 * Các tab của cửa sổ chính theo vai trò (xem docs/PHAN_QUYEN.md). Bản tích hợp chỉ giữ giao diện Swing của
 * Thành viên, Cơ sở vật chất, Mượn – Trả và Báo cáo Mượn – Trả; các module khác vẫn còn mã và SQL nhưng
 * không có tab. Chỉ Tú sửa file này.
 */
public final class SwingModules {

    private SwingModules() {
    }

    /** Ban Chủ nhiệm / Người quản lý: đầy đủ bốn tab. */
    public static List<SwingModule> all() {
        return choVaiTro(VaiTro.BAN_CHU_NHIEM);
    }

    public static List<SwingModule> choVaiTro(VaiTro vaiTro) {
        if (vaiTro == VaiTro.THANH_VIEN) {
            // Thành viên: hồ sơ của mình, tra cứu thiết bị, phiếu mượn của mình. Các panel tự ẩn nút quản lý.
            return List.of(new HoSoCaNhanSwingModule(), new CoSoVatChatSwingModule(), new MuonTraSwingModule());
        }
        return List.of(new ThanhVienSwingModule(), new CoSoVatChatSwingModule(), new MuonTraSwingModule(),
                new BaoCaoMuonTraSwingModule());
    }
}
