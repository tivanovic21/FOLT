package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.ServisPartnerKlijent;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

@SessionScoped
@Named("novaNarudzba")
public class NovaNarudzba implements Serializable {
	private static final long serialVersionUID = 4L;

	private static final String NOVA_NARUDZBA_AKCIJA = "nova narudžba";
	private static final String RACUN_AKCIJA = "izdavanje račun";

	private static final int UNAUTHORIZED_KOD = 401;
	private static final int POGRESKA_KOD = 409;
	private static final int OK_KOD_POST = 201;
	private static final int OK_KOD_GET = 200;

	@Inject
	@RestClient
	ServisPartnerKlijent servisPartner;

	@Inject
	PregledPonude pregledPonude;

	@Inject
	PrijavaKorisnika prijavaKorisnika;

	@Inject
	GlobalniPodaci globalniPodaci;

	@Inject
	ZapisiFacade zapisiFacade;

	private List<Jelovnik> jelovnik;
	private List<KartaPica> kartaPica;

	private String odabranoJelo;
	private float kolicinaJela = 1.0f;
	private String odabranoPice;
	private float kolicinaPica = 1.0f;

	private List<Jelovnik> narucenaJela;
	private List<KartaPica> narucenaPica;
	private boolean aktivnaNarudzba = false;

	private String poruka;
	private boolean uspjeh;

	private List<Float> kolicineJela;
	private List<Float> kolicinePica;

	private String korisnikNarudzbe;

	public List<Jelovnik> getJelovnik() {
		if ((this.jelovnik == null || this.jelovnik.isEmpty()) && prijavaKorisnika.isPrijavljen()
				&& prijavaKorisnika.isPartnerOdabran()) {
			this.jelovnik = pregledPonude.ucitajIDohvatiJelovnik();
		}
		return this.jelovnik;
	}

	public List<KartaPica> getKartaPica() {
		if ((this.kartaPica == null || this.kartaPica.isEmpty()) && prijavaKorisnika.isPrijavljen()
				&& prijavaKorisnika.isPartnerOdabran()) {
			this.kartaPica = pregledPonude.ucitajIDohvatiKartuPica();
		}
		return this.kartaPica;
	}

	public String getOdabranoJelo() {
		return odabranoJelo;
	}

	public void setOdabranoJelo(String odabranoJelo) {
		this.odabranoJelo = odabranoJelo;
	}

	public float getKolicinaJela() {
		return kolicinaJela;
	}

	public void setKolicinaJela(float kolicinaJela) {
		this.kolicinaJela = kolicinaJela;
	}

	public String getOdabranoPice() {
		return odabranoPice;
	}

	public void setOdabranoPice(String odabranoPice) {
		this.odabranoPice = odabranoPice;
	}

	public float getKolicinaPica() {
		return kolicinaPica;
	}

	public void setKolicinaPica(float kolicinaPica) {
		this.kolicinaPica = kolicinaPica;
	}

	public List<Jelovnik> getNarucenaJela() {
		return narucenaJela;
	}

	public List<KartaPica> getNarucenaPica() {
		return narucenaPica;
	}

	public boolean isAktivnaNarudzba() {
		return aktivnaNarudzba;
	}

	public String getPoruka() {
		return poruka;
	}

	public boolean isUspjeh() {
		return uspjeh;
	}

	public List<Float> getKolicineJela() {
		return kolicineJela;
	}

	public List<Float> getKolicinePica() {
		return kolicinePica;
	}

	@PostConstruct
	public void init() {
		this.narucenaJela = new ArrayList<>();
		this.narucenaPica = new ArrayList<>();
		this.jelovnik = this.pregledPonude.ucitajIDohvatiJelovnik();
		this.kartaPica = this.pregledPonude.ucitajIDohvatiKartuPica();
		this.kolicineJela = new ArrayList<>();
		this.kolicinePica = new ArrayList<>();

		ucitajAkoPostojiOtvorenaNarudzba();
	}

	private void ucitajPostojecuNarudzbu() {
	    try {
	        String korisnickoIme = prijavaKorisnika.getKorisnickoIme();
	        String lozinka = prijavaKorisnika.getLozinka();

	        var response = servisPartner.getNarudzba(korisnickoIme, lozinka);

	        if (response.getStatus() == OK_KOD_GET) {
	            String json = response.readEntity(String.class);

	            Gson gson = new Gson();
	            Type listType = new TypeToken<List<Narudzba>>() {}.getType();
	            List<Narudzba> narudzbe = gson.fromJson(json, listType);

	            this.narucenaJela.clear();
	            this.kolicineJela.clear();
	            this.narucenaPica.clear();
	            this.kolicinePica.clear();

	            for (Narudzba narudzba : narudzbe) {
	                if (narudzba.jelo()) {
	                    Jelovnik jelo = pronadjiJelo(narudzba.id());
	                    if (jelo != null) {
	                        narucenaJela.add(jelo);
	                        kolicineJela.add(narudzba.kolicina());
	                    }
	                } else {
	                    KartaPica pice = pronadjiPice(narudzba.id());
	                    if (pice != null) {
	                        narucenaPica.add(pice);
	                        kolicinePica.add(narudzba.kolicina());
	                    }
	                }
	            }

	            this.poruka = "Učitana postojeća narudžba";
	            this.uspjeh = true;
	        } else {
	            this.poruka = "Nema aktivne narudžbe za korisnika";
	            this.uspjeh = false;
	        }
	    } catch (Exception e) {
	        System.out.println("Greška pri učitavanju postojeće narudžbe: " + e.getMessage());
	        this.poruka = "Greška pri učitavanju narudžbe: " + e.getMessage();
	        this.uspjeh = false;
	    }
	}


