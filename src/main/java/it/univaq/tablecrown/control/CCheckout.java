package it.univaq.tablecrown.control;

import it.univaq.tablecrown.dao.PersistentManager;
import it.univaq.tablecrown.entity.*;
import it.univaq.tablecrown.utility.UBancaMockService;
import it.univaq.tablecrown.utility.UFlashMessage;
import it.univaq.tablecrown.utility.UHTTPMethods;
import it.univaq.tablecrown.utility.UPaymentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CCheckout extends BaseController{

    private final UPaymentService paymentService;

    public CCheckout() {
        super();
        this.paymentService = new UBancaMockService();
    }

    /**
     * Mostra la pagina di checkout con il riepilogo del carrello,
     * indirizzi e carte salvate dall'utente.
     * URL: GET /checkout
     */
    public void mostraCheckout(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, em);
        if (utente == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("provenienza_checkout", true);

        // Recupero del carrello dalla sessione
        @SuppressWarnings("unchecked")
        Map<Long, Integer> carrello = (Map<Long, Integer>) session.getAttribute("carrello");
        if (carrello == null || carrello.isEmpty()) {
            UFlashMessage.addMessage(session, "danger", "Il tuo carrello è vuoto. Aggiungi dei prodotti prima di procedere.");
            response.sendRedirect(request.getContextPath() + "/catalogo/giochi-da-tavolo");
            return;
        }

        PersistentManager pm = new PersistentManager(em);

        // Recuperiamo gli indirizzi dell'utente
        List<EIndirizzo> indirizziUtente = pm.PMgetObjListOnAttribute(EIndirizzo.class, "utente", utente);

        // Ordiniamo mettendo in cima l'indirizzo predefinito
        List<EIndirizzo> indirizziOrdinati = new ArrayList<>();
        EIndirizzo predefinito = null;
        for (EIndirizzo ind : indirizziUtente) {
            if (ind.isPredefinito()) {
                predefinito = ind;
            } else {
                indirizziOrdinati.add(ind);
            }
        }
        if (predefinito != null) {
            indirizziOrdinati.add(0, predefinito);
        }

        // Recuperiamo le carte salvate dell'utente
        List<ECartaDiCredito> carteUtente = pm.PMgetObjListOnAttribute(ECartaDiCredito.class, "utente", utente);

        // Costruzione items carrello e totale provvisorio
        List<Map<String, Object>> carrelloItems = buildCarrelloItems(carrello, pm);
        float totaleCarrello = calcolaTotaleCarrello(carrelloItems);

        request.setAttribute("tipo_checkout", "prodotti");
        request.setAttribute("azione_checkout", request.getContextPath() + "/checkout/acquista");
        request.setAttribute("prodotti_carrello", carrelloItems);
        request.setAttribute("totale_carrello", totaleCarrello);
        request.setAttribute("indirizzi", indirizziOrdinati);
        request.setAttribute("carte", carteUtente);

        renderView(request, response, "checkout");
    }

    /**
     * Processa l'acquisto entro una singola transazione ACID.
     * URL: POST /checkout/acquista
     */
    public void elaboraAcquisto(HttpServletRequest request, HttpServletResponse response, EntityManager em)
            throws IOException {

        if (reindirizzaGestore(request, response)) {
            return;
        }

        EUtente utente = utenteCorrente(request, em);
        HttpSession session = request.getSession(true);
        PersistentManager pm = new PersistentManager(em);

        EntityTransaction tx = em.getTransaction();

        try {
            // 1. Risoluzione dell'indirizzo
            Long idIndirizzo = UHTTPMethods.postLong(request, "id_indirizzo", null);
            EIndirizzo indirizzo = risolviIndirizzo(idIndirizzo, utente, pm);

            // 2. Risoluzione / Creazione della carta di credito
            String sceltaCarta = UHTTPMethods.postString(request, "scelta_carta", "salvata");
            Long idCartaSalvata = UHTTPMethods.postLong(request, "id_carta_salvata", null);
            ECartaDiCredito carta = risolviCartaPagamento(request, sceltaCarta, idCartaSalvata, utente, pm);

            // 3. Recupero carrello
            @SuppressWarnings("unchecked")
            Map<Long, Integer> carrello = (Map<Long, Integer>) session.getAttribute("carrello");
            if (carrello == null || carrello.isEmpty()) {
                throw new IllegalArgumentException("Il carrello è vuoto. Impossibile completare l'ordine.");
            }

            // 4. AVVIO TRANSAZIONE JPA
            tx.begin();

            // Creazione nuova entità Ordine
            EOrdine ordine = new EOrdine(utente, indirizzo, carta);

            // 5. Verifica e aggiornamento giacenze per ciascun prodotto
            for (Map.Entry<Long, Integer> entry : carrello.entrySet()) {
                Long idProdotto = entry.getKey();
                int quantitaRichiesta = entry.getValue();

                EProdotto prodotto = pm.PMgetObjOnAttribute(EProdotto.class, "idProdotto", idProdotto);
                if (prodotto == null) {
                    throw new IllegalArgumentException("Un prodotto nel carrello non è più presente a catalogo.");
                }

                if (!prodotto.isAcquistabile()) {
                    throw new IllegalStateException("Il prodotto '" + prodotto.getNomeProdotto() + "' non è attualmente disponibile per l'acquisto.");
                }

                if (prodotto.getQuantita() < quantitaRichiesta) {
                    throw new IllegalStateException("Quantità richiesta per '" + prodotto.getNomeProdotto()
                            + "' superiore alla disponibilità in magazzino (" + prodotto.getQuantita() + " disponibili).");
                }

                // Aggiorna giacenze e statistiche di vendita (Domain Logic)
                prodotto.aggiornaQuantita(prodotto.getQuantita() - quantitaRichiesta);
                prodotto.aggiungiVendite(quantitaRichiesta);
                pm.PMsaveObj(prodotto);

                // Associa il prodotto con la relativa quantità all'ordine
                ordine.aggiungiProdotto(prodotto, quantitaRichiesta);
            }

            // 6. Addebito simulato con Banca Mock
            float totaleOrdine = ordine.calcolaTotale();
            boolean pagamentoEsito = paymentService.effettuaPagamento(carta.getToken(), totaleOrdine);

            if (!pagamentoEsito) {
                throw new RuntimeException("La transazione bancaria è stata rifiutata.");
            }

            // 7. Salvataggio definitivo dell'ordine su Database
            boolean salvato = pm.PMsaveObj(ordine);
            if (!salvato) {
                throw new RuntimeException("Impossibile registrare l'ordine a sistema.");
            }

            // 8. COMMIT TRANSAZIONE
            tx.commit();

            // Svuotiamo il carrello solo a transazione completata con successo
            session.removeAttribute("carrello");

            UFlashMessage.addMessage(session, "success", "Acquisto completato con successo!");
            response.sendRedirect(request.getContextPath() + "/profilo/ordini");

        } catch (IllegalArgumentException | IllegalStateException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            UFlashMessage.addMessage(session, "danger", "Errore nell'ordine: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/checkout");

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            UFlashMessage.addMessage(session, "danger", "Si è verificato un errore durante l'acquisto: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/checkout");
        }
    }

    // =========================================================================
    // HELPER PRIVATI
    // =========================================================================

    private EIndirizzo risolviIndirizzo(Long idIndirizzo, EUtente utente, PersistentManager pm) {
        if (idIndirizzo == null) {
            throw new IllegalArgumentException("È necessario selezionare un indirizzo di spedizione.");
        }

        EIndirizzo indirizzo = pm.PMgetObjOnAttribute(EIndirizzo.class, "idIndirizzo", idIndirizzo);
        if (indirizzo == null || !indirizzo.getUtente().getIdPersona().equals(utente.getIdPersona())) {
            throw new IllegalArgumentException("L'indirizzo di spedizione selezionato non è valido.");
        }

        return indirizzo;
    }

    private ECartaDiCredito risolviCartaPagamento(HttpServletRequest request, String sceltaCarta, Long idCartaSalvata,
                                                  EUtente utente, PersistentManager pm) {
        if ("salvata".equalsIgnoreCase(sceltaCarta)) {
            if (idCartaSalvata == null) {
                throw new IllegalArgumentException("Seleziona una carta di credito valida.");
            }
            ECartaDiCredito carta = pm.PMgetObjOnAttribute(ECartaDiCredito.class, "idCarta", idCartaSalvata);
            if (carta == null || !carta.getUtente().getIdPersona().equals(utente.getIdPersona())) {
                throw new IllegalArgumentException("La carta selezionata non è valida.");
            }
            return carta;

        } else if ("nuova".equalsIgnoreCase(sceltaCarta)) {
            String numeroCarta = UHTTPMethods.postString(request, "numero_carta", null);
            String cvv = UHTTPMethods.postString(request, "cvv", null);
            String titolare = UHTTPMethods.postString(request, "titolare", null);
            String scadenza = UHTTPMethods.postString(request, "scadenza", null); // es. MM/YY
            boolean salvaCarta = UHTTPMethods.postBool(request, "salva_carta", false);

            Map<String, String> tokenData = paymentService.generaToken(numeroCarta, cvv);
            String token = tokenData.get("token");
            String ultimeQuattroCifre = tokenData.get("ultimeQuattroCifre");

            ECartaDiCredito nuovaCarta = new ECartaDiCredito(
                    titolare,
                    scadenza,
                    ultimeQuattroCifre,
                    token,
                    utente,
            );

            // Se l'utente ha scelto di salvarla per acquisti futuri, la persistiamo
            if (salvaCarta) {
                pm.PMsaveObj(nuovaCarta);
            }

            return nuovaCarta;
        }

        throw new IllegalArgumentException("Metodo di pagamento non valido.");
    }
}
