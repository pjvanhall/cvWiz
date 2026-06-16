package nl.codeclan.cvwiz.mapper;


import nl.codeclan.cvwiz.dto.BeheerderDto;
import nl.codeclan.cvwiz.model.Manager;

import java.util.UUID;

public class ManagerMapper {

    public static Manager managerDtoToManager(BeheerderDto dto) {
        if (dto.getId() != null) {
            return new Manager(UUID.fromString(dto.getId()), dto.getVoornaam(), dto.getAchternaam(), dto.getTelefoon(), dto.getEmailAdres());
        } else {
            return new Manager(dto.getVoornaam(), dto.getAchternaam(), dto.getTelefoon(), dto.getEmailAdres());
        }
    }

    public static BeheerderDto managerToManagerDto(Manager m) {
        return new BeheerderDto(m.getFirstname(), m.getManagerId().toString(), m.getLastname(), m.getTelephone(), m.getEmail());
    }
}
