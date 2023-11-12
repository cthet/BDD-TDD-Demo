package unit;

import com.wealcome.testbdd.adapters.InMemoryAuthenticationGateway;
import com.wealcome.testbdd.adapters.InMemoryBookingRepository;
import com.wealcome.testbdd.adapters.InMemoryCustomerAccountRepository;
import com.wealcome.testbdd.domain.Booking;
import com.wealcome.testbdd.domain.Customer;
import com.wealcome.testbdd.domain.CustomerAccount;
import com.wealcome.testbdd.domain.VTC;
import com.wealcome.testbdd.domain.gateways.AuthenticationGateway;
import com.wealcome.testbdd.domain.repositories.BookingRepository;
import com.wealcome.testbdd.domain.repositories.CustomerAccountRepository;
import com.wealcome.testbdd.usecases.BookVTC;
import de.bechte.junit.runners.context.HierarchicalContextRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(HierarchicalContextRunner.class)
public class BookVTCTest {

  private final BookingRepository bookingRepository = new InMemoryBookingRepository();
  private final CustomerAccountRepository customerAccountRepository = new InMemoryCustomerAccountRepository();
  private final VTC marcVTC = new VTC("abc", "Marc", "DUPUIS");
  private static final Customer jeanMichelCustomer = new Customer("def", "Jean-Michel", "DUPONT");
  private static final Customer patrickCustomer = new Customer("ghi", "Patrick", "THOMAS");
  private static final Customer michaelCustomer = new Customer("abc", "Michael", "AZERHAD");
  private static final String ARCHEREAU_PARIS = "43 rue Archereau 75019 Paris";
  private static final String CLISSON_PARIS = "2 rue Clisson 75013 Paris";
  private static final String LAFFITE_PARIS = "21 rue Laffitte 75009 Paris";
  private static final String VICTOR_HUGO_AUBERVILLIERS = "111 avenue Victor Hugo, 93300 Aubervilliers";
  private static final String FLANDRE_PARIS = "2 Avenue de Flandre 75019 Paris";
  private final AuthenticationGateway authenticationGateway = new InMemoryAuthenticationGateway();

  @Test
  public void shouldManageToBookAVTC() {
    authenticationGateway.authenticate(jeanMichelCustomer);
    assertCanBookAVTC(marcVTC, ARCHEREAU_PARIS, CLISSON_PARIS);
  }

  public class IntraMural {

    public class FirstCustomer {
      @Test
      public void shouldChargeCustomerAccountUponBookingWithoutBenefitingCreditNote() {
        authenticationGateway.authenticate(jeanMichelCustomer);
        CustomerAccount initialJeanMichelCustomerAccount = customerAccount(jeanMichelCustomer, 35, 10);
        customerAccountRepository.add(initialJeanMichelCustomerAccount);
        bookVTC(marcVTC, ARCHEREAU_PARIS, CLISSON_PARIS);
        assertThatCustomerAccountIsChargedUponBooking(customerAccount(jeanMichelCustomer, 5, 10));
      }
    }
    public class SecondCustomer {
      @Test
      public void shouldChargeCustomerAccountUponBooking() {
        authenticationGateway.authenticate(patrickCustomer);
        CustomerAccount initialPatrickCustomerAccount = customerAccount(patrickCustomer, 46, 10);
        customerAccountRepository.add(initialPatrickCustomerAccount);
        bookVTC(marcVTC, CLISSON_PARIS, LAFFITE_PARIS);
        assertThatCustomerAccountIsChargedUponBooking(customerAccount(patrickCustomer, 16, 10));
      }
    }
    public class ThirdCustomer {
      @Test
      public void shouldChargeCustomerAccountUponBooking() {
        authenticationGateway.authenticate(michaelCustomer);
        CustomerAccount initialPatrickCustomerAccount = customerAccount(michaelCustomer, 30, 10);
        customerAccountRepository.add(initialPatrickCustomerAccount);
        bookVTC(marcVTC, CLISSON_PARIS, LAFFITE_PARIS);
        assertThatCustomerAccountIsChargedUponBooking(customerAccount(michaelCustomer, 0, 10));
      }
    }
  }

  public class LeavingParis {
    @Test
    public void shouldChargeCustomerAccountUponBookingUsingFullCreditNote() {
      authenticationGateway.authenticate(jeanMichelCustomer);
      CustomerAccount initialJeanMichelCustomerAccount = customerAccount(jeanMichelCustomer, 50, 10);
      customerAccountRepository.add(initialJeanMichelCustomerAccount);
      bookVTC(marcVTC, FLANDRE_PARIS, VICTOR_HUGO_AUBERVILLIERS);
      assertThatCustomerAccountIsChargedUponBooking(customerAccount(jeanMichelCustomer, 10, 0));
    }

    @Test
    public void shouldChargeCustomerAccountUponBookingUsingAnExceedingCreditNote() {
      authenticationGateway.authenticate(jeanMichelCustomer);
      CustomerAccount initialJeanMichelCustomerAccount = customerAccount(jeanMichelCustomer, 50, 51);
      customerAccountRepository.add(initialJeanMichelCustomerAccount);
      bookVTC(marcVTC, FLANDRE_PARIS, VICTOR_HUGO_AUBERVILLIERS);
      assertThatCustomerAccountIsChargedUponBooking(customerAccount(jeanMichelCustomer, 50, 1));
    }

  }


  private void assertCanBookAVTC(VTC vtc, String startPoint, String endPoint) {
    bookVTC(vtc, startPoint, endPoint);
    assertVTCIsBooked(jeanMichelCustomer, vtc, startPoint, endPoint);
  }

  private void bookVTC(VTC vtc, String startPoint, String endPoint) {
    new BookVTC(customerAccountRepository, bookingRepository,authenticationGateway).handle(vtc, startPoint, endPoint);
  }

  private void assertVTCIsBooked(Customer customer, VTC vtc, String startPoint, String endPoint) {
    assertThat(bookingRepository.all(), hasItem(new Booking(customer, vtc, startPoint, endPoint)));
  }

  private CustomerAccount customerAccount(Customer customer, int balance, int creditNote) {
    return new CustomerAccount(customer.getId(), BigDecimal.valueOf(balance), BigDecimal.valueOf(creditNote));
  }

  private void assertThatCustomerAccountIsChargedUponBooking(CustomerAccount expectedCustomerAccount) {
    assertThat(customerAccountRepository.all(), hasItem(expectedCustomerAccount));
  }

}
