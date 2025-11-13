package service;

import model.WorkType;
import model.PayStrategy;
import java.util.*;

public class SalaryDepartment {
    private List<WorkType> workTypes;
    private Set<String> workNames;

    public SalaryDepartment() {
        this.workTypes = new ArrayList<>();
        this.workNames = new HashSet<>();
    }

    public boolean isWorkExists(String name) {
        return workNames.contains(name);
    }

    public void addWorkType(String name, PayStrategy strategy) throws InvalidPayException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidPayException("Название работы не может быть пустым!");
        }
        String trimmedName = name.trim();
        if (workNames.contains(trimmedName)) {
            throw new InvalidPayException("Работа с названием '" + trimmedName + "' уже существует!");
        }
        workTypes.add(new WorkType(trimmedName, strategy));
        workNames.add(trimmedName);
    }

    public void addWorkTypeUnsafe(String name, PayStrategy strategy) throws InvalidPayException {
        workTypes.add(new WorkType(name, strategy));
        workNames.add(name);
    }

    public double getAveragePay() throws InvalidPayException {
        if (workTypes.isEmpty()) {
            throw new InvalidPayException("Нет данных для расчёта средней оплаты.");
        }
        double total = 0;
        for (WorkType wt : workTypes) {
            total += wt.getPay();
        }
        return total / workTypes.size();
    }

    public void displayAllWorkTypes() {
        if (workTypes.isEmpty()) {
            System.out.println("Нет добавленных типов работ.");
            return;
        }
        for (WorkType wt : workTypes) {
            System.out.println(wt);
        }
    }

    public List<WorkType> getWorkTypes() {
        return new ArrayList<>(workTypes);
    }

    public void removeWorkType(String name) {
        String n = name.trim();
        workTypes.removeIf(wt -> wt.getName().equals(n));
        workNames.remove(n);
    }

    public WorkType findWorkType(String name) {
        return workTypes.stream().filter(wt -> wt.getName().equals(name.trim())).findFirst().orElse(null);
    }

    public void updateWorkType(String oldName, String newName, PayStrategy newStrategy) throws InvalidPayException {
        removeWorkType(oldName);
        addWorkType(newName, newStrategy);
    }
}