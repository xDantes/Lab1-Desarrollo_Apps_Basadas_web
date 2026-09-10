package cr.ac.una.lab1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

@EnabledIf("cr.ac.una.lab1.DockerUtils#isDockerAvailable")
class Lab1ApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }
}
