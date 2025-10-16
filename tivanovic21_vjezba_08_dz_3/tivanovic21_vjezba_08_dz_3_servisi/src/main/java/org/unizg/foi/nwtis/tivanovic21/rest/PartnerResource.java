package org.unizg.foi.nwtis.tivanovic21.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.dao.KorisnikDAO;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/partner")
public class PartnerResource {
	@Inject
	@ConfigProperty(name = "adresaPartner")
	private String partnerAdresa;

	@Inject
	@ConfigProperty(name = "mreznaVrataKrajPartner")
	private String mreznaVrataKrajPartner;

	@Inject
	@ConfigProperty(name = "mreznaVrataRadPartner")
	private String mreznaVrataRadPartner;

	@Inject
	@ConfigProperty(name = "kodZaAdminPartnera")
	private String kodZaAdminPartnera;

	@Inject
	@ConfigProperty(name = "idPartner")
	private String idPartner;

	@Inject
	@ConfigProperty(name = "kodZaKraj")
	private String kodZaKraj;

	@Inject
	@ConfigProperty(name = "urlBazaPodataka")
	private String urlBazaPodataka;

	@Inject
	@ConfigProperty(name = "korisnickoImeBazaPodataka")
	private String korisnickoImeBazaPodataka;

	@Inject
	@ConfigProperty(name = "lozinkaBazaPodataka")
	private String lozinkaBazaPodataka;

	@Inject
	@ConfigProperty(name = "upravljacBazaPodataka")
	private String upravljacBazaPodataka;

	@Inject
	RestConfiguration restConfiguration;

