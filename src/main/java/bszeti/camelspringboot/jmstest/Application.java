package bszeti.camelspringboot.jmstest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
	

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


	@Bean 
	public String messageWithSetLenght (@Value("${send.message.length}") Integer sendMessageLength){
		if (sendMessageLength>0) {
            return String.format("%1$"+sendMessageLength+ "s", "").replace(" ","M");
        }
		return "";
	}
}
