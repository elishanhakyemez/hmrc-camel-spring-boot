package co.hmrc.elis.sandbox.camelspringboot.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@DisplayName("CamelSpringBootConstants Tests")
class CamelSpringBootConstantsTest {
    @Test
    @DisplayName("Could not be instantiated")
    public void camelSpringBootConstantsCannotBeInstantiated() throws NoSuchMethodException {
        Constructor<CamelSpringBootConstants> declaredConstructor = CamelSpringBootConstants.class.getDeclaredConstructor();
        declaredConstructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, declaredConstructor::newInstance);
    }
}
