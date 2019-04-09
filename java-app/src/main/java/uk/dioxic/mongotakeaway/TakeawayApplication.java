package uk.dioxic.mongotakeaway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import uk.dioxic.mongotakeaway.domain.NodeInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@Slf4j
@SpringBootApplication
public class TakeawayApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(TakeawayApplication.class, args);
	}

	@Bean
	public NodeInfo nodeInfo(@Value("${server.port}") int port) throws UnknownHostException {
		return new NodeInfo(InetAddress.getLocalHost().getHostAddress(), port, ProcessHandle.current().pid());
	}

	@Override
	public void run(ApplicationArguments args) {
		log.info("Application started with command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
		log.info("NonOptionArgs: {}", args.getNonOptionArgs());
		log.info("OptionNames: {}", args.getOptionNames());

		for (String name : args.getOptionNames()){
			log.info("arg-" + name + "=" + args.getOptionValues(name));
		}

	}

}
