package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jf;

import java.io.Serializable;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("nadzornaKonzolaPartner")
@ViewScoped
public class NadzornaKonzolaPartner implements Serializable {

	private static final long serialVersionUID = 10L;

	@Inject
	@RestClient
	private ServisPartnerKlijent servisPartner;

	@Inject
	private GlobalniPodaci globalniPodaci;

	private int statusP;
	private int statusP1;
	private int statusP2;
	private boolean samoOperacija = false;
	private int statusOperacije;

	private String novaPoruka = "";

	public void dohvatiStatuse() {
		try {
			this.samoOperacija = false;
			this.statusP = this.servisPartner.headPosluzitelj().getStatus();
			this.statusP1 = this.servisPartner.headPosluziteljStatus(1).getStatus();

			globalniPodaci.setStatusPartner(this.statusP == 200);
		} catch (Exception e) {
			System.err.println("Greška pri dohvaćanju statusa: " + e.getMessage());
			this.statusP = 500;
			this.statusP1 = 500;

			globalniPodaci.setStatusPartner(false);
		}
	}

	public String start(int id) {
		try {
			this.statusOperacije = this.servisPartner.headPosluziteljStart(id).getStatus();
			this.samoOperacija = true;
		} catch (Exception e) {
			System.err.println("Greška pri pokretanju poslužitelja " + id + ": " + e.getMessage());
			this.statusOperacije = 500;
			this.samoOperacija = true;
		}
		return null;
	}

	public String pauza(int id) {
		try {
			this.statusOperacije = this.servisPartner.headPosluziteljPauza(id).getStatus();
			this.samoOperacija = true;
		} catch (Exception e) {
			System.err.println("Greška pri pauziranju poslužitelja " + id + ": " + e.getMessage());
			this.statusOperacije = 500;
			this.samoOperacija = true;
		}
		return null;
	}

	public String kraj() {
		try {
			this.statusOperacije = this.servisPartner.headPosluziteljKraj().getStatus();
			this.samoOperacija = true;
			globalniPodaci.setStatusPartner(false);
			dohvatiStatuse();
		} catch (Exception e) {
			System.err.println("Greška pri završavanju rada poslužitelja: " + e.getMessage());
			this.statusOperacije = 500;
			this.samoOperacija = true;
		}
		return null;
	}

	public int getUkupnoBrojOtvorenihNarudzbi() {
		return globalniPodaci.getSveOtvoreneNarudzbe().values().stream().mapToInt(Integer::intValue).sum();
	}

	public int getUkupnoBrojRacuna() {
		return globalniPodaci.getSviPlaceniRacuni().values().stream().mapToInt(Integer::intValue).sum();
	}

	public String getPocetniStatusRada() {
		return (this.statusP == 200) ? "RADI" : "NE RADI";
	}

	public GlobalniPodaci getGlobalniPodaci() {
		return globalniPodaci;
	}

	public int getStatusP() {
		return statusP;
	}

	public void setStatusP(int statusP) {
		this.statusP = statusP;
	}

	public int getStatusP1() {
		return statusP1;
	}

	public void setStatusP1(int statusP1) {
		this.statusP1 = statusP1;
	}

	public int getStatusP2() {
		return statusP2;
	}

	public void setStatusP2(int statusP2) {
		this.statusP2 = statusP2;
	}

	public boolean isSamoOperacija() {
		return samoOperacija;
	}

	public void setSamoOperacija(boolean samoOperacija) {
		this.samoOperacija = samoOperacija;
	}

	public int getStatusOperacije() {
		return statusOperacije;
	}

	public void setStatusOperacije(int statusOperacije) {
		this.statusOperacije = statusOperacije;
	}

	public String getNovaPoruka() {
		return novaPoruka;
	}

	public void setNovaPoruka(String novaPoruka) {
		this.novaPoruka = novaPoruka;
	}
}
