package it.univaq.tablecrown.control;

import it.univaq.tablecrown.dao.PersistentManager;
import it.univaq.tablecrown.entity.EGestore;
import it.univaq.tablecrown.entity.EProdotto;
import it.univaq.tablecrown.entity.EUtente;
import it.univaq.tablecrown.utility.UFlashMessage;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.io.InputStream;
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
        request.setAttribute("base_url", request.getContextPath());
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

    /**
     * TODO: da controllare
     */
    protected void renderizza(String nomeTemplate, HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        //Costruisce il model leggendo gli attributi della request
        Map<String, Object> model = new HashMap<>();
        Enumeration<String> nomiAttributi = request.getAttributeNames();
        while (nomiAttributi.hasMoreElements()) {
            String nome = nomiAttributi.nextElement();
            model.put(nome, request.getAttribute(nome));
        }

        //Carica il template e lo processa scrivendo l'HTML sulla response
        try {
            response.setContentType("text/html; charset=UTF-8");
            freemarker.template.Template template =
                    it.univaq.tablecrown.utility.UFreeMarker.getConfig().getTemplate(nomeTemplate);
            template.process(model, response.getWriter());
        } catch (freemarker.template.TemplateException | IOException e) {
            throw new IOException("Errore nel rendering del template " + nomeTemplate, e);
        }
    }

    //==========================================================================
    // METODI UTILI IN COMUNE FRA I CONTROLLER
    //==========================================================================

    // GENERICI

    /**
     * Formatta un importo come stringa con 2 decimali fissi e punto come separatore.
     */
    protected String formattaImporto(double importo) {
        return String.format(Locale.US, "%.2f", importo);
    }

    /**
     * Recupera in sicurezza un Part (file caricato) dalla richiesta HTTP multipart.
     */
    //TODO: DA RIVEDERE
    protected Part estraiPart(HttpServletRequest request, String nomeCampo) {
        try {
            return request.getPart(nomeCampo);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Converte il Part di un'immagine caricata nel relativo array di byte (byte[]).
     * Restituisce null se l'immagine non è stata selezionata nel form.
     */
    //TODO: DA RIVEDERE
    protected byte[] estraiImmagine(Part filePart) throws IOException {
        if (filePart == null || filePart.getSize() == 0 || filePart.getSubmittedFileName() == null || filePart.getSubmittedFileName().trim().isEmpty()) {
            return null;
        }
        try (InputStream is = filePart.getInputStream()) {
            return is.readAllBytes();
        }
    }

    // CATALOGO/PRODOTTI

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

        String priceMinRaw = request.getParameter("prezzoMin");
        String priceMaxRaw = request.getParameter("prezzoMax");
        String ratingMinRaw = request.getParameter("ratingMin");
        String ordinamentoRaw = request.getParameter("ordinamento");

        filtri.put("prezzoMin", isNumeric(priceMinRaw) ? Double.parseDouble(priceMinRaw) : 0.0); //TODO: ????
        filtri.put("prezzoMax", isNumeric(priceMaxRaw) ? Double.parseDouble(priceMaxRaw) : null);
        filtri.put("prezzoRangeMin", 0.0);
        filtri.put("prezzoRangeMax", null); // Verrà impostato dal PM

        filtri.put("disponibilita", estraiListaDaRequest(request, "disponibilita"));

        List<String> valoriEvidenza = estraiListaDaRequest(request, "inEvidenzaFiltro");
        List<String> inEvidenza = new ArrayList<>();
        if (valoriEvidenza != null && IN_EVIDENZA_VALIDI != null) {
            for (String s : valoriEvidenza) {
                if (s != null && IN_EVIDENZA_VALIDI.contains(s)) {
                    inEvidenza.add(s);
                }
            }
        }

        filtri.put("inEvidenzaFiltro", inEvidenza);

        filtri.put("ratingMin", isNumeric(ratingMinRaw) ? Double.parseDouble(ratingMinRaw) : 0.0);

        // Controllo preventivo su ordinamentoRaw per evitare NullPointerException con List.of()
        String ordinamentoValido = null;
        if (ordinamentoRaw != null && !ordinamentoRaw.trim().isEmpty() && ORDINAMENTO_VALIDI != null && ORDINAMENTO_VALIDI.contains(ordinamentoRaw)) {
            ordinamentoValido = ordinamentoRaw;
        }
        filtri.put("ordinamento", ordinamentoValido);

        return filtri;
    }

    /**
     * Estrai i filtri specifici per la categoria Giochi da Tavolo.
     */
    protected Map<String, Object> estraiFiltriGiochi(HttpServletRequest request) {
        Map<String, Object> filtri = estraiFiltriPrezzo(request);

        String ageMinRaw = request.getParameter("etaMinima");
        String playersMinRaw = request.getParameter("giocatoriMin");
        String mostraEspansioni = request.getParameter("mostraEspansioni");

        filtri.put("categoria", estraiListaDaRequest(request, "categoria"));
        filtri.put("mostraEspansioni", !"0".equals(mostraEspansioni));
        filtri.put("etaMinima", isNumeric(ageMinRaw) ? Integer.parseInt(ageMinRaw) : null);
        filtri.put("giocatoriMin", isNumeric(playersMinRaw) ? Integer.parseInt(playersMinRaw) : null);
        filtri.put("difficolta", estraiListaDaRequest(request, "difficolta"));
        filtri.put("lingua", estraiListaDaRequest(request, "lingua"));
        filtri.put("danno", estraiListaDaRequest(request, "danno"));

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

        filtri.put("prezzoRangeMax", rangeMax);
        if (filtri.get("prezzoMax") == null) {
            filtri.put("prezzoMax", rangeMax);
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

        Map<String, Object> datiPagina = new HashMap<>();
        datiPagina.put("vista", vista);
        datiPagina.put("prodotti", prodotti);
        datiPagina.put("totaleRisultati", totaleRisultati);
        datiPagina.put("paginaCorrente", pagina);
        datiPagina.put("totalePagine", totalePagine);
        datiPagina.put("query", searchQuery != null ? searchQuery : "");
        datiPagina.put("filtri", filtri);

        // Valori di default per le opzioni dei select/checkbox nei template
        datiPagina.put("difficoltaOptions", Collections.emptyList());
        datiPagina.put("categorieDisponibili", Collections.emptyList());

        //Riversiamo i dati nella request per renderli compatibili con il metodo renderizza()
        for (Map.Entry<String, Object> entry : datiPagina.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }

        preparaDatiLayout(request, vista, datiPagina);

        //Deleghiamo il rendering al metodo generico creato già nel BaseController
        renderizza(vista + ".ftl", request, response);

//        //TODO: BOH DA CAPIRE STA COSA (DA CANCELLARE??) (SERVONO ENTRAMBI renderCatalogo E renderizza??)
//        //Rendering con FreeMarker
//        Map<String, Object> model = new HashMap<>();
//        Enumeration<String> nomiAttributi = request.getAttributeNames();
//        while (nomiAttributi.hasMoreElements()) {
//            String nome = nomiAttributi.nextElement();
//            model.put(nome, request.getAttribute(nome));
//        }
//
//        try {
//            response.setContentType("text/html; charset=UTF-8");
//            // Poiché i file .ftl sono direttamente dentro /WEB-INF/templates/
//            freemarker.template.Template template =
//                    it.univaq.tablecrown.utility.UFreeMarker.getConfig().getTemplate(vista + ".ftl");
//            template.process(model, response.getWriter());
//        } catch (freemarker.template.TemplateException | IOException e) {
//            throw new ServletException("Errore nel rendering del template " + vista, e);
//        }
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

    /**
     * Restituisce prodotti "correlati" delegando la ricerca al PersistentManager,
     * escludendo gli ID passati in idsEsclusi e limitando il risultato a limit.
     */
    protected List<EProdotto> prodottiCorrelati(EntityManager em, List<Long> idsEsclusi, int limit) {
        if (idsEsclusi == null) {
            idsEsclusi = Collections.emptyList();
        }
        PersistentManager pm = new PersistentManager(em);
        return pm.PMfindCorrelati(idsEsclusi, limit);
    }

    /**
     * Overload di comodità con limit di default a 8.
     */
    protected List<EProdotto> prodottiCorrelati(EntityManager em, List<Long> idsEsclusi) {
        return prodottiCorrelati(em, idsEsclusi, 8);
    }

    /**
     * Simile a utenteCorrente(), ma utile in contesti in cui il login è facoltativo
     * (es. pagine pubbliche che mostrano contenuto diverso se l'utente è loggato).
     * Non forza mai un redirect: restituisce null se non loggato o senza ruolo 'utente'.
     */
    protected EUtente utenteCorrenteOpzionale(HttpServletRequest request, EntityManager em) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object utenteLoggato = session.getAttribute("utenteLoggato");
        if (utenteLoggato instanceof EUtente) {
            return (EUtente) utenteLoggato;
        }

        // Se in sessione è stato salvato idPersona (Long o Integer)
        Object idPersonaObj = session.getAttribute("idPersona");
        Long idPersona = null;
        if (idPersonaObj instanceof Long) {
            idPersona = (Long) idPersonaObj;
        } else if (idPersonaObj instanceof Integer) {
            idPersona = ((Integer) idPersonaObj).longValue();
        }

        if (idPersona == null) {
            return null;
        }

        PersistentManager pm = new PersistentManager(em);
        return pm.PMgetObjOnAttribute(EUtente.class, "idPersona", idPersona);
    }

    /**
     * Recupera l'utente correntemente loggato nel DB, verificando che abbia il ruolo 'utente'.
     * Se l'utente non è valido o non viene trovato nel DB, invalida la sessione, ripulisce
     * i cookie e reindirizza alla pagina di login.
     *
     * @return L'istanza di EUtente, oppure null se la risposta è già stata reindirizzata (redirect).
     */
    protected EUtente utenteCorrente(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws IOException {

        // Verifica del ruolo obbligatorio
        if (!requireRole(request, response, EUtente.class)) {
            return null; // requireRole ha già gestito il redirect se fallito
        }

        EUtente utente = utenteCorrenteOpzionale(request, em);

        // Difensivo: se per qualsiasi motivo non troviamo l'utente nel db,
        // puliamo la sessione e lo reindirizziamo al login
        if (utente == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate(); // Invalida e distrugge la sessione HTTP
            }

            // Creiamo una nuova sessione solo per impostare il messaggio flash
            HttpSession newSession = request.getSession(true);
            UFlashMessage.addMessage(newSession, "danger", "Sessione non valida o scaduta. Effettuare nuovamente il login.");

            response.sendRedirect(request.getContextPath() + "/accedi");
            return null;
        }

        return utente;
    }


}
