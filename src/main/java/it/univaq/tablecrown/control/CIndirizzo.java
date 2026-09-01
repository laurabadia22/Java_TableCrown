package it.univaq.tablecrown.control;


import it.univaq.tablecrown.dao.PersistentManager;
import it.univaq.tablecrown.entity.EIndirizzo;
import it.univaq.tablecrown.entity.EUtente;
import it.univaq.tablecrown.utility.UFlashMessage;
import it.univaq.tablecrown.utility.UHTTPMethods;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller deputato alla gestione degli indirizzi di spedizione dell'utente.
 */
public class CIndirizzo<ObjectMapper> extends BaseController{

    private final ObjectMapper objectMapper;

    public CIndirizzo() {
        super();
        this.objectMapper = new ObjectMapper(); //TODO: NON VA BENEEE
    }

    //==========================================================================
    // HELPER DI SUPPORTO INTERNO
    //==========================================================================

    /**
     * Rimuove lo stato "predefinito" da tutti gli indirizzi di un determinato utente.
     * Utile quando se ne imposta uno nuovo come predefinito.
     */
    private void azzeraPredefinito(List<EIndirizzo> indirizzi, PersistentManager pm) {
        if (indirizzi == null) return;

        for (EIndirizzo indirizzo : indirizzi) {
            if (indirizzo.isPredefinito()) {
                indirizzo.rimuoviPredefinito();
                pm.PMsaveObj(indirizzo);
            }
        }
    }

    /**
     * Invia una risposta in formato JSON senza richiedere librerie esterne.
     */
    //TODO: DA RIVEDERE/CANCELLARE O IN CASO AGGIUNGERE LIBRERIA jackson AL pom.xml
    private void inviaRispostaJson(HttpServletResponse response, Map<String, Object> jsonMap) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder json = new StringBuilder("{");
        boolean primo = true;

        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            if (!primo) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");

