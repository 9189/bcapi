FROM eclipse-temurin:25-jre
WORKDIR /app
RUN addgroup --system spring && adduser --system spring --ingroup spring
COPY target/*.jar app.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]