FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM openjdk:11 AS runner
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/SportsDayAPI*all.jar /app/SportsDayAPI.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/SportsDayAPI.jar"]