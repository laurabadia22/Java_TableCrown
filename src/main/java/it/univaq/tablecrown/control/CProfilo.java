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

        request.setAttribute("datiHub", datiHub);
        request.getRequestDispatcher("/WEB-INF/templates/profilo_hub.ftl").forward(request, response);
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

        request.setAttribute("nomeUtente", utente.getNomePersona());
        request.setAttribute("emailUtente", utente.getEmailPersona());
        request.setAttribute("immagineUtente", utente.getImgPersona());
        request.setAttribute("dataNascitaUtente", utente.getDataNascita() != null ? utente.getDataNascita().toString() : "");

        request.getRequestDispatcher("/WEB-INF/templates/profilo_account.ftl").forward(request, response);
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

            Part imgPart = request.getPart("img_profilo");
            if (imgPart != null && imgPart.getSize() > 0) {
                //Estrae il nome originale del file
                String fileName = java.nio.file.Paths.get(imgPart.getSubmittedFileName()).getFileName().toString();

                //Genera un nome univoco per evitare sovrascritture di file con lo stesso nome
                String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

                //Definisce la cartella di destinazione sul server
                String uploadPath = request.getServletContext().getRealPath("") + java.io.File.separator + "uploads" + java.io.File.separator + "profili";
                java.io.File uploadDir = new java.io.File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                //Salva fisicamente il file
                String filePath = uploadPath + java.io.File.separator + uniqueFileName;
                imgPart.write(filePath);

                //Salva il percorso relativo nel database tramite l'entity
                String relativePath = "uploads/profili/" + uniqueFileName;
                utente.aggiornaImmagine(relativePath);
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

        request.setAttribute("ordini", ordini);
        request.getRequestDispatcher("/WEB-INF/templates/profilo_ordini.ftl").forward(request, response);
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

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        PersistentManager pm = new PersistentManager(em);
        EWishlist wishlist = pm.PMgetObjOnAttribute(EWishlist.class, "utente", utente);

        Set<EProdotto> prodotti = (wishlist != null) ? wishlist.getProdotti() : Collections.emptySet();

        request.setAttribute("prodotti", prodotti);
        request.getRequestDispatcher("/WEB-INF/templates/profilo_wishlist.ftl").forward(request, response);
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

        request.setAttribute("indirizzi", indirizzi);
        request.getRequestDispatcher("/WEB-INF/templates/profilo_indirizzi.ftl").forward(request, response);
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

        request.setAttribute("metodi", carte);
        request.getRequestDispatcher("/WEB-INF/templates/profilo_pagamenti.ftl").forward(request, response);
    }
}
