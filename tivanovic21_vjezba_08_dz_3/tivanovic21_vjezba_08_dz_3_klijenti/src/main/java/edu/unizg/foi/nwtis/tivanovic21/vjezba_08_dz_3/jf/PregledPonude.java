package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("pregledPonude")
public class PregledPonude implements Serializable {
	private static final long serialVersionUID = 3L;

	private static final int STATUS_OK = 200;

	@Inject
	@RestClient
	ServisPartnerKlijent sp;

	@Inject
	PrijavaKorisnika pk;

	private List<Jelovnik> jelovnik;
	private List<KartaPica> kp;

	private String porukaGreske;

	public List<Jelovnik> getJelovnikList() {
		return this.jelovnik;
	}

	public List<KartaPica> getKartaPicaList() {
		return this.kp;
	}

	public String getPorukaGreske() {
		return this.porukaGreske;
	}
	
	public List<Jelovnik> ucitajIDohvatiJelovnik() {
		if (this.jelovnik == null || this.jelovnik.isEmpty())
			dohvatiJelovnik();
		
		return this.jelovnik;
	}
	
	public List<KartaPica> ucitajIDohvatiKartuPica() {
		if (this.kp == null || this.kp.isEmpty())
			dohvatiKartuPica();
		
		return this.kp;
	}

	public void dohvatiJelovnik() {
		if (pk.isPrijavljen() && pk.isPartnerOdabran()) {
			String kor = pk.getKorisnickoIme();
			String loz = pk.getLozinka();

			var odg = sp.getJelovnik(kor, loz);
			if (odg.getStatus() == STATUS_OK) {
				String json = odg.readEntity(String.class);
				Gson gson = new Gson();
				Type type = new TypeToken<List<Jelovnik>>() {
				}.getType();
				this.jelovnik = gson.fromJson(json, type);
				this.jelovnik.sort(Comparator.comparingInt(j -> Integer.parseInt(j.id().replaceAll("\\D+", ""))));
			} else {
				this.jelovnik = null;
			}
		} else {
			this.porukaGreske = "Morate odabrati partnera!";
		}
	}

	public void dohvatiKartuPica() {
		if (pk.isPrijavljen() && pk.isPartnerOdabran()) {
			String kor = pk.getKorisnickoIme();
			String loz = pk.getLozinka();

			var odg = sp.getKartaPica(kor, loz);
			if (odg.getStatus() == STATUS_OK) {
				String json = odg.readEntity(String.class);
				Gson gson = new Gson();
				Type type = new TypeToken<List<KartaPica>>() {
				}.getType();
				this.kp = gson.fromJson(json, type);
				this.kp.sort(Comparator.comparingInt(k -> Integer.parseInt(k.id().replaceAll("\\D+", ""))));
			} else {
				this.kp = null;
			}
		} else {
			this.porukaGreske = "Morate odabrati partnera!";
		}
	}
}
