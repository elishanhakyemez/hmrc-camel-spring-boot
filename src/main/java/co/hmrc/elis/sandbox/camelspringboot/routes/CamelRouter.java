package co.hmrc.elis.sandbox.camelspringboot.routes;

import co.hmrc.elis.sandbox.camelspringboot.model.User;
import co.hmrc.elis.sandbox.camelspringboot.exception.UserAlreadyExistException;
import co.hmrc.elis.sandbox.camelspringboot.service.UserService;
import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.dataformat.XmlJsonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.Map;

import static co.hmrc.elis.sandbox.camelspringboot.util.CamelSpringBootConstants.*;

@Component
public class CamelRouter extends RouteBuilder {
    @Value("${camel.component.servlet.mapping.context-path}")
    private String contextPath;

    @Autowired
    private Environment environment;

    @Autowired
    private UserService userService;

    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean(new CamelHttpTransportServlet(), contextPath);
        servlet.setName("CamelServlet");
        return servlet;
    }

    @Override
    public void configure() throws JAXBException {
        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .enableCORS(true)
                .port(environment.getProperty("server.port", "8090"))
                .contextPath(contextPath);

        onException(ValidationException.class)
                .log("${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                .handled(true)
                .setBody(simple(INVALID_INPUT_ERROR_MESSAGE));
        onException(UserAlreadyExistException.class)
                .log("${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(409))
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                .handled(true)
                .setBody(simple(USER_ALREADY_EXIST_ERROR_MESSAGE));

        rest("/users").description("User REST service")
                .consumes("application/json")
                .produces("application/json")

                .get()
                .description("Find all users")
                .outType(User[].class)
                .responseMessage().code(200).message("All users successfully returned").endResponseMessage()
                .to("bean:userService?method=findUsers")

                .post()
                .description("Create a user")
                .route()
                .log("Incoming User JSON:\n${body}")
                .log("Validating Incoming User JSON")
                .to(VALIDATE_USER_JSON_ROUTE_NAME)
                .log("User JSON Validated Successfully")
                .log("Transforming User JSON To XML, Validated JSON:\n${body}")
                .to(TRANSFORM_JSON_TO_XML_ROUTE_NAME)
                .log("User JSON Transformed to XML Successfully, Transformed XML:\n${body}")
                .log("Validating User XML")
                .to(VALIDATE_USER_XML_ROUTE_NAME)
                .log("User XML Validated Successfully")
                .log("Converting User XML To POJO")
                .to(CONVERT_XML_TO_POJO_ROUTE_NAME)
                .log("User XML Converted to POJO Successfully")
                .log("Creating User")
                .to(CREATE_USER_ROUTE_NAME)
                .log("User Created Successfully");

        from(VALIDATE_USER_JSON_ROUTE_NAME)
                .marshal().json(JsonLibrary.Jackson, true)
                .to("json-validator:user-schema.json");

        Map<String, String> options = new HashMap<>();
        options.put(XmlJsonDataFormat.ROOT_NAME, "User");
        from(TRANSFORM_JSON_TO_XML_ROUTE_NAME)
                .unmarshal().xmljson(options);

        from(VALIDATE_USER_XML_ROUTE_NAME)
                .to("validator:user-schema.xsd");

        JAXBContext jaxbContext = JAXBContext.newInstance(User.class);
        JaxbDataFormat jaxbDataFormat = new JaxbDataFormat(jaxbContext);
        from(CONVERT_XML_TO_POJO_ROUTE_NAME)
                .unmarshal(jaxbDataFormat);

        from(CREATE_USER_ROUTE_NAME)
                .process(exchange -> {
                    User user = (User) exchange.getIn().getBody();
                    User createdUser = userService.createUser(user);
                    exchange.getIn().setBody(createdUser);
                }).marshal().json(JsonLibrary.Jackson)
                .unmarshal().json(JsonLibrary.Jackson, true)
                .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON_VALUE))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));
    }
}
