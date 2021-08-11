package com.ledgerco.app.calculators;

import static com.ledgerco.app.constants.ServiceConstants.MONTHS_IN_A_YEAR;

public class MonthlyEMICalculator implements IEMICalculator {
    @Override
    public double calculateEMI(double principalAmount, double interestAmount, int years) {
        return Math.ceil((principalAmount + interestAmount) / calculateTenure(years));
    }

    @Override
    public int calculateTenure(int years) {
        return (years * MONTHS_IN_A_YEAR);
    }
}
