package it.univaq.tablecrown.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Servlet principale che intercetta tutte le richieste HTTP
 * e le smista ai rispettivi Controller.
 */
@WebServlet(urlPatterns = "/*")
public class FrontController extends HttpServlet {

    //Factory singleton per la gestione delle connessioni JPA verso il db
    private static EntityManagerFactory emf;

    /**
     * Metodo del ciclo di vita della Servlet invocato una sola volta all'avvio dell'applicazione.
     * Inizializza l'EntityManagerFactory per l'intera app.
     */
    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("tablecrown-pu");
        try {
            it.univaq.tablecrown.utility.UFreeMarker.init(getServletContext());
        } catch (IOException e) {
            throw new ServletException("Errore nell'inizializzazione di FreeMarker", e);
        }
    }

    /**
     * Metodo del ciclo di vita della Servlet invocato alla chiusura/undeployment dell'applicazione.
     * Garantisce il corretto rilascio delle risorse di persistenza (qui l'emf)
     */
    @Override
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    /**
     * Intercetta tutte le chiamate effettuate con metodo HTTP GET.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    /**
     * Intercetta tutte le chiamate effettuate con metodo HTTP POST
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        process(request, response);
    }

    /**
     * Metodo centrale di smistamento (Routing) per tutte le richieste in ingresso.
     * Analizza il path dell'URL e il metodo HTTP, istanzia il controller specifico su richiesta
     * e gestisce il ciclo di vita dell'EntityManager per l'interazione con il DB.
     */
    private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //Esclusione dei file statici (CSS, JS, Immagini)
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/static/") || path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") || path.endsWith(".jpg")) {
            getServletContext().getNamedDispatcher("default").forward(request, response);
            return;
        }

        //Parsing dell'URL per estrarre rotta principale e sottorotte
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        String[] parti = cleanPath.isEmpty() ? new String[]{"home"} : cleanPath.split("/");
        String routePrincipale = parti[0].toLowerCase();
        String sottoRoute = parti.length > 1 ? parti[1].toLowerCase() : "";
        String metodoHTTP = request.getMethod();

        //Apertura dell'EntityManager dedicato alla singola Request
        EntityManager em = emf.createEntityManager();

        try {
            switch (routePrincipale) {

                case "home":
                    if ("GET".equals(metodoHTTP)) {
                        new CHome().mostraHome(request, response, em);
                    } else {
                        mostra404(response);
                    }
                    break;

                case "catalogo":
                    if (!"GET".equals(metodoHTTP)) {
                        mostra404(response);
                        break;
                    }

                    CCatalogo catalogoController = new CCatalogo();
                    switch (sottoRoute) {
                        case "giochi-da-tavolo":
                            catalogoController.mostraCatalogoGiochi(request, response, em);
                            break;
                        case "bustine":
                            catalogoController.mostraCatalogoBustine(request, response, em);
                            break;
                        case "porta-dadi":
                            catalogoController.mostraCatalogoPortaDadi(request, response, em);
                            break;
                        default:
                            mostra404(response);
                            break;
                    }
                    break;

                case "offerte":
                    if ("GET".equals(metodoHTTP)) {
                        new CCatalogo().mostraOfferte(request, response, em);
                    } else {
                        mostra404(response);
                    }
                    break;

                case "accedi":
                    if ("GET".equals(metodoHTTP)) {
                        new CAutenticazione().mostraFormLogin(request, response, em);
                    } else {
                        mostra404(response);
                    }
                    break;

                case "registrati":
                    if ("GET".equals(metodoHTTP)) {
                        new CAutenticazione().mostraFormRegistrazione(request, response, em);
                    } else {
                        mostra404(response);
                    }
                    break;

                case "logout":
                    if ("GET".equals(metodoHTTP)) {
                        new CAutenticazione().logout(request, response, em);
                    } else {
                        mostra404(response);
                    }
                    break;

                case "login":
                    if ("POST".equals(metodoHTTP)) {
                        new CAutenticazione().login(request, response, em);
                    } else {
                        mostra404(response);
                    }
                    break;

                case "registrazione":
                    if ("POST".equals(metodoHTTP)) {
                        new CAutenticazione().registrazione(request, response, em);
                    } else {
                        mostra404(response);
                    }
                    break;

                //TODO: aggiungere altri case man mano che vengono creati i controller

                default:
                    mostra404(response);
                    break;
            }
        } catch (Exception e) {
            throw new ServletException("Errore durante l'elaborazione della richiesta", e);
        } finally {
            //Garantisce la chiusura della connessione al db al termine della request
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Metodo per gestire le rotte inesistenti o i metodi non ammessi.
     */
    private void mostra404(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Pagina non trovata");
    }

}
