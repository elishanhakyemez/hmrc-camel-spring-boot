package co.hmrc.elis.sandbox.camelspringboot;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {CamelSpringBootApplication.class})
@DisplayName("CamelSpringBootApplication Context Load Test")
@Slf4j
public class CamelSpringBootApplicationTest {
    @Test
    @DisplayName("Context-Load Test")
    public void contextLoads() {
        log.info("CONTEXT_LOADED");
        assertTrue(true);
    }
}