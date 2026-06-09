package nl.codeclan.cvwiz;


import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.ErvaringDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.dto.TechniekMatrixDto;
import nl.codeclan.cvwiz.mapper.CVMapper;
import nl.codeclan.cvwiz.mapper.ConsultantMapper;
import nl.codeclan.cvwiz.model.Consultant;
import nl.codeclan.cvwiz.model.Cv;
import nl.codeclan.cvwiz.model.Experience;
import nl.codeclan.cvwiz.model.SkillMatrix;
import nl.codeclan.cvwiz.repository.ConsultantRepository;
import nl.codeclan.cvwiz.service.ConsultantService;
import nl.codeclan.cvwiz.service.CvService;
import nl.codeclan.cvwiz.service.ExperienceService;
import nl.codeclan.cvwiz.service.SkillMatrixService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConsultantServiceTest {

    @InjectMocks
    private ConsultantService service;


    @Mock
    ConsultantRepository repo;
    @Mock
    CvService cvService;
    @Mock
    ExperienceService expSer;
    @Mock
    SkillMatrixService skms;
    @Mock
    UUID id;
    @Mock
    UUID id1;
    @Mock
    Consultant consultant;
    @Mock
    Cv cv;
    @Mock
    SkillMatrix skim;
    @Mock
    Experience exp;
    @Mock
    List<Experience> expList;
    @Mock
    List<Cv> cvList;
    @Mock
    Consultant consultant1;
    @Mock
    Cv cv1;
    @Mock
    SkillMatrix skim1;
    @Mock
    List<Experience> expList1;
    @Mock
    List<Cv> cvList1;
    @Mock
    MedewerkerDto mdw;
    @Mock
    CurriculumVitaeDto cvDto;
    @Mock
    TechniekMatrixDto skimDto;
    @Mock
    ErvaringDto ervDto;
    @Mock
    List<ErvaringDto> ervList;
    @Mock
    List<CurriculumVitaeDto> cvDtoList;
    @Mock
    MedewerkerDto mdw1;
    @Mock
    CurriculumVitaeDto cvDto1;
    @Mock
    TechniekMatrixDto skimDto1;
    @Mock
    ErvaringDto ervDto1;
    @Mock
    List<ErvaringDto> ervList1;
    @Mock
    List<CurriculumVitaeDto> cvDtoList1;
    @Mock
    MedewerkerDto mdw2;

    @BeforeEach
    public void init() {
        id = UUID.fromString("f8861001-69e5-4a55-bb50-4fb935242684");
        id1 = UUID.fromString("d5f30ce2-078f-439e-88a9-45ff4bbe1964");
        List<String> s = List.of("React", "Angular", "Spring Boot", "Java");
        Map<String, Map<String, Integer>> categories = new HashMap<>();
        Map<String, Integer> fronTech = new HashMap<>();
        fronTech.put("NPM", 0);
        fronTech.put("ngBootstrap", 0);
        categories.put("Frontend Technologies", fronTech);
        skim = new SkillMatrix(1L, categories);
        exp = new Experience(1L, "bedrijf.bv", "30-10-2021 t/m 30-04-2026", "directeur", "branche", "heel veel", "gevaarlijk", "overleven");
        expList = List.of(exp);
        cv = new Cv(1L, "Cv van Kees", s, "profiel van Kees", "Opleiding van Kees", skim, expList);
        cvList = List.of(cv);
        consultant = new Consultant(id1, "Kees", "van der Plas", "0123-456789", "kees.vd.plas@iets.nl", cv, cvList);
        skim1 = new SkillMatrix(2L, categories);
        expList1 = List.of(exp);
        cv1 = new Cv(2L, "Cv van Kees", s, "profiel van Kees", "Opleiding van Kees", skim1, expList1);
        cvList1 = List.of(cv1);
        consultant1 = new Consultant(id, "Kees", "van der Plas", "0123-456789", "kees.vd.plas@iets.nl", cv1, cvList1);
        skimDto = new TechniekMatrixDto(1L, categories);
        ervDto = new ErvaringDto(1L, "bedrijf.bv", "30-10-2021 t/m 30-04-2026", "directeur", "branche", "heel veel", "gevaarlijk", "overleven");
        ervList = List.of(ervDto);
        cvDto = new CurriculumVitaeDto(1L, "Cv van Kees", s, "profiel van Kees", "Opleiding van Kees", skimDto, ervList);
        cvDtoList = List.of(cvDto);
        mdw = new MedewerkerDto("Kees", id1.toString(), "van der Plas", "0123-456789", "kees.vd.plas@iets.nl", cvDto, cvDtoList);
        skimDto1 = new TechniekMatrixDto(2L, categories);
        ervDto1 = new ErvaringDto(2L, "bedrijf.bv", "30-10-2021 t/m 30-04-2026", "directeur", "branche", "heel veel", "gevaarlijk", "overleven");
        ervList1 = List.of(ervDto1);
        cvDto1 = new CurriculumVitaeDto(2L, "Cv van Kees", s, "profiel van Kees", "Opleiding van Kees", skimDto1, ervList1);
        cvDtoList1 = List.of(cvDto1);
        mdw1 = new MedewerkerDto("Kees", id.toString(), "van der Plas", "0123-456789", "kees.vd.plas@iets.nl", cvDto1, cvDtoList1);
    }

    @Test
    public void createNewConsultantTest() throws FileNotFoundException {
        MockedStatic<UUID> uuid = mockStatic(UUID.class);
        uuid.when(UUID::randomUUID).thenReturn(id).thenReturn(id).thenReturn(id1);
        MockedStatic<ConsultantMapper> cons = mockStatic(ConsultantMapper.class);
        cons.when(() -> ConsultantMapper.mapConsultantToConsultantDto(any())).thenReturn(mdw);
        cons.when(() -> ConsultantMapper.mapConsultantDtoToConsultant(any())).thenReturn(consultant);
        MockedStatic<CVMapper> cvm = mockStatic(CVMapper.class);
        cvm.when(() -> CVMapper.mapCVToCVDto(any())).thenReturn(cvDto);
        cvm.when(() -> CVMapper.mapCVDtoToCV(any())).thenReturn(cv);
        when(cvService.createCVForNewConsultant(any())).thenReturn(cvDto);
        when(repo.save(any())).thenReturn(consultant);
        when(repo.existsById(id)).thenReturn(true);
        when(repo.existsById(id1)).thenReturn(false);

        MedewerkerDto m = service.createNewConsultant(mdw);

        verify(repo, times(1)).save(any());
        verify(repo, times(3)).existsById(any());
        assertThat(m).isEqualTo(mdw);
        cons.close();
        uuid.close();
    }

    @Test
    public void updateConsultantWhenOriginalCvAndUsedCvListDoesNotEqualsConsultantCvAndCVListTest() throws FileNotFoundException {
        MockedStatic<ConsultantMapper> cons = mockStatic(ConsultantMapper.class);
        cons.when(() -> ConsultantMapper.mapConsultantToConsultantDto(any())).thenReturn(mdw1);
        cons.when(() -> ConsultantMapper.mapConsultantDtoToConsultant(any())).thenReturn(consultant1);
        when(repo.existsById(id)).thenReturn(true);
        when(repo.getReferenceById(id)).thenReturn(consultant);
        when(cvService.cvExist(any())).thenReturn(true);
        when(cvService.updateCV(any())).thenReturn(cvDto);

        MedewerkerDto m = service.updateConsultant(mdw1);
        verify(repo, times(1)).save(any());
        verify(repo, times(1)).existsById(any());
        verify(repo, times(1)).getReferenceById(any());
        assertThat(m.getId()).isEqualTo(mdw1.getId());
        assertThat(m.getOrgineleCv()).isEqualTo(mdw1.getOrgineleCv());
        assertThat(m.getCvLijst()).isEqualTo(mdw1.getCvLijst());
        assertThat(m.getVoornaam()).isEqualTo(mdw1.getVoornaam());
        assertThat(m.getAchternaam()).isEqualTo(mdw1.getAchternaam());
        assertThat(m.getTelefoon()).isEqualTo(mdw1.getTelefoon());
        assertThat(m.getEmailAdres()).isEqualTo(mdw1.getEmailAdres());
        cons.close();

    }

    @Test
    public void updateConsultantWhenOriginalCvAndUsedCvListDoesNotEqualsConsultantCvAndCvAndCvIdDoesNotExistListTest() throws FileNotFoundException {
        MockedStatic<ConsultantMapper> cons = mockStatic(ConsultantMapper.class);
        cons.when(() -> ConsultantMapper.mapConsultantToConsultantDto(any())).thenReturn(mdw1);
        cons.when(() -> ConsultantMapper.mapConsultantDtoToConsultant(any())).thenReturn(consultant1);
        when(repo.existsById(id)).thenReturn(true);
        when(repo.getReferenceById(id)).thenReturn(consultant);
        when(cvService.cvExist(any())).thenReturn(false);
        when(cvService.createNewCv(any())).thenReturn(cvDto);
        when(cvService.getCvById(any())).thenReturn(cvDto);

        MedewerkerDto m = service.updateConsultant(mdw1);
        verify(repo, times(1)).save(any());
        verify(repo, times(1)).existsById(any());
        verify(repo, times(1)).getReferenceById(any());
        assertThat(m.getId()).isEqualTo(mdw1.getId());
        assertThat(m.getOrgineleCv()).isEqualTo(mdw1.getOrgineleCv());
        assertThat(m.getCvLijst()).isEqualTo(mdw1.getCvLijst());
        assertThat(m.getVoornaam()).isEqualTo(mdw1.getVoornaam());
        assertThat(m.getAchternaam()).isEqualTo(mdw1.getAchternaam());
        assertThat(m.getTelefoon()).isEqualTo(mdw1.getTelefoon());
        assertThat(m.getEmailAdres()).isEqualTo(mdw1.getEmailAdres());
        cons.close();

    }

    @Test
    public void updateConsultantFieldsDoNotEqualsConsultantTest() throws FileNotFoundException {
        MockedStatic<ConsultantMapper> cons = mockStatic(ConsultantMapper.class);
        cons.when(() -> ConsultantMapper.mapConsultantToConsultantDto(any())).thenReturn(mdw);
        cons.when(() -> ConsultantMapper.mapConsultantDtoToConsultant(any())).thenReturn(consultant);
        MockedStatic<CVMapper> cvs = mockStatic(CVMapper.class);
        cvs.when(() -> CVMapper.mapCVDtoToCV(any())).thenReturn(cv);
        cvs.when(() -> CVMapper.CollectorCvDtoListToCvList(any())).thenReturn(cvList);
        when(repo.existsById(any())).thenReturn(true);
        when(repo.getReferenceById(any())).thenReturn(consultant);

        MedewerkerDto m = service.updateConsultant(mdw);
        verify(repo, times(1)).save(any());
        verify(repo, times(1)).existsById(any());
        assertThat(m.getId()).isEqualTo(mdw.getId());
        assertThat(m.getOrgineleCv()).isEqualTo(mdw.getOrgineleCv());
        assertThat(m.getCvLijst()).isEqualTo(mdw.getCvLijst());
        assertThat(m.getVoornaam()).isEqualTo(mdw.getVoornaam());
        assertThat(m.getAchternaam()).isEqualTo(mdw.getAchternaam());
        assertThat(m.getTelefoon()).isEqualTo(mdw.getTelefoon());
        assertThat(m.getEmailAdres()).isEqualTo(mdw.getEmailAdres());
        cons.close();
        cvs.close();
    }

    @Test
    public void updateConsultantThrowsFileNotFoundExceptionTest() {
        when(repo.existsById(any())).thenReturn(false);

        assertThrowsExactly(FileNotFoundException.class, () -> service.updateConsultant(mdw));
    }

    @Test
    public void addNewCvToUsedCvList() {
        when(repo.getReferenceById(any())).thenReturn(consultant1);
        MockedStatic<CVMapper> cvs = mockStatic(CVMapper.class);
        List<CurriculumVitaeDto> list2 = List.of(cvDto1, cvDto);
        mdw2 = mdw1;
        mdw2.setCvLijst(list2);
        cvs.when(() -> CVMapper.CollectorCvDtoListToCvList(any())).thenReturn(cvList);
        MockedStatic<ConsultantMapper> cons = mockStatic(ConsultantMapper.class);
        cons.when(() -> ConsultantMapper.mapConsultantToConsultantDto(any())).thenReturn(mdw2);

        MedewerkerDto m = service.addNewCvToUsedCVList(mdw.getId(), cvDto1);
        verify(repo, times(1)).save(any());
        verify(repo, times(1)).getReferenceById(any());
        assertThat(m.getId()).isEqualTo(mdw1.getId());
        assertThat(m.getCvLijst().size()).isEqualTo(mdw1.getCvLijst().size());
        cvs.close();
        cons.close();
    }

    @Test
    public void getConsultantTest() throws FileNotFoundException {
        when(repo.existsById(any())).thenReturn(true);
        MockedStatic<ConsultantMapper> cons = mockStatic(ConsultantMapper.class);
        cons.when(() -> ConsultantMapper.mapConsultantToConsultantDto(any())).thenReturn(mdw);

        MedewerkerDto m = service.getConsultant(mdw.getId());
        verify(repo, times(1)).existsById(any());
        assertThat(m.getId()).isEqualTo(mdw.getId());
        cons.close();
    }

    @Test
    public void getConsultantThrowsFileNotFoundExceptionTest() {
        when(repo.existsById(any())).thenReturn(false);
        assertThrowsExactly(FileNotFoundException.class, () -> service.getConsultant(mdw.getId()));
    }

    @Test
    public void getConsultantByNameTest() throws FileNotFoundException {
        when(repo.getByFirstnameAndLastname(any(), any())).thenReturn(Optional.ofNullable(consultant));
        MockedStatic<ConsultantMapper> cons = mockStatic(ConsultantMapper.class);
        cons.when(() -> ConsultantMapper.mapConsultantToConsultantDto(any())).thenReturn(mdw);

        MedewerkerDto m = service.getConsultantByName("Kees", "van der Plas");
        verify(repo, times(1)).getByFirstnameAndLastname(any(), any());
        assertThat(m.getId()).isEqualTo(mdw.getId());
        cons.close();
    }

    @Test
    public void getConsultantByNameThrowsFileNotFoundExceptionTest() {
        when(repo.getByFirstnameAndLastname(any(), any())).thenReturn(Optional.empty());
        assertThrowsExactly(FileNotFoundException.class, () -> service.getConsultantByName("Piet", "van der Plas"));
    }

    @Test
    public void deleteConsultantTest() throws FileNotFoundException {
        when(repo.existsById(any())).thenReturn(true);
        service.deleteConsultant(mdw);
        verify(repo, times(1)).deleteById(any());
    }

    @Test
    public void deleteConsultantThrowsFileNotFoundExceptionTest() {
        when(repo.existsById(any())).thenReturn(false);
        assertThrowsExactly(FileNotFoundException.class, () -> service.deleteConsultant(mdw));
    }
}
