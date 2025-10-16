package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.GenericType;

@RequestScoped
@Named("korisnici")
public class Korisnici implements Serializable {
	private static final long serialVersionUID = 6L;
	private static final int STATUS_OK_GET = 200;

	@Inject
	KorisniciFacade korisniciFacade;

	@Inject
	ServisPartnerKlijent servisPartner;

	private String ime;
	private String prezime;
	private List<Korisnik> korisnici;
	private String poruka;
	private boolean uspjeh = false;

	public String pretraziKorisnikeAPI() {
		try {
			var odg = this.servisPartner.getKorisnici();
			if (odg.getStatus() == STATUS_OK_GET) {
				List<Korisnik> sviKorisnici = odg.readEntity(new GenericType<List<Korisnik>>() {
				});

				if ((this.ime == null || this.ime.trim().isEmpty())
						&& (this.prezime == null || this.prezime.trim().isEmpty())) {
					this.korisnici = sviKorisnici;
					this.poruka = "Prikazani su svi korisnici";
				} else {
					String imeLower = (this.ime != null && !this.ime.trim().isEmpty()) ? this.ime.trim().toLowerCase()
							: "";
					String prezimeLower = (this.prezime != null && !this.prezime.trim().isEmpty())
							? this.prezime.trim().toLowerCase()
							: "";

					this.korisnici = sviKorisnici.stream().filter(k -> {
						boolean imeMatches = imeLower.isEmpty() || k.ime().toLowerCase().contains(imeLower);
						boolean prezimeMatches = prezimeLower.isEmpty()
								|| k.prezime().toLowerCase().contains(prezimeLower);
						return imeMatches && prezimeMatches;
					}).collect(Collectors.toList());

					if (this.korisnici.isEmpty()) {
						this.poruka = "Nema korisnika koji odgovaraju kriterijima pretrage";
						this.korisnici = null;
					} else {
						this.poruka = "Pronađeno je " + korisnici.size() + " korisnika";
					}
				}
				this.uspjeh = true;
			} else {
				this.poruka = "Greška pri dohvaćanju korisnika, status: " + odg.getStatus();
				this.uspjeh = false;
				this.korisnici = null;
			}
		} catch (Exception e) {
			this.poruka = "Greška pri dohvaćanju korisnika: " + e.getMessage();
			this.uspjeh = false;
			this.korisnici = null;
		}
		return null;
	}

	public String pretraziKorisnikeBaza() {
		try {
			if ((this.ime == null || this.ime.trim().isEmpty())
					&& (this.prezime == null || this.prezime.trim().isEmpty())) {
				var korisniciEntiteti = this.korisniciFacade.findAll();
				this.korisnici = this.korisniciFacade.pretvori(korisniciEntiteti);
				this.poruka = "Prikazani su svi korisnici";
			} else {
				String imePattern = (this.ime != null && !this.ime.trim().isEmpty()) ? "%" + this.ime.trim() + "%"
						: "%";
				String prezimePattern = (this.prezime != null && !this.prezime.trim().isEmpty())
						? "%" + this.prezime.trim() + "%"
						: "%";

				var korisniciEntiteti = this.korisniciFacade.findAll(prezimePattern, imePattern);
				this.korisnici = this.korisniciFacade.pretvori(korisniciEntiteti);

				if (this.korisnici != null && !this.korisnici.isEmpty()) {
					this.poruka = "Pronađeno je " + this.korisnici.size() + " korisnika";
				} else {
					this.poruka = "Nema korisnika koji odgovaraju kriterijima pretrage";
					this.korisnici = null;
				}
			}
			this.uspjeh = true;
		} catch (Exception e) {
			this.poruka = "Greška pri pretraživanju korisnika: " + e.getMessage();
			this.korisnici = null;
			this.uspjeh = false;
		}
		return null;
	}

	public String ocisti() {
		this.ime = "";
		this.prezime = "";
		this.korisnici = null;
		this.poruka = "";
		this.uspjeh = false;
		return null;
	}

	public String prikaziSveKorisnikeBaza() {
		this.ime = "";
		this.prezime = "";
		return pretraziKorisnikeBaza();
	}

	public String prikaziSveKorisnikeAPI() {
		this.ime = "";
		this.prezime = "";
		return pretraziKorisnikeAPI();
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

	public List<Korisnik> getKorisnici() {
		return korisnici;
	}

	public String getPoruka() {
		return poruka;
	}

	public boolean isUspjeh() {
		return uspjeh;
	}

}
