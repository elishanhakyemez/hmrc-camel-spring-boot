package co.hmrc.elis.sandbox.camelspringboot.exception;

public class UserAlreadyExistException extends Exception {
    private static final long serialVersionUID = 3936058467073076135L;

    public UserAlreadyExistException(String message) {
        super(message);
    }
}
