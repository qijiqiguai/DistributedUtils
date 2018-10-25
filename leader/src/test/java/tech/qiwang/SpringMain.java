package tech.qiwang;


import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SpringMain {
    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringMain.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
