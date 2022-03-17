FROM openjdk:8-alpine

COPY target/uberjar/tc-demo.jar /tc-demo/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/tc-demo/app.jar"]
