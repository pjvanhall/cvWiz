package nl.codeclan.cvwiz.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import nl.codeclan.cvwiz.util.InputValidationUtil;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.beans.PropertyEditorSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@ControllerAdvice
public class InputSanitizationAdvice extends RequestBodyAdviceAdapter {

    private final ObjectMapper objectMapper;

    public InputSanitizationAdvice() {
        this(new ObjectMapper());
    }

    InputSanitizationAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.getFactory().configure(JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature(), true);
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(InputValidationUtil.sanitize(text));
            }
        });
    }

    @Override
    @NullMarked
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    @NullMarked
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        Charset charset = getCharset(inputMessage.getHeaders());
        String body = new String(StreamUtils.copyToByteArray(inputMessage.getBody()), charset);
        String sanitizedBody = sanitizeBody(body, isJsonContent(inputMessage.getHeaders()));
        return new SanitizedHttpInputMessage(inputMessage.getHeaders(), sanitizedBody.getBytes(charset));
    }

    String sanitizeBody(String body, boolean jsonBody) {
        if (body == null || body.isEmpty()) {
            return body;
        }

        if (!jsonBody) {
            return InputValidationUtil.sanitize(body);
        }

        try {
            JsonNode root = objectMapper.readTree(InputValidationUtil.removeControlCharacters(body));
            return objectMapper.writeValueAsString(sanitizeJsonNode(root, null));
        } catch (JsonProcessingException e) {
            return body;
        }
    }

    private JsonNode sanitizeJsonNode(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            return node;
        }
        if (node.isTextual()) {
            return shouldSkipField(fieldName) ? node : TextNode.valueOf(InputValidationUtil.sanitize(node.asText()));
        }
        if (node.isArray()) {
            ArrayNode sanitizedArray = objectMapper.createArrayNode();
            for (JsonNode item : node) {
                sanitizedArray.add(sanitizeJsonNode(item, fieldName));
            }
            return sanitizedArray;
        }
        if (node.isObject()) {
            ObjectNode sanitizedObject = objectMapper.createObjectNode();
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                String sanitizedFieldName = shouldSkipField(entry.getKey()) ? entry.getKey() : InputValidationUtil.sanitize(entry.getKey());
                sanitizedObject.set(sanitizedFieldName, sanitizeJsonNode(entry.getValue(), entry.getKey()));
            }
            return sanitizedObject;
        }

        return node;
    }

    private boolean shouldSkipField(String fieldName) {
        if (fieldName == null) {
            return false;
        }

        String normalizedFieldName = fieldName.toLowerCase();
        return normalizedFieldName.contains("password")
                || normalizedFieldName.contains("token")
                || normalizedFieldName.contains("secret")
                || normalizedFieldName.contains("authorization");
    }

    private boolean isJsonContent(HttpHeaders headers) {
        MediaType contentType = headers.getContentType();
        return contentType != null
                && (MediaType.APPLICATION_JSON.includes(contentType) || contentType.getSubtype().endsWith("+json"));
    }

    private Charset getCharset(HttpHeaders headers) {
        MediaType contentType = headers.getContentType();
        return contentType != null && contentType.getCharset() != null ? contentType.getCharset() : StandardCharsets.UTF_8;
    }

    private record SanitizedHttpInputMessage(HttpHeaders headers, byte[] body) implements HttpInputMessage {
        @Override
        @NullMarked
        public InputStream getBody() {
            return new ByteArrayInputStream(body);
        }

        @Override
        @NullMarked
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}
