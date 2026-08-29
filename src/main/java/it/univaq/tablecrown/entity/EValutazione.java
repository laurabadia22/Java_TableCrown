package it.univaq.tablecrown.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class EValutazione {

    @Column(name = "valutazione_media", nullable = false)
    private float media = 0.0f;

    @Column(name = "numero_valutazioni", nullable = false)
    private int numeroValutazioni = 0;

    //Costruttore vuoto
    public EValutazione() {
    }

    // --- GETTER ---
    public float getMedia() {
        return media;
    }

    public int getNumeroValutazioni() {
        return numeroValutazioni;
    }

    // --- METODI DI DOMINIO ---

    /**
     * Aggiunge una valutazione a stelle (1-5) e aggiorna la media in modo incrementale,
     * senza dover conservare lo storico delle singole valutazioni.
     */
    public void aggiungiValutazione(int stelle) {
        if (stelle < 1 || stelle > 5) {
            throw new IllegalArgumentException("La valutazione deve essere compresa tra 1 e 5 stelle.");
        }
        this.media = (this.media * this.numeroValutazioni + stelle) / (this.numeroValutazioni + 1);
        this.numeroValutazioni++;
    }
}
