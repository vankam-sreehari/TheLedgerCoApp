package com.ledgerco.app.calculators;

public class SimpleInterestCalculator implements InterestCalculator {
    @Override
    public double calculateInterest(Double principal, int years, Double rateOfInterest) {
        return Math.ceil((principal * years * rateOfInterest) / 100);
    }
}
