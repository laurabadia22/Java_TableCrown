package it.univaq.tablecrown.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Entity
@Table(name = "utente")
public class EUtente extends EPersona {

    @Column(name = "data_nascita", nullable = false)
    private LocalDate dataNascita;

    @Column(name = "data_registrazione", nullable = false)
    private LocalDateTime dataRegistrazione;

    // TODO: riattivare quando EOrdine è tradotta
//    @OneToMany(mappedBy = "utente")
//    private List<EOrdine> ordini = new ArrayList<>();

    // TODO: riattivare quando EIndirizzo è tradotta
//    @OneToMany(mappedBy = "utente")
//    private List<EIndirizzo> indirizzi = new ArrayList<>();

    //Costruttore vuoto per Hibernate
    protected EUtente() {
    }

    public EUtente(String nomeUtente, String emailUtente, String passwordUtente, LocalDate dataNascita, byte[] imgUtente) {
        super(nomeUtente, emailUtente, passwordUtente, imgUtente);
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

    // TODO: riattivare quando EOrdine è tradotta
//    public List<EOrdine> getOrdini() {
//        return ordini;
//    }
//
//    public void riceviOrdine(EOrdine ordine) {
//        if (!this.ordini.contains(ordine)) {
//            this.ordini.add(ordine);
//        }
//    }

    // TODO: riattivare quando EIndirizzo è tradotta
//    public List<EIndirizzo> getIndirizzi() {
//        return indirizzi;
//    }
//
//    public void riceviIndirizzo(EIndirizzo indirizzo) {
//        if (!this.indirizzi.contains(indirizzo)) {
//            this.indirizzi.add(indirizzo);
//        }
//    }

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
