package com.ledgerco.app.service;

import com.ledgerco.app.constants.ServiceHelper;
import com.ledgerco.app.entities.*;
import com.ledgerco.app.calculators.IEMICalculator;
import com.ledgerco.app.calculators.InterestCalculator;
import com.ledgerco.app.calculators.MonthlyEMICalculator;
import com.ledgerco.app.calculators.SimpleInterestCalculator;
import com.ledgerco.app.exception.*;

import java.util.HashMap;
import java.util.Map;


public class LoanServiceManager {

    private static LoanServiceManager loanServiceManager;
    private static Map<String, Bank> banks;
    private static Map<String, Borrower> borrowers;

    private LoanServiceManager() {
        // SingleTon Class
    }

    public static LoanServiceManager getInstance() {
        if (loanServiceManager == null) {
            loanServiceManager = new LoanServiceManager();
        }
        return loanServiceManager;
    }


    public Bank getBank(String name) {
        if (banks == null) {
            banks = new HashMap<>();
        }
        if (banks.isEmpty() || !banks.containsKey(name)) {
            banks.put(name, createBank(name));
        }
        return banks.get(name);
    }

    private Bank createBank(String name) {
        return new Bank(name);
    }


    public Borrower getBorrower(String name) {
        if (borrowers == null) {
            borrowers = new HashMap<>();
        }
        if (borrowers.isEmpty() || !borrowers.containsKey(name)) {
            borrowers.put(name, createBorrower(name));
        }
        return borrowers.get(name);
    }


    private Borrower createBorrower(String name) {
        return new Borrower(name);
    }


    public void createLoanAccount(Bank bank, Borrower borrower, Double principalAmount, int years, Double interestRate) throws LoanCreationException {
        bank.addLoanAccount(createLoanAccount(borrower, principalAmount, LoanType.PERSONAL, interestRate, years, new SimpleInterestCalculator(), new MonthlyEMICalculator()));
    }

    public void payLumpSumAmount(Bank bank, Borrower borrower, Double lumpSumAmount, int currentEMICount) throws NoAccountException, EMINotValidException, LumSumAmountExceededException, NoOutStandingAmountException {
        if(currentEMICount <= 0){
            throw new EMINotValidException("EMI Number cannot be non-positive");
        }
        LoanAccount loanAccount = bank.getLoanAccount(ServiceHelper.generateLoanAccountNumber(bank, borrower));
        loanAccount.PayLumSumAmount(lumpSumAmount, currentEMICount);
    }


    public Double amountPaidTillEMINumber(Bank bank, Borrower borrower, int emiNumber) throws NoAccountException, EMINotValidException {
        if(emiNumber <= 0){
            throw new EMINotValidException("EMI Number cannot be non-positive");
        }
        LoanAccount loanAccount = bank.getLoanAccount(ServiceHelper.generateLoanAccountNumber(bank, borrower));
        return loanAccount.amountPaidTillEMINumber(emiNumber);
    }


    public int getRemainingEMICount(Bank bank, Borrower borrower, int emiNumber) throws NoAccountException, EMINotValidException {
        if(emiNumber <= 0){
            throw new EMINotValidException("EMI Number cannot be non-positive");
        }
        LoanAccount loanAccount = bank.getLoanAccount(ServiceHelper.generateLoanAccountNumber(bank, borrower));
        return loanAccount.getRemainingEMICount(emiNumber);
    }


    private LoanAccount createLoanAccount(Borrower borrower, Double principalAmount,
                                          LoanType loanType, Double interestRate,
                                          int years, InterestCalculator interestCalculator,
                                          IEMICalculator monthlyEMICalculator) throws LoanCreationException {
        if (principalAmount <= 0) {
            throw new LoanCreationException("principal amount cannot be non-positive");
        }

        if (interestRate <= 0) {
            throw new LoanCreationException("interest rate cannot be non-positive");
        }

        if (years <= 0) {
            throw new LoanCreationException("years cannot be non-positive");
        }

        return new LoanAccount(borrower, loanType, interestRate, principalAmount, years, interestCalculator, monthlyEMICalculator);
    }


}
