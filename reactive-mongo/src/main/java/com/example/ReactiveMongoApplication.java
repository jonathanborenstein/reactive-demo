package com.example;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@SpringBootApplication
@RestController
public class ReactiveMongoApplication {

	private PersonRepository personRepository;
	
	private List<String> list = new ArrayList<String>();

	public ReactiveMongoApplication(PersonRepository personRepository) {
		this.personRepository = personRepository;
	}

	private final Log log = LogFactory.getLog(getClass());

	@Bean
	CommandLineRunner demo (){
		return args -> {
			
			personRepository.save(new Person("Jon")).subscribe();
			personRepository.save(new Person("Joe")).subscribe();
			personRepository.save(new Person("Jane")).subscribe();
			
			list.add("Jon");
			list.add("Joe");
			list.add("Jane");
		};
	}

	@GetMapping(value = "/person", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<Person> getAllPersons() {
		Flux<Person> personFlux = Flux.fromStream(Stream.generate(() -> 
			personRepository.findByName(list.get(new Random().nextInt(list.size()))).block()));
		Flux<Long> durationFlux = Flux.interval(Duration.ofSeconds(1));
		return Flux.zip(personFlux, durationFlux).map(Tuple2::getT1);
	}


	@PostMapping("/post")
	void savePerson(@RequestBody Person person) {
		personRepository.save(person).subscribe();
		list.add(person.getName());
	}
	
	@GetMapping("/all")
	Flux<Person> findAllPersons() {
		return personRepository.findAll();
	}


	public static void main(String[] args) {
		SpringApplication.run(ReactiveMongoApplication.class, args);
	
	}
}

@Document
class Person{

	@Id
	String id;

	private String name;

	public Person(){

	}

	public Person(String name) {
		this.name = name;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

@Repository
interface PersonRepository extends ReactiveMongoRepository<Person, String>{

	Mono<Person> findByName(String string);

}
