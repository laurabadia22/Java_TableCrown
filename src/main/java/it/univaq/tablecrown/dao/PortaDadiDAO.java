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
            // 1. Inizializziamo i costruttori per la query dinamica (RIMOSSA LA JOIN)
            StringBuilder jpqlBase = new StringBuilder("FROM EPortaDadi p ");
            List<String> condizioni = new ArrayList<>();
            Map<String, Object> parametri = new HashMap<>();

            // 2. Costruzione dinamica dei filtri
            if (filtri.containsKey("price_min")) {
                condizioni.add("p.prezzo >= :prezzo_min"); // MODIFICATO
                parametri.put("prezzo_min", filtri.get("price_min"));
            }

            if (filtri.containsKey("price_max")) {
                condizioni.add("p.prezzo <= :prezzo_max"); // MODIFICATO
                parametri.put("prezzo_max", filtri.get("price_max"));
            }

            if (filtri.containsKey("disponibilita")) {
                Object dispObj = filtri.get("disponibilita");
                List<DisponibilitaProdotto> enumDisp = new ArrayList<>();

                // Controlliamo se la Servlet ci ha passato una Lista (es. List<String> o List<DisponibilitaProdotto>)
                if (dispObj instanceof List<?>) {
                    List<?> listaValori = (List<?>) dispObj;
                    for (Object valoreScelto : listaValori) {
                        if (valoreScelto instanceof DisponibilitaProdotto) {
                            enumDisp.add((DisponibilitaProdotto) valoreScelto);
                        } else if (valoreScelto instanceof String) {
                            try {
                                // L'equivalente Java del tryFrom di PHP
                                enumDisp.add(DisponibilitaProdotto.valueOf(((String) valoreScelto).toUpperCase()));
                            } catch (IllegalArgumentException e) {
                                // La stringa non corrisponde a nessun Enum, la ignoriamo
                            }
                        }
                    }
                }
                // Controlliamo se la Servlet ci ha passato direttamente l'array nativo della request HTML (String[])
                else if (dispObj instanceof String[]) {
                    for (String valoreScelto : (String[]) dispObj) {
                        try {
                            enumDisp.add(DisponibilitaProdotto.valueOf(valoreScelto.toUpperCase()));
                        } catch (IllegalArgumentException e) { }
                    }
                }

                if (!enumDisp.isEmpty()) {
                    condizioni.add("p.disponibilitaProdotto IN (:disponibilita)");
                    parametri.put("disponibilita", enumDisp);
                }
            }

            if (filtri.containsKey("in_evidenza_filtro")) {
                @SuppressWarnings("unchecked")
                List<String> evidenza = (List<String>) filtri.get("in_evidenza_filtro");
                if (evidenza != null) {
                    if (evidenza.contains("novita")) {
                        condizioni.add("p.dataPubblicazione >= :datalimite");
                        parametri.put("datalimite", LocalDateTime.now().minusMonths(1));
                    }
                    if (evidenza.contains("sconti")) {
                        // Esige che lo sconto sia > 0 e che (la data sia nulla OPPURE nel futuro)
                        condizioni.add("p.sconto.sconto > 0 AND (p.sconto.scadenzaOfferta IS NULL OR p.sconto.scadenzaOfferta > :oggiSconti)");
                        parametri.put("oggiSconti", LocalDateTime.now());
                    }
                }
            }

            if (filtri.containsKey("rating_min") && ((Number) filtri.get("rating_min")).doubleValue() > 0) {
                condizioni.add("p.valutazioneMedia >= :ratingMin");
                parametri.put("ratingMin", filtri.get("rating_min"));
            }

            // 3. Unione delle condizioni
            if (!condizioni.isEmpty()) {
                jpqlBase.append("WHERE ").append(String.join(" AND ", condizioni)).append(" ");
            }

            // 4. Query: COUNT
            TypedQuery<Long> queryCount = em.createQuery("SELECT COUNT(p) " + jpqlBase.toString(), Long.class);
            parametri.forEach(queryCount::setParameter);
            Long totale = queryCount.getSingleResult();

            // 5. Query: MIN / MAX Prezzo
            TypedQuery<Object[]> queryMinMax = em.createQuery("SELECT MIN(p.prezzo), MAX(p.prezzo) " + jpqlBase.toString(), Object[].class); // MODIFICATO
            parametri.forEach(queryMinMax::setParameter);
            Object[] estremi = queryMinMax.getSingleResult();

            double prezzoMinimo = (estremi[0] != null) ? ((Number) estremi[0]).doubleValue() : 0.0;
            double prezzoMassimo = (estremi[1] != null) ? ((Number) estremi[1]).doubleValue() : 50.0;

            // 6. Query: ORDINAMENTO E PAGINAZIONE (Risultati finali)
            StringBuilder jpqlMain = new StringBuilder("SELECT p ").append(jpqlBase.toString());

            String ordinamento = (String) filtri.get("ordinamento");
            if (ordinamento != null && !ordinamento.isEmpty()) {
                switch (ordinamento) {
                    case "prezzo-asc":  jpqlMain.append("ORDER BY p.prezzo ASC"); break; // MODIFICATO
                    case "prezzo-desc": jpqlMain.append("ORDER BY p.prezzo DESC"); break; // MODIFICATO
                    case "popolarita":  jpqlMain.append("ORDER BY p.numeroVendite DESC"); break;
                    case "rating":      jpqlMain.append("ORDER BY p.valutazioneMedia DESC"); break;
                    default:            jpqlMain.append("ORDER BY p.dataPubblicazione DESC"); break;
                }
            } else {
                jpqlMain.append("ORDER BY p.dataPubblicazione DESC");
            }

            TypedQuery<EPortaDadi> queryMain = em.createQuery(jpqlMain.toString(), EPortaDadi.class);
            parametri.forEach(queryMain::setParameter); // Inietta tutti i parametri mappati

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