	public String kreirajNarudzbu() {
		if (this.aktivnaNarudzba) {
			this.poruka = "Već postoji aktivna narudžba";
			this.uspjeh = false;
			return null;
		}

		if (!this.prijavaKorisnika.isPrijavljen() || !this.prijavaKorisnika.isPartnerOdabran()) {
			this.poruka = "Morate odabrati partnera";
			this.uspjeh = false;
			return null;
		}

		try {
			this.jelovnik = pregledPonude.getJelovnikList();
			this.kartaPica = pregledPonude.getKartaPicaList();

			var odg = servisPartner.postNarudzba(this.prijavaKorisnika.getKorisnickoIme(),
					this.prijavaKorisnika.getLozinka());

			if (odg.getStatus() == OK_KOD_POST) {
				this.aktivnaNarudzba = true;
				this.korisnikNarudzbe = prijavaKorisnika.getKorisnickoIme();
				this.globalniPodaci.dodajOtvorenuNarudzbu(prijavaKorisnika.getOdabraniPartner().id());
				this.globalniPodaci.dodajAktivnuNarudzbuZaKorisnika(this.korisnikNarudzbe, prijavaKorisnika.getOdabraniPartner().id());
				obaviZapisAkcije(NOVA_NARUDZBA_AKCIJA);

				this.poruka = "Narudžba je uspješno kreirana";
				this.uspjeh = true;
			} else if (odg.getStatus() == UNAUTHORIZED_KOD) {
				this.poruka = "Neispravna autentikacija";
				this.uspjeh = false;
			} else if (odg.getStatus() == POGRESKA_KOD) {
				this.poruka = "Već postoji narudžba za korisnika";
				this.uspjeh = false;
			} else {
				this.poruka = "Greška pri kreiranju narudžbe. Status: " + odg.getStatus();
				this.uspjeh = false;
			}
		} catch (Exception e) {
			this.poruka = "Greška pri kreiranju narudžbe: " + e.getMessage();
			this.uspjeh = false;
		}

		return null;
	}

	public String platiNarudzbu() {
		if (!this.aktivnaNarudzba) {
			this.poruka = "Nema aktivne narudžbe za plaćanje";
			this.uspjeh = false;
			return null;
		}

		try {
			var odg = this.servisPartner.postRacun(this.prijavaKorisnika.getKorisnickoIme(),
					this.prijavaKorisnika.getLozinka());

			if (odg.getStatus() == OK_KOD_POST) {
				this.aktivnaNarudzba = false;
				this.globalniPodaci.ukloniOtvorenuNarudzbu(this.prijavaKorisnika.getOdabraniPartner().id());
				this.globalniPodaci.ukloniAktivnuNarudzbuZaKorisnika(this.korisnikNarudzbe);
				this.globalniPodaci.dodajPlaceniRacun(this.prijavaKorisnika.getOdabraniPartner().id());

				obaviZapisAkcije(RACUN_AKCIJA);

				this.narucenaJela.clear();
				this.narucenaPica.clear();

				this.poruka = "Račun je uspješno plaćen";
				this.uspjeh = true;
			} else if (odg.getStatus() == UNAUTHORIZED_KOD) {
				this.poruka = "Neispravna autentikacija";
				this.uspjeh = false;
			} else if (odg.getStatus() == POGRESKA_KOD) {
				this.poruka = "Nema otvorene narudžbe";
				this.uspjeh = false;
			} else {
				this.poruka = "Greška pri plaćanju računa, status: " + odg.getStatus();
				this.uspjeh = false;
			}
		} catch (Exception e) {
			this.poruka = "Greška pri plaćanju računa: " + e.getMessage();
			this.uspjeh = false;
		}
		return null;
	}

