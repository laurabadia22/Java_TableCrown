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

    // =========================================================================
    // 3. METODI SPECIFICI: PORTA DADI
    // =========================================================================

    public Map<String, Object> PMfindPortaDadi(Map<String, Object> filtri, int limit, int offset) {
        PortaDadiDAO dao = new PortaDadiDAO(em);
        return dao.findPortaDadi(filtri, limit, offset);
    }

    public Map<String, Object> PMricercaPortaDadi(String stringaDiRicerca, int limit, int offset) {
        PortaDadiDAO dao = new PortaDadiDAO(em);
        return dao.ricercaPortaDadi(stringaDiRicerca, limit, offset);
    }

    // =========================================================================
    // 4. METODI SPECIFICI: BUSTINE
    // =========================================================================

    public Map<String, Object> PMfindBustine(Map<String, Object> filtri, int limit, int offset) {
        BustineDAO dao = new BustineDAO(em);
        return dao.findBustine(filtri, limit, offset);
    }

    public Map<String, Object> PMricercaBustine(String stringaDiRicerca, int limit, int offset) {
        BustineDAO dao = new BustineDAO(em);
        return dao.ricercaBustine(stringaDiRicerca, limit, offset);
    }

    // =========================================================================
    // 5. METODI SPECIFICI: GIOCHI DA TAVOLO
    // =========================================================================

    public Map<String, Object> PMfindGiochi(Map<String, Object> filtri, int limit, int offset) {
        GiocoDaTavoloDAO dao = new GiocoDaTavoloDAO(em);
        return dao.findGiochi(filtri, limit, offset);
    }

    public Map<String, Object> PMricercaGiochi(String stringaDiRicerca, int limit, int offset) {
        GiocoDaTavoloDAO dao = new GiocoDaTavoloDAO(em);
        return dao.ricercaGiochi(stringaDiRicerca, limit, offset);
    }

    // =========================================================================
    // 6. METODI SPECIFICI: ORDINI
    // =========================================================================

    public int PMcontaOrdiniTotali() {
        OrdineDAO dao = new OrdineDAO(em);
        return dao.contaOrdiniTotali();
    }

    public float PMcontaVenditeTotali() {
        OrdineDAO dao = new OrdineDAO(em);
        return dao.contaVenditeTotali();
    }
}