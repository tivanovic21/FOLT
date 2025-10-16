package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jf;

import java.io.Serializable;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("spavanje")
public class Spavanje implements Serializable {
	private static final long serialVersionUID = 5L;
	private static final int STATUS_OK_GET = 200;

	@Inject
	@RestClient
	ServisPartnerKlijent servisPartner;

	private String vrijeme;
	private String poruka;
	private boolean uspjeh;

	public String getVrijeme() {
		return vrijeme;
	}

	public void setVrijeme(String vrijeme) {
		this.vrijeme = vrijeme;
	}

	public String getPoruka() {
		return poruka;
	}

	public boolean isUspjeh() {
		return uspjeh;
	}

	public String aktivirajSpavanje() {
		try {
			if (this.vrijeme == null || this.vrijeme.trim().isEmpty()) {
				this.poruka = "Vrijeme spavanja je obavezno.";
				this.uspjeh = false;
				return null;
			}

			int vrijemeInt;
			try {
				vrijemeInt = Integer.parseInt(this.vrijeme.trim());
				if (vrijemeInt <= 0) {
					this.poruka = "Vrijeme spavanja mora biti pozitivan broj.";
					this.uspjeh = false;
					return null;
				}
			} catch (NumberFormatException e) {
				this.poruka = "Vrijeme spavanja mora biti valjani broj.";
				this.uspjeh = false;
				return null;
			}

			var response = this.servisPartner.getSpavanje(vrijemeInt);
			var status = response.getStatus();

			if (status == STATUS_OK_GET) {
				this.poruka = "Spavanje je uspješno aktivirano na " + vrijeme + " sekundi.";
				this.uspjeh = true;
				this.vrijeme = "";
			} else {
				this.poruka = "Greška pri aktiviranju spavanja. Status: " + status;
				this.uspjeh = false;
			}

		} catch (Exception e) {
			this.poruka = "Greška pri aktiviranju spavanja: " + e.getMessage();
			this.uspjeh = false;
		}

		return null;
	}

}
