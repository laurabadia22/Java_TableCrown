package it.univaq.tablecrown.utility;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UFlashMessage {

    private static final String CHIAVE_SESSIONE = "flash";

    // Aggiunge un messaggio flash alla sessione, organizzato per tipo
    // (es. "success" -> ["Prodotto aggiunto al carrello."])
    @SuppressWarnings("unchecked")
    public static void addMessage(HttpSession session, String type, String message) {
        Map<String, List<String>> messages =
                (Map<String, List<String>>) session.getAttribute(CHIAVE_SESSIONE);
        if (messages == null) {
            messages = new HashMap<>();
        }
        messages.computeIfAbsent(type, k -> new ArrayList<>()).add(message);
        session.setAttribute(CHIAVE_SESSIONE, messages);
    }

    // Recupera tutti i messaggi flash e li elimina dalla sessione —
    // va chiamato nella View dopo averli mostrati, così compaiono una sola volta
    @SuppressWarnings("unchecked")
    public static Map<String, List<String>> getMessage(HttpSession session) {
        Map<String, List<String>> messages =
                (Map<String, List<String>>) session.getAttribute(CHIAVE_SESSIONE);
        if (messages == null) {
            return new HashMap<>();
        }
        session.removeAttribute(CHIAVE_SESSIONE);
        return messages;
    }

    public static boolean hasMessage(HttpSession session) {
        return session.getAttribute(CHIAVE_SESSIONE) != null;
    }
}
