package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jf;

import java.io.Serializable;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.ServisPartnerKlijent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("statusPartnera")
public class StatusPartnera implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final int SERVER_RADI_KOD = 200;
	
	private String statusPoruka;
	private boolean serverRadi;
	private int statusKod;
	
	@Inject
	@RestClient
	ServisPartnerKlijent sp;
	
	public void provjeriStatus() {
		try {
			var statusT = this.sp.headPosluzitelj().getStatus();
			this.statusKod = statusT;
			this.serverRadi = statusT == SERVER_RADI_KOD;
			this.statusPoruka = "RADI";
		} catch (Exception e) {
			this.serverRadi = false;
			this.statusKod = 500;
			this.statusPoruka = "NE RADI";
		}
	}
	
	public boolean isRadi() {
		return this.serverRadi;
	}
	
	public String getPoruka() {
		return this.statusPoruka;
	}
	
	public int getKod() {
		return this.statusKod;
	}
}
