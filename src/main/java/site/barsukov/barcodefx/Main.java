package site.barsukov.barcodefx;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class Main  {
    public static void main(String[] args) {
        Application.launch(JfxStarter.class, args);
    }

}
