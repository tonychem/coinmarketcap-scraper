package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Утилитарный класс, содержащий часто используемые в приложении константы.
 */
public class ApplicationConstantHolder {

    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    public static final DateTimeFormatter DEFAULT_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_PATTERN);
    public static final ZoneOffset DEFAULT_ZONE_OFFSET = ZoneOffset.UTC;
    public static final String INDICES_NAME_FORMAT = "%s-%d-%d-%d";

    /**
     * Возвращает настроенный json маппер для приложения.
     */
    public static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}
