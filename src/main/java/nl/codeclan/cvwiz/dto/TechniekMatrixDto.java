package nl.codeclan.cvwiz.dto;

import java.util.Map;

public record TechniekMatrixDto(Long id, Map<String, Map<String, Integer>> matrix) {
}
