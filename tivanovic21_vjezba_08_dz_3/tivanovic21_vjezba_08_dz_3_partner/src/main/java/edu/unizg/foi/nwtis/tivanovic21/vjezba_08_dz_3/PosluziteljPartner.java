package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import edu.unizg.foi.nwtis.podaci.Obracun;

public class PosluziteljPartner {

	/** Konfiguracijski podaci */
	private Konfiguracija konfig;

	/** Pokretač dretvi */
	private ExecutorService executor = null;

	/** Pauza dretve. */
	private int pauzaDretve = 1000;

	/** IP adresa poslužitelja */
	private String adresa = "";

	/** Mrezna vrata za kraj rada partnera */
	private int mreznaVrataKrajPartner = 0;

	/** Mrezna vrata za registraciju partnera */
	private int mreznaVrataRegistracija = 0;

	/** Mrezna vrata komunikaciju s tvrtkom oko dohvaćanja podataka */
	private int mreznaVrataRad = 0;

	/** Mrezna vrata na koja se spajaju kupci */
	private int mreznaVrata = 0;

	/** Broj čekača */
	private int brojCekaca = 0;

	/** Kvota narudžbi koja se mora ispuniti da se pošalje obračun */
	private int kvotaNarudzbi = 0;

	/** Kod za kraj rada */
	private String kodZaKraj = "";

	/** Admin kod za partnera */
	private String adminKodPartnera = "";

	/** Brojač naplaćenih narudžbi */
	private AtomicInteger brojNaplacenihNarudzbi = new AtomicInteger(0);

	/** Zastavica za kraj rada */
	private AtomicBoolean kraj = new AtomicBoolean(false);

	private final AtomicBoolean kupacPauza = new AtomicBoolean(false);

	/** Predlošci za prepoznavanje komandi */
	private final Pattern predlozakJelovnik = Pattern.compile("^JELOVNIK\\s+(.+)$");
	private final Pattern predlozakKartaPica = Pattern.compile("^KARTAPIĆA\\s+(.+)$");
	private final Pattern predlozakNarudzba = Pattern.compile("^NARUDŽBA\\s+(.+)$");
	private final Pattern predlozakJelo = Pattern.compile("^JELO\\s+(.+)\\s+(.+)\\s+(.+)$");
	private final Pattern predlozakPice = Pattern.compile("^PIĆE\\s+(.+)\\s+(.+)\\s+(.+)$");
	private final Pattern predlozakRacun = Pattern.compile("^RAČUN\\s+(.+)$");
	private Pattern predlozakKraj = Pattern.compile("^KRAJ$");
	private final Pattern predlozakPartner = Pattern.compile("^PARTNER$");
	private final Pattern predlozakStanje = Pattern.compile("^STANJE\\s+(.+)$");
	private final Pattern predlozakKrajServer = Pattern.compile("^KRAJ\\s+(\\S+)$");
	private final Pattern predlozakOsvjezi = Pattern.compile("^OSVJEŽI\\s+(\\w+)$");
	private final Pattern predlozakStatusPartner = Pattern.compile("^STATUS\\s+(\\w+)\\s+(\\d+)$");
	private final Pattern predlozakPauzaPartner = Pattern.compile("^PAUZA\\s+(\\w+)\\s+(\\d+)$");
	private final Pattern predlozakStartPartner = Pattern.compile("^START\\s+(\\w+)\\s+(\\d+)$");
	private final Pattern predlozakSpavaPartner = Pattern.compile("^SPAVA\\s+(\\w+)\\s+(\\d+)$");

	private Map<String, Jelovnik> jelovnik = new ConcurrentHashMap<>();
	private Map<String, KartaPica> kartaPica = new ConcurrentHashMap<>();
	private Map<String, List<Narudzba>> otvoreneNarudzbe = new ConcurrentHashMap<>();
	private List<Narudzba> placeneNarudzbe = new ArrayList<>();

	/** Objekti za sinkroni rad */
	private final ReentrantLock narudzbaLock = new ReentrantLock();
	private final ReentrantLock racunLock = new ReentrantLock();

	/** Objekti za praćenje dretvi i socketa prilikom prisilnog zaustavljanja */
	private final Set<Thread> aktivneDretve = Collections.synchronizedSet(new HashSet<>());
	private final Set<Socket> aktivniSocketi = Collections.synchronizedSet(new HashSet<>());

