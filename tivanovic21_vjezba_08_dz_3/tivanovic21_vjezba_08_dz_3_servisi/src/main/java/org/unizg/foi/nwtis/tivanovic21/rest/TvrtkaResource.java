package org.unizg.foi.nwtis.tivanovic21.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.PartnerPopis;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.dao.ObracunDAO;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.dao.PartnerDAO;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/tvrtka")
public class TvrtkaResource {

	@Inject
	@ConfigProperty(name = "adresa")
	private String tvrtkaAdresa;
	@Inject
	@ConfigProperty(name = "mreznaVrataKraj")
	private String mreznaVrataKraj;
	@Inject
	@ConfigProperty(name = "mreznaVrataRegistracija")
	private String mreznaVrataRegistracija;
	@Inject
	@ConfigProperty(name = "mreznaVrataRad")
	private String mreznaVrataRad;
	@Inject
	@ConfigProperty(name = "kodZaAdminTvrtke")
	private String kodZaAdminTvrtke;
	@Inject
	@ConfigProperty(name = "kodZaKraj")
	private String kodZaKraj;
	@Inject
	@ConfigProperty(name = "klijentTvrtkaInfo/mp-rest/url")
	private String klijentTvrtkaInfoRestUrl;

	private String apiTvrtkaKrajInfo = "api/tvrtka/kraj/info";
	private String apiTvrtkaObracunInfo = "api/tvrtka/obracun/ws";

	@Inject
	RestConfiguration restConfiguration;

