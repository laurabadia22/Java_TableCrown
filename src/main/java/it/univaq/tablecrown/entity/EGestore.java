package it.univaq.tablecrown.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "gestore")
public class EGestore extends EPersona {

    //Costruttore vuoto per Hibernate
    protected EGestore() {
    }

    public EGestore(String nome, String email, String password, String img) {
        super(nome, email, password, img);
    }
}
