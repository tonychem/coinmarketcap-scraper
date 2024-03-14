package entrypoint;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import utils.entity.TomcatUrl;

import java.nio.file.Paths;

public class TomcatRunner implements Runnable {
    private final TomcatUrl tomcatUrl;
    private final Tomcat tomcat;

    private final Context context;

    public TomcatRunner(Tomcat tomcat, TomcatUrl tomcatUrl) {
        this.tomcat = tomcat;
        this.tomcatUrl = tomcatUrl;
        context = tomcat.addContext("", Paths.get(".").toString());
    }

    /**
     * Запуск Tomcat сервера
     */
    @Override
    public void run() {
        tomcat.setPort(tomcatUrl.port());
        tomcat.setHostname(tomcatUrl.hostname());
        tomcat.getHost().setAppBase(".");

        Connector connector = tomcat.getConnector();
        connector.setProperty("maxThreads", "2");

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Динамическая регистрация сервлета на сервере
     */
    public void registerServlet(HttpServlet servlet) {
        Class<?> servletClass = servlet.getClass();
        Tomcat.addServlet(context, servletClass.getSimpleName(), servlet);
        String servletPattern = servletClass.getAnnotation(WebServlet.class)
                .value()[0];
        context.addServletMappingDecoded(servletPattern, servletClass.getSimpleName());
    }
}
