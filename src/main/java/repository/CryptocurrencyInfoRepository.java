package repository;

import model.CryptocurrencyInfo;

import java.util.List;

public interface CryptocurrencyInfoRepository extends DataDefinitionProcessor {
    Long addInfo(CryptocurrencyInfo cryptocurrencyInfo);
    CryptocurrencyInfo getInfo(Long id);

    List<CryptocurrencyInfo> getInfosBy(RepositoryRequest request);

    void deleteInfo(Long id);

    void updateInfo(Long id, CryptocurrencyInfo info);


}
