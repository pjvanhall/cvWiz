package nl.codeclan.cvwiz.support;

import nl.codeclan.cvwiz.model.Consultant;
import nl.codeclan.cvwiz.model.CustomUser;
import nl.codeclan.cvwiz.model.Cv;
import nl.codeclan.cvwiz.model.Experience;
import nl.codeclan.cvwiz.model.Manager;
import nl.codeclan.cvwiz.model.SkillMatrix;
import nl.codeclan.cvwiz.repository.ConsultantRepository;
import nl.codeclan.cvwiz.repository.CustomUserRepository;
import nl.codeclan.cvwiz.repository.CvRepository;
import nl.codeclan.cvwiz.repository.ExperienceRepository;
import nl.codeclan.cvwiz.repository.ManagerRepository;
import nl.codeclan.cvwiz.repository.SkillMatrixRepository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Function;

public final class RepositoryDoubles {

    private RepositoryDoubles() {
    }

    public static TestRepository<SkillMatrixRepository, SkillMatrix, Long> skillMatrices() {
        return new TestRepository<>(SkillMatrixRepository.class, SkillMatrix::getSkillMatrixId);
    }

    public static TestRepository<ExperienceRepository, Experience, Long> experiences() {
        return new TestRepository<>(ExperienceRepository.class, Experience::getId);
    }

    public static TestRepository<CvRepository, Cv, Long> cvs() {
        return new TestRepository<>(CvRepository.class, Cv::getCvId);
    }

    public static TestRepository<ManagerRepository, Manager, UUID> managers() {
        return new TestRepository<>(ManagerRepository.class, Manager::getManagerId);
    }

    public static ConsultantTestRepository consultants() {
        return new ConsultantTestRepository();
    }

    public static TestRepository<CustomUserRepository, CustomUser, String> users() {
        return new TestRepository<>(CustomUserRepository.class, CustomUser::getUsername);
    }

    public static class TestRepository<R, T, ID> implements InvocationHandler {
        private final Class<R> repositoryType;
        private final Function<T, ID> idExtractor;
        private final Map<ID, T> entities = new LinkedHashMap<>();
        private final List<T> savedEntities = new ArrayList<>();
        private final List<ID> deletedIds = new ArrayList<>();
        private final Queue<Boolean> existsOverrides = new ArrayDeque<>();
        private final R repository;

        TestRepository(Class<R> repositoryType, Function<T, ID> idExtractor) {
            this.repositoryType = repositoryType;
            this.idExtractor = idExtractor;
            this.repository = repositoryType.cast(Proxy.newProxyInstance(
                    repositoryType.getClassLoader(),
                    new Class<?>[]{repositoryType},
                    this
            ));
        }

        public R repository() {
            return repository;
        }

        public void put(T entity) {
            entities.put(idExtractor.apply(entity), entity);
        }

        public Optional<T> find(ID id) {
            return Optional.ofNullable(entities.get(id));
        }

        public Collection<T> entities() {
            return entities.values();
        }

        public List<T> savedEntities() {
            return savedEntities;
        }

        public List<ID> deletedIds() {
            return deletedIds;
        }

        public void queueExists(boolean... values) {
            for (boolean value : values) {
                existsOverrides.add(value);
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getDeclaringClass().equals(Object.class)) {
                return handleObjectMethod(proxy, method, args);
            }

            String name = method.getName();
            if ("save".equals(name) && args != null && args.length == 1) {
                return save(args[0]);
            }
            if ("saveAll".equals(name) && args != null && args.length == 1) {
                return saveAll(args[0]);
            }
            if ("findById".equals(name) && args != null && args.length == 1) {
                return Optional.ofNullable(entities.get(args[0]));
            }
            if ("existsById".equals(name) && args != null && args.length == 1) {
                return existsById(args[0]);
            }
            if ("getReferenceById".equals(name) && args != null && args.length == 1) {
                return entities.get(args[0]);
            }
            if ("findAll".equals(name) && (args == null || args.length == 0)) {
                return new ArrayList<>(entities.values());
            }
            if ("count".equals(name) && (args == null || args.length == 0)) {
                return (long) entities.size();
            }
            if ("delete".equals(name) && args != null && args.length == 1) {
                deleteEntity(args[0]);
                return null;
            }
            if ("deleteById".equals(name) && args != null && args.length == 1) {
                deleteById(args[0]);
                return null;
            }
            if ("flush".equals(name) && (args == null || args.length == 0)) {
                return null;
            }

            Object customResult = handleCustomMethod(method, args);
            if (customResult != UnsupportedMethod.INSTANCE) {
                return customResult;
            }

            throw new UnsupportedOperationException(repositoryType.getSimpleName() + "." + method.getName() + " is not implemented in this test double");
        }

        protected Object handleCustomMethod(Method method, Object[] args) {
            return UnsupportedMethod.INSTANCE;
        }

        @SuppressWarnings("unchecked")
        private Object save(Object value) {
            T entity = (T) value;
            entities.put(idExtractor.apply(entity), entity);
            savedEntities.add(entity);
            return entity;
        }

        @SuppressWarnings("unchecked")
        private List<T> saveAll(Object value) {
            List<T> saved = new ArrayList<>();
            for (T entity : (Iterable<T>) value) {
                saved.add((T) save(entity));
            }
            return saved;
        }

        private boolean existsById(Object id) {
            if (!existsOverrides.isEmpty()) {
                return existsOverrides.remove();
            }
            return entities.containsKey(id);
        }

        @SuppressWarnings("unchecked")
        private void deleteEntity(Object value) {
            T entity = (T) value;
            deleteById(idExtractor.apply(entity));
        }

        @SuppressWarnings("unchecked")
        private void deleteById(Object id) {
            entities.remove(id);
            deletedIds.add((ID) id);
        }

        private Object handleObjectMethod(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "toString" -> repositoryType.getSimpleName() + " test double";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }

    public static final class ConsultantTestRepository extends TestRepository<ConsultantRepository, Consultant, UUID> {

        private ConsultantTestRepository() {
            super(ConsultantRepository.class, Consultant::getConsultantId);
        }

        @Override
        protected Object handleCustomMethod(Method method, Object[] args) {
            if ("findByFirstnameIgnoreCaseAndLastnameIgnoreCase".equals(method.getName()) && args != null && args.length == 2) {
                return entities().stream()
                        .filter(consultant -> equalsIgnoreCase(consultant.getFirstname(), args[0]))
                        .filter(consultant -> equalsIgnoreCase(consultant.getLastname(), args[1]))
                        .findFirst();
            }
            if ("findByCustomUserUsername".equals(method.getName()) && args != null && args.length == 1) {
                return entities().stream()
                        .filter(consultant -> consultant.getCustomUser() != null)
                        .filter(consultant -> Objects.equals(consultant.getCustomUser().getUsername(), args[0]))
                        .findFirst();
            }
            return super.handleCustomMethod(method, args);
        }

        private boolean equalsIgnoreCase(String value, Object other) {
            return other instanceof String otherValue && value != null && value.equalsIgnoreCase(otherValue);
        }
    }

    private enum UnsupportedMethod {
        INSTANCE
    }
}
