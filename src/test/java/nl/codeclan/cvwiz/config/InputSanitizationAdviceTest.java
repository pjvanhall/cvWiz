package nl.codeclan.cvwiz.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;

import java.beans.PropertyEditor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class InputSanitizationAdviceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final InputSanitizationAdvice advice = new InputSanitizationAdvice(objectMapper);

    @Test
    public void defaultConstructorUsesDefaultObjectMapper() {
        assertThat(new InputSanitizationAdvice().sanitizeBody("cmd /c whoami", false)).isEqualTo("");
    }

    @Test
    public void sanitizeBodySanitizesRawStringBodies() {
        String value = advice.sanitizeBody("cmd /c whoami", false);

        assertThat(value).isEqualTo("");
    }

    @Test
    public void supportsEveryRequestBody() {
        assertThat(advice.supports(null, String.class, null)).isTrue();
    }

    @Test
    public void sanitizeBodyLeavesNullAndEmptyBodiesAlone() {
        assertThat(advice.sanitizeBody(null, true)).isNull();
        assertThat(advice.sanitizeBody("", true)).isEqualTo("");
    }

    @Test
    public void sanitizeBodyLeavesInvalidJsonUnchanged() {
        String value = advice.sanitizeBody("{cmd /c whoami", true);

        assertThat(value).isEqualTo("{cmd /c whoami");
    }

    @Test
    public void sanitizeBodyAcceptsSingleQuotedJsonAndWritesValidJson() throws Exception {
        String value = advice.sanitizeBody("{'profiel':'Candidate\\'s test experience'}", true);

        JsonNode sanitized = objectMapper.readTree(value);

        assertThat(sanitized.get("profiel").asText()).isEqualTo("Candidate's test experience");
    }

    @Test
    public void sanitizeBodyKeepsApostrophesInsideJsonStringValues() throws Exception {
        String json = """
                {
                  "profiel": "Candidate's experience with API testing"
                }
                """;

        JsonNode sanitized = objectMapper.readTree(advice.sanitizeBody(json, true));

        assertThat(sanitized.get("profiel").asText()).isEqualTo("Candidate's experience with API testing");
    }

    @Test
    public void sanitizeBodyRemovesControlCharactersBeforeJsonParsing() throws Exception {
        String json = "{\"profiel\":\"Jan\u0002Pier\"}";

        JsonNode sanitized = objectMapper.readTree(advice.sanitizeBody(json, true));

        assertThat(sanitized.get("profiel").asText()).isEqualTo("JanPier");
    }

    @Test
    public void sanitizeBodyHandlesJsonRootValues() {
        assertThat(advice.sanitizeBody("null", true)).isEqualTo("null");
        assertThat(advice.sanitizeBody("123", true)).isEqualTo("123");
        assertThat(advice.sanitizeBody("\"cmd /c whoami\"", true)).isEqualTo("\"\"");
    }

    @Test
    public void sanitizeBodySanitizesJsonStringValuesBeforeDtoCreation() throws Exception {
        String json = """
                {
                  "voornaam": "Jane & whoami",
                  "achternaam": "DROP TABLE managers",
                  "telefoon": "0612345678",
                  "emailAdres": "jane@example.com"
                }
                """;

        JsonNode sanitized = objectMapper.readTree(advice.sanitizeBody(json, true));

        assertThat(sanitized.get("voornaam").asText()).isEqualTo("Jane");
        assertThat(sanitized.get("achternaam").asText()).isEqualTo("managers");
        assertThat(sanitized.get("telefoon").asText()).isEqualTo("0612345678");
        assertThat(sanitized.get("emailAdres").asText()).isEqualTo("jane@example.com");
    }

    @Test
    public void sanitizeBodySanitizesNestedJsonArrays() throws Exception {
        String json = """
                {
                  "competenties": ["Java", "powershell -Command whoami"],
                  "ervaring": [
                    {
                      "bedrijf": "ACME | whoami",
                      "situatie": "cmd /c whoami",
                      "taak": "DROP TABLE custom_user"
                    }
                  ]
                }
                """;

        JsonNode sanitized = objectMapper.readTree(advice.sanitizeBody(json, true));

        assertThat(sanitized.get("competenties").get(0).asText()).isEqualTo("Java");
        assertThat(sanitized.get("competenties").get(1).asText()).isEqualTo("");
        assertThat(sanitized.get("ervaring").get(0).get("bedrijf").asText()).isEqualTo("ACME");
        assertThat(sanitized.get("ervaring").get(0).get("situatie").asText()).isEqualTo("");
        assertThat(sanitized.get("ervaring").get(0).get("taak").asText()).isEqualTo("custom_user");
    }

    @Test
    public void sanitizeBodySanitizesJsonFieldNames() throws Exception {
        String json = """
                {
                  "cmd /c whoami": "DROP TABLE custom_user"
                }
                """;

        JsonNode sanitized = objectMapper.readTree(advice.sanitizeBody(json, true));

        assertThat(sanitized.has("")).isTrue();
        assertThat(sanitized.get("").asText()).isEqualTo("custom_user");
    }

    @Test
    public void sanitizeBodyDoesNotChangePasswordFields() throws Exception {
        String json = """
                {
                  "username": "piet",
                  "password": "A!cmd /c whoami",
                  "token": "cmd /c whoami",
                  "secret": "cmd /c whoami",
                  "authorization": "cmd /c whoami"
                }
                """;

        JsonNode sanitized = objectMapper.readTree(advice.sanitizeBody(json, true));

        assertThat(sanitized.get("username").asText()).isEqualTo("piet");
        assertThat(sanitized.get("password").asText()).isEqualTo("A!cmd /c whoami");
        assertThat(sanitized.get("token").asText()).isEqualTo("cmd /c whoami");
        assertThat(sanitized.get("secret").asText()).isEqualTo("cmd /c whoami");
        assertThat(sanitized.get("authorization").asText()).isEqualTo("cmd /c whoami");
    }

    @Test
    public void beforeBodyReadSanitizesJsonBodiesWithExplicitCharset() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        HttpInputMessage inputMessage = new StaticHttpInputMessage(headers, "{\"name\":\"cmd /c whoami\"}".getBytes(StandardCharsets.UTF_8));

        HttpInputMessage sanitized = advice.beforeBodyRead(inputMessage, null, String.class, null);

        assertThat(new String(sanitized.getBody().readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("{\"name\":\"\"}");
        assertThat(sanitized.getHeaders()).isSameAs(headers);
    }

    @Test
    public void beforeBodyReadTreatsJsonSuffixContentTypesAsJson() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "merge-patch+json"));
        HttpInputMessage inputMessage = new StaticHttpInputMessage(headers, "{\"name\":\"DROP TABLE users\"}".getBytes(StandardCharsets.UTF_8));

        HttpInputMessage sanitized = advice.beforeBodyRead(inputMessage, null, String.class, null);

        assertThat(new String(sanitized.getBody().readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("{\"name\":\"users\"}");
    }

    @Test
    public void beforeBodyReadSanitizesNonJsonBodiesWithDefaultCharset() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        HttpInputMessage inputMessage = new StaticHttpInputMessage(headers, "cmd /c whoami".getBytes(StandardCharsets.UTF_8));

        HttpInputMessage sanitized = advice.beforeBodyRead(inputMessage, null, String.class, null);

        assertThat(new String(sanitized.getBody().readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("");
    }

    @Test
    public void initBinderSanitizesRequestParamStrings() {
        WebDataBinder binder = new WebDataBinder(null);
        advice.initBinder(binder);

        PropertyEditor editor = binder.findCustomEditor(String.class, null);
        editor.setAsText("cmd /c whoami");

        assertThat(editor.getValue()).isEqualTo("");
    }

    private record StaticHttpInputMessage(HttpHeaders headers, byte[] body) implements HttpInputMessage {
        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(body);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}
