package it.univaq.tablecrown.control;

import it.univaq.tablecrown.entity.EGestore;
import it.univaq.tablecrown.entity.EUtente;
import it.univaq.tablecrown.utility.UFlashMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            session.setAttribute("flash_message", "È necessario effettuare l'accesso per visualizzare questa pagina.");
            session.setAttribute("flash_type", "warning");

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
}
