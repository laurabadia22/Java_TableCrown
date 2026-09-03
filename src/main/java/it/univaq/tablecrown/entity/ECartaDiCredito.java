package it.univaq.tablecrown.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Entity
@Table(name = "carta_di_credito")
public class ECartaDiCredito {

    private static final Pattern SCADENZA_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/\\d{2}$"); //TODO: da ricontrollare

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCartaDiCredito")
    private Long idCartaDiCredito;

    @Column(name = "titolare", nullable = false, length = 100)
    private String titolare;

    @Column(name = "dataScadenza", nullable = false)
    private YearMonth dataScadenza;

    @Column(name = "ultimeQuattroCifre", nullable = false, length = 4)
    private String ultimeQuattroCifre;

    @Column(name = "token", nullable = false, length = 255, unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "utente_id", referencedColumnName = "idPersona", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private EUtente utente;

    //Costruttore vuote per Hibernate
    protected ECartaDiCredito() {
    }

    //Costruttore di dominio
    public ECartaDiCredito(EUtente utente, String titolare, String dataScadenzaStringa,
                           String ultimeQuattroCifre, String token) {
        this.utente = utente;
        this.impostaNomeTitolare(titolare);
        this.traduciStringaData(dataScadenzaStringa);
        this.impostaUltimeQuattroCifre(ultimeQuattroCifre);
        this.token = token;
    }

    // --- GETTER ---

    public String getScadenzaFormattata() {
        return this.dataScadenza.format(DateTimeFormatter.ofPattern("MM/yy"));
    }

    public String getNumeroMascherato() {
        return "**** **** **** " + this.ultimeQuattroCifre;
    }

    public EUtente getUtente() {
        return utente;
    }

    public Long getIdCartaDiCredito() {
        return idCartaDiCredito;
    }

    public String getNomeTitolare() {
        return titolare;
    }

    public YearMonth getDataScadenza() {
        return dataScadenza;
    }

    public String getNumero() {
        return ultimeQuattroCifre;
    }

    public String getToken() {
        return token;
    }

    // --- METODI DI DOMINIO ---

    public void impostaNomeTitolare(String titolare) {
        String trimmed = titolare == null ? "" : titolare.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Il nome del titolare non può essere vuoto.");
        }
        this.titolare = trimmed.toUpperCase(); // sempre in maiuscolo, come nell'originale
    }

    public void impostaUltimeQuattroCifre(String ultimeQuattroCifre) {
        boolean valido = ultimeQuattroCifre != null
                && ultimeQuattroCifre.length() == 4
                && ultimeQuattroCifre.chars().allMatch(Character::isDigit);
        if (!valido) {
            throw new IllegalArgumentException("Le ultime cifre devono essere esattamente 4 numeri.");
        }
        this.ultimeQuattroCifre = ultimeQuattroCifre;
    }

    // Trasforma la stringa "12/26" in uno YearMonth (dicembre 2026)
    public void traduciStringaData(String scadenzaStringa) {
        if (scadenzaStringa == null || !SCADENZA_PATTERN.matcher(scadenzaStringa).matches()) {
            throw new IllegalArgumentException("La scadenza deve essere nel formato MM/AA.");
        }

        String[] parti = scadenzaStringa.split("/");
        int mese = Integer.parseInt(parti[0]);
        int anno = 2000 + Integer.parseInt(parti[1]);

        try {
            this.dataScadenza = YearMonth.of(anno, mese);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Data di scadenza non valida.");
        }

        if (this.isScaduta()) {
            throw new IllegalArgumentException("La carta è già scaduta.");
        }
    }

    public boolean isScaduta() {
        LocalDate fineValidita = this.dataScadenza.atEndOfMonth();
        return LocalDate.now().isAfter(fineValidita);
    }

}
