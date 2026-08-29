package it.univaq.tablecrown.entity;

import it.univaq.tablecrown.entity.enumerativi.DisponibilitaProdotto;
import jakarta.persistence.*;

@Entity
@Table(name = "porta_dadi")
@DiscriminatorValue("portaDadi")
public class EPortaDadi extends EProdotto{

    //Costruttore vuoto per Hibernate
    protected EPortaDadi() {
        super();
    }

    //Costruttore con logica di dominio
    public EPortaDadi(String nomeProdotto, String descrizioneProdotto, DisponibilitaProdotto disponibilitaProdotto, int quantita, byte[] imgProdotto, float prezzo){
        super(nomeProdotto, descrizioneProdotto, disponibilitaProdotto, quantita, imgProdotto, prezzo);
    }
}