	@HEAD
	@Operation(summary = "Provjera statusa poslužitelja partner")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_headPartner", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPartner", description = "Vrijeme trajanja metode")
	public Response headPartner() {
		var status = posaljiKomandu("STATUS " + this.kodZaAdminPartnera + " " + Integer.parseInt(this.idPartner));
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("status/{id}")
	@HEAD
	@Operation(summary = "Provjera statusa dijela poslužitelja partner")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPartnerStatus", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPartnerStatus", description = "Vrijeme trajanja metode")
	public Response headPartnerStatus(@PathParam("id") int id) {
		var status = posaljiKomandu("STATUS " + this.kodZaAdminPartnera + " " + id);
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}

	@Path("pauza/{id}")
	@HEAD
	@Operation(summary = "Postavljanje dijela poslužitelja partner u pauzu")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPartnerPauza", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPartnerPauza", description = "Vrijeme trajanja metode")
	public Response headPartnerPauza(@PathParam("id") int id) {
		var status = posaljiKomandu("PAUZA " + this.kodZaAdminPartnera + " " + id);
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}

	@Path("start/{id}")
	@HEAD
	@Operation(summary = "Postavljanje dijela poslužitelja partner u rad")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPartnerStart", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPartnerStart", description = "Vrijeme trajanja metode")
	public Response headPartnerStart(@PathParam("id") int id) {
		var status = posaljiKomandu("START " + this.kodZaAdminPartnera + " " + id);
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}

	@Path("kraj")
	@HEAD
	@Operation(summary = "Zaustavljanje poslužitelja partner")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "204", description = "Pogrešna operacija") })
	@Counted(name = "brojZahtjeva_headPartnerKraj", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_headPartnerKraj", description = "Vrijeme trajanja metode")
	public Response headPartnerKraj() {
		var status = posaljiKomandu("KRAJ " + this.kodZaKraj);
		if (status != null && status.startsWith("OK")) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.NO_CONTENT).build();
		}
	}

	@GET
	@Path("jelovnik")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dohvaćanje jelovnika iz partnera")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "401", description = "Neispravna autentikacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getPartnerJelovnik", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getPartnerJelovnik", description = "Vrijeme trajanja metode")
	public Response getPartnerJelovnik(@HeaderParam("korisnik") String korisnik,
			@HeaderParam("lozinka") String lozinka) {

		if (!provjeriAutentikaciju(korisnik, lozinka)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		var jelovnik = posaljiKomanduZaJson("JELOVNIK " + korisnik, Integer.parseInt(this.mreznaVrataRadPartner));
		if (jelovnik != null && !jelovnik.startsWith("ERROR")) {
			return Response.status(Response.Status.OK).entity(jelovnik).build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("kartapica")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dohvaćanje karte pića iz partnera")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "401", description = "Neispravna autentikacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getPartnerKartapica", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getPartnerKartapica", description = "Vrijeme trajanja metode")
	public Response getPartnerKartapica(@HeaderParam("korisnik") String korisnik,
			@HeaderParam("lozinka") String lozinka) {

		if (!provjeriAutentikaciju(korisnik, lozinka)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		var kartapica = posaljiKomanduZaJson("KARTAPIĆA " + korisnik, Integer.parseInt(this.mreznaVrataRadPartner));
		if (kartapica != null && !kartapica.startsWith("ERROR")) {
			return Response.status(Response.Status.OK).entity(kartapica).build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("narudzba")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dohvaćanje narudžbi iz partnera")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "401", description = "Neispravna autentikacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getPartnerNarudzba", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getPartnerNarudzba", description = "Vrijeme trajanja metode")
	public Response getPartnerNarudzba(@HeaderParam("korisnik") String korisnik,
			@HeaderParam("lozinka") String lozinka) {

		if (!provjeriAutentikaciju(korisnik, lozinka)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		var narudzba = posaljiKomanduZaJson("STANJE " + korisnik, Integer.parseInt(this.mreznaVrataRadPartner));
		if (narudzba != null && !narudzba.startsWith("ERROR")) {
			return Response.status(Response.Status.OK).entity(narudzba).build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("narudzba")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dohvaćanje narudžbi iz partnera")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "401", description = "Neispravna autentikacija"),
			@APIResponse(responseCode = "409", description = "Već postoji narudžba za korisnika"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getPartnerNarudzba", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getPartnerNarudzba", description = "Vrijeme trajanja metode")
	public Response postPartnerNarudzba(@HeaderParam("korisnik") String korisnik,
			@HeaderParam("lozinka") String lozinka) {

		if (!provjeriAutentikaciju(korisnik, lozinka)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		var odgovor = posaljiKomandu("NARUDŽBA " + korisnik, Integer.parseInt(this.mreznaVrataRadPartner));
		if (odgovor != null) {
			if (odgovor.equals("OK")) {
				return Response.status(Response.Status.CREATED).build();
			} else if (odgovor.contains("ERROR 44")) {
				return Response.status(Response.Status.CONFLICT).build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("jelo")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dodavanje jela u narudžbu")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Jelo uspješno dodano"),
			@APIResponse(responseCode = "401", description = "Neispravna autentikacija"),
			@APIResponse(responseCode = "409", description = "Nema otvorene narudžbe ili jelo ne postoji"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_postPartnerJelo", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postPartnerJelo", description = "Vrijeme trajanja metode")
	public Response postPartnerJelo(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka,
			Narudzba narudzba) {

		if (!provjeriAutentikaciju(korisnik, lozinka)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		if (narudzba == null || narudzba.id() == null || narudzba.id().trim().isEmpty() || narudzba.kolicina() <= 0) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		var odgovor = posaljiKomandu("JELO " + korisnik + " " + narudzba.id() + " " + narudzba.kolicina(),
				Integer.parseInt(this.mreznaVrataRadPartner));

		if (odgovor != null) {
			if (odgovor.equals("OK")) {
				return Response.status(Response.Status.CREATED).build();
			} else if (odgovor.contains("ERROR 41") || odgovor.contains("ERROR 43")) {
				return Response.status(Response.Status.CONFLICT).build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("pice")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dodavanje jela u narudžbu")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Jelo uspješno dodano"),
			@APIResponse(responseCode = "401", description = "Neispravna autentikacija"),
			@APIResponse(responseCode = "409", description = "Nema otvorene narudžbe ili jelo ne postoji"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_postPartnerJelo", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postPartnerJelo", description = "Vrijeme trajanja metode")
	public Response postPartnerPice(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka,
			Narudzba narudzba) {

		if (!provjeriAutentikaciju(korisnik, lozinka)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		if (narudzba == null || narudzba.id() == null || narudzba.id().trim().isEmpty() || narudzba.kolicina() <= 0) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		var odgovor = posaljiKomandu("PIĆE " + korisnik + " " + narudzba.id() + " " + narudzba.kolicina(),
				Integer.parseInt(this.mreznaVrataRadPartner));

		if (odgovor != null) {
			if (odgovor.equals("OK")) {
				return Response.status(Response.Status.CREATED).build();
			} else if (odgovor.contains("ERROR 41") || odgovor.contains("ERROR 43")) {
				return Response.status(Response.Status.CONFLICT).build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("racun")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Zahtjev za račun otvorene narudžbe")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Račun uspješno kreiran"),
			@APIResponse(responseCode = "401", description = "Neispravna autentikacija"),
			@APIResponse(responseCode = "409", description = "Nema otvorene narudžbe"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_postPartnerRacun", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postPartnerRacun", description = "Vrijeme trajanja metode")
	public Response postPartnerRacun(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka) {

		if (!provjeriAutentikaciju(korisnik, lozinka)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		var odgovor = posaljiKomanduRacun("RAČUN " + korisnik, Integer.parseInt(this.mreznaVrataRadPartner));

		if (odgovor != null) {
			if (odgovor.equals("OK")) {
				return Response.status(Response.Status.CREATED).build();
			} else if (!odgovor.startsWith("ERROR")) {
				return Response.status(Response.Status.CREATED).entity(odgovor).build();
			} else if (odgovor.contains("ERROR 43")) {
				return Response.status(Response.Status.CONFLICT).build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("korisnik")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dohvaćanje liste korisnika")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getKorisnici", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getKorisnici", description = "Vrijeme trajanja metode")
	public Response getKorisnici() {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var korisnikDAO = new KorisnikDAO(vezaBP);
			var korisnici = korisnikDAO.dohvatiSve();
			return Response.ok(korisnici).status(Response.Status.OK).build();
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
					"Greška pri dohvaćanju korisnika: " + e.getMessage(), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("korisnik/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dohvaćanje korisnika prema id")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Uspješna operacija"),
			@APIResponse(responseCode = "404", description = "Korisnik nije pronađen"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_getKorisnik", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_getKorisnik", description = "Vrijeme trajanja metode")
	public Response getKorisnik(@PathParam("id") String korisnickoIme) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var korisnikDAO = new KorisnikDAO(vezaBP);
			var korisnik = korisnikDAO.dohvati(korisnickoIme, null, false);
			if (korisnik != null) {
				return Response.ok(korisnik).status(Response.Status.OK).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
					"Greška pri dohvaćanju korisnika: " + e.getMessage(), e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("korisnik")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Dodavanje novog korisnika")
	@APIResponses(value = { @APIResponse(responseCode = "201", description = "Korisnik uspješno kreiran"),
			@APIResponse(responseCode = "409", description = "Korisnik već postoji"),
			@APIResponse(responseCode = "500", description = "Interna pogreška") })
	@Counted(name = "brojZahtjeva_postKorisnik", description = "Koliko puta je pozvana operacija servisa")
	@Timed(name = "trajanjeMetode_postKorisnik", description = "Vrijeme trajanja metode")
	public Response postKorisnik(Korisnik korisnik) {
		try (var vezaBP = this.restConfiguration.dajVezu()) {
			var korisnikDAO = new KorisnikDAO(vezaBP);

			var postojeciKorisnik = korisnikDAO.dohvati(korisnik.korisnik(), null, false);
			if (postojeciKorisnik != null) {
				return Response.status(Response.Status.CONFLICT).build();
			}

			boolean status = korisnikDAO.dodaj(korisnik);
			if (status) {
				return Response.status(Response.Status.CREATED).entity(korisnik).build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
					"Greška pri dodavanju korisnika: " + e.getMessage(), e);
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
			var status = posaljiKomandu("SPAVA " + this.kodZaAdminPartnera + " " + trajanje);
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
			int port = Integer.parseInt(this.mreznaVrataKrajPartner);
			return posaljiKomandu(komanda, port);
		} catch (NumberFormatException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
					"Greška pri parsiranju porta: " + e.getMessage(), e);
			return null;
		}
	}

	private String posaljiKomandu(String komanda, int port) {
		try (Socket mreznaUticnica = new Socket(this.partnerAdresa, port);
				BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"))) {

			out.write(komanda + "\n");
			out.flush();
			mreznaUticnica.shutdownOutput();

			var linija = in.readLine();
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, komanda + " -> " + linija);

			mreznaUticnica.shutdownInput();
			return linija;

		} catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
					"Greška u komunikaciji s partnerom: " + e.getMessage(), e);
			return null;
		}
	}

	private String posaljiKomanduZaJson(String komanda, int port) {
		try (Socket mreznaUticnica = new Socket(this.partnerAdresa, port);
				BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"))) {

			out.write(komanda + "\n");
			out.flush();
			mreznaUticnica.shutdownOutput();

			var statusLine = in.readLine();
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, komanda + " status -> " + statusLine);

			if (statusLine != null && statusLine.equals("OK")) {
				var jsonLine = in.readLine();
				Logger.getLogger(this.getClass().getName()).log(Level.INFO, komanda + " json -> " + jsonLine);
				mreznaUticnica.shutdownInput();
				return jsonLine;
			} else {
				mreznaUticnica.shutdownInput();
				return statusLine;
			}

		} catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
					"Greška u komunikaciji s partnerom: " + e.getMessage(), e);
			return null;
		}
	}

	private String posaljiKomanduRacun(String komanda, int port) {
		try (Socket mreznaUticnica = new Socket(this.partnerAdresa, port);
				BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"))) {

			out.write(komanda + "\n");
			out.flush();
			mreznaUticnica.shutdownOutput();

			var statusLine = in.readLine();
			Logger.getLogger(this.getClass().getName()).log(Level.INFO, komanda + " status -> " + statusLine);

			if (statusLine != null && statusLine.equals("OK")) {
				var jsonLine = in.readLine();
				if (jsonLine != null && !jsonLine.trim().isEmpty()) {
					Logger.getLogger(this.getClass().getName()).log(Level.INFO, komanda + " json -> " + jsonLine);
					mreznaUticnica.shutdownInput();
					return jsonLine;
				} else {
					Logger.getLogger(this.getClass().getName()).log(Level.INFO,
							komanda + " -> OK bez JSON podataka (kvota nije dosegnuta)");
					mreznaUticnica.shutdownInput();
					return "OK";
				}
			} else {
				mreznaUticnica.shutdownInput();
				return statusLine;
			}

		} catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
					"Greška u komunikaciji s partnerom: " + e.getMessage(), e);
			return null;
		}
	}

	private boolean provjeriAutentikaciju(String korisnik, String lozinka) {
		if (korisnik == null || lozinka == null || korisnik.trim().isEmpty() || lozinka.trim().isEmpty()) {
			return false;
		}

		try {
			Class.forName(this.upravljacBazaPodataka);
			try (Connection connection = DriverManager.getConnection(this.urlBazaPodataka,
					this.korisnickoImeBazaPodataka, this.lozinkaBazaPodataka)) {

				String sql = "SELECT COUNT(*) FROM korisnici WHERE LOWER(KORISNIK) = LOWER(?) AND LOZINKA = ?";
				try (PreparedStatement stmt = connection.prepareStatement(sql)) {
					stmt.setString(1, korisnik.toLowerCase());
					stmt.setString(2, lozinka);

					try (ResultSet rs = stmt.executeQuery()) {
						if (rs.next()) {
							return rs.getInt(1) > 0;
						}
					}
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
					"Greška pri provjeri autentikacije: " + e.getMessage(), e);
		}

		return false;
	}
}
