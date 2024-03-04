package service;

import client.DynamicParameterQuery;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ParsingProcessorTest {

    @ParameterizedTest(name = "{index} - {2}")
    @MethodSource("queriesBySymbolsProvider")
    public void shouldGenerateQueriesWithSymbolsProvided(String[] symbolArr, int expectedQuerySize, String comment) {
        ParsingProcessor processor = new ParsingProcessor(null);
        DynamicParameterQuery[] queries = processor.queryBySymbols(symbolArr);
        assertThat(queries.length).isEqualTo(expectedQuerySize);
    }

    private static Stream<Arguments> queriesBySymbolsProvider() {
        return Stream.of(
                Arguments.of(randomWordGenerator(100), 1, "One query per 100 symbols"),
                Arguments.of(randomWordGenerator(101), 2, "Two queries with sizes 100 + 1"),
                Arguments.of(randomWordGenerator(1000), 10, "Ten queries with 100 each")
        );
    }

    private static String[] randomWordGenerator(int quantity) {
        String[] resultArray = new String[quantity];
        char[] alphabet = new char[26];

        for (int i = 0; i < alphabet.length; i++) {
            alphabet[i] = (char) ('a' + i);
        }

        Random rnd = new Random();

        for (int i = 0; i < quantity; i++) {
            StringBuilder word = new StringBuilder();
            for (int ii = 0; ii < 4; ii++) {
                word.append(alphabet[rnd.nextInt(0, alphabet.length)]);
            }
            resultArray[i] = word.toString();
        }

        return resultArray;
    }
}