package net.cubosoft.tafelab;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CsTafelabMqttApplication {

	private static volatile ConfigurableApplicationContext context;
	private static ClassLoader mainThreadClassLoader;
	
	public static void main(String[] args) {
		mainThreadClassLoader = Thread.currentThread().getContextClassLoader();
		context = SpringApplication.run(CsTafelabMqttApplication.class, args);
	}
	
	  public static void restart() {
	        ApplicationArguments args = context.getBean(ApplicationArguments.class);

	        Thread thread = new Thread(() -> {
	            context.close();
	            context = SpringApplication.run(CsTafelabMqttApplication.class, args.getSourceArgs());
	        });
	        thread.setContextClassLoader(mainThreadClassLoader);
	        thread.setDaemon(false);
	        thread.start();
	    }

}
