package it.univaq.tablecrown.dao;

import it.univaq.tablecrown.entity.EBustine;
import it.univaq.tablecrown.entity.enumerativi.DisponibilitaProdotto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

public class BustineDAO extends GenericDAO {

    public BustineDAO(EntityManager em) {
        super(em);
    }

    public Map<String, Object> findBustine(Map<String, Object> filtri, int limit, int offset) {
        try {
            List<String> condizioniBase = new ArrayList<>();
            Map<String, Object> parametri = new HashMap<>();

            // 1. LA FORMULA MAGICA: Usiamo CURRENT_TIMESTAMP così Hibernate non va più in crash per i parametri mancanti!
            String prezzoReale = "(CASE " +
                    "WHEN b.sconto IS NOT NULL AND b.sconto.sconto > 0 AND (b.sconto.scadenzaOfferta IS NULL OR b.sconto.scadenzaOfferta > CURRENT_TIMESTAMP) " +
                    "THEN (b.prezzo * (1.0 - b.sconto.sconto / 100.0)) " +
                    "ELSE b.prezzo END)";

            // 2. FILTRO RICERCA
            if (filtri.get("query") != null) {
                String queryCercata = (String) filtri.get("query");
                if (!queryCercata.trim().isEmpty()){
                    condizioniBase.add("(LOWER(b.nomeProdotto) LIKE LOWER(:query) OR LOWER(b.descrizioneProdotto) LIKE LOWER(:query))");
                    parametri.put("query", "%" + queryCercata.trim() + "%");
                }
            }

            // 3. FILTRI ENUM E RATING (Senza i prezzi)
            if (filtri.get("disponibilita") != null) {
                Object dispObj = filtri.get("disponibilita");
                List<DisponibilitaProdotto> enumDisp = new ArrayList<>();

                if (dispObj instanceof List<?>) {
                    for (Object valoreScelto : (List<?>) dispObj) {
                        if (valoreScelto instanceof DisponibilitaProdotto) {
                            enumDisp.add((DisponibilitaProdotto) valoreScelto);
                        } else if (valoreScelto instanceof String) {
                            try { enumDisp.add(DisponibilitaProdotto.valueOf(((String) valoreScelto).toUpperCase())); } catch (IllegalArgumentException ignored) {}
                        }
                    }
                } else if (dispObj instanceof String[]) {
                    for (String valoreScelto : (String[]) dispObj) {
                        try { enumDisp.add(DisponibilitaProdotto.valueOf(valoreScelto.toUpperCase())); } catch (IllegalArgumentException ignored) {}
                    }
                }

                if (!enumDisp.isEmpty()) {
                    condizioniBase.add("b.disponibilitaProdotto IN (:disponibilita)");
                    parametri.put("disponibilita", enumDisp);
                }
            }

            if (filtri.get("inEvidenzaFiltro") != null) {
                @SuppressWarnings("unchecked")
                List<String> evidenza = (List<String>) filtri.get("inEvidenzaFiltro");
                if (evidenza != null) {
                    if (evidenza.contains("novita")) {
                        condizioniBase.add("b.dataPubblicazione >= :datalimite");
                        parametri.put("datalimite", java.time.LocalDate.now().minusMonths(1));
                    }
                    if (evidenza.contains("sconti")) {
                        condizioniBase.add("b.sconto.sconto > 0 AND (b.sconto.scadenzaOfferta IS NULL OR b.sconto.scadenzaOfferta > CURRENT_TIMESTAMP)");
                    }
                }
            }

            if (filtri.get("ratingMin") != null && ((Number) filtri.get("ratingMin")).doubleValue() > 0) {
                condizioniBase.add("b.valutazioneMedia >= :ratingMin");
                parametri.put("ratingMin", filtri.get("ratingMin"));
            }

            // 4. CALCOLO MIN E MAX ASSOLUTI (Applicato al prezzo REALE scontato)
            StringBuilder jpqlSenzaPrezzi = new StringBuilder("FROM EBustine b ");
            if (!condizioniBase.isEmpty()) {
                jpqlSenzaPrezzi.append("WHERE ").append(String.join(" AND ", condizioniBase)).append(" ");
            }

            jakarta.persistence.TypedQuery<Object[]> queryMinMax = em.createQuery("SELECT MIN(" + prezzoReale + "), MAX(" + prezzoReale + ") " + jpqlSenzaPrezzi.toString(), Object[].class);
            parametri.forEach(queryMinMax::setParameter);
            Object[] estremi = queryMinMax.getSingleResult();

            double rawMin = (estremi[0] != null) ? ((Number) estremi[0]).doubleValue() : 0.0;
            double rawMax = (estremi[1] != null) ? ((Number) estremi[1]).doubleValue() : 50.0;

            double prezzoMinimo = Math.round(rawMin * 100.0) / 100.0;
            double prezzoMassimo = Math.round(rawMax * 100.0) / 100.0;

            // 5. AGGIUNTA DEI FILTRI DI PREZZO ALLA QUERY FINALE
            List<String> condizioniPrezzo = new ArrayList<>(condizioniBase);

            if (filtri.get("prezzoMin") != null && ((Number) filtri.get("prezzoMin")).doubleValue() > 0) {
                condizioniPrezzo.add(prezzoReale + " >= :prezzoMin");
                parametri.put("prezzoMin", filtri.get("prezzoMin"));
            }

            if (filtri.get("prezzoMax") != null && ((Number) filtri.get("prezzoMax")).doubleValue() > 0) {
                condizioniPrezzo.add(prezzoReale + " <= :prezzoMax");
                parametri.put("prezzoMax", filtri.get("prezzoMax"));
            }

            // Assemblaggio della stringa finale
            StringBuilder jpqlFinale = new StringBuilder("FROM EBustine b ");
            if (!condizioniPrezzo.isEmpty()) {
                jpqlFinale.append("WHERE ").append(String.join(" AND ", condizioniPrezzo)).append(" ");
            }

            // 6. ESECUZIONE QUERY (COUNT E LISTA)
            jakarta.persistence.TypedQuery<Long> queryCount = em.createQuery("SELECT COUNT(b) " + jpqlFinale.toString(), Long.class);
            parametri.forEach(queryCount::setParameter);
            Long totale = queryCount.getSingleResult();

            StringBuilder jpqlMain = new StringBuilder("SELECT b ").append(jpqlFinale.toString());

            String ordinamento = (String) filtri.get("ordinamento");
            if (ordinamento != null && !ordinamento.isEmpty()) {
                switch (ordinamento) {
                    case "prezzo_asc":  jpqlMain.append("ORDER BY ").append(prezzoReale).append(" ASC"); break;
                    case "prezzo_desc": jpqlMain.append("ORDER BY ").append(prezzoReale).append(" DESC"); break;
                    case "popolarita":  jpqlMain.append("ORDER BY b.numeroVendite DESC"); break;
                    case "valutazione": jpqlMain.append("ORDER BY b.valutazione.media DESC"); break;
                    default:            jpqlMain.append("ORDER BY b.dataPubblicazione DESC"); break;
                }
            } else {
                jpqlMain.append("ORDER BY b.dataPubblicazione DESC");
            }

            jakarta.persistence.TypedQuery<EBustine> queryMain = em.createQuery(jpqlMain.toString(), EBustine.class);
            parametri.forEach(queryMain::setParameter);

            queryMain.setFirstResult(offset);
            queryMain.setMaxResults(limit);
            List<EBustine> risultati = queryMain.getResultList();

            Map<String, Object> response = new HashMap<>();
            response.put("risultati", risultati);
            response.put("totale", totale.intValue());
            response.put("rangemin", prezzoMinimo);
            response.put("rangemax", prezzoMassimo);
            return response;

        } catch (Exception e) {
            System.err.println("Errore in findBustine: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("risultati", new ArrayList<>());
            fallback.put("totale", 0);
            fallback.put("rangemin", 0.0);
            fallback.put("rangemax", 50.0);
            return fallback;
        }
    }

    public Map<String, Object> ricercaBustine(String stringaDiRicerca, int limit, int offset) {
        try {
            String testoPulito = (stringaDiRicerca != null) ? stringaDiRicerca.trim() : "";

            if (testoPulito.isEmpty()) {
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("risultati", new ArrayList<>());
                fallback.put("totale", 0);
                return fallback;
            }

            String jpqlBase = "FROM EBustine b WHERE b.nomeProdotto LIKE :ricerca OR b.descrizioneProdotto LIKE :ricerca";
            String termineRicerca = "%" + testoPulito + "%";

            // Conteggio risultati
            Long totale = em.createQuery("SELECT COUNT(b) " + jpqlBase, Long.class)
                    .setParameter("ricerca", termineRicerca)
                    .getSingleResult();

            // Estrazione risultati impaginati
            List<EBustine> risultati = em.createQuery("SELECT b " + jpqlBase, EBustine.class)
                    .setParameter("ricerca", termineRicerca)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();

            Map<String, Object> response = new HashMap<>();
            response.put("risultati", risultati);
            response.put("totale", totale.intValue());
            return response;

        } catch (Exception e) {
            System.err.println("Errore in ricercaBustine: " + e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("risultati", new ArrayList<>());
            fallback.put("totale", 0);
            return fallback;
        }
    }
}