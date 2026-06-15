package nl.codeclan.cvwiz;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;

class CvWizApplicationTests {

    @Test
    void canConstructApplicationClass() {
        assertThat(new CvWizApplication()).isNotNull();
    }

    @Test
    void mainDelegatesToSpringApplicationRunner() {
        BiFunction<Class<?>, String[], org.springframework.context.ConfigurableApplicationContext> originalRunner = CvWizApplication.applicationRunner;
        AtomicReference<Class<?>> source = new AtomicReference<>();
        AtomicReference<String[]> arguments = new AtomicReference<>();
        String[] args = {"--server.port=0"};
        CvWizApplication.applicationRunner = (applicationClass, applicationArguments) -> {
            source.set(applicationClass);
            arguments.set(applicationArguments);
            return null;
        };

        try {
            CvWizApplication.main(args);
        } finally {
            CvWizApplication.applicationRunner = originalRunner;
        }

        assertThat(source.get()).isEqualTo(CvWizApplication.class);
        assertThat(arguments.get()).isSameAs(args);
    }
}
