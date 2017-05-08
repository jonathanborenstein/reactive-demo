package com.example;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.MediaType;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.integration.json.ObjectToJsonTransformer;
import org.springframework.integration.splitter.DefaultMessageSplitter;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

@SpringBootApplication
@RestController
public class ReactiveSseApplication {
	
	private PersonRepository personRepository;
	
	private DataSource dataSource;
	

	public ReactiveSseApplication(PersonRepository personRepository, DataSource dataSource) {
		this.personRepository = personRepository;
		this.dataSource = dataSource;
	}

	private final Log log = LogFactory.getLog(getClass());
	
	@Bean
	CommandLineRunner demo (){
		return args -> {
				personRepository.save(new Person("Jon"));
				personRepository.save(new Person("Joe"));
				personRepository.save(new Person("Jane"));
		};
	}
	
	@GetMapping(value = "/person", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<List<Person>> getAllPersons() {
		Flux<List<Person>> personFlux = Flux.fromStream(Stream.generate(() -> personRepository.findAll()));
		Flux<Long> durationFlux = Flux.interval(Duration.ofSeconds(1));
		return Flux.zip(personFlux, durationFlux).map(Tuple2::getT1);
	}
	
	@GetMapping(value = "/sink", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	Flux<String> sink() {
		return Flux.create(sink -> {
			MessageHandler handler = msg -> sink.next((String) (msg.getPayload()));
			sink.onCancel(() -> filesChannel().unsubscribe(handler));
			filesChannel().subscribe(handler);
		});
	}
	
	@PostMapping("/post")
	void savePerson(@RequestBody Person person) {
		personRepository.save(person);
	}
	
	@Bean
	DefaultMessageSplitter commaSplitter() {
		DefaultMessageSplitter splitter = new DefaultMessageSplitter();
		splitter.setDelimiters(",");
		return splitter;
	}

	@Bean
	public JdbcPollingChannelAdapter jdbcMessageSource() {
		return new JdbcPollingChannelAdapter(this.dataSource, "Select * from person");
	}

	@Bean
	IntegrationFlow pollingFlow(){
		return IntegrationFlows.from(jdbcMessageSource(),
				c -> c.poller(Pollers.fixedRate(10000).maxMessagesPerPoll(1)))
				.split(commaSplitter())
				.transform(new ObjectToJsonTransformer())
				.channel(filesChannel())
				.get();
	}

	@Bean
	SubscribableChannel filesChannel() {
		return MessageChannels.publishSubscribe().get();
	}
	

	public static void main(String[] args) {
		SpringApplication.run(ReactiveSseApplication.class, args);
	}
}

@Entity
class Person{
	
	@Id 
	@GeneratedValue
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

@Repository
interface PersonRepository extends JpaRepository<Person, Long>{
	
}