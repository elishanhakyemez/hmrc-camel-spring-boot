package co.hmrc.elis.sandbox.camelspringboot.routes;

import co.hmrc.elis.sandbox.camelspringboot.CamelSpringBootApplication;
import co.hmrc.elis.sandbox.camelspringboot.model.User;
import co.hmrc.elis.sandbox.camelspringboot.util.CamelSpringBootConstants;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CamelSpringBootApplication.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CamelRouteTest {
    private static final int TIMEOUT = 5000;

    @LocalServerPort
    private int port;
    @Value("${camel.component.servlet.mapping.context-path}")
    private String contextPath;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT);
        requestFactory.setReadTimeout(TIMEOUT);
        restTemplate.setRequestFactory(requestFactory);
        restTemplate.getMessageConverters().add(0, mappingJackson2HttpMessageConverter());
        return restTemplate;
    }

    protected String createURLWithPort(String uri) {
        return "http://localhost:" + port + contextPath.substring(0, contextPath.length() - 2) + "/" + uri;
    }

    @Nested
    @DisplayName("Camel User Controller Integration Tests")
    class CamelUserControllerIntegrationTests {
        @Test
        @DisplayName("When list all users is called, system should return all the users")
        void lisAllUsers() {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity entity = new HttpEntity(headers);

                ResponseEntity<List<User>> response = restTemplate().exchange(
                        new URI(createURLWithPort("users")),
                        HttpMethod.GET, entity, new ParameterizedTypeReference<List<User>>() {
                        });

                assertAll(
                        () -> assertThat("HTTP Status Code is not OK!",
                                response.getStatusCode(), is(equalTo(HttpStatus.OK))),
                        () -> assertNotNull(response.getBody(), "List of users not returned!"),
                        () -> assertTrue(response.getBody().stream().anyMatch(u -> u.getName().equals("Steve Jobs")), "Steve Jobs NOT found in the list!"),
                        () -> assertTrue(response.getBody().stream().anyMatch(u -> u.getName().equals("Bill Gates")), "Bill Gates NOT found in the list!"),
                        () -> assertTrue(response.getBody().stream().anyMatch(u -> u.getName().equals("Elon Musk")), "Elon Musk NOT found in the list!")
                );
            } catch (Exception exception) {
                exception.printStackTrace();
                fail();
            }
        }

        @Test
        @DisplayName("When user create endpoint called with a valid payload for non-existing user, the user should be created")
        void createUserHappyPathWithValidJSONPayload() {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                User request = new User();
                request.setName("Elishan Hakyemez");
                HttpEntity<User> httpRequest = new HttpEntity<>(request, headers);

                ResponseEntity<User> response = restTemplate().exchange(
                        new URI(createURLWithPort("users")),
                        HttpMethod.POST, httpRequest, User.class);

                assertAll(
                        () -> assertThat("HTTP Status Code is not CREATED!",
                                response.getStatusCode(), is(equalTo(HttpStatus.CREATED))),
                        () -> assertNotNull(response.getBody(), "Created User NOT returned!"),
                        () -> assertNotNull(response.getBody().getId(), "Created User ID NOT generated!"),
                        () -> assertThat("User name NOT equal!",
                                response.getBody().getName(), is(equalTo(request.getName())))
                );
            } catch (Exception exception) {
                exception.printStackTrace();
                fail();
            }
        }

        @Test
        @DisplayName("When user create endpoint called with a invalid payload for non-existing user, the system should return HTTP 400")
        void createUserUnhappyPathWithInvalidJSONPayload() {
            HttpClientErrorException.BadRequest badRequest = Assertions.assertThrows(
                    HttpClientErrorException.BadRequest.class, () -> {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);

                        String invalidJsonRequest = "{\"title\": \"Computer Scientist\"}";

                        HttpEntity<String> httpRequest = new HttpEntity<>(invalidJsonRequest, headers);

                        ResponseEntity<User> response = restTemplate().exchange(
                                new URI(createURLWithPort("users")),
                                HttpMethod.POST, httpRequest, User.class);
                    }
            );
            String errorMessage = badRequest.getResponseBodyAsString();
            assertAll(
                    () -> assertThat("HTTP Status Code is not Bad Request",
                            badRequest.getStatusCode(), is(equalTo(HttpStatus.BAD_REQUEST))),
                    () -> assertThat("Error Message is not " + CamelSpringBootConstants.INVALID_INPUT_ERROR_MESSAGE,
                            errorMessage, is(equalTo(CamelSpringBootConstants.INVALID_INPUT_ERROR_MESSAGE)))
            );
        }

        @Test
        @DisplayName("When user create endpoint called with a valid payload for existing user, the system should return HTTP 409")
        void createExistingUserWithValidJSONPayload() {
            HttpClientErrorException.Conflict conflictRequest = Assertions.assertThrows(
                    HttpClientErrorException.Conflict.class, () -> {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);

                        User request = new User();
                        request.setName("Steve Jobs");
                        HttpEntity<User> httpRequest = new HttpEntity<>(request, headers);

                        restTemplate().exchange(
                                new URI(createURLWithPort("users")),
                                HttpMethod.POST, httpRequest, User.class);
                    }
            );
            String errorMessage = conflictRequest.getResponseBodyAsString();
            assertAll(
                    () -> assertThat("HTTP Status Code is not Conflict",
                            conflictRequest.getStatusCode(), is(equalTo(HttpStatus.CONFLICT))),
                    () -> assertThat("Error Message is not " + CamelSpringBootConstants.USER_ALREADY_EXIST_ERROR_MESSAGE,
                            errorMessage, is(equalTo(CamelSpringBootConstants.USER_ALREADY_EXIST_ERROR_MESSAGE)))
            );
        }
    }
}
