package it.univaq.tablecrown.utility;

import jakarta.servlet.http.HttpSession;

import java.util.Enumeration;

public class USession {

    //TODO: CLASSE DA CANCELLARE!!!!!!

    //Distrugge completamente la sessione - usato al logout
    public static void destroySession(HttpSession session) {
        session.invalidate();
    }

    //Svuota tutte le variabili di sessione senza distruggere la sessione.
    //Bisogna rimuovere ogni attributo esplicitamente.
    public static void unsetSession(HttpSession session) {
        Enumeration<String> nomi = session.getAttributeNames();
        while (nomi.hasMoreElements()) {
            session.removeAttribute(nomi.nextElement());
        }
    }

    public static Object getSessionElement(HttpSession session, String id) {
        return session.getAttribute(id);
    }

    public static void unsetSessionElement(HttpSession session, String id) {
        session.removeAttribute(id);
    }

    public static void setSessionElement(HttpSession session, String id, Object value) {
        session.setAttribute(id, value);
    }

    public static boolean isSetSessionElement(HttpSession session, String id) {
        return session.getAttribute(id) != null;
    }
}
