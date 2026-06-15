package nl.codeclan.cvwiz.dto;

import jakarta.validation.constraints.Size;

import java.util.Map;

public record TechniekMatrixDto(Long id, @Size(max = 100) Map<String, Map<String, Integer>> matrix) {
}
