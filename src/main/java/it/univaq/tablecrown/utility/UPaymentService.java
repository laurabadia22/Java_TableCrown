package it.univaq.tablecrown.utility;

import java.util.Map;

public interface UPaymentService {

    /**
     * Valida i dati di una carta e genera un token fittizio.
     * @return Map contenente "token" e "ultimeQuattroCifre"
     */
    Map<String, String> generaToken(String numeroCarta, String cvv);

    /**
     * Simula l'addebito sul servizio bancario.
     */
    boolean effettuaPagamento(String token, float importo);
}
