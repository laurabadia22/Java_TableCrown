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

import java.io.File;
import java.nio.file.Paths;
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

    protected static final int RISULTATI_PER_PAGINA = 15;
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
    }//TODO: MAI USATOOO???

    /**
     * Recupera in sicurezza un Part (file caricato) dalla richiesta HTTP multipart.
     * Restituisce null se il campo non esiste o non è stato caricato alcun file.
     */
    protected Part estraiPart(HttpServletRequest request, String nomeCampo) {
        try {
            Part part = request.getPart(nomeCampo);
            if (part != null && part.getSize() > 0 && part.getSubmittedFileName() != null
                    && !part.getSubmittedFileName().trim().isEmpty()) {
                return part;
            }
        } catch (Exception e) {
            // In caso di eccezioni di parsing multipart o campo assente
            return null;
        }
        return null;
    }

    /**
     * Salva un file caricato su disco generandone un nome univoco e restituisce
     * il percorso relativo pronto da salvare nel Database.
     *
     * @param request       La richiesta HTTP per ricavare la root del ServletContext
     * @param nomeCampo     Il nome del campo input type="file" nel form HTML
     * @param sottoCartella La cartella specifica dentro 'uploads' (es. "profili", "prodotti")
     * @return Percorso relativo (es. "uploads/profili/uuid_nome.png") oppure null se nessun file caricato.
     * @throws IOException In caso di errori durante la scrittura del file su disco.
     */
    protected String salvaImmagineSuDisco(HttpServletRequest request, String nomeCampo, String sottoCartella)
            throws IOException {

        Part part = estraiPart(request, nomeCampo);
        if (part == null) {
            return null;
        }

        // 1. Estrazione del nome originale del file
        String fileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();

        // 2. Generazione nome univoco per evitare sovrascritture (UUID)
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

        // 3. Calcolo del percorso fisico sul server
        String uploadPath = request.getServletContext().getRealPath("")
                + File.separator + "uploads"
                + File.separator + sottoCartella;

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 4. Scrittura del file su disco
        String filePath = uploadPath + File.separator + uniqueFileName;
        part.write(filePath);

        // 5. Restituisce il percorso relativo formattato
        return "uploads/" + sottoCartella + "/" + uniqueFileName;
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

    // UTENTI

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

    // CARRELLO

    /**
     * Trasforma la mappa del carrello della sessione in una lista di Map contenenti
     * l'entità EProdotto, la quantità, il prezzo unitario scontato e il subtotale.
     */
    protected List<Map<String, Object>> buildCarrelloItems(Map<Long, Integer> carrelloMap, PersistentManager pm) {
        List<Map<String, Object>> carrelloItems = new ArrayList<>();
        if (carrelloMap == null || carrelloMap.isEmpty()) {
            return carrelloItems;
        }

        for (Map.Entry<Long, Integer> entry : carrelloMap.entrySet()) {
            Long idProdotto = entry.getKey();
            Integer quantita = entry.getValue();

            EProdotto prodotto = pm.PMgetObjOnAttribute(EProdotto.class, "idProdotto", idProdotto);
            if (prodotto != null) {
                double prezzoUnitario = prodotto.getPrezzoScontato();
                double subtotale = prezzoUnitario * quantita;

                Map<String, Object> item = new HashMap<>();
                item.put("prodotto", prodotto);
                item.put("quantita", quantita);
                item.put("prezzoUnitario", prezzoUnitario);
                item.put("subtotale", subtotale);

                carrelloItems.add(item);
            }
        }
        return carrelloItems;
    }

    /**
     * Calcola la somma dei subtotali per gli elementi del carrello.
     */
    protected double calcolaTotaleCarrello(List<Map<String, Object>> carrelloItems) {
        double totale = 0.0;
        for (Map<String, Object> item : carrelloItems) {
            Object subtotaleObj = item.get("subtotale");
            if (subtotaleObj instanceof Number) {
                totale += ((Number) subtotaleObj).doubleValue();
            }
        }
        return totale;
    }

}
