package edu.unizg.foi.nwtis.tivanovic21.vjezba_02;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;


/**
 * Klasa Vjezba_02_2 koja služi kao primjer za uključivanje vlastite biblioteke/knjižnice klasa
 */
public class Vjezba_02_2 {

  /**
   * Konstruktor klase Vjezba_02_2.
   */
  public Vjezba_02_2() {}

  /**
   * Glavna/početna metoda kod rada u znakovnom sučelju.
   *
   * @param args argumenti koji se prenose iz komandne linije
   */
  public static void main(String[] args) {
    if (args.length < 2 || args.length > 4) {
      System.out.println("Broj argumenata nije u rasponu 2 - 4.");
      return;
    }
    String komanda = args[0];
    switch (komanda) {
      case "L":
        komandaL(args);
        break;
      case "P":
        komandaP(args);
        break;
      case "S":
        komandaS(args);
        break;
      case "K":
        komandaK(args);
        break;
      case "D":
        komandaD(args);
        break;
      case "E":
        komandaE(args);
        break;
    }
  }

  private static void komandaE(String[] args) {
    try {
      Konfiguracija konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(args[1]);
      boolean status = konfig.obrisiSvePostavke();
      System.out.println("Status: " + status);
      konfig.spremiKonfiguraciju();
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(Vjezba_02_2.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static void komandaD(String[] args) {
    try {
      Konfiguracija konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(args[1]);
      String k = args[2];
      boolean status = konfig.obrisiPostavku(k);
      System.out.println("Status: " + status);
      konfig.spremiKonfiguraciju();
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(Vjezba_02_2.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static void komandaK(String[] args) {
    try {
      Konfiguracija konfigU = KonfiguracijaApstraktna.preuzmiKonfiguraciju(args[1]);
      Konfiguracija konfigI = KonfiguracijaApstraktna.kreirajKonfiguraciju(args[2]);
      konfigU.dajSvePostavke().keySet().stream()
          .forEach(k -> konfigI.spremiPostavku((String) k, konfigU.dajPostavku((String) k)));
      konfigI.spremiKonfiguraciju();
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(Vjezba_02_2.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static void komandaS(String[] args) {
    try {

      Konfiguracija konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(args[1]);
      String k = args[2];
      String v = args[3];
      konfig.spremiPostavku(k, v);
      konfig.spremiKonfiguraciju();
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(Vjezba_02_2.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static void komandaP(String[] args) {
    try {
      Konfiguracija konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(args[1]);
      String k = args[2];
      String v = konfig.dajPostavku(k);
      System.out.println("k: " + k + " v: " + v);
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(Vjezba_02_2.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static void komandaL(String[] args) {
    try {
      Konfiguracija konfig = KonfiguracijaApstraktna.preuzmiKreirajKonfiguraciju(args[1]);
      Properties postavke = konfig.dajSvePostavke();
      System.out.println("Sve postavke");
      for (Object o : postavke.keySet()) {
        String k = (String) o;
        String v = postavke.getProperty(k);
        System.out.println("k: " + k + " v: " + v);
      }
      System.out.println();
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(Vjezba_02_2.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
