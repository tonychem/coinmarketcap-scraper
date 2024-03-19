FROM maven:latest AS build
COPY ./src /src
COPY ./pom.xml pom.xml
RUN mvn -f pom.xml clean install

FROM amazoncorretto:21-alpine
COPY --from=build ./target/*-jar-with-dependencies.jar /jar/CoinmarketcapScraper.jar
ENTRYPOINT ["java", "-cp", "/jar/CoinmarketcapScraper.jar", "entrypoint.CoinmarketcapScraperApplication"]