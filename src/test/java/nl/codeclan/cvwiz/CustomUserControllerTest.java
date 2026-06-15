package nl.codeclan.cvwiz;

import nl.codeclan.cvwiz.controller.CustomUserController;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserControllerTest {

    @Test
    void standaloneApiUserCreationEndpointIsNotExposed() {
        assertThat(Arrays.stream(CustomUserController.class.getDeclaredMethods())
                .flatMap(method -> Arrays.stream(method.getAnnotationsByType(PostMapping.class)))
                .flatMap(mapping -> Arrays.stream(mapping.value())))
                .doesNotContain("/nieuweGebruiker");
    }
}
