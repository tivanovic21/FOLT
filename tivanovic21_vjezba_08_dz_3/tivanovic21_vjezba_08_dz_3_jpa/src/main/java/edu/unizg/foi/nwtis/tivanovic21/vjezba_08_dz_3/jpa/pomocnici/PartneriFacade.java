package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.entiteti.Partneri;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

@Stateless
public class PartneriFacade extends EntityManagerProducer implements Serializable {
	private static final long serialVersionUID = 3595041736540495884L;

	private CriteriaBuilder cb;

	@PostConstruct
	private void init() {
		cb = getEntityManager().getCriteriaBuilder();
	}

	public void create(Partneri partneri) {
		getEntityManager().persist(partneri);
	}

	public void edit(Partneri partneri) {
		getEntityManager().merge(partneri);
	}

	public void remove(Partneri partneri) {
		getEntityManager().remove(getEntityManager().merge(partneri));
	}

	public Partneri find(Object id) {
		return getEntityManager().find(Partneri.class, id);
	}

	public List<Partneri> findAll() {
		CriteriaQuery<Partneri> cq = cb.createQuery(Partneri.class);
		cq.select(cq.from(Partneri.class));
		return getEntityManager().createQuery(cq).getResultList();
	}

	public List<Partneri> findRange(int[] range) {
		CriteriaQuery<Partneri> cq = cb.createQuery(Partneri.class);
		cq.select(cq.from(Partneri.class));
		TypedQuery<Partneri> q = getEntityManager().createQuery(cq);
		q.setMaxResults(range[1] - range[0]);
		q.setFirstResult(range[0]);
		return q.getResultList();
	}

	public int count() {
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		cq.select(cb.count(cq.from(Partneri.class)));
		return ((Long) getEntityManager().createQuery(cq).getSingleResult()).intValue();
	}

	public Partner pretvori(Partneri p) {
		if (p == null) {
			return null;
		}
		var pObjekt = new Partner(p.getId(), p.getNaziv(), p.getVrstakuhinje(), p.getAdresa(), p.getMreznavrata(), p.getMreznavratakraj(),(float) p.getGpssirina(), (float) p.getGpsduzina(), p.getSigurnosnikod(), p.getAdminkod());

		return pObjekt;
	}

	public Partneri pretvori(Partner p) {
		if (p == null) {
			return null;
		}
		
		var pE = new Partneri();
		pE.setId(p.id());
		pE.setNaziv(p.naziv());
		pE.setVrstakuhinje(p.vrstaKuhinje());
		pE.setAdresa(p.adresa());
		pE.setMreznavrata(p.mreznaVrata());
		pE.setMreznavratakraj(p.mreznaVrataKraj());
		pE.setGpsduzina((double) p.gpsDuzina());
		pE.setGpssirina((double) p.gpsDuzina());
		pE.setSigurnosnikod(p.sigurnosniKod());
		pE.setAdminkod(p.adminKod());
		
		return pE;
	}

	public List<Partner> pretvori(List<Partneri> partneriE) {
		List<Partner> partneri = new ArrayList<>();
		for (Partneri pEntitet : partneriE) {
			var pObjekt = pretvori(pEntitet);

			partneri.add(pObjekt);
		}

		return partneri;
	}
}
