package baocaomuontra;

import java.util.List;

/**
 * Các chỉ số thống kê tổng quan của báo cáo mượn – trả cơ sở vật chất (mục 2.2).
 *
 * @param tongSoLuotMuon      Tổng số lượt mượn (số dòng giao dịch mượn thiết bị)
 * @param tongSoLuotTra       Tổng số lượt trả (số dòng giao dịch đã trả)
 * @param soCSVCDangMuon      Tổng số lượng cơ sở vật chất đang được mượn (chưa trả)
 * @param soCSVCDaTra         Tổng số lượng cơ sở vật chất đã trả
 * @param soTruongHopQuaHan   Số trường hợp trả quá hạn (trả muộn hoặc quá hạn chưa trả)
 * @param soTruongHopChuaTra  Số trường hợp chưa trả
 */
public record ThongKeMuonTra(
        int tongSoLuotMuon,
        int tongSoLuotTra,
        int soCSVCDangMuon,
        int soCSVCDaTra,
        int soTruongHopQuaHan,
        int soTruongHopChuaTra) {

    public static final ThongKeMuonTra TRONG = new ThongKeMuonTra(0, 0, 0, 0, 0, 0);

    /**
     * Tính toán các chỉ số thống kê tổng quan từ danh sách chi tiết giao dịch mượn – trả.
     */
    public static ThongKeMuonTra tuDanhSach(List<BaoCaoMuonTraItem> danhSach) {
        if (danhSach == null || danhSach.isEmpty()) {
            return TRONG;
        }
        int tongLuotMuon = danhSach.size();
        int tongLuotTra = 0;
        int soCSVCDangMuon = 0;
        int soCSVCDaTra = 0;
        int soQuaHan = 0;
        int soChuaTra = 0;

        for (BaoCaoMuonTraItem item : danhSach) {
            if (item.daTra()) {
                tongLuotTra++;
                soCSVCDaTra += item.soLuongMuon();
            } else {
                soChuaTra++;
                soCSVCDangMuon += item.soLuongMuon();
            }
            if (item.laQuaHan()) {
                soQuaHan++;
            }
        }

        return new ThongKeMuonTra(
                tongLuotMuon,
                tongLuotTra,
                soCSVCDangMuon,
                soCSVCDaTra,
                soQuaHan,
                soChuaTra);
    }

    public int tongSoLuongMuon() {
        return soCSVCDangMuon + soCSVCDaTra;
    }
}

