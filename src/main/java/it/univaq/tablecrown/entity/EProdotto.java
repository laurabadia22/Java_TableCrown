package it.univaq.tablecrown.entity;

import it.univaq.tablecrown.entity.enumerativi.DisponibilitaProdotto;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prodotto")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
public abstract class EProdotto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProdotto")
    private Long idProdotto;

    @Column(name = "nomeProdotto", nullable = false, length = 255)
    private String nomeProdotto;

    @Column(name = "img_prodotto", length = 255)
    private String imgProdotto;

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

    @Embedded
    private EValutazione valutazione = new EValutazione();

    private static final int GIORNI_NOVITA = 30;

    //Costruttore vuoto richiesto da Hibernate
    protected  EProdotto() {
    }

    //Costruttore con logica di dominio
    protected EProdotto(String nomeProdotto, String descrizioneProdotto, DisponibilitaProdotto disponibilitaProdotto, int quantita, String imgProdotto, float prezzo) {
        this.rinominaProdotto(nomeProdotto);
        this.aggiornaDescrizione(descrizioneProdotto);
        this.aggiornaImg(imgProdotto);
        this.disponibilitaProdotto = disponibilitaProdotto;
        this.aggiornaQuantita(quantita);
        this.assegnaPrezzo(prezzo);
        this.dataPubblicazione = LocalDateTime.now();
        this.numeroVendite = 0;
    }

    // --- GETTER ---
    public Long getIdProdotto() {
        return idProdotto;
    }

    public String getNomeProdotto() {
        return nomeProdotto;
    }

    public String getImgProdotto() {
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

    public EValutazione getValutazione() {
        return valutazione;
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

    public void aggiornaImg(String imgProdotto) {
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

    public void aggiungiValutazione(int stelle){
        this.valutazione.aggiungiValutazione(stelle);
    }

    public float getValutazioneMedia() {
        return this.valutazione.getMedia();
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
