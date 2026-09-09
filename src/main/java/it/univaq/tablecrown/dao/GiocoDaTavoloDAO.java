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
            List<String> condizioniBase = new ArrayList<>();
            Map<String, Object> parametri = new HashMap<>();

            // 1. FILTRI ENUM (Identici a prima)
            if (filtri.get("difficolta") != null) {
                List<DifficoltaGioco> enumDiff = parseEnumList(filtri.get("difficolta"), DifficoltaGioco.class);
                if (!enumDiff.isEmpty()) {
                    condizioniBase.add("g.difficolta IN (:difficolta)");
                    parametri.put("difficolta", enumDiff);
                }
            }
            if (filtri.get("lingua") != null) {
                List<LinguaGioco> enumLingua = parseEnumList(filtri.get("lingua"), LinguaGioco.class);
                if (!enumLingua.isEmpty()) {
                    condizioniBase.add("g.lingua IN (:lingua)");
                    parametri.put("lingua", enumLingua);
                }
            }
            if (filtri.get("danno") != null) {
                List<LivelloDannoGiochi> enumDanno = parseEnumList(filtri.get("danno"), LivelloDannoGiochi.class);
                if (!enumDanno.isEmpty()) {
                    condizioniBase.add("g.livelloDanno IN (:danni)");
                    parametri.put("danni", enumDanno);
                }
            }
            if (filtri.get("disponibilita") != null) {
                List<DisponibilitaProdotto> enumDisp = parseEnumList(filtri.get("disponibilita"), DisponibilitaProdotto.class);
                if (!enumDisp.isEmpty()) {
                    condizioniBase.add("g.disponibilitaProdotto IN (:disponibilita)");
                    parametri.put("disponibilita", enumDisp);
                }
            }
            if (filtri.get("categoria") != null) {
                List<Categoria> enumCat = parseEnumList(filtri.get("categoria"), Categoria.class);
                if (!enumCat.isEmpty()) {
                    condizioniBase.add("EXISTS (SELECT c FROM EGiocoDaTavolo g2 JOIN g2.categoria c WHERE g2 = g AND c IN (:categorie))");
                    parametri.put("categorie", enumCat);
                }
            }

            // 2. ALTRI FILTRI (Senza i prezzi)
            if (filtri.get("mostraEspansioni") != null && Boolean.FALSE.equals(filtri.get("mostraEspansioni"))) {
                condizioniBase.add("g.giocoBase IS NULL");
            }
            if (filtri.get("giocatoriMin") != null) {
                condizioniBase.add("g.numeroGiocatoriMin <= :giocatoriMin AND g.numeroGiocatoriMax >= :giocatoriMin");
                parametri.put("giocatoriMin", filtri.get("giocatoriMin"));
            }
            if (filtri.get("giocatoriMax") != null) {
                condizioniBase.add("g.numeroGiocatoriMax = :giocatoriMax");
                parametri.put("giocatoriMax", filtri.get("giocatoriMax"));
            }
            if (filtri.get("etaMinima") != null) {
                condizioniBase.add("g.etaMinima = :etaMinima");
                parametri.put("etaMinima", filtri.get("etaMinima"));
            }
            if (filtri.get("ratingMin") != null && ((Number) filtri.get("ratingMin")).doubleValue() > 0) {
                condizioniBase.add("g.valutazioneMedia >= :ratingMin");
                parametri.put("ratingMin", filtri.get("ratingMin"));
            }

            if (filtri.get("inEvidenzaFiltro") != null) {
                @SuppressWarnings("unchecked")
                List<String> evidenza = (List<String>) filtri.get("inEvidenzaFiltro");
                if (evidenza != null) {
                    if (evidenza.contains("novita")) {
                        condizioniBase.add("g.dataPubblicazione >= :datalimite AND g.disponibilitaProdotto = :dispNovita");
                        parametri.put("datalimite", LocalDate.now().minusMonths(1));
                        parametri.put("dispNovita", DisponibilitaProdotto.DISPONIBILE);
                    }
                    if (evidenza.contains("sconti")) {
                        // CORRETTO: utilizzo dei campi diretti dell'entità per evitare crash JPQL
                        condizioniBase.add("g.valoreSconto > 0 AND (g.scadenzaOfferta IS NULL OR g.scadenzaOfferta > :oggiSconti)");
                        parametri.put("oggiSconti", LocalDateTime.now());
                    }
                }
            }

            // 3. CALCOLO MIN E MAX ASSOLUTI (Senza filtri di prezzo applicati)
            StringBuilder jpqlSenzaPrezzi = new StringBuilder("FROM EGiocoDaTavolo g ");
            if (!condizioniBase.isEmpty()) {
                jpqlSenzaPrezzi.append("WHERE ").append(String.join(" AND ", condizioniBase)).append(" ");
            }

            Query queryMinMax = em.createQuery("SELECT MIN(g.prezzo), MAX(g.prezzo) " + jpqlSenzaPrezzi.toString());
            parametri.forEach(queryMinMax::setParameter);
            Object[] estremi = (Object[]) queryMinMax.getSingleResult();

            double rawMin = (estremi[0] != null) ? ((Number) estremi[0]).doubleValue() : 0.0;
            double rawMax = (estremi[1] != null) ? ((Number) estremi[1]).doubleValue() : 200.0;
            double prezzoMinimo = Math.round(rawMin * 100.0) / 100.0;
            double prezzoMassimo = Math.round(rawMax * 100.0) / 100.0;

            // 4. AGGIUNTA DEI FILTRI DI PREZZO ALLA QUERY FINALE
            List<String> condizioniPrezzo = new ArrayList<>(condizioniBase);

            if (filtri.get("prezzoMin") != null && ((Number) filtri.get("prezzoMin")).doubleValue() > 0) {
                condizioniPrezzo.add("g.prezzo >= :prezzoMin");
                parametri.put("prezzoMin", filtri.get("prezzoMin"));
            }

            // Protezione aggiunta: controlla che prezzoMax sia maggiore di 0 per evitare bug del frontend
            if (filtri.get("prezzoMax") != null && ((Number) filtri.get("prezzoMax")).doubleValue() > 0) {
                condizioniPrezzo.add("g.prezzo <= :prezzoMax");
                parametri.put("prezzoMax", filtri.get("prezzoMax"));
            }

            // Assemblaggio della stringa query finale (Count e Main)
            StringBuilder jpqlFinale = new StringBuilder("FROM EGiocoDaTavolo g ");
            if (!condizioniPrezzo.isEmpty()) {
                jpqlFinale.append("WHERE ").append(String.join(" AND ", condizioniPrezzo)).append(" ");
            }

            // 5. ESECUZIONE QUERY PRINCIPALI
            Query queryCount = em.createQuery("SELECT COUNT(DISTINCT g) " + jpqlFinale.toString());
            parametri.forEach(queryCount::setParameter);
            Long totale = (Long) queryCount.getSingleResult();

            StringBuilder jpqlMain = new StringBuilder("SELECT DISTINCT g ").append(jpqlFinale.toString());
            String ordinamento = (String) filtri.get("ordinamento");

            if (ordinamento != null && !ordinamento.isEmpty()) {
                switch (ordinamento) {
                    // CORRETTO: Uso dell'underscore per matchare i dati inviati dal BaseController
                    case "prezzo_asc":  jpqlMain.append("ORDER BY g.prezzo ASC"); break;
                    case "prezzo_desc": jpqlMain.append("ORDER BY g.prezzo DESC"); break;
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
            // Stampiamo l'intero StackTrace invece del solo messaggio per individuare subito futuri bug JPQL
            System.err.println("Errore in findGiochi (Filtri applicati: " + filtri + "):");
            e.printStackTrace();

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

        //l'oggetto è già una singola istanza dell'Enum
        if (enumClass.isInstance(obj)) {
            result.add(enumClass.cast(obj));
            return result;
        }

        //l'oggetto è una singola Stringa
        if (obj instanceof String) {
            String str = ((String) obj).trim();
            if (!str.isEmpty()) {
                try {
                    result.add(Enum.valueOf(enumClass, str.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
            return result;
        }

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