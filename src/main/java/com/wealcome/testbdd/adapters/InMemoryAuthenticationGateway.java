package com.wealcome.testbdd.adapters;

import com.wealcome.testbdd.domain.Customer;
import com.wealcome.testbdd.applicationVTC.port.gateways.AuthenticationGateway;

import java.util.Optional;

public class InMemoryAuthenticationGateway implements AuthenticationGateway {

    private Customer currentCustomer;

    public void authenticate(Customer c) {
        this.currentCustomer = c;
    }

    public Optional<Customer> currentCustomer() {
        return Optional.ofNullable(currentCustomer);
    }
}
