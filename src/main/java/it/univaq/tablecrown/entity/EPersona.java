package it.univaq.tablecrown.entity;

import jakarta.persistence.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.regex.Pattern;

@MappedSuperclass
public abstract class EPersona {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpersona")
    private Long idPersona;

    @Column(name = "nomepersona", nullable = false, length = 100)
    private String nomePersona;

    @Column(name = "imgpersona", length = 255)
    private String imgPersona;

    @Column(name = "emailpersona", nullable = false, length = 180, unique = true)
    private String emailPersona;

    @Column(name = "passwordpersona", nullable = false)
    private String passwordPersona;


    //Costruttore vuoto per Hibernate
    protected EPersona() {
    }

    //Costruttore di dominio
    protected EPersona(String nomePersona, String emailPersona, String passwordPersona, String imgPersona) {
        this.rinomina(nomePersona);
        this.cambiaEmail(emailPersona);
        this.cambiaPassword(passwordPersona);
        this.imgPersona = imgPersona;
    }

    // --- GETTER ---
    public Long getIdPersona() {
        return idPersona;
    }

    public String getNomePersona() {
        return nomePersona;
    }

    public String getImgPersona() {
        return imgPersona;
    }

    public String getEmailPersona() {
        return emailPersona;
    }

    // --- METODI DI DOMINIO ---

    public void rinomina(String nuovoNome) {
        if (nuovoNome == null || nuovoNome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome non può essere vuoto.");
        }
        this.nomePersona = nuovoNome.trim();
    }

    public void cambiaEmail(String nuovaEmail) {
        String email = nuovaEmail == null ? "" : nuovaEmail.trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email non valida.");
        }
        this.emailPersona = email;
    }

    public void cambiaPassword(String nuovaPassword) {
        if (nuovaPassword == null || nuovaPassword.length() < 8) {
            throw new IllegalArgumentException("La password deve essere lunga almeno 8 caratteri.");
        }
        this.passwordPersona = BCrypt.hashpw(nuovaPassword, BCrypt.gensalt());
    }

    public void aggiornaImmagine(String img) {
        this.imgPersona = img;
    }

    public boolean verificaPassword(String password) {
        return BCrypt.checkpw(password, this.passwordPersona);
    }
}
