package co.hmrc.elis.sandbox.camelspringboot.service;

import co.hmrc.elis.sandbox.camelspringboot.exception.UserAlreadyExistException;
import co.hmrc.elis.sandbox.camelspringboot.model.User;

import java.util.Collection;

public interface UserService {
    User createUser(User user) throws UserAlreadyExistException;

    Collection<User> findUsers();
}