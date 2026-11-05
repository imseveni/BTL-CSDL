package banchuyenmon;

import common.Refreshable;
import common.SwingSupport;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Bảng ban và bảng thành viên của ban đang chọn. */
public class BanChuyenMonSwingUi extends JPanel implements Refreshable {
    private final BanChuyenMonService service;
    private final DefaultTableModel banModel = SwingSupport.model("Mã ban", "Tên ban", "Số thành viên");
    private final DefaultTableModel tvModel = SwingSupport.model("MSV", "Họ và tên", "Chức vụ", "Trạng thái");
    private final JTable banTable = SwingSupport.table(banModel, "banchuyenmon.table");
    private final JTable tvTable = SwingSupport.table(tvModel, "banchuyenmon.members");
    private final JLabel title = new JLabel("Thành viên — chọn một ban");
    private final JLabel status = new JLabel(" ");
    private final SwingSupport.Tasks tasks = new SwingSupport.Tasks(this, status);
    private List<BanThongKe> bans = new ArrayList<>();
    private List<ThanhVienTrongBan> members = new ArrayList<>();
    private boolean rendering;
    private record BanOption(String maBan, String tenBan) {
        @Override public String toString() { return maBan + " – " + tenBan; }
    }

    public BanChuyenMonSwingUi(BanChuyenMonService service) {
        this.service = service;
        setName("banchuyenmon.panel"); setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(SwingSupport.button("Tải lại", "banchuyenmon.reload", this::reload));
        bar.add(SwingSupport.button("Thêm ban", "banchuyenmon.add", () -> editBan(null)));
        bar.add(SwingSupport.button("Sửa tên", "banchuyenmon.edit", () -> editBan(selectedBan())));
        bar.add(SwingSupport.button("Xóa ban", "banchuyenmon.delete", this::deleteBan));
        bar.add(SwingSupport.button("Chuyển ban", "banchuyenmon.move", this::moveMember));
        bar.add(SwingSupport.button("Đổi chức vụ", "banchuyenmon.position", this::changePosition));
        add(bar, BorderLayout.NORTH);
        JPanel detail = new JPanel(new BorderLayout(5, 5)); detail.add(title, BorderLayout.NORTH);
        detail.add(SwingSupport.scroll(tvTable), BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, SwingSupport.scroll(banTable), detail);
        split.setResizeWeight(.48); add(split, BorderLayout.CENTER); add(status, BorderLayout.SOUTH);
        banTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !rendering && !tasks.isBusy()) loadMembers();
        });
        reload();
    }
    @Override public boolean isBusy() { return tasks.isBusy(); }
    @Override public void resetAndReload() { reload(); }
    private void reload() {
        String keep = selectedCode();
        tasks.run("Đang tải ban...", service::danhSach, list -> {
            bans = list; rendering = true;
            try {
                banModel.setRowCount(0);
                for (BanThongKe b : bans) banModel.addRow(new Object[]{b.maBan(), b.tenBan(), b.soThanhVien()});
                selectBan(keep);
            } finally { rendering = false; }
            loadMembers();
        });
    }
    private void selectBan(String code) {
        if (bans.isEmpty()) return;
        int index = 0;
        for (int i=0;i<bans.size();i++) if (bans.get(i).maBan().equals(code)) { index=i; break; }
        banTable.setRowSelectionInterval(index,index);
    }
    private void loadMembers() {
        String code = selectedCode(); tvModel.setRowCount(0);
        if (code == null) { title.setText("Thành viên — chọn một ban"); return; }
        title.setText("Thành viên của ban " + code);
        tasks.run("Đang tải thành viên...", () -> service.thanhVienTheoBan(code), list -> {
            if (!code.equals(selectedCode())) return;
            members = list;
            for (ThanhVienTrongBan tv : list) tvModel.addRow(new Object[]{tv.msv(),tv.hoVaTen(),
                    SwingSupport.text(tv.chucVu()),SwingSupport.text(tv.trangThaiHD())});
            status.setText(list.size()+" thành viên trong ban.");
        });
    }
    private String selectedCode() { int r=banTable.getSelectedRow(); return r<0?null:bans.get(banTable.convertRowIndexToModel(r)).maBan(); }
    private BanThongKe selectedBan() { String code=selectedCode(); if(code==null) throw new IllegalArgumentException("Hãy chọn một ban."); return bans.stream().filter(b->b.maBan().equals(code)).findFirst().orElseThrow(); }
    private ThanhVienTrongBan selectedMember() { int r=tvTable.getSelectedRow(); if(r<0) throw new IllegalArgumentException("Hãy chọn một thành viên."); return members.get(tvTable.convertRowIndexToModel(r)); }
    private void editBan(BanThongKe current) {
        boolean add=current==null; JTextField code=new JTextField(add?"":current.maBan()); code.setEditable(add);
        JTextField name=new JTextField(add?"":current.tenBan());
        JPanel form=SwingSupport.form(new String[]{"Mã ban *","Tên ban *"},code,name);
        SwingSupport.editor(this,add?"Thêm ban":"Sửa tên ban",form,
                ()->new BanChuyenMon(code.getText(),name.getText()),
                value->{if(add)service.them(value);else service.sua(value);},this::reload);
    }
    private void deleteBan() { BanThongKe b=selectedBan();
        if(SwingSupport.confirm(this,"Xóa ban "+b.maBan()+"? Chỉ xóa được khi ban không còn thành viên."))
            tasks.run("Đang xóa ban...",()->{service.xoa(b.maBan());return true;},x->reload());
    }
    private void moveMember() {
        ThanhVienTrongBan tv=selectedMember(); if(bans.size()<2) throw new IllegalArgumentException("Cần có ít nhất hai ban để chuyển thành viên.");
        JComboBox<BanOption> target=new JComboBox<>(bans.stream().filter(b->!b.maBan().equals(selectedCode()))
                .map(b->new BanOption(b.maBan(),b.tenBan())).toArray(BanOption[]::new));
        JPanel form=SwingSupport.form(new String[]{"Thành viên","Ban mới *"},new JLabel(tv.msv()+" · "+tv.hoVaTen()),target);
        SwingSupport.editor(this,"Chuyển ban","Chức vụ hiện tại được giữ nguyên.","Chuyển",form,
                ()->(BanOption)target.getSelectedItem(),b->service.chuyenBan(tv.msv(),b.maBan()),this::reload);
    }
    private void changePosition() {
        ThanhVienTrongBan tv=selectedMember(); JTextField position=new JTextField(tv.chucVu()==null?"":tv.chucVu());
        JPanel form=SwingSupport.form(new String[]{"Thành viên","Chức vụ mới *"},new JLabel(tv.msv()+" · "+tv.hoVaTen()),position);
        SwingSupport.editor(this,"Đổi chức vụ",form,()->position.getText(),v->service.doiChucVu(tv.msv(),v),this::reload);
    }
}
