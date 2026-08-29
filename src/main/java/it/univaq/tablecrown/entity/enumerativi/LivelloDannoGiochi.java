package it.univaq.tablecrown.entity.enumerativi;

public enum LivelloDannoGiochi {
    DANNO_LEGGERO(5.0f),
    DANNO_MODERATO(10.0f),
    DANNO_GRAVE(15.0f);


    private final float scontoPercentuale;

    LivelloDannoGiochi(float scontoPercentuale) { //costruttore dell'enum
        this.scontoPercentuale = scontoPercentuale;
    }

    public float getScontoPercentuale() {
        return scontoPercentuale;
    }
}
