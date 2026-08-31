package it.univaq.tablecrown.listener;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

// annotazione per comunicare a Tomcat che questo codice è una listener (si deve avviare in automatico all'avvio dell'app)
@WebListener
public class AppListener implements ServletContextListener {

    private EntityManagerFactory emf;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // all'accensione di Tomcat si crea l'entity manager factory una sola volta per tutta la vita dell'app
        emf = Persistence.createEntityManagerFactory("TableCrownPU");

        // l'entity manager factory dovrà essere globale, salviamo in memoria il puntatore alla factory
        sce.getServletContext().setAttribute("emf", emf);

        System.out.println("db acceso");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //quando Tomcat si spegne chiudiamo anche l'emf
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("db spento");
        }
    }
}
