package edu.unizg.foi.nwtis.tivanovic21.vjezba_03;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

public class Vjezba_03 {
  private Konfiguracija konfig;
  private AtomicInteger brojSlobodnihDretvi = new AtomicInteger(0);
  private AtomicInteger ukupanBrojDretvi = new AtomicInteger(0);
  private int maksDubinaDirektorija = 0;
  private Queue<PodaciOdgodenaPutanja> odgodenePutanje = new ConcurrentLinkedQueue<>();
  private String nazivPocetnogDirektorija = ".";
  private String predlozakNazivaDatoteke = "";
  private String trazenaRijec = "";
  private Queue<Future<SkupPodatakaPretrazivanja>> rezultati =
      new ConcurrentLinkedQueue<Future<SkupPodatakaPretrazivanja>>();
  private ExecutorService izvrsitelj;
  private int pauzaDretve = 0;

  public static void main(String[] args) {
    if (args.length != 4) {
      System.out.println("Molim unesite 4 argumenta!");
      return;
    }

    var program = new Vjezba_03();
    if (!program.ucitajKonfiguraciju(args[0])) {
      return;
    }

    program.brojSlobodnihDretvi.set(Integer.parseInt(program.konfig.dajPostavku("maksDretvi")));
    program.maksDubinaDirektorija = Integer.parseInt(program.konfig.dajPostavku("maksDubina"));
    program.pauzaDretve = Integer.parseInt(program.konfig.dajPostavku("pauzaDretve"));
    program.nazivPocetnogDirektorija = args[1];
    program.trazenaRijec = args[2];
    program.trazenaRijec = args[3];

    Thread.Builder graditelj = Thread.ofVirtual();
    ThreadFactory tvornica = graditelj.factory();
    program.izvrsitelj = Executors.newThreadPerTaskExecutor(tvornica);

    Path putanjaDirektorija = Path.of(program.nazivPocetnogDirektorija).toAbsolutePath();
    program.rezultati
        .add(program.izvrsitelj.submit(() -> program.pretraziPutanju(putanjaDirektorija, 0)));

    var sviPodaci = new ArrayList<PodaciPretrazivanja>();

    try {
      Thread.sleep(program.pauzaDretve);
    } catch (Exception e) {
      e.printStackTrace();
    }


    // TODO riješiti za odgođene putanje

    for (var f : program.rezultati) {
      if (f.isDone()) {
        sviPodaci.addAll(f.resultNow().getPodaci());
      } else {
        while (!f.isDone()) {
          try {
            Thread.sleep(program.pauzaDretve);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }

    // TODO ispisati podatke iz kolekcije sviPodaci
  }

  public boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      return true;
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }

    return false;
  }

  public SkupPodatakaPretrazivanja pretraziPutanju(Path nazivPutanje, int vazecaDubinaDirektorija) {
    this.brojSlobodnihDretvi.decrementAndGet();
    this.ukupanBrojDretvi.incrementAndGet();

    var skupPodataka = new SkupPodatakaPretrazivanja();

    System.out.println(nazivPutanje + " " + vazecaDubinaDirektorija);

    try (var tokPodataka = Files.list(nazivPutanje)) {
      tokPodataka.forEach(element -> {
        if (Files.isDirectory(element)) {
          if (vazecaDubinaDirektorija < this.maksDubinaDirektorija) {
            if (this.brojSlobodnihDretvi.get() > 0) {
              this.rezultati.add(this.izvrsitelj
                  .submit(() -> this.pretraziPutanju(element, vazecaDubinaDirektorija + 1)));
            } else {
              var odgodenaPutanja = new PodaciOdgodenaPutanja(element, vazecaDubinaDirektorija + 1);
              this.odgodenePutanje.add(odgodenaPutanja);
            }
          }
        } else if (Files.isReadable(element)) {
          pretraziDatoteku(nazivPutanje, element, skupPodataka);
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      // Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
    }

    return skupPodataka;
  }

  private void pretraziDatoteku(Path nazivPutanje, Path element,
      SkupPodatakaPretrazivanja skupPodataka) {
    // TODO provjeriti da li naziv datoteke odgovara zadanom predlošku naziva
    System.out.println("Datoteka: " + element);
    // TODO otvoriti datoteku, čitati red po red, provjeriti ima li traženu riječ
  }

  public void potraziRijecUDatoteci(String nazivDatoteke) {
    System.out.println("Naziv datoteke: " + nazivDatoteke);
  }

}
