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
            String elasticsearchHost = (String) elasticsearchProperties.get("host");

            if (elasticsearchHost.matches("\\$\\{\\w+:\\w+\\}")) {
                constantBuilder.elasticsearchHost(
                        getProperty(elasticsearchHost.substring(2, elasticsearchHost.length() - 1))
                );
            } else {
                constantBuilder.elasticsearchHost(elasticsearchHost);
            }
        }

        if (elasticsearchProperties.get("port") != null) {
            String elasticsearchPortString = elasticsearchProperties.get("port").toString();

            if (elasticsearchPortString.matches("\\$\\{\\w+:\\w+\\}")) {
                constantBuilder.elasticsearchPort(
                        Integer.parseInt(getProperty(elasticsearchPortString.substring(2, elasticsearchPortString.length() - 1)))
                );
            } else {
                constantBuilder.elasticsearchPort(Integer.parseInt(elasticsearchPortString));
            }
        }

        if (elasticsearchProperties.get("scheme") != null) {
            String elasticsearchScheme = (String) elasticsearchProperties.get("scheme");

            if (elasticsearchScheme.matches("\\$\\{\\w+:\\w+\\}")) {
                constantBuilder.elasticsearchScheme(
                        getProperty(elasticsearchScheme.substring(2, elasticsearchScheme.length() - 1))
                );
            } else {
                constantBuilder.elasticsearchScheme(elasticsearchScheme);
            }
        }

        Map<String, Object> tomcatProperties = (Map<String, Object>) properties.get("tomcat");

        if (tomcatProperties.get("host") != null) {
            String tomcatHost = (String) tomcatProperties.get("host");

            if (tomcatHost.matches("\\$\\{\\w+:\\w+\\}")) {
                constantBuilder.tomcatHost(
                        getProperty(tomcatHost.substring(2, tomcatHost.length() - 1))
                );
            } else {
                constantBuilder.tomcatHost(tomcatHost);
            }
        }

        if (tomcatProperties.get("port") != null) {
            String tomcatPortString = tomcatProperties.get("port").toString();

            if (tomcatPortString.matches("\\$\\{\\w+:\\w+\\}")) {
                constantBuilder.tomcatPort(
                        Integer.parseInt(getProperty(tomcatPortString.substring(2, tomcatPortString.length() - 1)))
                );
            } else {
                constantBuilder.tomcatPort(Integer.parseInt(tomcatPortString));
            }
        }

        return constantBuilder.build();
    }

    private static String getProperty(String pair) {
        String[] splittedString = pair.split(":");

        if (System.getProperty(splittedString[0]) != null) {
            return System.getProperty(splittedString[0]);
        }

        return splittedString[1];
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
