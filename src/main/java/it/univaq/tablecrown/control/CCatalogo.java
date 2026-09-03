package it.univaq.tablecrown.control;

import it.univaq.tablecrown.dao.PersistentManager;
import it.univaq.tablecrown.entity.EBustine;
import it.univaq.tablecrown.entity.EGiocoDaTavolo;
import it.univaq.tablecrown.entity.EPortaDadi;
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
 * Controller deputato alla visualizzazione dei cataloghi dei prodotti e della ricerca.
 */
public class CCatalogo extends BaseController{

    public CCatalogo() {
        super();
    }

    /**
     * Mostra la pagina del catalogo dei giochi da tavolo.
     * URL: GET /catalogo/giochi-da-tavolo
     */
    public void mostraCatalogoGiochi(HttpServletRequest request, HttpServletResponse response, EntityManager em) throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) return;

        int pagina = estraiPaginaRichiesta(request);
        String query = request.getParameter("q");
        if (query != null) query = query.trim();

        Map<String, Object> risultatoGrezzo;
        Map<String, Object> filtri;

        PersistentManager pm = new PersistentManager(em);

        if (query != null && !query.isEmpty()) {
            filtri = new HashMap<>();
            risultatoGrezzo = pm.PMricercaGiochi(query, RISULTATI_PER_PAGINA, (pagina - 1) * RISULTATI_PER_PAGINA);
        } else {
            filtri = estraiFiltriGiochi(request);
            risultatoGrezzo = pm.PMfindGiochi(filtri, RISULTATI_PER_PAGINA, (pagina - 1) * RISULTATI_PER_PAGINA);
            filtri = completaFiltriPrezzo(filtri, risultatoGrezzo);
        }

        renderCatalogo(request, response, "catalogoGiochi", risultatoGrezzo, pagina, filtri, query);
    }

    /**
     * Mostra il catalogo delle bustine.
     * URL: GET /catalogo/bustine
     */
    public void mostraCatalogoBustine(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) return;

        int pagina = estraiPaginaRichiesta(request);
        String query = request.getParameter("q");
        if (query != null) query = query.trim();

        Map<String, Object> risultatoGrezzo;
        Map<String, Object> filtri;

        PersistentManager pm = new PersistentManager(em);

        if (query != null && !query.isEmpty()) {
            filtri = new HashMap<>();
            risultatoGrezzo = pm.PMricercaBustine(query, RISULTATI_PER_PAGINA, (pagina - 1) * RISULTATI_PER_PAGINA);
        } else {
            filtri = estraiFiltriPrezzo(request);
            risultatoGrezzo = pm.PMfindBustine(filtri, RISULTATI_PER_PAGINA, (pagina - 1) * RISULTATI_PER_PAGINA);
            filtri = completaFiltriPrezzo(filtri, risultatoGrezzo);
        }

        renderCatalogo(request, response, "catalogoBustine", risultatoGrezzo, pagina, filtri, query);
    }

    /**
     * Mostra il catalogo dei porta dadi.
     * URL: GET /catalogo/porta-dadi
     */
    public void mostraCatalogoPortaDadi(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) return;

        int pagina = estraiPaginaRichiesta(request);
        String query = request.getParameter("q");
        if (query != null) query = query.trim();

        Map<String, Object> risultatoGrezzo;
        Map<String, Object> filtri;

        PersistentManager pm = new PersistentManager(em);

        if (query != null && !query.isEmpty()) {
            filtri = new HashMap<>();
            risultatoGrezzo = pm.PMricercaPortaDadi(query, RISULTATI_PER_PAGINA, (pagina - 1) * RISULTATI_PER_PAGINA);
        } else {
            filtri = estraiFiltriPrezzo(request);
            risultatoGrezzo = pm.PMfindPortaDadi(filtri, RISULTATI_PER_PAGINA, (pagina - 1) * RISULTATI_PER_PAGINA);
            filtri = completaFiltriPrezzo(filtri, risultatoGrezzo);
        }

        renderCatalogo(request, response, "catalogoPortaDadi", risultatoGrezzo, pagina, filtri, query);
    }

    /**
     * Mostra il catalogo unico delle offerte
     * URL: GET /offerte
     */
    public void mostraOfferte(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) return;

        int pagina = estraiPaginaRichiesta(request);

        PersistentManager pm = new PersistentManager(em);

        Map<String, Object> risultatoGrezzo = pm.PMfindProdottiInOfferta(RISULTATI_PER_PAGINA, (pagina - 1) * RISULTATI_PER_PAGINA);

        renderCatalogo(request, response, "catalogoOfferte", risultatoGrezzo, pagina, new HashMap<>(), null);
    }

    /**
     * Restituisce l'URL del catalogo specifico in base all'istanza del prodotto.
     */
    //TODO: forse va spostato nel base controller/usato per il dettaglio del prodotto
    protected String urlCatalogo(EProdotto prodotto) {
        if (prodotto instanceof EGiocoDaTavolo) {
            return "/catalogo/giochi-da-tavolo";
        }
        if (prodotto instanceof EBustine) {
            return "/catalogo/bustine";
        }
        if (prodotto instanceof EPortaDadi) {
            return "/catalogo/porta-dadi";
        }
        return "/";
    }

    @Override
    protected List<Map<String, String>> getBreadcrumbs(String currentPage) {
        List<Map<String, String>> breadcrumbs = new ArrayList<>();

        Map<String, String> home = new HashMap<>();
        home.put("label", "Home");
        home.put("url", "/");
        breadcrumbs.add(home);

        Map<String, String> current = new HashMap<>();
        switch (currentPage != null ? currentPage : "") {
            case "catalogoGiochi":
                current.put("label", "Giochi da tavolo");
                current.put("url", "/catalogo/giochi-da-tavolo");
                break;
            case "catalogoBustine":
                current.put("label", "Bustine");
                current.put("url", "/catalogo/bustine");
                break;
            case "catalogoPortaDadi":
                current.put("label", "Porta Dadi");
                current.put("url", "/catalogo/porta-dadi");
                break;
            case "catalogoOfferte":
                current.put("label", "Offerte");
                current.put("url", "/offerte");
                break;
            default:
                return breadcrumbs;
        }

        breadcrumbs.add(current);
        return breadcrumbs;
    }
}
