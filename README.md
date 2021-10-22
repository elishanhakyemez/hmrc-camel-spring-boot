# hmrc-camel-spring-boot
A basic camel spring-boot application

* To build project, run:  
    `mvn clean install dockerfile:build`  
* In order to start the application in docker, run:  
    `docker run -p 8080:8080 -t hmrc-camel-spring-boot/camel-spring-boot-sandbox`
* To test running instance, please use Postman export located under _resources/postman_ folder
* To check coverage, please check _target/jacoco-report_ after build, below is a current screen-shot 
![Jococo Report](jacoco-report.PNG?raw=true "Jococo Report")