package it.univaq.tablecrown.utility;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class UBancaMockService implements UPaymentService{

    private static final SecureRandom random = new SecureRandom();

    @Override
    public Map<String, String> generaToken(String numeroCarta, String cvv) {
        if (numeroCarta == null || cvv == null) {
            throw new IllegalArgumentException("Numero di carta e CVV non possono essere nulli.");
        }

        String numeroPulito = numeroCarta.replaceAll("[\\s\\-]+", "");
        String cvvPulito = cvv.trim();

        if (numeroPulito.length() < 13 || numeroPulito.length() > 19 || !numeroPulito.matches("\\d+")) {
            throw new IllegalArgumentException("Numero di carta non valido. Richiesta rifiutata dalla banca.");
        }

        if (cvvPulito.length() < 3 || cvvPulito.length() > 4 || !cvvPulito.matches("\\d+")) {
            throw new IllegalArgumentException("CVV non valido.");
        }

        // Generazione token: "tok_" + 16 caratteri esadecimali casuali
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder("tok_");
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        String ultimeQuattro = numeroPulito.substring(numeroPulito.length() - 4);

        Map<String, String> risultato = new HashMap<>();
        risultato.put("token", sb.toString());
        risultato.put("ultimeQuattroCifre", ultimeQuattro);

        return risultato;
    }

    @Override
    public boolean effettuaPagamento(String token, float importo) {
        if (token == null || !token.startsWith("tok_")) {
            throw new IllegalArgumentException("Token di pagamento non valido o assente.");
        }

        if (importo <= 0) {
            throw new IllegalArgumentException("L'importo deve essere maggiore di zero.");
        }

        // Simula esito positivo dalla banca
        return true;
    }
}
