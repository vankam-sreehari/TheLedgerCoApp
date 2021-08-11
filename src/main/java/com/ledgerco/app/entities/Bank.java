package com.ledgerco.app.entities;

import com.ledgerco.app.constants.ServiceHelper;
import com.ledgerco.app.exception.NoAccountException;

import java.util.HashMap;
import java.util.Map;

public class Bank {
    private String name;
    private Map<String, LoanAccount> loanAccounts;

    public Bank(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public LoanAccount getLoanAccount(String loanId) throws NoAccountException {
        if (loanAccounts.isEmpty() || !loanAccounts.containsKey(loanId)) {
            throw new NoAccountException("loanId is not present with the Bank");
        }
        return loanAccounts.get(loanId);
    }


    public void addLoanAccount(LoanAccount loanAccount) {
        if (loanAccounts == null) {
            loanAccounts = new HashMap<>();
        }

        loanAccounts.put(ServiceHelper.generateLoanAccountNumber(this, loanAccount.getBorrower()), loanAccount);
    }


    @Override
    public String toString() {
        return name;
    }
}
