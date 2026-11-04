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
        try {
            //lista per parti della query jpql
            List<String> condizioniBase = new ArrayList<>();
            //dizionario con parametri e valori da sostituire nei parametri dellea condizioneBase
            Map<String, Object> parametri = new HashMap<>();

            String prezzoScontato = "(p.prezzo * (1.0 - p.sconto.sconto / 100.0))";

            // estraggo solo offerte valide
            condizioniBase.add("p.sconto.sconto > 0");
            condizioniBase.add("(p.sconto.scadenzaOfferta IS NULL OR p.sconto.scadenzaOfferta > CURRENT_TIMESTAMP)");

            //filtro sui prodotti effettivamente disponibili
            condizioniBase.add("p.disponibilitaProdotto = :disponibile");
            parametri.put("disponibile", DisponibilitaProdotto.DISPONIBILE);
            condizioniBase.add("p.quantita > 0");

            //controllo se è stata utilizzata la barra di ricerca
            Object queryObj = filtri.get("query");
            if (queryObj instanceof String && !((String) queryObj).trim().isEmpty()) {
                condizioniBase.add("(LOWER(p.nomeProdotto) LIKE LOWER(:query) OR LOWER(p.descrizioneProdotto) LIKE LOWER(:query))");
                parametri.put("query", "%" + ((String) queryObj).trim() + "%");
            }

            String jpqlSenzaPrezzi = "FROM EProdotto p WHERE " + String.join(" AND ", condizioniBase) + " ";

            //calcolo i limiti dello slider per la ricerca effettuata
            TypedQuery<Object[]> queryMinMax = em.createQuery(
                    "SELECT MIN(" + prezzoScontato + "), MAX(" + prezzoScontato + ") " + jpqlSenzaPrezzi,
                    Object[].class);
            parametri.forEach(queryMinMax::setParameter);
            Object[] estremi = queryMinMax.getSingleResult();

            double rawMin = (estremi[0] != null) ? ((Number) estremi[0]).doubleValue() : 0.0;
            double rawMax = (estremi[1] != null) ? ((Number) estremi[1]).doubleValue() : 200.0;
            double prezzoMinimo = Math.round(rawMin * 100.0) / 100.0;
            double prezzoMassimo = Math.round(rawMax * 100.0) / 100.0;

            //clono la query aggiungendo i vincoli di prezzo
            List<String> condizioniPrezzo = new ArrayList<>(condizioniBase);
            Map<String, Object> parametriPrezzo = new HashMap<>(parametri);

            //valori settati dall'utente nello slider, toDouble è una funzione interna privata per parsificare in double un valore
            Double prezzoMin = toDouble(filtri.get("prezzoMin"));
            if (prezzoMin != null && prezzoMin > 0) {
                condizioniPrezzo.add(prezzoScontato + " >= :prezzoMin");
                parametriPrezzo.put("prezzoMin", prezzoMin);
            }

            Double prezzoMax = toDouble(filtri.get("prezzoMax"));
            if (prezzoMax != null && prezzoMax > 0) {
                condizioniPrezzo.add(prezzoScontato + " <= :prezzoMax");
                parametriPrezzo.put("prezzoMax", prezzoMax);
            }

            String jpqlFinale = "FROM EProdotto p WHERE " + String.join(" AND ", condizioniPrezzo) + " ";

            // creazione count
            TypedQuery<Long> queryCount = em.createQuery("SELECT COUNT(p) " + jpqlFinale, Long.class);
            parametriPrezzo.forEach(queryCount::setParameter);
            Long totale = queryCount.getSingleResult();

            // ordinamento, offerte in scadenza prima di quelle senza scadenza
            String orderDefault = "ORDER BY CASE WHEN p.sconto.scadenzaOfferta IS NULL THEN 1 ELSE 0 END ASC, "
                    + "p.sconto.scadenzaOfferta ASC";
            String ordinamento = (String) filtri.get("ordinamento");
            String orderBy;
            if (ordinamento == null || ordinamento.isEmpty()) {
                orderBy = orderDefault;
            } else {
                switch (ordinamento) {
                    case "prezzo_asc":  orderBy = "ORDER BY " + prezzoScontato + " ASC"; break;
                    case "prezzo_desc": orderBy = "ORDER BY " + prezzoScontato + " DESC"; break;
                    case "popolarita":  orderBy = "ORDER BY p.numeroVendite DESC"; break;
                    case "valutazione": orderBy = "ORDER BY p.valutazione.media DESC"; break;
                    default:            orderBy = orderDefault; break;
                }
            }

            //costruzione select
            TypedQuery<EProdotto> queryMain = em.createQuery("SELECT p " + jpqlFinale + orderBy, EProdotto.class);
            parametriPrezzo.forEach(queryMain::setParameter);
            queryMain.setFirstResult(offset);
            queryMain.setMaxResults(limit);
            List<EProdotto> risultati = queryMain.getResultList();

            //metto i risultati in un dizionario per il control
            Map<String, Object> response = new HashMap<>();
            response.put("risultati", risultati);
            response.put("totale", totale.intValue());   // stesso tipo di findBustine
            response.put("rangemin", prezzoMinimo);
            response.put("rangemax", prezzoMassimo);
            return response;

        } catch (Exception e) {
            System.err.println("Errore in findProdottiInOfferta: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("risultati", new ArrayList<>());
            fallback.put("totale", 0);
            fallback.put("rangemin", 0.0);
            fallback.put("rangemax", 200.0);
            return fallback;
        }
    }

    // converte in Double un valore arrivato dal controller (Number o String)
    private static Double toDouble(Object valore) {
        if (valore instanceof Number) return ((Number) valore).doubleValue();
        if (valore instanceof String && !((String) valore).trim().isEmpty()) {
            try { return Double.parseDouble(((String) valore).trim().replace(",", ".")); }
            catch (NumberFormatException ignored) {}
        }
        return null;
    }


//    public boolean utenteHasProdotto(int idUtente, int idProdotto) {
//        try {
//            String jpql = "SELECT COUNT(o) FROM EOrdine o " +
//                    "JOIN o.ordineItems oi " +
//                    "JOIN oi.prodotto p " +
//                    "WHERE o.utente.idPersona = :idUtente " +
//                    "AND p.idProdotto = :idProdotto " +
//                    "AND o.stato = :statoCompletato";
//
//            Long risultato = em.createQuery(jpql, Long.class)
//                    .setParameter("idUtente", idUtente)
//                    .setParameter("idProdotto", idProdotto)
//                    .setParameter("statoCompletato", StatoOrdine.IN_LAVORAZIONE)
//                    .getSingleResult();
//
//            return risultato > 0;
//
//        } catch (Exception e) {
//            System.err.println("Errore in utenteHasProdotto: " + e.getMessage());
//            return false;
//        }
//    }


    //prodottiEsclusi sono i prodotti che il cliente ha già nel carrello e che sta guardando attualmente, perciò non sono mostrati nei correlati
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