package it.univaq.tablecrown.dao;

import it.univaq.tablecrown.entity.EOrdine;
import it.univaq.tablecrown.entity.enumerativi.StatoOrdine;
import jakarta.persistence.EntityManager;

public class OrdineDAO extends GenericDAO {

    public OrdineDAO(EntityManager em) {
        super(em);
    }

    public int contaOrdiniTotali() {
        try {
            String jpql = "SELECT COUNT(o) FROM EOrdine o";
            Long totale = em.createQuery(jpql, Long.class).getSingleResult();
            return totale != null ? totale.intValue() : 0;
        } catch (Exception e) {
            System.err.println("Errore in contaOrdiniTotali: " + e.getMessage());
            return 0;
        }
    }

    public float contaVenditeTotali() {
        try {
            // Sfruttiamo la navigazione diretta degli oggetti di JPA: oi.ordine.stato
            String jpql = "SELECT SUM(oi.quantita * oi.prezzoUnitario * (1 - (oi.scontoApplicato / 100))) " +
                    "FROM EOrdineItem oi WHERE oi.ordine.stato = :stato";

            // Usiamo 'Number' perché il SUM di valori float su DB può essere restituito da JPA come Double
            Number risultato = em.createQuery(jpql, Number.class)
                    .setParameter("stato", StatoOrdine.CONSEGNATO)
                    .getSingleResult();

            return risultato != null ? risultato.floatValue() : 0.0f;
        } catch (Exception e) {
            System.err.println("Errore in contaVenditeTotali: " + e.getMessage());
            return 0.0f;
        }
    }
}