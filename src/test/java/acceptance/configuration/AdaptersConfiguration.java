package acceptance.configuration;

import com.wealcome.testbdd.adapters.InMemoryAlertNotificationAdapter;
import com.wealcome.testbdd.applicationVTC.port.gateways.AlertNotificationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AdaptersConfiguration {

    @Bean
    @Scope("cucumber-glue")
    public AlertNotificationAdapter alertNotificationAdapter() {
        return new InMemoryAlertNotificationAdapter();
    }
}
