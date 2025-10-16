package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;


public class KorisnikKupac {

	/** Konfiguracijski podaci */
	private Konfiguracija konfig;

	/** Mrezna vrata za registraciju partnera */
	private int mreznaVrataRegistracija = 0;

	/** Id partnera */
	private int partnerId = 0;

	private static final String REGEX_CSV_DELIMITER = ";";
	private static final String REGEX_RAZMAK_CMD = "\\s+";
	private static final Pattern PATTERN_CSV_DATOTEKA = Pattern.compile(".*\\.csv$");
	private static final Pattern PATTERN_JSON_KOMANDE = Pattern.compile("^(JELOVNIK|KARTAPIĆA).*");
    private static final Pattern PATTERN_DOZVOLJENE_KOMANDE =
            Pattern.compile("^(RAČUN|JELOVNIK|JELO|PIĆE|KARTAPIĆA|NARUDŽBA)(\\b.*)?$");


	public static void main(String[] args) {
		if (args.length < 1 || args.length > 2) {
			System.out.println("Neispravan bropj argumenata");
			return;
		}

		var program = new KorisnikKupac();
		program.pripremiKreni(args);
	}

	/**
	 * Priprema program za pokretanje
	 * 
	 * @param args
	 */
	private void pripremiKreni(String[] args) {
		if (!ucitajKonfiguraciju(args[0])) {
			return;
		}

		try {
			String csvDatoteka = args[1];
			if (!PATTERN_CSV_DATOTEKA.matcher(csvDatoteka).matches()) {
				System.out.println("Neispravna datoteka");
				return;
			}
			this.obradiDatoteku(csvDatoteka);
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * Ucitaj konfiguraciju.
	 *
	 * @param nazivDatoteke naziv datoteke
	 * @return true, ako je uspješno učitavanje konfiguracije
	 */
	private boolean ucitajKonfiguraciju(String nazivDatoteke) {
		try {
			this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
			this.ucitajKonfiguracijskePodatke();
			return true;
		} catch (NeispravnaKonfiguracija ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}

	/**
	 * Učitava konfiguracijske podatke
	 */
	private void ucitajKonfiguracijskePodatke() {
		this.mreznaVrataRegistracija = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));
		this.partnerId = Integer.parseInt(this.konfig.dajPostavku("partner"));
	}

	/**
	 * Učitava csv datoteku
	 * 
	 * @param nazivDatoteke
	 */
	private void obradiDatoteku(String nazivDatoteke) {
		Path putanja = Path.of(nazivDatoteke);
		if (!Files.exists(putanja) || !Files.isRegularFile(putanja) || !Files.isReadable(putanja)) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Datoteka {0} ne postoji ili nije čitljiva",
					nazivDatoteke);
			return;
		}

		try (BufferedReader br = Files.newBufferedReader(putanja, StandardCharsets.UTF_8)) {
			String linija;
			while ((linija = br.readLine()) != null) {
				if (linija.isBlank() || linija.isEmpty())
					continue;
				this.obradiLinijuDatoteke(linija);
			}
		} catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Greška pri čitanju datoteke {0}",
					nazivDatoteke);
		}
	}

	/**
	 * Obrađuje pojedinačnu liniju iz csv datoteke
	 * 
	 * @param linija
	 */
	private void obradiLinijuDatoteke(String linija) {
		try {
			String[] dijelovi = linija.split(REGEX_CSV_DELIMITER);
			String korisnik = dijelovi[0];
			String adresa = dijelovi[1];
			int mreznaVrata = Integer.parseInt(dijelovi[2]);
			int spavanje = Integer.parseInt(dijelovi[3]);
			String komanda = dijelovi[4];
			
			if (!PATTERN_DOZVOLJENE_KOMANDE.matcher(komanda).matches()) {
				return;
			}

			String[] komandaDijelovi = komanda.split(REGEX_RAZMAK_CMD);

			if (komandaDijelovi.length > 1 && !komandaDijelovi[1].equals(korisnik)) {
				return;
			}

			Thread.sleep(spavanje);
			posaljiKomandu(adresa, mreznaVrata, komanda);
		} catch (NumberFormatException | InterruptedException | IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * Šalje komandu kupca prema partneru
	 * 
	 * @param adresa
	 * @param mreznaVrata
	 * @param komanda
	 */
	private void posaljiKomandu(String adresa, int mreznaVrata, String komanda) throws IOException {
		try (Socket mreznaUticnica = new Socket(adresa, mreznaVrata)) {
			BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

			this.posaljiPoruku(out, komanda);
			String odgovor = in.readLine();
			posaljiPoruku(out, odgovor);

			if (odgovor != null && odgovor.equals("OK")) {
				if (PATTERN_JSON_KOMANDE.matcher(komanda).matches()) {
					String jsonPodaci = in.readLine();
					posaljiPoruku(out, jsonPodaci);
				}
			}
		}
	}

	/**
	 * Šalje poruku prema van, ovisno o printeru
	 */
	private void posaljiPoruku(PrintWriter out, String poruka) {
		out.println(poruka);
		out.flush();
	}

}
