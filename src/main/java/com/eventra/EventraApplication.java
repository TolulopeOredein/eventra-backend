package com.eventra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class EventraApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventraApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void checkDatabaseConnection(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("✅✅✅ DATABASE CONNECTED SUCCESSFULLY! ✅✅✅");
            System.out.println("URL: " + conn.getMetaData().getURL());
        } catch (Exception e) {
            System.err.println("❌❌❌ DATABASE CONNECTION FAILED! ❌❌❌");
            System.err.println("Error: " + e.getMessage());
        }
    }
}