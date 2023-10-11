FROM maven:latest AS stage1
WORKDIR /EventPlannerApp
COPY pom.xml /EventPlannerApp
RUN mvn dependency:resolve
COPY . /EventPlannerApp
RUN mkdir /EventPlannerApp/jarfiles
RUN cp /EventPlannerApp/target/EventPlannerApp-0.1.jar /EventPlannerApp/jarfiles
RUN mvn clean

FROM openjdk:17 AS final
COPY --from=stage1 /EventPlannerApp/jarfiles/EventPlannerApp-0.1.jar EventPlannerApp-0.1.jar
EXPOSE 8080
CMD ["java", "-jar", "EventPlannerApp-0.1.jar"]