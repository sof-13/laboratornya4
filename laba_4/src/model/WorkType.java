package model;

import service.InvalidPayException;

public class WorkType {
    private String name;
    private PayStrategy strategy;

    public WorkType(String name, PayStrategy strategy) {
        this.name = name;
        this.strategy = strategy;
    }

    public String getName() {
        return name;
    }

    public PayStrategy getStrategy() {
        return strategy;
    }

    public double getPay() throws InvalidPayException {
        return strategy.calculatePay();
    }

    @Override
    public String toString() {
        try {
            return String.format("Работа: %s, Оплата: %.0f%s",
                    name, getPay(), strategy.getBonusInfo());
        } catch (InvalidPayException e) {
            return "Ошибка при расчёте оплаты для " + name;
        }
    }
}