	public String dodajJelo() {
		if (!this.aktivnaNarudzba) {
			this.poruka = "Prvo morate kreirati narudžbu";
			this.uspjeh = false;
			return null;
		}

		if (this.kolicinaJela < 1 || this.kolicinaJela > 100) {
			this.poruka = "Količina jela mora biti između 1 i 100";
			this.uspjeh = false;
			return null;
		}

		if (this.odabranoJelo == null || this.odabranoJelo.isEmpty()) {
			poruka = "Morate odabrati jelo";
			uspjeh = false;
			return null;
		}

		try {
			var jelo = pronadjiJelo(this.odabranoJelo);
			if (jelo == null) {
				this.poruka = "Odabrano jelo nije pronađeno";
				this.uspjeh = false;
				return null;
			}

			var narudzba = new Narudzba(prijavaKorisnika.getKorisnickoIme(), odabranoJelo, true, kolicinaJela,
					jelo.cijena(), System.currentTimeMillis());

			var response = servisPartner.postJelo(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka(),
					narudzba);

			if (response.getStatus() == OK_KOD_POST) {
				this.narucenaJela.add(jelo);
				this.kolicineJela.add(this.kolicinaJela);

				this.poruka = "Jelo je dodano u narudžbu";
				this.uspjeh = true;
				this.kolicinaJela = 1.0f;
				this.odabranoJelo = "";
			} else if (response.getStatus() == UNAUTHORIZED_KOD) {
				this.poruka = "Neispravna autentikacija";
				this.uspjeh = false;
			} else if (response.getStatus() == POGRESKA_KOD) {
				this.poruka = "Nema otvorene narudžbe ili jelo ne postoji";
				this.uspjeh = false;
			} else {
				this.poruka = "Greška pri dodavanju jela, status: " + response.getStatus();
				this.uspjeh = false;
			}
		} catch (Exception e) {
			this.poruka = "Greška pri dodavanju jela: " + e.getMessage();
			this.uspjeh = false;
		}

		return null;
	}

	public String dodajPice() {
		if (!this.aktivnaNarudzba) {
			this.poruka = "Prvo morate kreirati narudžbu";
			this.uspjeh = false;
			return null;
		}

		if (this.kolicinaPica < 1 || this.kolicinaPica > 100) {
			this.poruka = "Količina pića mora biti između 1 i 100";
			this.uspjeh = false;
			return null;
		}

		if (this.odabranoPice == null || this.odabranoPice.isEmpty()) {
			poruka = "Morate odabrati piće";
			uspjeh = false;
			return null;
		}

		try {
			var pice = pronadjiPice(this.odabranoPice);
			if (pice == null) {
				this.poruka = "Odabrano piće nije pronađeno";
				this.uspjeh = false;
				return null;
			}

			var narudzba = new Narudzba(prijavaKorisnika.getKorisnickoIme(), odabranoPice, false, kolicinaPica,
					pice.cijena(), System.currentTimeMillis());

			var response = servisPartner.postPice(prijavaKorisnika.getKorisnickoIme(), prijavaKorisnika.getLozinka(),
					narudzba);

			if (response.getStatus() == OK_KOD_POST) {
				this.narucenaPica.add(pice);
				this.kolicinePica.add(this.kolicinaPica);

				this.poruka = "Piće je dodano u narudžbu";
				this.uspjeh = true;
				this.kolicinaPica = 1.0f;
				this.odabranoPice = "";
			} else if (response.getStatus() == UNAUTHORIZED_KOD) {
				this.poruka = "Neispravna autentikacija";
				this.uspjeh = false;
			} else if (response.getStatus() == POGRESKA_KOD) {
				this.poruka = "Nema otvorene narudžbe ili piće ne postoji";
				this.uspjeh = false;
			} else {
				this.poruka = "Greška pri dodavanju pića, status: " + response.getStatus();
				this.uspjeh = false;
			}
		} catch (Exception e) {
			this.poruka = "Greška pri dodavanju pića: " + e.getMessage();
			this.uspjeh = false;
		}
		return null;
	}

	public void ucitajAkoPostojiOtvorenaNarudzba() {
		if (!prijavaKorisnika.isPrijavljen() || !prijavaKorisnika.isPartnerOdabran()) {
			return;
		}

		String trenutniKorisnik = prijavaKorisnika.getKorisnickoIme();

		if (korisnikNarudzbe != null && !korisnikNarudzbe.equals(trenutniKorisnik)) {
			aktivnaNarudzba = false;
			narucenaJela.clear();
			narucenaPica.clear();
			kolicineJela.clear();
			kolicinePica.clear();
			korisnikNarudzbe = null;
		}

		if (!aktivnaNarudzba && globalniPodaci.korisnikImaAktivnuNarudzbu(trenutniKorisnik)) {
			this.aktivnaNarudzba = true;
			this.korisnikNarudzbe = trenutniKorisnik;
			ucitajPostojecuNarudzbu();
		}
	}

	private Jelovnik pronadjiJelo(String id) {
		return this.jelovnik != null ? this.jelovnik.stream().filter(j -> j.id().equals(id)).findFirst().orElse(null)
				: null;
	}

	private KartaPica pronadjiPice(String id) {
		return this.kartaPica != null ? this.kartaPica.stream().filter(p -> p.id().equals(id)).findFirst().orElse(null)
				: null;
	}

	private void obaviZapisAkcije(String akcija) throws UnknownHostException {
		var req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String ip = req.getRemoteAddr().toString();
		String adresaRac = req.getRemoteHost();
		zapisiFacade.zapisiAkciju(prijavaKorisnika.getKorisnickoIme(), adresaRac, ip, akcija);
	}
}
