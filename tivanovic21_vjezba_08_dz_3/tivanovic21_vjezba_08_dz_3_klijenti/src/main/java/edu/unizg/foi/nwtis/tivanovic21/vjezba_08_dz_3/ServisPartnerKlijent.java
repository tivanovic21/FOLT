package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Narudzba;
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

@RegisterRestClient(configKey = "klijentPartner")
@Path("api/partner")
public interface ServisPartnerKlijent {
	@HEAD
	public Response headPosluzitelj();

	@Path("status/{id}")
	@HEAD
	public Response headPosluziteljStatus(@PathParam("id") int id);

	@Path("pauza/{id}")
	@HEAD
	public Response headPosluziteljPauza(@PathParam("id") int id);

	@Path("start/{id}")
	@HEAD
	public Response headPosluziteljStart(@PathParam("id") int id);

	@Path("kraj")
	@HEAD
	public Response headPosluziteljKraj();

	@Path("jelovnik")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getJelovnik(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka);

	@Path("kartapica")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getKartaPica(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka);
	
	@Path("narudzba")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getNarudzba(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka);

	@Path("korisnik")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getKorisnici();

	@Path("korisnik/{id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getKorisnik(@PathParam("id") String korIme);

	@Path("korisnik")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response postKorisnik(Korisnik korisnik);

	@Path("narudzba")
	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	public Response postNarudzba(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka);

	@Path("jelo")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response postJelo(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka,
			Narudzba narudzba);

	@Path("pice")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response postPice(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka,
			Narudzba narudzba);

	@Path("racun")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response postRacun(@HeaderParam("korisnik") String korisnik, @HeaderParam("lozinka") String lozinka);

	@Path("spava")
	@GET
	public Response getSpavanje(@QueryParam("vrijeme") int vrijeme);
}
