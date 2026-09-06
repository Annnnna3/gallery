FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY src ./src
RUN mkdir -p out \
    && javac --add-modules jdk.httpserver -encoding UTF-8 -d out src/main/java/com/artgallery/Main.java \
    && cp -R src/main/resources/* out/ \
    && printf 'Manifest-Version: 1.0\nMain-Class: com.artgallery.Main\n\n' > MANIFEST.MF \
    && jar cfm art-gallery-qa.jar MANIFEST.MF -C out .

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/art-gallery-qa.jar /app/art-gallery-qa.jar
EXPOSE 8080
ENTRYPOINT ["java", "--add-modules", "jdk.httpserver", "-jar", "/app/art-gallery-qa.jar"]
