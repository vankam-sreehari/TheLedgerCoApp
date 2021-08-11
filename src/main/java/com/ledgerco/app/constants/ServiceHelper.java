package com.ledgerco.app.constants;

import com.ledgerco.app.entities.Bank;
import com.ledgerco.app.entities.Borrower;

public class ServiceHelper {
    private ServiceHelper() {
        // Helper
    }

    public static String generateLoanAccountNumber(Bank bank, Borrower borrower){
        return bank.getName()+"_"+borrower.getName();
    }
}
