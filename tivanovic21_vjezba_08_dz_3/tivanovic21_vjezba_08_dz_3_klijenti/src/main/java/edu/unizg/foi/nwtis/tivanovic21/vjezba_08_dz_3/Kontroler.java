/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3;

import java.net.URI;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.mvc.View;
import jakarta.mvc.binding.BindingResult;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author NWTiS
 */
@Controller
@Path("tvrtka")
@RequestScoped
public class Kontroler {

	@Inject
	private Models model;

	@Inject
	private BindingResult bindingResult;

	@Inject
	private GlobalniPodaci globalniPodaci;

	@Inject
	@RestClient
	ServisTvrtkaKlijent servisTvrtka;

	@GET
	@Path("pocetak")
	@View("index.jsp")
	public void pocetak() {
	}

	@GET
	@Path("admin/nadzornaKonzolaTvrtka")
	@View("nadzornaKonzolaTvrtka.jsp")
	public void nadzornaKonzolaTvrtka() {
		this.model.put("globalniPodaci", globalniPodaci);
		dohvatiStatuse();
	}

	@GET
	@Path("admin/kraj")
	@View("status.jsp")
	public void kraj() {
		var status = this.servisTvrtka.headPosluziteljKraj().getStatus();
		this.model.put("statusOperacije", status);
		dohvatiStatuse();
	}

	@GET
	@Path("admin/status")
	@View("status.jsp")
	public void status() {
		dohvatiStatuse();
	}

	@GET
	@Path("admin/start/{id}")
	@View("status.jsp")
	public void startId(@PathParam("id") int id) {
		var status = this.servisTvrtka.headPosluziteljStart(id).getStatus();
		this.model.put("status", status);
		this.model.put("samoOperacija", true);
	}

	@GET
	@Path("admin/pauza/{id}")
	@View("status.jsp")
	public void pauzatId(@PathParam("id") int id) {
		var status = this.servisTvrtka.headPosluziteljPauza(id).getStatus();
		this.model.put("status", status);
		this.model.put("samoOperacija", true);
	}

	@GET
	@Path("partner")
	@View("partneri.jsp")
	public void partneri() {
		var odgovor = this.servisTvrtka.getPartneri();
		var status = odgovor.getStatus();
		if (status == 200) {
			var partneri = odgovor.readEntity(new GenericType<List<Partner>>() {
			});
			this.model.put("status", status);
			this.model.put("partneri", partneri);
		}
	}

	@GET
	@Path("partner/{id}")
	@View("partner.jsp")
	public void partner(@PathParam("id") int id) {
		try {
			var odgovor = this.servisTvrtka.getPartner(id);
			var status = odgovor.getStatus();

			if (status == 200) {
				var partner = odgovor.readEntity(Partner.class);
				this.model.put("partner", partner);
				this.model.put("status", status);
			} else if (status == 404) {
				this.model.put("greska", "Partner s ID " + id + " nije pronađen");
			} else {
				this.model.put("greska", "Greška pri dohvaćanju partnera. Status: " + status);
			}
		} catch (Exception e) {
			this.model.put("greska", "Greška pri dohvaćanju partnera: " + e.getMessage());
		}
	}

