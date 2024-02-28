package utils;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class PropertyFileReader {
    public static Map<String, Object> load(String path) {
        Yaml yaml = new Yaml();

        try (InputStream inputStream = PropertyFileReader.class.getResourceAsStream(path)) {
            return yaml.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
