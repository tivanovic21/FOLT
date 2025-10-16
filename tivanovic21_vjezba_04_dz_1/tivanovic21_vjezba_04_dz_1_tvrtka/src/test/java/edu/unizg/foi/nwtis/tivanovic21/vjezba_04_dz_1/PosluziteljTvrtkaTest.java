package edu.unizg.foi.nwtis.tivanovic21.vjezba_04_dz_1;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.vjezba_04_dz_1.podaci.KartaPica;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PosluziteljTvrtkaTest {
    private static final String CLASS_NAME = PosluziteljTvrtkaTest.class.getName();
    private static final List<Path> tempDatoteke = new ArrayList<>();
    private PosluziteljTvrtka testnaTvrtka;
    private String originalKodZaKraj;
    private int originalPauzaDretve;
    private AtomicBoolean originalKraj;

    @BeforeAll
    static void setUpBeforeClass() {
        tempDatoteke.clear();
    }

    @AfterAll
    static void tearDownAfterClass() {
        obrisiTempDatoteke();
    }

    @BeforeEach
    void setUp() throws Exception {
        this.testnaTvrtka = new PosluziteljTvrtka();
        
        this.originalKodZaKraj = dohvatiPrivatniAtribut(this.testnaTvrtka, "kodZaKraj");
        this.originalPauzaDretve = dohvatiPrivatniAtribut(this.testnaTvrtka, "pauzaDretve");
        this.originalKraj = dohvatiPrivatniAtribut(this.testnaTvrtka, "kraj");
    }

    @AfterEach
    void tearDown() {
        try {
            postaviPrivatniAtribut(this.testnaTvrtka, "kodZaKraj", this.originalKodZaKraj);
            postaviPrivatniAtribut(this.testnaTvrtka, "pauzaDretve", this.originalPauzaDretve);
            postaviPrivatniAtribut(this.testnaTvrtka, "kraj", this.originalKraj);
        } catch (Exception e) {
        }
        
        this.testnaTvrtka = null;
        obrisiTempDatoteke();
    }

    /**
	 * Helper metoda za kreiranje privremenih datoteka
	 */
    private static Path kreirajTempDatoteku(String nazivDatoteke, String sadrzaj) throws IOException {
        Path path = Path.of(nazivDatoteke);
        Files.writeString(path, sadrzaj);
        tempDatoteke.add(path);
        return path;
    }
    
    /**
     * Helper metoda za kreiranje privremenih konfiguracijskih datoteka
     */
    private static Konfiguracija kreirajTempKonfigDatoteku(String nazivDatoteke) throws NeispravnaKonfiguracija {
        Konfiguracija konfig = KonfiguracijaApstraktna.kreirajKonfiguraciju(nazivDatoteke);
        tempDatoteke.add(Path.of(nazivDatoteke));
        return konfig;
    }
    
    /**
     * Helper metoda za brisanje privremenih datoteka
     */
    private static void obrisiTempDatoteke() {
        for (Path path : tempDatoteke) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
            }
        }
        tempDatoteke.clear();
    }

    /**
     * Pomoćna metoda za pozivanje privatnih metoda putem refleksije
     * 
     * @param objekt           objekt nad kojim se poziva metoda
     * @param imeMetode        ime metode koja se poziva
     * @param tipoviParametara tipovi parametara metode
     * @param argumenti        argumenti koji se prosljeđuju metodi
     * @return rezultat pozivanja metode
     * @throws iznimku ako se desi prilikom refleksije
     */
    private Object pozoviPrivatnuMetodu(Object objekt, String imeMetode, Class<?>[] tipoviParametara,
            Object... argumenti) throws Exception {
        Method method = objekt.getClass().getDeclaredMethod(imeMetode, tipoviParametara);
        method.setAccessible(true);
        return method.invoke(objekt, argumenti);
    }

    /**
     * Pomoćna metoda za pozivanje privatnih metoda putem refleksije bez parametara
     * 
     * @param objekt    objekt nad kojim se poziva metoda
     * @param imeMetode ime metode koja se poziva
     * @return rezultat pozivanja metode
     * @throws iznimku ako se desi prilikom refleksije
     */
    private Object pozoviPrivatnuMetoduBezParametara(Object objekt, String imeMetode) throws Exception {
        return pozoviPrivatnuMetodu(objekt, imeMetode, new Class<?>[0]);
    }

    /**
     * Pomoćna metoda za pristup privatnim atributima putem refleksije
     * 
     * @param objekt      objekt nad kojim se pristupa polju
     * @param imeAtributa ime polja kojem se pristupa
     * @return vrijednost polja
     * @throws iznimku ako se desi prilikom refleksije
     */
    @SuppressWarnings("unchecked")
    private <T> T dohvatiPrivatniAtribut(Object objekt, String imeAtributa) throws Exception {
        Field field = objekt.getClass().getDeclaredField(imeAtributa);
        field.setAccessible(true);
        return (T) field.get(objekt);
    }

    /**
     * Pomoćna metoda za postavljanje privatnih atributa putem refleksije
     * 
     * @param objekt      objekt nad kojim se pristupa polju
     * @param imeAtributa ime polja kojem se pristupa
     * @param vrijednost  nova vrijednost polja
     * @throws iznimku ako se desi prilikom refleksije
     */
    private void postaviPrivatniAtribut(Object objekt, String imeAtributa, Object vrijednost) throws Exception {
        Field field = objekt.getClass().getDeclaredField(imeAtributa);
        field.setAccessible(true);
        field.set(objekt, vrijednost);
    }

    @Test
    @Order(1)
    void testUcitajKonfiguraciju() {
        try {
            String nazivDatoteke = CLASS_NAME + "_test1.txt";
            Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);

            konfig.spremiPostavku(CLASS_NAME, "1");
            konfig.spremiPostavku("2", CLASS_NAME);
            konfig.spremiPostavku("3", "4");
            konfig.spremiPostavku(CLASS_NAME, CLASS_NAME);

            konfig.spremiKonfiguraciju();

            assertTrue(this.testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke), "Problem kod učitavanja datoteke.");

            var props1 = konfig.dajSvePostavke();
            var props2 = this.testnaTvrtka.konfig.dajSvePostavke();

            var kljucevi1 = props1.keySet().stream().sorted().toArray();
            var kljucevi2 = props2.keySet().stream().sorted().toArray();

            assertArrayEquals(kljucevi1, kljucevi2, "Ključevi nisu isti");

            var vrijednosti1 = props1.values().stream().sorted().toArray();
            var vrijednosti2 = props2.values().stream().sorted().toArray();

            assertArrayEquals(vrijednosti1, vrijednosti2, "Vrijednosti nisu iste");

            for (var p : props1.keySet()) {
                if (!props2.get((String) p).equals(props1.get(p))) {
                    fail("Nema sve postavke.");
                }
            }
        } catch (NeispravnaKonfiguracija e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(2)
    void testUcitajKonfiguraciju_SaIspravnimAtributima() {
        try {
            String nazivDatoteke = CLASS_NAME + "_ispravno.txt";
            Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);

            konfig.spremiPostavku("kodZaKraj", "KRAJ");
            konfig.spremiPostavku("pauzaDretve", "1000");
            konfig.spremiPostavku("brojCekaca", "10");
            konfig.spremiPostavku("mreznaVrataKraj", "8000");
            konfig.spremiPostavku("mreznaVrataRegistracija", "8001");
            konfig.spremiPostavku("mreznaVrataRad", "8002");
            konfig.spremiPostavku("datotekaKartaPica", "kartaPica.json");
            konfig.spremiPostavku("datotekaObracuna", "obracuni.json");
            konfig.spremiPostavku("datotekaPartnera", "partneri.json");

            konfig.spremiKonfiguraciju();

            assertTrue(this.testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke),
                    "Problem kod učitavanja datoteke s validnim atributima.");

            var props1 = konfig.dajSvePostavke();
            var props2 = this.testnaTvrtka.konfig.dajSvePostavke();

            var kljucevi1 = props1.keySet().stream().sorted().toArray();
            var kljucevi2 = props2.keySet().stream().sorted().toArray();

            assertArrayEquals(kljucevi1, kljucevi2, "Ključevi nisu isti");

            var vrijednosti1 = props1.values().stream().sorted().toArray();
            var vrijednosti2 = props2.values().stream().sorted().toArray();

            assertArrayEquals(vrijednosti1, vrijednosti2, "Vrijednosti nisu iste");

            for (var p : props1.keySet()) {
                if (!props2.get((String) p).equals(props1.get(p))) {
                    fail("Nema sve postavke.");
                }
            }
        } catch (NeispravnaKonfiguracija e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(3)
    void testUcitajKonfiguraciju_SaNeispravnimNazivomDatoteke() {
        assertFalse(this.testnaTvrtka.ucitajKonfiguraciju("ja_ne_postojim.txt"),
                "Metoda treba vratiti false za nepostojeću datoteku.");
    }

    @Test
    @Order(4)
    void testUcitajKonfiguraciju_SaCustomAtributima() {
        try {
            String nazivDatoteke = CLASS_NAME + "_custom.txt";
            Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);

            konfig.spremiPostavku("kodZaKraj", "KRAJ");
            konfig.spremiPostavku("pauzaDretve", "1000");

            konfig.spremiPostavku("noviAtribut", "zaTestiranje");
            konfig.spremiPostavku("josJedanAtribut", "ponovnoZaTestiranje");

            konfig.spremiKonfiguraciju();

            assertTrue(this.testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke),
                    "Problem kod učitavanja datoteke s mješovitim atributima.");

            assertEquals("KRAJ", this.testnaTvrtka.konfig.dajPostavku("kodZaKraj"), "kodZaKraj nije ispravno učitan");
            assertEquals("1000", this.testnaTvrtka.konfig.dajPostavku("pauzaDretve"),
                    "pauzaDretve nije ispravno učitan");

            assertEquals("zaTestiranje", this.testnaTvrtka.konfig.dajPostavku("noviAtribut"),
                    "noviAtribut nije ispravno učitan");
            assertEquals("ponovnoZaTestiranje", this.testnaTvrtka.konfig.dajPostavku("josJedanAtribut"),
                    "josJedanAtribut nije ispravno učitan");
        } catch (NeispravnaKonfiguracija e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(5)
    void testUcitajKartuPica_SaIspravnimJsonom() {
        try {
            String nazivDatoteke = CLASS_NAME + "_karta_pica.json";
            String konfDatoteke = CLASS_NAME + "_konfig_karta.txt";

            KartaPica[] kartaPica = { new KartaPica("1", "Studena", 0.5f, 2.5f),
                    new KartaPica("2", "Coca Cola", 0.5f, 2.5f), new KartaPica("3", "Kava s mlijekom", 0.5f, 2.5f) };

            Gson gson = new Gson();
            String json = gson.toJson(kartaPica);
            kreirajTempDatoteku(nazivDatoteke, json);

            Konfiguracija konfig = kreirajTempKonfigDatoteku(konfDatoteke);
            konfig.spremiPostavku("datotekaKartaPica", nazivDatoteke);
            konfig.spremiKonfiguraciju();

            this.testnaTvrtka.ucitajKonfiguraciju(konfDatoteke);

            boolean rezultat = this.testnaTvrtka.ucitajKartuPica();

            assertTrue(rezultat, "Method treba vratiti true za isravnu kartu pića.");
        } catch (IOException | NeispravnaKonfiguracija e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(6)
    void testUcitajKartuPica_SaNepostojecomDatotekom() {
        try {
            String nazivDatoteke = "ja_ne_postojim.json";
            String konfDatoteke = CLASS_NAME + "_konfig_nepostojeca.txt";

            Konfiguracija konfig = kreirajTempKonfigDatoteku(konfDatoteke);
            konfig.spremiPostavku("datotekaKartaPica", nazivDatoteke);
            konfig.spremiKonfiguraciju();

            this.testnaTvrtka.ucitajKonfiguraciju(konfDatoteke);

            boolean rezultat = this.testnaTvrtka.ucitajKartuPica();

            assertFalse(rezultat, "Metoda treba vratiti false za nepostojeću datoteku.");
        } catch (NeispravnaKonfiguracija e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(7)
    void testUcitajKartuPica_SaNeispravnimJsonom() {
        String nazivDatoteke = CLASS_NAME + "_neispravan_json.json";
        String konfDatoteke = CLASS_NAME + "_konfig_neispravan.txt";

        try {
            String neispravanJson = "Ja nisam JSON";
            kreirajTempDatoteku(nazivDatoteke, neispravanJson);

            Konfiguracija konfig = kreirajTempKonfigDatoteku(konfDatoteke);
            konfig.spremiPostavku("datotekaKartaPica", nazivDatoteke);
            konfig.spremiKonfiguraciju();

            this.testnaTvrtka.ucitajKonfiguraciju(konfDatoteke);

            assertThrows(JsonSyntaxException.class, () -> {
                this.testnaTvrtka.ucitajKartuPica();
            }, "Očekivana iznimka JsonSyntaxException");

        } catch (IOException | NeispravnaKonfiguracija e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(8)
    void testUcitajKartuPica_SaNepotpunimJsonom() {
        String nazivDatoteke = CLASS_NAME + "_nepotpun_json.json";
        String konfDatoteke = CLASS_NAME + "_konfig_nepotpun.txt";

        try {
            String nepotpunJson = "[{\"id\": \"1\", \"naziv\": \"Studena\", \"cijena\": 2.5}";
            kreirajTempDatoteku(nazivDatoteke, nepotpunJson);

            Konfiguracija konfig = kreirajTempKonfigDatoteku(konfDatoteke);
            konfig.spremiPostavku("datotekaKartaPica", nazivDatoteke);
            konfig.spremiKonfiguraciju();

            this.testnaTvrtka.ucitajKonfiguraciju(konfDatoteke);

            assertThrows(JsonSyntaxException.class, () -> {
                this.testnaTvrtka.ucitajKartuPica();
            }, "Očekivana iznimka JsonSyntaxException");

        } catch (IOException | NeispravnaKonfiguracija e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(9)
    void testUcitajKartuPica_SaPraznimJsonom() {
        try {
            String nazivDatoteke = CLASS_NAME + "_prazan.json";
            String konfDatoteke = CLASS_NAME + "_konfig_prazan.txt";

            String prazanJson = "[]";
            kreirajTempDatoteku(nazivDatoteke, prazanJson);

            Konfiguracija konfig = kreirajTempKonfigDatoteku(konfDatoteke);
            konfig.spremiPostavku("datotekaKartaPica", nazivDatoteke);
            konfig.spremiKonfiguraciju();

            this.testnaTvrtka.ucitajKonfiguraciju(konfDatoteke);

            boolean rezultat = this.testnaTvrtka.ucitajKartuPica();

            assertTrue(rezultat, "Metoda treba vratiti true za prazan JSON");
        } catch (IOException | NeispravnaKonfiguracija e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(10)
    void testUcitajKuhinje_SaIspravnimPodacima() {
        try {
            String nazivDatoteke = CLASS_NAME + "_kuhinje_test.txt";
            Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);

            konfig.spremiPostavku("kuhinja_1_test", "Talijanska;Talijanska kuhinja");
            konfig.spremiPostavku("kuhinja_2_test", "Francuska;Francuska kuhinja");
            konfig.spremiKonfiguraciju();

            String jelovnikDatoteka1 = "kuhinja_1_test.json";
            String jelovnikJson1 = "[{\"id\":\"1\", \"naziv\":\"Jelo 1\", \"cijena\":10.5}, {\"id\":\"2\", \"naziv\":\"Jelo 2\", \"cijena\":15.0}]";
            kreirajTempDatoteku(jelovnikDatoteka1, jelovnikJson1);

            String jelovnikDatoteka2 = "kuhinja_2_test.json";
            String jelovnikJson2 = "[{\"id\":\"1\", \"naziv\":\"Jelo 1\", \"cijena\":10.5}, {\"id\":\"2\", \"naziv\":\"Jelo 2\", \"cijena\":15.0}]";
            kreirajTempDatoteku(jelovnikDatoteka2, jelovnikJson2);

            this.testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke);

            boolean rezultat = this.testnaTvrtka.ucitajKuhinje();

            assertTrue(rezultat, "Metoda treba vratiti true za ispravne podatke");
        } catch (NeispravnaKonfiguracija | IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Order(11)
    void testUcitajKuhinje_BezKonfiguracije() {
        Konfiguracija originalKonfig = this.testnaTvrtka.konfig;

        try {
            this.testnaTvrtka.konfig = null;

            boolean rezultat = this.testnaTvrtka.ucitajKuhinje();

            assertFalse(rezultat, "Metoda treba vratiti false kada se desi greška (npr. konfiguracija je null)");
        } finally {
            this.testnaTvrtka.konfig = originalKonfig;
        }
    }

    @Test
    @Order(12)
    void testUcitajKonfiguracijskePodatke() throws Exception {
        String nazivDatoteke = CLASS_NAME + "_konfig.txt";
        Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);

        konfig.spremiPostavku("kodZaKraj", "TEST_KRAJ");
        konfig.spremiPostavku("pauzaDretve", "2000");
        konfig.spremiPostavku("brojCekaca", "15");
        konfig.spremiKonfiguraciju();

        this.testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke);

        pozoviPrivatnuMetoduBezParametara(this.testnaTvrtka, "ucitajKonfiguracijskePodatke");

        String kodZaKraj = dohvatiPrivatniAtribut(this.testnaTvrtka, "kodZaKraj");
        int pauzaDretve = dohvatiPrivatniAtribut(this.testnaTvrtka, "pauzaDretve");
        int brojCekaca = dohvatiPrivatniAtribut(this.testnaTvrtka, "brojCekaca");

        assertEquals("TEST_KRAJ", kodZaKraj, "kodZaKraj nije ispravno postavljen");
        assertEquals(2000, pauzaDretve, "pauzaDretve nije ispravno postavljena");
        assertEquals(15, brojCekaca, "brojCekaca nije ispravno postavljen");
    }

    @Test
    @Order(13)
    void testUcitajKonfiguracijskePodatke_SaNeispravnimBrojem() throws Exception {
        String nazivDatoteke = CLASS_NAME + "_neispravan_broj.txt";
        Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);

        konfig.spremiPostavku("kodZaKraj", "TEST_KRAJ");
        konfig.spremiPostavku("pauzaDretve", "JA_NISAM_BROJ");
        konfig.spremiPostavku("brojCekaca", "15");
        konfig.spremiKonfiguraciju();

        this.testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke);

        assertThrows(InvocationTargetException.class, () -> {
            pozoviPrivatnuMetoduBezParametara(this.testnaTvrtka, "ucitajKonfiguracijskePodatke");
        }, "Očekivana iznimka InvocationTargetException");
    }

    @Test
    @Order(14)
    void testUcitajKonfiguracijskePodatke_BezSvihVrijednosti() throws Exception {
        String nazivDatoteke = CLASS_NAME + "_nepotpun_konfig.txt";
        Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);

        konfig.spremiPostavku("kodZaKraj", "TEST_KRAJ");
        konfig.spremiKonfiguraciju();

        this.testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke);

        assertThrows(InvocationTargetException.class, () -> {
            pozoviPrivatnuMetoduBezParametara(this.testnaTvrtka, "ucitajKonfiguracijskePodatke");
        }, "Očekivana iznimka InvocationTargetException");
    }

    @Test
    @Order(15)
    void testUcitajKonfiguracijskePodatke_OcuvanjeOriginalnihVrijednosti() throws Exception {
        try {
            postaviPrivatniAtribut(this.testnaTvrtka, "kodZaKraj", "INICIJALNA_VRIJEDNOST");
            postaviPrivatniAtribut(this.testnaTvrtka, "pauzaDretve", 5000);

            String nazivDatoteke = CLASS_NAME + "_konfig_ocuvanje.txt";
            Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);

            konfig.spremiPostavku("kodZaKraj", "NOVA_VRIJEDNOST");
            konfig.spremiPostavku("pauzaDretve", "1500");
            konfig.spremiPostavku("brojCekaca", "25");
            konfig.spremiKonfiguraciju();

            this.testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke);

            pozoviPrivatnuMetoduBezParametara(this.testnaTvrtka, "ucitajKonfiguracijskePodatke");

            String noviKodZaKraj = dohvatiPrivatniAtribut(this.testnaTvrtka, "kodZaKraj");
            int novaPauzaDretve = dohvatiPrivatniAtribut(this.testnaTvrtka, "pauzaDretve");

            assertNotEquals("INICIJALNA_VRIJEDNOST", noviKodZaKraj, "kodZaKraj bi trebao biti ažuriran");
            assertNotEquals(5000, novaPauzaDretve, "pauzaDretve bi trebala biti ažurirana");

            assertEquals("NOVA_VRIJEDNOST", noviKodZaKraj, "kodZaKraj bi trebao biti postavljen na novu vrijednost");
            assertEquals(1500, novaPauzaDretve, "pauzaDretve bi trebala biti postavljena na novu vrijednost");
        } catch (Exception e) {
        	fail(e.getMessage());
        }
    }

    @Test
    @Order(16)
    void testPokreniPosluziteljKraj_KrajJeTrue() throws Exception {
        String nazivDatoteke = CLASS_NAME + "_konfig_kraj.txt";
        Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);

        int port = 9999;
        konfig.spremiPostavku("mreznaVrataKraj", String.valueOf(port));
        konfig.spremiKonfiguraciju();

        this.testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke);

        postaviPrivatniAtribut(this.testnaTvrtka, "kraj", new AtomicBoolean(true));

        try {
            pozoviPrivatnuMetoduBezParametara(this.testnaTvrtka, "pokreniPosluziteljKraj");
            assertTrue(true, "Metoda bi trebala izaći (return) bez iznimke kada je kraj true");
        }  catch (Exception e) {
			fail(e.getMessage());
		}
    }

    @Test
    @Order(17)
    void testPokreniPosluziteljKraj_NeispravanPort() throws Exception {
        String nazivDatoteke = CLASS_NAME + "_neispravan_port.txt";
        Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);

        konfig.spremiPostavku("mreznaVrataKraj", "NEISPRAVAN_PORT");
        konfig.spremiKonfiguraciju();

        this.testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke);

        assertThrows(InvocationTargetException.class, () -> {
            pozoviPrivatnuMetoduBezParametara(this.testnaTvrtka, "pokreniPosluziteljKraj");
        }, "Očekivana iznimka InvocationTargetException");
    }
    
    @Test
    @Order(18)
    void testPokreniPosluziteljKraj_IspravanKod() {
      ExecutorService executor = null;
      try {
        String nazivDatoteke = CLASS_NAME + "_konfig_kraj_test.txt";
        String partneriDatoteka = CLASS_NAME + "_partneri_kraj_test.json";
        String picaDatoteka = CLASS_NAME + "_pica_kraj_test.json";
        
        kreirajTempDatoteku(partneriDatoteka, "[]");
        kreirajTempDatoteku(picaDatoteka, "[]");
        
        Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);
        
        int port = 9999;
        String ispravanKod = "tajniKod";
        
        konfig.spremiPostavku("mreznaVrataKraj", String.valueOf(port));
        konfig.spremiPostavku("brojCekaca", "1");
        konfig.spremiPostavku("kodZaKraj", ispravanKod);
        konfig.spremiPostavku("datotekaPartnera", partneriDatoteka);
        konfig.spremiPostavku("datotekaKartaPica", picaDatoteka);
        konfig.spremiKonfiguraciju();
        
        testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke);
        
        ExecutorService testExecutor = Executors.newCachedThreadPool();
        postaviPrivatniAtribut(testnaTvrtka, "executor", testExecutor);
        postaviPrivatniAtribut(testnaTvrtka, "kodZaKraj", ispravanKod);
        
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
          try {
            pozoviPrivatnuMetoduBezParametara(testnaTvrtka, "pokreniPosluziteljKraj");
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
        
        Thread.sleep(300);
        
        try (Socket partner = new Socket("localhost", port);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(partner.getOutputStream(), "utf8"), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(partner.getInputStream(), "utf8"))) {
            
          String komanda = "KRAJ " + ispravanKod;
          
          out.println(komanda);
          String odgovor = in.readLine();
          
          assertEquals("OK", odgovor, "Odgovor servera nije OK");
        }
        
      } catch (Exception e) {
        fail(e.getMessage());
      } finally {
        if (executor != null) {
          executor.shutdownNow();
        }
      }
    }
    
    @Test
    @Order(19)
    void testPokreniPosluziteljKraj_NeispravanKod() {
      ExecutorService executor = null;
      try {
        String nazivDatoteke = CLASS_NAME + "_konfig_kraj_test.txt";
        String partneriDatoteka = CLASS_NAME + "_partneri_kraj_test.json";
        String picaDatoteka = CLASS_NAME + "_pica_kraj_test.json";
        
        kreirajTempDatoteku(partneriDatoteka, "[]");
        kreirajTempDatoteku(picaDatoteka, "[]");
        
        Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);
        
        int port = 9999;
        String ispravanKod = "tajniKod";
        
        konfig.spremiPostavku("mreznaVrataKraj", String.valueOf(port));
        konfig.spremiPostavku("brojCekaca", "1");
        konfig.spremiPostavku("kodZaKraj", ispravanKod);
        konfig.spremiPostavku("datotekaPartnera", partneriDatoteka);
        konfig.spremiPostavku("datotekaKartaPica", picaDatoteka);
        konfig.spremiKonfiguraciju();
        
        testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke);
        
        ExecutorService testExecutor = Executors.newCachedThreadPool();
        postaviPrivatniAtribut(testnaTvrtka, "executor", testExecutor);
        postaviPrivatniAtribut(testnaTvrtka, "kodZaKraj", ispravanKod);
        
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
          try {
            pozoviPrivatnuMetoduBezParametara(testnaTvrtka, "pokreniPosluziteljKraj");
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
        
        Thread.sleep(300);
        
        try (Socket partner = new Socket("localhost", port);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(partner.getOutputStream(), "utf8"), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(partner.getInputStream(), "utf8"))) {
            
          String komanda = "KRAJ krivi_kod";
          
          out.println(komanda);
          String odgovor = in.readLine();
          
          assertEquals("ERROR 10 - Format komande nije ispravan ili nije ispravan kod za kraj", odgovor, "Odgovor servera nije ERROR 10");
        }
        
      } catch (Exception e) {
        fail(e.getMessage());
      } finally {
        if (executor != null) {
          executor.shutdownNow();
        }
      }
    }
    
    @Test
    @Order(20)
    void testPokreniPosluziteljKraj_PraznaKomanda() {
      ExecutorService executor = null;
      try {
        String nazivDatoteke = CLASS_NAME + "_konfig_kraj_test.txt";
        String partneriDatoteka = CLASS_NAME + "_partneri_kraj_test.json";
        String picaDatoteka = CLASS_NAME + "_pica_kraj_test.json";
        
        kreirajTempDatoteku(partneriDatoteka, "[]");
        kreirajTempDatoteku(picaDatoteka, "[]");
        
        Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);
        
        int port = 9999;
        String ispravanKod = "tajniKod";
        
        konfig.spremiPostavku("mreznaVrataKraj", String.valueOf(port));
        konfig.spremiPostavku("brojCekaca", "1");
        konfig.spremiPostavku("kodZaKraj", ispravanKod);
        konfig.spremiPostavku("datotekaPartnera", partneriDatoteka);
        konfig.spremiPostavku("datotekaKartaPica", picaDatoteka);
        konfig.spremiKonfiguraciju();
        
        testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke);
        
        ExecutorService testExecutor = Executors.newCachedThreadPool();
        postaviPrivatniAtribut(testnaTvrtka, "executor", testExecutor);
        postaviPrivatniAtribut(testnaTvrtka, "kodZaKraj", ispravanKod);
        
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
          try {
            pozoviPrivatnuMetoduBezParametara(testnaTvrtka, "pokreniPosluziteljKraj");
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
        
        Thread.sleep(300);
        
        try (Socket partner = new Socket("localhost", port);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(partner.getOutputStream(), "utf8"), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(partner.getInputStream(), "utf8"))) {
            
          String komanda = "";
          
          out.println(komanda);
          String odgovor = in.readLine();
          
          assertEquals("ERROR 19 - Nešto drugo nije u redu", odgovor, "Odgovor servera nije ERROR 19");
        }
        
      } catch (Exception e) {
        fail(e.getMessage());
      } finally {
        if (executor != null) {
          executor.shutdownNow();
        }
      }
    }
    
    @Test
    @Order(21)
    void testObradiKomandeZaRegistracijuPartnera_NeispravnaKomanda() {
        ExecutorService executor = null;
        try {
            String nazivDatoteke = CLASS_NAME + "_konfig_neispravna_komanda.txt";
            String picaDatoteka = CLASS_NAME + "_pica_test.json";
            
            kreirajTempDatoteku(picaDatoteka, "[]");
            
            Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);
            
            int port = 9998;
            konfig.spremiPostavku("mreznaVrataRegistracija", String.valueOf(port));
            konfig.spremiPostavku("brojCekaca", "1");
            konfig.spremiPostavku("datotekaKartaPica", picaDatoteka);
            konfig.spremiPostavku("kuhinja_1", "MK;Mediteranska kuhinja");
            konfig.spremiPostavku("kuhinja_2", "Francuska;Opis francuske kuhinje");
            konfig.spremiKonfiguraciju();
            
            testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke);
            
            ExecutorService testExecutor = Executors.newCachedThreadPool();
            postaviPrivatniAtribut(testnaTvrtka, "executor", testExecutor);
            
            executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    pozoviPrivatnuMetoduBezParametara(testnaTvrtka, "pokreniPosluziteljRegistracijaPartnera");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
   
            Thread.sleep(300);
        
            try (Socket client = new Socket("localhost", port);
                PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "utf8"), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), "utf8"))) {
            	
            	String komanda = "PARTNER";
            	out.println(komanda);
    
                String odgovor = in.readLine();
                assertEquals("ERROR 20 - Format komande nije ispravan", odgovor, "odgovor treba biti ERROR 20");
            }
            
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (executor != null) {
                executor.shutdownNow();
            }
        }
    }

    @Test
    @Order(22)
    void testObradiKomandeZaRegistracijuPartnera_Popis() {
        ExecutorService executor = null;
        try {
            String nazivDatoteke = CLASS_NAME + "_konfig_neispravna_komanda.txt";
            String partneriDatoteka = CLASS_NAME + "_partneri_test.json";
            String picaDatoteka = CLASS_NAME + "_pica_test.json";
            
            kreirajTempDatoteku(picaDatoteka, "[]");
            kreirajTempDatoteku(partneriDatoteka, "[]");
            
            Konfiguracija konfig = kreirajTempKonfigDatoteku(nazivDatoteke);
            
            int port = 9998;
            konfig.spremiPostavku("mreznaVrataRegistracija", String.valueOf(port));
            konfig.spremiPostavku("brojCekaca", "1");
            konfig.spremiPostavku("datotekaKartaPica", picaDatoteka);
            konfig.spremiPostavku("datotekaPartnera", partneriDatoteka); 
            konfig.spremiPostavku("kuhinja_1", "MK;Mediteranska kuhinja");
            konfig.spremiPostavku("kuhinja_2", "Francuska;Opis francuske kuhinje");
            konfig.spremiKonfiguraciju();
            
            testnaTvrtka.ucitajKonfiguraciju(nazivDatoteke);
            
            ExecutorService testExecutor = Executors.newCachedThreadPool();
            postaviPrivatniAtribut(testnaTvrtka, "executor", testExecutor);
            
            executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> {
                try {
                    pozoviPrivatnuMetoduBezParametara(testnaTvrtka, "pokreniPosluziteljRegistracijaPartnera");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
   
            Thread.sleep(300);
        
            try (Socket client = new Socket("localhost", port);
                PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), "utf8"), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), "utf8"))) {

                String komanda = "POPIS";
            	out.println(komanda);
    
                String odgovor = in.readLine();
                assertEquals("OK", odgovor, "odgovor treba biti OK");
                
                String json = in.readLine();
                assertEquals(json, "[]", "odgovor treba biti prazan JSON");
            }
            
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (executor != null) {
                executor.shutdownNow();
            }
        }
    }
}