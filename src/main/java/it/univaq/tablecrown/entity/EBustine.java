package it.univaq.tablecrown.entity;

import it.univaq.tablecrown.entity.enumerativi.DisponibilitaProdotto;


import jakarta.persistence.*;

@Entity
@Table(name = "bustine")
@DiscriminatorValue("bustine")
public class EBustine extends EProdotto{
    // Costruttore vuoto richiesto da Hibernate
    protected EBustine() {
        super();
    }

    // Costruttore completo che invoca il costruttore della classe padre (EProdotto)
    public EBustine(String nomeProdotto, String descrizioneProdotto, DisponibilitaProdotto disponibilitaProdotto, int quantita, String imgProdotto, float prezzo) {
        super(nomeProdotto, descrizioneProdotto, disponibilitaProdotto, quantita, imgProdotto, prezzo);
    }
}
