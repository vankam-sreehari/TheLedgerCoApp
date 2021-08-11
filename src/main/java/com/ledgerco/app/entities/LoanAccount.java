package com.ledgerco.app.entities;

import com.ledgerco.app.calculators.IEMICalculator;
import com.ledgerco.app.calculators.InterestCalculator;
import com.ledgerco.app.exception.EMINotValidException;
import com.ledgerco.app.exception.LumSumAmountExceededException;
import com.ledgerco.app.exception.NoOutStandingAmountException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class LoanAccount {
    private final LoanType loanType;
    private final Double principalAmount;
    private Borrower borrower;
    private Double rateOfInterest;
    private int years;

    private double outstandingAmount;
    private double emiAmount;
    private int remainingEMICount;
    private int endingEMINumber;
    private List<LedgerEntry> outstandingAmountLedger;
    private Map<Integer, Double> emiToOutstandingAmountMap;
    private InterestCalculator interestCalculator;


    private class LedgerEntry {
        int emiNumber;
        double outstandingAmount;

        public LedgerEntry(int emiNumber, double outstandingAmount) {
            this.emiNumber = emiNumber;
            this.outstandingAmount = outstandingAmount;
        }

        public int getEmiNumber() {
            return emiNumber;
        }

        public double getOutstandingAmount() {
            return outstandingAmount;
        }
    }


    public LoanAccount(Borrower borrower, LoanType loanType,
                       Double rateOfInterest, Double principalAmount,
                       int years, InterestCalculator interestCalculator,
                       IEMICalculator emiCalculator) {
        this.borrower = borrower;
        this.loanType = loanType;
        this.rateOfInterest = rateOfInterest;
        this.principalAmount = principalAmount;
        this.years = years;
        this.interestCalculator = interestCalculator;
        this.outstandingAmount = calculateInitialOutStandingAmount();
        this.emiAmount = emiCalculator.calculateEMI(principalAmount, interestCalculator.calculateInterest(principalAmount, years, rateOfInterest), years);
        this.endingEMINumber = emiCalculator.calculateTenure(years);
    }


    private double calculateInitialOutStandingAmount() {
        return Math.ceil(principalAmount + interestCalculator.calculateInterest(principalAmount, years, rateOfInterest));
    }

    public Borrower getBorrower() {
        return borrower;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public Double getRateOfInterest() {
        return rateOfInterest;
    }

    public Double getPrincipalAmount() {
        return principalAmount;
    }

    public int getYears() {
        return years;
    }

    public int getRemainingEMICount() {
        return remainingEMICount;
    }


    public void PayLumSumAmount(Double lumSumAmount, int emiNumber) throws EMINotValidException, LumSumAmountExceededException, NoOutStandingAmountException {
        if (outstandingAmount == 0) {
            throw new NoOutStandingAmountException("No OutStanding Balance");
        }
        if (endingEMINumber < emiNumber) {
            throw new EMINotValidException("EMI Number exceeds lastValidEMINumber");
        }

        if (outstandingAmountLedger == null) {
            outstandingAmountLedger = new ArrayList<>();
        }
        handleOutStandingAmountAndEmiCount(lumSumAmount, emiNumber);
        outstandingAmountLedger.add(new LedgerEntry(emiNumber, outstandingAmount));
        if (emiToOutstandingAmountMap == null) {
            emiToOutstandingAmountMap = new HashMap<>();
        }
        emiToOutstandingAmountMap.put(emiNumber, outstandingAmount);
    }

    private void handleOutStandingAmountAndEmiCount(Double lumSumAmount, int emiNumber) throws EMINotValidException, LumSumAmountExceededException {
        int lastLumSumPaymentEMINumber = 0;
        if (!outstandingAmountLedger.isEmpty()) {
            lastLumSumPaymentEMINumber = outstandingAmountLedger.get(outstandingAmountLedger.size() - 1).getEmiNumber();
        }
        if (lastLumSumPaymentEMINumber > emiNumber) {
            throw new EMINotValidException("EMI Number Request is non-increasing");
        }
        outstandingAmount = outstandingAmount - Math.ceil(emiAmount * (emiNumber - lastLumSumPaymentEMINumber));
        if (outstandingAmount >= lumSumAmount) {
            outstandingAmount -= lumSumAmount;
        } else {
            throw new LumSumAmountExceededException("You are paying " + (lumSumAmount - outstandingAmount) + "more");
        }
        remainingEMICount = (int) Math.ceil(outstandingAmount / emiAmount);
        endingEMINumber = emiNumber + remainingEMICount;
    }


    public double amountPaidTillEMINumber(int emiNumber) throws EMINotValidException {
        if (emiToOutstandingAmountMap != null
                && !emiToOutstandingAmountMap.isEmpty()
                && emiToOutstandingAmountMap.containsKey(emiNumber)) {

            return calculateInitialOutStandingAmount() - emiToOutstandingAmountMap.get(emiNumber);
        }
        if (endingEMINumber < emiNumber) {
            throw new EMINotValidException("EMI Number exceeds lastValidEMINumber");
        }
        if (outstandingAmountLedger != null) {
            List<LedgerEntry> ledgerEntriesBeforeThisEMI = outstandingAmountLedger.stream().filter(ledgerEntry -> ledgerEntry.getEmiNumber() < emiNumber).collect(Collectors.toList());
            if (!ledgerEntriesBeforeThisEMI.isEmpty()) {
                double lastOutStandingAmount = ledgerEntriesBeforeThisEMI.get(ledgerEntriesBeforeThisEMI.size() - 1).getOutstandingAmount();
                return calculateInitialOutStandingAmount() - (lastOutStandingAmount - Math.ceil((emiNumber - ledgerEntriesBeforeThisEMI.get(ledgerEntriesBeforeThisEMI.size() - 1).getEmiNumber()) * emiAmount));
            }
        }

        return Math.ceil(emiNumber * emiAmount);
    }

    public int getRemainingEMICount(int emiNumber) throws EMINotValidException {
        if (emiToOutstandingAmountMap != null
                && !emiToOutstandingAmountMap.isEmpty()
                && emiToOutstandingAmountMap.containsKey(emiNumber)) {
            return getRemainingEMICount();
        }

        double amountPaidTillNow = amountPaidTillEMINumber(emiNumber);
        return (int) Math.ceil((calculateInitialOutStandingAmount() - amountPaidTillNow) / emiAmount);
    }

}
