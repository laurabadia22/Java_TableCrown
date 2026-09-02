package it.univaq.tablecrown.control;

import it.univaq.tablecrown.dao.PersistentManager;
import it.univaq.tablecrown.entity.EGestore;
import it.univaq.tablecrown.entity.EUtente;
import it.univaq.tablecrown.utility.UFlashMessage;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller dedicato alla gestione del ciclo di vita dell'autenticazione.
 * Gestisce la registrazione, il login e il logout in sessione.
 */
public class CAutenticazione extends BaseController{

    public CAutenticazione() {
        super();
    }

    /**
     * Mostra il form di login (Richiesta GET).
     * URL: GET /accedi
     */
    public void mostraFormLogin(HttpServletRequest request, HttpServletResponse response, EntityManager em) throws ServletException, IOException {
        if (isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        HttpSession session = request.getSession(true);

        String redirectTo = request.getParameter("redirect_to");
        String emailValue = (String) session.getAttribute("old_email");

        // Pulizia dell'email salvata precedentemente in sessione
        session.removeAttribute("old_email");

        Map<String, Object> datiPagina = new HashMap<>();
        datiPagina.put("redirect_to", redirectTo);
        datiPagina.put("email_value", emailValue);

        preparaDatiLayout(request, "accedi", datiPagina);

        request.getRequestDispatcher("/WEB-INF/templates/login.ftl").forward(request, response);
    }

    /**
     * Mostra il form di registrazione (Richiesta GET).
     * URL: GET /registrati
     */
    public void mostraFormRegistrazione(HttpServletRequest request, HttpServletResponse response, EntityManager em) throws ServletException, IOException {
        if (isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        preparaDatiLayout(request, "registrati", new HashMap<>());

        request.getRequestDispatcher("/WEB-INF/templates/registrazione.ftl").forward(request, response);
    }

    /**
     * Gestisce l'invio dei dati del form di Login (Richiesta POST).
     * URL: POST /login
     */
    public void login(HttpServletRequest request, HttpServletResponse response, EntityManager em) throws ServletException, IOException {

        HttpSession session = request.getSession(true);

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String redirectTo = request.getParameter("redirect_to");

        //Controllo campi obbligatori
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            UFlashMessage.addMessage(session, "danger", "Tutti i campi sono obbligatori.");

            session.setAttribute("old_email", email);

            //Nota: URLEncoder.encode(..., StandardCharset.UTF_8) converte i caratteri speciali dell'URL in un formato sicuro per il browser
            String redirectUrl = request.getContextPath() + "/accedi"
                    + (redirectTo != null && !redirectTo.isEmpty() ? "?redirect_to=" + URLEncoder.encode(redirectTo, StandardCharsets.UTF_8) : "");
            response.sendRedirect(redirectUrl);
            return;
        }

        //Istanziazione del PersistentManager per questa specifica richiesta
        PersistentManager pm = new PersistentManager(em);

        //Controllo credenziali sul db (Utente o Gestore)
        Object persona = pm.PMgetObjOnAttribute(EUtente.class, "email", email);
        if (persona == null) {
            persona = pm.PMgetObjOnAttribute(EGestore.class, "email", email);
        }

        boolean passwordValida = false;
        if (persona instanceof EUtente) {
            passwordValida = ((EUtente) persona).verificaPassword(password);
        } else if (persona instanceof EGestore) {
            passwordValida = ((EGestore) persona).verificaPassword(password);
        }

        if (persona == null || !passwordValida) {
            UFlashMessage.addMessage(session, "danger", "Email o password errate. Riprova.");

            session.setAttribute("old_email", email);

            String redirectUrl = request.getContextPath() + "/accedi"
                    + (redirectTo != null && !redirectTo.isEmpty() ? "?redirect_to=" + URLEncoder.encode(redirectTo, StandardCharsets.UTF_8) : "");
            response.sendRedirect(redirectUrl);
            return;
        }

        //Salvataggio dell'utente in sessione
        session.setAttribute("utenteLoggato", persona);

        //Redirect in base al ruolo
        if (persona instanceof EGestore) {
            UFlashMessage.addMessage(session, "success", "Bentornato Gestore!");
            response.sendRedirect(request.getContextPath() + "/gestore/dashboard");
        } else {
            UFlashMessage.addMessage(session, "success", "Login effettuato con successo!");
            if (redirectTo != null && !redirectTo.isEmpty()) {
                response.sendRedirect(redirectTo);
            } else {
                response.sendRedirect(request.getContextPath() + "/");
            }
        }
    }

    /**
     * Gestisce la registrazione di un nuovo utente (Richiesta POST)
     * URL: POST /registrazione
     */
    public void registrazione(HttpServletRequest request, HttpServletResponse response, EntityManager em) throws ServletException, IOException {
        HttpSession session = request.getSession(true);

        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confermaPassword = request.getParameter("conferma_password");
        String dataNascitaStr = request.getParameter("data_nascita");

        if (nome == null || email == null || password == null || confermaPassword == null || dataNascitaStr == null ||
                nome.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty() || dataNascitaStr.trim().isEmpty()) {

            UFlashMessage.addMessage(session, "danger", "Tutti i campi sono obbligatori.");
            response.sendRedirect(request.getContextPath() + "/registrati");
            return;
        }

        if (!password.equals(confermaPassword)) {
            UFlashMessage.addMessage(session, "danger", "Le password non coincidono.");
            response.sendRedirect(request.getContextPath() + "/registrati");
            return;
        }

        //Istanziazione del PersistentManager
        PersistentManager pm = new PersistentManager(em);

        boolean esiste = pm.PMverificaEsistenza(EUtente.class, "email", email);
        if (esiste) {
            UFlashMessage.addMessage(session, "danger", "Questa email è già registrata.");
            response.sendRedirect(request.getContextPath() + "/registrati");
            return;
        }

        try {
            LocalDate dataNascita = LocalDate.parse(dataNascitaStr);
            EUtente nuovoUtente = new EUtente(nome, email, password, dataNascita);

            boolean salvato = pm.PMsaveObj(nuovoUtente);

            if (salvato) {
                UFlashMessage.addMessage(session, "success", "Registrazione completata! Effettua il login.");
                response.sendRedirect(request.getContextPath() + "/accedi");
            } else {
                UFlashMessage.addMessage(session, "danger", "Si è verificato un errore durante la registrazione. Riprova.");
                response.sendRedirect(request.getContextPath() + "/registrati");
            }
        } catch (DateTimeParseException e) {
            UFlashMessage.addMessage(session, "danger", "La data di nascita inserita non è valida.");
            response.sendRedirect(request.getContextPath() + "/registrati");
        } catch (IllegalArgumentException e) {
            UFlashMessage.addMessage(session, "danger", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/registrati");
        }
    }

    /**
     * Gestisce il logout dell'utente (Richiesta GET).
     * URL: GET /logout
     */
    public void logout(HttpServletRequest request, HttpServletResponse response, EntityManager em) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        if (session != null) {
            session.invalidate(); //distrugge la vecchia sessione
        }

        HttpSession newSession = request.getSession(true); //creo una nuova sessione con un id temporaneo per poter mostrare il flash message (che deve essere salvato in sessione)
        UFlashMessage.addMessage(newSession, "success", "Disconnessione effettuata. A presto!");

        response.sendRedirect(request.getContextPath() + "/");
    }

    @Override
    protected List<Map<String, String>> getBreadcrumbs(String currentPage) {
        List<Map<String, String>> breadcrumbs = new ArrayList<>();

        Map<String, String> home = new HashMap<>();
        home.put("label", "Home");
        home.put("url", "/");
        breadcrumbs.add(home);

        Map<String, String> current = new HashMap<>();
        if ("registrati".equals(currentPage)) {
            current.put("label", "Registrati");
            current.put("url", "/registrati");
        } else {
            current.put("label", "Login");
            current.put("url", "/accedi");
        }
        breadcrumbs.add(current);

        return breadcrumbs;
    }
}
