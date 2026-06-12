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



}