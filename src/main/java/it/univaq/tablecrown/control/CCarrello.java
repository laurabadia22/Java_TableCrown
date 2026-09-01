package it.univaq.tablecrown.control;


import it.univaq.tablecrown.dao.PersistentManager;
import it.univaq.tablecrown.entity.EProdotto;
import it.univaq.tablecrown.entity.EUtente;
import it.univaq.tablecrown.utility.UFlashMessage;
import it.univaq.tablecrown.utility.UHTTPMethods;

import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller dedicato alla gestione del carrello acquisti.
 * I dati del carrello risiedono esclusivamente nella sessione utente come Map<Long, Integer>.
 * Versione sincrona (senza AJAX).
 */
public class CCarrello extends BaseController {

    public CCarrello() {
        super();
    }

    //==========================================================================
    // HELPER DI SUPPORTO INTERNO PER LA SESSIONE
    //==========================================================================

    /**
     * Recupera la mappa del carrello dalla sessione utente.
     * Se non esiste, la inizializza e la salva in sessione.
     */
    @SuppressWarnings("unchecked")
    private Map<Long, Integer> recuperaCarrelloSessione(HttpSession session) {
        Map<Long, Integer> carrello = (Map<Long, Integer>) session.getAttribute("carrello");
        if (carrello == null) {
            carrello = new HashMap<>();
            session.setAttribute("carrello", carrello);
        }
        return carrello;
    }

    //==========================================================================
    // VISUALIZZAZIONE VISTA (GET)
    //==========================================================================

    /**
     * Prepara i dati per la pagina del carrello e inoltra alla vista FreeMarker.
     * URL: GET /carrello
     */
    public void mostraCarrello(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return; // Utente non loggato: utenteCorrente reindirizza al login
        }

        HttpSession session = request.getSession(true);
        Map<Long, Integer> carrelloMap = recuperaCarrelloSessione(session);
        PersistentManager pm = new PersistentManager(em);

        List<Map<String, Object>> carrelloItems = new ArrayList<>();
        double totaleComplessivo = 0.0;
        int totaleArticoli = 0;

        // Recuperiamo i dettagli dei prodotti presenti nel carrello dal DB
        for (Map.Entry<Long, Integer> entry : carrelloMap.entrySet()) {
            Long idProdotto = entry.getKey();
            Integer quantita = entry.getValue();

            EProdotto prodotto = pm.PMgetObjOnAttribute(EProdotto.class, "idProdotto", idProdotto);
            if (prodotto != null) {
                double prezzoUnitario = prodotto.getPrezzoScontato();
                double subtotale = prezzoUnitario * quantita;

                totaleComplessivo += subtotale;
                totaleArticoli += quantita;

                Map<String, Object> item = new HashMap<>();
                item.put("prodotto", prodotto);
                item.put("quantita", quantita);
                item.put("prezzoUnitario", prezzoUnitario);
                item.put("subtotale", subtotale);

                carrelloItems.add(item);
            }
        }

        Map<String, Object> carrelloSummary = new HashMap<>();
        carrelloSummary.put("totale", totaleComplessivo);
        carrelloSummary.put("n_articoli", totaleArticoli);

        // Impostiamo gli attributi di richiesta per la vista FreeMarker
        request.setAttribute("carrello_items", carrelloItems);
        request.setAttribute("carrello_summary", carrelloSummary);
        request.setAttribute("update_url", request.getContextPath() + "/carrello/aggiorna");
        request.setAttribute("remove_url", request.getContextPath() + "/carrello/rimuovi");

        // Inoltro alla Servlet/JSP/FreeMarker View
        request.getRequestDispatcher("/WEB-INF/views/carrello.ftl").forward(request, response);
    }

    //==========================================================================
    // AZIONI OPERATIVE (POST)
    //==========================================================================

    /**
     * Aggiunge un prodotto al carrello.
     * URL: POST /carrello/aggiungi
     */
    public void aggiungiAlCarrello(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        String referer = UHTTPMethods.getReferer(request, request.getContextPath() + "/catalogo");
        HttpSession session = request.getSession(true);
        PersistentManager pm = new PersistentManager(em);

        try {
            String idProdottoRaw = request.getParameter("id_prodotto");
            long idProdotto = (idProdottoRaw != null) ? Long.parseLong(idProdottoRaw) : 0L;

            String quantitaRaw = request.getParameter("quantita");
            int quantita = (quantitaRaw != null && !quantitaRaw.trim().isEmpty()) ? Integer.parseInt(quantitaRaw) : 1;

            if (idProdotto <= 0) {
                throw new IllegalArgumentException("Prodotto non specificato.");
            }

            if (quantita <= 0) {
                throw new IllegalArgumentException("La quantità deve essere maggiore di zero.");
            }

            EProdotto prodotto = pm.PMgetObjOnAttribute(EProdotto.class, "idProdotto", idProdotto);
            if (prodotto == null) {
                throw new IllegalArgumentException("Prodotto non trovato.");
            }

            if (!prodotto.isAcquistabile()) {
                throw new IllegalStateException("Prodotto non disponibile per l'acquisto.");
            }

            Map<Long, Integer> carrello = recuperaCarrelloSessione(session);
            int quantitaAttuale = carrello.getOrDefault(idProdotto, 0);
            carrello.put(idProdotto, quantitaAttuale + quantita);

            session.setAttribute("carrello", carrello);
            UFlashMessage.addMessage(session, "success", "Prodotto aggiunto al carrello!");

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
        }

        response.sendRedirect(referer);
    }

    /**
     * Aggiorna la quantità di un prodotto nel carrello.
     * URL: POST /carrello/aggiorna
     */
    public void aggiornaQuantita(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        HttpSession session = request.getSession(true);

        try {
            String idProdottoRaw = request.getParameter("id_prodotto");
            long idProdotto = (idProdottoRaw != null) ? Long.parseLong(idProdottoRaw) : 0L;

            String quantitaRaw = request.getParameter("quantita");
            int nuovaQuantita = (quantitaRaw != null) ? Integer.parseInt(quantitaRaw) : 1;

            if (nuovaQuantita < 1) {
                throw new IllegalArgumentException("La quantità deve essere almeno 1.");
            }

            Map<Long, Integer> carrello = recuperaCarrelloSessione(session);

            if (!carrello.containsKey(idProdotto)) {
                throw new IllegalArgumentException("Prodotto non trovato nel carrello.");
            }

            carrello.put(idProdotto, nuovaQuantita);
            session.setAttribute("carrello", carrello);

            UFlashMessage.addMessage(session, "success", "Quantità aggiornata!");

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/carrello");
    }

    /**
     * Rimuove un prodotto dal carrello.
     * URL: POST /carrello/rimuovi
     */
    public void rimuoviDalCarrello(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        HttpSession session = request.getSession(true);

        try {
            String idProdottoRaw = request.getParameter("id_prodotto");
            long idProdotto = (idProdottoRaw != null) ? Long.parseLong(idProdottoRaw) : 0L;

            Map<Long, Integer> carrello = recuperaCarrelloSessione(session);
            carrello.remove(idProdotto);

            session.setAttribute("carrello", carrello);
            UFlashMessage.addMessage(session, "success", "Prodotto rimosso dal carrello.");

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/carrello");
    }
}
