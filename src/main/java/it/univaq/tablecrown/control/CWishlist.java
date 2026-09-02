package it.univaq.tablecrown.control;

import it.univaq.tablecrown.dao.PersistentManager;
import it.univaq.tablecrown.entity.EProdotto;
import it.univaq.tablecrown.entity.EUtente;
import it.univaq.tablecrown.entity.EWishlist;
import it.univaq.tablecrown.utility.UFlashMessage;
import it.univaq.tablecrown.utility.UHTTPMethods;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller deputato alla gestione operativa della wishlist (aggiunta e rimozione)
 * La sola visualizzazione della Wishlist è delegata a CProfilo
 */
public class CWishlist extends BaseController{

    public CWishlist() {
        super();
    }

    //==========================================================================
    // HELPER PRIVATI INTERNI
    //==========================================================================

    /**
     * Recupera o crea da zero la wishlist dell'utente.
     * Garantisce che l'utente abbia sempre una wishlist attiva su cui operare.
     */
    private EWishlist recuperaWishlist(EUtente utente, PersistentManager pm) {
        EWishlist wishlist = pm.PMgetObjOnAttribute(EWishlist.class, "utente", utente);

        if (wishlist == null) {
            wishlist = new EWishlist(utente);
            pm.PMsaveObj(wishlist);
        }

        return wishlist;
    }

    //==========================================================================
    // AZIONI OPERATIVE (POST)
    //==========================================================================

    /**
     * Aggiunge un prodotto alla wishlist.
     * URL: POST /wishlist/aggiungi
     */
    public void aggiungiAllaWishlist(HttpServletRequest request, HttpServletResponse response, EntityManager em)
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
            String idProdottoRaw = request.getParameter("idProdotto");
            long idProdotto = (idProdottoRaw != null) ? Long.parseLong(idProdottoRaw) : 0L;

            EProdotto prodotto = pm.PMgetObjOnAttribute(EProdotto.class, "idProdotto", idProdotto);

            if (prodotto == null) {
                throw new IllegalArgumentException("Il prodotto selezionato non esiste.");
            }

            EWishlist wishlist = recuperaWishlist(utente, pm);
            wishlist.addProdotto(prodotto);

            boolean salvato = pm.PMsaveObj(wishlist);

            if (!salvato) {
                throw new RuntimeException("Si è verificato un errore durante l'aggiunta alla wishlist.");
            }

            UFlashMessage.addMessage(session, "success", "Prodotto aggiunto alla wishlist!");

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
        }

        response.sendRedirect(referer);
    }

    /**
     * Rimuove un prodotto dalla wishlist dell'utente.
     * URL: POST /wishlist/rimuovi
     */
    public void rimuoviDallaWishlist(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        String referer = UHTTPMethods.getReferer(request, request.getContextPath() + "/profilo/wishlist");
        HttpSession session = request.getSession(true);
        PersistentManager pm = new PersistentManager(em);

        try {
            String idProdottoRaw = request.getParameter("idProdotto");
            long idProdotto = (idProdottoRaw != null) ? Long.parseLong(idProdottoRaw) : 0L;

            EProdotto prodotto = pm.PMgetObjOnAttribute(EProdotto.class, "idProdotto", idProdotto);

            if (prodotto == null) {
                throw new IllegalArgumentException("Il prodotto selezionato non esiste.");
            }

            EWishlist wishlist = pm.PMgetObjOnAttribute(EWishlist.class, "utente", utente);

            if (wishlist != null) {
                if (wishlist.getProdotti().contains(prodotto)) {
                    wishlist.removeProdotto(prodotto);
                    pm.PMsaveObj(wishlist);
                }
            }

            UFlashMessage.addMessage(session, "success", "Prodotto rimosso dalla wishlist!");

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
        }

        response.sendRedirect(referer);
    }
}
