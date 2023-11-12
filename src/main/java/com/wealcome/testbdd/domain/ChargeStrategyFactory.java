package com.wealcome.testbdd.domain;

public class ChargeStrategyFactory {

    public static ChargeStrategy create(String startPoint, String destinationPoint) {
        if(startPoint.contains("Paris") && destinationPoint.contains("Paris"))
            return new IntraMuralChargesStrategy();
        if(destinationPoint.contains("Paris"))
            return new EnteringParisChargeStrategy();
        return new LeavingParisChargeStrategy();
    }
}
