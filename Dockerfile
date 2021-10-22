FROM openjdk:8-jdk-alpine
VOLUME /tmp
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /hmrc-camel-spring-boot/lib
COPY ${DEPENDENCY}/META-INF /hmrc-camel-spring-boot/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /hmrc-camel-spring-boot
ENTRYPOINT ["java","-cp","hmrc-camel-spring-boot:hmrc-camel-spring-boot/lib/*","co.hmrc.elis.sandbox.camelspringboot.CamelSpringBootApplication"]