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
            List<String> condizioniBase = new ArrayList<>();
            Map<String, Object> parametri = new HashMap<>();

            // 1. LA FORMULA MAGICA: Prezzo reale calcolato direttamente dal database con CURRENT_TIMESTAMP
            String prezzoReale = "(CASE " +
                    "WHEN p.sconto IS NOT NULL AND p.sconto.sconto > 0 AND (p.sconto.scadenzaOfferta IS NULL OR p.sconto.scadenzaOfferta > CURRENT_TIMESTAMP) " +
                    "THEN (p.prezzo * (1.0 - p.sconto.sconto / 100.0)) " +
                    "ELSE p.prezzo END)";

            // 2. FILTRO RICERCA
            if (filtri.get("query") != null) {
                String queryCercata = (String) filtri.get("query");
                if (!queryCercata.trim().isEmpty()) {
                    condizioniBase.add("(LOWER(p.nomeProdotto) LIKE LOWER(:query) OR LOWER(p.descrizioneProdotto) LIKE LOWER(:query))");
                    parametri.put("query", "%" + queryCercata.trim() + "%");
                }
            }

            // 3. FILTRI ENUM E RATING (Senza i prezzi)
            if (filtri.get("disponibilita") != null) {
                Object dispObj = filtri.get("disponibilita");
                List<DisponibilitaProdotto> enumDisp = new ArrayList<>();

                if (dispObj instanceof List<?>) {
                    List<?> listaValori = (List<?>) dispObj;
                    for (Object valoreScelto : listaValori) {
                        if (valoreScelto instanceof DisponibilitaProdotto) {
                            enumDisp.add((DisponibilitaProdotto) valoreScelto);
                        } else if (valoreScelto instanceof String) {
                            try {
                                enumDisp.add(DisponibilitaProdotto.valueOf(((String) valoreScelto).toUpperCase()));
                            } catch (IllegalArgumentException e) { }
                        }
                    }
                } else if (dispObj instanceof String[]) {
                    for (String valoreScelto : (String[]) dispObj) {
                        try {
                            enumDisp.add(DisponibilitaProdotto.valueOf(valoreScelto.toUpperCase()));
                        } catch (IllegalArgumentException e) { }
                    }
                }

                if (!enumDisp.isEmpty()) {
                    condizioniBase.add("p.disponibilitaProdotto IN (:disponibilita)");
                    parametri.put("disponibilita", enumDisp);
                }
            }

            if (filtri.get("inEvidenzaFiltro") != null) {
                @SuppressWarnings("unchecked")
                List<String> evidenza = (List<String>) filtri.get("inEvidenzaFiltro");
                if (evidenza != null) {
                    if (evidenza.contains("novita")) {
                        condizioniBase.add("p.dataPubblicazione >= :datalimite");
                        parametri.put("datalimite", java.time.LocalDateTime.now().minusMonths(1));
                    }
                    if (evidenza.contains("sconti")) {
                        condizioniBase.add("p.sconto.sconto > 0 AND (p.sconto.scadenzaOfferta IS NULL OR p.sconto.scadenzaOfferta > CURRENT_TIMESTAMP)");
                    }
                }
            }

            if (filtri.get("ratingMin") != null && ((Number) filtri.get("ratingMin")).doubleValue() > 0) {
                condizioniBase.add("p.valutazioneMedia >= :ratingMin");
                parametri.put("ratingMin", filtri.get("ratingMin"));
            }

            // 4. CALCOLO MIN E MAX ASSOLUTI (Applicato al prezzo REALE)
            StringBuilder jpqlSenzaPrezzi = new StringBuilder("FROM EPortaDadi p ");
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

            // Assemblaggio della query per COUNT e SELECT
            StringBuilder jpqlFinale = new StringBuilder("FROM EPortaDadi p ");
            if (!condizioniPrezzo.isEmpty()) {
                jpqlFinale.append("WHERE ").append(String.join(" AND ", condizioniPrezzo)).append(" ");
            }

            // 6. ESECUZIONE QUERY (COUNT E LISTA)
            jakarta.persistence.TypedQuery<Long> queryCount = em.createQuery("SELECT COUNT(p) " + jpqlFinale.toString(), Long.class);
            parametri.forEach(queryCount::setParameter);
            Long totale = queryCount.getSingleResult();

            StringBuilder jpqlMain = new StringBuilder("SELECT p ").append(jpqlFinale.toString());

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

            jakarta.persistence.TypedQuery<EPortaDadi> queryMain = em.createQuery(jpqlMain.toString(), EPortaDadi.class);
            parametri.forEach(queryMain::setParameter);

            queryMain.setFirstResult(offset);
            queryMain.setMaxResults(limit);
            List<EPortaDadi> risultati = queryMain.getResultList();

            // 7. Costruzione della Risposta
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