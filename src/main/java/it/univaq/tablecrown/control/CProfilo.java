package it.univaq.tablecrown.control;

import it.univaq.tablecrown.dao.PersistentManager;
import it.univaq.tablecrown.entity.*;
import it.univaq.tablecrown.utility.UFlashMessage;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller deputato alla gestione dell'area personale dell'utente.
 * Gestisce la visualizzazione dello storico ordini, wishlist, indirizzi e carte salvate,
 * oltre alle modifiche del profilo (anagrafica, password, eliminazione).
 */
public class CProfilo extends BaseController {

    private static final List<Map<String, String>> MENU_VOCI = List.of(
            Map.of("label", "Modifica account", "url", "/profilo/modifica"),
            Map.of("label", "I Miei Ordini", "url", "/profilo/ordini"),
            Map.of("label", "Wishlist", "url", "/profilo/wishlist"),
            Map.of("label", "I Miei Indirizzi", "url", "/profilo/indirizzi"),
            Map.of("label", "Metodi di Pagamento", "url", "/profilo/pagamenti")
    );

    public CProfilo() {
        super();
    }

    //==========================================================================
    // HUB PROFILO (GET)
    //==========================================================================

    /**
     * URL: GET /profilo
     */
    public void mostraHub(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        Map<String, Object> datiHub = new HashMap<>();
        datiHub.put("nomeUtente", utente.getNomePersona());
        datiHub.put("emailUtente", utente.getEmailPersona());
        datiHub.put("immagineUtente", utente.getImgPersona());
        datiHub.put("menuVoci", MENU_VOCI);

        Map<String, Object> datiPagina = new HashMap<>();
        datiPagina.put("datiHub", datiHub);
        preparaDatiLayout(request, "profilo", datiPagina);

        renderizza("areaPersonale.ftl", request, response);
    }

    //==========================================================================
    // MODIFICA ACCOUNT (GET & POST)
    //==========================================================================

    /**
     * URL: GET /profilo/modifica
     */
    public void mostraAccount(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        Map<String, Object> datiPagina = new HashMap<>();
        datiPagina.put("nomeUtente", utente.getNomePersona());
        datiPagina.put("emailUtente", utente.getEmailPersona());
        datiPagina.put("immagineUtente", utente.getImgPersona());
        datiPagina.put("dataNascitaUtente", utente.getDataNascita() != null ? utente.getDataNascita().toString() : "");
        preparaDatiLayout(request, "profilo", datiPagina);

        renderizza("profiloModificaAccount.ftl", request, response);
    }

