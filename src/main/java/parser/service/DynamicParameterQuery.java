package parser.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Объект, который содержит строковое представление параметров запроса
 */
public class DynamicParameterQuery {

    private final String query;
    private final int queriedParams;

    private DynamicParameterQuery(String query, int queriedParams) {
        this.query = query;
        this.queriedParams = queriedParams;
    }

    public static QueryBuilder builder() {
        return new QueryBuilder();
    }

    public static class QueryBuilder {
        private List<String> idList;
        private List<String> slugList;

        private List<String> symbolList;

        public QueryBuilder id(String id) {
            if (idList == null) {
                idList = new ArrayList<>();
            }
            idList.add(id);
            return this;
        }

        public QueryBuilder slug(String slug) {
            if (slugList == null) {
                slugList = new ArrayList<>();
            }
            slugList.add(slug);
            return this;
        }

        public QueryBuilder symbol(String symbol) {
            if (symbolList == null) {
                symbolList = new ArrayList<>();
            }
            symbolList.add(symbol);
            return this;
        }

        public DynamicParameterQuery build() {
            List<String> resultQuery = new ArrayList<>();
            int queries = 0;

            if (idList != null) {
                String idQuery = String.join(",", idList);
                resultQuery.add("id=" + idQuery);
                queries += idList.size();
            }

            if (slugList != null) {
                String slugQuery = String.join(",", slugList);
                resultQuery.add("slug=" + slugQuery);
                queries += slugList.size();
            }

            if (symbolList != null) {
                String symbolQuery = String.join(",", symbolList);
                resultQuery.add("symbol=" + symbolQuery);
                queries += symbolList.size();
            }

            return new DynamicParameterQuery(String.join("&", resultQuery), queries);
        }
    }

    public String getQuery() {
        return query;
    }

    public int getQueriedParams() {
        return queriedParams;
    }

    public static DynamicParameterQuery emptyQuery() {
        return new DynamicParameterQuery("", 0);
    }
}
