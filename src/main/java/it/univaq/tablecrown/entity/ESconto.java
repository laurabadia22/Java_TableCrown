package it.univaq.tablecrown.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public class ESconto {

    @Column(name = "sconto", nullable = false)
    private float sconto = 0.0f;

    @Column(name = "scadenza_offerta")
    private LocalDateTime scadenzaOfferta;

    // Costruttore vuoto richiesto da Hibernate (e utile per un prodotto "nuovo" senza sconto)
    public ESconto() {
    }

    // --- GETTER ---
    public float getSconto() {
        return sconto;
    }

    public LocalDateTime getScadenzaOfferta() {
        return scadenzaOfferta;
    }

    // --- METODI DI DOMINIO ---

    /**
     * Aggiorna lo sconto. Se è già presente uno sconto, lo somma a quello esistente
     * (sconti cumulativi), con un tetto massimo del 100%.
     */
    public void aggiornaSconto(float sconto, LocalDateTime scadenzaOfferta) {
        if (sconto < 0 || sconto > 100) {
            throw new IllegalArgumentException("Lo sconto deve essere compreso tra 0 e 100.");
        }
        this.sconto += sconto;
        if (this.sconto > 100) {
            this.sconto = 100;
        }
        if (scadenzaOfferta != null) {
            this.scadenzaOfferta = scadenzaOfferta;
        }
    }

    /**
     * Rimuove lo sconto, azzerandolo.
     */
    public void rimuoviSconto() {
        this.sconto = 0;
        this.scadenzaOfferta = null;
    }

    /**
     * Controlla se lo sconto è attualmente attivo (diverso da zero e non scaduto).
     */
    public boolean hasSconto() {
        return this.sconto != 0
                && this.scadenzaOfferta != null
                && this.scadenzaOfferta.isAfter(LocalDateTime.now());
    }

    /**
     * Applica lo sconto a un valore di partenza, restituendo il prezzo finale.
     */
    public float applicaA(float valore) {
        if (hasSconto()) {
            return valore * (1 - this.sconto / 100);
        }
        return valore;
    }
}