	@GET
	@Path("privatno/obracuni")
	@View("obracuni.jsp")
	public void obracuni(@QueryParam("od") String od, @QueryParam("do") String doParam, @QueryParam("tip") String tip) {
		try {
			Long vrijemeOd = null;
			Long vrijemeDo = null;

			if (od != null && !od.isEmpty()) {
				vrijemeOd = Long.parseLong(od);
			}
			if (doParam != null && !doParam.isEmpty()) {
				vrijemeDo = Long.parseLong(doParam);
			}

			Response odgovor;
			switch (tip != null ? tip : "sve") {
			case "jelo":
				odgovor = this.servisTvrtka.getObracuniJelo(vrijemeOd, vrijemeDo);
				break;
			case "pice":
				odgovor = this.servisTvrtka.getObracuniPice(vrijemeOd, vrijemeDo);
				break;
			default:
				odgovor = this.servisTvrtka.getObracuni(vrijemeOd, vrijemeDo);
				break;
			}

			var status = odgovor.getStatus();
			if (status == 200) {
				var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {
				});
				this.model.put("obracuni", obracuni);
			} else {
				this.model.put("greska", "Greška pri dohvaćanju obračuna. Status: " + status);
			}

			this.model.put("od", od);
			this.model.put("do", doParam);
			this.model.put("tip", tip);
			this.model.put("status", status);

		} catch (Exception e) {
			this.model.put("greska", "Greška pri dohvaćanju obračuna: " + e.getMessage());
		}
	}

	@GET
	@Path("privatno/obracuni/partner/{id}")
	@View("obracuniPartnera.jsp")
	public void obracuniPartnera(@PathParam("id") int partnerId, @QueryParam("od") String od,
			@QueryParam("do") String doParam) {
		try {
			Long vrijemeOd = null;
			Long vrijemeDo = null;

			if (od != null && !od.isEmpty()) {
				vrijemeOd = Long.parseLong(od);
			}
			if (doParam != null && !doParam.isEmpty()) {
				vrijemeDo = Long.parseLong(doParam);
			}

			var odgovor = this.servisTvrtka.getObracunPartner(partnerId, vrijemeOd, vrijemeDo);
			var status = odgovor.getStatus();

			if (status == 200) {
				var obracuni = odgovor.readEntity(new GenericType<List<Obracun>>() {
				});
				this.model.put("obracuni", obracuni);
			} else {
				this.model.put("greska", "Greška pri dohvaćanju obračuna partnera. Status: " + status);
			}

			this.model.put("partnerId", partnerId);
			this.model.put("od", od);
			this.model.put("do", doParam);
			this.model.put("status", status);

		} catch (Exception e) {
			this.model.put("greska", "Greška pri dohvaćanju obračuna partnera: " + e.getMessage());
		}
	}

	@GET
	@Path("admin/noviPartner")
	@View("noviPartner.jsp")
	public void noviPartner(@QueryParam("uspjeh") String uspjeh) {
		if ("true".equals(uspjeh)) {
			this.model.put("poruka", "Partner je uspješno dodan.");
			this.model.put("pogreska", false);
		}
	}

	@POST
	@Path("admin/noviPartner")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response dodajPartnera(@FormParam("naziv") String naziv, @FormParam("vrstaKuhinje") String vrstaKuhinje,
			@FormParam("adresa") String adresa, @FormParam("mreznaVrata") String mreznaVrataStr,
			@FormParam("mreznaVrataKraj") String mreznaVrataKrajStr, @FormParam("gpsSirina") String gpsSirinaStr,
			@FormParam("gpsDuzina") String gpsDuzinaStr, @FormParam("sigurnosniKod") String sigurnosniKod,
			@FormParam("adminKod") String adminKod) {
		try {
			if (naziv == null || naziv.trim().isEmpty() || adresa == null || adresa.trim().isEmpty()) {
				this.model.put("poruka", "Naziv i adresa su obavezni podaci.");
				this.model.put("pogreska", true);

				postaviVrijednostiModelaPartner(naziv, vrstaKuhinje, adresa, mreznaVrataStr, mreznaVrataKrajStr,
						gpsSirinaStr, gpsDuzinaStr, sigurnosniKod, adminKod);
				return Response.ok().build();
			}

			int mreznaVrata = 0;
			int mreznaVrataKraj = 0;
			float gpsSirina = 0.0f;
			float gpsDuzina = 0.0f;

			try {
				if (mreznaVrataStr != null && !mreznaVrataStr.trim().isEmpty()) {
					mreznaVrata = Integer.parseInt(mreznaVrataStr.trim());
				}
				if (mreznaVrataKrajStr != null && !mreznaVrataKrajStr.trim().isEmpty()) {
					mreznaVrataKraj = Integer.parseInt(mreznaVrataKrajStr.trim());
				}
				if (gpsSirinaStr != null && !gpsSirinaStr.trim().isEmpty()) {
					gpsSirina = Float.parseFloat(gpsSirinaStr.trim());
				}
				if (gpsDuzinaStr != null && !gpsDuzinaStr.trim().isEmpty()) {
					gpsDuzina = Float.parseFloat(gpsDuzinaStr.trim());
				}
			} catch (NumberFormatException e) {
				this.model.put("poruka", "Neispravne numeričke vrijednosti.");
				this.model.put("pogreska", true);
				postaviVrijednostiModelaPartner(naziv, vrstaKuhinje, adresa, mreznaVrataStr, mreznaVrataKrajStr,
						gpsSirinaStr, gpsDuzinaStr, sigurnosniKod, adminKod);
				return Response.ok().build();
			}

			int nextId = getNextPartnerId();

			Partner partner = new Partner(nextId, naziv.trim(), vrstaKuhinje != null ? vrstaKuhinje.trim() : "",
					adresa.trim(), mreznaVrata, mreznaVrataKraj, gpsSirina, gpsDuzina,
					sigurnosniKod != null ? sigurnosniKod.trim() : "", adminKod != null ? adminKod.trim() : "");

			var odgovor = this.servisTvrtka.postPartner(partner);
			var status = odgovor.getStatus();

			if (status == 201) {
				return Response.seeOther(URI.create("../mvc/tvrtka/admin/noviPartner?uspjeh=true")).build();
			} else if (status == 409) {
				this.model.put("poruka", "Partner već postoji ili je došlo do pogreške.");
				this.model.put("pogreska", true);
				postaviVrijednostiModelaPartner(naziv, vrstaKuhinje, adresa, mreznaVrataStr, mreznaVrataKrajStr,
						gpsSirinaStr, gpsDuzinaStr, sigurnosniKod, adminKod);
				return Response.ok().build();
			} else {
				this.model.put("poruka", "Greška pri dodavanju partnera. Status: " + status);
				this.model.put("pogreska", true);
				postaviVrijednostiModelaPartner(naziv, vrstaKuhinje, adresa, mreznaVrataStr, mreznaVrataKrajStr,
						gpsSirinaStr, gpsDuzinaStr, sigurnosniKod, adminKod);
				return Response.ok().build();
			}

		} catch (Exception e) {
			System.err.println("Exception occurred: " + e.getMessage());
			e.printStackTrace();
			this.model.put("poruka", "Greška pri dodavanju partnera: " + e.getMessage());
			this.model.put("pogreska", true);
			return Response.ok().build();
		}
	}

	@GET
	@Path("admin/spavanje")
	@View("spavanje.jsp")
	public void spavanje(@QueryParam("uspjeh") String uspjeh, @QueryParam("vrijeme") String vrijeme) {
		if ("true".equals(uspjeh) && vrijeme != null) {
			this.model.put("poruka", "Spavanje je uspješno aktivirano na " + vrijeme + " sekundi.");
			this.model.put("pogreska", false);
		}
	}

	@POST
	@Path("admin/spavanje")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response aktivirajSpavanje(@FormParam("vrijeme") String vrijemeStr) {
		try {
			if (vrijemeStr == null || vrijemeStr.trim().isEmpty()) {
				this.model.put("poruka", "Vrijeme spavanja je obavezno.");
				this.model.put("pogreska", true);
				this.model.put("vrijeme", vrijemeStr);
				return Response.ok().build();
			}

			int vrijeme;
			try {
				vrijeme = Integer.parseInt(vrijemeStr.trim());
				if (vrijeme <= 0) {
					this.model.put("poruka", "Vrijeme spavanja mora biti pozitivan broj.");
					this.model.put("pogreska", true);
					this.model.put("vrijeme", vrijemeStr);
					return Response.ok().build();
				}
			} catch (NumberFormatException e) {
				this.model.put("poruka", "Vrijeme spavanja mora biti valjani broj.");
				this.model.put("pogreska", true);
				this.model.put("vrijeme", vrijemeStr);
				return Response.ok().build();
			}

			var odgovor = this.servisTvrtka.getSpavanje(vrijeme);
			var status = odgovor.getStatus();

			if (status == 200) {
				return Response.seeOther(URI.create("../mvc/tvrtka/admin/spavanje?uspjeh=true&vrijeme=" + vrijeme))
						.build();
			} else {
				this.model.put("poruka", "Greška pri aktiviranju spavanja. Status: " + status);
				this.model.put("pogreska", true);
				this.model.put("vrijeme", vrijemeStr);
				return Response.ok().build();
			}

		} catch (Exception e) {
			System.err.println("Exception occurred: " + e.getMessage());
			e.printStackTrace();
			this.model.put("poruka", "Greška pri aktiviranju spavanja: " + e.getMessage());
			this.model.put("pogreska", true);
			this.model.put("vrijeme", vrijemeStr);
			return Response.ok().build();
		}
	}

	private void postaviVrijednostiModelaPartner(String naziv, String vrstaKuhinje, String adresa, String mreznaVrata,
			String mreznaVrataKraj, String gpsSirina, String gpsDuzina, String sigurnosniKod, String adminKod) {
		this.model.put("naziv", naziv);
		this.model.put("vrstaKuhinje", vrstaKuhinje);
		this.model.put("adresa", adresa);
		this.model.put("mreznaVrata", mreznaVrata);
		this.model.put("mreznaVrataKraj", mreznaVrataKraj);
		this.model.put("gpsSirina", gpsSirina);
		this.model.put("gpsDuzina", gpsDuzina);
		this.model.put("sigurnosniKod", sigurnosniKod);
		this.model.put("adminKod", adminKod);
	}

	private int getNextPartnerId() {
		try {
			var odgovor = this.servisTvrtka.getPartneri();
			if (odgovor.getStatus() == 200) {
				var partneri = odgovor.readEntity(new GenericType<List<Partner>>() {
				});

				if (partneri == null || partneri.isEmpty()) {
					return 1;
				}

				int maxId = partneri.stream().mapToInt(Partner::id).max().orElse(0);

				return maxId + 1;
			}
		} catch (Exception e) {
			System.err.println("Error getting partner count: " + e.getMessage());
		}

		return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
	}

	private void dohvatiStatuse() {
		this.model.put("samoOperacija", false);
		try {
			var statusT = this.servisTvrtka.headPosluzitelj().getStatus();
			this.model.put("statusT", statusT);
		} catch (Exception e) {
			this.model.put("statusT", 500);
		}

		try {
			var statusT1 = this.servisTvrtka.headPosluziteljStatus(1).getStatus();
			this.model.put("statusT1", statusT1);
		} catch (Exception e) {
			this.model.put("statusT1", 500);
		}

		try {
			var statusT2 = this.servisTvrtka.headPosluziteljStatus(2).getStatus();
			this.model.put("statusT2", statusT2);
		} catch (Exception e) {
			this.model.put("statusT2", 500);
		}
	}

}
