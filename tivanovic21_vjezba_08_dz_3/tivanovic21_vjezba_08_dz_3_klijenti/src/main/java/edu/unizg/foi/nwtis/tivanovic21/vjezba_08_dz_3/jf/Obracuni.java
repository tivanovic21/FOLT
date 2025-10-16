package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.pomocnici.ObracuniFacade;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("obracuni")
public class Obracuni implements Serializable {
	private static final long serialVersionUID = 8L;

	@Inject
	ObracuniFacade obracuniFacade;

	@Inject
	PrijavaKorisnika prijavaKorisnika;

	private Date datumOd;
	private Date datumDo;

	private String timestampOd;
	private String timestampDo;

	private boolean dateFormat = true;

	private List<Obracun> obracuni;
	private String poruka;
	private boolean uspjeh = false;

	public String pretraziObracune() {
		try {
			if (!this.prijavaKorisnika.isPrijavljen() || !this.prijavaKorisnika.isPartnerOdabran()) {
				this.poruka = "Morate odabrati partnera prije pretrage obračuna";
				this.uspjeh = false;
				this.obracuni = null;
				return null;
			}

			long vrijemeOd, vrijemeDo;

			if (this.dateFormat) {
				if (this.datumOd == null || this.datumDo == null) {
					this.poruka = "Morate unijeti i datum od i datum do";
					this.uspjeh = false;
					this.obracuni = null;
					return null;
				}
				if (this.datumOd.after(this.datumDo)) {
					this.poruka = "Datum od ne može biti veći od datuma do";
					this.uspjeh = false;
					this.obracuni = null;
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
					this.obracuni = null;
					return null;
				}
				if (vrijemeOd > vrijemeDo) {
					this.poruka = "Vrijeme od ne može biti veće od vremena do";
					this.uspjeh = false;
					this.obracuni = null;
					return null;
				}
			}

			int partnerId = this.prijavaKorisnika.getOdabraniPartner().id();
			var obracuniEntiteti = this.obracuniFacade.filterPoPartneruIVremenu(partnerId, vrijemeOd, vrijemeDo);
			this.obracuni = this.obracuniFacade.pretvori(obracuniEntiteti);

			if (this.obracuni != null && !this.obracuni.isEmpty()) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
				this.poruka = "Pronađeno je " + this.obracuni.size() + " obračuna za partnera '"
						+ this.prijavaKorisnika.getOdabraniPartner().naziv() + "' u razdoblju od "
						+ sdf.format(new Date(vrijemeOd)) + " do " + sdf.format(new Date(vrijemeDo)) + ".";
				this.uspjeh = true;
			} else {
				this.poruka = "Nema obračuna u zadanom razdoblju.";
				this.uspjeh = true;
				this.obracuni = null;
			}

		} catch (Exception e) {
			this.poruka = "Greška pri pretraživanju obračuna: " + e.getMessage();
			this.uspjeh = false;
			this.obracuni = null;
		}
		return null;
	}

	public String ocisti() {
		this.datumOd = null;
		this.datumDo = null;
		this.timestampOd = null;
		this.timestampDo = null;
		this.obracuni = null;
		this.poruka = "";
		this.uspjeh = false;
		return null;
	}

	public String formatDatum(long vrijeme) {
		return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(vrijeme));
	}

	public String formatJeloPice(boolean jelo) {
		return jelo ? "Jelo" : "Piće";
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

	public List<Obracun> getObracuni() {
		return this.obracuni;
	}

	public String getPoruka() {
		return this.poruka;
	}

	public boolean isUspjeh() {
		return this.uspjeh;
	}
}
