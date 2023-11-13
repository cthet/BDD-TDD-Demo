package com.wealcome.testbdd.applicationVTC.port.repositories;

import com.wealcome.testbdd.domain.Booking;

import java.util.Set;

public interface BookingRepository {

    Set<Booking> all();

    void add(Booking booking);
}
