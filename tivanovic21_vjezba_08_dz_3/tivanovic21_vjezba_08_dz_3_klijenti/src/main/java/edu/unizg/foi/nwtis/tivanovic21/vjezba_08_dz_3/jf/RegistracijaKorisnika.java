package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jf;

import java.io.Serializable;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("registracijaKorisnika")
public class RegistracijaKorisnika implements Serializable {
	private static final long serialVersionUID = -1826447623127477398L;
	

    @Inject
    RestConfiguration restConfiguration;
    
    @Inject
    @RestClient
    ServisPartnerKlijent servisPartner;
    
    private String korisnickoIme;
    private String lozinka;
    private String ime;
    private String prezime;
    private String email;
    private String poruka;
    private boolean uspjeh;
    private int statusKod;
    
    public String registrirajKorisnika() {
        try {
            if (korisnickoIme == null || korisnickoIme.trim().isEmpty() ||
                lozinka == null || lozinka.trim().isEmpty() ||
                ime == null || ime.trim().isEmpty() ||
                prezime == null || prezime.trim().isEmpty()) {
                
                poruka = "Korisničko ime, lozinka, ime i prezime su obavezni.";
                uspjeh = false;
                return null;
            }

            var korisnik = new Korisnik(
                korisnickoIme.trim(), 
                lozinka.trim(), 
                prezime.trim(), 
                ime.trim(), 
                email != null ? email.trim() : ""
            );

            var response = servisPartner.postKorisnik(korisnik);
            statusKod = response.getStatus();

            if (statusKod == 201) {
                poruka = "Korisnik je uspješno registriran.";
                uspjeh = true;
                ocistiPolja();
            } else if (statusKod == 409) {
                poruka = "Korisnik s tim korisničkim imenom već postoji.";
                uspjeh = false;
            } else if (statusKod == 500) {
                poruka = "Interna greška poslužitelja pri registraciji korisnika.";
                uspjeh = false;
            } else {
                poruka = "Neočekivana greška pri registraciji korisnika. Status: " + statusKod;
                uspjeh = false;
            }
        } catch (Exception e) {
            poruka = "Greška pri registraciji korisnika: " + e.getMessage();
            uspjeh = false;
            statusKod = 500;
        }
        return null;
    }
    
    private void ocistiPolja() {
        korisnickoIme = "";
        lozinka = "";
        ime = "";
        prezime = "";
        email = "";
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }
    
    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }
    
    public String getLozinka() {
        return lozinka;
    }
    
    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }
    
    public String getIme() {
        return ime;
    }
    
    public void setIme(String ime) {
        this.ime = ime;
    }
    
    public String getPrezime() {
        return prezime;
    }
    
    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPoruka() {
        return poruka;
    }
    
    public boolean isUspjeh() {
        return uspjeh;
    }
    
    public int getStatusKod() {
        return statusKod;
    }
}
