package nl.codeclan.cvwiz;

import jakarta.persistence.EntityNotFoundException;

import nl.codeclan.cvwiz.dto.BeheerderDto;
import nl.codeclan.cvwiz.mapper.ManagerMapper;
import nl.codeclan.cvwiz.model.Manager;
import nl.codeclan.cvwiz.repository.ManagerRepository;
import nl.codeclan.cvwiz.service.ManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import java.io.FileNotFoundException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagerServiceTest {

    @Mock
    UUID id;
    @Mock
    UUID id1;
    @Mock
    Manager manager;
    @Mock
    Manager manager1;
    @Mock
    BeheerderDto behDto;
    @Mock
    BeheerderDto behDto1;
    @InjectMocks
    private ManagerService service;
    @Mock
    private ManagerRepository repo;

    @BeforeEach
    public void init() {
        id = UUID.randomUUID();
        id1 = UUID.randomUUID();
        manager = new Manager(id1, "Jan", "op 't Hof", "0122-456789", "j.op.t.hof@iest.nl");
        behDto = new BeheerderDto("Jan", id1.toString(), "op 't Hof", "0122-456789", "j.op.t.hof@iets.nl");
        manager1 = new Manager(id1, "Jan-Kees", "op 't Hof", "0122-456789", "jk.op.t.hof@iest.nl");
        behDto1 = new BeheerderDto("Jan-Kees", id1.toString(), "op 't Hof", "0122-456789", "jk.op.t.hof@iets.nl");

    }

    @Test
    public void createNewManagerTest() {
        MockedStatic<UUID> uuid = mockStatic(UUID.class);
        uuid.when(UUID::randomUUID).thenReturn(id).thenReturn(id1);
        when(repo.existsById(any())).thenReturn(true).thenReturn(false);
        MockedStatic<ManagerMapper> man = mockStatic(ManagerMapper.class);
        man.when(() -> ManagerMapper.managerToManagerDto(any())).thenReturn(behDto);
        man.when(() -> ManagerMapper.managerDtoToManager(any())).thenReturn(manager);
        when(repo.save(any())).thenReturn(manager);

        BeheerderDto d = service.createNewManager(behDto);

        verify(repo, times(2)).existsById(any());
        verify(repo, times(1)).save(any());
        assertThat(d.getId()).isEqualTo(id1.toString());
        assertThat(d.getVoornaam()).isEqualTo(behDto.getVoornaam());
        assertThat(d.getAchternaam()).isEqualTo(behDto.getAchternaam());
        assertThat(d.getTelefoon()).isEqualTo(behDto.getTelefoon());
        assertThat(d.getEmailAdres()).isEqualTo(behDto.getEmailAdres());
        man.close();
        uuid.close();
    }

    @Test
    public void updateManagerTest() throws EntityNotFoundException {
        when(repo.existsById(any())).thenReturn(true);
        MockedStatic<ManagerMapper> man = mockStatic(ManagerMapper.class);
        man.when(() -> ManagerMapper.managerToManagerDto(any())).thenReturn(behDto1);
        man.when(() -> ManagerMapper.managerDtoToManager(any())).thenReturn(manager1);

        BeheerderDto d = service.updateManager(behDto1);

        verify(repo, times(1)).existsById(any());
        verify(repo, times(1)).save(any());
        assertThat(d.getId()).isEqualTo(id1.toString());
        assertThat(d.getVoornaam()).isEqualTo(behDto1.getVoornaam());
        assertThat(d.getAchternaam()).isEqualTo(behDto1.getAchternaam());
        assertThat(d.getTelefoon()).isEqualTo(behDto1.getTelefoon());
        assertThat(d.getEmailAdres()).isEqualTo(behDto1.getEmailAdres());
        man.close();
    }

    @Test
    public void updateManagerThrowsEntityNotFoundException() throws EntityNotFoundException {
        when(repo.existsById(any())).thenReturn(false);
        assertThrowsExactly(EntityNotFoundException.class, () -> service.updateManager(behDto1));
    }

    @Test
    public void getManagerTest() throws FileNotFoundException {
        MockedStatic<ManagerMapper> man = mockStatic(ManagerMapper.class);
        man.when(() -> ManagerMapper.managerToManagerDto(any())).thenReturn(behDto);
        when(repo.existsById(any())).thenReturn(true);
        when(repo.getReferenceById(any())).thenReturn(manager);

        BeheerderDto d = service.getManager(id1);
        verify(repo, times(1)).existsById(any());
        verify(repo, times(1)).getReferenceById(any());
        assertThat(d.getId()).isEqualTo(id1.toString());
        assertThat(d.getVoornaam()).isEqualTo(behDto.getVoornaam());
        assertThat(d.getAchternaam()).isEqualTo(behDto.getAchternaam());
        assertThat(d.getTelefoon()).isEqualTo(behDto.getTelefoon());
        assertThat(d.getEmailAdres()).isEqualTo(behDto.getEmailAdres());
        man.close();
    }

    @Test
    public void getManagerThrowsFileNotFoundException() {
        when(repo.existsById(any())).thenReturn(false);
        assertThrowsExactly(FileNotFoundException.class, () -> service.getManager(id1));
    }

    @Test
    public void deleteManagerTest() throws EntityNotFoundException, FileNotFoundException {
        when(repo.existsById(any())).thenReturn(true);
        service.deleteManager(id);
        verify(repo, times(1)).existsById(any());
        verify(repo, times(1)).deleteById(any());
    }

    @Test
    public void deleteManagerThrowsEntityNotFoundException() {
        when(repo.existsById(any())).thenReturn(false);
        assertThrowsExactly(FileNotFoundException.class, () -> service.deleteManager(id1));
    }


}
