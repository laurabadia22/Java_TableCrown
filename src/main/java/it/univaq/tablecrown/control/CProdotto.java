package it.univaq.tablecrown.control;

import it.univaq.tablecrown.dao.PersistentManager;
import it.univaq.tablecrown.entity.EGiocoDaTavolo;
import it.univaq.tablecrown.entity.EProdotto;
import it.univaq.tablecrown.entity.EUtente;
import it.univaq.tablecrown.entity.EWishlist;
import it.univaq.tablecrown.utility.UFlashMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.persistence.EntityManager;

import java.io.IOException;
import java.util.*;

/**
 * Controller deputato alla gestione dei dettagli di un prodotto.
 * Gestisce la visualizzazione pubblica di uno specifico prodotto.
 */
public class CProdotto extends BaseController{

    public  CProdotto() {
        super();
    }

    /**
     * Mostra la pagina di dettaglio di un prodotto specifico.
     * URL: GET /prodotto?id=X (Accesso libero)
     */
    public void mostraDettaglioProdotto(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        // Se un gestore tenta di accedere, viene reindirizzato alla sua dashboard
        if (reindirizzaGestore(request, response)) {
            return;
        }

        String idParam = request.getParameter("id");
        long idProdotto = 0L;

        try {
            if (idParam != null) {
                idProdotto = Long.parseLong(idParam);
            }
        } catch (NumberFormatException e) {
            idProdotto = 0L;
        }

        if (idProdotto <= 0L) {
            HttpSession session = request.getSession(true);
            UFlashMessage.addMessage(session, "danger", "ID prodotto non valido.");
            response.sendRedirect(request.getContextPath() + "/catalogo/giochi-da-tavolo");
            return;
        }

        PersistentManager pm = new PersistentManager(em);
        EProdotto prodotto = pm.PMgetObjOnAttribute(EProdotto.class, "idProdotto", idProdotto);

        if (prodotto == null) {
            HttpSession session = request.getSession(true);
            UFlashMessage.addMessage(session, "danger", "Il prodotto non esiste o non è più disponibile.");
            response.sendRedirect(request.getContextPath() + "/catalogo/giochi-da-tavolo");
            return;
        }

        // Costruiamo la mappa di dati per la vista
        Map<String, Object> datiPagina = costruisciDatiVista(request, prodotto, pm, em);

        // Prepariamo i dati del layout globale (Navbar, Footer, Breadcrumbs, Flash Message)
        preparaDatiLayout(request, "prodotto", datiPagina);

        // Inoltro alla pagina JSP
        request.getRequestDispatcher("/WEB-INF/views/dettaglio_prodotto.ftl").forward(request, response);
    }

    //==========================================================================
    // HELPER PRIVATI
    //==========================================================================

    /**
     * Costruisce la mappa dei dati da passare alla vista per il rendering del dettaglio.
     */
    private Map<String, Object> costruisciDatiVista(HttpServletRequest request, EProdotto prodotto, PersistentManager pm, EntityManager em) {
        Map<String, Object> dati = new HashMap<>();

        dati.put("prodotto", prodotto);
        dati.put("isInWishlist", isProdottoInWishlist(request, prodotto.getIdProdotto(), pm, em));

        // Gestione Valutazioni e Stelle
        if (prodotto.getValutazione() != null) {
            dati.put("valutazioneMedia", prodotto.getValutazioneMedia());
            dati.put("numeroValutazioni", prodotto.getValutazione().getNumeroValutazioni());
        } else {
            dati.put("valutazioneMedia", 0.0f);
            dati.put("numeroValutazioni", 0);
        }

        // Recupero prodotti correlati (escludendo l'ID del prodotto corrente)
        List<Long> idsEsclusi = Collections.singletonList(prodotto.getIdProdotto());
        List<EProdotto> correlati = pm.PMfindCorrelati(idsEsclusi,8);
        dati.put("correlati", correlati);

        // Se si tratta di un Gioco da Tavolo, recuperiamo eventuali informazioni specifiche
        if (prodotto instanceof EGiocoDaTavolo) {
            EGiocoDaTavolo gioco = (EGiocoDaTavolo) prodotto;
            dati.put("giocoBase", gioco.getGiocoBase());
            dati.put("categoria", gioco.getCategoria());
            dati.put("componenti", gioco.getComponenti());
            dati.put("numeroGiocatoriMin", gioco.getNumeroGiocatoriMin());
            dati.put("numeroGiocatoriMax", gioco.getNumeroGiocatoriMax());
            dati.put("etaMinima", gioco.getEtaMinima());
            dati.put("durataMedia", gioco.getDurataMedia());
            dati.put("lingua", gioco.getLingua());
            dati.put("difficolta", gioco.getDifficolta());

            // Gestione Livello Danno
            if (gioco.getLivelloDanno() != null) {
                dati.put("livelloDanno", gioco.getLivelloDanno());
                dati.put("scontoDanno", gioco.getLivelloDanno().getScontoPercentuale());
            }
            dati.put("descrizioneDanno", gioco.getDescrizioneDanno());
        }

        return dati;
    }

    /**
     * Verifica se il prodotto è presente nella wishlist dell'utente loggato.
     */
    private boolean isProdottoInWishlist(HttpServletRequest request, Long idProdotto, PersistentManager pm, EntityManager em) {
        EUtente utente = utenteCorrenteOpzionale(request, em);
        if (utente == null) {
            return false;
        }

        EWishlist wishlist = pm.PMgetObjOnAttribute(EWishlist.class, "utente", utente);
        if (wishlist == null || wishlist.getProdotti() == null) {
            return false;
        }

        for (EProdotto p : wishlist.getProdotti()) {
            if (p.getIdProdotto() == idProdotto) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gestisce dinamicamente i Breadcrumbs per la pagina di dettaglio prodotto.
     */
    @Override
    protected List<Map<String, String>> getBreadcrumbs(String currentPage) {
        List<Map<String, String>> breadcrumbs = new ArrayList<>();

        Map<String, String> homeStep = new HashMap<>();
        homeStep.put("label", "Home");
        homeStep.put("url", "/");
        breadcrumbs.add(homeStep);

        // Nel caso specifico dei breadcrumbs per CProdotto, la rotta viene personalizzata
        // direttamente nel ciclo della richiesta se necessario o gestita con un fallback generico
        Map<String, String> currentStep = new HashMap<>();
        currentStep.put("label", "Dettaglio Prodotto");
        currentStep.put("url", "#");
        breadcrumbs.add(currentStep);

        return breadcrumbs;
    }
}
