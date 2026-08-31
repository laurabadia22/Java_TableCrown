package it.univaq.tablecrown.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericDAO {

    // Contiene l'EntityManager passato dall'esterno
    private EntityManager em;

    public GenericDAO(EntityManager em) {
        this.em = em;
    }


    public boolean saveObj(Object obj) {
        try {
            em.getTransaction().begin();
            em.merge(obj);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            System.err.println("Errore salvataggio: " + e.getMessage());
            return false;
        }
    }


    //la <T> viene usata per indicare a java che ancora non sappiamo quale tipo andremo a restituire con il metodo
    //lo scopre poi con Class<T> e assegna a tutte le T presenti quell'esatto oggetto
    public <T> T getObjOnAttribute(Class<T> entityClass, String field, Object value) {
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + field + " = :val";
            return em.createQuery(jpql, entityClass)
                    .setParameter("val", value)
                    .setMaxResults(1)
                    .getSingleResult();
        //questo catch serve per pescare le eccezioni generate dalla mancanza dell'oggetto cercato
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            System.err.println("Errore recupero singolo oggetto: " + e.getMessage());
            return null;
        }
    }


    public <T> List<T> getObjListOnAttribute(Class<T> entityClass, String field, Object value) {
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + field + " = :val";
            return em.createQuery(jpql, entityClass)
                    .setParameter("val", value)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Errore recupero lista: " + e.getMessage());
            return List.of();
        }
    }


    public <T> List<T> getObjListBetween(Class<T> entityClass, String field, Object start, Object end) {
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + field + " BETWEEN :start AND :end";
            return em.createQuery(jpql, entityClass)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Errore recupero lista between: " + e.getMessage());
            return List.of();
        }
    }


    public <T> List<T> getObjListOrdered(Class<T> entityClass, String field, String ordinationType, int quantity) {
        try {
            // ordinationType deve essere "ASC" o "DESC"
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e ORDER BY e." + field + " " + ordinationType;
            return em.createQuery(jpql, entityClass)
                    .setMaxResults(quantity)
                    .getResultList();
        } catch (Exception e) {
            System.err.println("Errore recupero lista ordinata: " + e.getMessage());
            return List.of();
        }
    }


    public boolean verificaEsistenza(Class<?> entityClass, String field, Object value) {
        try {
            String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e WHERE e." + field + " = :val";
            //la risposta alla query non è un oggetto, ma un tipo long (intero)
            Long conteggio = em.createQuery(jpql, Long.class)
                    .setParameter("val", value)
                    .getSingleResult();
            return conteggio > 0;
        } catch (Exception e) {
            System.err.println("Errore verifica esistenza: " + e.getMessage());
            return false;
        }
    }


    public <T> Map<String, Object> getRicerca(Class<T> entityClass, String str, String field, int limit, int offset) {
        Map<String, Object> response = new HashMap<>();
        try {
            //Esegue il conteggio totale
            String countJpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e WHERE e." + field + " LIKE :ricerca";
            Long totale = em.createQuery(countJpql, Long.class)
                    .setParameter("ricerca", "%" + str + "%")
                    .getSingleResult();

            //Esegue la ricerca paginata
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + field + " LIKE :ricerca";
            List<T> risultati = em.createQuery(jpql, entityClass)
                    .setParameter("ricerca", "%" + str + "%")
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();

            response.put("risultati", risultati);
            response.put("totale", totale);
            return response;
        } catch (Exception e) {
            System.err.println("Errore ricerca: " + e.getMessage());
            response.put("risultati", List.of());
            response.put("totale", 0L);
            return response;
        }
    }


    public boolean deleteObj(Object obj) {
        try {
            em.getTransaction().begin();
            // JPA richiede che l'oggetto sia attaccato ad un em prima di eliminarlo
            em.remove(em.contains(obj) ? obj : em.merge(obj));
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            System.err.println("Errore cancellazione: " + e.getMessage());
            return false;
        }
    }


    public <T> List<T> getAll(Class<T> entityClass) {
        try {
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            return em.createQuery(jpql, entityClass).getResultList();
        } catch (Exception e) {
            System.err.println("Errore getAll: " + e.getMessage());
            return List.of();
        }
    }
}