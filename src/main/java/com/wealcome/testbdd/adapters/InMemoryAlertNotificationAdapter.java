package com.wealcome.testbdd.adapters;

import com.wealcome.testbdd.applicationVTC.port.gateways.AlertNotificationAdapter;

public class InMemoryAlertNotificationAdapter implements AlertNotificationAdapter {

    public void sendAlert(String message) {
        System.out.println(message);
    }
}
