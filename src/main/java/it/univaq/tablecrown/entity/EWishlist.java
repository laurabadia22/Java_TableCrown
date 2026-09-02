package it.univaq.tablecrown.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "wishlist")
public class EWishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_wishlist")
    private Long idWishlist;

    @Column(name = "data_creazione", nullable = false)
    private LocalDateTime dataCreazione;

    @OneToOne
    @JoinColumn(name = "utente_id", referencedColumnName = "idPersona", nullable = false)
    private EUtente utente;

    @ManyToMany
    @JoinTable(
            name = "wishlist_prodotto",
            joinColumns = @JoinColumn(name = "wishlist_id", referencedColumnName = "id_wishlist"),
            inverseJoinColumns = @JoinColumn(name = "prodotto_id", referencedColumnName = "idProdotto")
    )
    private Set<EProdotto> prodotti = new LinkedHashSet<>();

    // Costruttore vuoto richiesto da Hibernate
    protected EWishlist() {
    }

    //Costruttore di dominio
    public EWishlist(EUtente utente) {
        this.dataCreazione = LocalDateTime.now();
        this.utente = utente;
    }

    // --- GETTER ---
    public Long getIdWishlist() {
        return idWishlist;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    public EUtente getUtente() {
        return utente;
    }

    public Set<EProdotto> getProdotti() {
        return prodotti;
    }

    // --- METODI DI DOMINIO ---

    public void addProdotto(EProdotto prodotto) {
        this.prodotti.add(prodotto);
    }

    public void removeProdotto(EProdotto prodotto) {
        this.prodotti.remove(prodotto);
    }
}