	@HEAD
	@Operation(summary = "Provjera statusa poslužitelja tvrtka")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluzitelj", description = "Vrijeme trajanja metode")
	public Response headPosluzitelj() {
		var status = posaljiKomandu("KRAJ xxx");
		if (status != null) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.CONFLICT).build();
		}
	}

	@Path("status/{id}")
	@HEAD
	@Operation(summary = "Provjera statusa dijela poslužitelja tvrtka")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_eadPosluziteljStatus", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_eadPosluziteljStatus", description = "Vrijeme trajanja metode")
	public Response headPosluziteljStatus(@PathParam("id") int id) {
		var status = posaljiKomandu("STATUS " + this.kodZaAdminTvrtke + " " + id);
		if (status != null) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.CONFLICT).build();
		}
	}

	@Path("pauza/{id}")
	@HEAD
	@Operation(summary = "Postavljanje dijela poslužitelja tvrtka u pauzu")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPosluziteljPauza", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluziteljPauza", description = "Vrijeme trajanja metode")
	public Response headPosluziteljPauza(@PathParam("id") int id) {
		var status = posaljiKomandu("PAUZA " + this.kodZaAdminTvrtke + " " + id);
		System.out.println("STATUS PAUZE: " + status);
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}

	@Path("start/{id}")
	@HEAD
	@Operation(summary = "Postavljanje dijela poslužitelja tvrtka u rad")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPosluziteljStart", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluziteljStart", description = "Vrijeme trajanja metode")
	public Response headPosluziteljStart(@PathParam("id") int id) {
		var status = posaljiKomandu("START " + this.kodZaAdminTvrtke + " " + id);
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}

	@Path("kraj")
	@HEAD
	@Operation(summary = "Zaustavljanje poslužitelja tvrtka")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPosluziteljKraj", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluziteljKraj", description = "Vrijeme trajanja metode")
	public Response headPosluziteljKraj() {
		var status = posaljiKomandu("KRAJWS " + this.kodZaKraj);
		if (status != null) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}

	@Path("kraj/info")
	@HEAD
	@Operation(summary = "Informacija o zaustavljanju poslužitelja tvrtka")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPosluziteljKrajInfo", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
	public Response headPosluziteljKrajInfo() {
		var status = posaljiKomandu("KRAJ xxx");
//		Client client = ClientBuilder.newClient();
//
//		try {
//			var res = client.target(this.klijentTvrtkaInfoRestUrl).path(this.apiTvrtkaKrajInfo).request().get();
//			System.out.println("status: " + res.getStatus());
//		} catch (Exception e) {
//			System.out.println("greška prilikom slanja zahtjeva na klijenti rest api: " + e.getMessage());
//		} finally {
//			client.close();
//		}

		if (status == null) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}

	@Path("partner")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat svih partnera")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getPartneri", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getPartneri", description = "Vrijeme trajanja metode")
	public Response getPartneri() {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var partnerDAO = new PartnerDAO(vezaBP);
			var partneri = partnerDAO.dohvatiSve(true);
			return Response.ok(partneri).status(Response.Status.OK).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("partner/{id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat jednog partnera")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "404", description = "Ne postoji resurs"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getPartner", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getPartner", description = "Vrijeme trajanja metode")
	public Response getPartner(@PathParam("id") int id) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var partnerDAO = new PartnerDAO(vezaBP);
			var partner = partnerDAO.dohvati(id, false);
			if (partner != null) {
				return Response.ok(partner).status(Response.Status.OK).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("partner/provjera")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat partnera koji su i u bazi i na poslužitelju")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getPartneriProvrjera", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getPartneriProvrjera", description = "Vrijeme trajanja metode")
	public Response getPartneriProvrjera() {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var partnerDAO = new PartnerDAO(vezaBP);
			var partneriDB = partnerDAO.dohvatiSve(false);

			var jsonResponse = posaljiKomanduZaJson("POPIS", Integer.parseInt(this.mreznaVrataRegistracija));
			if (jsonResponse != null && !jsonResponse.startsWith("ERROR")) {

				try {
					Gson gson = new Gson();
					PartnerPopis[] partneriArray = gson.fromJson(jsonResponse, PartnerPopis[].class);

					List<PartnerPopis> partneriServer = Arrays.asList(partneriArray);

					Set<Integer> serverIds = partneriServer.stream().map(PartnerPopis::id).collect(Collectors.toSet());

					List<Partner> filtriraniPartneri = partneriDB.stream().filter(p -> {
						return serverIds.contains(p.id());
					}).collect(Collectors.toList());

					return Response.ok(filtriraniPartneri).build();

				} catch (Exception parseException) {
					parseException.printStackTrace();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
				}
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("partner")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dodavanje novog partnera")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
			@APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_postPartner", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postPartner", description = "Vrijeme trajanja metode")
	public Response postPartner(Partner partner) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var partnerDAO = new PartnerDAO(vezaBP);
			var status = partnerDAO.dodaj(partner);
			if (status) {
				return Response.status(Response.Status.CREATED).build();
			} else {
				return Response.status(Response.Status.CONFLICT).build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("jelovnik")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat svih jelovnika")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getJelovnici", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getJelovnici", description = "Vrijeme trajanja metode")
	public Response getJelovnici() {
		JsonArray mergedJelovnici = new JsonArray();
		Gson gson = new Gson();

		try (var vezaBP = restConfiguration.dajVezu()) {
			PartnerDAO dao = new PartnerDAO(vezaBP);
			List<Partner> partneriDB = dao.dohvatiSve(false);

			String jsonResponse = posaljiKomanduZaJson("POPIS", Integer.parseInt(this.mreznaVrataRegistracija));
			if (jsonResponse == null || jsonResponse.startsWith("ERROR")) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}

			try {
				PartnerPopis[] partneriArray = gson.fromJson(jsonResponse, PartnerPopis[].class);
				List<PartnerPopis> partneriServer = Arrays.asList(partneriArray);
				Set<Integer> serverIds = partneriServer.stream().map(PartnerPopis::id).collect(Collectors.toSet());

				List<Partner> filtriraniPartneri = partneriDB.stream().filter(p -> serverIds.contains(p.id()))
						.collect(Collectors.toList());

				for (Partner p : filtriraniPartneri) {
					String sigurnosniKod = Integer.toHexString((p.naziv() + p.adresa()).hashCode());
					String cmd = String.format("JELOVNIK %d %s", p.id(), sigurnosniKod);

					String response = posaljiKomanduZaJson(cmd, Integer.parseInt(this.mreznaVrataRad));
					if (response != null && !response.startsWith("ERROR")) {
						try {
							JsonElement elem = JsonParser.parseString(response);
							if (elem.isJsonArray()) {
								elem.getAsJsonArray().forEach(mergedJelovnici::add);
							}
						} catch (Exception ex) {
							Logger.getLogger(getClass().getName()).log(Level.WARNING, "Nepravilan JSON " + p.id(), ex);
						}
					} else {
						Logger.getLogger(getClass().getName()).log(Level.WARNING,
								"Error getting jelovnik for partner " + p.id() + ": " + response);
					}
				}

				return Response.ok(gson.toJson(mergedJelovnici)).build();

			} catch (Exception parseException) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error parsing server response",
						parseException);
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}

		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Internal error", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("jelovnik/{id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat jelovnika po ID-u")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "404", description = "Ne postoji resurs"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getJelovnik", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getJelovnik", description = "Vrijeme trajanja metode")
	public Response getJelovnik(@PathParam("id") int id) {
		Gson gson = new Gson();

		try (var vezaBP = this.restConfiguration.dajVezu()) {
			PartnerDAO partnerDAO = new PartnerDAO(vezaBP);
			Partner partner = partnerDAO.dohvati(id, false);

			if (partner == null) {
				return Response.status(Response.Status.NOT_FOUND).build();
			}

			String sigurnosniKod = Integer.toHexString((partner.naziv() + partner.adresa()).hashCode());
			String cmd = String.format("JELOVNIK %d %s", partner.id(), sigurnosniKod);

			String response = posaljiKomanduZaJson(cmd, Integer.parseInt(this.mreznaVrataRad));
			if (response != null && !response.startsWith("ERROR")) {
				try {
					JsonElement elem = JsonParser.parseString(response);
					return Response.ok(gson.toJson(elem)).build();
				} catch (Exception ex) {
					Logger.getLogger(getClass().getName()).log(Level.WARNING,
							"Nepravilan JSON za partnera " + partner.id(), ex);
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
				}
			} else {
				Logger.getLogger(getClass().getName()).log(Level.WARNING,
						"Greška prilikom dohvaćanja jelovnika za partnera " + partner.id() + ": " + response);
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}

		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Interna greška", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("kartapica")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat karte pića")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getKartaPica", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getKartaPica", description = "Vrijeme trajanja metode")
	public Response getKartaPica() {
		JsonArray mergedKartaPica = new JsonArray();
		Gson gson = new Gson();

		try (var vezaBP = restConfiguration.dajVezu()) {
			PartnerDAO dao = new PartnerDAO(vezaBP);
			List<Partner> partneriDB = dao.dohvatiSve(false);

			String jsonResponse = posaljiKomanduZaJson("POPIS", Integer.parseInt(this.mreznaVrataRegistracija));
			if (jsonResponse == null || jsonResponse.startsWith("ERROR")) {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}

			PartnerPopis[] partneriArray = gson.fromJson(jsonResponse, PartnerPopis[].class);
			Set<Integer> serverIds = Arrays.stream(partneriArray).map(PartnerPopis::id).collect(Collectors.toSet());

			List<Partner> filtrirani = partneriDB.stream().filter(p -> serverIds.contains(p.id()))
					.collect(Collectors.toList());

			for (Partner p : filtrirani) {
				String sigurnosniKod = Integer.toHexString((p.naziv() + p.adresa()).hashCode());
				String cmd = String.format("KARTAPIĆA %d %s", p.id(), sigurnosniKod);

				String response = posaljiKomanduZaJson(cmd, Integer.parseInt(this.mreznaVrataRad));
				if (response != null && !response.startsWith("ERROR")) {
					try {
						JsonElement elem = JsonParser.parseString(response);
						if (elem.isJsonArray()) {
							elem.getAsJsonArray().forEach(mergedKartaPica::add);
						}
					} catch (JsonSyntaxException ex) {
						Logger.getLogger(getClass().getName()).log(Level.WARNING,
								"Nepravilan JSON za partnera " + p.id(), ex);
					}
				} else {
					Logger.getLogger(getClass().getName()).log(Level.WARNING,
							"Greška prilikom dohvaćanja karte pića za partnera " + p.id() + ": " + response);
				}
			}

			return Response.ok(gson.toJson(mergedKartaPica)).build();

		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Interna greška", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("obracun")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat svih obračuna")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getObracuni", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getObracuni", description = "Vrijeme trajanja metode")
	public Response getObracuni(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var obracunDAO = new ObracunDAO(vezaBP);
			List<Obracun> obracuni;

			if (vrijemeOd != null || vrijemeDo != null) {
				obracuni = obracunDAO.dohvatiPoVremenu(vrijemeOd, vrijemeDo);
			} else {
				obracuni = obracunDAO.dohvatiSve();
			}

			return Response.ok(obracuni).status(Response.Status.OK).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("obracun/jelo")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat obračuna za jela")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getObracuniJelo", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getObracuniJelo", description = "Vrijeme trajanja metode")
	public Response getObracuniJelo(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var obracunDAO = new ObracunDAO(vezaBP);
			List<Obracun> obracuni;

			if (vrijemeOd != null || vrijemeDo != null) {
				obracuni = obracunDAO.dohvatiJelaPoVremenu(vrijemeOd, vrijemeDo);
			} else {
				obracuni = obracunDAO.dohvatiJela();
			}

			return Response.ok(obracuni).status(Response.Status.OK).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("obracun/pice")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat obračuna za piće")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getObracuniPice", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getObracuniPice", description = "Vrijeme trajanja metode")
	public Response getObracuniPice(@QueryParam("od") Long vrijemeOd, @QueryParam("do") Long vrijemeDo) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var obracunDAO = new ObracunDAO(vezaBP);
			List<Obracun> obracuni;

			if (vrijemeOd != null || vrijemeDo != null) {
				obracuni = obracunDAO.dohvatiPicePoVremenu(vrijemeOd, vrijemeDo);
			} else {
				obracuni = obracunDAO.dohvatiPice();
			}

			return Response.ok(obracuni).status(Response.Status.OK).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("obracun/{id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dohvat obračuna za partnera")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getObracunPartner", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getObracunPartner", description = "Vrijeme trajanja metode")
	public Response getObracunPartner(@PathParam("id") int partnerId, @QueryParam("od") Long vrijemeOd,
			@QueryParam("do") Long vrijemeDo) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var obracunDAO = new ObracunDAO(vezaBP);
			List<Obracun> obracuni;

			if (vrijemeOd != null || vrijemeDo != null) {
				obracuni = obracunDAO.dohvatiPoPartneruIVremenu(partnerId, vrijemeOd, vrijemeDo);
			} else {
				obracuni = obracunDAO.dohvatiPoPartneru(partnerId);
			}

			return Response.ok(obracuni).status(Response.Status.OK).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("obracun")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dodavanje novog obračuna")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Uspješno kreiran resurs"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_postObracun", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postObracun", description = "Vrijeme trajanja metode")
	public Response postObracun(List<Obracun> obracuni) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var obracunDAO = new ObracunDAO(vezaBP);

			for (Obracun obracun : obracuni) {
				obracunDAO.dodaj(obracun);
			}

//			Client client = ClientBuilder.newClient();
//
//			try {
//				var res = client.target(this.klijentTvrtkaInfoRestUrl).path(this.apiTvrtkaObracunInfo).request().get();
//				System.out.println("status: " + res.getStatus());
//			} catch (Exception e) {
//				System.out.println("greška prilikom slanja zahtjeva na klijenti rest api: " + e.getMessage());
//			} finally {
//				client.close();
//			}

			return Response.status(Response.Status.CREATED).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("obracun/ws")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Operation(summary = "Dodavanje novog obračuna i slanje na poslužitelj")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Uspješno kreiran resurs"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_postObracunWS", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postObracunWS", description = "Vrijeme trajanja metode")
	public Response postObracunWS(List<Obracun> obracuni) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var obracunDAO = new ObracunDAO(vezaBP);

			for (Obracun obracun : obracuni) {
				obracunDAO.dodaj(obracun);
			}

			if (!obracuni.isEmpty()) {
				int partnerId = obracuni.get(0).partner();

				var partnerDAO = new PartnerDAO(vezaBP);
				var partner = partnerDAO.dohvati(partnerId, false);

				if (partner != null) {
					String sigurnosniKod = Integer.toHexString((partner.naziv() + partner.adresa()).hashCode());

					Gson gson = new Gson();
					String jsonData = gson.toJson(obracuni);

					String response = posaljiKomanduObracun(partnerId, sigurnosniKod, jsonData);

					if (response != null && response.equals("OK")) {
						return Response.status(Response.Status.CREATED).build();
					} else {
						return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
					}
				}
			}

			return Response.status(Response.Status.CREATED).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("spava")
	@GET
	@Operation(summary = "Spavanje dretve")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getSpava", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getSpava", description = "Vrijeme trajanja metode")
	public Response getSpava(@QueryParam("vrijeme") int trajanje) {
		try {
			var status = posaljiKomandu("SPAVA " + this.kodZaAdminTvrtke + " " + trajanje);
			if (status != null && status.equals("OK")) {
				return Response.status(Response.Status.OK).build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	private String posaljiKomandu(String komanda) {
		try {
			int defaultPort = Integer.parseInt(this.mreznaVrataKraj);
			return posaljiKomandu(komanda, defaultPort);
		} catch (NumberFormatException e) {
			System.out.println("Error pri parsiranju porta: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private String posaljiKomandu(String komanda, int port) {
		try (Socket mreznaUticnica = otvoriSocket(this.tvrtkaAdresa, port);
				BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"))) {

			out.write(komanda + "\n");
			out.flush();
			mreznaUticnica.shutdownOutput();

			String linija = in.readLine();
			System.out.println(komanda + " -> " + linija);

			mreznaUticnica.shutdownInput();
			return linija;

		} catch (SocketTimeoutException e) {
			System.out.println("Timeout while connecting or reading from " + this.tvrtkaAdresa + ":" + port);
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
		}
		return null;
	}

	private String posaljiKomanduZaJson(String komanda, int port) {
		try {
			Socket mreznaUticnica = otvoriSocket(this.tvrtkaAdresa, port);
			BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
			out.write(komanda + "\n");
			out.flush();
			mreznaUticnica.shutdownOutput();

			var statusLine = in.readLine();
			System.out.println(komanda + " status -> " + statusLine);

			if (statusLine != null && statusLine.equals("OK")) {
				var jsonLine = in.readLine();
				System.out.println(komanda + " json -> " + jsonLine);
				mreznaUticnica.shutdownInput();
				mreznaUticnica.close();
				return jsonLine;
			} else {
				mreznaUticnica.shutdownInput();
				mreznaUticnica.close();
				return statusLine;
			}
		} catch (IOException e) {
			System.out.println("Error u posaljiKomanduZaJson: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private String posaljiKomanduObracun(int partnerId, String sigurnosniKod, String jsonData) {
		try {
			Socket mreznaUticnica = otvoriSocket(this.tvrtkaAdresa, Integer.parseInt(this.mreznaVrataRad));
			BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

			out.write(String.format("OBRAČUNWS %d %s\n", partnerId, sigurnosniKod));
			out.write(jsonData + "\n");
			out.flush();

			mreznaUticnica.shutdownOutput();
			var linija = in.readLine();
			System.out.println("OBRAČUNWS -> " + linija);
			mreznaUticnica.shutdownInput();
			mreznaUticnica.close();
			return linija;
		} catch (IOException e) {
			System.out.println("Error u posaljiKomanduObracun: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private Socket otvoriSocket(String adresa, int port) throws IOException {
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress(adresa, port), 10000);
		socket.setSoTimeout(15000);
		return socket;
	}

}
