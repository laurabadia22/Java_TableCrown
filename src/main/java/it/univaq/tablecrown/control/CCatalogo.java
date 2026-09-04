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

        // 1. Estrazione dei filtri dall'URL (request)
        Map<String, Object> filtri = new HashMap<>();

        String prezzoMin = request.getParameter("prezzoMin");
        if (prezzoMin != null && !prezzoMin.trim().isEmpty()) {
            try {
                filtri.put("prezzoMin", Double.parseDouble(prezzoMin));
            } catch (NumberFormatException ignored) {}
        }

        String prezzoMax = request.getParameter("prezzoMax");
        if (prezzoMax != null && !prezzoMax.trim().isEmpty()) {
            try {
                filtri.put("prezzoMax", Double.parseDouble(prezzoMax));
            } catch (NumberFormatException ignored) {}
        }

        PersistentManager pm = new PersistentManager(em);

        // 2. Passaggio della mappa filtri al metodo del PersistentManager / DAO
        Map<String, Object> risultatoGrezzo = pm.PMfindProdottiInOfferta(
                filtri,
                RISULTATI_PER_PAGINA,
                (pagina - 1) * RISULTATI_PER_PAGINA
        );

        // 3. Passaggio della mappa filtri alla vista (invece di new HashMap<>())
        renderCatalogo(request, response, "catalogoOfferte", risultatoGrezzo, pagina, filtri, null);
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
