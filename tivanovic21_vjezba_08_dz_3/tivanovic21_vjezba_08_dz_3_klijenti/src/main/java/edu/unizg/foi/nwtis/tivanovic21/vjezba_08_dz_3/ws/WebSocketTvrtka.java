package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.ws;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.GlobalniPodaci;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.ServisTvrtkaKlijent;
import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/tvrtka")
public class WebSocketTvrtka {
	@Inject
	private ServisTvrtkaKlijent servisTvrtkaKlijent;

	@Inject
	private GlobalniPodaci gp;

	private static final Pattern FORMAT_PORUKA = Pattern.compile("^[^;]+;\\d+;.*$");

	static Queue<Session> queue = new ConcurrentLinkedQueue<>();

	public static void send(String poruka) {
		try {
			for (Session session : queue) {
				if (session.isOpen()) {
					System.out.println("Šaljem poruku: " + poruka);
					session.getBasicRemote().sendText(poruka);
					System.out.println("Ispis: " + session.getBasicRemote().toString());
				}
			}
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
	}

	@OnOpen
	public void openConnection(Session session, EndpointConfig conf) {
		queue.add(session);
		System.out.println("Otvorena veza.");

		try {
			int status = this.servisTvrtkaKlijent.headPosluzitelj().getStatus();
			this.gp.setStatusTvrtka(status == 200);
		} catch (Exception e) {
			this.gp.setStatusTvrtka(false);
		}
	}

	@OnClose
	public void closedConnection(Session session, CloseReason reason) {
		queue.remove(session);
		System.out.println("Zatvorena veza.");
	}

	@OnMessage
	public void Message(Session session, String poruka) {
		System.out.println("Primljena poruka: " + poruka);

		if (FORMAT_PORUKA.matcher(poruka).matches()) {
			String[] dijelovi = poruka.split(";", 3);
			if (dijelovi.length < 3) {
				System.out.println("Neispravna poruka: " + poruka);
				return;
			}
			this.gp.setStatusTvrtka("RADI".equals(dijelovi[0]));
			this.gp.setBrojObracuna(Integer.parseInt(dijelovi[1]));
			this.gp.setPoruka(dijelovi[2]);
			send(poruka);
		}
	}

	@OnError
	public void error(Session session, Throwable t) {
		queue.remove(session);
		System.out.println("Zatvorena veza zbog pogreške.");
	}

	private void posaljiGlobalnePodatke() {
		String radi = this.gp.getStatusTvrtka() ? "RADI" : "NE RADI";
		int brojObracuna = this.gp.getBrojObracuna();
		String staraPoruka = this.gp.getPoruka();

		String message = radi + ";" + brojObracuna + ";" + (staraPoruka.isBlank() ? "" : staraPoruka);
		send(message);
	}
}
