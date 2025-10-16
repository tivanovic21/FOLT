package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.entiteti.Partneri;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("pregledPartnera")
public class PregledPartnera implements Serializable {
	private static final long serialVersionUID = 2L;
	@Inject
	private PartneriFacade partneriFacade;

	private List<Partner> partneri = new ArrayList<>();
	private Partner odabraniPartner;
	private int odabraniPartnerId;

	private String greska;

	@PostConstruct
	public void init() {
		String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
		if (idParam != null && !idParam.isEmpty()) {
			try {
				odabraniPartnerId = Integer.parseInt(idParam);
				ucitajPartnera(odabraniPartnerId);
			} catch (NumberFormatException e) {
				greska = "Neispravan ID partnera: " + idParam;
			}
		} else {
			ucitajPartnere();
		}
	}

	public void ucitajPartnere() {
		try {
			List<Partneri> partneriEntiteti = partneriFacade.findAll();
			this.partneri = partneriFacade.pretvori(partneriEntiteti);
		} catch (Exception e) {
			this.greska = "Greška pri dohvaćanju partnera: " + e.getMessage();
		}
	}

	public void ucitajPartnera(int id) {
		try {
			Partneri entitet = partneriFacade.find(id);
			this.odabraniPartner = partneriFacade.pretvori(entitet);
			if (this.odabraniPartner == null) {
				this.greska = "Partner s ID " + id + " nije pronađen";
			}
		} catch (Exception e) {
			this.greska = "Greška pri dohvaćanju partnera: " + e.getMessage();
		}
	}

	public String prikaziPartnera(int partnerId) {
		return "partner.xhtml?faces-redirect=true&id=" + partnerId;
	}

	public List<Partner> getPartneri() {
		return this.partneri;
	}

	public Partner getOdabraniPartner() {
		return this.odabraniPartner;
	}

	public String getGreska() {
		return this.greska;
	}

	public int getOdabraniPartnerId() {
		return this.odabraniPartnerId;
	}

	public void setOdabraniPartnerId(int odabraniPartnerId) {
		this.odabraniPartnerId = odabraniPartnerId;
	}
}
