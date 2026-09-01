package it.univaq.tablecrown.entity;

import it.univaq.tablecrown.entity.enumerativi.*;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "gioco_da_tavolo")
@DiscriminatorValue("gioco")
public class EGiocoDaTavolo extends EProdotto{
    @ElementCollection
    @CollectionTable(name = "gioco_categoria", joinColumns = @JoinColumn(name = "id_prodotto"))
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria")
    private Set<Categoria> categoria = new LinkedHashSet<>(); //Set evita duplicati; LinkedHashSet mantiene l'ordine di inserimento

    @ElementCollection
    @CollectionTable(name = "gioco_componente", joinColumns = @JoinColumn(name = "id_prodotto"))
    @Column(name = "componente")
    private Set<String> componenti = new LinkedHashSet<>();

    @ManyToOne
    @JoinColumn(name = "gioco_base_id", referencedColumnName = "id_prodotto", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EGiocoDaTavolo giocoBase;

    @Column(name = "numero_giocatori_min", nullable = false)
    private int numeroGiocatoriMin;

    @Column(name = "numero_giocatori_max", nullable = false)
    private int numeroGiocatoriMax;

    @Column(name = "eta_minima", nullable = false)
    private int etaMinima;

    @Column(name = "durata_media", nullable = false)
    private int durataMedia;

    @Enumerated(EnumType.STRING)
    @Column(name = "livello_danno")
    private LivelloDannoGiochi livelloDanno;

    @Column(name = "descrizione_danno", columnDefinition = "TEXT")
    private String descrizioneDanno;

    @Enumerated(EnumType.STRING)
    @Column(name = "lingua", nullable = false)
    private LinguaGioco lingua;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficolta", nullable = false)
    private DifficoltaGioco difficolta;


    protected EGiocoDaTavolo() {
        super();
    }

    public EGiocoDaTavolo(String nomeProdotto, String descrizioneProdotto, DisponibilitaProdotto disponibilitaProdotto, int quantita, float prezzo, Set<Categoria> categoria, Set<String> componenti, DifficoltaGioco difficolta, LinguaGioco lingua, String imgProdotto, EGiocoDaTavolo giocoBase, int numeroGiocatoriMin, int numeroGiocatoriMax, int etaMinima, int durataMedia, LivelloDannoGiochi livelloDanno, String descrizioneDanno) {
        super(nomeProdotto, descrizioneProdotto, disponibilitaProdotto, quantita, imgProdotto, prezzo);
        this.categoria = new LinkedHashSet<>(categoria);
        this.verificaCategoria();
        this.componenti = new LinkedHashSet<>(componenti);
        this.verificaComponenti();
        this.difficolta = difficolta;
        this.lingua = lingua;
        this.giocoBase = giocoBase;
        this.verificaVincoliEspansione();
        this.numeroGiocatoriMin = numeroGiocatoriMin;
        this.numeroGiocatoriMax = numeroGiocatoriMax;
        this.verificaNumGiocatori();
        this.etaMinima = etaMinima;
        this.verificaEtaMinima();
        this.durataMedia = durataMedia;
        this.verificaDurataMedia();

        if (livelloDanno != null) {
            this.aggiungiDanno(livelloDanno, descrizioneDanno);
        } else if (descrizioneDanno != null) {
            throw new IllegalArgumentException("Non può essere presente una descrizione del danno se non è presente un danno.");
        }
    }

    // --- GETTER ---
    public Set<Categoria> getCategoria() {
        return categoria;
    }

    public Set<String> getComponenti() {
        return componenti;
    }

    public EGiocoDaTavolo getGiocoBase() {
        return giocoBase;
    }

    public int getNumeroGiocatoriMin() {
        return numeroGiocatoriMin;
    }

    public int getNumeroGiocatoriMax() {
        return numeroGiocatoriMax;
    }

    public int getEtaMinima() {
        return etaMinima;
    }

    public int getDurataMedia() {
        return durataMedia;
    }

    public LinguaGioco getLingua() {
        return lingua;
    }

    public DifficoltaGioco getDifficolta() {
        return difficolta;
    }

    public LivelloDannoGiochi getLivelloDanno() {
        return livelloDanno;
    }

    public String getDescrizioneDanno() {
        return descrizioneDanno;
    }

    // --- METODI DI DOMINIO ---

    public void impostaLingua(LinguaGioco lingua) {
        this.lingua = lingua;
    }

    public void impostaDifficolta(DifficoltaGioco difficolta) {
        this.difficolta = difficolta;
    }

    public void aggiungiCategoria(Categoria categoria) {
        this.categoria.add(categoria);
    }

    public void rimuoviCategoria(Categoria categoria) {
        this.categoria.remove(categoria);
    }

    public void aggiungiComponente(String componente) {
        String trimmed = componente.trim();
        if (!trimmed.isEmpty()) {
            this.componenti.add(trimmed);
        }
    }

    public void rimuoviComponente(String componente) {
        this.componenti.remove(componente.trim());
    }

    public void aggiungiDanno(LivelloDannoGiochi livelloDanno, String descrizioneDanno) {
        if (descrizioneDanno == null || descrizioneDanno.trim().isEmpty()) {
            throw new IllegalArgumentException("La descrizione del danno non può essere vuota.");
        }
        this.livelloDanno = livelloDanno;
        this.descrizioneDanno = descrizioneDanno.trim();
        this.getSconto().aggiornaSconto(livelloDanno.getScontoPercentuale(), null);
    }

    private void verificaVincoliEspansione() {
        if (this.giocoBase != null) {
            if (this.giocoBase.getGiocoBase() != null) {
                throw new IllegalArgumentException("Il gioco base non può essere a sua volta un'espansione.");
            }
            if (this.giocoBase.getIdProdotto() != null
                    && this.giocoBase.getIdProdotto().equals(this.getIdProdotto())) {
                throw new IllegalArgumentException("Un gioco da tavolo non può essere un'espansione di se stesso.");
            }
        }
    }

    private void verificaNumGiocatori() {
        if (this.numeroGiocatoriMin <= 0) {
            throw new IllegalArgumentException("Il numero minimo di giocatori deve essere maggiore di 0.");
        }
        if (this.numeroGiocatoriMin > this.numeroGiocatoriMax) {
            throw new IllegalArgumentException("Il numero minimo di giocatori non può essere maggiore del numero massimo di giocatori.");
        }
    }

    private void verificaEtaMinima() {
        if (this.etaMinima <= 0) {
            throw new IllegalArgumentException("L'età minima deve essere maggiore di 0.");
        }
    }

    private void verificaDurataMedia() {
        if (this.durataMedia <= 0) {
            throw new IllegalArgumentException("La durata media deve essere maggiore di 0.");
        }
    }

    private void verificaCategoria() {
        if (this.categoria.isEmpty()) {
            throw new IllegalArgumentException("Il gioco da tavolo deve appartenere ad almeno una categoria.");
        }
    }

    private void verificaComponenti() {
        if (this.componenti.isEmpty()) {
            throw new IllegalArgumentException("Il gioco da tavolo deve avere almeno un componente.");
        }
    }
}
