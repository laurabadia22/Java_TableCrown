package it.univaq.tablecrown.entity;

import jakarta.persistence.*;

import java.util.regex.Pattern;

@Entity
@Table(name = "indirizzo")
public class EIndirizzo {

    private static final Pattern CAP_PATTERN = Pattern.compile("^\\d{5}$"); //TODO: da ricontrollare

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_indirizzo")
    private Long idIndirizzo;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "via", nullable = false, length = 255)
    private String via;

    @Column(name = "citta", nullable = false, length = 100)
    private String citta;

    @Column(name = "cap", nullable = false, length = 5)
    private String cap;

    @Column(name = "provincia", nullable = false, length = 100)
    private String provincia;

    @Column(name = "nazione", nullable = false, length = 100)
    private String nazione;

    @Column(name = "nomeCitofono", nullable = false, length = 100)
    private String nomeCitofono;

    @ManyToOne
    @JoinColumn(name = "utente_id", referencedColumnName = "idPersona", nullable = false)
    private EUtente utente;

    @Column(name = "predefinito", nullable = false)
    private boolean predefinito;

    //Costruttore vuoto per Hibernate
    protected EIndirizzo() {
    }

    //Costruttore di dominio
    public EIndirizzo(String nome, String via, String citta, String cap, String provincia, String nazione, String nomeCitofono, EUtente utente, boolean predefinito) {
        this.impostaNome(nome);
        this.impostaVia(via);
        this.impostaCitta(citta);
        this.impostaCap(cap);
        this.impostaProvincia(provincia);
        this.impostaNazione(nazione);
        this.impostaNomeCitofono(nomeCitofono);
        this.utente = utente;
        this.predefinito = predefinito;
        utente.riceviIndirizzo(this); // sincronizza la relazione bidirezionale
    }

    // --- GETTER ---
    public Long getIdIndirizzo() {
        return idIndirizzo;
    }

    public String getNome() {
        return nome;
    }

    public String getVia() {
        return via;
    }

    public String getCitta() {
        return citta;
    }

    public String getCap() {
        return cap;
    }

    public String getProvincia() {
        return provincia;
    }

    public String getNazione() {
        return nazione;
    }

    public String getNomeCitofono() {
        return nomeCitofono;
    }

    public EUtente getUtente() {
        return utente;
    }

    public boolean isPredefinito() {
        return predefinito;
    }

    // --- METODI DI DOMINIO ---

    public void impostaNome(String nome) {
        String trimmed = nome == null ? "" : nome.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Il nome dell'indirizzo non può essere vuoto.");
        }
        this.nome = trimmed;
    }

    public void impostaVia(String via) {
        String trimmed = via == null ? "" : via.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("La via non può essere vuota.");
        }
        this.via = trimmed;
    }

    public void impostaCitta(String citta) {
        String trimmed = citta == null ? "" : citta.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("La città non può essere vuota.");
        }
        this.citta = trimmed;
    }

    public void impostaCap(String cap) {
        String trimmed = cap == null ? "" : cap.trim();
        if (!CAP_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("CAP non valido. Deve essere composto da 5 cifre.");
        }
        this.cap = trimmed;
    }

    public void impostaProvincia(String provincia) {
        String trimmed = provincia == null ? "" : provincia.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("La provincia non può essere vuota.");
        }
        this.provincia = trimmed;
    }

    public void impostaNazione(String nazione) {
        String trimmed = nazione == null ? "" : nazione.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("La nazione non può essere vuota.");
        }
        this.nazione = trimmed;
    }

    public void impostaNomeCitofono(String nomeCitofono) {
        String trimmed = nomeCitofono == null ? "" : nomeCitofono.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Il nome del citofono non può essere vuoto.");
        }
        this.nomeCitofono = trimmed;
    }

    // L'unicità dell'indirizzo predefinito viene verificata lato Control.
    public void impostaPredefinito() {
        this.predefinito = true;
    }

    public void rimuoviPredefinito() {
        this.predefinito = false;
    }
}
