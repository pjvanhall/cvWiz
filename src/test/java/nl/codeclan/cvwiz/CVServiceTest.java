package nl.codeclan.cvwiz;


import nl.codeclan.cvwiz.dto.CurriculumVitaeDto;
import nl.codeclan.cvwiz.dto.ErvaringDto;
import nl.codeclan.cvwiz.dto.MedewerkerDto;
import nl.codeclan.cvwiz.dto.TechniekMatrixDto;
import nl.codeclan.cvwiz.mapper.CVMapper;
import nl.codeclan.cvwiz.mapper.ExperienceMapper;
import nl.codeclan.cvwiz.mapper.SkillMatrixMapper;
import nl.codeclan.cvwiz.model.Cv;
import nl.codeclan.cvwiz.model.Experience;
import nl.codeclan.cvwiz.model.SkillMatrix;
import nl.codeclan.cvwiz.repository.CvRepository;
import nl.codeclan.cvwiz.service.CvService;
import nl.codeclan.cvwiz.service.ExperienceService;
import nl.codeclan.cvwiz.service.SkillMatrixService;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileNotFoundException;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CVServiceTest {

    @InjectMocks
    private CvService service;

    @Mock
    private ExperienceService expSer;
    @Mock
    private SkillMatrixService skms;
    @Mock
    private CvRepository cvRepo;
    @Mock
    private ErvaringDto edto;
    @Mock
    private ErvaringDto edto1;
    @Mock
    private ErvaringDto edto2;
    @Mock
    private Experience exp;
    @Mock
    private Experience exp1;
    @Mock
    private SkillMatrix skim1;
    @Mock
    private TechniekMatrixDto tmdto;
    @Mock
    private SkillMatrix skim2;
    @Mock
    private TechniekMatrixDto tmdto1;
    @Mock
    private Cv cv1;
    @Mock
    private Cv cv2;
    @Mock
    private CurriculumVitaeDto cvdto;
    @Mock
    private CurriculumVitaeDto cvdto1;
    @Mock
    private CurriculumVitaeDto cvdto2;
    final List<String> s = new ArrayList<>();
    final List<Experience> expList = new ArrayList<>();
    final List<ErvaringDto> ervList = new ArrayList<>();
    final UUID uuid = UUID.randomUUID();

    @BeforeEach
    public void init() {
        edto1 = new ErvaringDto(1L, "bedrijf.bv", "30-10-2021 t/m 30-04-2026", "directeur", "branche", "heel veel", "gevaarlijk", "overleven");
        edto2 = new ErvaringDto(2L, "ander bedrijf.bv", "30-10-2021 t/m 30-04-2026", "concierge", "schoonmaak", "heel veel poetsen", "vies", "schoonmaken");
        exp = new Experience(1L, "bedrijf.bv", "30-10-2021 t/m 30-04-2026", "directeur", "branche", "heel veel", "gevaarlijk", "overleven");
        exp1 = new Experience(2L, "ander bedrijf.bv", "30-10-2021 t/m 30-04-2026", "concierge", "schoonmaak", "heel veel poetsen", "vies", "schoonmaken");
        Map<String, Map<String, Integer>> categories = getStringMapMap();
        tmdto = new TechniekMatrixDto(1L, categories);
        skim1 = new SkillMatrix(1L, categories);
        s.add("HTML");
        s.add("CSS");
        expList.add(exp);
        ervList.add(edto1);
        cv1 = new Cv(1L, "cv van Kees", s, "profiel van Kees", "Opleiding van Kees", skim1, expList);
        cvdto = new CurriculumVitaeDto(1L, "cv van Kees ", s, "profiel van Kees", "Opleiding van Kees", tmdto, ervList);
        cvdto2 = new CurriculumVitaeDto(1L, "cv van Kees ", s, "profiel van Kees", "Opleiding van Kees", tmdto, ervList);
        cvdto1 = new CurriculumVitaeDto(1L, "cv van Kees van der Plas", s, "profiel van Kees van der Plas", "Opleidingen van Kees", tmdto, ervList);
    }

    private static @NonNull Map<String, Map<String, Integer>> getStringMapMap() {
        Map<String, Map<String, Integer>> categories = new HashMap<>();
        Map<String, Integer> fronTech = new HashMap<>();
        fronTech.put("HTML", 0);
        fronTech.put("CSS", 0);
        fronTech.put("SCSS", 0);
        fronTech.put("LESS", 0);
        fronTech.put("SASS", 0);
        fronTech.put("Vue", 0);
        fronTech.put("React", 0);
        fronTech.put("Angular", 0);
        fronTech.put("AngularJS", 0);
        fronTech.put("ExtJS", 0);
        fronTech.put("RxJS", 0);
        fronTech.put("Flutter", 0);
        fronTech.put("Tailwind", 0);
        fronTech.put("Bootstrap", 0);
        fronTech.put("Storybook", 0);
        fronTech.put("Vite", 0);
        fronTech.put("NPM", 0);
        fronTech.put("ngBootstrap", 0);
        categories.put("Frontend Technologies", fronTech);
        return categories;
    }


    @Test
    public void createCVForNewConsultant() throws FileNotFoundException {
        when(expSer.createExperience(null)).thenReturn(edto);
        when(skms.createNewSkillMatrixOfBaseMatrix()).thenReturn(tmdto);
        when(cvRepo.save(any())).thenReturn(cv1);
        MockedStatic<SkillMatrixMapper> map = mockStatic(SkillMatrixMapper.class);
        map.when(() -> SkillMatrixMapper.mapSkillMatrixToDto(any())).thenReturn(tmdto);
        MockedStatic<ExperienceMapper> mockedStatic = Mockito.mockStatic(ExperienceMapper.class);
        mockedStatic.when(() -> ExperienceMapper.ExperienceToExperienceDto(any())).thenReturn(edto);
        mockedStatic.when(() -> ExperienceMapper.ExperienceDtoToExperience(any())).thenReturn(exp);
        MockedStatic<CVMapper> cvm = mockStatic(CVMapper.class);
        cvm.when(() -> CVMapper.mapCVDtoToCV(any())).thenReturn(cv1);

        service.createCVForNewConsultant(cvdto);

        verify(cvRepo, times(1)).save(any());
        map.close();
        mockedStatic.close();
        cvm.close();
    }

    @Test
    public void createNewCvTest() {
        when(cvRepo.count()).thenReturn(0L);
        MockedStatic<CVMapper> cvm = mockStatic(CVMapper.class);
        cvm.when(() -> CVMapper.mapCVDtoToCV(any())).thenReturn(cv1);
        cvm.when(() -> CVMapper.mapCVToCVDto(any())).thenReturn(cvdto);

        CurriculumVitaeDto d = service.createNewCv(cvdto);

        verify(cvRepo, times(1)).save(any());
        verify(cvRepo, times(1)).count();
        assertThat(d.getId()).isEqualTo(1L);
        cvm.close();
    }

    @Test
    public void getCvByIdTest() throws FileNotFoundException {
        when(cvRepo.existsById(any())).thenReturn(true);
        when(cvRepo.getReferenceById(any())).thenReturn(cv1);
        MockedStatic<CVMapper> cvm = mockStatic(CVMapper.class);
        cvm.when(() -> CVMapper.mapCVDtoToCV(any())).thenReturn(cv1);
        cvm.when(() -> CVMapper.mapCVToCVDto(any())).thenReturn(cvdto);

        CurriculumVitaeDto d = service.getCvById(1L);

        verify(cvRepo, times(1)).existsById(any());
        verify(cvRepo, times(1)).getReferenceById(any());
        assertThat(d.getId()).isEqualTo(1L);
        cvm.close();
    }

    @Test
    public void getCvByIdThrowsExceptionTest() {
        when(cvRepo.existsById(any())).thenReturn(false);

        assertThrowsExactly(FileNotFoundException.class, () -> service.getCvById(1L));
    }

    @Test
    public void updateCVWhenOnlyCvAttributesAreChangedTest() throws FileNotFoundException {
        when(cvRepo.existsById(any())).thenReturn(true);
        when(cvRepo.getReferenceById(any())).thenReturn(cv1);
        when(cvRepo.save(any())).thenReturn(cv1);
        MockedStatic<CVMapper> cvm = mockStatic(CVMapper.class);
        cvm.when(() -> CVMapper.mapCVDtoToCV(any())).thenReturn(cv1);
        cvm.when(() -> CVMapper.mapCVToCVDto(any())).thenReturn(cvdto).thenReturn(cvdto1);

        CurriculumVitaeDto d = service.updateCV(cvdto1);

        verify(cvRepo, times(1)).save(any());
        verify(cvRepo, times(1)).getReferenceById(any());
        assertThat(d.getId()).isEqualTo(1L);
        assertThat(d.getErvaring()).isEqualTo(cvdto1.getErvaring());
        assertThat(d.getCompetenties()).isEqualTo(cvdto1.getCompetenties());
        assertThat(d.getMatrix()).isEqualTo(cvdto1.getMatrix());
        assertThat(d.getOpleiding()).isEqualTo(cvdto1.getOpleiding());
        assertThat(d.getProfiel()).isEqualTo(cvdto1.getProfiel());
        assertThat(d.getCompetenties()).isEqualTo(cvdto1.getCompetenties());
        assertThat(d.getBestandsNaam()).isEqualTo(cvdto1.getBestandsNaam());
        cvm.close();
    }

    @Test
    public void updateCVWhenOnlyMatrixIsChangedTest() throws FileNotFoundException {
        Map<String, Integer> backTech = new HashMap<>();
        backTech.put("Java", 0);
        backTech.put("Spring Boot", 0);
        Map<String, Map<String, Integer>> categories = skim2.getSkills();
        categories.put("Backend Technologies", backTech);
        tmdto1 = new TechniekMatrixDto(1L, categories);
        cvdto2.setMatrix(tmdto1);
        skim2 = new SkillMatrix(1L, categories);
        cv2 = new Cv(1L, "cv van Kees", s, "profiel van Kees", "Opleiding van Kees", skim2, expList);
        when(cvRepo.existsById(any())).thenReturn(true);
        when(cvRepo.getReferenceById(any())).thenReturn(cv1);
        when(cvRepo.save(any())).thenReturn(cv2);
        MockedStatic<CVMapper> cvm = mockStatic(CVMapper.class);
        cvm.when(() -> CVMapper.mapCVDtoToCV(any())).thenReturn(cv2);
        cvm.when(() -> CVMapper.mapCVToCVDto(any())).thenReturn(cvdto).thenReturn(cvdto2);
        MockedStatic<SkillMatrixMapper> map = mockStatic(SkillMatrixMapper.class);
        map.when(() -> SkillMatrixMapper.mapSkillMatrixToDto(any())).thenReturn(tmdto1);


        CurriculumVitaeDto d = service.updateCV(cvdto2);

        verify(cvRepo, times(1)).save(any());
        verify(cvRepo, times(1)).getReferenceById(any());
        assertThat(d.getId()).isEqualTo(1L);
        assertThat(d.getErvaring()).isEqualTo(cvdto2.getErvaring());
        assertThat(d.getCompetenties()).isEqualTo(cvdto2.getCompetenties());
        assertThat(d.getMatrix()).isEqualTo(cvdto2.getMatrix());
        assertThat(d.getOpleiding()).isEqualTo(cvdto2.getOpleiding());
        assertThat(d.getProfiel()).isEqualTo(cvdto2.getProfiel());
        assertThat(d.getCompetenties()).isEqualTo(cvdto2.getCompetenties());
        assertThat(d.getBestandsNaam()).isEqualTo(cvdto2.getBestandsNaam());
        cvm.close();
        map.close();
    }

    @Test
    public void updateCVWhenOnlyExperienceIsChangedTest() throws FileNotFoundException {
        expList.add(exp1);
        List<ErvaringDto> ervList1 = new ArrayList<>();
        ervList1.add(edto2);
        ervList1.add(edto1);
        cv2 = new Cv(1L, "cv van Kees", s, "profiel van Kees", "Opleiding van Kees", skim1, expList);
        cvdto2.setErvaring(ervList1);
        when(cvRepo.existsById(any())).thenReturn(true);
        when(cvRepo.getReferenceById(any())).thenReturn(cv1);
        when(cvRepo.save(any())).thenReturn(cv2);
        MockedStatic<CVMapper> cvm = mockStatic(CVMapper.class);
        cvm.when(() -> CVMapper.mapCVDtoToCV(any())).thenReturn(cv2);
        cvm.when(() -> CVMapper.mapCVToCVDto(any())).thenReturn(cvdto).thenReturn(cvdto2);
        MockedStatic<ExperienceMapper> mockedStatic = Mockito.mockStatic(ExperienceMapper.class);
        mockedStatic.when(() -> ExperienceMapper.ExperienceToExperienceDto(any())).thenReturn(edto);
        mockedStatic.when(() -> ExperienceMapper.ExperienceDtoToExperience(any())).thenReturn(exp);


        CurriculumVitaeDto d = service.updateCV(cvdto2);

        verify(cvRepo, times(1)).save(any());
        verify(cvRepo, times(1)).getReferenceById(any());
        assertThat(d.getId()).isEqualTo(1L);
        assertThat(d.getErvaring()).isEqualTo(cvdto2.getErvaring());
        assertThat(d.getCompetenties()).isEqualTo(cvdto2.getCompetenties());
        assertThat(d.getMatrix()).isEqualTo(cvdto2.getMatrix());
        assertThat(d.getOpleiding()).isEqualTo(cvdto2.getOpleiding());
        assertThat(d.getProfiel()).isEqualTo(cvdto2.getProfiel());
        assertThat(d.getCompetenties()).isEqualTo(cvdto2.getCompetenties());
        assertThat(d.getBestandsNaam()).isEqualTo(cvdto2.getBestandsNaam());
        cvm.close();
        mockedStatic.close();
    }

    @Test
    public void updateCvThrowsFileNotFoundExceptionTest() {
        when(cvRepo.existsById(any())).thenReturn(false);
        assertThrowsExactly(FileNotFoundException.class, () -> service.updateCV(cvdto2));
    }

    @Test
    public void deleteCvByIdTest() throws FileNotFoundException {
        when(cvRepo.existsById(any())).thenReturn(true);
        when(cvRepo.getReferenceById(any())).thenReturn(cv1);

        service.deleteCvById(1L);

        verify(cvRepo, times(1)).deleteById(1L);
    }

    @Test
    public void deleteCvThrowsFileNotFoundExceptionTest() {
        when(cvRepo.existsById(any())).thenReturn(false);

        assertThrowsExactly(FileNotFoundException.class, () -> service.deleteCvById(1L));
    }

    @Test
    public void updateOriginalCvTest() throws FileNotFoundException {
        List<CurriculumVitaeDto> cvlist = new ArrayList<>();
        cvlist.add(cvdto);
        MedewerkerDto medewerker = new MedewerkerDto("Kees", uuid.toString(), "van der Plas", "0123-456789", "kees.vd.Plas@iest.nl", cvdto, cvlist);
        when(cvRepo.existsById(any())).thenReturn(true);
        when(cvRepo.save(any())).thenReturn(cv2);
        MockedStatic<CVMapper> cvm = mockStatic(CVMapper.class);
        cvm.when(() -> CVMapper.mapCVDtoToCV(any())).thenReturn(cv2);
        cvm.when(() -> CVMapper.mapCVToCVDto(any())).thenReturn(cvdto2);

        CurriculumVitaeDto d = service.updateOriginalCV(medewerker, cvdto2);
        verify(cvRepo, times(1)).save(any());
        assertThat(d.getId()).isEqualTo(1L);
        assertThat(d.getErvaring()).isEqualTo(cvdto2.getErvaring());
        assertThat(d.getCompetenties()).isEqualTo(cvdto2.getCompetenties());
        assertThat(d.getMatrix()).isEqualTo(cvdto2.getMatrix());
        assertThat(d.getOpleiding()).isEqualTo(cvdto2.getOpleiding());
        assertThat(d.getProfiel()).isEqualTo(cvdto2.getProfiel());
        assertThat(d.getCompetenties()).isEqualTo(cvdto2.getCompetenties());
        assertThat(d.getBestandsNaam()).isEqualTo(cvdto2.getBestandsNaam());
        cvm.close();
    }

    @Test
    public void updateOriginalCvThrowsFileNotFoundExceptionWhenDtoIdDoesNotEqualsMedewerkerOriginalCvIdTest() {
        List<CurriculumVitaeDto> cvlist = new ArrayList<>();
        cvlist.add(cvdto);
        cvdto2.setId(2L);
        MedewerkerDto medewerker = new MedewerkerDto("Kees", uuid.toString(), "van der Plas", "0123-456789", "kees.vd.Plas@iest.nl", cvdto, cvlist);
        when(cvRepo.existsById(any())).thenReturn(true);

        assertThrowsExactly(FileNotFoundException.class, () -> service.updateOriginalCV(medewerker, cvdto2));
    }

    @Test
    public void updateOriginalCvThrowsFileNotFoundExceptionWhenDtoIdEqualsMedewerkerOriginalCvIdTest() {
        List<CurriculumVitaeDto> cvlist = new ArrayList<>();
        cvlist.add(cvdto);
        MedewerkerDto medewerker = new MedewerkerDto("Kees", uuid.toString(), "van der Plas", "0123-456789", "kees.vd.Plas@iest.nl", cvdto, cvlist);
        when(cvRepo.existsById(any())).thenReturn(false);

        assertThrowsExactly(FileNotFoundException.class, () -> service.updateOriginalCV(medewerker, cvdto2));
    }

    @Test
    public void cvExistsReturnsTrueTest() {
        when(cvRepo.existsById(1L)).thenReturn(true);

        boolean b = service.cvExist(1L);

        assertThat(b).isTrue();
    }

    @Test
    public void cvExistsReturnsFalseTest() {
        when(cvRepo.existsById(1L)).thenReturn(false);

        boolean b = service.cvExist(1L);

        assertThat(b).isFalse();
    }
}
