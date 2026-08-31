package it.univaq.tablecrown.dao;

import it.univaq.tablecrown.entity.EGiocoDaTavolo;
import it.univaq.tablecrown.entity.enumerativi.Categoria;
import it.univaq.tablecrown.entity.enumerativi.DifficoltaGioco;
import it.univaq.tablecrown.entity.enumerativi.DisponibilitaProdotto;
import it.univaq.tablecrown.entity.enumerativi.LinguaGioco;
import it.univaq.tablecrown.entity.enumerativi.LivelloDannoGiochi;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiocoDaTavoloDAO extends GenericDAO {

    public GiocoDaTavoloDAO(EntityManager em) {
        super(em);
    }

    public Map<String, Object> findGiochi(Map<String, Object> filtri, int limit, int offset) {
        try {
            StringBuilder jpqlBase = new StringBuilder("FROM EGiocoDaTavolo g JOIN g.prezzo pr ");
            List<String> condizioni = new ArrayList<>();
            Map<String, Object> parametri = new HashMap<>();

            // 1. Filtri Enum Base
            if (filtri.containsKey("difficolta")) {
                List<DifficoltaGioco> enumDiff = parseEnumList(filtri.get("difficolta"), DifficoltaGioco.class);
                if (!enumDiff.isEmpty()) {
                    condizioni.add("g.difficolta IN (:difficolta)");
                    parametri.put("difficolta", enumDiff);
                }
            }

            if (filtri.containsKey("lingua_selected")) {
                List<LinguaGioco> enumLingua = parseEnumList(filtri.get("lingua_selected"), LinguaGioco.class);
                if (!enumLingua.isEmpty()) {
                    condizioni.add("g.lingua IN (:lingua)");
                    parametri.put("lingua", enumLingua);
                }
            }

            // AGGIORNATO: Il danno ora è un campo diretto di g
            if (filtri.containsKey("danno_selected")) {
                List<LivelloDannoGiochi> enumDanno = parseEnumList(filtri.get("danno_selected"), LivelloDannoGiochi.class);
                if (!enumDanno.isEmpty()) {
                    condizioni.add("g.livelloDanno IN (:danni)");
                    parametri.put("danni", enumDanno);
                }
            }

            if (filtri.containsKey("disponibilita")) {
                List<DisponibilitaProdotto> enumDisp = parseEnumList(filtri.get("disponibilita"), DisponibilitaProdotto.class);
                if (!enumDisp.isEmpty()) {
                    condizioni.add("g.disponibilitaProdotto IN (:disponibilita)");
                    parametri.put("disponibilita", enumDisp);
                }
            }

            // AGGIORNATO: Filtro Categorie (Set di Enum con JOIN dinamico)
            if (filtri.containsKey("categoria_selected")) {
                List<Categoria> enumCat = parseEnumList(filtri.get("categoria_selected"), Categoria.class);
                if (!enumCat.isEmpty()) {
                    jpqlBase.append("JOIN g.categoria cat ");
                    condizioni.add("cat IN (:categorie)");
                    parametri.put("categorie", enumCat);
                }
            }

            // 3. Filtri Numerici ed Esatti
            if (filtri.containsKey("price_min")) {
                condizioni.add("pr.valore >= :price_min");
                parametri.put("price_min", filtri.get("price_min"));
            }

            if (filtri.containsKey("price_max")) {
                condizioni.add("pr.valore <= :price_max");
                parametri.put("price_max", filtri.get("price_max"));
            }

            if (filtri.containsKey("mostra_espansioni") && Boolean.FALSE.equals(filtri.get("mostra_espansioni"))) {
                condizioni.add("g.giocoBase IS NULL");
            }

            if (filtri.containsKey("players_min")) {
                condizioni.add("g.numeroGiocatoriMin = :players_min");
                parametri.put("players_min", filtri.get("players_min"));
            }

            if (filtri.containsKey("players_max")) {
                condizioni.add("g.numeroGiocatoriMax = :players_max");
                parametri.put("players_max", filtri.get("players_max"));
            }

            if (filtri.containsKey("age_min")) {
                condizioni.add("g.etaMinima = :age_min");
                parametri.put("age_min", filtri.get("age_min"));
            }

            if (filtri.containsKey("rating_min") && ((Number) filtri.get("rating_min")).doubleValue() > 0) {
                condizioni.add("g.valutazioneMedia >= :ratingMin");
                parametri.put("ratingMin", filtri.get("rating_min"));
            }

            // 4. Novità e Sconti
            if (filtri.containsKey("in_evidenza_filtro")) {
                @SuppressWarnings("unchecked")
                List<String> evidenza = (List<String>) filtri.get("in_evidenza_filtro");
                if (evidenza != null) {
                    if (evidenza.contains("novita")) {
                        condizioni.add("g.dataPubblicazione >= :datalimite AND g.disponibilitaProdotto = :dispNovita");
                        parametri.put("datalimite", LocalDate.now().minusMonths(1));
                        parametri.put("dispNovita", DisponibilitaProdotto.DISPONIBILE);
                    }
                    if (evidenza.contains("sconti")) {
                        condizioni.add("pr.sconto > 0");
                    }
                }
            }

            // Assemblaggio WHERE
            if (!condizioni.isEmpty()) {
                jpqlBase.append("WHERE ").append(String.join(" AND ", condizioni)).append(" ");
            }

            // Query COUNT (DISTINCT necessario a causa del potenziale JOIN con categoria)
            Query queryCount = em.createQuery("SELECT COUNT(DISTINCT g) " + jpqlBase.toString());
            parametri.forEach(queryCount::setParameter);
            Long totale = (Long) queryCount.getSingleResult();

            // Query MIN/MAX
            Query queryMinMax = em.createQuery("SELECT MIN(pr.valore), MAX(pr.valore) " + jpqlBase.toString());
            parametri.forEach(queryMinMax::setParameter);
            Object[] estremi = (Object[]) queryMinMax.getSingleResult();

            double prezzoMinimo = (estremi[0] != null) ? ((Number) estremi[0]).doubleValue() : 0.0;
            double prezzoMassimo = (estremi[1] != null) ? ((Number) estremi[1]).doubleValue() : 200.0;

            // Query Principale (DISTINCT obbligatorio)
            StringBuilder jpqlMain = new StringBuilder("SELECT DISTINCT g ").append(jpqlBase.toString());

            String ordinamento = (String) filtri.get("ordinamento");
            if (ordinamento != null && !ordinamento.isEmpty()) {
                switch (ordinamento) {
                    case "prezzo-asc":  jpqlMain.append("ORDER BY pr.valore ASC"); break;
                    case "prezzo-desc": jpqlMain.append("ORDER BY pr.valore DESC"); break;
                    case "popolarita":  jpqlMain.append("ORDER BY g.numeroVendite DESC"); break;
                    case "rating":      jpqlMain.append("ORDER BY g.valutazioneMedia DESC"); break;
                    default:            jpqlMain.append("ORDER BY g.dataPubblicazione DESC"); break;
                }
            } else {
                jpqlMain.append("ORDER BY g.dataPubblicazione DESC");
            }

            TypedQuery<EGiocoDaTavolo> queryMain = em.createQuery(jpqlMain.toString(), EGiocoDaTavolo.class);
            parametri.forEach(queryMain::setParameter);
            queryMain.setFirstResult(offset);
            queryMain.setMaxResults(limit);
            List<EGiocoDaTavolo> risultati = queryMain.getResultList();

            Map<String, Object> response = new HashMap<>();
            response.put("risultati", risultati);
            response.put("totale", totale.intValue());
            response.put("rangemin", prezzoMinimo);
            response.put("rangemax", prezzoMassimo);
            return response;

        } catch (Exception e) {
            System.err.println("Errore in findGiochi: " + e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("risultati", new ArrayList<>());
            fallback.put("totale", 0);
            fallback.put("rangemin", 0.0);
            fallback.put("rangemax", 200.0);
            return fallback;
        }
    }

    public Map<String, Object> ricercaGiochi(String stringaDiRicerca, int limit, int offset) {
        try {
            String testoPulito = (stringaDiRicerca != null) ? stringaDiRicerca.trim() : "";

            if (testoPulito.isEmpty()) {
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("risultati", new ArrayList<>());
                fallback.put("totale", 0);
                return fallback;
            }

            String jpqlBase = "FROM EGiocoDaTavolo g WHERE g.nomeProdotto LIKE :ricerca OR g.descrizioneProdotto LIKE :ricerca";
            String termineRicerca = "%" + testoPulito + "%";

            Long totale = em.createQuery("SELECT COUNT(g) " + jpqlBase, Long.class)
                    .setParameter("ricerca", termineRicerca)
                    .getSingleResult();

            List<EGiocoDaTavolo> risultati = em.createQuery("SELECT g " + jpqlBase, EGiocoDaTavolo.class)
                    .setParameter("ricerca", termineRicerca)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();

            Map<String, Object> response = new HashMap<>();
            response.put("risultati", risultati);
            response.put("totale", totale.intValue());
            return response;

        } catch (Exception e) {
            System.err.println("Errore in ricercaGiochi: " + e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("risultati", new ArrayList<>());
            fallback.put("totale", 0);
            return fallback;
        }
    }

    // =========================================================================
    // HELPER: Convertitore Universale di Enum
    // =========================================================================
    private <E extends Enum<E>> List<E> parseEnumList(Object obj, Class<E> enumClass) {
        List<E> result = new ArrayList<>();
        if (obj == null) return result;

        if (obj instanceof List<?>) {
            for (Object val : (List<?>) obj) {
                if (enumClass.isInstance(val)) {
                    result.add(enumClass.cast(val));
                } else if (val instanceof String) {
                    try { result.add(Enum.valueOf(enumClass, ((String) val).toUpperCase())); }
                    catch (IllegalArgumentException ignored) {}
                }
            }
        } else if (obj instanceof String[]) {
            for (String val : (String[]) obj) {
                try { result.add(Enum.valueOf(enumClass, val.toUpperCase())); }
                catch (IllegalArgumentException ignored) {}
            }
        }
        return result;
    }
}