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
            //lista di stringhe che rappresentano gli identificativi
            List<String> condizioniBase = new ArrayList<>();
            //mappa per i valori associati ai parametri nominali
            Map<String, Object> parametri = new HashMap<>();

            //controllo se il prodotto bustine ha uo sconto associato valido. current_timestump è una funzione nativa JPQL che restituisce data e ora correnti
            String prezzoReale = "(CASE " +
                    "WHEN b.sconto IS NOT NULL AND b.sconto.sconto > 0 AND (b.sconto.scadenzaOfferta IS NULL OR b.sconto.scadenzaOfferta > CURRENT_TIMESTAMP) " +
                    "THEN (b.prezzo * (1.0 - b.sconto.sconto / 100.0)) " +
                    "ELSE b.prezzo END)";

            //controllo se è stata utilizzata la barra di ricerca
            if (filtri.get("query") != null) {
                String queryCercata = (String) filtri.get("query");
                if (!queryCercata.trim().isEmpty()){
                    condizioniBase.add("(LOWER(b.nomeProdotto) LIKE LOWER(:query) OR LOWER(b.descrizioneProdotto) LIKE LOWER(:query))");
                    parametri.put("query", "%" + queryCercata.trim() + "%");
                }
            }

            //controllo sui filtri selezionati
            if (filtri.get("disponibilita") != null) {
                Object dispObj = filtri.get("disponibilita");
                List<DisponibilitaProdotto> enumDisp = new ArrayList<>();

                //se il control manda una lista di disponibilità, ciclo  su ogni elemento per estrarlo
                if (dispObj instanceof List<?>) {
                    for (Object valoreScelto : (List<?>) dispObj) {
                        //se è un enum di DisponibilitaProdotto lo aggiunge alla lista delle disponibilità selezionate
                        if (valoreScelto instanceof DisponibilitaProdotto) {
                            enumDisp.add((DisponibilitaProdotto) valoreScelto);
                        } else if (valoreScelto instanceof String) {//se il valore è una stringa lo converte in un enum di disponibiitaprodotto
                            try { enumDisp.add(DisponibilitaProdotto.valueOf(((String) valoreScelto).toUpperCase())); } catch (IllegalArgumentException ignored) {}
                        }
                    }
                //se invece è un array di stringhe trasforma le varie stringhe nell'enum corrispondente
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

//            if (filtri.get("inEvidenzaFiltro") != null) {
//                @SuppressWarnings("unchecked")
//                List<String> evidenza = (List<String>) filtri.get("inEvidenzaFiltro");
//                if (evidenza != null) {
//                    if (evidenza.contains("novita")) {
//                        condizioniBase.add("b.dataPubblicazione >= :datalimite");
//                        parametri.put("datalimite", java.time.LocalDate.now().minusMonths(1));
//                    }
//                    if (evidenza.contains("sconti")) {
//                        condizioniBase.add("b.sconto.sconto > 0 AND (b.sconto.scadenzaOfferta IS NULL OR b.sconto.scadenzaOfferta > CURRENT_TIMESTAMP)");
//                    }
//                }
//            }

              //filtro sulla valutazione media, estraggo i prodotti con valutazione maggiore di un certo valore
//            if (filtri.get("ratingMin") != null && ((Number) filtri.get("ratingMin")).doubleValue() > 0) {
//                condizioniBase.add("b.valutazione.media >= :ratingMin");
//                parametri.put("ratingMin", filtri.get("ratingMin"));
//            }

            //costruisce la stringa JPQL unendo tutte le condizioni tranne quelle sui prezzi
            StringBuilder jpqlSenzaPrezzi = new StringBuilder("FROM EBustine b ");
            if (!condizioniBase.isEmpty()) {
                jpqlSenzaPrezzi.append("WHERE ").append(String.join(" AND ", condizioniBase)).append(" ");
            }


            //calcolo i prezzi massimi e minimi trovati dalla query jpql fatta senza prezzi per poter aggiornare lo slidere dei prezzi dinamicamente sulla ricerca fatta
            jakarta.persistence.TypedQuery<Object[]> queryMinMax = em.createQuery("SELECT MIN(" + prezzoReale + "), MAX(" + prezzoReale + ") " + jpqlSenzaPrezzi.toString(), Object[].class);
            parametri.forEach(queryMinMax::setParameter);
            //la query restituisce un array di 2 oggetti
            Object[] estremi = queryMinMax.getSingleResult();

            double rawMin = (estremi[0] != null) ? ((Number) estremi[0]).doubleValue() : 0.0;
            double rawMax = (estremi[1] != null) ? ((Number) estremi[1]).doubleValue() : 50.0;

            double prezzoMinimo = Math.round(rawMin * 100.0) / 100.0;
            double prezzoMassimo = Math.round(rawMax * 100.0) / 100.0;

            // clona la query iniziale aggiungendo i vincoli sul prezzo eventualemnte richiesti dall'utente
            List<String> condizioniPrezzo = new ArrayList<>(condizioniBase);

            if (filtri.get("prezzoMin") != null && ((Number) filtri.get("prezzoMin")).doubleValue() > 0) {
                condizioniPrezzo.add(prezzoReale + " >= :prezzoMin");
                parametri.put("prezzoMin", filtri.get("prezzoMin"));
            }

            if (filtri.get("prezzoMax") != null && ((Number) filtri.get("prezzoMax")).doubleValue() > 0) {
                condizioniPrezzo.add(prezzoReale + " <= :prezzoMax");
                parametri.put("prezzoMax", filtri.get("prezzoMax"));
            }

            // costruzione jpql finale per count e select
            StringBuilder jpqlFinale = new StringBuilder("FROM EBustine b ");
            if (!condizioniPrezzo.isEmpty()) {
                jpqlFinale.append("WHERE ").append(String.join(" AND ", condizioniPrezzo)).append(" ");
            }

            //esecuzione della count per la paginazione
            jakarta.persistence.TypedQuery<Long> queryCount = em.createQuery("SELECT COUNT(b) " + jpqlFinale.toString(), Long.class);
            parametri.forEach(queryCount::setParameter);
            Long totale = queryCount.getSingleResult();

            //costruzione query finale con select
            StringBuilder jpqlMain = new StringBuilder("SELECT b ").append(jpqlFinale.toString());

            //uso la query finale creata per inserire l'ordinamento richiesto dall'utente
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

            //eseguo query con ordinamento
            jakarta.persistence.TypedQuery<EBustine> queryMain = em.createQuery(jpqlMain.toString(), EBustine.class);
            parametri.forEach(queryMain::setParameter);

            //risultati per paginazione e "segnalibro" per prodotto successivo nelle varie pagine
            queryMain.setFirstResult(offset);
            queryMain.setMaxResults(limit);
            List<EBustine> risultati = queryMain.getResultList();

            //creo il dizionario con i vari risultati da passare al control
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