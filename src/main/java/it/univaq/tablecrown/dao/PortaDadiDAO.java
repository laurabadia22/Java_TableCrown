package it.univaq.tablecrown.dao;

import it.univaq.tablecrown.entity.EPortaDadi;
import it.univaq.tablecrown.entity.enumerativi.DisponibilitaProdotto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortaDadiDAO extends GenericDAO {

    public PortaDadiDAO(EntityManager em) {
        super(em);
    }

    public Map<String, Object> findPortaDadi(Map<String, Object> filtri, int limit, int offset) {
        try {
            //lista di stringhe che rappresentano gli identificativi
            List<String> condizioniBase = new ArrayList<>();
            //mappa per i valori associati ai parametri nominali
            Map<String, Object> parametri = new HashMap<>();

            //controllo se il prodotto bustine ha uo sconto associato valido. current_timestump è una funzione nativa JPQL che restituisce data e ora correnti
            String prezzoReale = "(CASE " +
                    "WHEN p.sconto IS NOT NULL AND p.sconto.sconto > 0 AND (p.sconto.scadenzaOfferta IS NULL OR p.sconto.scadenzaOfferta > CURRENT_TIMESTAMP) " +
                    "THEN (p.prezzo * (1.0 - p.sconto.sconto / 100.0)) " +
                    "ELSE p.prezzo END)";

            //controllo se è stata usata la barra di ricerca
            if (filtri.get("query") != null) {
                String queryCercata = (String) filtri.get("query");
                if (!queryCercata.trim().isEmpty()) {
                    condizioniBase.add("(LOWER(p.nomeProdotto) LIKE LOWER(:query) OR LOWER(p.descrizioneProdotto) LIKE LOWER(:query))");
                    parametri.put("query", "%" + queryCercata.trim() + "%");
                }
            }

            //controllo sui filtri selezionati
            if (filtri.get("disponibilita") != null) {
                Object dispObj = filtri.get("disponibilita");
                List<DisponibilitaProdotto> enumDisp = new ArrayList<>();

                //se il control manda una lista di disponibilità, controllo se sono stringhe o oggetti misti e faccio il parsing con l'enum
                if (dispObj instanceof List<?>) {
                    for (Object valoreScelto : (List<?>) dispObj) {
                        //se è un enum di DisponibilitaProdotto lo aggiunge alla lista delle disponibilità selezionate
                        if (valoreScelto instanceof DisponibilitaProdotto) {
                            enumDisp.add((DisponibilitaProdotto) valoreScelto);
                        } else if (valoreScelto instanceof String) {
                            try {
                                enumDisp.add(DisponibilitaProdotto.valueOf(((String) valoreScelto).toUpperCase()));
                            } catch (IllegalArgumentException ignored) { }
                        }
                    }
                    //se invece è un array di stringhe trasforma le varie stringhe nell'enum corrispondente
                } else if (dispObj instanceof String[]) {
                    for (String valoreScelto : (String[]) dispObj) {
                        try {
                            enumDisp.add(DisponibilitaProdotto.valueOf(valoreScelto.toUpperCase()));
                        } catch (IllegalArgumentException ignored) { }
                    }
                }

                if (!enumDisp.isEmpty()) {
                    condizioniBase.add("p.disponibilitaProdotto IN (:disponibilita)");
                    parametri.put("disponibilita", enumDisp);
                }
            }

//            if (filtri.get("inEvidenzaFiltro") != null) {
//                @SuppressWarnings("unchecked")
//                List<String> evidenza = (List<String>) filtri.get("inEvidenzaFiltro");
//                if (evidenza != null) {
//                    if (evidenza.contains("novita")) {
//                        condizioniBase.add("p.dataPubblicazione >= :datalimite");
//                        parametri.put("datalimite", java.time.LocalDateTime.now().minusMonths(1));
//                    }
//                    if (evidenza.contains("sconti")) {
//                        condizioniBase.add("p.sconto.sconto > 0 AND (p.sconto.scadenzaOfferta IS NULL OR p.sconto.scadenzaOfferta > CURRENT_TIMESTAMP)");
//                    }
//                }
//            }

//            if (filtri.get("ratingMin") != null && ((Number) filtri.get("ratingMin")).doubleValue() > 0) {
//                condizioniBase.add("p.valutazione.media >= :ratingMin");
//                parametri.put("ratingMin", filtri.get("ratingMin"));
//            }

            //costruisce la stringa jpql per tutte le condizioni ad esclusione dei limirti slider prezzo
            StringBuilder jpqlSenzaPrezzi = new StringBuilder("FROM EPortaDadi p ");
            if (!condizioniBase.isEmpty()) {
                jpqlSenzaPrezzi.append("WHERE ").append(String.join(" AND ", condizioniBase)).append(" ");
            }

            //calcolo prezzi max e min trovati dalla ricerca con la query precedente per settare gli slider
            jakarta.persistence.TypedQuery<Object[]> queryMinMax = em.createQuery("SELECT MIN(" + prezzoReale + "), MAX(" + prezzoReale + ") " + jpqlSenzaPrezzi.toString(), Object[].class);
            parametri.forEach(queryMinMax::setParameter);
            Object[] estremi = queryMinMax.getSingleResult();

            double rawMin = (estremi[0] != null) ? ((Number) estremi[0]).doubleValue() : 0.0;
            double rawMax = (estremi[1] != null) ? ((Number) estremi[1]).doubleValue() : 50.0;

            double prezzoMinimo = Math.round(rawMin * 100.0) / 100.0;
            double prezzoMassimo = Math.round(rawMax * 100.0) / 100.0;

            //clona la query precedente e aggiunge i filtri prezzo
            List<String> condizioniPrezzo = new ArrayList<>(condizioniBase);

            if (filtri.get("prezzoMin") != null && ((Number) filtri.get("prezzoMin")).doubleValue() > 0) {
                condizioniPrezzo.add(prezzoReale + " >= :prezzoMin");
                parametri.put("prezzoMin", filtri.get("prezzoMin"));
            }

            if (filtri.get("prezzoMax") != null && ((Number) filtri.get("prezzoMax")).doubleValue() > 0) {
                condizioniPrezzo.add(prezzoReale + " <= :prezzoMax");
                parametri.put("prezzoMax", filtri.get("prezzoMax"));
            }

            // costruzione query finale per count e select
            StringBuilder jpqlFinale = new StringBuilder("FROM EPortaDadi p ");
            if (!condizioniPrezzo.isEmpty()) {
                jpqlFinale.append("WHERE ").append(String.join(" AND ", condizioniPrezzo)).append(" ");
            }

            //esecuzione count
            jakarta.persistence.TypedQuery<Long> queryCount = em.createQuery("SELECT COUNT(p) " + jpqlFinale.toString(), Long.class);
            parametri.forEach(queryCount::setParameter);
            Long totale = queryCount.getSingleResult();

            //cosstruzione select
            StringBuilder jpqlMain = new StringBuilder("SELECT p ").append(jpqlFinale.toString());

            //costruzione ordinamento tramite l'ultimojpql
            String ordinamento = (String) filtri.get("ordinamento");
            if (ordinamento != null && !ordinamento.isEmpty()) {
                switch (ordinamento) {
                    case "prezzo_asc":  jpqlMain.append("ORDER BY ").append(prezzoReale).append(" ASC"); break;
                    case "prezzo_desc": jpqlMain.append("ORDER BY ").append(prezzoReale).append(" DESC"); break;
                    case "popolarita":  jpqlMain.append("ORDER BY p.numeroVendite DESC"); break;
                    case "valutazione": jpqlMain.append("ORDER BY p.valutazione.media DESC"); break;
                    default:            jpqlMain.append("ORDER BY p.dataPubblicazione DESC"); break;
                }
            } else {
                jpqlMain.append("ORDER BY p.dataPubblicazione DESC");
            }

            //esecuzione jpql finale
            jakarta.persistence.TypedQuery<EPortaDadi> queryMain = em.createQuery(jpqlMain.toString(), EPortaDadi.class);
            parametri.forEach(queryMain::setParameter);

            queryMain.setFirstResult(offset);
            queryMain.setMaxResults(limit);
            List<EPortaDadi> risultati = queryMain.getResultList();

            //
            Map<String, Object> response = new HashMap<>();
            response.put("risultati", risultati);
            response.put("totale", totale.intValue());
            response.put("rangemin", prezzoMinimo);
            response.put("rangemax", prezzoMassimo);
            return response;

        } catch (Exception e) {
            System.err.println("Errore in findPortaDadi: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> fallback = new HashMap<>();
            fallback.put("risultati", new ArrayList<>());
            fallback.put("totale", 0);
            fallback.put("rangemin", 0.0);
            fallback.put("rangemax", 50.0);
            return fallback;
        }
    }

    public Map<String, Object> ricercaPortaDadi(String stringaDiRicerca, int limit, int offset) {
        try {
            String testoPulito = (stringaDiRicerca != null) ? stringaDiRicerca.trim() : "";

            if (testoPulito.isEmpty()) {
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("risultati", new ArrayList<>());
                fallback.put("totale", 0);
                return fallback;
            }

            String jpqlBase = "FROM EPortaDadi p WHERE p.nomeProdotto LIKE :ricerca OR p.descrizioneProdotto LIKE :ricerca";
            String termineRicerca = "%" + testoPulito + "%";

            // Count
            Long totale = em.createQuery("SELECT COUNT(p) " + jpqlBase, Long.class)
                    .setParameter("ricerca", termineRicerca)
                    .getSingleResult();

            // Risultati impaginati
            List<EPortaDadi> risultati = em.createQuery("SELECT p " + jpqlBase, EPortaDadi.class)
                    .setParameter("ricerca", termineRicerca)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();

            Map<String, Object> response = new HashMap<>();
            response.put("risultati", risultati);
            response.put("totale", totale.intValue());
            return response;

        } catch (Exception e) {
            System.err.println("Errore in ricercaPortaDadi: " + e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("risultati", new ArrayList<>());
            fallback.put("totale", 0);
            return fallback;
        }
    }
}