package it.univaq.tablecrown.control;

import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller deputato alla gestione della HomePage e della navigazione pubblica principale.
 */
public class CHome extends BaseController {

    public CHome() {
        super();
    }

    /**
     * Mostra la HomePage del sito.
     * Recupera i nuovi arrivi ed i prodotti in offerta da mostrare
     * URL: GET / o GET /home
     */
    public void mostraHome(HttpServletRequest request, HttpServletResponse response, EntityManager em) throws ServletException, IOException {
        //Se un gestore tenta di accedere alla Home, viene reindirizzato alla sua deshboard
        if (reindirizzaGestore(request, response)) {
            return;
        }

        //Recupero dei prodotti in offerta
        // TODO: serve ancora il persistent manager con i suoi metodi

        //Recupero dei nuovi arrivi ordinati per data di pubblicazione
        // TODO

        //Preparazione della mappa con i dati della pagina
        // TODO

        //Popolamento delle variabili globali della Request e inoltro dei dati verso Presentation
        // TODO
    }

    /**
     * Override per gestire il percorso Breadcrumb della HomePage
     */
    @Override
    protected List<Map<String, String>> getBreadcrumbs(String currentPage) {
        List<Map<String, String>> breadcrumbs = new ArrayList<>();
        Map<String, String> homeStep = new HashMap<>();
        homeStep.put("label", "Home");
        homeStep.put("url", "/");
        breadcrumbs.add(homeStep);
        return breadcrumbs;
    }
}
