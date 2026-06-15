package nl.codeclan.cvwiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDto(
        @NotBlank
        @Size(max = 100)
        String username,

        @NotBlank
        @Size(max = 128)
        String password
) {
}
