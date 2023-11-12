package com.wealcome.testbdd.domain;

import java.math.BigDecimal;

public class IntraMuralChargesStrategy implements ChargeStrategy {
    @Override
    public Charge charge(BigDecimal creditNote) {
        return new Charge(BigDecimal.valueOf(30));
    }
}
