FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon
RUN ls /home/gradle/src/build/libs/

FROM openjdk:11 AS runner
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/SportsDayAPI*all.jar /app/SportsDayAPI.jar
ENTRYPOINT ["java","-jar","/app/SportsDayAPI.jar"]