	/**
	 * Glavna metoda koja pokreće program.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1 || args.length > 2) {
			System.out.println("Neispravan broj argumenata");
			return;
		}

		var program = new PosluziteljPartner();
		program.pripremiKreni(args[0], args);
	}

	/**
	 * Priprema program za pokretanje
	 * 
	 * @param args
	 */
	private void pripremiKreni(String nazivDatoteke, String[] args) {
		if (!ucitajKonfiguraciju(nazivDatoteke)) {
			return;
		}

		try {
			if (args.length == 1) {
				if (args.length == 1) {
					registrirajPartnera();
				}
			} else {
				String komanda = args[1];
				if (predlozakKraj.matcher(komanda).matches()) {
					posaljiKraj();
				} else if (predlozakPartner.matcher(komanda).matches()) {
					dodajShutdownHook();
					List<Future<?>> posluzitelji = pripremiVirtualneDretve();
					pratiStatusPosluzitelja(posluzitelji);
				} else {
					System.out.println("Neispravna komanda");
				}
			}
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
			ucitajKonfiguracijskePodatke();
			return true;
		} catch (NeispravnaKonfiguracija ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}

	/**
	 * Dodaje shutdown hook za obradu prisilnog zaustavljanja.
	 */
	private void dodajShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			int brojDretviZaZaustavljanje = this.aktivneDretve.size();
			int brojSocketaZaZaustavljanje = this.aktivniSocketi.size();

			this.kraj.set(true);

			synchronized (aktivniSocketi) {
				for (Socket socket : new ArrayList<>(this.aktivniSocketi)) {
					try {
						if (!socket.isClosed()) {
							socket.close();
						}
					} catch (IOException e) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
								"Greška prilikom zatvaranja socketa", e);
					}
				}
			}

			try {
				if (executor != null) {
					executor.shutdown();
					if (!executor.awaitTermination(this.pauzaDretve, TimeUnit.MILLISECONDS)) {
						executor.shutdownNow();
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			System.out.println("%nZatvoreno %d virtualnih dretvi i %d socket-a.".formatted(brojDretviZaZaustavljanje,
					brojSocketaZaZaustavljanje));
		}));
	}

	/**
	 * Kreira dretve za poslužitelja i pokreće ih.
	 *
	 */
	private List<Future<?>> pripremiVirtualneDretve() {
		var builder = Thread.ofVirtual();
		var factory = builder.factory();
		this.executor = Executors.newThreadPerTaskExecutor(factory);
		List<Future<?>> posluzitelji = new ArrayList<>();
		posluzitelji.add(pokreniIPratiVirtualneDretve(this::pokreniPosluziteljKupacRad));
		posluzitelji.add(pokreniIPratiVirtualneDretve(this::pokreniPosluziteljKraj));
		return posluzitelji;
	}

	/**
	 * Prati status poslužitelja i čeka na završetak rada dretvi.
	 * 
	 * @param posluzitelji lista dretvi
	 */
	private void pratiStatusPosluzitelja(List<Future<?>> posluzitelji) {
		while (!this.kraj.get()) {
			try {
				boolean sviGotovi = posluzitelji.stream().allMatch(Future::isDone);
				if (sviGotovi)
					break;
				Thread.sleep(this.pauzaDretve);
			} catch (InterruptedException e) {
				Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
			}
		}

		if (this.executor != null && !this.executor.isShutdown()) {
			this.executor.shutdown();
		}
	}

	/**
	 * Učitava specifične konfiguracijske podatke.
	 */
	private void ucitajKonfiguracijskePodatke() {
		this.adresa = this.konfig.dajPostavku("adresa");
		this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));
		this.brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));
		this.mreznaVrataRegistracija = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));
		this.mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
		this.mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrata"));
		this.kvotaNarudzbi = Integer.parseInt(this.konfig.dajPostavku("kvotaNarudzbi"));
		this.mreznaVrataKrajPartner = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKrajPartner"));
		this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
		this.adminKodPartnera = this.konfig.dajPostavku("kodZaAdmin");
	}

	/**
	 * Šalje komandu za registraciju partnera ovisno o podacima u konfiguracijskoj
	 * datoteci.
	 */
	private void registrirajPartnera() throws IOException {
		try (Socket ss = new Socket(this.adresa, this.mreznaVrataRegistracija)) {
			BufferedReader in = new BufferedReader(new InputStreamReader(ss.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(ss.getOutputStream(), "utf8"));
			String adresaPartnera = InetAddress.getLocalHost().getHostAddress();

			String komanda = String.format("PARTNER %s \"%s\" %s %s %s %s %s %s %s", konfig.dajPostavku("id"),
					konfig.dajPostavku("naziv"), konfig.dajPostavku("kuhinja"), adresaPartnera,
					konfig.dajPostavku("mreznaVrata"), konfig.dajPostavku("gpsSirina"), konfig.dajPostavku("gpsDuzina"),
					konfig.dajPostavku("mreznaVrataKrajPartner"), konfig.dajPostavku("kodZaAdmin"));

			posaljiPoruku(out, komanda);
			String odgovor = in.readLine();

			if (odgovor.startsWith("OK")) {
				try {
					String sigKod = spremiSigurnosniKod(odgovor);
					posaljiPoruku(out, "OK " + sigKod);
				} catch (NeispravnaKonfiguracija e) {
					Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
				}
			} else {
				posaljiPoruku(out, odgovor);
			}

			ss.close();
		} catch (IOException | NumberFormatException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * Sprema sigurnosni kod u konfiguraciju.
	 * 
	 * @param odgovor
	 * @return sigurnosni kod
	 * @throws NeispravnaKonfiguracija
	 */
	private String spremiSigurnosniKod(String odgovor) throws NeispravnaKonfiguracija {
		String sigKod = odgovor.split(" ")[1];

		if (konfig.dajPostavku("sigKod") == null) {
			konfig.spremiPostavku("sigKod", sigKod);
		} else {
			konfig.azurirajPostavku("sigKod", sigKod);
		}
		konfig.spremiKonfiguraciju();
		return sigKod;
	}

	/**
	 * Salje komandu za kraj rada poslužitelja (tvrtka).
	 */
	private void posaljiKraj() {
		var kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
		var adresa = this.konfig.dajPostavku("adresa");
		var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));

		try {
			var mreznaUticnica = new Socket(adresa, mreznaVrata);
			BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
			posaljiPoruku(out, "KRAJ " + kodZaKraj);
			mreznaUticnica.shutdownOutput();
			var linija = in.readLine();
			mreznaUticnica.shutdownInput();
			if (linija.equals("OK")) {
				System.out.println("Uspješan kraj poslužitelja.");
			}
			mreznaUticnica.close();
			this.kraj.set(true);
		} catch (IOException e) {
		}
	}

	/**
	 * Pokreće posluzitelja za rad s kupcima
	 */
	protected void pokreniPosluziteljKupacRad() {
		if (!dohvatiJelovnik()) {
			System.out.println("ERROR 46 - Neuspješno preuzimanje jelovnika");
			return;
		}

		if (!dohvatiKartuPica()) {
			System.out.println("ERROR 47 - Neuspješno preuzimanje karte pića");
			return;
		}

		try (ServerSocket ss = new ServerSocket(this.mreznaVrata, this.brojCekaca)) {
			while (!this.kraj.get()) {
				try {
					Socket mreznaUticnica = ss.accept();
					dodajSocket(mreznaUticnica);
					pokreniIPratiVirtualneDretve(() -> {
						try {
							obradiZahtjevKupca(mreznaUticnica);
						} finally {
							try {
								if (!mreznaUticnica.isClosed()) {
									mreznaUticnica.close();
								}
							} catch (IOException e) {
								Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
							} finally {
								ukloniSocket(mreznaUticnica);
							}
						}
					});
				} catch (IOException e) {
					if (!this.kraj.get()) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
					}
				}
			}
		} catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * Pokreće poslužitelja za kraj rada partnera
	 */
	protected void pokreniPosluziteljKraj() {
		try (ServerSocket ss = new ServerSocket(this.mreznaVrataKrajPartner, this.brojCekaca)) {
			while (!this.kraj.get()) {
				try {
					Socket mreznaUticnica = ss.accept();
					dodajSocket(mreznaUticnica);
					this.pokreniIPratiVirtualneDretve(() -> {
						try {
							obradiKraj(mreznaUticnica);
						} finally {
							try {
								if (!mreznaUticnica.isClosed()) {
									mreznaUticnica.close();
								}
							} catch (IOException e) {
								Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
							} finally {
								ukloniSocket(mreznaUticnica);
							}
						}
					});
				} catch (IOException e) {
					if (!this.kraj.get()) {
						Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
					}
				}
			}
		} catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * Obrada zahtjeva za kraj rada partnera.
	 * 
	 * @param mreznaUticnica mrežna utičnica
	 */
	protected void obradiKraj(Socket mreznaUticnica) {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"))) {

			String linija = in.readLine();
			mreznaUticnica.shutdownInput();

			if (!provjeriPostojanjeUnosaLinije(linija)) {
				posaljiPoruku(out, "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj");
			} else {
				if (predlozakKrajServer.matcher(linija).matches()) {
					obradiKomandaKraj(linija, out);
				} else if (predlozakOsvjezi.matcher(linija).matches()) {
					obradiOsvjezi(linija, out);
				} else if (predlozakStatusPartner.matcher(linija).matches()) {
					obradiStatus(linija, out);
				} else if (predlozakPauzaPartner.matcher(linija).matches()) {
					obradiPauzu(linija, out);
				} else if (predlozakStartPartner.matcher(linija).matches()) {
					obradiStart(linija, out);
				} else if (predlozakSpavaPartner.matcher(linija).matches()) {
					obradiSpava(linija, out);
				} else {
					posaljiPoruku(out, "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj");
				}
			}

			mreznaUticnica.shutdownOutput();
			mreznaUticnica.close();
		} catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * Obrada komande za kraj partnera.
	 */
	private void obradiKomandaKraj(String linija, PrintWriter out) {
		Matcher krajMatcher = predlozakKrajServer.matcher(linija);
		if (!krajMatcher.matches()) {
			posaljiPoruku(out, "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj");
			return;
		}

		String kod = krajMatcher.group(1);
		if (!this.kodZaKraj.equals(kod)) {
			posaljiPoruku(out, "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj");
			return;
		}

		this.kraj.set(true);
		posaljiPoruku(out, "OK");
	}

	/**
	 * Obrada komande za osvježavanje podataka.
	 */
	private void obradiOsvjezi(String linija, PrintWriter out) {
		Matcher osvjeziMatcher = predlozakOsvjezi.matcher(linija);
		if (!osvjeziMatcher.matches()) {
			posaljiPoruku(out, "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj");
			return;
		}

		String adminKod = osvjeziMatcher.group(1);
		if (!this.adminKodPartnera.equals(adminKod)) {
			posaljiPoruku(out, "ERROR 61 - Pogrešan kodZaAdminPartnera");
			return;
		}

		if (this.kupacPauza.get()) {
			posaljiPoruku(out, "ERROR 62 - Pogrešna promjena pauze ili starta");
			return;
		}

		if (dohvatiJelovnik() && dohvatiKartuPica()) {
			posaljiPoruku(out, "OK");
		} else {
			posaljiPoruku(out, "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj");
		}
	}

	/**
	 * Obrada komande za provjeru statusa.
	 */
	private void obradiStatus(String linija, PrintWriter out) {
		Matcher statusMatcher = predlozakStatusPartner.matcher(linija);
		if (!statusMatcher.matches()) {
			posaljiPoruku(out, "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj");
			return;
		}

		String adminKod = statusMatcher.group(1);
		int id = Integer.parseInt(statusMatcher.group(2));

		if (!this.adminKodPartnera.equals(adminKod)) {
			posaljiPoruku(out, "ERROR 61 - Pogrešan kodZaAdminPartnera");
			return;
		}

		if (id != 1) {
			posaljiPoruku(out, "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj");
			return;
		}

		int stanje = this.kupacPauza.get() ? 0 : 1;
		posaljiPoruku(out, "OK " + stanje);
	}

	/**
	 * Obrada komande za pauziranje.
	 */
	private void obradiPauzu(String linija, PrintWriter out) {
		Matcher pauzaMatcher = predlozakPauzaPartner.matcher(linija);
		if (!pauzaMatcher.matches()) {
			posaljiPoruku(out, "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj");
			return;
		}

		String adminKod = pauzaMatcher.group(1);
		int id = Integer.parseInt(pauzaMatcher.group(2));

		if (!this.adminKodPartnera.equals(adminKod)) {
			posaljiPoruku(out, "ERROR 61 - Pogrešan kodZaAdminPartnera");
			return;
		}

		if (id != 1) {
			posaljiPoruku(out, "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj");
			return;
		}

		if (this.kupacPauza.get()) {
			posaljiPoruku(out, "ERROR 62 - Pogrešna promjena pauze ili starta");
			return;
		}

		this.kupacPauza.set(true);
		posaljiPoruku(out, "OK");
	}

	/**
	 * Obrada komande za pokretanje.
	 */
	private void obradiStart(String linija, PrintWriter out) {
		Matcher startMatcher = predlozakStartPartner.matcher(linija);
		if (!startMatcher.matches()) {
			posaljiPoruku(out, "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj");
			return;
		}

		String adminKod = startMatcher.group(1);
		int id = Integer.parseInt(startMatcher.group(2));

		if (!this.adminKodPartnera.equals(adminKod)) {
			posaljiPoruku(out, "ERROR 61 - Pogrešan kodZaAdminPartnera");
			return;
		}

		if (id != 1) {
			posaljiPoruku(out, "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj");
			return;
		}

		if (!this.kupacPauza.get()) {
			posaljiPoruku(out, "ERROR 62 - Pogrešna promjena pauze ili starta");
			return;
		}

		this.kupacPauza.set(false);
		posaljiPoruku(out, "OK");
	}

	/**
	 * Obrada komande za spavanje.
	 */
	private void obradiSpava(String linija, PrintWriter out) {
		Matcher spavaMatcher = predlozakSpavaPartner.matcher(linija);
		if (!spavaMatcher.matches()) {
			posaljiPoruku(out, "ERROR 60 - Format komande nije ispravan ili nije ispravan kod za kraj");
			return;
		}

		String adminKod = spavaMatcher.group(1);
		int trajanje = Integer.parseInt(spavaMatcher.group(2));

		if (!this.adminKodPartnera.equals(adminKod)) {
			posaljiPoruku(out, "ERROR 61 - Pogrešan kodZaAdminPartnera");
			return;
		}

		try {
			Thread.sleep(trajanje);
			posaljiPoruku(out, "OK");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			posaljiPoruku(out, "ERROR 63 - Prekid spavanja dretve");
		}
	}

	/**
	 * Obrađuje zahtjeve klijenata
	 * 
	 * @param socket Socket klijenta
	 */
	private void obradiZahtjevKupca(Socket socket) {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"), true)) {

			String linija = in.readLine();
			if (!provjeriPostojanjeUnosaLinije(linija)) {
				posaljiPoruku(out, "ERROR 49 - Nešto drugo nije u redu");
				return;
			}

			obradiNaredbu(out, linija, in);
		} catch (IOException e) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
		} catch (NumberFormatException e) {
			try {
				PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"), true);
				posaljiPoruku(out, "ERROR 40 - Format komande nije ispravan");
			} catch (IOException ex) {
				Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	/**
	 * Obrađuje naredbu koju je kupac poslao
	 * 
	 * @param out
	 * @param linija
	 */
	private void obradiNaredbu(PrintWriter out, String linija, BufferedReader in) {
		Matcher matcherJelovnik = predlozakJelovnik.matcher(linija);
		Matcher matcherKartaPica = predlozakKartaPica.matcher(linija);
		Matcher matcherNarudzba = predlozakNarudzba.matcher(linija);
		Matcher matcherJelo = predlozakJelo.matcher(linija);
		Matcher matcherPice = predlozakPice.matcher(linija);
		Matcher matcherRacun = predlozakRacun.matcher(linija);
		Matcher matcherStanje = predlozakStanje.matcher(linija);

		if (matcherJelovnik.matches()) {
			obradiJelovnik(matcherJelovnik.group(1), out);
		} else if (matcherKartaPica.matches()) {
			obradiKartuPica(matcherKartaPica.group(1), out);
		} else if (matcherNarudzba.matches()) {
			obradiNarudzbu(matcherNarudzba.group(1), out);
		} else if (matcherJelo.matches()) {
			obradiJelo(matcherJelo.group(1), matcherJelo.group(2), Float.parseFloat(matcherJelo.group(3)), out);
		} else if (matcherPice.matches()) {
			obradiPice(matcherPice.group(1), matcherPice.group(2), Float.parseFloat(matcherPice.group(3)), out);
		} else if (matcherRacun.matches()) {
			obradiRacun(matcherRacun.group(1), out);
		} else if (matcherStanje.matches()) {
			obradiStanje(matcherStanje.group(1), out);
		} else {
			posaljiPoruku(out, "ERROR 40 - Format komande nije ispravan");
		}
	}

	/**
	 * Vraća popis jela iz jelovnika kupcu
	 *
	 * @param out izlazni stream
	 */
	private void obradiJelovnik(String kupac, PrintWriter out) {
		if (!provjeriPostojanjeUnosaLinije(kupac)) {
			posaljiPoruku(out, "ERROR 49 - Nešto drugo nije u redu");
			return;
		}

		if (this.jelovnik.isEmpty()) {
			posaljiPoruku(out, "ERROR 46 - Neuspješno preuzimanje jelovnika");
			return;
		}

		Gson gson = new Gson();
		List<Jelovnik> popisJela = new ArrayList<>(this.jelovnik.values());
		posaljiPoruku(out, "OK");
		posaljiPoruku(out, gson.toJson(popisJela));
	}

	/**
	 * Vraća popis jela iz jelovnika kupcu
	 *
	 * @param out izlazni stream
	 */
	private void obradiKartuPica(String kupac, PrintWriter out) {
		if (!provjeriPostojanjeUnosaLinije(kupac)) {
			posaljiPoruku(out, "ERROR 49 - Nešto drugo nije u redu");
			return;
		}

		if (this.kartaPica.isEmpty()) {
			posaljiPoruku(out, "ERROR 47 - Neuspješno preuzimanje karte pića");
			return;
		}

		Gson gson = new Gson();
		List<KartaPica> popisPica = new ArrayList<>(this.kartaPica.values());
		posaljiPoruku(out, "OK");
		posaljiPoruku(out, gson.toJson(popisPica));
	}

	/**
	 * Kreira novu otvorenu narudžbu za kupca
	 *
	 * @param kupac naziv kupca
	 * @param out   izlazni stream
	 */
	private void obradiNarudzbu(String kupac, PrintWriter out) {
		if (!provjeriPostojanjeUnosaLinije(kupac)) {
			posaljiPoruku(out, "ERROR 49 - Nešto drugo nije u redu");
			return;
		}

		this.narudzbaLock.lock();
		try {
			if (this.otvoreneNarudzbe.containsKey(kupac)) {
				posaljiPoruku(out, "ERROR 44 - Već postoji otvorena narudžba za korisnika/kupca");
				return;
			}

			this.otvoreneNarudzbe.put(kupac, new ArrayList<>());
			posaljiPoruku(out, "OK");
		} finally {
			this.narudzbaLock.unlock();
		}
	}

	/**
	 * Dodaje jelo u otvorenu narudžbu kupca
	 *
	 * @param kupac    naziv kupca
	 * @param jeloId   ID jela
	 * @param kolicina količina jela
	 * @param out      izlazni stream
	 */
	private void obradiJelo(String kupac, String jeloId, float kolicina, PrintWriter out) {
		if (!provjeriPostojanjeUnosaLinije(kupac) || !provjeriPostojanjeUnosaLinije(jeloId) || kolicina <= 0) {
			posaljiPoruku(out, "ERROR 49 - Nešto drugo nije u redu");
			return;
		}

		this.narudzbaLock.lock();
		try {
			if (!this.otvoreneNarudzbe.containsKey(kupac)) {
				posaljiPoruku(out, "ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca");
				return;
			}

			if (!this.jelovnik.containsKey(jeloId)) {
				posaljiPoruku(out, "ERROR 41 - Ne postoji jelo s id u kolekciji jelovnika kod partnera");
				return;
			}

			Jelovnik jelo = this.jelovnik.get(jeloId);
			Narudzba stavka = new Narudzba(kupac, jeloId, true, kolicina, jelo.cijena(), System.currentTimeMillis());
			this.otvoreneNarudzbe.get(kupac).add(stavka);
			posaljiPoruku(out, "OK");
		} finally {
			this.narudzbaLock.unlock();
		}
	}

	/**
	 * Dodaje piće u otvorenu narudžbu kupca
	 *
	 * @param kupac    naziv kupca
	 * @param piceId   ID pića
	 * @param kolicina količina pića
	 * @param out      izlazni stream
	 */
	private void obradiPice(String kupac, String piceId, float kolicina, PrintWriter out) {
		if (!provjeriPostojanjeUnosaLinije(kupac) || !provjeriPostojanjeUnosaLinije(piceId) || kolicina <= 0) {
			posaljiPoruku(out, "ERROR 49 - Nešto drugo nije u redu");
			return;
		}

		this.narudzbaLock.lock();
		try {
			if (!this.otvoreneNarudzbe.containsKey(kupac)) {
				posaljiPoruku(out, "ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca");
				return;
			}

			if (!this.kartaPica.containsKey(piceId)) {
				posaljiPoruku(out, "ERROR 42 - Ne postoji piće s id u kolekciji karte pića kod partnera");
				return;
			}

			KartaPica pice = this.kartaPica.get(piceId);
			Narudzba stavka = new Narudzba(kupac, piceId, false, kolicina, pice.cijena(), System.currentTimeMillis());
			this.otvoreneNarudzbe.get(kupac).add(stavka);
			posaljiPoruku(out, "OK");

		} finally {
			this.narudzbaLock.unlock();
		}
	}

	/**
	 * Zatvara otvorenu narudžbu i dodaje je u listu naplaćenih narudžbi
	 *
	 * @param kupac naziv kupca
	 * @param out   izlazni stream
	 */
	private synchronized void obradiRacun(String kupac, PrintWriter out) {
		if (!provjeriPostojanjeUnosaLinije(kupac)) {
			posaljiPoruku(out, "ERROR 49 - Nešto drugo nije u redu");
			return;
		}

		this.racunLock.lock();
		try {
			if (!this.otvoreneNarudzbe.containsKey(kupac)) {
				posaljiPoruku(out, "ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca");
				return;
			}

			List<Narudzba> narudzba = new ArrayList<>(otvoreneNarudzbe.get(kupac));
			this.placeneNarudzbe.addAll(narudzba);
			this.otvoreneNarudzbe.remove(kupac);

			int trenutniBrojNaplacenihNarudzbi = brojNaplacenihNarudzbi.incrementAndGet();

			if (trenutniBrojNaplacenihNarudzbi % this.kvotaNarudzbi == 0) {
				kreirajObracun(out);
			} else {
				posaljiPoruku(out, "OK");
			}

		} finally {
			this.racunLock.unlock();
		}
	}

	/**
	 * Provjerava stanje narudžbe za kupca.
	 *
	 * @param kupac naziv kupca
	 * @param out   izlazni stream
	 */
	private void obradiStanje(String kupac, PrintWriter out) {
		if (!provjeriPostojanjeUnosaLinije(kupac)) {
			posaljiPoruku(out, "ERROR 49 - Nešto drugo nije u redu");
			return;
		}

		if (this.kupacPauza.get()) {
			posaljiPoruku(out, "ERROR 48 - Poslužitelj za prijem zahtjeva kupaca u pauzi");
			return;
		}

		this.narudzbaLock.lock();
		try {
			if (!this.otvoreneNarudzbe.containsKey(kupac)) {
				posaljiPoruku(out, "ERROR 43 - Ne postoji otvorena narudžba za korisnika/kupca");
				return;
			}

			List<Narudzba> narudzba = this.otvoreneNarudzbe.get(kupac);
			Gson gson = new Gson();
			posaljiPoruku(out, "OK");
			posaljiPoruku(out, gson.toJson(narudzba));

		} finally {
			this.narudzbaLock.unlock();
		}
	}

	/**
	 * Kreira obračun i šalje ga tvrtki
	 *
	 * @param out izlazni stream
	 */
	private void kreirajObracun(PrintWriter out) {
		Map<String, List<Narudzba>> grupiraneNarudzbe = this.placeneNarudzbe.stream()
				.collect(Collectors.groupingBy(Narudzba::id));

		List<Obracun> obracuni = new ArrayList<>();
		int partnerId = Integer.parseInt(this.konfig.dajPostavku("id"));

		for (Map.Entry<String, List<Narudzba>> entry : grupiraneNarudzbe.entrySet()) {
			String id = entry.getKey();
			List<Narudzba> stavke = entry.getValue();

			float ukupnaKolicina = (float) stavke.stream().mapToDouble(Narudzba::kolicina).sum();
			boolean isJelo = stavke.get(0).jelo();
			float cijena = stavke.get(0).cijena();

			obracuni.add(new Obracun(partnerId, id, isJelo, ukupnaKolicina, cijena, System.currentTimeMillis()));
		}

		if (!obracuni.isEmpty()) {
			if (posaljiObracun(obracuni)) {
				this.placeneNarudzbe.clear();
				Gson gson = new Gson();
				posaljiPoruku(out, "OK");
				posaljiPoruku(out, gson.toJson(obracuni));
			} else {
				posaljiPoruku(out, "ERROR 45 - Neuspješno slanje obračuna");
			}
		}
	}

	/**
	 * Šalje obračun tvrtki
	 *
	 * @param obracuni lista obračuna
	 * @return true ako je uspješno poslan obračun, inače false
	 */
	private boolean posaljiObracun(List<Obracun> obracuni) {
		try (Socket ss = new Socket(this.adresa, this.mreznaVrataRad);
				BufferedReader in = new BufferedReader(new InputStreamReader(ss.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(ss.getOutputStream(), "utf8"))) {

			Gson gson = new Gson();
			String jsonObracun = gson.toJson(obracuni);

			posaljiPoruku(out, "OBRAČUN " + dohvatiIdISigurnosniKod() + "\n" + jsonObracun);
			String odgovor = in.readLine();

			ss.close();
			return odgovor != null && odgovor.equals("OK");
		} catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
			return false;
		}
	}

	/**
	 * Dohvaća ID i sigurnosni kod iz konfiguracijske datoteke.
	 *
	 * @return ID i sigurnosni kod
	 */
	private String dohvatiIdISigurnosniKod() {
		return konfig.dajPostavku("id") + " " + konfig.dajPostavku("sigKod");
	}

	/**
	 * Učitava podatke o jelovniku iz tvrtke.
	 *
	 * @param in, out
	 * @return true ako uspješno preuzme jelovnik, inače false
	 */
	private boolean dohvatiJelovnik() {
		if (!provjeriPostojanjeUnosaLinije(dohvatiIdISigurnosniKod())) {
			return false;
		}

		try (Socket ss = new Socket(this.adresa, this.mreznaVrataRad);
				BufferedReader in = new BufferedReader(new InputStreamReader(ss.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(ss.getOutputStream(), "utf8"))) {

			posaljiPoruku(out, "JELOVNIK " + dohvatiIdISigurnosniKod());
			String odgovor = in.readLine();

			if (odgovor == null || !odgovor.equals("OK")) {
				return false;
			}

			String jsonJelovnik = in.readLine();
			mapirajIzJsona(jsonJelovnik, Jelovnik[].class, this.jelovnik);
			return true;
		} catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
			return false;
		}

	}

	/**
	 * Učitava podatke o karti pića iz tvrtke.
	 *
	 * @param in, out
	 * @return true ako uspješno preuzme kartu pića, inače false
	 */
	private boolean dohvatiKartuPica() {
		if (!provjeriPostojanjeUnosaLinije(dohvatiIdISigurnosniKod())) {
			return false;
		}

		try (Socket ss = new Socket(this.adresa, this.mreznaVrataRad);
				BufferedReader in = new BufferedReader(new InputStreamReader(ss.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(ss.getOutputStream(), "utf8"))) {

			posaljiPoruku(out, "KARTAPIĆA " + dohvatiIdISigurnosniKod());
			String odgovor = in.readLine();

			if (odgovor == null || !odgovor.equals("OK")) {
				return false;
			}

			String jsonKartaPica = in.readLine();
			mapirajIzJsona(jsonKartaPica, KartaPica[].class, this.kartaPica);
			return true;
		} catch (IOException e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
			return false;
		}

	}

	/**
	 * Šalje poruku prema van, ovisno o printeru
	 */
	private void posaljiPoruku(PrintWriter out, String poruka) {
		out.println(poruka);
		out.flush();
	}

	/**
	 * Provjeri da li je linija prazna ili null.
	 *
	 * @param linija linija
	 * @return true ako je linija prazna ili null, inače false
	 */
	private boolean provjeriPostojanjeUnosaLinije(String linija) {
		return linija != null && !linija.isBlank() && !linija.isEmpty()
				&& !Pattern.compile("^\\s*$").matcher(linija).matches();
	}

	/**
	 * Mapira JSON string u mapu.
	 *
	 * @param jsonString JSON string
	 * @param klasa      klasa objekta
	 * @param mapa       mapa
	 */
	private <T> void mapirajIzJsona(String jsonString, Class<T[]> klasa, Map<String, T> mapa) {
		Gson gson = new Gson();
		T[] objekti = gson.fromJson(jsonString, klasa);
		for (T objekt : objekti) {
			if (objekt instanceof Jelovnik jelo) {
				mapa.put(jelo.id(), objekt);
			} else if (objekt instanceof KartaPica pice) {
				mapa.put(pice.id(), objekt);
			}
		}
	}

	/**
	 * Pokreće virtualne dretve te ažurira set aktivnih dretvi ovisno o statusu
	 * dretve.
	 *
	 * @param task zadatak koji treba izvršiti
	 * @return Future<?> koji predstavlja rezultat izvršenja zadatka
	 */
	private Future<?> pokreniIPratiVirtualneDretve(Runnable task) {
		return this.executor.submit(() -> {
			Thread trenutnaDretva = Thread.currentThread();
			this.aktivneDretve.add(trenutnaDretva);
			try {
				task.run();
			} finally {
				this.aktivneDretve.remove(trenutnaDretva);
			}
		});
	}

	/**
	 * Dodaje socket u set aktivnih socket-a
	 *
	 * @param socket socket
	 */
	private void dodajSocket(Socket socket) {
		this.aktivniSocketi.add(socket);
	}

	/**
	 * Uklanja socket iz set aktivnih socket-a
	 *
	 * @param socket socket
	 */
	private void ukloniSocket(Socket socket) {
		this.aktivniSocketi.remove(socket);
	}

}
