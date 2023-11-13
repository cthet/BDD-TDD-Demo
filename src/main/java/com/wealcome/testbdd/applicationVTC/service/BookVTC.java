package com.wealcome.testbdd.applicationVTC.service;

import com.wealcome.testbdd.applicationVTC.port.gateways.AlertNotificationAdapter;
import com.wealcome.testbdd.applicationVTC.port.gateways.AuthenticationGateway;
import com.wealcome.testbdd.applicationVTC.port.repositories.BookingRepository;
import com.wealcome.testbdd.applicationVTC.port.repositories.CustomerAccountRepository;
import com.wealcome.testbdd.domain.Booking;
import com.wealcome.testbdd.domain.ChargeStrategyFactory;
import com.wealcome.testbdd.domain.Customer;
import com.wealcome.testbdd.domain.VTC;

import java.util.Optional;

public class BookVTC {

    private final CustomerAccountRepository customerAccountRepository;
    private final BookingRepository bookingRepository;
    private final AuthenticationGateway authenticationGateway;
    private final AlertNotificationAdapter alertNotificationAdapter;


    public BookVTC(CustomerAccountRepository customerAccountRepository, BookingRepository bookingRepository, AuthenticationGateway authenticationGateway, AlertNotificationAdapter alertNotificationAdapter) {
        this.customerAccountRepository = customerAccountRepository;
        this.bookingRepository = bookingRepository;
        this.authenticationGateway = authenticationGateway;
        this.alertNotificationAdapter = alertNotificationAdapter;
    }

    public void handle(VTC vtc, String startPoint, String destinationPoint) {
        Optional<Customer> optCustomer =  authenticationGateway.currentCustomer();

        optCustomer.ifPresentOrElse(customer -> {
            customerAccountRepository.byId(customer.getId()).ifPresent( customerAccount ->  {
                if(customerAccount.hasSufficientBalance(ChargeStrategyFactory.create(startPoint, destinationPoint))) {
                    applyBooking(new Booking(customer, vtc, startPoint, destinationPoint));
                    chargeCustomer(customer, startPoint, destinationPoint);
                } else {
                    alertNotificationAdapter.sendAlert("Alerte: Solde insuffisant!");
                }
            });
        }, () -> alertNotificationAdapter.sendAlert("Alerte: Identification du client impossible!"));
    }

    private void applyBooking(Booking booking) {
        bookingRepository.add(booking);
    }

    private void chargeCustomer(Customer customer, String startPoint, String destinationPoint) {
        customerAccountRepository.byId(customer.getId()).ifPresent(customerAccount ->
                customerAccount.charge(ChargeStrategyFactory.create(startPoint, destinationPoint)));
    }


}
