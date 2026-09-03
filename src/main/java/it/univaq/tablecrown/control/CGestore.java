package it.univaq.tablecrown.control;

import it.univaq.tablecrown.dao.PersistentManager;
import it.univaq.tablecrown.entity.*;
import it.univaq.tablecrown.entity.enumerativi.*;
import it.univaq.tablecrown.utility.UFlashMessage;
import it.univaq.tablecrown.utility.UHTTPMethods;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Controller deputato alla gestione delle funzionalità riservate al Gestore del negozio.
 */
public class CGestore extends BaseController {

    public CGestore() {
        super();
    }

    //==========================================================================
    // RICHIESTE GET - VISUALIZZAZIONE
    //==========================================================================

    /**
     * Mostra la dashboard principale del gestore con le statistiche quantitative.
     * URL: GET /gestore/dashboard
     */
    public void mostraDashboardGestore(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (!requireRole(request, response, EGestore.class)) {
            return;
        }

        PersistentManager pm = new PersistentManager(em);

        long ordiniTotali = pm.PMcontaOrdiniTotali();
        double venditeTotali = pm.PMcontaVenditeTotali();

        Map<String, Object> datiPagina = new HashMap<>();
        datiPagina.put("vista", "gestore_dashboard");
        datiPagina.put("ordiniTotali", ordiniTotali);
        datiPagina.put("venditeTotali", venditeTotali);

        preparaDatiLayout(request, "gestore_dashboard", datiPagina);
        renderizza("gestore_dashboard.ftl", request, response);
    }

    /**
     * Mostra il catalogo dei giochi da tavolo specifico per il gestore.
     * URL: GET /gestore/catalogo/giochi-da-tavolo
     */
    public void mostraCatalogoGiochiGestore(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (!requireRole(request, response, EGestore.class)) {
            return;
        }

        PersistentManager pm = new PersistentManager(em);
        int pagina = estraiPaginaRichiesta(request);
        String query = UHTTPMethods.get(request, "q", null);
        query = (query != null && !query.trim().isEmpty()) ? query.trim() : null;

        Map<String, Object> risultatoGrezzo;
        Map<String, Object> filtri;

        int offset = (pagina - 1) * RISULTATI_PER_PAGINA;

        if (query != null) {
            filtri = new HashMap<>();
            risultatoGrezzo = pm.PMricercaGiochi(query, RISULTATI_PER_PAGINA, offset);
        } else {
            filtri = estraiFiltriGiochi(request);
            risultatoGrezzo = pm.PMfindGiochi(filtri, RISULTATI_PER_PAGINA, offset);
            filtri = completaFiltriPrezzo(filtri, risultatoGrezzo);
        }

        renderCatalogo(request, response, "gestore_catalogo_giochi", risultatoGrezzo, pagina, filtri, query);
    }

    /**
     * Mostra il catalogo delle bustine specifico per il gestore.
     * URL: GET /gestore/catalogo/bustine
     */
    public void mostraCatalogoBustineGestore(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (!requireRole(request, response, EGestore.class)) {
            return;
        }

        PersistentManager pm = new PersistentManager(em);
        int pagina = estraiPaginaRichiesta(request);
        String query = UHTTPMethods.get(request, "q", null);
        query = (query != null && !query.trim().isEmpty()) ? query.trim() : null;

        Map<String, Object> risultatoGrezzo;
        Map<String, Object> filtri;

        int offset = (pagina - 1) * RISULTATI_PER_PAGINA;

        if (query != null) {
            filtri = new HashMap<>();
            risultatoGrezzo = pm.PMricercaBustine(query, RISULTATI_PER_PAGINA, offset);
        } else {
            filtri = estraiFiltriPrezzo(request);
            risultatoGrezzo = pm.PMfindBustine(filtri, RISULTATI_PER_PAGINA, offset);
            filtri = completaFiltriPrezzo(filtri, risultatoGrezzo);
        }

        renderCatalogo(request, response, "gestore_catalogo_bustine", risultatoGrezzo, pagina, filtri, query);
    }

