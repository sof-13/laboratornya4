package gui;

import model.BasePayStrategy;
import model.BonusPayStrategy;
import model.PayStrategy;
import model.WorkType;
import service.SalaryDepartment;
import service.InvalidPayException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Comparator;
import java.util.List;

public class MainWindow extends JFrame {
    private SalaryDepartment department = new SalaryDepartment();
    private DefaultTableModel tableModel;
    private JTable table;

    public MainWindow() {
        setTitle("Отдел расчёта зарплаты");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 500);
        setLocationRelativeTo(null);

        String[] columns = {"Название", "Оплата", "Бонус"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton btnAdd = new JButton("Добавить");
        JButton btnEdit = new JButton("Редактировать");
        JButton btnDelete = new JButton("Удалить");
        JButton btnLoad = new JButton("Загрузить");
        JButton btnSave = new JButton("Сохранить");
        JButton btnAvg = new JButton("Средняя оплата");

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(btnAdd);
        panel.add(btnEdit);
        panel.add(btnDelete);
        panel.add(btnLoad);
        panel.add(btnSave);
        panel.add(btnAvg);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> new AddEditWindow(this, department, null));
        btnEdit.addActionListener(this::handleEdit);
        btnDelete.addActionListener(this::handleDelete);
        btnLoad.addActionListener(e -> loadFromFile());
        btnSave.addActionListener(e -> saveToFile());
        btnAvg.addActionListener(e -> showAverage());

        initSampleData();
        refreshTable();
    }

    private void initSampleData() {
        try {
            department.addWorkType("Тестирование", new BasePayStrategy(2500.0));
            department.addWorkType("Анализ данных", new BonusPayStrategy(4000.0, 15.0));
            department.addWorkType("Разработка", new BonusPayStrategy(6000.0, 10.0));
            department.addWorkType("Документирование", new BasePayStrategy(2000.0));
        } catch (InvalidPayException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при инициализации: " + ex.getMessage());
        }
    }

    public void refreshTable() {
        List<WorkType> works = department.getWorkTypes();
        works.sort(Comparator.comparing(WorkType::getName));

        tableModel.setRowCount(0);
        for (WorkType wt : works) {
            try {
                String bonus = wt.getStrategy().getBonusInfo();
                tableModel.addRow(new Object[]{
                        wt.getName(),
                        String.format("%.0f", wt.getPay()),
                        bonus.isEmpty() ? "Нет" : bonus
                });
            } catch (InvalidPayException ex) {
                tableModel.addRow(new Object[]{wt.getName(), "Ошибка", "–"});
            }
        }
    }

    private void handleEdit(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Выберите запись для редактирования.");
            return;
        }
        String name = (String) tableModel.getValueAt(row, 0);
        new AddEditWindow(this, department, name);
    }

    private void handleDelete(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Выберите запись для удаления.");
            return;
        }
        String name = (String) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Удалить '" + name + "'?", "Подтверждение", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            department.removeWorkType(name);
            refreshTable();
        }
    }

    private void showAverage() {
        try {
            double avg = department.getAveragePay();
            JOptionPane.showMessageDialog(this, String.format("Средняя оплата: %.2f руб.", avg));
        } catch (InvalidPayException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
        }
    }

    private void saveToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("salary_data.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
                List<WorkType> works = department.getWorkTypes();
                works.sort(Comparator.comparing(WorkType::getName));

                for (WorkType wt : works) {
                    PayStrategy strat = wt.getStrategy();
                    if (strat instanceof BasePayStrategy) {
                        BasePayStrategy bs = (BasePayStrategy) strat;
                        out.printf("%s|base|%.0f|0%n", wt.getName(), bs.getBaseRate());
                    } else if (strat instanceof BonusPayStrategy) {
                        BonusPayStrategy b = (BonusPayStrategy) strat;
                        out.printf("%s|bonus|%.0f|%d%n", wt.getName(), b.getBaseRate(), (int) Math.round(b.getBonusPercent()));
                    }
                }
                JOptionPane.showMessageDialog(this, "Данные успешно сохранены в:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка сохранения:\n" + ex.getMessage());
            }
        }
    }

    private void loadFromFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            SalaryDepartment newDepartment = new SalaryDepartment(); // временный отдел

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                String line;
                int lineNumber = 0;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split("\\|");
                    if (parts.length != 4) {
                        throw new IOException("Неверный формат в строке " + lineNumber + ": ожидалось 4 поля, получено " + parts.length);
                    }

                    String name = parts[0];
                    String type = parts[1];
                    double base, bonus;

                    try {
                        base = Double.parseDouble(parts[2]);
                        bonus = Double.parseDouble(parts[3]);
                    } catch (NumberFormatException e) {
                        throw new IOException("Некорректное число в строке " + lineNumber);
                    }

                    PayStrategy strategy;
                    if ("base".equals(type)) {
                        strategy = new BasePayStrategy(base);
                    } else if ("bonus".equals(type)) {
                        strategy = new BonusPayStrategy(base, bonus);
                    } else {
                        throw new IOException("Неизвестный тип работы в строке " + lineNumber + ": '" + type + "'");
                    }

                    // Пробуем добавить — если имя дублируется или данные невалидны → InvalidPayException
                    newDepartment.addWorkType(name, strategy);

                }
                // Если дошли сюда — файл валиден
                this.department = newDepartment;
                refreshTable();
                JOptionPane.showMessageDialog(this, "Данные успешно загружены.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка загрузки:\n" + ex.getMessage() + "\n\n" +
                                "Убедитесь, что файл имеет формат:\n" +
                                "Название|тип|ставка|процент\n" +
                                "Пример:\n" +
                                "Разработка|bonus|6000|10",
                        "Ошибка загрузки",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}