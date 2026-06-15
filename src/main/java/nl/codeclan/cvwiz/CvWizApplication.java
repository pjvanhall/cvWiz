package nl.codeclan.cvwiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.function.BiFunction;

@SpringBootApplication
public class CvWizApplication {

    static BiFunction<Class<?>, String[], ConfigurableApplicationContext> applicationRunner = SpringApplication::run;

    public static void main(String[] args) {
        applicationRunner.apply(CvWizApplication.class, args);
    }

}
