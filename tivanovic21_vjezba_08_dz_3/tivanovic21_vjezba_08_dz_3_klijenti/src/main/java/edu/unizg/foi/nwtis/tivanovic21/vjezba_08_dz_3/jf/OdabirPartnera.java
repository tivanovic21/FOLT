package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.dao.PartnerDAO;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("odabirParnera")
public class OdabirPartnera implements Serializable {

	private static final long serialVersionUID = -524581462819739622L;

	@Inject
	PrijavaKorisnika prijavaKorisnika;

	@Inject
	RestConfiguration restConfiguration;

	@Inject
	GlobalniPodaci gp;

	private List<Partner> partneri = new ArrayList<>();

	private int partner;

	private String poruka;

	public int getPartner() {
		return partner;
	}

	public String getPoruka() {
		return poruka;
	}

	public void setPartner(int partner) {
		this.partner = partner;
	}

	public List<Partner> getPartneri() {
		return partneri;
	}

	@PostConstruct
	public void ucitajPartnere() {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var partnerDAO = new PartnerDAO(vezaBP);
			this.partneri = partnerDAO.dohvatiSve(true);
		} catch (Exception e) {
		}
		
		if (postojiOtvorenaNarudzba()) {
			this.poruka = "Imate otvorenu narudžbu s partnerom " + this.prijavaKorisnika.getOdabraniPartner().naziv()
					+ " i morate zatvoriti narudžbu kako bi promijenili partnera!";
		} else {
			this.poruka = null;
		}
			
	}

	private boolean postojiOtvorenaNarudzba() {
		if (gp.korisnikImaAktivnuNarudzbu(prijavaKorisnika.getKorisnickoIme())) {
			int aktivniPartnerId = gp.getPartnerZaKorisnika(prijavaKorisnika.getKorisnickoIme());
			Optional<Partner> aktivniPartner = this.partneri.stream().filter(p -> p.id() == aktivniPartnerId)
					.findFirst();
			aktivniPartner.ifPresent(p -> prijavaKorisnika.setOdabraniPartner(p));
			prijavaKorisnika.setPartnerOdabran(true);

			return true;
		}
		return false;
	}

	public String odaberiPartnera() {
		if (postojiOtvorenaNarudzba()) {
			this.poruka = "Imate otvorenu narudžbu s partnerom " + this.prijavaKorisnika.getOdabraniPartner().naziv()
					+ " i morate zatvoriti narudžbu kako bi promijenili partnera!";
			return "odabirPartnera.xhtml?faces-redirect=true";
		} else {
			if (this.partner > 0) {
				Optional<Partner> partnerO = this.partneri.stream().filter((p) -> p.id() == this.partner).findFirst();
				if (partnerO.isPresent()) {
					this.prijavaKorisnika.setOdabraniPartner(partnerO.get());
					this.prijavaKorisnika.setPartnerOdabran(true);
				} else {
					this.prijavaKorisnika.setPartnerOdabran(false);
				}
			} else {
				this.prijavaKorisnika.setPartnerOdabran(false);
			}
			this.poruka = null;
			return "index.html?faces-redirect=true";
		}

	}

}
