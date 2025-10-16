package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.PartnerPopis;

public class PosluziteljTvrtka {

	/** Konfiguracijski podaci */
	protected Konfiguracija konfig;

	/** Pokretač dretvi */
	private ExecutorService executor = null;

	/** Pauza dretve. */
	private int pauzaDretve = 1000;

	/** Kod za kraj rada */
	private String kodZaKraj = "";

	/** Admin kod za tvrtku */
	private String adminKodTvrtke = "";

	/** Zastavica za kraj rada */
	private AtomicBoolean kraj = new AtomicBoolean(false);

	/** Broj čekača */
	private int brojCekaca = 0;

	private Map<Integer, Partner> partneri = new ConcurrentHashMap<>();
	private Map<String, String> kuhinje = new ConcurrentHashMap<>();
	private Map<String, Map<String, Jelovnik>> jelovnici = new ConcurrentHashMap<>();
	private Map<String, KartaPica> kartaPica = new ConcurrentHashMap<>();
	private List<Obracun> obracuni = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Regex uzorci
	 */
	private static final Pattern REGISTRACIJA_PARTNERA_CMD = Pattern.compile(
			"^PARTNER\\s+(\\d+)\\s+\"(.+?)\"\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(-?\\d+\\.\\d+)\\s+(-?\\d+\\.\\d+)\\s+(\\d+)\\s+(\\S+)$");
	private static final Pattern OBRISI_PARTNERA_CMD = Pattern.compile("^OBRIŠI\\s+(\\d+)\\s+([0-9a-fA-F]+)$");
	private static final Pattern POPIS_CMD = Pattern.compile("^POPIS$");
	private static final Pattern JELOVNIK_CMD = Pattern.compile("^JELOVNIK\\s+(\\d+)\\s+([0-9a-fA-F]+)$");
	private static final Pattern KARTAPICA_CMD = Pattern.compile("^KARTAPIĆA\\s+(\\d+)\\s+([0-9a-fA-F]+)$");
	private static final Pattern OBRACUN_CMD = Pattern
			.compile("(?s)^OBRAČUN\\s+(\\d+)\\s+([0-9a-fA-F]+)(?:\\s+(\\[.*))?$");
	private static final Pattern STATUS_CMD = Pattern.compile("^STATUS\\s+(\\w+)\\s+(\\d+)$");
	private static final Pattern PAUZA_CMD = Pattern.compile("^PAUZA\\s+(\\w+)\\s+(\\d+)$");
	private static final Pattern START_CMD = Pattern.compile("^START\\s+(\\w+)\\s+(\\d+)$");
	private static final Pattern SPAVA_CMD = Pattern.compile("^SPAVA\\s+(\\w+)\\s+(\\d+)$");
	private static final Pattern KRAJWS_CMD = Pattern.compile("^KRAJWS\\s+(\\S+)$");
	private static final Pattern OSVJEZI_CMD = Pattern.compile("^OSVJEŽI\\s+(\\w+)$");
	private static final Pattern OBRACUNWS_CMD = Pattern
			.compile("(?s)^OBRAČUNWS\\s+(\\d+)\\s+([0-9a-fA-F]+)(?:\\s+(\\[.*))?$");
	private static final Pattern KUHINJA_REGEX = Pattern
			.compile("^(kuhinja_[0-9]+)=([a-zčćžšđA-ZČĆŽŠĐ]+);([a-zčćžšđA-ZČĆŽŠĐ ]+)$");

	/** Zastavice za praćenje pauza dijelova poslužitelja */
	private final AtomicBoolean registracijaPauza = new AtomicBoolean(false);
	private final AtomicBoolean partnerPauza = new AtomicBoolean(false);

	/** Objekt za sinkroni rad */
	private final ReentrantLock obracunLock = new ReentrantLock();

	/** Objekti za praćenje dretvi i socketa prilikom prisilnog zaustavljanja */
	private final Set<Thread> aktivneDretve = Collections.synchronizedSet(new HashSet<>());
	private final Set<Socket> aktivniSocketi = Collections.synchronizedSet(new HashSet<>());

	/**
	 * Glavna metoda.
	 *
	 * @param args argumenti - očekuje putanju do konfiguracijske datoteke
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Broj argumenata nije 1.");
			return;
		}

		var program = new PosluziteljTvrtka();
		var nazivDatoteke = args[0];

		program.pripremiKreni(nazivDatoteke);
	}

	/**
	 * Priprema i pokreće server.
	 *
	 * @param nazivDatoteke naziv datoteke
	 */
	public void pripremiKreni(String nazivDatoteke) {
		try {
			if (!this.ucitajKonfiguraciju(nazivDatoteke) || !this.ucitajKartuPica() || !this.ucitajKuhinje()) {
				return;
			}
			ucitajPartnere();
			ucitajObracune();
			ucitajKonfiguracijskePodatke();
			dodajShutdownHook();
			List<Future<?>> posluzitelji = pripremiVirtualneDretve();
			pratiStatusPosluzitelja(posluzitelji);
		} catch (NeispravnaKonfiguracija e) {
			return;
		}
	}

	/**
	 * Kreira listu virtualnih dretvi
	 * 
	 * @return posluzitelji - lista vritualnih dretvi
	 */
	private List<Future<?>> pripremiVirtualneDretve() {
		var builder = Thread.ofVirtual();
		var factory = builder.factory();
		this.executor = Executors.newThreadPerTaskExecutor(factory);

		List<Future<?>> posluzitelji = new ArrayList<>();
		posluzitelji.add(this.pokreniIPratiVirtualneDretve(this::pokreniPosluziteljKraj));
		posluzitelji.add(this.pokreniIPratiVirtualneDretve(this::pokreniPosluziteljRegistracijaPartnera));
		posluzitelji.add(this.pokreniIPratiVirtualneDretve(this::pokreniPosluziteljPartnerRad));
		return posluzitelji;
	}

