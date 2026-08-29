package it.univaq.tablecrown.entity;

import it.univaq.tablecrown.entity.enumerativi.DisponibilitaProdotto;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "porta_dadi")
public class EPortaDadi extends EProdotto{

    //Costruttore vuoto per Hibernate
    public EPortaDadi() {
        super();
    }

    //Costruttore con logica di dominio
    public EPortaDadi(String nomeProdotto, String descrizioneProdotto, DisponibilitaProdotto disponibilitaProdotto, int quantita, byte[] imgProdotto){
        super(nomeProdotto, descrizioneProdotto, disponibilitaProdotto, quantita, imgProdotto); //TODO: DA AGGIUNGERE IL PREZZO
    }
}
