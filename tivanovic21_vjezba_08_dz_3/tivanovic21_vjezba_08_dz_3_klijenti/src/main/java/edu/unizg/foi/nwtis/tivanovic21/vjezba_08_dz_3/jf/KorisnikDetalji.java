package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jf;

import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Map;

@Named("korisnikDetalji")
@RequestScoped
public class KorisnikDetalji implements Serializable {
    private static final long serialVersionUID = 7L;
	private static final int STATUS_OK_GET = 200;

    @Inject
    private ServisPartnerKlijent servisPartner;

    private Korisnik korisnik;
    private String poruka;

    @PostConstruct
    public void init() {
        Map<String, String> params = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap();

        String korIme = params.get("korIme");
        if (korIme != null && !korIme.isBlank()) {
            try {
                var odg = this.servisPartner.getKorisnik(korIme);
                if (odg.getStatus() == STATUS_OK_GET) {
                	this.korisnik = odg.readEntity(Korisnik.class);
                } else {
                	this.poruka = "Korisnik nije pronađen (status: " + odg.getStatus() + ")";
                }
            } catch (Exception e) {
            	this.poruka = "Greška pri dohvaćanju korisnika: " + e.getMessage();
            }
        } else {
        	this.poruka = "Nije proslijeđeno korisničko ime";
        }
    }

    public Korisnik getKorisnik() {
        return this.korisnik;
    }

    public String getPoruka() {
        return this.poruka;
    }
}
