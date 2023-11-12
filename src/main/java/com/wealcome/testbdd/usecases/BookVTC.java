package com.wealcome.testbdd.usecases;

import com.wealcome.testbdd.domain.Booking;
import com.wealcome.testbdd.domain.ChargeStrategyFactory;
import com.wealcome.testbdd.domain.Customer;
import com.wealcome.testbdd.domain.VTC;
import com.wealcome.testbdd.domain.gateways.AuthenticationGateway;
import com.wealcome.testbdd.domain.repositories.BookingRepository;
import com.wealcome.testbdd.domain.repositories.CustomerAccountRepository;

public class BookVTC {

    private final CustomerAccountRepository customerAccountRepository;
    private final BookingRepository bookingRepository;
    private final AuthenticationGateway authenticationGateway;

    public BookVTC(CustomerAccountRepository customerAccountRepository, BookingRepository bookingRepository, AuthenticationGateway authenticationGateway) {
        this.customerAccountRepository = customerAccountRepository;
        this.bookingRepository = bookingRepository;
        this.authenticationGateway = authenticationGateway;
    }

    public void handle(VTC vtc, String startPoint, String destinationPoint) {
        authenticationGateway.currentCustomer().ifPresent(customer -> {                ;
                applyBooking(new Booking(customer, vtc, startPoint, destinationPoint));
                chargeCustomer(customer, startPoint, destinationPoint);}
            );

    }

    private void applyBooking(Booking booking) {
        bookingRepository.add(booking);
    }

    private void chargeCustomer(Customer customer, String startPoint, String destinationPoint) {
        customerAccountRepository.byId(customer.getId()).ifPresent(customerAccount ->
                customerAccount.charge(ChargeStrategyFactory.create(startPoint, destinationPoint)));
    }
}
