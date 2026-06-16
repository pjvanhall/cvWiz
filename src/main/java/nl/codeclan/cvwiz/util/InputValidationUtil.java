package nl.codeclan.cvwiz.util;

import java.security.SecureRandom;
import java.util.regex.Pattern;

public final class InputValidationUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final String UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String PASSWORD_SPECIAL_CHARACTERS = "!@#$%^&*()-_+[]{}:,.?";
    private static final String PASSWORD_ALPHABET = UPPERCASE_LETTERS + LOWERCASE_LETTERS + DIGITS + PASSWORD_SPECIAL_CHARACTERS;
    private static final Pattern CONTROL_CHARACTERS = Pattern.compile("\\p{Cntrl}");
    private static final Pattern SQL_COMMENTS = Pattern.compile("(?i)(--|#|/\\*|\\*/)");
    private static final Pattern SQL_TAUTOLOGIES = Pattern.compile("(?i)'?\\s*\\b(or|and)\\s+[^\\s]+\\s*=\\s*[^\\s]+");
    private static final Pattern SQL_STRING_TERMINATOR_BEFORE_STATEMENT = Pattern.compile("'(?=\\s*\\)?\\s*;)");
    private static final Pattern SQL_DROP_COMMAND = Pattern.compile("(?i)\\bdrop\\b(?=\\s|;|$)(?:\\s+(?:table|database|schema|index|view|sequence|trigger|function|procedure))?");
    private static final Pattern SQL_COMMANDS = Pattern.compile(
            "(?i)\\b(union\\s+select|select\\s+.+\\s+from|insert\\s+into|update\\s+\\w+\\s+set|delete\\s+from|drop\\s+table|alter\\s+table|truncate\\s+table|create\\s+table|exec(?:ute)?\\s+)\\b"
    );
    private static final Pattern SQL_FUNCTIONS = Pattern.compile("(?i)\\b(?:select\\s+)?(waitfor\\s+delay|sleep\\s*\\(|benchmark\\s*\\(|pg_sleep\\s*\\()");
    private static final Pattern CMD_ENVIRONMENT_VARIABLES = Pattern.compile("%[^%\\s]+%");
    private static final Pattern CMD_COMMANDS = Pattern.compile(
            "(?i)\\b(assoc|attrib|bcdedit|bitsadmin|cacls|call|cd|chcp|chdir|chkdsk|chkntfs|cipher|cls|cmd|color|comp|compact|convert|copy|curl|date|del|dir|diskpart|doskey|driverquery|echo|endlocal|erase|exit|expand|fc|find|findstr|for|format|fsutil|ftype|ftp|getmac|goto|gpresult|help|hostname|icacls|if|ipconfig|label|md|mkdir|mklink|mode|more|move|net|netsh|netstat|nslookup|path|pathping|pause|ping|popd|powercfg|powershell|print|prompt|pushd|pwsh|rd|recover|reg|rem|ren|rename|replace|robocopy|route|runas|rmdir|sc|schtasks|set|setlocal|shutdown|sort|start|subst|systeminfo|takeown|taskkill|tasklist|telnet|time|title|tracert|tree|type|ver|verify|vol|wevtutil|where|whoami|wmic|xcopy)\\b"
    );
    private static final Pattern CMD_SWITCHES = Pattern.compile("(?i)(/[ck]|-(command|encodedcommand|enc|executionpolicy|noprofile|nop|windowstyle|w))\\b");
    private static final Pattern CMD_CONTROL_OPERATORS = Pattern.compile("[&|^%!]");
    private static final Pattern UNSAFE_DELIMITERS = Pattern.compile("[;=<>\"`\\\\]");
    private static final Pattern EXTRA_WHITESPACE = Pattern.compile("\\s{2,}");

    private InputValidationUtil() {
    }

    public static String generateRandomString(int length) {
        return generateSafePassword(length);
    }

    public static String generateSafePassword(int length) {
        if (length < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("password length must be at least " + MIN_PASSWORD_LENGTH);
        }

        char[] password = new char[length];
        password[0] = randomCharacterFrom(UPPERCASE_LETTERS);
        password[1] = randomCharacterFrom(LOWERCASE_LETTERS);
        password[2] = randomCharacterFrom(DIGITS);
        password[3] = randomCharacterFrom(PASSWORD_SPECIAL_CHARACTERS);

        for (int i = 4; i < length; i++) {
            password[i] = randomCharacterFrom(PASSWORD_ALPHABET);
        }

        shuffle(password);
        return new String(password);
    }

    public static String generateRandomString(int length, String alphabet) {
        if (length < 0) {
            throw new IllegalArgumentException("length cannot be negative");
        }
        if (alphabet == null || alphabet.isEmpty()) {
            throw new IllegalArgumentException("alphabet cannot be empty");
        }

        StringBuilder value = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            value.append(alphabet.charAt(SECURE_RANDOM.nextInt(alphabet.length())));
        }
        return value.toString();
    }

    private static char randomCharacterFrom(String alphabet) {
        return alphabet.charAt(SECURE_RANDOM.nextInt(alphabet.length()));
    }

    private static void shuffle(char[] characters) {
        for (int i = characters.length - 1; i > 0; i--) {
            int index = SECURE_RANDOM.nextInt(i + 1);
            char current = characters[i];
            characters[i] = characters[index];
            characters[index] = current;
        }
    }

    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }

        String sanitized = removeControlCharacters(input.trim());
        sanitized = SQL_COMMENTS.matcher(sanitized).replaceAll("");
        sanitized = SQL_TAUTOLOGIES.matcher(sanitized).replaceAll("");
        sanitized = SQL_STRING_TERMINATOR_BEFORE_STATEMENT.matcher(sanitized).replaceAll("");
        sanitized = SQL_DROP_COMMAND.matcher(sanitized).replaceAll("");
        sanitized = SQL_COMMANDS.matcher(sanitized).replaceAll("");
        sanitized = SQL_FUNCTIONS.matcher(sanitized).replaceAll("");
        sanitized = CMD_ENVIRONMENT_VARIABLES.matcher(sanitized).replaceAll("");
        sanitized = CMD_CONTROL_OPERATORS.matcher(sanitized).replaceAll("");
        sanitized = CMD_COMMANDS.matcher(sanitized).replaceAll("");
        sanitized = CMD_SWITCHES.matcher(sanitized).replaceAll("");
        sanitized = UNSAFE_DELIMITERS.matcher(sanitized).replaceAll("");
        return EXTRA_WHITESPACE.matcher(sanitized).replaceAll(" ").trim();
    }

    public static String removeControlCharacters(String input) {
        if (input == null) {
            return "";
        }
        return CONTROL_CHARACTERS.matcher(input).replaceAll("");
    }
}
