package model;

import service.InvalidPayException;

public interface PayStrategy {
    double calculatePay() throws InvalidPayException;
    String getBonusInfo();
}