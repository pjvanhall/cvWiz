package nl.codeclan.cvwiz;

import nl.codeclan.cvwiz.util.InputValidationUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InputValidationUtilTest {

    @Test
    public void generateRandomStringReturnsSafePasswordWithRequestedLength() {
        String value = InputValidationUtil.generateRandomString(32);

        assertThat(value.length()).isEqualTo(32);
        assertThat(value.matches(".*[A-Z].*")).isTrue();
        assertThat(value.matches(".*[a-z].*")).isTrue();
        assertThat(value.matches(".*[0-9].*")).isTrue();
        assertThat(value.matches(".*[!@#$%^&*()\\-_+\\[\\]{}:,.?].*")).isTrue();
        assertThat(value.matches("[A-Za-z0-9!@#$%^&*()\\-_+\\[\\]{}:,.?]+")).isTrue();
    }

    @Test
    public void generateRandomStringCanUseCustomAlphabet() {
        String value = InputValidationUtil.generateRandomString(20, "ab");

        assertThat(value.length()).isEqualTo(20);
        assertThat(value.matches("[ab]+")).isTrue();
    }

    @Test
    public void generateSafePasswordAllowsMinimumLength() {
        String value = InputValidationUtil.generateSafePassword(12);

        assertThat(value.length()).isEqualTo(12);
        assertThat(value.matches(".*[A-Z].*")).isTrue();
        assertThat(value.matches(".*[a-z].*")).isTrue();
        assertThat(value.matches(".*[0-9].*")).isTrue();
        assertThat(value.matches(".*[!@#$%^&*()\\-_+\\[\\]{}:,.?].*")).isTrue();
    }

    @Test
    public void generateRandomStringWithCustomAlphabetAllowsZeroLength() {
        assertThat(InputValidationUtil.generateRandomString(0, "ab")).isEqualTo("");
    }

    @Test
    public void generateRandomStringRejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> InputValidationUtil.generateRandomString(-1));
        assertThrows(IllegalArgumentException.class, () -> InputValidationUtil.generateRandomString(11));
        assertThrows(IllegalArgumentException.class, () -> InputValidationUtil.generateRandomString(-1, "ab"));
        assertThrows(IllegalArgumentException.class, () -> InputValidationUtil.generateRandomString(10, ""));
        assertThrows(IllegalArgumentException.class, () -> InputValidationUtil.generateRandomString(10, null));
    }

    @Test
    public void constructorIsPrivateUtilityConstructor() throws Exception {
        Constructor<InputValidationUtil> constructor = InputValidationUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InputValidationUtil instance = constructor.newInstance();

        assertThat(instance).isNotNull();
    }

    @Test
    public void sanitizeRemovesUnsafeCharactersAndTrimsInput() {
        String value = InputValidationUtil.sanitize("  <script>alert('x')\\</script>`\u0000  ");

        assertThat(value).isEqualTo("scriptalert('x')/script");
    }

    @Test
    public void sanitizeKeepsApostrophesInNormalText() {
        String value = InputValidationUtil.sanitize("Candidate's API testing experience");

        assertThat(value).isEqualTo("Candidate's API testing experience");
    }

    @Test
    public void sanitizeRemovesSqlInjectionTautologies() {
        String value = InputValidationUtil.sanitize("admin' OR 1=1 --");

        assertThat(value).isEqualTo("admin");
    }

    @Test
    public void sanitizeRemovesSqlCommandFragments() {
        String value = InputValidationUtil.sanitize("Robert'); DROP TABLE custom_user;--");

        assertThat(value).isEqualTo("Robert) custom_user");
    }

    @Test
    public void sanitizeRemovesStandaloneDropKeyword() {
        String value = InputValidationUtil.sanitize("DROP");

        assertThat(value).isEqualTo("");
    }

    @Test
    public void sanitizeRemovesMixedCaseDropCommand() {
        String value = InputValidationUtil.sanitize("DrOp DATABASE cvw;");

        assertThat(value).isEqualTo("cvw");
    }

    @Test
    public void sanitizeRemovesCommentObfuscatedDropCommand() {
        String value = InputValidationUtil.sanitize("DR/**/OP TABLE custom_user");

        assertThat(value).isEqualTo("custom_user");
    }

    @Test
    public void sanitizeDoesNotRemoveDropInsideNormalWords() {
        String value = InputValidationUtil.sanitize("dropdown menu");

        assertThat(value).isEqualTo("dropdown menu");
    }

    @Test
    public void sanitizeRemovesUnionSelectFragments() {
        String value = InputValidationUtil.sanitize("test UNION SELECT username, password FROM custom_user");

        assertThat(value).isEqualTo("test username, password FROM custom_user");
    }

    @Test
    public void sanitizeRemovesSqlDelayFunctions() {
        String value = InputValidationUtil.sanitize("search'; SELECT pg_sleep(10);--");

        assertThat(value).isEqualTo("search 10)");
    }

    @Test
    public void sanitizeKeepsNormalWordsContainingSqlTerms() {
        String value = InputValidationUtil.sanitize("manager and consultant working on frontend selection");

        assertThat(value).isEqualTo("manager and consultant working on frontend selection");
    }

    @Test
    public void sanitizeRemovesCmdCommandChains() {
        String value = InputValidationUtil.sanitize("hello & del C:\\temp\\file.txt | whoami");

        assertThat(value).isEqualTo("hello C:tempfile.txt");
    }

    @Test
    public void sanitizeRemovesCmdShellInvocation() {
        String value = InputValidationUtil.sanitize("cmd /c whoami && ipconfig");

        assertThat(value).isEqualTo("");
    }

    @Test
    public void sanitizeRemovesCmdEnvironmentVariableExecution() {
        String value = InputValidationUtil.sanitize("%COMSPEC% /c format C:");

        assertThat(value).isEqualTo("C:");
    }

    @Test
    public void sanitizeRemovesCaretObfuscatedCmdCommands() {
        String value = InputValidationUtil.sanitize("po^wer^shell -Command whoami");

        assertThat(value).isEqualTo("");
    }

    @Test
    public void sanitizeDoesNotRemoveCmdNamesInsideNormalWords() {
        String value = InputValidationUtil.sanitize("directory copywriting typewriter");

        assertThat(value).isEqualTo("directory copywriting typewriter");
    }

    @Test
    public void sanitizeReturnsEmptyStringForNullInput() {
        assertThat(InputValidationUtil.sanitize(null)).isEqualTo("");
    }

    @Test
    public void removeControlCharactersKeepsOtherTextUntouched() {
        assertThat(InputValidationUtil.removeControlCharacters("Jan\u0002Pier's CV")).isEqualTo("JanPier's CV");
        assertThat(InputValidationUtil.removeControlCharacters(null)).isEqualTo("");
    }
}
