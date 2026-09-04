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
import java.time.LocalDateTime;

public class GiocoDaTavoloDAO extends GenericDAO {

    public GiocoDaTavoloDAO(EntityManager em) {
        super(em);
    }

    public Map<String, Object> findGiochi(Map<String, Object> filtri, int limit, int offset) {
        try {
            // RIMOSSO: il JOIN su g.prezzo, ora usiamo direttamente g
            StringBuilder jpqlBase = new StringBuilder("FROM EGiocoDaTavolo g ");

            List<String> condizioni = new ArrayList<>();
            Map<String, Object> parametri = new HashMap<>();

            // Filtri Enum Base
            if (filtri.get("difficolta") != null) {
                List<DifficoltaGioco> enumDiff = parseEnumList(filtri.get("difficolta"), DifficoltaGioco.class);
                if (!enumDiff.isEmpty()) {
                    condizioni.add("g.difficolta IN (:difficolta)");
                    parametri.put("difficolta", enumDiff);
                }
            }

            if (filtri.get("lingua") != null) {
                List<LinguaGioco> enumLingua = parseEnumList(filtri.get("lingua"), LinguaGioco.class);
                if (!enumLingua.isEmpty()) {
                    condizioni.add("g.lingua IN (:lingua)");
                    parametri.put("lingua", enumLingua);
                }
            }

            if (filtri.get("danno") != null) {
                List<LivelloDannoGiochi> enumDanno = parseEnumList(filtri.get("danno"), LivelloDannoGiochi.class);
                if (!enumDanno.isEmpty()) {
                    condizioni.add("g.livelloDanno IN (:danni)");
                    parametri.put("danni", enumDanno);
                }
            }

            if (filtri.get("disponibilita") != null) {
                List<DisponibilitaProdotto> enumDisp = parseEnumList(filtri.get("disponibilita"), DisponibilitaProdotto.class);
                if (!enumDisp.isEmpty()) {
                    condizioni.add("g.disponibilitaProdotto IN (:disponibilita)");
                    parametri.put("disponibilita", enumDisp);
                }
            }

            // Filtro Categorie (Set di Enum con JOIN dinamico) - QUESTO RIMANE perché le categorie sono una collezione (tabella a parte)
            if (filtri.get("categoria") != null) {
                List<Categoria> enumCat = parseEnumList(filtri.get("categoria"), Categoria.class);
                if (!enumCat.isEmpty()) {
                    jpqlBase.append("JOIN g.categoria cat ");
                    condizioni.add("cat IN (:categorie)");
                    parametri.put("categorie", enumCat);
                }
            }

            // 3. Filtri Numerici ed Esatti
            if (filtri.get("prezzoMin") != null && ((Number) filtri.get("prezzoMin")).doubleValue() > 0) {
                condizioni.add("g.prezzo >= :prezzoMin"); // MODIFICATO da pr.valore a g.prezzo
                parametri.put("prezzoMin", filtri.get("prezzoMin"));
            }

            if (filtri.get("prezzoMax") != null) {
                condizioni.add("g.prezzo <= :prezzoMax"); // MODIFICATO da pr.valore a g.prezzo
                parametri.put("prezzoMax", filtri.get("prezzoMax"));
            }

            if (filtri.get("mostraEspansioni") != null && Boolean.FALSE.equals(filtri.get("mostraEspansioni"))) {
                condizioni.add("g.giocoBase IS NULL");
            }

            if (filtri.get("giocatoriMin") != null) {
                condizioni.add("g.numeroGiocatoriMin <= :giocatoriMin AND g.numeroGiocatoriMax >= :giocatoriMin");
                parametri.put("giocatoriMin", filtri.get("giocatoriMin"));
            }

            if (filtri.get("giocatoriMax") != null) {
                condizioni.add("g.numeroGiocatoriMax = :giocatoriMax");
                parametri.put("giocatoriMax", filtri.get("giocatoriMax"));
            }

            if (filtri.get("etaMinima") != null) {
                condizioni.add("g.etaMinima = :etaMinima");
                parametri.put("etaMinima", filtri.get("etaMinima"));
            }

            if (filtri.get("ratingMin") != null && ((Number) filtri.get("ratingMin")).doubleValue() > 0) {
                condizioni.add("g.valutazioneMedia >= :ratingMin");
                parametri.put("ratingMin", filtri.get("ratingMin"));
            }



            // 4. Novità e Sconti
            if (filtri.get("inEvidenzaFiltro") != null) {
                @SuppressWarnings("unchecked")
                List<String> evidenza = (List<String>) filtri.get("inEvidenzaFiltro");
                if (evidenza != null) {
                    if (evidenza.contains("novita")) {
                        condizioni.add("g.dataPubblicazione >= :datalimite AND g.disponibilitaProdotto = :dispNovita");
                        parametri.put("datalimite", LocalDate.now().minusMonths(1));
                        parametri.put("dispNovita", DisponibilitaProdotto.DISPONIBILE);
                    }
                    if (evidenza.contains("sconti")) {
                        // Esige che lo sconto sia > 0 e che (la data sia nulla OPPURE nel futuro)
                        condizioni.add("g.sconto.sconto > 0 AND (g.sconto.scadenzaOfferta IS NULL OR g.sconto.scadenzaOfferta > :oggiSconti)");
                        parametri.put("oggiSconti", LocalDateTime.now());
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

            // Query MIN/MAX - MODIFICATO da pr.valore a g.prezzo
            Query queryMinMax = em.createQuery("SELECT MIN(g.prezzo), MAX(g.prezzo) " + jpqlBase.toString());
            parametri.forEach(queryMinMax::setParameter);
            Object[] estremi = (Object[]) queryMinMax.getSingleResult();

            double rawMin = (estremi[0] != null) ? ((Number) estremi[0]).doubleValue() : 0.0;
            double rawMax = (estremi[1] != null) ? ((Number) estremi[1]).doubleValue() : 200.0;

            // Arrotonda a due cifre decimali (es. 14.039949 -> 14.04)
            double prezzoMinimo = Math.round(rawMin * 100.0) / 100.0;
            double prezzoMassimo = Math.round(rawMax * 100.0) / 100.0;

            // Query Principale (DISTINCT obbligatorio)
            StringBuilder jpqlMain = new StringBuilder("SELECT DISTINCT g ").append(jpqlBase.toString());

            String ordinamento = (String) filtri.get("ordinamento");
            if (ordinamento != null && !ordinamento.isEmpty()) {
                switch (ordinamento) {
                    case "prezzo-asc":  jpqlMain.append("ORDER BY g.prezzo ASC"); break; // MODIFICATO
                    case "prezzo-desc": jpqlMain.append("ORDER BY g.prezzo DESC"); break; // MODIFICATO
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
    //metodo di supporto per questa classe utilizzato nel metofdo find, funziona per un qualsiasi tipo di enum <E>
    private <E extends Enum<E>> List<E> parseEnumList(Object obj, Class<E> enumClass) {
        List<E> result = new ArrayList<>();
        if (obj == null) return result;

        //l'oggetto è un'istanza di una qualsiasi lista di oggetti generici?
        if (obj instanceof List<?>) {
            //(List<?>) obj è un cast di obj, serve per trattarlo come una list, di qualsiasi tipo <?> per poterci iterare sopra
            for (Object val : (List<?>) obj) {
                //controllo se il valore che sto controllando è già un enumerativo del tipo che mi è stato passato, in tal caso lo aggiunge direttamente alla lista risultato
                if (enumClass.isInstance(val)) {
                    result.add(enumClass.cast(val));
                }
                //controllo se il valore è una stringa e lo converto nel tipo di enumerativo passato
                //altrimenti restituisco un'eccezione senza bloccare l'esecuzione
                else if (val instanceof String) {
                    try { result.add(Enum.valueOf(enumClass, ((String) val).toUpperCase())); }
                    catch (IllegalArgumentException ignored) {}
                }
            }
        //l'oggetto è un array di stringhe
        } else if (obj instanceof String[]) {
            for (String val : (String[]) obj) {
                try { result.add(Enum.valueOf(enumClass, val.toUpperCase())); }
                catch (IllegalArgumentException ignored) {}
            }
        }
        return result;
    }
}