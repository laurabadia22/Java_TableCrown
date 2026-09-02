package it.univaq.tablecrown.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ordine_item")
public class EOrdineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ordine_item")
    private Long idOrdineItem;

    @Column(name = "quantita", nullable = false)
    private int quantita;

    // Snapshot del prezzo/sconto al momento dell'acquisto: se il prezzo del
    // prodotto cambia in futuro, lo storico dell'ordine rimane corretto.
    @Column(name = "prezzo_unitario", nullable = false)
    private float prezzoUnitario;

    @Column(name = "sconto_applicato", nullable = false)
    private float scontoApplicato;

    @ManyToOne
    @JoinColumn(name = "ordine_id", referencedColumnName = "id_ordine", nullable = false)
    private EOrdine ordine;

    @ManyToOne
    @JoinColumn(name = "prodotto_id", referencedColumnName = "idProdotto", nullable = false)
    private EProdotto prodotto;

    //Costruttore vuoto per Hibernate
    protected EOrdineItem() {
    }

    //Costruttore di dominio
    public EOrdineItem(int quantita, EOrdine ordine, EProdotto prodotto) {
        this.impostaQuantita(quantita);
        this.ordine = ordine;
        this.prodotto = prodotto;
        // Snapshot del prezzo/sconto attuali del prodotto.
        this.prezzoUnitario = prodotto.getPrezzo();
        this.scontoApplicato = prodotto.getSconto().getSconto();
        ordine.addOrdineItem(this); // coerenza bidirezionale con EOrdine
    }

    // --- GETTER ---

    public Long getIdOrdineItem() {
        return idOrdineItem;
    }

    public int getQuantita() {
        return quantita;
    }

    public EOrdine getOrdine() {
        return ordine;
    }

    public EProdotto getProdotto() {
        return prodotto;
    }

    public float getPrezzoUnitario() {
        return prezzoUnitario;
    }

    public float getScontoApplicato() {
        return scontoApplicato;
    }

    // --- METODI DI DOMINIO ---

    public void impostaQuantita(int quantita) {
        if (quantita <= 0) {
            throw new IllegalArgumentException("La quantità deve essere maggiore di 0.");
        }
        this.quantita = quantita;
    }

    public float calcolaTotaleItem() {
        return this.prezzoUnitario * this.quantita * (1 - this.scontoApplicato / 100);
    }
}
