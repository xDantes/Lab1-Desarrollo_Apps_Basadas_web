package cr.ac.una.lab1.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Layout personalizado de Logback que emite los registros en formato JSON estructurado
 * hacia la salida estándar (stdout) para observabilidad mínima en entornos reales.
 */
public class StructuredJsonLayout extends LayoutBase<ILoggingEvent> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String doLayout(ILoggingEvent event) {
        Map<String, Object> logEntry = new LinkedHashMap<>();
        logEntry.put("timestamp", Instant.ofEpochMilli(event.getTimeStamp()).toString());
        logEntry.put("level", event.getLevel().toString());
        logEntry.put("logger", event.getLoggerName());
        logEntry.put("thread", event.getThreadName());
        logEntry.put("message", event.getFormattedMessage());

        IThrowableProxy throwable = event.getThrowableProxy();
        if (throwable != null) {
            logEntry.put("exceptionClass", throwable.getClassName());
            logEntry.put("exceptionMessage", throwable.getMessage());
        }

        try {
            return mapper.writeValueAsString(logEntry) + System.lineSeparator();
        } catch (Exception e) {
            return event.getFormattedMessage() + System.lineSeparator();
        }
    }
}
