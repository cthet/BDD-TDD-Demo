package acceptance;

import com.wealcome.testbdd.domain.CustomerAccount;
import com.wealcome.testbdd.applicationVTC.port.gateways.AuthenticationGateway;
import com.wealcome.testbdd.applicationVTC.port.repositories.CustomerAccountRepository;
import io.cucumber.java8.En;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class CustomerAccountSteps implements En {

    public CustomerAccountSteps(CustomerAccountRepository customerAccountRepository,
                                AuthenticationGateway authenticationGateway) {

        And("^le solde de mon compte est de \"([^\"]*)\" euros TTC avec \"([^\"]*)\" euros TTC d'avoir$",
                (String balance, String creditNote) -> {
                    authenticationGateway.currentCustomer().ifPresent(customer -> {
                        CustomerAccount expectedCustomerAccount = new CustomerAccount(
                                customer.getId(),
                                BigDecimal.valueOf(Long.parseLong(balance)),
                                BigDecimal.valueOf(Long.parseLong(creditNote)));
                        if (shouldInitCustomerAccount(customerAccountRepository)) {
                            customerAccountRepository.add(expectedCustomerAccount);
                        } else
                            assertEquals(expectedCustomerAccount, customerAccountRepository.byId(customer.getId()).get());
                    });

                });
    }

    private boolean shouldInitCustomerAccount(CustomerAccountRepository customerAccountRepository) {
        return customerAccountRepository.all().isEmpty();
    }
}
