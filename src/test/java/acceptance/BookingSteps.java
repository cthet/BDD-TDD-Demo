package acceptance;

import com.wealcome.testbdd.applicationVTC.port.gateways.AlertNotificationAdapter;
import com.wealcome.testbdd.applicationVTC.port.gateways.AuthenticationGateway;
import com.wealcome.testbdd.applicationVTC.port.repositories.BookingRepository;
import com.wealcome.testbdd.applicationVTC.port.repositories.CustomerAccountRepository;
import com.wealcome.testbdd.applicationVTC.port.repositories.VTCRepository;
import com.wealcome.testbdd.applicationVTC.service.BookVTC;
import com.wealcome.testbdd.domain.Booking;
import com.wealcome.testbdd.domain.Customer;
import com.wealcome.testbdd.domain.VTC;
import io.cucumber.java8.En;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class BookingSteps implements En {

    public BookingSteps(VTCRepository vtcRepository,
                        BookingRepository bookingRepository,
                        CustomerAccountRepository customerAccountRepository,
                        AuthenticationGateway authenticationGateway,
                        AlertNotificationAdapter alertNotificationAdapter) {

        final BookVTC bookVTC = new BookVTC(customerAccountRepository, bookingRepository,authenticationGateway, alertNotificationAdapter);
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final PrintStream originalOut = System.out;

        BookingAttempt bookingAttempt = new BookingAttempt();

        When("^je tente de réserver le VTC \"([^\"]*)\" de \"([^\"]*)\" à \"([^\"]*)\"$",
                (String firstName, String startPoint, String destinationPoint) -> {
                    vtcRepository.all().stream().filter(vtc -> vtc.getFirstName().equals(firstName)).forEach(vtc -> {
                        System.setOut(new PrintStream(outContent));
                        bookVTC.handle(vtc, startPoint, destinationPoint);
                        Optional<Customer> optCustomer = authenticationGateway.currentCustomer();
                        optCustomer.ifPresent(bookingAttempt::setCustomer);
                        bookingAttempt.setVTC(vtc);
                        bookingAttempt.setStartPoint(startPoint);
                        bookingAttempt.setDestinationPoint(destinationPoint);
                    });
                });

        Then("^la réservation est effective$", () -> {
            Set<Booking> bookings = bookingRepository.all();
            assertEquals(1, bookings.size());
            assertEquals(new Booking(bookingAttempt.customer, bookingAttempt.vtc,
                    bookingAttempt.startPoint, bookingAttempt.destinationPoint), bookings.iterator().next());
        });

        Then("^la réservation n'est pas effective$", () -> {
            Set<Booking> bookings = bookingRepository.all();
            assertEquals(0, bookings.size());
        });
        And("^et une alerte pour insuffisance de solde se lève$", () -> {
            assertEquals("Alerte: Solde insuffisant!", outContent.toString().trim());
            System.setOut(originalOut);
        });
        And("^et une alerte pour identification du client impossible se lève$", () -> {
            assertEquals("Alerte: Identification du client impossible!", outContent.toString().trim());
            System.setOut(originalOut);
        });
    }

    private static class BookingAttempt {

        private Customer customer;
        private VTC vtc;
        private String startPoint;
        private String destinationPoint;

        void setCustomer(Customer customer) {
            this.customer = customer;
        }

        void setVTC(VTC VTC) {
            this.vtc = VTC;
        }

        void setStartPoint(String startPoint) {
            this.startPoint = startPoint;
        }

        void setDestinationPoint(String destinationPoint) {
            this.destinationPoint = destinationPoint;
        }
    }
}
