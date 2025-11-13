package gui;

import model.BasePayStrategy;
import model.BonusPayStrategy;
import model.PayStrategy;
import model.WorkType;
import service.InvalidPayException;
import service.SalaryDepartment;

import javax.swing.*;
import java.awt.*;

public class AddEditWindow extends JFrame {
    private MainWindow parent;
    private SalaryDepartment department;
    private String editModeName = null;

    private JTextField nameField = new JTextField(20);
    private JRadioButton noBonusRadio = new JRadioButton("Без надбавки");
    private JRadioButton yesBonusRadio = new JRadioButton("С надбавкой");
    private JTextField baseField = new JTextField(10);
    private JTextField bonusField = new JTextField(10);

    public AddEditWindow(MainWindow parent, SalaryDepartment department, String workNameToEdit) {
        this.parent = parent;
        this.department = department;
        this.editModeName = workNameToEdit;

        setTitle(editModeName == null ? "Добавить работу" : "Редактировать: " + workNameToEdit);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        if (editModeName != null) {
            WorkType wt = department.findWorkType(editModeName);
            if (wt != null) {
                nameField.setText(wt.getName());
                PayStrategy s = wt.getStrategy();
                if (s instanceof BasePayStrategy) {
                    noBonusRadio.setSelected(true);
                    baseField.setText(String.format("%.0f", ((BasePayStrategy) s).getBaseRate()));
                    bonusField.setEnabled(false);
                } else if (s instanceof BonusPayStrategy) {
                    yesBonusRadio.setSelected(true);
                    BonusPayStrategy bs = (BonusPayStrategy) s;
                    baseField.setText(String.format("%.0f", bs.getBaseRate()));
                    bonusField.setText(String.format("%.0f", bs.getBonusPercent()));
                    bonusField.setEnabled(true);
                }
            }
        }

        ButtonGroup group = new ButtonGroup();
        group.add(noBonusRadio);
        group.add(yesBonusRadio);
        noBonusRadio.setSelected(true);

        yesBonusRadio.addActionListener(e -> bonusField.setEnabled(true));
        noBonusRadio.addActionListener(e -> bonusField.setEnabled(false));
        bonusField.setEnabled(false);

        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Название:"), gbc);
        gbc.gridx = 1; add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; add(new JLabel("Тип:"), gbc);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(noBonusRadio);
        radioPanel.add(yesBonusRadio);
        gbc.gridx = 1; add(radioPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 2; add(new JLabel("Базовая ставка:"), gbc);
        gbc.gridx = 1; add(baseField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; add(new JLabel("Процент надбавки:"), gbc);
        gbc.gridx = 1; add(bonusField, gbc);

        JButton saveBtn = new JButton("Сохранить");
        saveBtn.addActionListener(e -> save());
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; add(saveBtn, gbc);

        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void save() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Название не может быть пустым!");
            return;
        }

        try {
            // Замена запятой на точку (на случай ввода 1000,50)
            String baseText = baseField.getText().replace(',', '.');
            double base = Math.round(Double.parseDouble(baseText));

            PayStrategy strategy;
            if (yesBonusRadio.isSelected()) {
                String bonusText = bonusField.getText().replace(',', '.');
                double bonus = Math.round(Double.parseDouble(bonusText));
                strategy = new BonusPayStrategy(base, bonus);
            } else {
                strategy = new BasePayStrategy(base);
            }

            if (editModeName == null) {
                department.addWorkType(name, strategy);
            } else {
                department.updateWorkType(editModeName, name, strategy);
            }

            parent.refreshTable();
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Некорректное число в ставке или бонусе.");
        } catch (InvalidPayException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
        }
    }
}