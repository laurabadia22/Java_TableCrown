package it.univaq.tablecrown.control;

import it.univaq.tablecrown.dao.PersistentManager;
import it.univaq.tablecrown.entity.EProdotto;
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

        //Istanziazione del PersistentManager
        PersistentManager pm = new PersistentManager(em);

        //Recupero dei prodotti in offerta (max 5 prodotti)
        Map<String, Object> mappaOfferte = pm.PMfindProdottiInOfferta(5, 0);

        @SuppressWarnings("unchecked")
        List<EProdotto> offerte = (List<EProdotto>) mappaOfferte.getOrDefault("risultati", new ArrayList<EProdotto>());

        //Recupero dei nuovi arrivi ordinati per data di pubblicazione decrescente (max 10)
        List<EProdotto> nuoviArrivi = pm.PMgetObjListOrdered(EProdotto.class, "dataPubblicazione", "DESC", 10);

        //Preparazione della mappa con i dati specifici della pagina
        Map<String, Object> datiPagina = new HashMap<>();
        datiPagina.put("offerte", offerte);
        datiPagina.put("nuoviArrivi", nuoviArrivi);

        //Popolamento delle variabili globali della Request e inoltro dei dati verso Presentation
        preparaDatiLayout(request, "home", datiPagina);
        request.getRequestDispatcher("/WEB-INF/templates/home.ftl").forward(request, response);
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
