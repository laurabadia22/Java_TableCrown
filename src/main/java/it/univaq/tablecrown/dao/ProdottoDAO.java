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
import java.time.LocalDateTime;

/**
 * Estende GenericDAO per ereditare l'EntityManager condiviso
 * e i metodi generici (saveObj, getAll, ecc.). Qui aggiungiamo solo le query specifiche per EProdotto
 */
public class ProdottoDAO extends GenericDAO {

    public ProdottoDAO(EntityManager em) {
        super(em);
    }

    public Map<String, Object> findProdottiInOfferta(int limit, int offset) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Nota: il CASE WHEN va direttamente nell'ORDER BY, non serve
            // dichiararlo nel SELECT come "colonna nascosta" (non esiste in JPQL).
            String jpql = "SELECT p FROM EProdotto p " +
                    "WHERE p.sconto.sconto > 0 " +
                    "AND (p.sconto.scadenzaOfferta IS NULL OR p.sconto.scadenzaOfferta > :oggi) " +
                    "AND p.disponibilitaProdotto = :disponibile " +
                    "AND p.quantita > 0 " +
                    //ordino i prodotti in offerta, mettendo però prima quelli con scadenza imminente
                    "ORDER BY CASE WHEN p.sconto.scadenzaOfferta IS NULL THEN 1 ELSE 0 END ASC, " +
                    "p.sconto.scadenzaOfferta ASC";
//            String jpql = "SELECT p FROM EProdotto p " +
//                    "WHERE p.sconto.sconto > 0 " +
//                    "AND (p.sconto.scadenzaOfferta IS NULL OR p.sconto.scadenzaOfferta > :oggi) " +
//                    "AND p.disponibilitaProdotto = :disponibile " +
//                    "AND p.quantita > 0 " +
//                    "ORDER BY p.sconto.scadenzaOfferta ASC NULLS LAST";

            TypedQuery<EProdotto> query = em.createQuery(jpql, EProdotto.class)
                    .setParameter("disponibile", DisponibilitaProdotto.DISPONIBILE)
                    .setParameter("oggi", LocalDateTime.now())
                    .setFirstResult(offset)
                    .setMaxResults(limit);

            List<EProdotto> risultati = query.getResultList();


            String countJpql = "SELECT COUNT(p) FROM EProdotto p " +
                    "WHERE p.sconto.sconto > 0 " +
                    "AND p.disponibilitaProdotto = :disponibile " +
                    "AND p.quantita > 0";

            Long totale = em.createQuery(countJpql, Long.class)
                    .setParameter("disponibile", DisponibilitaProdotto.DISPONIBILE)
                    .getSingleResult();

            response.put("risultati", risultati);
            response.put("totale", totale);

        } catch (Exception e) {
            System.err.println("Errore in findProdottiInOfferta: " + e.getMessage());
            response.put("risultati", List.of());
            response.put("totale", 0L);
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