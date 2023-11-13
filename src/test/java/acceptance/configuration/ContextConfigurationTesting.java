package acceptance.configuration;


import io.cucumber.java8.En;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.ContextConfiguration;

@CucumberContextConfiguration
@ContextConfiguration(classes = {
        RepositoriesConfiguration.class,
        GatewaysConfiguration.class,
        AdaptersConfiguration.class
})
class ContextConfigurationTesting implements En {

}