	/**
	 * Učitava konfiguracijske podatke prilikom početka rada
	 */
	private void ucitajKonfiguracijskePodatke() {
		this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
		this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));
		this.brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));
		this.adminKodTvrtke = this.konfig.dajPostavku("kodZaAdminTvrtke");
	}

	/**
	 * Pokreće dretvu za obradu zahtjeva za kraj rada.
	 */
	protected void pokreniPosluziteljKraj() {
		var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));
		try (ServerSocket ss = new ServerSocket(mreznaVrata, this.brojCekaca)) {
			while (!this.kraj.get()) {
				var mreznaUticnica = ss.accept();
				dodajSocket(mreznaUticnica);
				this.pokreniIPratiVirtualneDretve(() -> {
					try {
						obradiKraj(mreznaUticnica);
					} finally {
						try {
							mreznaUticnica.close();
						} catch (IOException e) {
						}
						ukloniSocket(mreznaUticnica);
					}
				});
			}
			ss.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Dodaje shutdown hook za obradu prisilnog zaustavljanja.
	 */
	private void dodajShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {

			int brojDretviZaZaustavljanje = this.aktivneDretve.size();
			int brojSocketaZaZaustavljanje = this.aktivniSocketi.size();

			synchronized (this.aktivniSocketi) {
				for (Socket socket : new ArrayList<>(this.aktivniSocketi)) {
					try {
						if (!socket.isClosed())
							socket.close();
					} catch (IOException e) {
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
	 * Pokreće dretvu za obradu zahtjeva za registraciju partnera.
	 */
	protected void pokreniPosluziteljRegistracijaPartnera() {
		int mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));
		try (ServerSocket ss = new ServerSocket(mreznaVrata, this.brojCekaca)) {
			while (!this.kraj.get()) {
				Socket mreznaUticnica = ss.accept();
				dodajSocket(mreznaUticnica);
				this.pokreniIPratiVirtualneDretve(() -> {
					try {
						obradiKomandeZaRegistracijuPartnera(mreznaUticnica);
					} finally {
						try {
							mreznaUticnica.close();
						} catch (IOException e) {
						}
						ukloniSocket(mreznaUticnica);
					}
				});
			}
			ss.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Pokreće dretvu za obradu zahtjeva za rad partnera.
	 */
	protected void pokreniPosluziteljPartnerRad() {
		int mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
		try (ServerSocket ss = new ServerSocket(mreznaVrata, this.brojCekaca)) {
			while (!this.kraj.get()) {
				Socket mreznaUticnica = ss.accept();
				dodajSocket(mreznaUticnica);
				mreznaUticnica.setSoTimeout(this.pauzaDretve);

				this.pokreniIPratiVirtualneDretve(() -> {
					try {
						obradiKomandeZaRadPartnera(mreznaUticnica);
					} finally {
						try {
							mreznaUticnica.close();
						} catch (IOException e) {
						}
						ukloniSocket(mreznaUticnica);
					}
				});
			}
			ss.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Obrada zahtjeva za kraj rada.
	 *
	 * @param mreznaUticnica mrežna utičnica
	 * @return true ako je uspješno obrađen zahtjev
	 */
	protected Boolean obradiKraj(Socket mreznaUticnica) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
			String linija = in.readLine();
			mreznaUticnica.shutdownInput();

			if (!provjeriPostojanjeUnosaLinije(linija)) {
				out.write("ERROR 19 - Nešto drugo nije u redu\n");
			} else {
				if (provjeriKomanduZaKraj(linija)) {
					obradiKomandaKraj(out);
				} else if (STATUS_CMD.matcher(linija).matches()) {
					obradiStatus(linija, out);
				} else if (PAUZA_CMD.matcher(linija).matches()) {
					obradiPauzu(linija, out);
				} else if (START_CMD.matcher(linija).matches()) {
					obradiStart(linija, out);
				} else if (SPAVA_CMD.matcher(linija).matches()) {
					obradiSpava(linija, out);
				} else if (KRAJWS_CMD.matcher(linija).matches()) {
					obradiKrajWS(linija, out);
				} else if (OSVJEZI_CMD.matcher(linija).matches()) {
					obradiOsvjezi(linija, out);
				} else {
					out.write("ERROR 10 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
				}
			}

			out.flush();
			mreznaUticnica.shutdownOutput();
			mreznaUticnica.close();
		} catch (Exception e) {
		}
		return Boolean.TRUE;
	}

	/**
	 * Učitava kartu pića iz datoteke.
	 *
	 * @return true, ako je uspješno učitavanje
	 */
	protected boolean ucitajKartuPica() {
		var nazivDatotekePica = this.konfig.dajPostavku("datotekaKartaPica");
		var datoteka = Path.of(nazivDatotekePica);

		if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka) || !Files.isReadable(datoteka)) {
			return false;
		}

		try (var br = Files.newBufferedReader(datoteka, StandardCharsets.UTF_8)) {
			Gson gson = new Gson();
			var kartaPicaNiz = gson.fromJson(br, KartaPica[].class);
			Arrays.stream(kartaPicaNiz).forEach(kp -> this.kartaPica.put(kp.id(), kp));
		} catch (IOException ex) {
			return false;
		}

		return true;
	}

	/**
	 * Učitava kuhinje iz konfiguracijske datoteke.
	 *
	 * @return true, ako je uspješno učitavanje
	 */
	protected boolean ucitajKuhinje() {
		try {
			this.konfig.dajSvePostavke().entrySet().stream().filter(e -> e.getKey().toString().startsWith("kuhinja_"))
					.forEach(k -> {
						Matcher kuhinja = KUHINJA_REGEX.matcher(k.toString());
						if (kuhinja.matches()) {
							String kuhinjaId = kuhinja.group(1);
							String kuhinjaNaziv = kuhinja.group(2);
							String kuhinjaOpis = kuhinja.group(3);

							if (ucitajJelovnik(kuhinjaId, kuhinjaNaziv))
								this.kuhinje.put(kuhinjaNaziv, kuhinjaOpis);
						}
					});
		} catch (Exception ex) {
			return false;
		}

		return true;
	}

	/**
	 * Učitava obračune iz datoteke. Stvara datoteku ako ne postoji. Syncrhonized
	 * zbog potrebe za thread safe načinom rada.
	 */
	protected synchronized void ucitajObracune() {
		var datoteka = Path.of(this.konfig.dajPostavku("datotekaObracuna"));
		if (!pripremiDatoteku(datoteka, null, "[]"))
			return;

		this.obracuni.clear();
		try (var br = Files.newBufferedReader(datoteka, StandardCharsets.UTF_8)) {
			Gson gson = new Gson();
			var obracuniNiz = gson.fromJson(br, Obracun[].class);
			if (obracuniNiz != null) {
				Arrays.stream(obracuniNiz).forEach(o -> this.obracuni.add(o));
			}
		} catch (IOException ex) {
		}
	}

	/**
	 * Sprema obračune u datoteku. Syncrhonized zbog potrebe za thread safe načinom
	 * rada.
	 */
	protected synchronized void spremiObracune() {
		var datoteka = Path.of(this.konfig.dajPostavku("datotekaObracuna"));

		if (!pripremiDatoteku(datoteka, null, null))
			return;

		try (var bw = Files.newBufferedWriter(datoteka, StandardCharsets.UTF_8)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(this.obracuni, bw);
		} catch (IOException ex) {
		}
	}

	/**
	 * Sprema partnere u datoteku. Syncrhonized zbog potrebe za thread safe načinom
	 * rada.
	 * 
	 * @throws NeispravnaKonfiguracija
	 */
	protected synchronized void spremiPartnere() throws NeispravnaKonfiguracija {
		var datoteka = Path.of(this.konfig.dajPostavku("datotekaPartnera"));

		if (!pripremiDatoteku(datoteka, null, null))
			return;

		try (var bw = Files.newBufferedWriter(datoteka, StandardCharsets.UTF_8)) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(this.partneri, bw);

		} catch (IOException ex) {
		}
	}

	/**
	 * Učitava partnere iz datoteku. Syncrhonized zbog potrebe za thread safe
	 * načinom rada.
	 * 
	 * @throws NeispravnaKonfiguracija
	 */
	protected synchronized void ucitajPartnere() throws NeispravnaKonfiguracija {
		var datoteka = Path.of(this.konfig.dajPostavku("datotekaPartnera"));

		if (!pripremiDatoteku(datoteka, null, "{}"))
			return;

		try (var br = Files.newBufferedReader(datoteka, StandardCharsets.UTF_8)) {
			Gson gson = new GsonBuilder().create();
			Type type = new TypeToken<List<Partner>>() {
			}.getType();
			List<Partner> listaPartnera = gson.fromJson(br, type);

			this.partneri = new ConcurrentHashMap<>();
			if (listaPartnera != null) {
				for (Partner p : listaPartnera) {
					this.partneri.put(p.id(), p);
				}
			}

		} catch (IOException ex) {
			this.partneri = new ConcurrentHashMap<>();
		}
	}

	/**
	 * Učitava jelovnik iz datoteke. Synchronized zbog potrebe da bude thread safe
	 *
	 * @param kuhinjaId    ID kuhinje
	 * @param kuhinjaNaziv naziv kuhinje
	 * @return true, ako je uspješno učitavanje
	 */
	protected synchronized boolean ucitajJelovnik(String kuhinjaId, String kuhinjaNaziv) {
		var nazivJelovnikDatoteke = kuhinjaId + ".json";
		var datoteka = Path.of(nazivJelovnikDatoteke);

		if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka) || !Files.isReadable(datoteka)) {
			return false;
		}

		try (var br = Files.newBufferedReader(datoteka)) {
			Gson gson = new Gson();
			Jelovnik[] jela = gson.fromJson(br, Jelovnik[].class);

			Map<String, Jelovnik> jelovnik = new ConcurrentHashMap<>();
			for (Jelovnik j : jela) {
				jelovnik.put(j.id(), j);
			}

			this.jelovnici.put(kuhinjaNaziv, jelovnik);
		} catch (IOException | JsonParseException e) {
			return false;
		}

		return true;
	}

	/**
	 * Ucitaj konfiguraciju.
	 *
	 * @param nazivDatoteke naziv datoteke
	 * @return true, ako je uspješno učitavanje konfiguracije
	 */
	public boolean ucitajKonfiguraciju(String nazivDatoteke) {
		try {
			this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
			return true;
		} catch (NeispravnaKonfiguracija ex) {
		}
		return false;
	}

	/**
	 * Prati status dretvi posluzitelja i čeka da svi posluzitelji završe.
	 *
	 * @param posluzitelji lista posluzitelja
	 */
	private void pratiStatusPosluzitelja(List<Future<?>> posluzitelji) {
		while (!this.kraj.get()) {
			try {
				boolean sviGotovi = posluzitelji.stream().allMatch(Future::isDone);
				if (sviGotovi)
					break;
				Thread.sleep(this.pauzaDretve);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Obrada komande za kraj Tvrtke.
	 *
	 * @param linija ulazna linija
	 * @param out    izlazni tok
	 */
	private void obradiKomandaKraj(PrintWriter out) {
		boolean sviOK = true;
		for (Partner p : this.partneri.values()) {
			try (Socket sock = new Socket(p.adresa(), p.mreznaVrataKraj());
					PrintWriter pw = new PrintWriter(
							new OutputStreamWriter(sock.getOutputStream(), StandardCharsets.UTF_8), true);
					BufferedReader br = new BufferedReader(
							new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8))) {

				postaviTimeout(sock, 5000);

				pw.write("KRAJ " + this.kodZaKraj + "\n");
				pw.flush();

				String resp = br.readLine();
				if (resp != null && !"OK".equals(resp)) {
					sviOK = false;
					break;
				}
			} catch (SocketTimeoutException ex) {
				continue;
			} catch (IOException e) {
				sviOK = false;
				break;
			}
		}

		if (!sviOK) {
			out.write("ERROR 14 - Barem jedan partner nije završio rad\n");
			out.flush();
			return;
		}

		try {
			String base = this.konfig.dajPostavku("restAdresa");
			URL url = new URI(base + "/kraj/info").toURL();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);

			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				out.write("OK\n");
				this.kraj.set(true);
			} else {
				out.write("ERROR 17 - RESTful zahtjev nije uspješan\n");
			}
			conn.disconnect();
		} catch (IOException | URISyntaxException e) {
			out.write("ERROR 17 - RESTful zahtjev nije uspješan\n");
		} finally {
			out.flush();
		}
	}

	/**
	 * Obrada komande za provjeru statusa poslužitelja.
	 *
	 * @param linija ulazna linija
	 * @param out    izlazni tok
	 */
	private void obradiStatus(String linija, PrintWriter out) {
		Matcher statusMatcher = STATUS_CMD.matcher(linija);
		if (!statusMatcher.matches()) {
			out.write("ERROR 20 - Format komande nije ispravan\n");
			return;
		}

		String adminKod = statusMatcher.group(1);
		int id = Integer.parseInt(statusMatcher.group(2));

		try {
			if (!this.adminKodTvrtke.equals(adminKod)) {
				out.write("ERROR 12 - Pogrešan kodZaAdminTvrtke\n");
				return;
			} else if (id != 1 && id != 2) {
				out.write("ERROR 20 - Format komande nije ispravan\n");
				return;
			} else {
				int stanje = (id == 1 ? (registracijaPauza.get() ? 0 : 1) : (partnerPauza.get() ? 0 : 1));
				out.write("OK " + stanje + "\n");
			}
		} finally {
			out.flush();
		}
	}

	/**
	 * Obrada komande za pauziranje poslužitelja.
	 *
	 * @param linija ulazna linija
	 * @param out    izlazni tok
	 */
	private void obradiPauzu(String linija, PrintWriter out) {
		Matcher pauzaMatcher = PAUZA_CMD.matcher(linija);
		if (!pauzaMatcher.matches()) {
			out.write("ERROR 20 - Format komande nije ispravan\n");
			return;
		}

		String adminKod = pauzaMatcher.group(1);
		int id = Integer.parseInt(pauzaMatcher.group(2));

		try {
			if (!this.adminKodTvrtke.equals(adminKod)) {
				out.write("ERROR 12 - Pogrešan kodZaAdminTvrtke\n");
				return;
			} else if (id != 1 && id != 2) {
				out.write("ERROR 20 - Format komande nije ispravan\n");
				return;
			} else {
				boolean vecPauziran = (id == 1 ? registracijaPauza.get() : partnerPauza.get());

				if (vecPauziran) {
					out.write("ERROR 13 - Pogrešna promjena pauze ili starta\n");
					return;
				} else {
					if (id == 1) {
						registracijaPauza.set(true);
					} else {
						partnerPauza.set(true);
					}
					out.write("OK\n");
				}
			}
		} finally {
			out.flush();
		}
	}

	/**
	 * Obrada komande za pokretanje poslužitelja.
	 *
	 * @param linija ulazna linija
	 * @param out    izlazni tok
	 */
	private void obradiStart(String linija, PrintWriter out) {
		Matcher startMatcher = START_CMD.matcher(linija);
		if (!startMatcher.matches()) {
			out.write("ERROR 20 - Format komande nije ispravan\n");
			return;
		}

		String adminKod = startMatcher.group(1);
		int id = Integer.parseInt(startMatcher.group(2));

		try {
			if (!this.adminKodTvrtke.equals(adminKod)) {
				out.write("ERROR 12 - Pogrešan kodZaAdminTvrtke\n");
				return;
			} else if (id != 1 && id != 2) {
				out.write("ERROR 20 - Format komande nije ispravan\n");
				return;
			} else {
				boolean pauziran = (id == 1 ? registracijaPauza.get() : partnerPauza.get());
				if (!pauziran) {
					out.write("ERROR 13 - Pogrešna promjena pauze ili starta\n");
				} else {
					if (id == 1) {
						registracijaPauza.set(false);
					} else {
						partnerPauza.set(false);
					}
					out.write("OK\n");
				}
			}
		} finally {
			out.flush();
		}
	}

	/**
	 * Obrada komandu za spavanje poslužitelja.
	 *
	 * @param linija ulazna linija
	 * @param out    izlazni tok
	 */
	private void obradiSpava(String linija, PrintWriter out) {
		Matcher spavaMatcher = SPAVA_CMD.matcher(linija);
		if (!spavaMatcher.matches()) {
			out.write("ERROR 20 - Format komande nije ispravan\n");
			return;
		}

		String adminKod = spavaMatcher.group(1);
		int trajanje = Integer.parseInt(spavaMatcher.group(2));

		try {
			if (!this.adminKodTvrtke.equals(adminKod)) {
				out.write("ERROR 12 - Pogrešan kodZaAdminTvrtke\n");
				return;
			} else {
				try {
					Thread.sleep(trajanje);
					out.write("OK\n");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					out.write("ERROR 16 - Prekid spavanja dretve\n");
					return;
				}
			}
		} finally {
			out.flush();
		}
	}

	/**
	 * Obrada komande za slanje KRAJ komande partnerima na novi port.
	 *
	 * @param linija ulazna linija
	 * @param out    izlazni tok
	 */
	private void obradiKrajWS(String linija, PrintWriter out) {
		Matcher krajWSMatcher = KRAJWS_CMD.matcher(linija);
		if (!krajWSMatcher.matches()) {
			out.write("ERROR 20 - Format komande nije ispravan\n");
			return;
		}

		String kod = krajWSMatcher.group(1);

		if (!this.kodZaKraj.equals(kod)) {
			out.write("ERROR 10 - Format komande nije ispravan ili nije ispravan kod za kraj\n");
			out.flush();
			return;
		}

		boolean sviOK = true;

		for (Partner p : this.partneri.values()) {
			try {
				Socket sock = new Socket();
				sock.connect(new InetSocketAddress(p.adresa(), p.mreznaVrataKraj()), 10000);
				this.postaviTimeout(sock, 5000);

				try (sock;
						PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), "utf8"), true);
						BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream(), "utf8"))) {

					pw.write("KRAJ " + kod + "\n");
					pw.flush();

					String resp = br.readLine();
					if (!"OK".equals(resp)) {
						sviOK = false;
						break;
					}
				}
			} catch (IOException e) {
				continue;
			}
		}

		if (sviOK) {
			out.write("OK\n");
			this.kraj.set(true);
		} else {
			out.write("ERROR 14 - Barem jedan partner nije završio rad\n");
		}
		out.flush();
	}

	/**
	 * Obrada komande za osvježavanje podataka.
	 *
	 * @param linija ulazna linija
	 * @param out    izlazni tok
	 */
	private void obradiOsvjezi(String linija, PrintWriter out) {
		Matcher m = OSVJEZI_CMD.matcher(linija);
		if (!m.matches()) {
			out.write("ERROR 20 - Format komande nije ispravan\n");
			out.flush();
			return;
		}

		String adminKod = m.group(1);

		try {
			if (!this.adminKodTvrtke.equals(adminKod)) {
				out.write("ERROR 12 - Pogrešan kodZaAdminTvrtke\n");
			} else if (partnerPauza.get()) {
				out.write("ERROR 15 - Poslužitelj za partnere u pauzi\n");
			} else {
				try {
					ucitajKartuPica();
					ucitajKuhinje();
					out.write("OK\n");
				} catch (Exception e) {
					out.write("ERROR 19 - Nešto drugo nije u redu\n");
				}
			}
		} finally {
			out.flush();
		}
	}

	/**
	 * Obrada zahtjeva za registraciju partnera.
	 *
	 * @param mreznaUticnica mrežna utičnica
	 */
	private void obradiKomandeZaRegistracijuPartnera(Socket mreznaUticnica) {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"))) {
			String linija = in.readLine();
			if (!provjeriPostojanjeUnosaLinije(linija)) {
				out.write("ERROR 29 - Nešto drugo nije u redu\n");
				return;
			}

			if (POPIS_CMD.matcher(linija).matches()) {
				obradiPopis(out);
			} else if (OBRISI_PARTNERA_CMD.matcher(linija).matches()) {
				obradiBrisanjePartnera(linija, out);
			} else if (REGISTRACIJA_PARTNERA_CMD.matcher(linija).matches()) {
				obradiRegistracijuPartnera(linija, out);
			} else {
				out.write("ERROR 20 - Format komande nije ispravan\n");
			}

		} catch (IOException e) {
			this.ispisiGresku(mreznaUticnica, "ERROR 29 - Nešto drugo nije u redu");
		} finally {
			try {
				mreznaUticnica.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Obrada zahtjeva za rad partnera.
	 *
	 * @param mreznaUticnica mrežna utičnica
	 */
	private void obradiKomandeZaRadPartnera(Socket mreznaUticnica) {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));) {
			String linija = in.readLine();

			if (!provjeriPostojanjeUnosaLinije(linija)) {
				out.write("ERROR 19\n");
				out.flush();
				return;
			}

			if (linija.startsWith("OBRAČUN") || linija.startsWith("OBRAČUNWS")) {
				try {
					linija = procitajCijeluKomandu(linija, in);
				} catch (IOException e) {
					this.ispisiGresku(mreznaUticnica, "ERROR 35 - Neispravan obračun\n");
				}
			}

			if (JELOVNIK_CMD.matcher(linija).matches()) {
				obradiJelovnik(linija, out);
			} else if (KARTAPICA_CMD.matcher(linija).matches()) {
				obradiKartuPica(linija, out);
			} else if (OBRACUN_CMD.matcher(linija).matches()) {
				obradiObracun(linija, out);
			} else if (OBRACUNWS_CMD.matcher(linija).matches()) {
				obradiObracunWS(linija, out);
			} else {
				out.write("ERROR 30 - Format komande nije ispravan\n");
				out.flush();
			}
		} catch (IOException | JsonSyntaxException e) {
			this.ispisiGresku(mreznaUticnica, "ERROR 35 - Neispravan obračun\n");
		} catch (Exception e) {
			this.ispisiGresku(mreznaUticnica, "ERROR 39 - Nešto drugo nije u redu\n");
		}
	}

	/**
	 * Obrada komande popis partnera.
	 *
	 * @param out izlazni tok
	 */
	private void obradiPopis(PrintWriter out) {
		Gson gson = new Gson();
		List<PartnerPopis> popis = partneri.values().stream().map(p -> new PartnerPopis(p.id(), p.naziv(),
				p.vrstaKuhinje(), p.adresa(), p.mreznaVrata(), p.gpsSirina(), p.gpsDuzina())).toList();
		out.write("OK\n");
		out.write(gson.toJson(popis) + "\n");
	}

	/**
	 * Obrada komande za brisanje partnera.
	 *
	 * @param linija ulazna linija
	 * @param out    izlazni tok
	 */
	private void obradiBrisanjePartnera(String linija, PrintWriter out) {
		try {
			Matcher obrisiMatcher = OBRISI_PARTNERA_CMD.matcher(linija);
			if (!obrisiMatcher.matches()) {
				out.write("ERROR 20 - Format komande nije ispravan\n");
				return;
			}

			int id = Integer.parseInt(obrisiMatcher.group(1));
			String sigKod = obrisiMatcher.group(2);

			Partner partner = this.partneri.get(id);

			if (partner == null) {
				out.write(
						"ERROR 23 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
				return;
			}

			if (!partner.sigurnosniKod().equals(sigKod)) {
				out.write("ERROR 22 - Neispravan sigurnosni kod partnera\n");
				return;
			}

			this.partneri.remove(id);
			spremiPartnere();
			out.write("OK\n");
		} catch (NumberFormatException e) {
			out.write("ERROR 20 - Format komande nije ispravan\n");
		} catch (Exception e) {
			out.write("ERROR 29 - Nešto drugo nije u redu\n");
		} finally {
			out.flush();
		}
	}

	/**
	 * Obrada komande za registraciju partnera.
	 *
	 * @param linija ulazna linija
	 * @param out    izlazni tok
	 */
	private void obradiRegistracijuPartnera(String linija, PrintWriter out) {
		try {
			Matcher registracijaMatcher = REGISTRACIJA_PARTNERA_CMD.matcher(linija);
			if (!registracijaMatcher.matches()) {
				out.write("ERROR 20 - Format komande nije ispravan\n");
				return;
			}

			if (this.registracijaPauza.get()) {
				out.write("ERROR 24 - Poslužitelj za registraciju partnera u pauzi\n");
				return;
			}

			int id = Integer.parseInt(registracijaMatcher.group(1));
			String naziv = registracijaMatcher.group(2);
			String vrstaKuhinje = registracijaMatcher.group(3);
			String adresa = registracijaMatcher.group(4);
			int mreznaVrata = Integer.parseInt(registracijaMatcher.group(5));
			float gpsSirina = Float.parseFloat(registracijaMatcher.group(6));
			float gpsDuzina = Float.parseFloat(registracijaMatcher.group(7));
			int mreznaVrataKraj = Integer.parseInt(registracijaMatcher.group(8));
			String adminKod = registracijaMatcher.group(9);

			if (!provjeriPostojanjeKuhinje(vrstaKuhinje)) {
				out.write("ERROR 20 - Format komande nije ispravan\n");
				return;
			}

			if (this.partneri.containsKey(id)) {
				out.write("ERROR 21 - Već postoji partner s id u kolekciji partnera\n");
				return;
			}

			String sigurnosniKod = kreirajPartnera(id, naziv, vrstaKuhinje, adresa, mreznaVrata, gpsSirina, gpsDuzina,
					mreznaVrataKraj, adminKod);
			spremiPartnere();
			out.printf("OK %s\n", sigurnosniKod);
		} catch (NumberFormatException e) {
			out.write("ERROR 20 - Format komande nije ispravan\n");
		} catch (Exception e) {
			out.write("ERROR 29 - Nešto drugo nije u redu\n");
		} finally {
			out.flush();
		}
	}

	/**
	 * Obrada komande za ispis jelovnika.
	 *
	 * @param linija ulazna linija
	 * @param out    izlazni tok
	 */
	private void obradiJelovnik(String linija, PrintWriter out) {
		try {
			Matcher jelovnikMatcher = JELOVNIK_CMD.matcher(linija);
			if (!jelovnikMatcher.matches()) {
				out.write("ERROR 30 - Format komande nije ispravan\n");
				return;
			}

			int partnerId = Integer.parseInt(jelovnikMatcher.group(1));
			String sigKod = jelovnikMatcher.group(2);

			Partner partner = this.partneri.get(partnerId);
			if (partner == null || (partner != null && !partner.sigurnosniKod().equals(sigKod))) {
				out.write(
						"ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
				return;
			}

			if (this.kuhinje.get(partner.vrstaKuhinje()) == null
					|| this.jelovnici.get(partner.vrstaKuhinje()) == null) {
				out.write("ERROR 32 - Ne postoji jelovnik s vrsom kuhinje koju partner ima ugovorenu\n");
				return;
			}

			try {
				Map<String, Jelovnik> jelovnikMapa = this.jelovnici.get(partner.vrstaKuhinje());

				Gson gson = new Gson();
				List<Jelovnik> popisJela = jelovnikMapa.values().stream()
						.map(j -> new Jelovnik(j.id(), j.naziv(), j.cijena())).toList();
				out.write("OK\n");
				out.flush();
				out.write(gson.toJson(popisJela) + "\n");
			} catch (JsonParseException e) {
				out.write("ERROR 33 - Neispravan jelovnik\n");
				return;
			}

		} catch (NumberFormatException e) {
			out.write("ERROR 30 - Format komande nije ispravan\n");
			return;
		} catch (Exception e) {
			out.write("ERROR 39 - Nešto drugo nije u redu\n");
			return;
		} finally {
			out.flush();
		}
	}

	/**
	 * Obrada komande za ispis karte pića.
	 *
	 * @param linija ulazna linija
	 * @param out    izlazni tok
	 */
	private void obradiKartuPica(String linija, PrintWriter out) {
		Matcher jelovnikMatcher = KARTAPICA_CMD.matcher(linija);
		if (!jelovnikMatcher.matches()) {
			out.write("ERROR 30 - Format komande nije ispravan\n");
			return;
		}

		int partnerId = Integer.parseInt(jelovnikMatcher.group(1));
		String sigKod = jelovnikMatcher.group(2);

		Partner partner = this.partneri.get(partnerId);
		if (partner == null || !partner.sigurnosniKod().equals(sigKod)) {
			out.write(
					"ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
			return;
		}

		try {
			Gson gson = new Gson();
			List<KartaPica> popis = kartaPica.values().stream()
					.map(kp -> new KartaPica(kp.id(), kp.naziv(), kp.kolicina(), kp.cijena())).toList();
			out.write("OK\n");
			out.write(gson.toJson(popis) + "\n");
		} catch (JsonParseException e) {
			out.write("ERROR 34 - Neispravan obračun\n");
			return;
		} catch (Exception e) {
			out.write("ERROR 39 - Nešto drugo nije u redu\n");
			return;
		} finally {
			out.flush();
		}
	}

	/**
	 * Obrada komande za obračun. Synchronized zbog potrebe za thread safe načinom
	 * rada
	 *
	 * @param linija ulazna linija
	 * @param out    izlazni tok
	 */
	private synchronized void obradiObracun(String linija, PrintWriter out) {
		Matcher obracunMatcher = OBRACUN_CMD.matcher(linija);
		if (!obracunMatcher.matches()) {
			out.write("ERROR 30 - Format komande nije ispravan\n");
			return;
		}

		System.out.println("linija: " + linija);

		int partnerId = Integer.parseInt(obracunMatcher.group(1));
		String sigKod = obracunMatcher.group(2);
		String jsonObjekt = obracunMatcher.group(3);

		Partner partner = this.partneri.get(partnerId);
		if (partner == null || !partner.sigurnosniKod().equals(sigKod)) {
			out.write(
					"ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
			return;
		}

		if (jsonObjekt == null || jsonObjekt.isBlank() || !(jsonObjekt.startsWith("[") && jsonObjekt.endsWith("]"))) {
			out.write("ERROR 35 - Neispravan obračun\n");
			return;
		}

		this.obracunLock.lock();
		try {
			ucitajObracune();
			Gson gson = new Gson();
			List<Obracun> noviObracuni = Arrays.asList(gson.fromJson(jsonObjekt, Obracun[].class));

			if (noviObracuni.stream().anyMatch(o -> o.partner() != partnerId)) {
				out.write("ERROR 35 - Neispravan obračun\n");
				return;
			}

			upisiNoveObracune(noviObracuni);

			try {
				String restAdresa = this.konfig.dajPostavku("restAdresa");
				URL url = new URI(restAdresa + "/obracun").toURL();
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setDoOutput(true);
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);

				try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(),
						StandardCharsets.UTF_8)) {
					writer.write(jsonObjekt);
					writer.flush();
				}

				int responseCode = conn.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_CREATED) {
					out.write("OK\n");
				} else {
					out.write("ERROR 37 - RESTful zahtjev nije uspješan\n");
				}

				conn.disconnect();
			} catch (IOException | URISyntaxException e) {
				out.write("ERROR 37 - RESTful zahtjev nije uspješan\n");
			}

		} catch (JsonParseException e) {
			out.write("ERROR 35 - Neispravan obračun\n");
		} catch (Exception e) {
			out.write("ERROR 39 - Nešto drugo nije u redu\n");
		} finally {
			out.flush();
			this.obracunLock.unlock();
		}
	}

	/**
	 * Obrada komande za obračun.
	 *
	 * @param linija ulazna linija
	 * @param out    izlazni tok
	 */
	private synchronized void obradiObracunWS(String linija, PrintWriter out) {
		Matcher obracunMatcher = OBRACUNWS_CMD.matcher(linija);
		if (!obracunMatcher.matches()) {
			out.write("ERROR 30 - Format komande nije ispravan\n");
			return;
		}

		int partnerId = Integer.parseInt(obracunMatcher.group(1));
		String sigKod = obracunMatcher.group(2);
		String jsonObjekt = obracunMatcher.group(3);

		if (partnerPauza.get()) {
			out.write("ERROR 36 - Poslužitelj za partnere u pauzi\n");
			out.flush();
			return;
		}

		Partner partner = this.partneri.get(partnerId);
		if (partner == null || !partner.sigurnosniKod().equals(sigKod)) {
			out.write(
					"ERROR 31 - Ne postoji partner s id u kolekciji partnera i/ili neispravan sigurnosni kod partnera\n");
			return;
		}

		if (jsonObjekt == null || jsonObjekt.isBlank() || !(jsonObjekt.startsWith("[") && jsonObjekt.endsWith("]"))) {
			out.write("ERROR 35 - Neispravan obračun\n");
			return;
		}

		this.obracunLock.lock();
		try {
			ucitajObracune();
			Gson gson = new Gson();
			List<Obracun> noviObracuni = Arrays.asList(gson.fromJson(jsonObjekt, Obracun[].class));

			if (noviObracuni.stream().anyMatch(o -> o.partner() != partnerId)) {
				out.write("ERROR 35 - Neispravan obračun\n");
				return;
			}

			upisiNoveObracune(noviObracuni);
			out.write("OK\n");
		} catch (JsonParseException e) {
			out.write("ERROR 35 - Neispravan obračun\n");
		} catch (Exception e) {
			out.write("ERROR 39 - Nešto drugo nije u redu\n");
		} finally {
			out.flush();
			this.obracunLock.unlock();
		}
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
	 * Provjeri da li je linija komanda za kraj.
	 *
	 * @param linija linija
	 * @return true ako je komanda za kraj, inače false
	 */
	private boolean provjeriKomanduZaKraj(String linija) {
		return Pattern.compile("^\\s*KRAJ " + Pattern.quote(this.kodZaKraj) + "\\s*$").matcher(linija).matches();
	}

	/**
	 * Provjeri da li postoji kuhinja prema konfiguraciji.
	 *
	 * @param vrstaKuhinje vrsta kuhinje
	 * @return true ako postoji kuhinja, inače false
	 */
	private boolean provjeriPostojanjeKuhinje(String vrstaKuhinje) {
		return this.konfig.dajSvePostavke().entrySet().stream()
				.filter(e -> e.getKey().toString().startsWith("kuhinja_"))
				.map(e -> e.getValue().toString().split(";")[0]).anyMatch(vrsta -> vrsta.equals(vrstaKuhinje));
	}

	/**
	 * Metoda za isčitavanje svih redaka u OBRAČUN komandi.
	 *
	 * @param ulazna linija, BufferedReader
	 * @return linija sa svim redovima
	 */
	private String procitajCijeluKomandu(String linija, BufferedReader in) throws IOException {
		StringBuilder cijelaKomanda = new StringBuilder();
		cijelaKomanda.append(linija).append(" ");

		while (true) {
			String novaLinija = in.readLine();

			if (!provjeriPostojanjeUnosaLinije(novaLinija))
				break;

			cijelaKomanda.append(novaLinija);
			if (novaLinija.endsWith("]"))
				break;
		}

		return linija = cijelaKomanda.toString();
	}

	/**
	 * Kreira datoteku s opcionalnim upisom
	 * 
	 * @param datoteka
	 */
	private void kreirajDatoteku(Path datoteka, String pocetniTekst) throws IOException {
		Path parent = datoteka.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		Files.createFile(datoteka);

		if (pocetniTekst != null && !pocetniTekst.isBlank() && !pocetniTekst.isEmpty())
			Files.writeString(datoteka, pocetniTekst);
	}

	/**
	 * Kreira novog partnera
	 * 
	 * @param id
	 * @param naziv
	 * @param vrstaKuhinje
	 * @param adresa
	 * @param mreznaVrata
	 * @param mreznaVrataKraj
	 * @param gpsSirina
	 * @param gpsDuzina
	 * @param adminKod
	 * 
	 * @return sigurnosniKod
	 */
	private String kreirajPartnera(int id, String naziv, String vrstaKuhinje, String adresa, int mreznaVrata,
			float gpsSirina, float gpsDuzina, int mreznaVrataKraj, String adminKod) {
		String sigurnosniKod = Integer.toHexString((naziv + adresa).hashCode());

		Partner partner = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj, gpsSirina,
				gpsDuzina, sigurnosniKod, adminKod);

		this.partneri.put(id, partner);
		return sigurnosniKod;
	}

	/**
	 * Metoda koja odrađuje cijeli proces upisivanja novih obračuna u datoteku i
	 * memoriju
	 * 
	 * @param noviObracuni
	 * @throws NeispravnaKonfiguracija
	 */
	private synchronized void upisiNoveObracune(List<Obracun> noviObracuni) throws NeispravnaKonfiguracija {
		List<Obracun> temp = new ArrayList<>(this.obracuni);
		temp.addAll(noviObracuni);
		this.obracuni = Collections.synchronizedList(temp);
		spremiObracune();
	}

	/**
	 * Pomoćna metoda za ispisivanje greške.
	 */
	private void ispisiGresku(Socket socket, String errorMsg) {
		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf8"))) {
			out.write(errorMsg);
			out.flush();
		} catch (IOException e) {
		}
	}

	/**
	 * Priprema datoteku za rad. Ako datoteka ne postoji, stvara je. Ako nije obična
	 * datoteka ili nije moguće pisati u nju, ispisuje grešku.
	 *
	 * @param datoteka putanja do datoteke
	 * @param poruka   poruka za ispis greške
	 * @return true ako je sve u redu, inače false
	 */
	protected boolean pripremiDatoteku(Path datoteka, String poruka, String upis) {
		if (!Files.exists(datoteka)) {
			try {
				kreirajDatoteku(datoteka, upis);
			} catch (IOException ex) {
				return false;
			}
		}

		if (!Files.isRegularFile(datoteka) || !Files.isWritable(datoteka) || !Files.isReadable(datoteka)) {
			return false;
		}

		return true;
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

	/**
	 * Postavlja timeout za socket. Ovo se koristi kod KRAJ komande u tvrtci zato
	 * što postoji i KRAJ komanda u partneru koja kada se aktivira gasi partnera, a
	 * tvrtka nikako ne može znati da je partner ugašen stoga bi ušla u beskonačnu
	 * petlju čekajući odgovor od partnera.
	 *
	 * @param socket  socket
	 * @param vrijeme vrijeme u milisekundama
	 */
	private void postaviTimeout(Socket socket, int vrijeme) {
		try {
			socket.setSoTimeout(vrijeme);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

}
