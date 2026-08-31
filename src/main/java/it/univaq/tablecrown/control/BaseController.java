package it.univaq.tablecrown.control;

import it.univaq.tablecrown.entity.EGestore;
import it.univaq.tablecrown.entity.EUtente;
import it.univaq.tablecrown.utility.UFlashMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller astratto di base che centralizza le funzionalità comuni
 */
public abstract class BaseController {

    //Tipi di prodotto validi per il catalogo
    protected static final Map<String, String> TIPI_PRODOTTO = Map.of(
            "giochi-da-tavolo", "Giochi da Tavolo",
            "bustine", "Bustine",
            "porta-dadi", "Porta Dadi"
    );

    protected static final int RISULTATI_PER_PAGINA = 20;
    protected static final List<String> ORDINAMENTO_VALIDI = List.of("prezzo_asc", "prezzo_desc", "novita", "valutazione");
    protected static final List<String> IN_EVIDENZA_VALIDI = List.of("offerte", "novita", "piu_venduti");

    //Costruttore base.
    public BaseController() {
    }

    /**
     * Prepara le variabili globali richieste dalla grafica (navbar, utente loggato, breadcrumbs)
     * e le unisce a quelle specifiche della singola pagina prima dell'inoltro alla vista.
     */
    public void preparaDatiLayout(HttpServletRequest request, String currentPage, Map<String, Object> data) {
        //Dati globali necessari per ogni pagina
        request.setAttribute("baseUrl", request.getContextPath());
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("breadcrumbs", getBreadcrumbs(currentPage));

        //Recupero utente e stato sessione
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object utenteLoggato = session.getAttribute("utenteLoggato");
            if (utenteLoggato != null) {
                request.setAttribute("utente", utenteLoggato);

                // Calcolo conteggio carrello solo per gli utenti standard
                if (utenteLoggato instanceof EUtente) {
                    @SuppressWarnings("unchecked")
                    Map<Long, Integer> carrello = (Map<Long, Integer>) session.getAttribute("carrello");
                    if (carrello != null) {
                        int cartCount = carrello.values().stream().mapToInt(Integer::intValue).sum();
                        if (cartCount > 0) {
                            request.setAttribute("cartCount", cartCount);
                        }
                    }
                }
            }

            //Gestione dei Flash Messages
            if (UFlashMessage.hasMessage(session)) {
                Map<String, List<String>> tuttiIFlash = UFlashMessage.getMessage(session);

                // Prendiamo il primo tipo di messaggio presente (es. "warning" o "success")
                for (Map.Entry<String, List<String>> entry : tuttiIFlash.entrySet()) {
                    List<String> listaMessaggi = entry.getValue();
                    if (listaMessaggi != null && !listaMessaggi.isEmpty()) {
                        request.setAttribute("flash_message", listaMessaggi.get(0));
                        request.setAttribute("flash_type", entry.getKey());
                        break; // Inviamo un solo messaggio attivo alla volta
                    }
                }
            }
        }

        //Inserimento dei dati specifici passati dal controller figlio nella request
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Metodo di default per i breadcrumbs (override nelle sottoclassi se necessario).
     */
    protected List<Map<String, String>> getBreadcrumbs(String currentPage) {
        return new ArrayList<>();
    }

