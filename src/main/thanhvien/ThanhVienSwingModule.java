package thanhvien;

import common.SwingModule;
import quyenhan.QuyenHanService;

import javax.swing.JComponent;

/** Module 01 trong cửa sổ Swing. Người phụ trách: Tú. */
public class ThanhVienSwingModule implements SwingModule {

    @Override
    public String getTitle() {
        return "Thành viên";
    }

    @Override
    public JComponent createPanel() {
        return new ThanhVienWorkspace(new ThanhVienService(), new QuyenHanService());
    }
}
