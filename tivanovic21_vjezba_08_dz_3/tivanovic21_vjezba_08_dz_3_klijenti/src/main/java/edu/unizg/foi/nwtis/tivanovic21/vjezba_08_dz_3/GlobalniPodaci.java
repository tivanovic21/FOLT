package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GlobalniPodaci {
	private int brojObracuna;
	private Map<Integer, Integer> brojOtvorenihNarudzbi = new ConcurrentHashMap<>();
	private Map<Integer, Integer> brojRacuna = new ConcurrentHashMap<>();

	private boolean statusTvrtka;
	private boolean statusPartner;
	private String poruka;
	private String porukaPartner;

	private Map<String, Boolean> korisniciSAktivnimNarudzbama = new ConcurrentHashMap<>();
	private Map<String, Integer> korisniciIPartneriNarudzbi = new ConcurrentHashMap<>();

	public boolean getStatusTvrtka() {
		return this.statusTvrtka;
	}

	public void setStatusTvrtka(boolean status) {
		this.statusTvrtka = status;
	}

	public String getPoruka() {
		return this.poruka;
	}

	public void setPoruka(String poruka) {
		this.poruka = poruka;
	}

	public String getPorukaPartner() {
		return this.porukaPartner;
	}

	public void setPorukaPartner(String poruka) {
		this.porukaPartner = poruka;
	}

	public int getBrojObracuna() {
		return this.brojObracuna;
	}

	public void setBrojObracuna(int brojObracuna) {
		this.brojObracuna = brojObracuna;
	}

	public void incrementBrojObracuna() {
		this.brojObracuna++;
	}

	public void decrementBrojObracuna() {
		if (this.brojObracuna > 0)
			this.brojObracuna--;
	}

	public void dodajOtvorenuNarudzbu(int partnerId) {
		this.brojOtvorenihNarudzbi.merge(partnerId, 1, Integer::sum);
	}

	public void ukloniOtvorenuNarudzbu(int partnerId) {
		this.brojOtvorenihNarudzbi.computeIfPresent(partnerId, (id, count) -> (count > 1) ? count - 1 : null);
	}

	public int getBrojOtvorenihNarudzbi(int partnerId) {
		return this.brojOtvorenihNarudzbi.getOrDefault(partnerId, 0);
	}

	public Map<Integer, Integer> getSveOtvoreneNarudzbe() {
		return this.brojOtvorenihNarudzbi;
	}

	public void dodajPlaceniRacun(int partnerId) {
		this.brojRacuna.merge(partnerId, 1, Integer::sum);
	}

	public int getBrojPlacenihRacuna(int partnerId) {
		return this.brojRacuna.getOrDefault(partnerId, 0);
	}

	public Map<Integer, Integer> getSviPlaceniRacuni() {
		return this.brojRacuna;
	}

	public void dodajAktivnuNarudzbuZaKorisnika(String korisnickoIme, int partnerId) {
		this.korisniciSAktivnimNarudzbama.put(korisnickoIme, true);
		this.korisniciIPartneriNarudzbi.put(korisnickoIme, partnerId);
	}

	public void ukloniAktivnuNarudzbuZaKorisnika(String korisnickoIme) {
		this.korisniciSAktivnimNarudzbama.remove(korisnickoIme);
		this.korisniciIPartneriNarudzbi.remove(korisnickoIme);
	}

	public boolean korisnikImaAktivnuNarudzbu(String korisnickoIme) {
		return this.korisniciSAktivnimNarudzbama.containsKey(korisnickoIme);
	}

	public Integer getPartnerZaKorisnika(String korisnickoIme) {
		return this.korisniciIPartneriNarudzbi.get(korisnickoIme);
	}

	public void setStatusPartner(boolean status) {
		this.statusPartner = status;
	}
}