    /**
     * Verifica se un utente è attualmente loggato in sessione
     */
    public boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("utenteLoggato") != null;
    }

    /**
     * Forza l'utente a loggarsi;
     * in caso contrario imposta un messaggio e reindirizza al form di login.
     * Restituisce true se l'utente è loggato, false se è stato eseguito il redirect al login
     */
    public boolean requireLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isLoggedIn(request)) {
            HttpSession session = request.getSession(true);
            UFlashMessage.addMessage(session, "warning", "È necessario effettuare l'accesso per visualizzare questa pagina.");

            response.sendRedirect(request.getContextPath() + "/accedi");
            return false;
        }
        return true;
    }

    /**
     * Verifica che l'utente sia di una determinata classe (EUtente, EGestore)
     * Restituisce true se l'utente ha il ruolo richiesto, altrimenti false.
     */
    public boolean requireRole(HttpServletRequest request, HttpServletResponse response, Class<?> targetRole) throws IOException {
        if (!requireLogin(request, response)) {
            return false;
        }

        Object utenteLoggato = request.getSession().getAttribute("utenteLoggato");

        // Verifichiamo l'istanza dell'oggetto in sessione (es. utenteLoggato instanceof Gestore)
        if (!targetRole.isInstance(utenteLoggato)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso Negato: Non hai i permessi necessari.");
            return false;
        }
        return true;
    }

    /**
     * Se l'utente è loggato ed è un gestore, lo reindirizza automaticamente alla
     * sua dashboard.
     * Restituisce true se è stato eseguito il redirect, altrimenti false.
     */
    protected boolean reindirizzaGestore(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isLoggedIn(request)) {
            Object utenteLoggato = request.getSession().getAttribute("utenteLoggato");

            // Se l'utente in sessione è un'istanza di Gestore (senza Admin)
            if (utenteLoggato instanceof EGestore) {
                response.sendRedirect(request.getContextPath() + "/gestore/dashboard");
                return true;
            }
        }
        return false;
    }

    //==========================================================================
    // METODI UTILI PER PASSAGGIO DATI A PRESENTATION
    //==========================================================================

    // CATALOGO

    /**
     * Valida e restituisce il numero di pagina richiesto dalla query GET.
     * Ritorna un valore >= 1.
     */
    protected int estraiPaginaRichiesta(HttpServletRequest request) {
        String pageParam = request.getParameter("page");
        if (pageParam == null || pageParam.trim().isEmpty()) {
            return 1;
        }
        try {
            int page = Integer.parseInt(pageParam.trim());
            return Math.max(1, page);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /**
     * Calcola il numero totale di pagine.
     */
    protected int calcolaTotalePagine(long totaleRisultati) {
        if (totaleRisultati <= 0) return 1;
        return (int) Math.ceil((double) totaleRisultati / RISULTATI_PER_PAGINA);
    }

    /**
     * Mantiene il numero di pagina entro il range [1, totalePagine].
     */
    protected int clampPagina(int pagina, int totalePagine) {
        if (totalePagine <= 0) return 1;
        return Math.max(1, Math.min(pagina, totalePagine)); //TODO: VEDI IL WARNING
    }

    /**
     * Trasforma il valore di un parametro GET (singolo o multiplo) in una lista di stringhe.
     */
    protected List<String> estraiListaDaRequest(HttpServletRequest request, String parametro) {
        String[] valori = request.getParameterValues(parametro);
        if (valori == null || valori.length == 0) {
            return new ArrayList<>();
        }
        return Arrays.stream(valori)
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Estrae i filtri in base al prezzo e ordinamento condivisi da tutti i prodotti.
     */
    protected Map<String, Object> estraiFiltriPrezzo(HttpServletRequest request) {
        Map<String, Object> filtri = new HashMap<>();

        String priceMinRaw = request.getParameter("price_min");
        String priceMaxRaw = request.getParameter("price_max");
        String ratingMinRaw = request.getParameter("rating_min");
        String ordinamentoRaw = request.getParameter("ordinamento");

        filtri.put("price_min", isNumeric(priceMinRaw) ? Double.parseDouble(priceMinRaw) : 0.0); //TODO: ????
        filtri.put("price_max", isNumeric(priceMaxRaw) ? Double.parseDouble(priceMaxRaw) : null);
        filtri.put("price_range_min", 0.0);
        filtri.put("price_range_max", null); // Verrà impostato dal PM

        filtri.put("disponibilita", estraiListaDaRequest(request, "disponibilita"));

        List<String> inEvidenza = estraiListaDaRequest(request, "in_evidenza_filtro").stream()
                .filter(IN_EVIDENZA_VALIDI::contains)
                .collect(Collectors.toList());
        filtri.put("in_evidenza_filtro", inEvidenza);

        filtri.put("rating_min", isNumeric(ratingMinRaw) ? Double.parseDouble(ratingMinRaw) : 0.0);
        filtri.put("ordinamento", ORDINAMENTO_VALIDI.contains(ordinamentoRaw) ? ordinamentoRaw : null);

        return filtri;
    }

    /**
     * Estrai i filtri specifici per la categoria Giochi da Tavolo.
     */
    protected Map<String, Object> estraiFiltriGiochi(HttpServletRequest request) {
        Map<String, Object> filtri = estraiFiltriPrezzo(request);

        String ageMinRaw = request.getParameter("age_min");
        String playersMinRaw = request.getParameter("players_min");
        String mostraEspansioni = request.getParameter("mostra_espansioni");

        filtri.put("categoria_selected", estraiListaDaRequest(request, "categoria_selected"));
        filtri.put("mostra_espansioni", !"0".equals(mostraEspansioni));
        filtri.put("age_min", isNumeric(ageMinRaw) ? Integer.parseInt(ageMinRaw) : null);
        filtri.put("players_min", isNumeric(playersMinRaw) ? Integer.parseInt(playersMinRaw) : null);
        filtri.put("difficolta", estraiListaDaRequest(request, "difficolta"));
        filtri.put("lingua_selected", estraiListaDaRequest(request, "lingua_selected"));
        filtri.put("danno_selected", estraiListaDaRequest(request, "danno_selected"));

        return filtri;
    }

    /**
     * Completa l'array dei filtri con il valore del prezzo massimo reale restituito dal db.
     */
    protected Map<String, Object> completaFiltriPrezzo(Map<String, Object> filtri, Map<String, Object> risultatoGrezzo) {
        Double rangeMax = 0.0;
        if (risultatoGrezzo != null && risultatoGrezzo.get("rangemax") != null) {
            rangeMax = ((Number) risultatoGrezzo.get("rangemax")).doubleValue();
        }

        filtri.put("price_range_max", rangeMax);
        if (filtri.get("price_max") == null) {
            filtri.put("price_max", rangeMax);
        }

        return filtri;
    }

    /**
     * Prepara la mappa dati globale per il rendering della vista del catalogo.
     */
    protected void renderCatalogo(HttpServletRequest request, HttpServletResponse response, String vista,
                                  Map<String, Object> risultatoGrezzo, int pagina, Map<String, Object> filtri,
                                  String searchQuery) throws ServletException, IOException {

        long totaleRisultati = 0;
        List<?> prodotti = new ArrayList<>();

        if (risultatoGrezzo != null) {
            if (risultatoGrezzo.get("totale") != null) {
                totaleRisultati = ((Number) risultatoGrezzo.get("totale")).longValue();
            }
            if (risultatoGrezzo.get("risultati") != null) {
                prodotti = (List<?>) risultatoGrezzo.get("risultati");
            }
        }

        int totalePagine = calcolaTotalePagine(totaleRisultati);
        pagina = clampPagina(pagina, totalePagine);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current_page", pagina);
        pagination.put("total_pages", totalePagine);

        Map<String, Object> datiPagina = new HashMap<>();
        datiPagina.put("vista", vista);
        datiPagina.put("prodotti", prodotti);
        datiPagina.put("total_results", totaleRisultati);
        datiPagina.put("pagination", pagination);
        datiPagina.put("search_query", searchQuery);
        datiPagina.put("filtri", filtri);

        preparaDatiLayout(request, vista, datiPagina);

        String jspPath = "/WEB-INF/views/catalogo/" + vista + ".jsp";
        request.getRequestDispatcher(jspPath).forward(request, response);
    }

    private boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) return false;
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


}