    /**
     * Mostra il catalogo dei porta dadi specifico per il gestore.
     * URL: GET /gestore/catalogo/porta-dadi
     */
    public void mostraCatalogoPortaDadiGestore(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (!requireRole(request, response, EGestore.class)) {
            return;
        }

        PersistentManager pm = new PersistentManager(em);
        int pagina = estraiPaginaRichiesta(request);
        String query = UHTTPMethods.get(request, "q", null);
        query = (query != null && !query.trim().isEmpty()) ? query.trim() : null;

        Map<String, Object> risultatoGrezzo;
        Map<String, Object> filtri;

        int offset = (pagina - 1) * RISULTATI_PER_PAGINA;

        if (query != null) {
            filtri = new HashMap<>();
            risultatoGrezzo = pm.PMricercaPortaDadi(query, RISULTATI_PER_PAGINA, offset);
        } else {
            filtri = estraiFiltriPrezzo(request);
            risultatoGrezzo = pm.PMfindPortaDadi(filtri, RISULTATI_PER_PAGINA, offset);
            filtri = completaFiltriPrezzo(filtri, risultatoGrezzo);
        }

        renderCatalogo(request, response, "gestore_catalogo_portadadi", risultatoGrezzo, pagina, filtri, query);
    }

    /**
     * Mostra il form per creare un nuovo Gioco da Tavolo.
     * URL: GET /gestore/crea/gioco-da-tavolo
     */
    public void mostraFormCreaGiocoGestore(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (!requireRole(request, response, EGestore.class)) {
            return;
        }

        PersistentManager pm = new PersistentManager(em);
        Map<String, Object> risultatoGrezzo = pm.PMfindGiochi(new HashMap<>(), 500, 0);

        List<?> giochiDisponibili = (risultatoGrezzo != null && risultatoGrezzo.get("risultati") != null)
                ? (List<?>) risultatoGrezzo.get("risultati")
                : Collections.emptyList();

        Map<String, Object> datiPagina = new HashMap<>();
        datiPagina.put("vista", "gestore_creazione_gioco");
        datiPagina.put("giochiDisponibili", giochiDisponibili);
        datiPagina.put("danno_enum", LivelloDannoGiochi.values());
        datiPagina.put("difficolta_enum", DifficoltaGioco.values());
        datiPagina.put("categoria_enum", Categoria.values());
        datiPagina.put("lingue_enum", LinguaGioco.values());

        preparaDatiLayout(request, "gestore_creazione_gioco", datiPagina);
        renderizza("gestore_creazione_gioco.ftl", request, response);
    }

    /**
     * Mostra il form per creare nuove Bustine.
     * URL: GET /gestore/crea/bustine
     */
    public void mostraFormCreaBustineGestore(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!requireRole(request, response, EGestore.class)) {
            return;
        }

        Map<String, Object> datiPagina = new HashMap<>();
        datiPagina.put("vista", "gestore_creazione_bustine");

