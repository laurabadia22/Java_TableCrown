package it.univaq.tablecrown.dao;

import it.univaq.tablecrown.entity.EProdotto;
import it.univaq.tablecrown.entity.EOrdine;
import it.univaq.tablecrown.entity.enumerativi.DisponibilitaProdotto;
import it.univaq.tablecrown.entity.enumerativi.StatoOrdine;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * Estende GenericDAO per ereditare l'EntityManager condiviso
 * e i metodi generici (saveObj, getAll, ecc.). Qui aggiungiamo solo le query specifiche per EProdotto
 */
public class ProdottoDAO extends GenericDAO {

    public ProdottoDAO(EntityManager em) {
        super(em);
    }

    public Map<String, Object> findProdottiInOfferta(Map<String, Object> filtri, int limit, int offset) {
        Map<String, Object> response = new HashMap<>();
        try {
            StringBuilder jpqlBase = new StringBuilder("FROM EProdotto p ");
            List<String> condizioni = new ArrayList<>();
            Map<String, Object> parametri = new HashMap<>();

            // 1. Condizioni Fisse (Offerte attive e prodotti disponibili)
            condizioni.add("p.sconto.sconto > 0");
            condizioni.add("(p.sconto.scadenzaOfferta IS NULL OR p.sconto.scadenzaOfferta > :oggi)");
            parametri.put("oggi", LocalDateTime.now());

            condizioni.add("p.disponibilitaProdotto = :disponibile");
            parametri.put("disponibile", DisponibilitaProdotto.DISPONIBILE);

            condizioni.add("p.quantita > 0");

            // 2. Filtri Dinamici: Prezzo Min e Max (calcolati sul prezzo scontato)
            if (filtri.get("prezzoMin") != null) {
                condizioni.add("(p.prezzo * (1.0 - p.sconto.sconto / 100.0)) >= :prezzoMin");
                parametri.put("prezzoMin", filtri.get("prezzoMin"));
            }

            if (filtri.get("prezzoMax") != null) {
                condizioni.add("(p.prezzo * (1.0 - p.sconto.sconto / 100.0)) <= :prezzoMax");
                parametri.put("prezzoMax", filtri.get("prezzoMax"));
            }

            // Assemblaggio blocco WHERE
            if (!condizioni.isEmpty()) {
                jpqlBase.append("WHERE ").append(String.join(" AND ", condizioni)).append(" ");
            }

            // 3. Query: MIN / MAX Prezzo per gestire eventuali slider (calcolato sul prezzo scontato)
            TypedQuery<Object[]> queryMinMax = em.createQuery("SELECT MIN(p.prezzo * (1.0 - p.sconto.sconto / 100.0)), MAX(p.prezzo * (1.0 - p.sconto.sconto / 100.0)) " + jpqlBase.toString(), Object[].class);
            parametri.forEach(queryMinMax::setParameter);
            Object[] estremi = queryMinMax.getSingleResult();

            double rawMin = (estremi[0] != null) ? ((Number) estremi[0]).doubleValue() : 0.0;
            double rawMax = (estremi[1] != null) ? ((Number) estremi[1]).doubleValue() : 200.0;

            // Applichiamo la correzione degli arrotondamenti per i float (es. 14.0399)
            double prezzoMinimo = Math.round(rawMin * 100.0) / 100.0;
            double prezzoMassimo = Math.round(rawMax * 100.0) / 100.0;

            // 4. Query: COUNT
            TypedQuery<Long> queryCount = em.createQuery("SELECT COUNT(p) " + jpqlBase.toString(), Long.class);
            parametri.forEach(queryCount::setParameter);
            Long totale = queryCount.getSingleResult();

            // 5. Query: Risultati e Ordinamento
            String jpqlMain = "SELECT p " + jpqlBase.toString() +
                    "ORDER BY CASE WHEN p.sconto.scadenzaOfferta IS NULL THEN 1 ELSE 0 END ASC, " +
                    "p.sconto.scadenzaOfferta ASC";

            TypedQuery<EProdotto> query = em.createQuery(jpqlMain, EProdotto.class);
            parametri.forEach(query::setParameter);

            query.setFirstResult(offset);
            query.setMaxResults(limit);

            List<EProdotto> risultati = query.getResultList();

            // Costruzione Risposta
            response.put("risultati", risultati);
            response.put("totale", totale);
            response.put("rangemin", prezzoMinimo);
            response.put("rangemax", prezzoMassimo);

        } catch (Exception e) {
            System.err.println("Errore in findProdottiInOfferta: " + e.getMessage());
            response.put("risultati", new ArrayList<>());
            response.put("totale", 0L);
            response.put("rangemin", 0.0);
            response.put("rangemax", 200.0);
        }
        return response;
    }


    public boolean utenteHasProdotto(int idUtente, int idProdotto) {
        try {
            String jpql = "SELECT COUNT(o) FROM EOrdine o " +
                    "JOIN o.ordineItems oi " +
                    "JOIN oi.prodotto p " +
                    "WHERE o.utente.idPersona = :idUtente " +
                    "AND p.idProdotto = :idProdotto " +
                    "AND o.stato = :statoCompletato";

            Long risultato = em.createQuery(jpql, Long.class)
                    .setParameter("idUtente", idUtente)
                    .setParameter("idProdotto", idProdotto)
                    .setParameter("statoCompletato", StatoOrdine.IN_LAVORAZIONE)
                    .getSingleResult();

            return risultato > 0;

        } catch (Exception e) {
            System.err.println("Errore in utenteHasProdotto: " + e.getMessage());
            return false;
        }
    }


    public List<EProdotto> findCorrelati(List<Long> prodottiEsclusi, int limit) {
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT p FROM EProdotto p " +
                            "WHERE p.quantita > 0 " +
                            "AND p.disponibilitaProdotto = :disponibile "
            );

            boolean haEsclusi = prodottiEsclusi != null && !prodottiEsclusi.isEmpty();
            if (haEsclusi) {
                jpql.append("AND p.idProdotto NOT IN :esclusi ");
            }
            jpql.append("ORDER BY p.numeroVendite DESC");

            TypedQuery<EProdotto> query = em.createQuery(jpql.toString(), EProdotto.class)
                    .setParameter("disponibile", DisponibilitaProdotto.DISPONIBILE)
                    .setMaxResults(limit);

            if (haEsclusi) {
                query.setParameter("esclusi", prodottiEsclusi);
            }

            return query.getResultList();

        } catch (Exception e) {
            System.err.println("Errore in findCorrelati: " + e.getMessage());
            return List.of();
        }
    }
}