package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Объект, который содержит строковое представление параметров запроса
 */
public class DynamicParameterQuery {

    private final String query;

    private DynamicParameterQuery(String query) {
        this.query = query;

    }

    public static DynamicParameterQueryBuilder builder() {
        return new DynamicParameterQueryBuilder();
    }

    public static class DynamicParameterQueryBuilder {
        private List<String> idList;
        private List<String> slugList;

        private List<String> symbolList;

        public DynamicParameterQueryBuilder id(String id) {
            if (idList == null) {
                idList = new ArrayList<>();
            }
            idList.add(id);
            return this;
        }

        public DynamicParameterQueryBuilder slug(String slug) {
            if (slugList == null) {
                slugList = new ArrayList<>();
            }
            slugList.add(slug);
            return this;
        }

        public DynamicParameterQueryBuilder symbol(String symbol) {
            if (symbolList == null) {
                symbolList = new ArrayList<>();
            }
            symbolList.add(symbol);
            return this;
        }

        public DynamicParameterQuery build() {
            List<String> resultQuery = new ArrayList<>();

            if (idList != null) {
                String idQuery = String.join(",", idList);
                resultQuery.add("id=" + idQuery);
            }

            if (slugList != null) {
                String slugQuery = String.join(",", slugList);
                resultQuery.add("slug=" + slugQuery);
            }

            if (symbolList != null) {
                String symbolQuery = String.join(",", symbolList);
                resultQuery.add("symbol=" + symbolQuery);
            }

            return new DynamicParameterQuery(String.join("&", resultQuery));
        }
    }

    public String getQuery() {
        return query;
    }
}
