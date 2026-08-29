package it.univaq.tablecrown.entity;

import it.univaq.tablecrown.entity.enumerativi.DisponibilitaProdotto;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prodotto")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
public abstract class EProdotto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prodotto")
    private Long idProdotto;

    @Column(name = "nome_prodotto", nullable = false, length = 255)
    private String nomeProdotto;

    @Lob
    @Column(name = "img_prodotto")
    private byte[] imgProdotto;

    @Column(name = "descrizione_prodotto", columnDefinition = "TEXT", nullable = false)
    private String descrizioneProdotto;

    @Enumerated(EnumType.STRING)
    @Column(name = "disponibilita_prodotto", nullable = false)
    private DisponibilitaProdotto disponibilitaProdotto;

    @Column(name = "quantita", nullable = false)
    private int quantita;

    @Column(name = "data_pubblicazione", nullable = false)
    private LocalDateTime dataPubblicazione;

    @Column(name = "prezzo", nullable = false)
    private float prezzo;

    @Embedded
    private ESconto sconto = new ESconto();

    @Column(name = "numero_vendite", nullable = false)
    protected int numeroVendite = 0;

    @Column(name = "valutazione_media", nullable = false)
    protected float valutazioneMedia = 0.0f;

    @OneToMany(mappedBy = "prodotto", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<ERecensione> recensioni = new ArrayList<>();

    private static final int GIORNI_NOVITA = 30;

    //Costruttore vuoto richiesto da Hibernate
    protected  EProdotto() {
    }

    //Costruttore con logica di dominio
    protected EProdotto(String nomeProdotto, String descrizioneProdotto, DisponibilitaProdotto disponibilitaProdotto, int quantita, byte[] imgProdotto, float prezzo) {
        this.rinominaProdotto(nomeProdotto);
        this.aggiornaDescrizione(descrizioneProdotto);
        this.aggiornaImg(imgProdotto);
        this.disponibilitaProdotto = disponibilitaProdotto;
        this.aggiornaQuantita(quantita);
        this.assegnaPrezzo(prezzo);
        this.dataPubblicazione = LocalDateTime.now();
        this.recensioni = new ArrayList<>();
        this.numeroVendite = 0;
        this.valutazioneMedia = 0.0f;
    }

    // --- GETTER ---
    public Long getIdProdotto() {
        return idProdotto;
    }

    public String getNomeProdotto() {
        return nomeProdotto;
    }

    public byte[] getImgProdotto() {
        return imgProdotto;
    }

    public String getDescrizioneProdotto() {
        return descrizioneProdotto;
    }

    public DisponibilitaProdotto getDisponibilitaProdotto() {
        return disponibilitaProdotto;
    }

    public int getQuantita() {
        return quantita;
    }

    public LocalDateTime getDataPubblicazione() {
        return dataPubblicazione;
    }

    public float getPrezzo() {
        return prezzo;
    }

    public ESconto getSconto() {
        return sconto;
    }

    public float getPrezzoScontato() {
        return sconto.applicaA(prezzo);
    }

    public List<ERecensione> getRecensioni() {
        return recensioni;
    }

    public int getNumeroVendite() {
        return numeroVendite;
    }

    // --- METODI DI DOMINIO ---

    public void rinominaProdotto(String nomeProdotto) {
        if (nomeProdotto == null || nomeProdotto.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del prodotto non può essere vuoto.");
        }
        this.nomeProdotto = nomeProdotto;
    }

    public void aggiornaDescrizione(String descrizioneProdotto) {
        if (descrizioneProdotto == null || descrizioneProdotto.trim().isEmpty()) {
            throw new IllegalArgumentException("La descrizione del prodotto non può essere vuota.");
        }
        this.descrizioneProdotto = descrizioneProdotto;
    }

    public void aggiornaImg(byte[] imgProdotto) {
        this.imgProdotto = imgProdotto;
    }

    public void aggiornaQuantita(int quantita) {
        if (quantita < 0) {
            throw new IllegalArgumentException("La quantità del prodotto non può essere negativa.");
        }
        this.quantita = quantita;
        if (this.quantita == 0) {
            this.disponibilitaProdotto = DisponibilitaProdotto.ESAURITO;
        } else if (this.disponibilitaProdotto == DisponibilitaProdotto.ESAURITO) {
            this.disponibilitaProdotto = DisponibilitaProdotto.DISPONIBILE;
        }
    }

    public void assegnaPrezzo(float prezzo) {
        if (prezzo < 0) {
            throw new IllegalArgumentException("Il prezzo non può essere negativo.");
        }
        this.prezzo = prezzo;
    }

    public void rendiDisponibile() {
        if (this.quantita == 0) {
            throw new IllegalArgumentException("Non è possibile rendere disponibile un prodotto con quantità 0.");
        }
        this.disponibilitaProdotto = DisponibilitaProdotto.DISPONIBILE;
    }

    public void rimuoviProdotto() {
        this.disponibilitaProdotto = DisponibilitaProdotto.NON_DISPONIBILE;
    }

    public boolean isDisponibile() {
        return this.disponibilitaProdotto == DisponibilitaProdotto.DISPONIBILE
                && this.quantita > 0;
    }

    public boolean isAcquistabile() {
        return isDisponibile();
    }

    public void addRecensione(ERecensione recensione) {
        if (!this.recensioni.contains(recensione)) {
            this.recensioni.add(recensione);
            this.valutazioneMedia = calcolaValutazioneMedia();
        }
    }

    public void removeRecensione(ERecensione recensione) {
        if (this.recensioni.remove(recensione)) {
            this.valutazioneMedia = calcolaValutazioneMedia();
        }
    }

    public float getValutazioneMedia() {
        if (this.recensioni == null || this.recensioni.isEmpty()) {
            return 0.0f;
        }
        float somma = 0.0f;
        for (ERecensione recensione : this.recensioni) {
            somma += recensione.getValutazione();
        }
        return somma / this.recensioni.size();
    }

    public void addVendite(int quantitaAcquistata) {
        if (quantitaAcquistata <= 0) {
            throw new IllegalArgumentException("La quantità di vendite da aggiungere deve essere maggiore di zero.");
        }
        this.numeroVendite += quantitaAcquistata;
    }

    public boolean isNovita() {
        if (this.dataPubblicazione == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(this.dataPubblicazione.plusDays(GIORNI_NOVITA));
    }
}
