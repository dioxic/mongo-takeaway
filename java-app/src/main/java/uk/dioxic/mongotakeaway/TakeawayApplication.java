package uk.dioxic.mongotakeaway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import uk.dioxic.mongotakeaway.domain.NodeInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class TakeawayApplication {

	public static void main(String[] args) {
		SpringApplication.run(TakeawayApplication.class, args);
	}

	@Bean
	public NodeInfo nodeInfo(@Value("${server.port}") int port) throws UnknownHostException {
		return new NodeInfo(InetAddress.getLocalHost().getHostAddress(), port, ProcessHandle.current().pid());
	}

}
