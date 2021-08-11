package com.ledgerco.app.entities;

public class Borrower {
    String name;

    public Borrower(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
