package nl.codeclan.cvwiz.mapper;


import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.dto.MedewerkerListDto;
import nl.codeclan.cvwiz.model.Consultant;

import java.util.UUID;


public class ConsultantMapper {


    public static Consultant mapConsultantDtoToConsultant(MedewerkerDto dto) {
        return new Consultant(UUID.fromString(dto.getId()), dto.getVoornaam(), dto.getAchternaam(), dto.getTelefoon(), dto.getEmailAdres(), CVMapper.mapCVDtoToCV(dto.getOrgineleCv()), CVMapper.CollectorCvDtoListToCvList(dto.getCvLijst()));
    }

    public static MedewerkerDto mapConsultantToConsultantDto(Consultant c) {
        return new MedewerkerDto(c.getFirstname(), c.getConsultantId().toString(), c.getLastname(), c.getTelephone(), c.getEmail(), CVMapper.mapCVToCVDto(c.getOriginalCV()), CVMapper.CollectorCvListToCvDtoList(c.getUsedCvs()));
    }

    public static MedewerkerListDto mapConsultantToListDto(Consultant c) {
        return new MedewerkerListDto(c.getConsultantId().toString(), c.getFirstname(), c.getLastname(), c.getTelephone(), c.getEmail());
    }

}
