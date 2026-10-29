package khoahoc;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import common.SwingSupport;

/** Giao diện Swing nằm cùng package phẳng với các lớp của module. */
public class KhoaHocSwingUi extends JPanel {
    private final KhoaHocService service;
    private final JTextField keyword = new JTextField(24);
    private final DefaultTableModel courses = SwingSupport.model("Mã khóa học", "Tên khóa học", "Khai giảng",
            "Bế giảng");
    private final DefaultTableModel members = SwingSupport.model("MSV", "Vai trò", "Kết quả đánh giá");
    private final JTable courseTable = SwingSupport.table(courses, "khoahoc.table");
    private final JTable memberTable = SwingSupport.table(members, "khoahoc.members");
    private final JLabel memberTitle = new JLabel("Thành viên tham gia — chọn một khóa học");
    private final JLabel status = new JLabel("Nhấn Tải lại để đọc danh sách từ MySQL.");
    private final SwingSupport.Tasks tasks = new SwingSupport.Tasks(this, status);
    private List<KhoaHoc> courseRows = new ArrayList<>();
    private List<ThamGiaKhoaHoc> memberRows = new ArrayList<>();
    private boolean rendering;

    public KhoaHocSwingUi(KhoaHocService service) {
        this.service = service;
        setName("khoahoc.panel");
        setLayout(new BorderLayout(10, 10));
        setBackground(SwingSupport.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        keyword.setName("khoahoc.keyword");
        keyword.setToolTipText("Tìm chuỗi con trong mã hoặc tên khóa học. Trống = tất cả.");
        keyword.addActionListener(event -> reload(null));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        toolbar.add(new JLabel("Mã / tên:"));
        toolbar.add(keyword);
        toolbar.add(SwingSupport.button("Tìm kiếm", "khoahoc.search", () -> reload(null)));
        toolbar.add(SwingSupport.button("Tải lại", "khoahoc.reload", () -> reload(selectedId())));
        toolbar.add(SwingSupport.button("Thêm khóa học", "khoahoc.add", () -> editCourse(null)));
        add(toolbar, BorderLayout.NORTH);

        JPanel courseSection = new JPanel(new BorderLayout(6, 6));
        JPanel courseActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        JLabel title = new JLabel("DANH SÁCH KHÓA HỌC");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        courseActions.add(title);
        courseActions.add(SwingSupport.button("Sửa thông tin", "khoahoc.edit", () -> editCourse(selectedCourse())));
        courseActions.add(SwingSupport.button("Xóa khóa học", "khoahoc.delete", this::deleteCourse));
        courseSection.add(courseActions, BorderLayout.NORTH);
        courseSection.add(SwingSupport.scroll(courseTable), BorderLayout.CENTER);

        JPanel memberSection = new JPanel(new BorderLayout(6, 6));
        JPanel memberActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        memberTitle.setFont(memberTitle.getFont().deriveFont(Font.BOLD));
        memberSection.add(memberTitle, BorderLayout.NORTH);
        memberSection.add(SwingSupport.scroll(memberTable), BorderLayout.CENTER);
        memberActions.add(SwingSupport.button("Thêm thành viên", "khoahoc.member.add", () -> editMember(null)));
        memberActions.add(SwingSupport.button("Sửa vai trò / đánh giá", "khoahoc.member.edit",
                () -> editMember(selectedMember())));
        memberActions.add(SwingSupport.button("Xóa lượt tham gia", "khoahoc.member.delete", this::deleteMember));
        memberSection.add(memberActions, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, courseSection, memberSection);
        split.setResizeWeight(0.57);
        split.setDividerLocation(290);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
        status.setName("khoahoc.status");
        add(status, BorderLayout.SOUTH);
        courseTable.getSelectionModel().addListSelectionListener(event -> {
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
            courseRows = List.of();
            memberRows = List.of();
            courses.setRowCount(0);
            members.setRowCount(0);
        } finally {
            rendering = false;
        }
        memberTitle.setText("Thành viên tham gia — chọn một khóa học");
        reload(null);
    }

    private void reload(String keepId) {
        String query = keyword.getText();
        tasks.run("Đang tải khóa học...", () -> service.search(query), rows -> {
            rendering = true;
            try {
                courseRows = rows;
                courses.setRowCount(0);
                for (KhoaHoc row : rows)
                    courses.addRow(new Object[] { row.getMaKhoaHoc(), row.getTenKhoaHoc(),
                            SwingSupport.text(row.getNgayKhaiGiang()), SwingSupport.text(row.getNgayBeGiang()) });
                int index = 0;
                for (int i = 0; i < rows.size(); i++) {
                    if (rows.get(i).getMaKhoaHoc().equals(keepId)) {
                        index = i;
                        break;
                    }
                }
                if (!rows.isEmpty()) {
                    int view = courseTable.convertRowIndexToView(index);
                    courseTable.setRowSelectionInterval(view, view);
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
        if (courseTable.getSelectedRow() < 0) {
            memberTitle.setText("Thành viên tham gia — chọn một khóa học");
            status.setText("Tìm thấy " + courseRows.size() + " khóa học.");
            return;
        }
        KhoaHoc course = selectedCourse();
        memberTitle.setText("Thành viên — " + course.getMaKhoaHoc() + " · " + course.getTenKhoaHoc());
        tasks.run("Đang tải người tham gia...", () -> service.participants(course.getMaKhoaHoc()), rows -> {
            if (!course.getMaKhoaHoc().equals(selectedId())) {
                loadMembers();
                return;
            }
            memberRows = rows;
            for (ThamGiaKhoaHoc row : rows)
                members.addRow(new Object[] { row.getMsv(),
                        SwingSupport.text(row.getVaiTro()), SwingSupport.text(row.getKetQuaDanhGia()) });
            status.setText(courseRows.size() + " khóa học  |  " + rows.size() + " người trong khóa đang chọn.");
        });
    }

    private String selectedId() {
        return courseTable.getSelectedRow() < 0 ? null : selectedCourse().getMaKhoaHoc();
    }

    private KhoaHoc selectedCourse() {
        int view = courseTable.getSelectedRow();
        if (view < 0)
            throw new IllegalArgumentException("Hãy chọn một khóa học trong bảng.");
        return courseRows.get(courseTable.convertRowIndexToModel(view));
    }

    private ThamGiaKhoaHoc selectedMember() {
        selectedCourse();
        int view = memberTable.getSelectedRow();
        if (view < 0)
            throw new IllegalArgumentException("Hãy chọn một người tham gia trong bảng phía dưới.");
        return memberRows.get(memberTable.convertRowIndexToModel(view));
    }

    private void editCourse(KhoaHoc current) {
        JTextField id = new JTextField(current == null ? "" : current.getMaKhoaHoc(), 28);
        JTextField name = new JTextField(current == null ? "" : current.getTenKhoaHoc());
        JTextField start = new JTextField(current == null ? "" : current.getNgayKhaiGiang().toString());
        JTextField end = new JTextField(
                current == null || current.getNgayBeGiang() == null ? "" : current.getNgayBeGiang().toString());
        id.setEditable(current == null);
        JPanel form = SwingSupport.form(
                new String[] { "Mã khóa học *", "Tên khóa học *", "Ngày khai giảng *", "Ngày bế giảng" },
                id, name, start, end);
        SwingSupport.editor(this, current == null ? "Thêm khóa học" : "Sửa khóa học", form,
                () -> new KhoaHoc(id.getText(), name.getText(), SwingSupport.date(start, "Ngày khai giảng", true),
                        SwingSupport.date(end, "Ngày bế giảng", false)),
                row -> {
                    if (current == null)
                        service.create(row);
                    else
                        service.update(row);
                },
                () -> reload(id.getText().trim()));
    }

    private void deleteCourse() {
        KhoaHoc row = selectedCourse();
        if (SwingSupport.confirm(this, "Xóa khóa học " + row.getMaKhoaHoc() + "?\nCần xóa các lượt tham gia trước.")) {
            tasks.run("Đang xóa khóa học...", () -> {
                service.delete(row.getMaKhoaHoc());
                return true;
            }, result -> reload(null));
        }
    }

    private void editMember(ThamGiaKhoaHoc current) {
        KhoaHoc course = selectedCourse();
        JTextField id = new JTextField(course.getMaKhoaHoc(), 28);
        id.setEditable(false);
        JTextField msv = new JTextField(current == null ? "" : current.getMsv());
        msv.setEditable(current == null);
        msv.setToolTipText("Nhập MSV đã tồn tại trong ThanhVien.");
        JTextField role = new JTextField(current == null || current.getVaiTro() == null ? "" : current.getVaiTro());
        JTextField result = new JTextField(
                current == null || current.getKetQuaDanhGia() == null ? "" : current.getKetQuaDanhGia());
        result.setToolTipText("Ví dụ: DangHoc, Dat, KhongDat.");
        JPanel form = SwingSupport.form(new String[] { "Khóa học", "MSV đã có *", "Vai trò", "Kết quả đánh giá" }, id,
                msv, role, result);
        SwingSupport.editor(this, current == null ? "Thêm thành viên vào khóa học" : "Sửa thông tin tham gia", form,
                () -> new ThamGiaKhoaHoc(msv.getText(), course.getMaKhoaHoc(), role.getText(), result.getText()),
                row -> {
                    if (current == null)
                        service.addParticipant(row);
                    else
                        service.updateParticipant(row);
                }, this::loadMembers);
    }

    private void deleteMember() {
        KhoaHoc course = selectedCourse();
        ThamGiaKhoaHoc row = selectedMember();
        if (SwingSupport.confirm(this,
                "Xóa lượt tham gia của " + row.getMsv() + " khỏi " + course.getMaKhoaHoc() + "?")) {
            tasks.run("Đang xóa lượt tham gia...", () -> {
                service.removeParticipant(course.getMaKhoaHoc(), row.getMsv());
                return true;
            }, result -> loadMembers());
        }
    }
}
