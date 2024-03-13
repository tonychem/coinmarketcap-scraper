package utils;

import org.yaml.snakeyaml.Yaml;
import utils.entity.Credential;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Утилитарный класс для выгрузки всех свойств из yaml-файла
 */
public class PropertyFileReader {
    public static ApplicationConstantHolder readConstants() {
        Map<String, Object> properties = PropertyFileReader.load("/propertyfile.yml");
        ApplicationConstantHolder.Builder constantBuilder = ApplicationConstantHolder.builder();

        List<Credential> credentials = ((List<String>) properties.get("credentials")).stream()
                .map(Credential::new)
                .toList();

        String[] symbols = ((List<String>) (((Map<String, Object>) (properties.get("task"))).get("symbols"))).stream()
                .toArray(String[]::new);

        constantBuilder.credentials(credentials);
        constantBuilder.symbols(symbols);

        Map<String, Object> elasticsearchProperties = (Map<String, Object>) properties.get("elasticsearch");

        if (elasticsearchProperties.get("host") != null) {
            constantBuilder.elasticsearchHost((String) elasticsearchProperties.get("host"));
        }

        if (elasticsearchProperties.get("port") != null) {
            constantBuilder.elasticsearchPort((int) elasticsearchProperties.get("port"));
        }

        if (elasticsearchProperties.get("scheme") != null) {
            constantBuilder.elasticsearchScheme((String) elasticsearchProperties.get("scheme"));
        }

        Map<String, Object> tomcatProperties = (Map<String, Object>) properties.get("tomcat");

        if (tomcatProperties.get("host") != null) {
            constantBuilder.tomcatHost((String) tomcatProperties.get("host"));
        }

        if (tomcatProperties.get("port") != null) {
            constantBuilder.tomcatPort((int) tomcatProperties.get("port"));
        }

        return constantBuilder.build();
    }

    private static Map<String, Object> load(String path) {
        Yaml yaml = new Yaml();

        try (InputStream inputStream = PropertyFileReader.class.getResourceAsStream(path)) {
            return yaml.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
