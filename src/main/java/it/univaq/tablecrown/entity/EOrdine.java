package it.univaq.tablecrown.entity;

import it.univaq.tablecrown.entity.enumerativi.StatoOrdine;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordine")
public class EOrdine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOrdine")
    private Long idOrdine;

    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    @Enumerated(EnumType.STRING)
    @Column(name = "stato", nullable = false)
    private StatoOrdine stato;

    @ManyToOne
    @JoinColumn(name = "utente_id", referencedColumnName = "idPersona", nullable = false)
    private EUtente utente;

    @ManyToOne
    @JoinColumn(name = "indirizzo_id", referencedColumnName = "id_indirizzo", nullable = false)
    private EIndirizzo indirizzoSpedizione;

    // Snapshot carta di credito: non salviamo il riferimento alla carta ma solo
    // i dati necessari, così se la carta viene eliminata o scade, lo storico
    // degli ordini rimane intatto.
    @Column(name = "ultimeQuattroCifreCarta", nullable = false, length = 4)
    private String ultimeQuattroCifreCarta;

    @Column(name = "nomeTitolareCarta", nullable = false, length = 100)
    private String nomeTitolareCarta;

    @OneToMany(mappedBy = "ordine", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<EOrdineItem> ordineItems = new ArrayList<>();

    //Costruttore vuote per Hibernate
    protected EOrdine() {
    }

    //Costruttore di dominio
    public EOrdine(EUtente utente, EIndirizzo indirizzo, ECartaDiCredito carta) {
        this.validaCartaUtente(carta, utente);
        this.data = LocalDateTime.now();
        this.stato = StatoOrdine.IN_LAVORAZIONE; // nasce sempre in lavorazione
        this.utente = utente;
        this.indirizzoSpedizione = indirizzo;
        // snapshot della carta — non il riferimento
        this.ultimeQuattroCifreCarta = carta.getNumero();
        this.nomeTitolareCarta = carta.getNomeTitolare();
        utente.riceviOrdine(this); // coerenza bidirezionale
    }

    // --- GETTER ---

    public Long getIdOrdine() {
        return idOrdine;
    }

    public LocalDateTime getData() {
        return data;
    }

    public StatoOrdine getStato() {
        return stato;
    }

    public EUtente getUtente() {
        return utente;
    }

    public EIndirizzo getIndirizzoSpedizione() {
        return indirizzoSpedizione;
    }

    public String getUltimeQuattroCifreCarta() {
        return ultimeQuattroCifreCarta;
    }

    public String getNomeTitolareCarta() {
        return nomeTitolareCarta;
    }

    public List<EOrdineItem> getOrdineItems() {
        return ordineItems;
    }

    // --- METODI DI DOMINIO ---

    public void spedisciOrdine() {
        if (this.stato != StatoOrdine.IN_LAVORAZIONE) {
            throw new IllegalStateException("Solo un ordine in lavorazione può essere spedito.");
        }
        this.stato = StatoOrdine.SPEDITO;
    }

    public void consegnaOrdine() {
        if (this.stato != StatoOrdine.SPEDITO) {
            throw new IllegalStateException("Solo un ordine già spedito può essere consegnato.");
        }
        this.stato = StatoOrdine.CONSEGNATO;
    }

    public void annulla() {
        if (this.stato == StatoOrdine.CONSEGNATO) {
            throw new IllegalStateException("Un ordine già consegnato non può essere annullato.");
        }
        if (this.stato == StatoOrdine.ANNULLATO) {
            throw new IllegalStateException("L'ordine è già annullato.");
        }
        this.stato = StatoOrdine.ANNULLATO;
    }

    // Aggiunge un prodotto all'ordine — crea automaticamente l'item
    public void aggiungiProdotto(EProdotto prodotto, int quantita) {
        if (this.stato != StatoOrdine.IN_LAVORAZIONE) {
            throw new IllegalStateException("Non puoi modificare un ordine che non è in lavorazione.");
        }
        for (EOrdineItem item : this.ordineItems) {
            if (item.getProdotto() == prodotto) {
                item.impostaQuantita(item.getQuantita() + quantita);
                return;
            }
        }
        // EOrdineItem chiama ordine.addOrdineItem(this) nel proprio costruttore
        new EOrdineItem(quantita, this, prodotto);
    }

    public void rimuoviProdotto(EProdotto prodotto) {
        if (this.stato != StatoOrdine.IN_LAVORAZIONE) {
            throw new IllegalStateException("Non puoi modificare un ordine che non è in lavorazione.");
        }
        for (EOrdineItem item : this.ordineItems) {
            if (item.getProdotto() == prodotto) {
                this.ordineItems.remove(item);
                return;
            }
        }
        throw new IllegalStateException("Il prodotto non è presente nell'ordine.");
    }

    // Chiamato da EOrdineItem nel costruttore per coerenza bidirezionale
    public void addOrdineItem(EOrdineItem item) {
        if (!this.ordineItems.contains(item)) {
            this.ordineItems.add(item);
        }
    }

    public float calcolaTotale() {
        float totale = 0.0f;
        for (EOrdineItem item : this.ordineItems) {
            totale += item.calcolaTotaleItem();
        }
        return totale;
    }

    // --- METODI PRIVATI ---

    private void validaCartaUtente(ECartaDiCredito carta, EUtente utente) {
        if (carta.getUtente() != utente) {
            throw new IllegalArgumentException("La carta non appartiene all'utente che sta effettuando l'ordine.");
        }
    }
}
