package it.univaq.tablecrown.utility;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class UHTTPMethods {

    // Restituisce il metodo HTTP della richiesta corrente (GET, POST, ecc.)
    public static String method(HttpServletRequest request) {
        return request.getMethod();
    }

    // NOTA: in Java, getParameter() legge sia dalla query string sia dal corpo
    // del form, quindi get() e post() usano lo stesso meccanismo
    public static String get(HttpServletRequest request, String param, String defaultValue) {
        String value = request.getParameter(param);
        return value != null ? value : defaultValue;
    }

    public static String post(HttpServletRequest request, String param, String defaultValue) {
        String value = request.getParameter(param);
        return value != null ? value : defaultValue;
    }

    public static String postString(HttpServletRequest request, String key, Integer maxLength) {
        String value = request.getParameter(key);
        value = value == null ? "" : value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Il campo '" + key + "' è richiesto.");
        }
        if (maxLength != null && value.length() > maxLength) {
            throw new IllegalArgumentException("Il campo '" + key + "' supera la lunghezza massima di " + maxLength + " caratteri.");
        }
        return value;
    }

    public static int postInt(HttpServletRequest request, String key, Integer min, Integer max) {
        String raw = request.getParameter(key);
        int value;
        try {
            value = Integer.parseInt(raw.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Il campo '" + key + "' deve essere un numero intero.");
        }
        if (min != null && value < min) {
            throw new IllegalArgumentException("Il campo '" + key + "' deve essere maggiore di " + min + ".");
        }
        if (max != null && value > max) {
            throw new IllegalArgumentException("Il campo '" + key + "' deve essere minore di " + max + ".");
        }
        return value;
    }

    public static float postFloat(HttpServletRequest request, String key, Float min, Float max) {
        String raw = request.getParameter(key);
        float value;
        try {
            value = Float.parseFloat(raw.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Il campo '" + key + "' deve essere un numero.");
        }
        if (min != null && value < min) {
            throw new IllegalArgumentException("Il campo '" + key + "' deve essere maggiore di " + min + ".");
        }
        if (max != null && value > max) {
            throw new IllegalArgumentException("Il campo '" + key + "' deve essere minore di " + max + ".");
        }
        return value;
    }

    public static LocalDate postDate(HttpServletRequest request, String key, String format) {
        String raw = request.getParameter(key);
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ofPattern(format));
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("Il campo '" + key + "' deve essere una data nel formato '" + format + "'.");
        }
    }

    // Richiede che la servlet chiamante sia annotata con @MultipartConfig,
    // altrimenti request.getPart() lancia eccezione
    public static Part postFile(HttpServletRequest request, String key) throws IOException, ServletException {
        Part file = request.getPart(key);
        if (file == null || file.getSize() == 0) {
            throw new IllegalArgumentException("File '" + key + "' non valido o non caricato.");
        }
        return file;
    }

    // Equivalente di un campo form con più valori (es. checkbox multipli,
    // name="categorie[]" in PHP -> name="categorie" ripetuto in HTML)
    public static String[] postArray(HttpServletRequest request, String key, boolean required) {
        String[] values = request.getParameterValues(key);
        if (values == null) {
            if (required) {
                throw new IllegalArgumentException("Il campo '" + key + "' è richiesto.");
            }
            return new String[0];
        }
        return values;
    }

    public static boolean postBool(HttpServletRequest request, String key, boolean defaultValue) {
        String raw = request.getParameter(key);
        if (raw == null) {
            return defaultValue;
        }
        String value = raw.trim().toLowerCase();
        return value.equals("true") || value.equals("1") || value.equals("on") || value.equals("yes");
    }

    public static boolean isAjax(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        if (requestedWith != null && requestedWith.equalsIgnoreCase("XMLHttpRequest")) {
            return true;
        }
        String accept = request.getHeader("Accept");
        if (accept != null && accept.toLowerCase().contains("application/json")) {
            return true;
        }
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().contains("application/json");
    }

    public static String getReferer(HttpServletRequest request, String fallback) {
        String requestedWith = request.getHeader("X-Requested-With");
        String referer = request.getHeader("Referer");
        return (requestedWith == null && referer != null) ? referer : fallback;
    }
}
