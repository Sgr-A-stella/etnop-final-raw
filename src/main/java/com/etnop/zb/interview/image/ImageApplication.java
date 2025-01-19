package com.etnop.zb.interview.image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Image application main entry point
 */
@SpringBootApplication
public class ImageApplication {

	private static Logger logger = LoggerFactory.getLogger(ImageApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ImageApplication.class, args);
		context.registerShutdownHook();

		logger.info("Image application started...");
	}
}