        preparaDatiLayout(request, "gestore_creazione_bustine", datiPagina);
        renderizza("gestore_creazione_bustine.ftl", request, response);
    }

    /**
     * Mostra il form per creare nuovi Porta Dadi.
     * URL: GET /gestore/crea/porta-dadi
     */
    public void mostraFormCreaPortaDadiGestore(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!requireRole(request, response, EGestore.class)) {
            return;
        }

        Map<String, Object> datiPagina = new HashMap<>();
        datiPagina.put("vista", "gestore_creazione_portadadi");

        preparaDatiLayout(request, "gestore_creazione_portadadi", datiPagina);
        renderizza("gestore_creazione_portadadi.ftl", request, response);
    }

    //==========================================================================
    // AZIONI CRUD SU PRODOTTI
    //==========================================================================

    /**
     * Crea un nuovo gioco da tavolo a partire dai dati inviati dal form.
     * URL: POST /gestore/catalogo/giochi-da-tavolo/nuovo
     */
    public void creaGiocoDaTavolo(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws IOException {

        if (!requireRole(request, response, EGestore.class)) {
            return;
        }

        HttpSession session = request.getSession(true);
        PersistentManager pm = new PersistentManager(em);

        try {
            // 1. Dati base del prodotto
            String nomeProdotto = UHTTPMethods.postString(request, "nomeProdotto", 255);
            String descrizioneProdotto = UHTTPMethods.postString(request, "descrizioneProdotto", 2000);
            float prezzo = UHTTPMethods.postFloat(request, "prezzoListino", 0f, Float.MAX_VALUE);
            int quantita = UHTTPMethods.postInt(request, "quantita", 0, Integer.MAX_VALUE);
            DisponibilitaProdotto disponibilita = Enum.valueOf(DisponibilitaProdotto.class,
                    UHTTPMethods.postString(request, "disponibilita", null));

            // 2. Salvataggio Immagine tramite BaseController
            String imgProdotto = salvaImmagineSuDisco(request, "img_prodotto", "prodotti");
            if (imgProdotto == null) {
                imgProdotto = "uploads/prodotti/default_game.png";
            }

            // 3. Categorie e Componenti
            String[] categorieRaw = UHTTPMethods.postArray(request, "categoria", true);
            Set<Categoria> categorie = new HashSet<>(parseEnumArray(categorieRaw, Categoria.class));

            String[] componentiArray = UHTTPMethods.postArray(request, "componenti", false);
            Set<String> componentiSet = (componentiArray != null)
                    ? new HashSet<>(Arrays.asList(componentiArray))
                    : new HashSet<>();

            // 4. Caratteristiche specifiche di gioco
            DifficoltaGioco difficolta = Enum.valueOf(DifficoltaGioco.class,
                    UHTTPMethods.postString(request, "difficolta", null));
            LinguaGioco lingua = Enum.valueOf(LinguaGioco.class,
                    UHTTPMethods.postString(request, "lingua", null));

            int numeroGiocatoriMin = UHTTPMethods.postInt(request, "numeroGiocatoriMin", 1, Integer.MAX_VALUE);
            int numeroGiocatoriMax = UHTTPMethods.postInt(request, "numeroGiocatoriMax", 1, Integer.MAX_VALUE);
            int etaMinima = UHTTPMethods.postInt(request, "etaMinima", 1, Integer.MAX_VALUE);
            int durataMedia = UHTTPMethods.postInt(request, "durataMedia", 1, Integer.MAX_VALUE);

            // 5. Ricerca eventuale Gioco Base (se è un'espansione)
            String giocoBaseIdRaw = UHTTPMethods.get(request, "giocoBaseId", null);
            EGiocoDaTavolo giocoBase = null;
            if (giocoBaseIdRaw != null && !giocoBaseIdRaw.trim().isEmpty()) {
                Long giocoBaseId = Long.parseLong(giocoBaseIdRaw.trim());
                giocoBase = pm.PMgetObjOnAttribute(EGiocoDaTavolo.class, "idProdotto", giocoBaseId);
            }

            // 6. Preparazione eventuale Danno iniziale
            boolean danneggiato = UHTTPMethods.postBool(request, "danneggiato", false);
            LivelloDannoGiochi livelloDanno = null;
            String descrizioneDanno = null;

            if (danneggiato) {
                livelloDanno = Enum.valueOf(LivelloDannoGiochi.class,
                        UHTTPMethods.postString(request, "livelloDanno", null));
                descrizioneDanno = UHTTPMethods.get(request, "descrizioneDanno", "");
            }

            // 7. Istanziazione Atomica tramite Costruttore Completo
            EGiocoDaTavolo gioco = new EGiocoDaTavolo(
                    nomeProdotto,
                    descrizioneProdotto,
                    disponibilita,
                    quantita,
                    prezzo,
                    categorie,
                    componentiSet,
                    difficolta,
                    lingua,
                    imgProdotto,
                    giocoBase,
                    numeroGiocatoriMin,
                    numeroGiocatoriMax,
                    etaMinima,
                    durataMedia,
                    livelloDanno,
                    descrizioneDanno
            );

            // 8. Gestione eventuale Sconto iniziale
            applicaScontoInizialeSePresente(request, gioco);

            // 9. Salvataggio su Database
            boolean salvato = pm.PMsaveObj(gioco);
            if (!salvato) {
                throw new RuntimeException("Impossibile salvare il nuovo gioco da tavolo nel database.");
            }

            UFlashMessage.addMessage(session, "success", "Gioco da tavolo pubblicato con successo!");
            response.sendRedirect(request.getContextPath() + "/gestore/catalogo/giochi-da-tavolo");

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/gestore/crea/gioco-da-tavolo");
        }
    }

    /**
     * URL: POST /gestore/catalogo/bustine/nuovo
     */
    public void creaBustine(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws IOException {

        if (!requireRole(request, response, EGestore.class)) {
            return;
        }

        HttpSession session = request.getSession(true);
        PersistentManager pm = new PersistentManager(em);

        try {
            String nomeProdotto = UHTTPMethods.postString(request, "nomeProdotto", 255);
            String descrizioneProdotto = UHTTPMethods.postString(request, "descrizioneProdotto", 2000);
            float prezzo = UHTTPMethods.postFloat(request, "prezzoListino", 0f, Float.MAX_VALUE);
            int quantita = UHTTPMethods.postInt(request, "quantita", 0, Integer.MAX_VALUE);

            // Salvataggio Immagine
            String imgProdotto = salvaImmagineSuDisco(request, "img_prodotto", "prodotti");
            if (imgProdotto == null) {
                imgProdotto = "uploads/prodotti/default_sleeves.png";
            }

            DisponibilitaProdotto disponibilita = Enum.valueOf(DisponibilitaProdotto.class,
                    UHTTPMethods.postString(request, "disponibilita", null));

            // Istanziazione Entità
            EBustine bustina = new EBustine(nomeProdotto, descrizioneProdotto, disponibilita, quantita, imgProdotto, prezzo);

            if (disponibilita == DisponibilitaProdotto.NON_DISPONIBILE) {
                bustina.rimuoviProdotto();
            }

            applicaScontoInizialeSePresente(request, bustina);

            boolean salvato = pm.PMsaveObj(bustina);
            if (!salvato) {
                throw new RuntimeException("Impossibile salvare le nuove bustine.");
            }

            UFlashMessage.addMessage(session, "success", "Bustine pubblicate con successo!");
            response.sendRedirect(request.getContextPath() + "/gestore/catalogo/bustine");

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/gestore/crea/bustine");
        }
    }

    /**
     * URL: POST /gestore/catalogo/porta-dadi/nuovo
     */
    public void creaPortaDadi(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws IOException {

        if (!requireRole(request, response, EGestore.class)) {
            return;
        }

        HttpSession session = request.getSession(true);
        PersistentManager pm = new PersistentManager(em);

        try {
            String nomeProdotto = UHTTPMethods.postString(request, "nomeProdotto", 255);
            String descrizioneProdotto = UHTTPMethods.postString(request, "descrizioneProdotto", 2000);
            float prezzo = UHTTPMethods.postFloat(request, "prezzoListino", 0f, Float.MAX_VALUE);
            int quantita = UHTTPMethods.postInt(request, "quantita", 0, Integer.MAX_VALUE);

            // Salvataggio Immagine
            String imgProdotto = salvaImmagineSuDisco(request, "img_prodotto", "prodotti");
            if (imgProdotto == null) {
                imgProdotto = "uploads/prodotti/default_diceholder.png";
            }

            DisponibilitaProdotto disponibilita = Enum.valueOf(DisponibilitaProdotto.class,
                    UHTTPMethods.postString(request, "disponibilita", null));

            // Istanziazione Entità
            EPortaDadi portaDadi = new EPortaDadi(nomeProdotto, descrizioneProdotto, disponibilita, quantita, imgProdotto, prezzo);

            if (disponibilita == DisponibilitaProdotto.NON_DISPONIBILE) {
                portaDadi.rimuoviProdotto();
            }

            applicaScontoInizialeSePresente(request, portaDadi);

            boolean salvato = pm.PMsaveObj(portaDadi);
            if (!salvato) {
                throw new RuntimeException("Impossibile salvare il porta dadi.");
            }

            UFlashMessage.addMessage(session, "success", "Porta dadi pubblicato con successo!");
            response.sendRedirect(request.getContextPath() + "/gestore/catalogo/porta-dadi");

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/gestore/crea/porta-dadi");
        }
    }
    /**
     * Rimuove (soft-delete) un prodotto dal catalogo impostando la disponibilità a "Non disponibile".
     * URL: POST /gestore/catalogo/prodotto/elimina
     */
    public void eliminaProdottoGestore(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws IOException {

        if (!requireRole(request, response, EGestore.class)) {
            return;
        }

        HttpSession session = request.getSession(true);
        PersistentManager pm = new PersistentManager(em);

        try {
            int idInt = UHTTPMethods.postInt(request, "id_prodotto", 1, Integer.MAX_VALUE);
            Long idProdotto = (long) idInt;

            EProdotto prodotto = pm.PMgetObjOnAttribute(EProdotto.class, "idProdotto", idProdotto);
            if (prodotto == null) {
                throw new IllegalArgumentException("Il prodotto selezionato non esiste.");
            }

            prodotto.rimuoviProdotto();

            boolean salvato = pm.PMsaveObj(prodotto);
            if (!salvato) {
                throw new RuntimeException("Si è verificato un errore durante la rimozione del prodotto.");
            }

            UFlashMessage.addMessage(session, "success", "Prodotto rimosso con successo!");

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/gestore/dashboard");
    }

    /**
     * Aggiorna la quantità in magazzino sommando il delta inviato dal form.
     * URL: POST /gestore/prodotti/quantita
     */
    public void aggiornaQuantitaProdottoGestore(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws IOException {

        if (!requireRole(request, response, EGestore.class)) {
            return;
        }

        HttpSession session = request.getSession(true);
        PersistentManager pm = new PersistentManager(em);

        try {
            int idInt = UHTTPMethods.postInt(request, "id_prodotto", 1, Integer.MAX_VALUE);
            Long idProdotto = (long) idInt;
            int deltaQuantita = UHTTPMethods.postInt(request, "delta_quantita", Integer.MIN_VALUE, Integer.MAX_VALUE);

            EProdotto prodotto = pm.PMgetObjOnAttribute(EProdotto.class, "idProdotto", idProdotto);
            if (prodotto == null) {
                throw new IllegalArgumentException("Il prodotto selezionato non esiste.");
            }

            int nuovaQuantita = prodotto.getQuantita() + deltaQuantita;
            if (nuovaQuantita < 0) {
                throw new IllegalArgumentException("La quantità totale non può essere negativa.");
            }

            prodotto.aggiornaQuantita(nuovaQuantita);

            boolean salvato = pm.PMsaveObj(prodotto);
            if (!salvato) {
                throw new RuntimeException("Si è verificato un errore durante l'aggiornamento della quantità.");
            }

            UFlashMessage.addMessage(session, "success", "La quantità del prodotto è stata aggiornata con successo!");

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/gestore/dashboard");
    }

    /**
     * Modifica lo sconto promozionale o applica un danno (giochi da tavolo).
     * URL: POST /gestore/prodotti/modifica
     */
    public void modificaProdottoGestore(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws IOException {

        if (!requireRole(request, response, EGestore.class)) {
            return;
        }

        HttpSession session = request.getSession(true);
        PersistentManager pm = new PersistentManager(em);

        try {
            int idInt = UHTTPMethods.postInt(request, "id_prodotto", 1, Integer.MAX_VALUE);
            Long idProdotto = (long) idInt;
            EProdotto prodotto = pm.PMgetObjOnAttribute(EProdotto.class, "idProdotto", idProdotto);

            if (prodotto == null) {
                throw new IllegalArgumentException("Il prodotto selezionato non esiste.");
            }

            boolean modificaSconto = UHTTPMethods.postBool(request, "modificaSconto", false);
            boolean rimuoviSconto = UHTTPMethods.postBool(request, "rimuoviSconto", false);

            if (modificaSconto) {
                float valoreSconto = UHTTPMethods.postFloat(request, "valoreSconto", 0f, 100f);
                LocalDateTime scadenza = null;
                String scadenzaRaw = UHTTPMethods.get(request, "scadenzaOfferta", null);

                if (scadenzaRaw != null && !scadenzaRaw.trim().isEmpty()) {
                    LocalDate date = UHTTPMethods.postDate(request, "scadenzaOfferta", "yyyy-MM-dd");
                    scadenza = date.atTime(LocalTime.MAX);
                }

                prodotto.getSconto().aggiornaSconto(valoreSconto, scadenza);
            }

            if (rimuoviSconto) {
                prodotto.getSconto().rimuoviSconto();
            }

            boolean danneggiato = UHTTPMethods.postBool(request, "danneggiato", false);
            if (prodotto instanceof EGiocoDaTavolo gioco && danneggiato) {
                LivelloDannoGiochi livello = Enum.valueOf(LivelloDannoGiochi.class,
                        UHTTPMethods.postString(request, "livelloDanno", null));
                String descrizioneDanno = UHTTPMethods.get(request, "descrizioneDanno", "");

                gioco.aggiungiDanno(livello, descrizioneDanno);
            }

            boolean salvato = pm.PMsaveObj(prodotto);
            if (!salvato) {
                throw new RuntimeException("Si è verificato un errore durante il salvataggio delle modifiche.");
            }

            UFlashMessage.addMessage(session, "success", "Prodotto aggiornato con successo!");

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/gestore/dashboard");
    }

    //==========================================================================
    // HELPER PRIVATI CONDIVISI
    //==========================================================================

    private void applicaScontoInizialeSePresente(HttpServletRequest request, EProdotto prodotto) {
        boolean scontoAttivo = UHTTPMethods.postBool(request, "scontoAttivo", false);
        if (scontoAttivo) {
            float valoreSconto = UHTTPMethods.postFloat(request, "valoreSconto", 0f, 100f);
            LocalDateTime scadenza = null;
            String scadenzaRaw = UHTTPMethods.get(request, "scadenzaOfferta", null);

            if (scadenzaRaw != null && !scadenzaRaw.trim().isEmpty()) {
                LocalDate date = UHTTPMethods.postDate(request, "scadenzaOfferta", "yyyy-MM-dd");
                scadenza = date.atTime(LocalTime.MAX);
            }

            prodotto.getSconto().aggiornaSconto(valoreSconto, scadenza);
        }
    }

    private <T extends Enum<T>> List<T> parseEnumArray(String[] rawValues, Class<T> enumClass) {
        List<T> enums = new ArrayList<>();
        for (String v : rawValues) {
            try {
                enums.add(Enum.valueOf(enumClass, v.trim()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (enums.isEmpty()) {
            throw new IllegalArgumentException("Selezionare almeno un valore valido.");
        }
        return enums;
    }

    @Override
    protected List<Map<String, String>> getBreadcrumbs(String currentPage) {
        List<Map<String, String>> breadcrumbs = new ArrayList<>();

        Map<String, String> home = new HashMap<>();
        home.put("label", "Dashboard Gestore");
        home.put("url", "/gestore/dashboard");
        breadcrumbs.add(home);

        switch (currentPage) {
            case "gestore_catalogo_giochi" -> {
                Map<String, String> b = new HashMap<>();
                b.put("label", "Giochi da tavolo");
                b.put("url", "/gestore/catalogo/giochi-da-tavolo");
                breadcrumbs.add(b);
            }
            case "gestore_catalogo_bustine" -> {
                Map<String, String> b = new HashMap<>();
                b.put("label", "Bustine");
                b.put("url", "/gestore/catalogo/bustine");
                breadcrumbs.add(b);
            }
            case "gestore_catalogo_portadadi" -> {
                Map<String, String> b = new HashMap<>();
                b.put("label", "Porta Dadi");
                b.put("url", "/gestore/catalogo/porta-dadi");
                breadcrumbs.add(b);
            }
            case "gestore_creazione_gioco" -> {
                Map<String, String> b = new HashMap<>();
                b.put("label", "Nuovo Gioco");
                b.put("url", "/gestore/crea/gioco-da-tavolo");
                breadcrumbs.add(b);
            }
            case "gestore_creazione_bustine" -> {
                Map<String, String> b = new HashMap<>();
                b.put("label", "Nuove Bustine");
                b.put("url", "/gestore/crea/bustine");
                breadcrumbs.add(b);
            }
            case "gestore_creazione_portadadi" -> {
                Map<String, String> b = new HashMap<>();
                b.put("label", "Nuovo Porta Dadi");
                b.put("url", "/gestore/crea/porta-dadi");
                breadcrumbs.add(b);
            }
        }

        return breadcrumbs;
    }
}
