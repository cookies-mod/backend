package dev.morazzer.cookies.backend;

import dev.morazzer.cookies.backend.auth.JwtUtils;
import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.context.support.RequestHandledEvent;

@SpringBootApplication
@EnableScheduling
public class BackendApplication {
    public static boolean devEnvironment = false;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean(autowireCandidate = false)
    public String creatAuth(JwtUtils jwtUtils) {
        final String meow = jwtUtils.createToken("unknown", UUID.randomUUID(), "meow", Collections.emptyList());
        System.out.println(meow);
        return meow;
    }

    @Bean(name = "isDevEnvironment")
    public boolean isDevelopment(Environment environment) {
        devEnvironment = environment.acceptsProfiles(Profiles.of("dev"));
        return devEnvironment;
    }

}
