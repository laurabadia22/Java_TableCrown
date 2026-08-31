package it.univaq.tablecrown.dao;

import it.univaq.tablecrown.entity.EProdotto;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;

public class PersistentManager {

    private EntityManager em;

    // Il costruttore riceve l'EntityManager dal FrontController
    public PersistentManager(EntityManager em) {
        this.em = em;
    }

    // =========================================================================
    // 1. METODI GENERICI (Delegati al GenericDAO)
    // =========================================================================

    public boolean PMsaveObj(Object obj) {
        GenericDAO dao = new GenericDAO(em);
        return dao.saveObj(obj);
    }

    public boolean PMdeleteObj(Object obj) {
        GenericDAO dao = new GenericDAO(em);
        return dao.deleteObj(obj);
    }

    public <T> T PMgetObjOnAttribute(Class<T> entityClass, String field, Object value) {
        GenericDAO dao = new GenericDAO(em);
        return dao.getObjOnAttribute(entityClass, field, value);
    }

    public <T> List<T> PMgetObjListOnAttribute(Class<T> entityClass, String field, Object value) {
        GenericDAO dao = new GenericDAO(em);
        return dao.getObjListOnAttribute(entityClass, field, value);
    }

    public <T> List<T> PMgetAll(Class<T> entityClass) {
        GenericDAO dao = new GenericDAO(em);
        return dao.getAll(entityClass);
    }

    public boolean PMverificaEsistenza(Class<?> entityClass, String field, Object value) {
        GenericDAO dao = new GenericDAO(em);
        return dao.verificaEsistenza(entityClass, field, value);
    }

    public <T> Map<String, Object> PMRicerca(Class<T> entityClass, String str, String field, int limit, int offset) {
        GenericDAO dao = new GenericDAO(em);
        return dao.getRicerca(entityClass, str, field, limit, offset);
    }

    // =========================================================================
    // 2. METODI SPECIFICI (Delegati ai DAO specifici come ProdottoDAO)
    // =========================================================================

    public Map<String, Object> PMfindProdottiInOfferta(int limit, int offset) {
        ProdottoDAO dao = new ProdottoDAO(em);
        return dao.findProdottiInOfferta(limit, offset);
    }

    public boolean utenteHasProdotto(int idUtente, int idProdotto) {
        ProdottoDAO dao = new ProdottoDAO(em);
        return dao.utenteHasProdotto(idUtente, idProdotto);
    }

    public List<EProdotto> findCorrelati (List<Long> prodottiEsclusi, int limit){
        ProdottoDAO dao = new ProdottoDAO(em);
        return dao.findCorrelati(prodottiEsclusi, limit);
    }



    // Man mano che creerai UtenteDAO, OrdineDAO, ecc.,
    // aggiungerai qui i loro metodi specifici!
}