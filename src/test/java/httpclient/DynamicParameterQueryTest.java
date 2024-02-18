package httpclient;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DynamicParameterQueryTest {
    @Test
    public void shouldGenerateExpectedUrlStringOutputWhenParamsAdded() {
        DynamicParameterQuery query = DynamicParameterQuery.builder()
                .symbol("BTC")
                .symbol("BSCS")
                .id("1")
                .id("2")
                .slug("bitcoin")
                .build();

        assertThat(query.getQuery()).contains("id=1,2");
        assertThat(query.getQuery()).contains("slug=bitcoin");
        assertThat(query.getQuery()).contains("symbol=BTC,BSCS");
        assertThat(query.getQuery()).isEqualTo("id=1,2&slug=bitcoin&symbol=BTC,BSCS");
    }

    @Test
    public void shouldGenerateEmptyUrlStringOutputWhenNoParams() {
        DynamicParameterQuery query = DynamicParameterQuery.builder()
                .build();
        assertThat(query.getQuery()).isEmpty();
    }

}