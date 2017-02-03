# Spring boot Camel REST + Swagger

An example application that shows how to configure spring boot + Camel with Spring JavaConfig and Camel's Swagger 
support with no web.xml.

## Quick Start

This project is built with [Spring Boot](http://projects.spring.io/spring-boot/) and uses 
[Apache Camel](http://camel.apache.org) for its REST API. 

To run this project use:

    mvn spring-boot:run

Then navigate to [http://localhost:8080/docs/index.html](http://localhost:8080/docs/index.html) to 
see the Swagger documentation.

The health check url: http://localhost:8080/health
hystrix:http://localhost:8761/
Eureka (service registry and discovery): http://localhost:8761/


google key:  AIzaSyC8-Qu9AKJuP2HohE6pQUmk--R4KWMmkZU 