            Object val = entry.getValue();
            if (val instanceof Number || val instanceof Boolean) {
                json.append(val);
            } else {
                String strVal = String.valueOf(val).replace("\"", "\\\"");
                json.append("\"").append(strVal).append("\"");
            }
            primo = false;
        }
        json.append("}");

        PrintWriter out = response.getWriter();
        out.print(json.toString());
        out.flush();
    }

    //==========================================================================
    // AZIONI OPERATIVE (POST)
    //==========================================================================

    /**
     * Aggiunge un nuovo indirizzo al profilo utente.
     * URL: POST /profilo/indirizzi/aggiungi
     */
    public void aggiungiIndirizzo(HttpServletRequest request, HttpServletResponse response, EntityManager em) throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        boolean isAjax = UHTTPMethods.isAjax(request);
        HttpSession session = request.getSession(true);

        Boolean daCheckoutObj = (Boolean) session.getAttribute("provenienza_checkout");
        boolean daCheckout = daCheckoutObj != null && daCheckoutObj;

        String redirectUrl;
        if (daCheckout) {
            session.removeAttribute("provenienza_checkout");
            redirectUrl = request.getContextPath() + "/checkout";
        } else {
            redirectUrl = request.getContextPath() + "/profilo/indirizzi";
        }

        PersistentManager pm = new PersistentManager(em);

        try {
            String nome = request.getParameter("nome");
            String via = request.getParameter("via");
            String citta = request.getParameter("citta");
            String cap = request.getParameter("cap");
            String provincia = request.getParameter("provincia");
            String nazione = request.getParameter("nazione");
            String nomeCitofono = request.getParameter("nomeCitofono");
            boolean voglioPredefinito = Boolean.parseBoolean(request.getParameter("predefinito"));

            List<EIndirizzo> indirizziEsistenti = pm.PMgetObjListOnAttribute(EIndirizzo.class, "utente", utente);
            boolean haGiaIndirizzi = indirizziEsistenti != null && !indirizziEsistenti.isEmpty();

            boolean predefinito = !haGiaIndirizzi || voglioPredefinito;

            if (predefinito) {
                azzeraPredefinito(indirizziEsistenti, pm);
            }

            EIndirizzo nuovoIndirizzo = new EIndirizzo(nome, via, citta, cap, provincia, nazione, nomeCitofono, utente, predefinito);

            boolean salvato = pm.PMsaveObj(nuovoIndirizzo);

            if (!salvato) {
                throw new RuntimeException("Si è verificato un errore durante l'aggiunta dell'indirizzo.");
            }

            if (isAjax) {
                Map<String, Object> jsonResponse = new HashMap<>();
                jsonResponse.put("status", "ok");
                jsonResponse.put("message", "Indirizzo salvato con successo!");
                jsonResponse.put("redirect", redirectUrl);
                inviaRispostaJson(response, jsonResponse);
                return;
            }

            UFlashMessage.addMessage(session, "success", "Nuovo indirizzo salvato!");
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            if (isAjax) {
                Map<String, Object> jsonResponse = new HashMap<>();
                jsonResponse.put("status", "error");
                jsonResponse.put("message", e.getMessage());
                inviaRispostaJson(response, jsonResponse);
                return;
            }

            UFlashMessage.addMessage(session, "danger", e.getMessage());
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Imposta un indirizzo esistente come predefinito.
     * URL: POST /profilo/indirizzi/predefinito
     */
    public void impostaPredefinito(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        boolean isAjax = UHTTPMethods.isAjax(request);
        HttpSession session = request.getSession(true);
        PersistentManager pm = new PersistentManager(em);

        try {
            String idIndirizzoRaw = request.getParameter("id_indirizzo");
            long idIndirizzo = (idIndirizzoRaw != null) ? Long.parseLong(idIndirizzoRaw) : 0L;

            EIndirizzo indirizzo = pm.PMgetObjOnAttribute(EIndirizzo.class, "idIndirizzo", idIndirizzo);

            if (indirizzo == null) {
                throw new IllegalArgumentException("L'indirizzo selezionato non esiste.");
            }

            if (indirizzo.getUtente().getIdPersona() != utente.getIdPersona()) {
                throw new SecurityException("Non sei autorizzato a modificare questo indirizzo.");
            }

            List<EIndirizzo> indirizziUtente = pm.PMgetObjListOnAttribute(EIndirizzo.class, "utente", utente);

            azzeraPredefinito(indirizziUtente, pm);

            indirizzo.impostaPredefinito();
            boolean salvato = pm.PMsaveObj(indirizzo);

            if (!salvato) {
                throw new RuntimeException("Si è verificato un errore durante l'impostazione dell'indirizzo come predefinito.");
            }

            if (isAjax) {
                Map<String, Object> jsonResponse = new HashMap<>();
                jsonResponse.put("status", "ok");
                jsonResponse.put("message", "Indirizzo impostato come predefinito!");
                inviaRispostaJson(response, jsonResponse);
                return;
            }

            UFlashMessage.addMessage(session, "success", "Indirizzo predefinito aggiornato con successo!");
            response.sendRedirect(request.getContextPath() + "/profilo/indirizzi");

        } catch (Exception e) {
            if (isAjax) {
                Map<String, Object> jsonResponse = new HashMap<>();
                jsonResponse.put("status", "error");
                jsonResponse.put("message", e.getMessage());
                inviaRispostaJson(response, jsonResponse);
                return;
            }

            UFlashMessage.addMessage(session, "danger", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/profilo/indirizzi");
        }
    }

    /**
     * Elimina un indirizzo.
     * URL: POST /profilo/indirizzi/elimina
     */
    public void eliminaIndirizzo(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws ServletException, IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, response, em);
        if (utente == null) {
            return;
        }

        boolean isAjax = UHTTPMethods.isAjax(request);
        String referer = UHTTPMethods.getReferer(request, request.getContextPath() + "/profilo/indirizzi");

        HttpSession session = request.getSession(true);
        PersistentManager pm = new PersistentManager(em);

        try {
            String idIndirizzoRaw = request.getParameter("id_indirizzo");
            long idIndirizzo = (idIndirizzoRaw != null) ? Long.parseLong(idIndirizzoRaw) : 0L;

            EIndirizzo indirizzoDaEliminare = pm.PMgetObjOnAttribute(EIndirizzo.class, "idIndirizzo", idIndirizzo);

            if (indirizzoDaEliminare == null) {
                throw new IllegalArgumentException("L'indirizzo selezionato non esiste.");
            }

            if (indirizzoDaEliminare.getUtente().getIdPersona() != utente.getIdPersona()) {
                throw new SecurityException("Non sei autorizzato a eliminare questo indirizzo.");
            }

            boolean eraPredefinito = indirizzoDaEliminare.isPredefinito();

            boolean eliminato;
            try {
                eliminato = pm.PMdeleteObj(indirizzoDaEliminare);
            } catch (Exception dbException) {
                throw new RuntimeException("Impossibile eliminare l'indirizzo perché è collegato ad uno o più ordini effettuati.");
            }

            if (!eliminato) {
                throw new RuntimeException("Si è verificato un errore durante l'eliminazione dell'indirizzo.");
            }

            if (eraPredefinito) {
                List<EIndirizzo> indirizziRimasti = pm.PMgetObjListOnAttribute(EIndirizzo.class, "utente", utente);

                if (indirizziRimasti != null && !indirizziRimasti.isEmpty()) {
                    EIndirizzo nuovoPredefinito = indirizziRimasti.get(0);
                    nuovoPredefinito.impostaPredefinito();
                    pm.PMsaveObj(nuovoPredefinito);
                }
            }

            if (isAjax) {
                Map<String, Object> jsonResponse = new HashMap<>();
                jsonResponse.put("status", "ok");
                jsonResponse.put("message", "Indirizzo eliminato con successo!");
                jsonResponse.put("idIndirizzo", idIndirizzo);
                inviaRispostaJson(response, jsonResponse);
                return;
            }

            UFlashMessage.addMessage(session, "success", "Indirizzo eliminato con successo!");
            response.sendRedirect(referer);

        } catch (Exception e) {
            if (isAjax) {
                Map<String, Object> jsonResponse = new HashMap<>();
                jsonResponse.put("status", "error");
                jsonResponse.put("message", e.getMessage());
                inviaRispostaJson(response, jsonResponse);
                return;
            }

            UFlashMessage.addMessage(session, "danger", e.getMessage());
            response.sendRedirect(referer);
        }
    }
}
