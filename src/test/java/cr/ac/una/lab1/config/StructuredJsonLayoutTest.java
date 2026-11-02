package cr.ac.una.lab1.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para StructuredJsonLayout (Laboratorio 7: Observabilidad).
 */
class StructuredJsonLayoutTest {

    private final StructuredJsonLayout layout = new StructuredJsonLayout();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("StructuredJsonLayout: emite JSON estructurado con timestamp, level, logger, thread y message")
    void test_doLayout_emiteJsonValido() throws Exception {
        ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        when(event.getTimeStamp()).thenReturn(1730550000000L);
        when(event.getLevel()).thenReturn(Level.INFO);
        when(event.getLoggerName()).thenReturn("cr.ac.una.lab1.business.AuthService");
        when(event.getThreadName()).thenReturn("main");
        when(event.getFormattedMessage()).thenReturn("Evento de seguridad: inicio de sesión exitoso");
        when(event.getThrowableProxy()).thenReturn(null);

        String json = layout.doLayout(event);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.has("timestamp")).isTrue();
        assertThat(root.get("level").asText()).isEqualTo("INFO");
        assertThat(root.get("logger").asText()).isEqualTo("cr.ac.una.lab1.business.AuthService");
        assertThat(root.get("thread").asText()).isEqualTo("main");
        assertThat(root.get("message").asText()).isEqualTo("Evento de seguridad: inicio de sesión exitoso");
    }

    @Test
    @DisplayName("StructuredJsonLayout: incluye exceptionClass y exceptionMessage si hay excepción")
    void test_doLayout_conExcepcion() throws Exception {
        ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
        when(event.getTimeStamp()).thenReturn(1730550000000L);
        when(event.getLevel()).thenReturn(Level.ERROR);
        when(event.getLoggerName()).thenReturn("cr.ac.una.lab1.presentation.GlobalExceptionHandler");
        when(event.getThreadName()).thenReturn("http-nio-8080-exec-1");
        when(event.getFormattedMessage()).thenReturn("Fallo inesperado");
        when(event.getThrowableProxy()).thenReturn(new ThrowableProxy(new RuntimeException("Error simulado")));

        String json = layout.doLayout(event);

        JsonNode root = objectMapper.readTree(json);
        assertThat(root.get("level").asText()).isEqualTo("ERROR");
        assertThat(root.get("exceptionClass").asText()).isEqualTo(RuntimeException.class.getName());
        assertThat(root.get("exceptionMessage").asText()).isEqualTo("Error simulado");
    }
}