    /**
     * Aggiorna nome, email ed eventuale nuova foto di profilo.
     * URL: POST /profilo/modifica
     */
    public void aggiornaAccount(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        HttpSession session = request.getSession(true);
        PersistentManager pm = new PersistentManager(em);

        try {
            String nome = request.getParameter("nome");
            String email = request.getParameter("email");

            if (nome != null && !nome.trim().isEmpty()) {
                utente.rinomina(nome);
            }
            if (email != null && !email.trim().isEmpty()) {
                utente.cambiaEmail(email);
            }

            // GESTIONE IMMAGINE PROFILO: Un'unica chiamata al metodo di BaseController
            String nuovaImmagine = salvaImmagineSuDisco(request, "img_profilo", "profili");
            if (nuovaImmagine != null) {
                utente.aggiornaImmagine(nuovaImmagine);
            }

            boolean salvato = pm.PMsaveObj(utente);

            if (salvato) {
                UFlashMessage.addMessage(session, "success", "Account modificato con successo!");
            } else {
                UFlashMessage.addMessage(session, "danger", "Si è verificato un errore durante la modifica del profilo.");
            }

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/profilo/modifica");
    }

    /**
     * Cambio password sincrono.
     * URL: POST /profilo/modifica/password
     */
    public void cambiaPassword(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        HttpSession session = request.getSession(true);
        PersistentManager pm = new PersistentManager(em);

        try {
            String vecchiaPassword = request.getParameter("vecchia_password");
            String nuovaPassword = request.getParameter("nuova_password");
            String confermaPassword = request.getParameter("conferma_password");

            if (!utente.verificaPassword(vecchiaPassword)) {
                throw new IllegalArgumentException("La vecchia password non è corretta.");
            }

            if (nuovaPassword == null || nuovaPassword.length() < 8 || !nuovaPassword.equals(confermaPassword)) {
                throw new IllegalArgumentException("La nuova password deve essere di almeno 8 caratteri e coincidere con la conferma.");
            }

            utente.cambiaPassword(nuovaPassword); // oppure utente.cambiaPassword(nuovaPassword);
            boolean salvato = pm.PMsaveObj(utente);

            if (salvato) {
                UFlashMessage.addMessage(session, "success", "Password aggiornata con successo!");
            } else {
                UFlashMessage.addMessage(session, "danger", "Errore durante l'aggiornamento della password.");
            }

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/profilo/modifica");
    }

    /**
     * Eliminazione account sincrona.
     * URL: POST /profilo/modifica/elimina
     */
    public void eliminaAccount(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        HttpSession session = request.getSession(true);
        PersistentManager pm = new PersistentManager(em);

        try {
            String password = request.getParameter("password");

            if (!utente.verificaPassword(password)) {
                throw new IllegalArgumentException("Password non corretta. Impossibile eliminare l'account.");
            }

            boolean eliminato = pm.PMdeleteObj(utente);

            if (eliminato) {
                session.invalidate(); // Distrugge la sessione dopo la cancellazione
                response.sendRedirect(request.getContextPath() + "/");
                return;
            } else {
                throw new RuntimeException("Si è verificato un errore durante la cancellazione dell'account.");
            }

        } catch (Exception e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/profilo/modifica");
        }
    }

    //==========================================================================
    // ORDINI (GET)
    //==========================================================================

    /**
     * URL: GET /profilo/ordini
     */
    public void mostraOrdini(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        PersistentManager pm = new PersistentManager(em);
        List<EOrdine> ordini = pm.PMgetObjListOnAttribute(EOrdine.class, "utente", utente);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
        List<Map<String, Object>> ordiniVista = new ArrayList<>();
        for (EOrdine ordine : ordini) {
            Map<String, Object> riga = new HashMap<>();
            riga.put("ordine", ordine);
            riga.put("dataFormattata", ordine.getData().format(formatter));
            ordiniVista.add(riga);
        }

        Map<String, Object> datiPagina = new HashMap<>();
        datiPagina.put("ordiniVista", ordiniVista);
        preparaDatiLayout(request, "profilo-ordini", datiPagina);

        renderizza("profiloOrdini.ftl", request, response);
    }

    //==========================================================================
    // WISHLIST (GET)
    //==========================================================================

    /**
     * URL: GET /profilo/wishlist
     */
    public void mostraWishlist(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }
        //TODO: forse non serve questo controllo?
        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        PersistentManager pm = new PersistentManager(em);
        EWishlist wishlist = pm.PMgetObjOnAttribute(EWishlist.class, "utente", utente);

        Set<EProdotto> prodotti = (wishlist != null) ? wishlist.getProdotti() : Collections.emptySet();

        // Estraiamo gli ID dei prodotti già in wishlist per escluderli dai correlati
        List<Long> idsEsclusi = new ArrayList<>();
        if (prodotti != null) {
            for (EProdotto p : prodotti) {
                idsEsclusi.add(p.getIdProdotto());
            }
        }

        // Recuperiamo i correlati
        List<EProdotto> correlati = prodottiCorrelati(em, idsEsclusi);

        // Prepariamo i dati per la vista
        Map<String, Object> datiPagina = new HashMap<>();
        datiPagina.put("prodotti", prodotti);
        datiPagina.put("correlati", correlati);

        preparaDatiLayout(request, "wishlist", datiPagina);

        renderizza("profiloWishlist.ftl", request, response);
    }

    //==========================================================================
    // INDIRIZZI (GET)
    //==========================================================================

    /**
     * URL: GET /profilo/indirizzi
     */
    public void mostraIndirizzi(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        PersistentManager pm = new PersistentManager(em);
        List<EIndirizzo> indirizzi = pm.PMgetObjListOnAttribute(EIndirizzo.class, "utente", utente);

        Map<String, Object> datiPagina = new HashMap<>();
        datiPagina.put("indirizzi", indirizzi);
        preparaDatiLayout(request, "profilo-indirizzi", datiPagina);

        renderizza("profiloIndirizzi.ftl", request, response);
    }

    //==========================================================================
    // METODI DI PAGAMENTO (GET)
    //==========================================================================

    /**
     * URL: GET /profilo/pagamenti
     */
    public void mostraMetodiPagamento(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        PersistentManager pm = new PersistentManager(em);
        List<ECartaDiCredito> carte = pm.PMgetObjListOnAttribute(ECartaDiCredito.class, "utente", utente);

        Map<String, Object> datiPagina = new HashMap<>();
        datiPagina.put("metodi", carte);
        preparaDatiLayout(request, "profilo-pagamenti", datiPagina);

        renderizza("profiloMetodiPagamento.ftl", request, response);
    }

    @Override
    protected List<Map<String, String>> getBreadcrumbs(HttpServletRequest request, String currentPage) {
        List<Map<String, String>> breadcrumbs = new ArrayList<>();
        HttpSession session = request.getSession(false);

        // Verifichiamo il flag di provenienza dal checkout
        boolean fromCheckout = session != null && Boolean.TRUE.equals(session.getAttribute("provenienza_checkout"));

        // Step 1: Home
        Map<String, String> home = new HashMap<>();
        home.put("label", "Home");
        home.put("url", "/");
        breadcrumbs.add(home);

        // Se proviene dal checkout ed è in una pagina di gestione Indirizzi o Pagamenti
        boolean isPaginaIndirizziOPagamenti = currentPage.startsWith("profilo-indirizzi") || currentPage.startsWith("profilo-pagamenti");

        if (fromCheckout && isPaginaIndirizziOPagamenti) {

            // RAMO CHECKOUT: Home -> Carrello -> Checkout -> Pagina Profilo
            Map<String, String> carrello = new HashMap<>();
            carrello.put("label", "Carrello");
            carrello.put("url", "/carrello");
            breadcrumbs.add(carrello);

            Map<String, String> checkout = new HashMap<>();
            checkout.put("label", "Checkout");
            checkout.put("url", "/checkout");
            breadcrumbs.add(checkout);

            String labelUltimoStep = switch (currentPage) {
                case "profilo-indirizzi" -> "I Miei Indirizzi";
                case "profilo-indirizzi-aggiungi" -> "Nuovo Indirizzo";
                case "profilo-pagamenti" -> "Metodi di Pagamento";
                case "profilo-pagamenti-aggiungi" -> "Nuova Carta";
                default -> "Gestione";
            };

            Map<String, String> ultimoStep = new HashMap<>();
            ultimoStep.put("label", labelUltimoStep);
            ultimoStep.put("url", "#");
            breadcrumbs.add(ultimoStep);

        } else {

            // RAMO NORMALE PROFILO: Home -> Area Personale -> Step Specifico
            Map<String, String> areaPersonale = new HashMap<>();
            areaPersonale.put("label", "Area Personale");
            areaPersonale.put("url", "profilo".equals(currentPage) ? "#" : "/profilo");
            breadcrumbs.add(areaPersonale);

            String labelMiddleStep = null;
            String urlMiddleStep = null;
            String labelUltimoStep = null;

            switch (currentPage) { //TODO: da allineare i nomi
                case "profilo-account" -> labelUltimoStep = "Modifica Account";
                case "profilo-ordini" -> labelUltimoStep = "I Miei Ordini";
                case "profilo-wishlist" -> labelUltimoStep = "Wishlist";
                case "profilo-indirizzi" -> labelUltimoStep = "I Miei Indirizzi";
                case "profilo-pagamenti" -> labelUltimoStep = "Metodi di Pagamento";
                case "profilo-indirizzi-aggiungi" -> {
                    labelMiddleStep = "I Miei Indirizzi";
                    urlMiddleStep = "/profilo/indirizzi";
                    labelUltimoStep = "Nuovo Indirizzo";
                }
                case "profilo-pagamenti-aggiungi" -> {
                    labelMiddleStep = "Metodi di Pagamento";
                    urlMiddleStep = "/profilo/pagamenti";
                    labelUltimoStep = "Nuova Carta";
                }
            }

            if (labelMiddleStep != null) {
                Map<String, String> middleStep = new HashMap<>();
                middleStep.put("label", labelMiddleStep);
                middleStep.put("url", urlMiddleStep);
                breadcrumbs.add(middleStep);
            }

            if (labelUltimoStep != null) {
                Map<String, String> ultimoStep = new HashMap<>();
                ultimoStep.put("label", labelUltimoStep);
                ultimoStep.put("url", "#");
                breadcrumbs.add(ultimoStep);
            }
        }

        return breadcrumbs;
    }
}
