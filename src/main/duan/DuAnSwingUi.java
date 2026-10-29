package duan;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import common.SwingSupport;

public class DuAnSwingUi extends JPanel {
    private final DuAnService service;
    private final JTextField keyword = new JTextField(24);
    private final DefaultTableModel projects = SwingSupport.model("Mã dự án", "Tên dự án", "Mục tiêu", "Bắt đầu",
            "Hạn hoàn thành");
    private final DefaultTableModel members = SwingSupport.model("MSV", "Vai trò", "Tiến độ riêng (%)");
    private final JTable projectTable = SwingSupport.table(projects, "duan.table");
    private final JTable memberTable = SwingSupport.table(members, "duan.members");
    private final JLabel memberTitle = new JLabel("Thành viên tham gia — chọn một dự án");
    private final JLabel status = new JLabel("Nhấn Tải lại để đọc danh sách từ MySQL.");
    private final JProgressBar progress = new JProgressBar(0, 100);
    private final SwingSupport.Tasks tasks = new SwingSupport.Tasks(this, status);
    private List<DuAn> projectRows = new ArrayList<>();
    private List<ThamGiaDuAn> memberRows = new ArrayList<>();
    private boolean rendering;

    public DuAnSwingUi(DuAnService service) {
        this.service = service;
        setName("duan.panel");
        setLayout(new BorderLayout(10, 10));
        setBackground(SwingSupport.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        keyword.setName("duan.keyword");
        keyword.setToolTipText("Tìm chuỗi con trong mã, tên hoặc mục tiêu. Trống = tất cả.");
        keyword.addActionListener(event -> reload(null));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        toolbar.add(new JLabel("Mã / tên / mục tiêu:"));
        toolbar.add(keyword);
        toolbar.add(SwingSupport.button("Tìm kiếm", "duan.search", () -> reload(null)));
        toolbar.add(SwingSupport.button("Tải lại", "duan.reload", () -> reload(selectedId())));
        toolbar.add(SwingSupport.button("Thêm dự án", "duan.add", () -> editProject(null)));
        add(toolbar, BorderLayout.NORTH);

        JPanel projectSection = new JPanel(new BorderLayout(6, 6));
        JPanel projectActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        JLabel title = new JLabel("DANH SÁCH DỰ ÁN");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        projectActions.add(title);
        projectActions.add(SwingSupport.button("Sửa thông tin", "duan.edit", () -> editProject(selectedProject())));
        projectActions.add(SwingSupport.button("Xóa dự án", "duan.delete", this::deleteProject));
        projectSection.add(projectActions, BorderLayout.NORTH);
        projectSection.add(SwingSupport.scroll(projectTable), BorderLayout.CENTER);

        JPanel memberSection = new JPanel(new BorderLayout(6, 6));
        JPanel memberHeader = new JPanel(new BorderLayout(8, 8));
        memberTitle.setFont(memberTitle.getFont().deriveFont(Font.BOLD));
        memberHeader.add(memberTitle, BorderLayout.CENTER);
        progress.setName("duan.progress");
        progress.setStringPainted(true);
        progress.setString("Tiến độ chung: chưa có");
        progress.setPreferredSize(new java.awt.Dimension(270, 27));
        progress.setToolTipText("AVG(TienDoRieng); bỏ qua các giá trị chưa có.");
        memberHeader.add(progress, BorderLayout.EAST);
        memberSection.add(memberHeader, BorderLayout.NORTH);
        memberSection.add(SwingSupport.scroll(memberTable), BorderLayout.CENTER);
        JPanel memberActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        memberActions.add(SwingSupport.button("Thêm thành viên", "duan.member.add", () -> editMember(null)));
        memberActions.add(
                SwingSupport.button("Sửa vai trò / tiến độ", "duan.member.edit", () -> editMember(selectedMember())));
        memberActions.add(SwingSupport.button("Xóa lượt tham gia", "duan.member.delete", this::deleteMember));
        memberSection.add(memberActions, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, projectSection, memberSection);
        split.setResizeWeight(0.57);
        split.setDividerLocation(290);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
        status.setName("duan.status");
        add(status, BorderLayout.SOUTH);
        projectTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && !rendering && !tasks.isBusy())
                loadMembers();
        });
    }

    public boolean isBusy() {
        return tasks.isBusy();
    }

    public void reload() {
        reload(selectedId());
    }

    public void resetAndReload() {
        keyword.setText("");
        rendering = true;
        try {
            projectRows = List.of();
            memberRows = List.of();
            projects.setRowCount(0);
            members.setRowCount(0);
        } finally {
            rendering = false;
        }
        memberTitle.setText("Thành viên tham gia — chọn một dự án");
        progress.setValue(0);
        progress.setString("Tiến độ chung: chưa có");
        reload(null);
    }

    private void reload(String keepId) {
        String query = keyword.getText();
        tasks.run("Đang tải dự án...", () -> service.search(query), rows -> {
            rendering = true;
            try {
                projectRows = rows;
                projects.setRowCount(0);
                for (DuAn row : rows)
                    projects.addRow(new Object[] { row.getMaDuAn(), row.getTenDuAn(),
                            SwingSupport.text(row.getMucTieu()), SwingSupport.text(row.getNgayBatDau()),
                            SwingSupport.text(row.getHanHoanThanh()) });
                int index = 0;
                for (int i = 0; i < rows.size(); i++) {
                    if (rows.get(i).getMaDuAn().equals(keepId)) {
                        index = i;
                        break;
                    }
                }
                if (!rows.isEmpty()) {
                    int view = projectTable.convertRowIndexToView(index);
                    projectTable.setRowSelectionInterval(view, view);
                }
            } finally {
                rendering = false;
            }
            loadMembers();
        });
    }

    private void loadMembers() {
        members.setRowCount(0);
        memberRows = List.of();
        progress.setValue(0);
        progress.setString("Tiến độ chung: chưa có");
        if (projectTable.getSelectedRow() < 0) {
            memberTitle.setText("Thành viên tham gia — chọn một dự án");
            status.setText("Tìm thấy " + projectRows.size() + " dự án.");
            return;
        }
        DuAn project = selectedProject();
        memberTitle.setText("Thành viên — " + project.getMaDuAn() + " · " + project.getTenDuAn());
        tasks.run("Đang tải người tham gia và tiến độ...", () -> new Participation(
                service.participants(project.getMaDuAn()), service.averageProgress(project.getMaDuAn())), data -> {
                    if (!project.getMaDuAn().equals(selectedId())) {
                        loadMembers();
                        return;
                    }
                    memberRows = data.rows();
                    for (ThamGiaDuAn row : data.rows())
                        members.addRow(new Object[] { row.getMsv(),
                                SwingSupport.text(row.getVaiTro()),
                                row.getTienDoRieng() == null ? "Chưa có" : row.getTienDoRieng().toPlainString() });
                    if (data.average() != null) {
                        progress.setValue(data.average().setScale(0, RoundingMode.HALF_UP).intValue());
                        progress.setString("Tiến độ chung: "
                                + data.average().setScale(2, RoundingMode.HALF_UP).toPlainString() + "%");
                    }
                    status.setText(
                            projectRows.size() + " dự án  |  " + data.rows().size() + " người trong dự án đang chọn.");
                });
    }

    private record Participation(List<ThamGiaDuAn> rows, BigDecimal average) {
    }

    private String selectedId() {
        return projectTable.getSelectedRow() < 0 ? null : selectedProject().getMaDuAn();
    }

    private DuAn selectedProject() {
        int view = projectTable.getSelectedRow();
        if (view < 0)
            throw new IllegalArgumentException("Hãy chọn một dự án trong bảng.");
        return projectRows.get(projectTable.convertRowIndexToModel(view));
    }

    private ThamGiaDuAn selectedMember() {
        selectedProject();
        int view = memberTable.getSelectedRow();
        if (view < 0)
            throw new IllegalArgumentException("Hãy chọn một người tham gia trong bảng phía dưới.");
        return memberRows.get(memberTable.convertRowIndexToModel(view));
    }

    private void editProject(DuAn current) {
        JTextField id = new JTextField(current == null ? "" : current.getMaDuAn(), 28);
        id.setEditable(current == null);
        JTextField name = new JTextField(current == null ? "" : current.getTenDuAn());
        JTextArea objective = new JTextArea(current == null || current.getMucTieu() == null ? "" : current.getMucTieu(),
                4, 28);
        objective.setLineWrap(true);
        objective.setWrapStyleWord(true);
        JTextField start = new JTextField(current == null ? "" : current.getNgayBatDau().toString());
        JTextField end = new JTextField(
                current == null || current.getHanHoanThanh() == null ? "" : current.getHanHoanThanh().toString());
        JPanel form = SwingSupport.form(
                new String[] { "Mã dự án *", "Tên dự án *", "Mục tiêu", "Ngày bắt đầu *", "Hạn hoàn thành" },
                id, name, SwingSupport.scroll(objective), start, end);
        SwingSupport.editor(this, current == null ? "Thêm dự án" : "Sửa dự án", form,
                () -> new DuAn(id.getText(), name.getText(), objective.getText(),
                        SwingSupport.date(start, "Ngày bắt đầu", true),
                        SwingSupport.date(end, "Hạn hoàn thành", false)),
                row -> {
                    if (current == null)
                        service.create(row);
                    else
                        service.update(row);
                },
                () -> reload(id.getText().trim()));
    }

    private void deleteProject() {
        DuAn row = selectedProject();
        if (SwingSupport.confirm(this, "Xóa dự án " + row.getMaDuAn() + "?\nCần xóa các lượt tham gia trước.")) {
            tasks.run("Đang xóa dự án...", () -> {
                service.delete(row.getMaDuAn());
                return true;
            }, result -> reload(null));
        }
    }

    private void editMember(ThamGiaDuAn current) {
        DuAn project = selectedProject();
        JTextField id = new JTextField(project.getMaDuAn(), 28);
        id.setEditable(false);
        JTextField msv = new JTextField(current == null ? "" : current.getMsv());
        msv.setEditable(current == null);
        msv.setToolTipText("Nhập MSV đã tồn tại trong ThanhVien.");
        JTextField role = new JTextField(current == null || current.getVaiTro() == null ? "" : current.getVaiTro());
        JTextField value = new JTextField(current == null ? "0"
                : current.getTienDoRieng() == null ? "" : current.getTienDoRieng().toPlainString());
        value.setToolTipText("Từ 0 đến 100, tối đa 2 chữ số thập phân. Trống = chưa có.");
        JPanel form = SwingSupport.form(new String[] { "Dự án", "MSV đã có *", "Vai trò", "Tiến độ riêng (%)" }, id,
                msv, role, value);
        SwingSupport.editor(this, current == null ? "Thêm thành viên vào dự án" : "Sửa vai trò và tiến độ", form,
                () -> new ThamGiaDuAn(msv.getText(), project.getMaDuAn(), role.getText(), SwingSupport.progress(value)),
                row -> {
                    if (current == null)
                        service.addParticipant(row);
                    else
                        service.updateParticipant(row);
                }, this::loadMembers);
    }

    private void deleteMember() {
        DuAn project = selectedProject();
        ThamGiaDuAn row = selectedMember();
        if (SwingSupport.confirm(this,
                "Xóa lượt tham gia của " + row.getMsv() + " khỏi " + project.getMaDuAn() + "?")) {
            tasks.run("Đang xóa lượt tham gia...", () -> {
                service.removeParticipant(project.getMaDuAn(), row.getMsv());
                return true;
            }, result -> loadMembers());
        }
    }
}
