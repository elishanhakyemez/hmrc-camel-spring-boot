package co.hmrc.elis.sandbox.camelspringboot.util;

public class CamelSpringBootConstants {
    public static final String VALIDATE_USER_JSON_ROUTE_NAME = "direct:validate-user-json";
    public static final String TRANSFORM_JSON_TO_XML_ROUTE_NAME = "direct:transform-json-to-xml";
    public static final String VALIDATE_USER_XML_ROUTE_NAME = "direct:validate-user-xml";
    public static final String CONVERT_XML_TO_POJO_ROUTE_NAME = "direct:convert-xml-to-pojo";
    public static final String CREATE_USER_ROUTE_NAME = "direct:createUser";
    public static final String INVALID_INPUT_ERROR_MESSAGE = "{\"errorMessage\": \"Invalid Input\"}";
    public static final String USER_ALREADY_EXIST_ERROR_MESSAGE = "{\"errorMessage\": \"User already exist!\"}";

    private CamelSpringBootConstants() {
        throw new UnsupportedOperationException("CamelSpringBootConstants cannot be instantiated!");
    }
}
