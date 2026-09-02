package it.univaq.tablecrown.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "utente")
public class EUtente extends EPersona {

    @Column(name = "dataNascita", nullable = false)
    private LocalDate dataNascita;

    @Column(name = "dataRegistrazione", nullable = false)
    private LocalDateTime dataRegistrazione;

    @OneToMany(mappedBy = "utente")
    private List<EOrdine> ordini = new ArrayList<>();

    @OneToMany(mappedBy = "utente")
    private List<EIndirizzo> indirizzi = new ArrayList<>();

    //Costruttore vuoto per Hibernate
    protected EUtente() {
    }

    //Costruttore di dominio
    public EUtente(String nomeUtente, String emailUtente, String passwordUtente, LocalDate dataNascita, String imgUtente) {
        super(nomeUtente, emailUtente, passwordUtente, imgUtente);
        this.impostaDataNascita(dataNascita);
        this.dataRegistrazione = LocalDateTime.now();
    }

    //Costruttore senza immagine (passa null a super)
    public EUtente(String nomeUtente, String emailUtente, String passwordUtente, LocalDate dataNascita) {
        super(nomeUtente, emailUtente, passwordUtente, null); // null per imgPersona
        this.impostaDataNascita(dataNascita);
        this.dataRegistrazione = LocalDateTime.now();
    }

    // --- GETTER ---
    public LocalDate getDataNascita() {
        return dataNascita;
    }

    public LocalDateTime getDataRegistrazione() {
        return dataRegistrazione;
    }

    public int getEta() {
        return calcolaEta(this.dataNascita);
    }

    public List<EOrdine> getOrdini() {
        return ordini;
    }

    public void riceviOrdine(EOrdine ordine) {
        if (!this.ordini.contains(ordine)) {
            this.ordini.add(ordine);
        }
    }

    public List<EIndirizzo> getIndirizzi() {
        return indirizzi;
    }

    public void riceviIndirizzo(EIndirizzo indirizzo) {
        if (!this.indirizzi.contains(indirizzo)) {
            this.indirizzi.add(indirizzo);
        }
    }

    // --- METODI DI DOMINIO ---

    public void aggiornaDataNascita(LocalDate nuovaDataNascita) {
        this.impostaDataNascita(nuovaDataNascita);
    }

    private void impostaDataNascita(LocalDate dataNascita) {
        if (dataNascita == null) {
            throw new IllegalArgumentException("La data di nascita non può essere nulla.");
        }
        if (dataNascita.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La data di nascita non può essere nel futuro.");
        }
        this.dataNascita = dataNascita;
    }

    private int calcolaEta(LocalDate dataNascita) {
        return Period.between(dataNascita, LocalDate.now()).getYears();
    }
}
