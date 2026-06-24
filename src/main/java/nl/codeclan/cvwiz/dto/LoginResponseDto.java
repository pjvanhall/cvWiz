package nl.codeclan.cvwiz.dto;

import java.util.List;

public record LoginResponseDto(String username, String name, String email, List<String> roles) {
}
