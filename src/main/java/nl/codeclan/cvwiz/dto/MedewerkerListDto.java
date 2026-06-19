package nl.codeclan.cvwiz.dto;

public record MedewerkerListDto(
    String id,
    String voornaam,
    String achternaam,
    String telefoon,
    String emailAdres,
    boolean hasCv
) {}
