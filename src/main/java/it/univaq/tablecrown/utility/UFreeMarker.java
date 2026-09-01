package it.univaq.tablecrown.utility;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import jakarta.servlet.ServletContext;
import java.io.File;
import java.io.IOException;

/**
 * Configurazione centralizzata del motore di template FreeMarker.
 * Inizializzata una sola volta all'avvio dell'applicazione.
 */
public class UFreeMarker {

    private static Configuration config;

    /**
     * Inizializza la Configuration di FreeMarker. Da chiamare una sola volta,
     * nell'init() del FrontController.
     */
    public static void init(ServletContext servletContext) throws IOException {
        config = new Configuration(Configuration.VERSION_2_3_32);
        config.setDefaultEncoding("UTF-8");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);

        String templatePath = servletContext.getRealPath("/WEB-INF/templates");
        config.setDirectoryForTemplateLoading(new File(templatePath));
    }

    public static Configuration getConfig() {
        return config;
    }
}