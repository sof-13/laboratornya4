package model;

import service.InvalidPayException;

public class BasePayStrategy implements PayStrategy {
    private double baseRate;

    public BasePayStrategy(double baseRate) throws InvalidPayException {
        if (baseRate <= 0) {
            throw new InvalidPayException("Базовая ставка должна быть положительной!");
        }
        if (baseRate >= 10000000) {
            throw new InvalidPayException("Базовая ставка должна быть <10000000!");
        }
        this.baseRate = baseRate;
    }

    @Override
    public double calculatePay() {
        return baseRate;
    }

    @Override
    public String getBonusInfo() {
        return "";
    }

    public double getBaseRate() {
        return baseRate;
    }
}