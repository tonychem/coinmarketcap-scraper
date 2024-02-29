package repository;

public interface DataDefinitionProcessor {
    boolean relationExists(String relationName);

    void deleteRelation(String relationName);

    void createRelation(String relationName);
}
