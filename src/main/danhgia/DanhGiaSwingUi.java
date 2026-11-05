package danhgia;

import common.Refreshable;
import common.SwingSupport;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Tra lịch sử theo thành viên, ghi quyết định và xem thống kê theo loại. */
public class DanhGiaSwingUi extends JPanel implements Refreshable {
    private final DanhGiaService service;
    private final JTextField msv=new JTextField(16);
    private final DefaultTableModel historyModel=SwingSupport.model("Mã ghi nhận","Loại","Hình thức","Lý do","Ngày quyết định","MSV");
    private final DefaultTableModel statsModel=SwingSupport.model("Loại đánh giá","Số đánh giá","Số thành viên");
    private final JTable historyTable=SwingSupport.table(historyModel,"danhgia.table");
    private final JTable statsTable=SwingSupport.table(statsModel,"danhgia.stats");
    private final JLabel status=new JLabel(" ");
    private final SwingSupport.Tasks tasks=new SwingSupport.Tasks(this,status);
    private List<DanhGia> history=new ArrayList<>();
    private boolean rendering;

    public DanhGiaSwingUi(DanhGiaService service){
        this.service=service;setName("danhgia.panel");setLayout(new BorderLayout(8,8));setBorder(BorderFactory.createEmptyBorder(12,14,12,14));
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT));bar.add(new JLabel("MSV:"));msv.setName("danhgia.msv");bar.add(msv);
        bar.add(SwingSupport.button("Xem lịch sử","danhgia.search",this::loadHistory));
        msv.addActionListener(e->loadHistory());bar.add(SwingSupport.button("Tải lại","danhgia.reload",this::resetAndReload));
        bar.add(SwingSupport.button("Thêm đánh giá","danhgia.add",()->edit(null)));
        bar.add(SwingSupport.button("Sửa đánh giá","danhgia.edit",()->edit(selected())));add(bar,BorderLayout.NORTH);
        JPanel stats=new JPanel(new BorderLayout(4,4));stats.add(new JLabel("THỐNG KÊ THEO LOẠI"),BorderLayout.NORTH);stats.add(SwingSupport.scroll(statsTable),BorderLayout.CENTER);
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,SwingSupport.scroll(historyTable),stats);split.setResizeWeight(.65);add(split,BorderLayout.CENTER);add(status,BorderLayout.SOUTH);reloadStats();
    }
    @Override public boolean isBusy(){return tasks.isBusy();}
    @Override public void resetAndReload(){if(!msv.getText().isBlank())loadHistory();reloadStats();}
    private void loadHistory(){String id=msv.getText().trim();if(id.isBlank())throw new IllegalArgumentException("Nhập MSV để tra cứu lịch sử.");tasks.run("Đang tải lịch sử đánh giá...",()->service.lichSu(id),list->{if(!id.equals(msv.getText().trim()))return;history=list;rendering=true;try{historyModel.setRowCount(0);for(DanhGia d:list)historyModel.addRow(new Object[]{d.maGhiNhan(),d.loaiDanhGia(),d.hinhThuc(),d.lyDo(),d.ngayQuyetDinh(),d.msv()});}finally{rendering=false;}status.setText(list.size()+" bản ghi đánh giá.");});}
    private void reloadStats(){tasks.run("Đang tải thống kê...",service::thongKe,list->{statsModel.setRowCount(0);for(ThongKeDanhGia s:list)statsModel.addRow(new Object[]{s.loaiDanhGia(),s.soDanhGia(),s.soThanhVien()});});}
    private DanhGia selected(){int r=historyTable.getSelectedRow();if(r<0)throw new IllegalArgumentException("Hãy chọn một đánh giá trong lịch sử.");return history.get(historyTable.convertRowIndexToModel(r));}
    private void edit(DanhGia current){boolean add=current==null;JTextField code=new JTextField(add?"":current.maGhiNhan());code.setEditable(add);JTextField member=new JTextField(add?msv.getText().trim():current.msv());JTextField type=new JTextField(add?"":current.loaiDanhGia());type.setToolTipText("Danh mục đang chờ thống nhất; ví dụ Khen thưởng, Kỷ luật.");JTextField formType=new JTextField(add?"":current.hinhThuc());JTextArea reason=new JTextArea(add?"":current.lyDo(),4,24);reason.setLineWrap(true);reason.setWrapStyleWord(true);JTextField date=new JTextField(add?LocalDate.now().toString():current.ngayQuyetDinh().toString());JPanel form=SwingSupport.form(new String[]{"Mã ghi nhận *","MSV *","Loại đánh giá *","Hình thức *","Lý do *","Ngày quyết định * (yyyy-MM-dd)"},code,member,type,formType,new JScrollPane(reason),date);SwingSupport.editor(this,add?"Thêm đánh giá":"Sửa đánh giá",form,()->new DanhGia(code.getText(),type.getText(),formType.getText(),reason.getText(),SwingSupport.date(date,"Ngày quyết định",true),member.getText()),d->{if(add)service.them(d);else service.sua(d);},()->{msv.setText(member.getText().trim());loadHistory();reloadStats();});}
}
