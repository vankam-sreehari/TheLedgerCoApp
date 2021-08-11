package com.ledgerco.app;

import com.ledgerco.app.constants.ServiceRequestType;
import com.ledgerco.app.entities.Bank;
import com.ledgerco.app.entities.Borrower;
import com.ledgerco.app.exception.InValidRequestException;
import com.ledgerco.app.service.LoanServiceManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static com.ledgerco.app.constants.ServiceConstants.*;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        //Scanner scanner = new Scanner(System.in);
        //String filePath = scanner.next();

        BufferedReader reader = new BufferedReader(
                new FileReader("/Users/sree.hari/Desktop/rome-models/TheLedgerCoApp/src/main/resources/InputFile"));
        try {
            String request;
            while ((request = reader.readLine()) != null) {
                handleRequest(request);
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Please enter valid input");
            System.exit(0);
        }
    }


    private static void handleRequest(String request) {
        try {
            String[] requestComponents = request.split(" ");
            validateRequest(requestComponents);
            ServiceRequestType serviceRequestType = getServiceRequestType(requestComponents[0]);
            LoanServiceManager loanServiceManager = LoanServiceManager.getInstance();
            switch (serviceRequestType) {
                case LOAN:
                    Bank loanProvider = loanServiceManager.getBank(requestComponents[1].toLowerCase());
                    Borrower loanReceiver = loanServiceManager.getBorrower(requestComponents[2].toLowerCase());
                    loanServiceManager.createLoanAccount(loanProvider, loanReceiver,
                            Double.parseDouble(requestComponents[3]),
                            Integer.parseInt(requestComponents[4]),
                            Double.parseDouble(requestComponents[5]));
                    break;
                case PAYMENT:
                    Bank paymentBank = loanServiceManager.getBank(requestComponents[1].toLowerCase());
                    Borrower payer = loanServiceManager.getBorrower(requestComponents[2].toLowerCase());
                    loanServiceManager.payLumpSumAmount(paymentBank, payer, Double.parseDouble(requestComponents[3]), Integer.parseInt(requestComponents[4]));
                    break;
                case BALANCE:
                    Bank bank = loanServiceManager.getBank(requestComponents[1].toLowerCase());
                    Borrower borrower = loanServiceManager.getBorrower(requestComponents[2].toLowerCase());
                    double amountPaidTillNow = loanServiceManager.amountPaidTillEMINumber(bank, borrower, Integer.parseInt(requestComponents[3]));
                    int emiLeft = loanServiceManager.getRemainingEMICount(bank, borrower, Integer.parseInt(requestComponents[3]));
                    System.out.println(bank + " " + borrower + " " + amountPaidTillNow + " " + emiLeft);
                    break;
                default:
                    throw new InValidRequestException("requestType not valid");
            }

        } catch (InValidRequestException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private static void validateRequest(String[] requestComponents) throws InValidRequestException {
        if (isNotValidLength(requestComponents)) {
            throw new InValidRequestException("Invalid Parameters count");
        }

        ServiceRequestType serviceRequestType = getServiceRequestType(requestComponents[0]);

        switch (serviceRequestType) {
            case LOAN:
                validateLoanRequest(requestComponents);
                break;
            case BALANCE:
                validateBalanceRequest(requestComponents);
                break;
            case PAYMENT:
                validatePaymentRequest(requestComponents);
                break;
            default:
                throw new InValidRequestException("requestType not valid");
        }


    }

    private static void validatePaymentRequest(String[] requestComponents) throws InValidRequestException {
        if (requestComponents.length != PAYMENT_REQUEST_PARAMETERS_COUNT) {
            throw new InValidRequestException("Invalid Parameters count");
        }
        try {
            Double.parseDouble(requestComponents[3]);
            Integer.parseInt(requestComponents[4]);
        } catch (NumberFormatException e) {
            throw new InValidRequestException(e.getMessage());
        }
    }

    private static void validateBalanceRequest(String[] requestComponents) throws InValidRequestException {
        if (requestComponents.length != BALANCE_REQUEST_PARAMETERS_COUNT) {
            throw new InValidRequestException("Invalid Parameters count");
        }
        try {
            Integer.parseInt(requestComponents[3]);
        } catch (NumberFormatException e) {
            throw new InValidRequestException(e.getMessage());
        }
    }

    private static void validateLoanRequest(String[] requestComponents) throws InValidRequestException {
        if (requestComponents.length != LOAN_REQUEST_PARAMETERS_COUNT) {
            throw new InValidRequestException("Invalid Parameters count");
        }
        try {
            Double.parseDouble(requestComponents[3]);
            Integer.parseInt(requestComponents[4]);
            Double.parseDouble(requestComponents[5]);
        } catch (NumberFormatException e) {
            throw new InValidRequestException(e.getMessage());
        }
    }

    private static ServiceRequestType getServiceRequestType(String requestType) throws InValidRequestException {
        try {
            return Enum.valueOf(ServiceRequestType.class, requestType);
        } catch (NullPointerException e) {
            throw new InValidRequestException("requestType cannot be null");
        } catch (IllegalArgumentException e) {
            throw new InValidRequestException("requestType not valid");
        }
    }

    private static boolean isNotValidLength(String[] requestComponents) {
        return !(requestComponents.length == LOAN_REQUEST_PARAMETERS_COUNT || requestComponents.length == PAYMENT_REQUEST_PARAMETERS_COUNT || requestComponents.length == BALANCE_REQUEST_PARAMETERS_COUNT);
    }


}
