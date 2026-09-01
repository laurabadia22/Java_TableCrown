package it.univaq.tablecrown.utility;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import jakarta.servlet.ServletContext;

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
    public static void init(ServletContext servletContext) {
        config = new Configuration(Configuration.VERSION_2_3_32);
        config.setServletContextForTemplateLoading(servletContext, "/WEB-INF/templates");
        config.setDefaultEncoding("UTF-8");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    }

    public static Configuration getConfig() {
        return config;
    }
}