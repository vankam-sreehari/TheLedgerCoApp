package com.ledgerco.app.calculators;

public interface IEMICalculator {
    double calculateEMI(double principalAmount, double interestAmount, int years);
    int calculateTenure(int years);
}
