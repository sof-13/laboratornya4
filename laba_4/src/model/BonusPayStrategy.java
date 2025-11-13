package model;

import service.InvalidPayException;

public class BonusPayStrategy implements PayStrategy {
    private double baseRate;
    private double bonusPercent;

    public BonusPayStrategy(double baseRate, double bonusPercent) throws InvalidPayException {
        if (baseRate <= 0) {
            throw new InvalidPayException("Базовая ставка должна быть положительной!");
        }
        if (baseRate >= 10000000) {
            throw new InvalidPayException("Базовая ставка должна быть <10000000!");
        }
        if (bonusPercent <= 0 || bonusPercent >= 100) {
            throw new InvalidPayException("Процент надбавки должен быть больше 0 и меньше 100!");
        }
        this.baseRate = baseRate;
        this.bonusPercent = bonusPercent;
    }

    @Override
    public double calculatePay() {
        return baseRate * (1 + bonusPercent / 100.0);
    }

    @Override
    public String getBonusInfo() {
        return String.format(" (надбавка +%d%%)", (int) Math.round(bonusPercent));
    }

    public double getBaseRate() {
        return baseRate;
    }

    public double getBonusPercent() {
        return bonusPercent;
    }
}