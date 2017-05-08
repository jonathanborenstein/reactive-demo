package com.example;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class ReactiveClientApplication {

	@Bean
	WebClient client (){
		return WebClient.create("http://localhost:8080");
	}

	@Bean
	CommandLineRunner demo (WebClient client){
		return args -> {
						
			client.get()
			.uri("/person")
			.accept(MediaType.TEXT_EVENT_STREAM)
			.retrieve()
			.bodyToFlux(String.class)
			.subscribe(System.out::println);

			
			Thread.sleep(4000);

			client.post()
			.uri("/post")
			.syncBody(new Person("Dave"))
			.exchange()
			.subscribe();

			Thread.sleep(4000);
			
			client.post()
			.uri("/post")
			.syncBody(new Person("Maria"))
			.exchange()
			.subscribe();
			
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(ReactiveClientApplication.class, args);
	}
}

class Person{
	

	Long id;
	
	private String name;
	
	public Person(){
		
	}
	
	public Person(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}