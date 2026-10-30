package it.univaq.tablecrown.control;

import it.univaq.tablecrown.entity.EGestore;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CPagineStatiche extends BaseController{
    public void mostraChiSiamo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Controllo standard per sicurezza/ruoli che hai già nel BaseController
        if (reindirizza(request, response)) return;

        HttpSession session = request.getSession(false);
        Object utente = (session != null) ? session.getAttribute("utenteLoggato") : null;
        String currentPage = (utente instanceof EGestore) ? "gestore_chi_siamo" : "chi_siamo";

        Map<String, Object> datiPagina = new HashMap<>();
        // Puoi passare dati dinamici se vuoi (es. numero di anni di attività), altrimenti la mappa resta vuota

        preparaDatiLayout(request, currentPage, datiPagina);
        renderizza("chiSiamo.ftl", request, response);
    }

    public void mostraDoveSiamo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (reindirizza(request, response)) return;

        HttpSession session = request.getSession(false);
        Object utente = (session != null) ? session.getAttribute("utenteLoggato") : null;
        String currentPage = (utente instanceof EGestore) ? "gestore_dove_siamo" : "dove_siamo";

        Map<String, Object> datiPagina = new HashMap<>();
        preparaDatiLayout(request, currentPage, datiPagina);
        renderizza("doveSiamo.ftl", request, response);
    }
}
