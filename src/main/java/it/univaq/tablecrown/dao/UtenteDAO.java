package it.univaq.tablecrown.dao;

import it.univaq.tablecrown.entity.EProdotto;
import it.univaq.tablecrown.entity.EOrdine;
import it.univaq.tablecrown.entity.enumerativi.DisponibilitaProdotto;
import it.univaq.tablecrown.entity.enumerativi.StatoOrdine;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtenteDAO extends GenericDAO {

    public UtenteDAO(EntityManager em) {
        super(em);
    }

    public int contaUtentiTotali() {
        try {
            String jpql = "SELECT COUNT(u) FROM EUtente u";
            Long totale = em.createQuery(jpql, Long.class).getSingleResult();
            return totale.intValue();
        } catch (Exception e) {
            System.err.println("Errore in contaUtentiTotali: " + e.getMessage());
            return 0;
        }
    }

    public int contaUtentiNuoviOggi() {
        try {
            // JPA riconosce la keyword CURRENT_DATE per prendere la data di oggi dal db
            String jpql = "SELECT COUNT(u) FROM EUtente u WHERE u.dataRegistrazione >= CURRENT_DATE";
            Long risultato = em.createQuery(jpql, Long.class).getSingleResult();
            return risultato.intValue();
        } catch (Exception e) {
            System.err.println("Errore in contaUtentiNuoviOggi: " + e.getMessage());
            return 0;
        }
    }

    public int contaUtentiSospesiTotali() {
        try {
            String jpql = "SELECT COUNT(u) FROM EUtente u WHERE u.stato = :statoUtente";
            Long risultato = em.createQuery(jpql, Long.class)
                    .setParameter("statoUtente", StatoUtente.SOSPESO)
                    .getSingleResult();
            return risultato.intValue();
        } catch (Exception e) {
            System.err.println("Errore in contaUtentiSospesiTotali: " + e.getMessage());
            return 0;
        }
    }

    public Map<String, Object> findUtentiConRecensioniSegnalate(String ordinamento) {
        try {
            // Prevenzione SQL Injection: JPA non permette parametri dinamici nell'ORDER BY
            String ordine = (ordinamento != null && ordinamento.equalsIgnoreCase("DESC")) ? "DESC" : "ASC";

            String jpql = "SELECT u, COUNT(s) FROM EUtente u JOIN u.segnalazioni s " +
                    "WHERE s.statosegnalazione = :stato " +
                    "GROUP BY u.idpersona " +
                    "ORDER BY COUNT(s) " + ordine;

            // Restituisce una lista di array generici, dove l'indice 0 è l'Utente e l'1 è il Conteggio
            List<Object[]> risultatiGrezzi = em.createQuery(jpql, Object[].class)
                    .setParameter("stato", StatoSegnalazione.IN_ATTESA)
                    .getResultList();

            // Traduzione dell'array_map di PHP in Java
            List<Map<String, Object>> listaRisultati = new ArrayList<>();
            for (Object[] riga : risultatiGrezzi) {
                Map<String, Object> mappaUtente = new HashMap<>();
                mappaUtente.put("utente", (EUtente) riga[0]);
                mappaUtente.put("numeroSegnalazioni", ((Long) riga[1]).intValue());
                listaRisultati.add(mappaUtente);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("risultati", listaRisultati);
            response.put("totale", listaRisultati.size());

            return response;

        } catch (Exception e) {
            System.err.println("Errore in findUtentiConRecensioniSegnalate: " + e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("risultati", new ArrayList<>());
            fallback.put("totale", 0);
            return fallback;
        }
    }

    public List<ERecensione> getRecensioniSegnalateDiUtente(int idUtente) {
        try {
            String jpql = "SELECT DISTINCT r FROM ESegnalazione s JOIN s.recensione r " +
                    "WHERE s.utente.idpersona = :idUtente AND s.statosegnalazione = :stato";

            return em.createQuery(jpql, ERecensione.class)
                    .setParameter("idUtente", idUtente)
                    .setParameter("stato", StatoSegnalazione.IN_ATTESA)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Errore in getRecensioniSegnalateDiUtente: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
