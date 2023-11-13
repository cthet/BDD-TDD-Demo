package unit;

import com.wealcome.testbdd.adapters.InMemoryAlertNotificationAdapter;
import com.wealcome.testbdd.adapters.InMemoryAuthenticationGateway;
import com.wealcome.testbdd.adapters.InMemoryBookingRepository;
import com.wealcome.testbdd.adapters.InMemoryCustomerAccountRepository;
import com.wealcome.testbdd.applicationVTC.port.gateways.AlertNotificationAdapter;
import com.wealcome.testbdd.applicationVTC.port.gateways.AuthenticationGateway;
import com.wealcome.testbdd.applicationVTC.port.repositories.BookingRepository;
import com.wealcome.testbdd.applicationVTC.port.repositories.CustomerAccountRepository;
import com.wealcome.testbdd.applicationVTC.service.BookVTC;
import com.wealcome.testbdd.domain.Booking;
import com.wealcome.testbdd.domain.Customer;
import com.wealcome.testbdd.domain.CustomerAccount;
import com.wealcome.testbdd.domain.VTC;
import de.bechte.junit.runners.context.HierarchicalContextRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(HierarchicalContextRunner.class)
public class BookVTCTest {

  private final BookingRepository bookingRepository = new InMemoryBookingRepository();
  private final CustomerAccountRepository customerAccountRepository = new InMemoryCustomerAccountRepository();
  private final AuthenticationGateway authenticationGateway = new InMemoryAuthenticationGateway();
  private final AlertNotificationAdapter alertNotificationAdapter = new InMemoryAlertNotificationAdapter();
  private final VTC marcVTC = new VTC("abc", "Marc", "DUPUIS");
  private static final Customer jeanMichelCustomer = new Customer("def", "Jean-Michel", "DUPONT");
  private static final Customer patrickCustomer = new Customer("ghi", "Patrick", "THOMAS");
  private static final Customer michaelCustomer = new Customer("abc", "Michael", "AZERHAD");
  private static final String ARCHEREAU_PARIS = "43 rue Archereau 75019 Paris";
  private static final String CLISSON_PARIS = "2 rue Clisson 75013 Paris";
  private static final String LAFFITE_PARIS = "21 rue Laffitte 75009 Paris";
  private static final String VICTOR_HUGO_AUBERVILLIERS = "111 avenue Victor Hugo, 93300 Aubervilliers";
  private static final String FLANDRE_PARIS = "2 Avenue de Flandre 75019 Paris";
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @Before
  public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
  }

  @After
  public void restoreStreams() {
    System.setOut(originalOut);
  }



  public class IntraMural {

    @Test
    public void shouldManageToBookAVTCWithOnlyBalance() {
      authenticationGateway.authenticate(jeanMichelCustomer);
      initCustomerAccount(jeanMichelCustomer, 30, 0);
      assertCanBookAVTC(jeanMichelCustomer, marcVTC, ARCHEREAU_PARIS, CLISSON_PARIS);
    }

    @Test
    public void shouldNotManageToBookAVTCWithInsufficientBalance() {
      authenticationGateway.authenticate(jeanMichelCustomer);
      initCustomerAccount(jeanMichelCustomer, 29, 30);
      assertCanNotBookAVTC(jeanMichelCustomer, marcVTC, ARCHEREAU_PARIS, CLISSON_PARIS);
    }

    public class FirstCustomer {

      @Test
      public void shouldChargeCustomerAccountUponBookingWithoutBenefitingCreditNote() {
        authenticationGateway.authenticate(jeanMichelCustomer);
        initCustomerAccount(jeanMichelCustomer, 35, 10);
        bookVTC(marcVTC, ARCHEREAU_PARIS, CLISSON_PARIS);
        assertThatCustomerAccountIsChecked(customerAccount(jeanMichelCustomer, 5, 10));
      }
    }

    public class SecondCustomer{
      @Test
      public void shouldChargeCustomerAccountUponBooking() {
        authenticationGateway.authenticate(patrickCustomer);
        initCustomerAccount(patrickCustomer, 46, 10);
        bookVTC(marcVTC, CLISSON_PARIS, LAFFITE_PARIS);
        assertThatCustomerAccountIsChecked(customerAccount(patrickCustomer, 16, 10));
      }
    }

    public class ThirdCustomer {

      @Test
      public void shouldChargeCustomerAccountUponBooking() {
        authenticationGateway.authenticate(michaelCustomer);
        initCustomerAccount(michaelCustomer, 30, 10);
        bookVTC(marcVTC, CLISSON_PARIS, LAFFITE_PARIS);
        assertThatCustomerAccountIsChecked(customerAccount(michaelCustomer, 0, 10));
      }
    }

    public class CustomerWithInsufficientFunds {

      @Test
      public void shouldNotChargeCustomerAccountIfVTCIsNotBooked() {
        authenticationGateway.authenticate(jeanMichelCustomer);
        initCustomerAccount(jeanMichelCustomer, 0, 10);
        bookVTC(marcVTC, ARCHEREAU_PARIS, CLISSON_PARIS);
        assertThatCustomerAccountIsChecked(customerAccount(jeanMichelCustomer, 0, 10));
      }

      @Test
      public void shouldSendAnAlertForInsufficientBalance() {
        authenticationGateway.authenticate(jeanMichelCustomer);
        initCustomerAccount(jeanMichelCustomer, 0, 10);
        bookVTC(marcVTC, ARCHEREAU_PARIS, CLISSON_PARIS);
        assertThatAnAlertForInsufficientBalanceIsSent();
      }
    }
  }


  public class LeavingParis {

    public class WithoutCreditNote {

      @Test
      public void shouldManageToBookAVTCWithOnlyBalance() {
        authenticationGateway.authenticate(jeanMichelCustomer);
        initCustomerAccount(jeanMichelCustomer, 50, 0);
        assertCanBookAVTC(jeanMichelCustomer, marcVTC, ARCHEREAU_PARIS, VICTOR_HUGO_AUBERVILLIERS);
      }

      @Test
      public void shouldChargeCustomerAccountUponBookingWithoutCreditNote() {
        authenticationGateway.authenticate(patrickCustomer);
        initCustomerAccount(patrickCustomer, 50, 0);
        bookVTC(marcVTC, ARCHEREAU_PARIS, VICTOR_HUGO_AUBERVILLIERS);
        assertThatCustomerAccountIsChecked(customerAccount(patrickCustomer, 0, 0));
      }
    }

    public class WithCreditNote {

      @Test
      public void shouldManageToBookAVTCWithOnlyCreditNote() {
        authenticationGateway.authenticate(jeanMichelCustomer);
        initCustomerAccount(jeanMichelCustomer, 0, 50);
        assertCanBookAVTC(jeanMichelCustomer, marcVTC, ARCHEREAU_PARIS, VICTOR_HUGO_AUBERVILLIERS);
      }

      @Test
      public void shouldManageToBookAVTCWithBalanceAndCreditNote() {
        authenticationGateway.authenticate(jeanMichelCustomer);
        initCustomerAccount(jeanMichelCustomer, 50, 10);
        assertCanBookAVTC(jeanMichelCustomer, marcVTC, ARCHEREAU_PARIS, VICTOR_HUGO_AUBERVILLIERS);
      }

      @Test
      public void shouldChargeCustomerAccountUponBookingUsingFullCreditNote() {
        authenticationGateway.authenticate(michaelCustomer);
        CustomerAccount initialMichaelCustomerAccount = customerAccount(michaelCustomer, 50, 10);
        customerAccountRepository.add(initialMichaelCustomerAccount);
        bookVTC(marcVTC, FLANDRE_PARIS, VICTOR_HUGO_AUBERVILLIERS);
        assertThatCustomerAccountIsChecked(customerAccount(michaelCustomer, 10, 0));
      }

      @Test
      public void shouldChargeCustomerAccountUponBookingUsingAnExceedingCreditNote() {
        authenticationGateway.authenticate(michaelCustomer);
        CustomerAccount initialMichaelCustomerAccount = customerAccount(michaelCustomer, 50, 51);
        customerAccountRepository.add(initialMichaelCustomerAccount);
        bookVTC(marcVTC, FLANDRE_PARIS, VICTOR_HUGO_AUBERVILLIERS);
        assertThatCustomerAccountIsChecked(customerAccount(michaelCustomer, 50, 1));
      }
    }
  }

  public class EnteringParis {
    @Test
    public void shouldManageToBookAVTC() {
      authenticationGateway.authenticate(michaelCustomer);
      initCustomerAccount(michaelCustomer, 50, 10);
      assertCanBookAVTC(michaelCustomer, marcVTC, VICTOR_HUGO_AUBERVILLIERS, ARCHEREAU_PARIS);
    }

    @Test
    public void shouldBeFreeWithoutAlteringCreditNote() {
      authenticationGateway.authenticate(michaelCustomer);
      CustomerAccount initialMichaelCustomerAccount = customerAccount(michaelCustomer, 50, 10);
      customerAccountRepository.add(initialMichaelCustomerAccount);
      bookVTC(marcVTC, VICTOR_HUGO_AUBERVILLIERS, ARCHEREAU_PARIS);
      assertThatCustomerAccountIsChecked(customerAccount(michaelCustomer, 50, 10));
    }
  }

  @Test
  public void shouldSendAnAlertIfCustomerIsNotAuthenticated() {
    bookVTC(marcVTC, ARCHEREAU_PARIS, CLISSON_PARIS);
    assertThatAnAlertForCustomerNotAuthenticatedIsSent();
  }



  private void initCustomerAccount(Customer customer, int balance, int creditNote){
    CustomerAccount customerAccount = customerAccount(customer, balance, creditNote);
    customerAccountRepository.add(customerAccount);
  }


  private void assertCanBookAVTC(Customer customer, VTC vtc, String startPoint, String endPoint) {
    bookVTC(vtc, startPoint, endPoint);
    assertVTCIsBooked(customer, vtc, startPoint, endPoint);
  }


  private void assertCanNotBookAVTC(Customer customer, VTC vtc, String startPoint, String endPoint) {
    bookVTC(vtc, startPoint, endPoint);
    assertVTCIsNotBooked(customer, vtc, startPoint, endPoint);
  }

  private void bookVTC(VTC vtc, String startPoint, String endPoint) {
    new BookVTC(customerAccountRepository, bookingRepository,authenticationGateway, alertNotificationAdapter).handle(vtc, startPoint, endPoint);
  }

  private void assertVTCIsBooked(Customer customer, VTC vtc, String startPoint, String endPoint) {
    assertThat(bookingRepository.all(), hasItem(new Booking(customer, vtc, startPoint, endPoint)));
  }

  private void assertVTCIsNotBooked(Customer customer, VTC vtc, String startPoint, String endPoint) {
    assertThat(bookingRepository.all(), not(hasItem(new Booking(customer, vtc, startPoint, endPoint))));
  }

  private CustomerAccount customerAccount(Customer customer, int balance, int creditNote) {
    return new CustomerAccount(customer.getId(), BigDecimal.valueOf(balance), BigDecimal.valueOf(creditNote));
  }

  private void assertThatCustomerAccountIsChecked(CustomerAccount expectedCustomerAccount) {
    assertThat(customerAccountRepository.all(), hasItem(expectedCustomerAccount));
  }

  private void assertThatAnAlertForInsufficientBalanceIsSent() {
    assertTrue(outContent.toString().contains("Alerte: Solde insuffisant!"));
  }

  private void assertThatAnAlertForCustomerNotAuthenticatedIsSent() {
    assertTrue(outContent.toString().contains("Alerte: Identification du client impossible!"));
  }




}
