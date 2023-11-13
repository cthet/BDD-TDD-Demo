package com.wealcome.testbdd.applicationVTC.port.gateways;

import com.wealcome.testbdd.domain.Customer;

import java.util.Optional;

public interface AuthenticationGateway {

    void authenticate(Customer c);

    Optional<Customer> currentCustomer();
}
