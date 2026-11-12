package thanhvien;

import common.SwingModule;
import javax.swing.JComponent;
import quyenhan.QuyenHanService;

/** Tab dành cho vai trò Thành viên: xem và cập nhật hồ sơ của chính mình. */
public class HoSoCaNhanSwingModule implements SwingModule {
    @Override
    public String getTitle() {
        return "Hồ sơ của tôi";
    }

    @Override
    public JComponent createPanel() {
        return new HoSoCaNhanPanel(new ThanhVienService(), new QuyenHanService());
    }
}
