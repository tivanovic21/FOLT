package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("pregledRada")
public class PregledRada implements Serializable {
	private static final long serialVersionUID = 9L;

	@Inject
	RestConfiguration restConfiguration;

	@Inject
	ZapisiFacade zapisiFacade;

	@Inject
	KorisniciFacade korisniciFacade;

	private String odabraniKorisnik;
	private List<Korisnik> korisnici;
	private Date datumOd;
	private Date datumDo;
	private String timestampOd;
	private String timestampDo;
	private boolean dateFormat = true;
	private List<Zapisi> zapisi;
	private String poruka;
	private boolean uspjeh = false;

	@PostConstruct
	public void init() {
		ucitajKorisnike();
	}

	public void ucitajKorisnike() {
		try {
			var korisniciEntiteti = this.korisniciFacade.findAll();
			this.korisnici = korisniciFacade.pretvori(korisniciEntiteti);
		} catch (Exception e) {
			this.poruka = "Greška pri učitavanju korisnika: " + e.getMessage();
			this.uspjeh = false;
		}
	}

	public String pretraziZapise() {
		try {
			if (this.odabraniKorisnik == null || this.odabraniKorisnik.trim().isEmpty()) {
				this.poruka = "Morate odabrati korisnika prije pretrage zapisa";
				this.uspjeh = false;
				this.zapisi = null;
				return null;
			}

			long vrijemeOd, vrijemeDo;

			if (this.dateFormat) {
				if (this.datumOd == null || this.datumDo == null) {
					this.poruka = "Morate unijeti i datum od i datum do.";
					this.uspjeh = false;
					this.zapisi = null;
					return null;
				}
				if (this.datumOd.after(this.datumDo)) {
					this.poruka = "Datum od ne može biti veći od datuma do.";
					this.uspjeh = false;
					this.zapisi = null;
					return null;
				}
				vrijemeOd = this.datumOd.getTime();
				vrijemeDo = this.datumDo.getTime() + (24 * 60 * 60 * 1000 - 1);
			} else {
				try {
					vrijemeOd = Long.parseLong(this.timestampOd);
					vrijemeDo = Long.parseLong(this.timestampDo);
				} catch (NumberFormatException e) {
					this.poruka = "Neispravan timestamp format. Unesite cijele brojeve";
					this.uspjeh = false;
					this.zapisi = null;
					return null;
				}
				if (vrijemeOd > vrijemeDo) {
					this.poruka = "Vrijeme od ne može biti veće od vremena do";
					this.uspjeh = false;
					this.zapisi = null;
					return null;
				}
			}

			var zapisiEntiteti = this.zapisiFacade.findByKorisnikIVrijeme(this.odabraniKorisnik, vrijemeOd, vrijemeDo);
			this.zapisi = zapisiEntiteti;

			if (this.zapisi != null && !this.zapisi.isEmpty()) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
				this.poruka = "Pronađeno je " + this.zapisi.size() + " zapisa za korisnika '" + this.odabraniKorisnik
						+ "' u razdoblju od " + sdf.format(new Date(vrijemeOd)) + " do "
						+ sdf.format(new Date(vrijemeDo)) + ".";
				this.uspjeh = true;
			} else {
				this.poruka = "Nema zapisa za odabranog korisnika u zadanom razdoblju";
				this.uspjeh = true;
				this.zapisi = null;
			}

		} catch (Exception e) {
			this.poruka = "Greška pri pretraživanju zapisa: " + e.getMessage();
			this.uspjeh = false;
			this.zapisi = null;
		}
		return null;
	}

	public String ocisti() {
		this.odabraniKorisnik = "";
		this.datumOd = null;
		this.datumDo = null;
		this.timestampOd = null;
		this.timestampDo = null;
		this.zapisi = null;
		this.poruka = "";
		this.uspjeh = false;
		return null;
	}

	public String formatDatum(long vrijeme) {
		return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(vrijeme));
	}

	public String getOdabraniKorisnikNaziv() {
		if (this.korisnici != null && this.odabraniKorisnik != null) {
			return this.korisnici.stream().filter(k -> k.korisnik().equals(this.odabraniKorisnik))
					.map(k -> k.ime() + " " + k.prezime()).findFirst().orElse(this.odabraniKorisnik);
		}
		return this.odabraniKorisnik;
	}

	public String getOdabraniKorisnik() {
		return this.odabraniKorisnik;
	}

	public void setOdabraniKorisnik(String odabraniKorisnik) {
		this.odabraniKorisnik = odabraniKorisnik;
	}

	public List<Korisnik> getKorisnici() {
		return this.korisnici;
	}

	public Date getDatumOd() {
		return this.datumOd;
	}

	public void setDatumOd(Date datumOd) {
		this.datumOd = datumOd;
	}

	public Date getDatumDo() {
		return this.datumDo;
	}

	public void setDatumDo(Date datumDo) {
		this.datumDo = datumDo;
	}

	public String getTimestampOd() {
		return this.timestampOd;
	}

	public void setTimestampOd(String timestampOd) {
		this.timestampOd = timestampOd;
	}

	public String getTimestampDo() {
		return this.timestampDo;
	}

	public void setTimestampDo(String timestampDo) {
		this.timestampDo = timestampDo;
	}

	public boolean isDateFormat() {
		return this.dateFormat;
	}

	public void setDateFormat(boolean dateFormat) {
		this.dateFormat = dateFormat;
	}

	public List<Zapisi> getZapisi() {
		return this.zapisi;
	}

	public String getPoruka() {
		return this.poruka;
	}

	public boolean isUspjeh() {
		return this.uspjeh;
	}
}
