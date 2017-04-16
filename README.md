# reactive-demo

This is a demo of the Spring WebFlux module including the WebClient, with a bit of Spring Data and Spring Integration involved as well.

First run the reactive-sse application and in your browser go to localhost:8080/person to see Server Sent Events of Flux<List<Person>>, which is generated from the getAllPersons() method.

Go to the localhost:8080/sink endpoint next to see the same thing except this time the Server Sent Events are being sent using Spring IntegrationFlow, which is polling an H2 in memory database.

Next go and run the reactive-client application (make sure to keep the reactive-sse application running) which will start on port 8090, but you will see server sent events being sent to your console, and then two more Person objects being posted to the server and then being displayed.

It is best to either download or clone the program, <code> git clone https://github.com/jonathanborenstein/reactive-demo.git </code>, and then import it into your ide in order to run the program.

You can also run the program by going to each directory (reactive-demo/reactive-sse and reactive-demo/reactive-client) and run <code>mvn clean spring-boot:run </code> .

The demo was inspired by the Spring Tips series that Spring Advocate Josh Long does.
