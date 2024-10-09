package it.bankitalia.sisna.proto.xlsimport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class XlsimportApplication {

	public static void main(String[] args) {
		SpringApplication.run(XlsimportApplication.class, args);
	}

}
