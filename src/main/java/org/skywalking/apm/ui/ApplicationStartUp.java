package org.skywalking.apm.ui;

import org.skywalking.apm.ui.tools.CollectorUIServerGetterTimer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class ApplicationStartUp extends SpringBootServletInitializer {

    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = SpringApplication.run(ApplicationStartUp.class, args);
        CollectorUIServerGetterTimer.INSTANCE.start(applicationContext);
    }
}
