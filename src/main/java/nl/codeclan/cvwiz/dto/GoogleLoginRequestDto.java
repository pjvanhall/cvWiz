package nl.codeclan.cvwiz.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequestDto(
        @NotBlank(message = "ID token is verplicht")
        String idToken
) {
}
