package it.univaq.tablecrown.control;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CPagineStatiche extends BaseController{
    public void mostraChiSiamo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Controllo standard per sicurezza/ruoli che hai già nel BaseController
        if (reindirizza(request, response)) return;

        Map<String, Object> datiPagina = new HashMap<>();
        // Puoi passare dati dinamici se vuoi (es. numero di anni di attività), altrimenti la mappa resta vuota

        preparaDatiLayout(request, "chi-siamo", datiPagina);
        renderizza("chiSiamo.ftl", request, response);
    }

    public void mostraDoveSiamo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (reindirizza(request, response)) return;

        Map<String, Object> datiPagina = new HashMap<>();
        preparaDatiLayout(request, "dove-siamo", datiPagina);
        renderizza("doveSiamo.ftl", request, response);
    }
}
