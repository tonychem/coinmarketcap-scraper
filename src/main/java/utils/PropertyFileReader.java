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

    /**
     * Метод вычитывает yml файл свойств и формирует объект пользовательский свойств.
     * @return Объект, содержащий пользовательские свойтсва.
     */
    public static PropertyHolder readConstants() {
        Map<String, Object> properties = PropertyFileReader.load("/propertyfile.yml");
        PropertyHolder.Builder propertyBuilder = PropertyHolder.builder();

        List<Credential> credentials = ((List<String>) properties.get("credentials")).stream()
                .map(Credential::new)
                .toList();

        String[] symbols = ((List<String>) (((Map<String, Object>) (properties.get("task"))).get("symbols"))).stream()
                .toArray(String[]::new);

        propertyBuilder.credentials(credentials);
        propertyBuilder.symbols(symbols);

        Map<String, Object> elasticsearchProperties = (Map<String, Object>) properties.get("elasticsearch");

        if (elasticsearchProperties.get("host") != null) {
            String elasticsearchHost = (String) elasticsearchProperties.get("host");

            if (elasticsearchHost.matches("\\$\\{\\w+:\\w+\\}")) {
                propertyBuilder.elasticsearchHost(
                        getEnvOrFileProperty(elasticsearchHost.substring(2, elasticsearchHost.length() - 1))
                );
            } else {
                propertyBuilder.elasticsearchHost(elasticsearchHost);
            }
        }

        if (elasticsearchProperties.get("port") != null) {
            String elasticsearchPortString = elasticsearchProperties.get("port").toString();

            if (elasticsearchPortString.matches("\\$\\{\\w+:\\w+\\}")) {
                propertyBuilder.elasticsearchPort(
                        Integer.parseInt(getEnvOrFileProperty(elasticsearchPortString.substring(2, elasticsearchPortString.length() - 1)))
                );
            } else {
                propertyBuilder.elasticsearchPort(Integer.parseInt(elasticsearchPortString));
            }
        }

        if (elasticsearchProperties.get("scheme") != null) {
            String elasticsearchScheme = (String) elasticsearchProperties.get("scheme");

            if (elasticsearchScheme.matches("\\$\\{\\w+:\\w+\\}")) {
                propertyBuilder.elasticsearchScheme(
                        getEnvOrFileProperty(elasticsearchScheme.substring(2, elasticsearchScheme.length() - 1))
                );
            } else {
                propertyBuilder.elasticsearchScheme(elasticsearchScheme);
            }
        }

        Map<String, Object> tomcatProperties = (Map<String, Object>) properties.get("tomcat");

        if (tomcatProperties.get("host") != null) {
            String tomcatHost = (String) tomcatProperties.get("host");

            if (tomcatHost.matches("\\$\\{\\w+:\\w+\\}")) {
                propertyBuilder.tomcatHost(
                        getEnvOrFileProperty(tomcatHost.substring(2, tomcatHost.length() - 1))
                );
            } else {
                propertyBuilder.tomcatHost(tomcatHost);
            }
        }

        if (tomcatProperties.get("port") != null) {
            String tomcatPortString = tomcatProperties.get("port").toString();

            if (tomcatPortString.matches("\\$\\{\\w+:\\w+\\}")) {
                propertyBuilder.tomcatPort(
                        Integer.parseInt(getEnvOrFileProperty(tomcatPortString.substring(2, tomcatPortString.length() - 1)))
                );
            } else {
                propertyBuilder.tomcatPort(Integer.parseInt(tomcatPortString));
            }
        }

        return propertyBuilder.build();
    }

    /**
     * На вход методу приходит пара строк, разделенных двоеточием. Если определен параметр среды, имя которого совпадает
     * с первой строкой, то возвращается это свойство. Иначе, возвращается вторая строка.
     * @param pair пара строк, разделенных двоеточием, напр. "string1:string2"
     * @return свойство
     */
    private static String getEnvOrFileProperty(String pair) {
        String[] splittedString = pair.split(":");

        if (System.getProperty(splittedString[0]) != null) {
            return System.getProperty(splittedString[0]);
        }

        return splittedString[1];
    }

    /**
     * Метод вычитывает yml файл
     * @param path путь до yml файла
     * @return ассоциативный массив свойств, заданных пользователем во внешнем файле.
     */
    private static Map<String, Object> load(String path) {
        Yaml yaml = new Yaml();

        try (InputStream inputStream = PropertyFileReader.class.getResourceAsStream(path)) {
            return yaml.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
