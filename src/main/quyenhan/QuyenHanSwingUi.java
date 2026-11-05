package quyenhan;

import common.Refreshable;
import common.SwingSupport;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Danh sách quyền và danh sách thành viên được cấp quyền đang chọn. */
public class QuyenHanSwingUi extends JPanel implements Refreshable {
    private final QuyenHanService service;
    private final DefaultTableModel permissionModel=SwingSupport.model("Mã quyền","Tên quyền","Mô tả");
    private final DefaultTableModel memberModel=SwingSupport.model("MSV","Họ và tên","Mã ban","Chức vụ");
    private final JTable permissionTable=SwingSupport.table(permissionModel,"quyenhan.table");
    private final JTable memberTable=SwingSupport.table(memberModel,"quyenhan.members");
    private final JTextField msv=new JTextField(12);
    private final JLabel title=new JLabel("Thành viên có quyền — chọn một quyền");
    private final JLabel status=new JLabel(" ");
    private final SwingSupport.Tasks tasks=new SwingSupport.Tasks(this,status);
    private List<QuyenHan> permissions=new ArrayList<>();
    private List<ThanhVienCoQuyen> members=new ArrayList<>();
    private boolean rendering;

    public QuyenHanSwingUi(QuyenHanService service){
        this.service=service;setName("quyenhan.panel");setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(12,14,12,14));
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(SwingSupport.button("Tải lại","quyenhan.reload",this::reload));
        bar.add(SwingSupport.button("Thêm quyền","quyenhan.add",()->edit(null)));
        bar.add(SwingSupport.button("Sửa quyền","quyenhan.edit",()->edit(selectedPermission())));
        bar.add(SwingSupport.button("Xóa quyền","quyenhan.delete",this::delete));
        bar.add(new JLabel("MSV:"));msv.setName("quyenhan.msv");bar.add(msv);
        bar.add(SwingSupport.button("Cấp quyền","quyenhan.grant",this::grant));
        bar.add(SwingSupport.button("Thu hồi","quyenhan.revoke",this::revoke));
        bar.add(SwingSupport.button("Quyền của MSV","quyenhan.member.lookup",this::lookup));
        add(bar,BorderLayout.NORTH);JPanel detail=new JPanel(new BorderLayout(5,5));detail.add(title,BorderLayout.NORTH);detail.add(SwingSupport.scroll(memberTable),BorderLayout.CENTER);
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,SwingSupport.scroll(permissionTable),detail);split.setResizeWeight(.48);add(split,BorderLayout.CENTER);add(status,BorderLayout.SOUTH);
        permissionTable.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&!rendering&&!tasks.isBusy())loadMembers();});reload();
    }
    @Override public boolean isBusy(){return tasks.isBusy();}
    @Override public void resetAndReload(){msv.setText("");reload();}
    private void reload(){String keep=selectedCode();tasks.run("Đang tải quyền...",service::danhSach,list->{permissions=list;rendering=true;try{permissionModel.setRowCount(0);for(QuyenHan q:list)permissionModel.addRow(new Object[]{q.maQuyenHan(),q.tenQuyenHan(),SwingSupport.text(q.moTaChiTiet())});select(keep);}finally{rendering=false;}loadMembers();});}
    private void select(String code){if(permissions.isEmpty())return;int i=0;for(int n=0;n<permissions.size();n++)if(permissions.get(n).maQuyenHan().equals(code)){i=n;break;}permissionTable.setRowSelectionInterval(i,i);}
    private void loadMembers(){String code=selectedCode();memberModel.setRowCount(0);if(code==null){title.setText("Thành viên có quyền — chọn một quyền");return;}title.setText("Thành viên được cấp quyền "+code);tasks.run("Đang tải thành viên...",()->service.thanhVienCoQuyen(code),list->{if(!code.equals(selectedCode()))return;members=list;for(ThanhVienCoQuyen tv:list)memberModel.addRow(new Object[]{tv.msv(),tv.hoVaTen(),tv.maBan(),SwingSupport.text(tv.chucVu())});status.setText(list.size()+" thành viên được cấp quyền.");});}
    private String selectedCode(){int r=permissionTable.getSelectedRow();return r<0?null:permissions.get(permissionTable.convertRowIndexToModel(r)).maQuyenHan();}
    private QuyenHan selectedPermission(){String code=selectedCode();if(code==null)throw new IllegalArgumentException("Hãy chọn một quyền.");return permissions.stream().filter(q->q.maQuyenHan().equals(code)).findFirst().orElseThrow();}
    private void edit(QuyenHan current){boolean add=current==null;JTextField code=new JTextField(add?"":current.maQuyenHan());code.setEditable(add);JTextField name=new JTextField(add?"":current.tenQuyenHan());JTextArea desc=new JTextArea(add||current.moTaChiTiet()==null?"":current.moTaChiTiet(),4,24);desc.setLineWrap(true);desc.setWrapStyleWord(true);JPanel form=SwingSupport.form(new String[]{"Mã quyền *","Tên quyền *","Mô tả"},code,name,new JScrollPane(desc));SwingSupport.editor(this,add?"Thêm quyền":"Sửa quyền","Xóa quyền sẽ thu hồi quyền này khỏi mọi thành viên.","Lưu",form,()->new QuyenHan(code.getText(),name.getText(),desc.getText()),q->{if(add)service.them(q);else service.sua(q);},this::reload);}
    private void delete(){QuyenHan q=selectedPermission();if(SwingSupport.confirm(this,"Xóa quyền "+q.maQuyenHan()+" và thu hồi khỏi tất cả thành viên?"))tasks.run("Đang xóa quyền...",()->{service.xoa(q.maQuyenHan());return true;},x->reload());}
    private void grant(){String q=selectedPermission().maQuyenHan();tasks.run("Đang cấp quyền...",()->{service.capQuyen(msv.getText(),q);return true;},x->{status.setText("Đã cấp quyền "+q+".");loadMembers();});}
    private void revoke(){String q=selectedPermission().maQuyenHan();tasks.run("Đang thu hồi quyền...",()->{service.thuHoiQuyen(msv.getText(),q);return true;},x->{status.setText("Đã thu hồi quyền "+q+".");loadMembers();});}
    private void lookup(){String id=msv.getText().trim();tasks.run("Đang tra cứu quyền...",()->service.quyenCuaThanhVien(id),list->{String result=list.isEmpty()?"Thành viên chưa được cấp quyền nào.":String.join("\n",list.stream().map(q->q.maQuyenHan()+" · "+q.tenQuyenHan()).toList());JOptionPane.showMessageDialog(this,result,"Quyền của "+id,JOptionPane.INFORMATION_MESSAGE);});}
}
