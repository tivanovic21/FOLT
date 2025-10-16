package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jf;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.pomocnici.KorisniciFacade;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.pomocnici.PartneriFacade;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.pomocnici.ZapisiFacade;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;

@SessionScoped
@Named("prijavaKorisnika")
public class PrijavaKorisnika implements Serializable {
	private static final long serialVersionUID = -1826447622277477398L;
	private String korisnickoIme;
	private String lozinka;
	private boolean prijavljen = false;
	private String poruka = "";
	private Korisnik korisnik;
	private Partner odabraniPartner;
	private boolean partnerOdabran = false;

	private static final String PRIJAVA_AKCIJA = "prijava";
	private static final String ODJAVA_AKCIJA = "odjava";

	@Inject
	RestConfiguration restConfiguration;

	@Inject
	KorisniciFacade korisniciFacade;

	@Inject
	ZapisiFacade zapisiFacade;

	@Inject
	PartneriFacade partneriFacade;
	
	@Inject
	GlobalniPodaci gp;

	@Inject
	private SecurityContext securityContext;

	public String getKorisnickoIme() {
		return korisnickoIme;
	}

	public void setKorisnickoIme(String korisnickoIme) {
		this.korisnickoIme = korisnickoIme;
	}

	public String getLozinka() {
		return lozinka;
	}

	public void setLozinka(String lozinka) {
		this.lozinka = lozinka;
	}

	public String getIme() {
		return this.korisnik.ime();
	}

	public String getPrezime() {
		return this.korisnik.prezime();
	}

	public String getEmail() {
		return this.korisnik.email();
	}

	public boolean isPrijavljen() {
		if (!this.prijavljen) {
			provjeriPrijavuKorisnika();
		}
		return this.prijavljen;
	}

	public String getPoruka() {
		return poruka;
	}

	public Partner getOdabraniPartner() {
		return odabraniPartner;
	}

	public void setOdabraniPartner(Partner odabraniPartner) {
		this.odabraniPartner = odabraniPartner;
	}

	public boolean isPartnerOdabran() {
		return partnerOdabran;
	}

	public void setPartnerOdabran(boolean partnerOdabran) {
		this.partnerOdabran = partnerOdabran;
	}

	public boolean isAdmin() {
		return securityContext.isCallerInRole("admin");
	}

	public boolean isNwtis() {
		return securityContext.isCallerInRole("nwtis");
	}

	@PostConstruct
	private void provjeriPrijavuKorisnika() {
		if (this.securityContext.getCallerPrincipal() != null) {
			var korIme = this.securityContext.getCallerPrincipal().getName();
			this.korisnik = this.korisniciFacade.pretvori(this.korisniciFacade.find(korIme));
			if (this.korisnik != null) {
				this.prijavljen = true;
				this.korisnickoIme = korIme;
				this.lozinka = this.korisnik.lozinka();

				provjeriGlobalnoPartnera();

				try {
					obaviZapisAkcije(PRIJAVA_AKCIJA);
				} catch (Exception e) {
					Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Neuspjelo logiranje prijave: ",
							e.getMessage());
				}
			}
		}
	}

	private void provjeriGlobalnoPartnera() {
		if (gp.korisnikImaAktivnuNarudzbu(korisnickoIme)) {
			int partnerId = gp.getPartnerZaKorisnika(korisnickoIme);
			var partner = this.partneriFacade.find(partnerId);
			if (partner != null) {
				this.odabraniPartner = this.partneriFacade.pretvori(partner);
				this.partnerOdabran = true;
			}
		}
	}

	public String odjavaKorisnika() {
		if (this.prijavljen) {
			try {
				obaviZapisAkcije(ODJAVA_AKCIJA);
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Neuspjelo logiranje odjave: ",
						e.getMessage());
			}

			this.prijavljen = false;

			FacesContext facesContext = FacesContext.getCurrentInstance();
			facesContext.getExternalContext().invalidateSession();

			return "/index.xhtml?faces-redirect=true";
		}
		return "";
	}

	private void obaviZapisAkcije(String akcija) throws UnknownHostException {
		var req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String ip = req.getRemoteAddr().toString();
		String adresaRac = req.getRemoteHost();
		zapisiFacade.zapisiAkciju(this.korisnickoIme, adresaRac, ip, akcija);
	}

}
