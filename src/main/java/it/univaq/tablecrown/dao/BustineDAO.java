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
            // RIMOSSO: il JOIN su b.prezzo
            StringBuilder jpqlBase = new StringBuilder("FROM EBustine b ");
            List<String> condizioni = new ArrayList<>();
            Map<String, Object> parametri = new HashMap<>();

            // 1. Filtri Prezzo
            if (filtri.get("prezzoMin") != null) {
                condizioni.add("b.prezzo >= :prezzoMin"); // MODIFICATO
                parametri.put("prezzoMin", filtri.get("prezzoMin"));
            }

            if (filtri.get("prezzoMax") != null) {
                condizioni.add("b.prezzo <= :prezzoMax"); // MODIFICATO
                parametri.put("prezzoMax", filtri.get("prezzoMax"));
            }

            // 2. Filtro Disponibilità (con protezione sicura per parsing flessibile)
            if (filtri.get("disponibilita") != null) {
                Object dispObj = filtri.get("disponibilita");
                List<DisponibilitaProdotto> enumDisp = new ArrayList<>();

                if (dispObj instanceof List<?>) {
                    for (Object valoreScelto : (List<?>) dispObj) {
                        if (valoreScelto instanceof DisponibilitaProdotto) {
                            enumDisp.add((DisponibilitaProdotto) valoreScelto);
                        } else if (valoreScelto instanceof String) {
                            try {
                                enumDisp.add(DisponibilitaProdotto.valueOf(((String) valoreScelto).toUpperCase()));
                            } catch (IllegalArgumentException ignored) {}
                        }
                    }
                } else if (dispObj instanceof String[]) {
                    for (String valoreScelto : (String[]) dispObj) {
                        try {
                            enumDisp.add(DisponibilitaProdotto.valueOf(valoreScelto.toUpperCase()));
                        } catch (IllegalArgumentException ignored) {}
                    }
                }

                if (!enumDisp.isEmpty()) {
                    condizioni.add("b.disponibilitaProdotto IN (:disponibilita)");
                    parametri.put("disponibilita", enumDisp);
                }
            }

            // 3. Filtro Novità e Sconti
            if (filtri.get("inEvidenzaFiltro") != null) {
                @SuppressWarnings("unchecked")
                List<String> evidenza = (List<String>) filtri.get("inEvidenzaFiltro");
                if (evidenza != null) {
                    if (evidenza.contains("novita")) {
                        condizioni.add("b.dataPubblicazione >= :datalimite");
                        parametri.put("datalimite", LocalDate.now().minusMonths(1));
                    }
                    if (evidenza.contains("sconti")) {
                        // Esige che lo sconto sia > 0 e che (la data sia nulla OPPURE nel futuro)
                        condizioni.add("b.sconto.sconto > 0 AND (b.sconto.scadenzaOfferta IS NULL OR b.sconto.scadenzaOfferta > :oggiSconti)");
                        parametri.put("oggiSconti", LocalDateTime.now());
                    }
                }
            }

            // 4. Filtro Rating
            if (filtri.get("ratingMin") != null && ((Number) filtri.get("ratingMin")).doubleValue() > 0) {
                condizioni.add("b.valutazioneMedia >= :ratingMin");
                parametri.put("ratingMin", filtri.get("ratingMin"));
            }

            // Assemblaggio del blocco WHERE
            if (!condizioni.isEmpty()) {
                jpqlBase.append("WHERE ").append(String.join(" AND ", condizioni)).append(" ");
            }

            // Query 1: Totale dei record
            Query queryCount = em.createQuery("SELECT COUNT(b) " + jpqlBase.toString());
            parametri.forEach(queryCount::setParameter);
            Long totale = (Long) queryCount.getSingleResult();

            // Query 2: Prezzi minimi e massimi
            Query queryMinMax = em.createQuery("SELECT MIN(b.prezzo), MAX(b.prezzo) " + jpqlBase.toString()); // MODIFICATO
            parametri.forEach(queryMinMax::setParameter);
            Object[] estremi = (Object[]) queryMinMax.getSingleResult();

            double rawMin = (estremi[0] != null) ? ((Number) estremi[0]).doubleValue() : 0.0;
            double rawMax = (estremi[1] != null) ? ((Number) estremi[1]).doubleValue() : 50.0;

            // Arrotonda a due cifre decimali (es. 14.039949 -> 14.04)
            double prezzoMinimo = Math.round(rawMin * 100.0) / 100.0;
            double prezzoMassimo = Math.round(rawMax * 100.0) / 100.0;

            // Query 3: Dati effettivi e Ordinamento
            StringBuilder jpqlMain = new StringBuilder("SELECT b ").append(jpqlBase.toString());

            String ordinamento = (String) filtri.get("ordinamento");
            if (ordinamento != null && !ordinamento.isEmpty()) {
                switch (ordinamento) {
                    case "prezzo-asc":  jpqlMain.append("ORDER BY b.prezzo ASC"); break; // MODIFICATO
                    case "prezzo-desc": jpqlMain.append("ORDER BY b.prezzo DESC"); break; // MODIFICATO
                    case "popolarita":  jpqlMain.append("ORDER BY b.numeroVendite DESC"); break;
                    case "rating":      jpqlMain.append("ORDER BY b.valutazioneMedia DESC"); break;
                    default:            jpqlMain.append("ORDER BY b.dataPubblicazione DESC"); break;
                }
            } else {
                jpqlMain.append("ORDER BY b.dataPubblicazione DESC");
            }

            TypedQuery<EBustine> queryMain = em.createQuery(jpqlMain.toString(), EBustine.class);
            parametri.forEach(queryMain::setParameter);

            queryMain.setFirstResult(offset);
            queryMain.setMaxResults(limit);
            List<EBustine> risultati = queryMain.getResultList();

            // Costruzione risposta
            Map<String, Object> response = new HashMap<>();
            response.put("risultati", risultati);
            response.put("totale", totale.intValue());
            response.put("rangemin", prezzoMinimo);
            response.put("rangemax", prezzoMassimo);
            return response;

        } catch (Exception e) {
            System.err.println("Errore in findBustine: " + e.getMessage());
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