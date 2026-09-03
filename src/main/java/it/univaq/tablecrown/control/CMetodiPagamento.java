package it.univaq.tablecrown.control;

import it.univaq.tablecrown.dao.PersistentManager;
import it.univaq.tablecrown.entity.ECartaDiCredito;
import it.univaq.tablecrown.entity.EUtente;
import it.univaq.tablecrown.utility.UBancaMockService;
import it.univaq.tablecrown.utility.UFlashMessage;
import it.univaq.tablecrown.utility.UHTTPMethods;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/**
 * Controller deputato alla gestione delle operazioni CRUD sui metodi di pagamento dell'utente.
 */
public class CMetodiPagamento extends BaseController{

    /**
     * Gestisce il salvataggio di una nuova carta di credito nel DB.
     * URL: POST /profilo/pagamenti/aggiungi
     */
    public void aggiungiCarta(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        // 1. Se un gestore/admin tenta di accedere, viene reindirizzato alla sua dashboard
        if (reindirizzaGestore(request, response)) {
            return;
        }

        // 2. Verifica che l'utente sia autenticato (EUtente)
        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return; // Redirect a /accedi già gestito da utenteCorrente
        }

        HttpSession session = request.getSession(true);

        try {
            // 3. Recupero dei parametri inviati da form tramite POST
            String numeroCarta = UHTTPMethods.postString(request, "numero_carta", null);
            String cvv = UHTTPMethods.postString(request, "cvv", null);
            String titolare = UHTTPMethods.postString(request, "titolare_carta", null);
            String scadenza = UHTTPMethods.postString(request, "scadenza_carta", null);

            // Validazione base dei dati obbligatori
            if (numeroCarta == null || numeroCarta.isBlank() ||
                    cvv == null || cvv.isBlank() ||
                    titolare == null || titolare.isBlank() ||
                    scadenza == null || scadenza.isBlank()) {

                UFlashMessage.addMessage(session, "danger", "Tutti i campi sono obbligatori.");
                response.sendRedirect(request.getContextPath() + "/profilo/pagamenti");
                return;
            }

            // 4. Simulazione validazione con il servizio bancario e generazione token
            UBancaMockService bancaService = new UBancaMockService();
            Map<String, String> datiToken = bancaService.generaToken(numeroCarta, cvv);
            String token = datiToken.get("token");

            // Estrazione delle ultime 4 cifre
            String trimmedNumero = numeroCarta.trim();
            String ultimeQuattroCifre = trimmedNumero.length() >= 4
                    ? trimmedNumero.substring(trimmedNumero.length() - 4)
                    : trimmedNumero;

            // 5. Istanziazione dell'entità ECartaDiCredito (utente, titolare, scadenza, ultimeQuattroCifre, token)
            ECartaDiCredito nuovaCarta = new ECartaDiCredito(
                    utente,
                    titolare,
                    scadenza,
                    ultimeQuattroCifre,
                    token
            );

            // 6. Salvataggio nel DB tramite PersistentManager
            PersistentManager pm = new PersistentManager(em);
            boolean salvato = pm.PMsaveObj(nuovaCarta);

            if (salvato) {
                UFlashMessage.addMessage(session, "success", "Metodo di pagamento salvato con successo!");
            } else {
                throw new Exception("Si è verificato un errore durante il salvataggio della carta di credito.");
            }

        } catch (IllegalArgumentException e) {
            // Cattura eccezioni di validazione lanciate dal costruttore di ECartaDiCredito o dalle utility
            UFlashMessage.addMessage(session, "danger", e.getMessage());
        } catch (Exception e) {
            // Cattura errori generici o del servizio di pagamento
            UFlashMessage.addMessage(session, "danger", e.getMessage());
        }

        // Pattern PRG: reindirizzamento
        response.sendRedirect(request.getContextPath() + "/profilo/pagamenti");
    }

    /**
     * Gestisce la rimozione sicura di una carta di credito salvata.
     * Mappatura suggerita: POST /profilo/pagamenti/elimina
     */
    public void eliminaCarta(HttpServletRequest request, HttpServletResponse response, EntityManager em)
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
            Long idCarta = UHTTPMethods.postLong(request, "id_carta", null);

            if (idCarta == null || idCarta <= 0) {
                UFlashMessage.addMessage(session, "danger", "Metodo di pagamento non valido o non specificato.");
                response.sendRedirect(request.getContextPath() + "/profilo/pagamenti");
                return;
            }

            PersistentManager pm = new PersistentManager(em);

            // Recuperiamo l'entità ECartaDiCredito usando il nome dell'attributo "idCartaDiCredito"
            ECartaDiCredito cartaDaEliminare = pm.PMgetObjOnAttribute(ECartaDiCredito.class, "idCartaDiCredito", idCarta);

            if (cartaDaEliminare == null) {
                UFlashMessage.addMessage(session, "danger", "La carta di credito selezionata non esiste.");
                response.sendRedirect(request.getContextPath() + "/profilo/pagamenti");
                return;
            }

            // Controllo di sicurezza: l'utente può eliminare solo una carta di sua proprietà
            if (!cartaDaEliminare.getUtente().getIdPersona().equals(utente.getIdPersona())) {
                UFlashMessage.addMessage(session, "danger", "Non sei autorizzato ad eliminare questa carta di credito.");
                response.sendRedirect(request.getContextPath() + "/profilo/pagamenti");
                return;
            }

            // Rimozione dal DB
            boolean eliminata = pm.PMdeleteObj(cartaDaEliminare);

            if (eliminata) {
                UFlashMessage.addMessage(session, "success", "Metodo di pagamento eliminato con successo!");
            } else {
                throw new Exception("Si è verificato un errore durante la rimozione della carta di credito.");
            }

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", "Impossibile completare l'operazione: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/profilo/pagamenti");
    }
}
