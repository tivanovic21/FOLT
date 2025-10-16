package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.entiteti.Zapisi;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Stateless
public class ZapisiFacade extends EntityManagerProducer implements Serializable {
	private static final long serialVersionUID = 3595928436540495884L;

	private CriteriaBuilder cb;

	@PostConstruct
	private void init() {
		cb = getEntityManager().getCriteriaBuilder();
	}

	public void create(Zapisi zapisi) {
		getEntityManager().persist(zapisi);
	}

	public void edit(Zapisi zapisi) {
		getEntityManager().merge(zapisi);
	}

	public void remove(Zapisi zapisi) {
		getEntityManager().remove(getEntityManager().merge(zapisi));
	}

	public Zapisi find(Object id) {
		return getEntityManager().find(Zapisi.class, id);
	}

	public List<Zapisi> findAll() {
		CriteriaQuery<Zapisi> cq = cb.createQuery(Zapisi.class);
		cq.select(cq.from(Zapisi.class));
		return getEntityManager().createQuery(cq).getResultList();
	}

	public List<Zapisi> findRange(int[] range) {
		CriteriaQuery<Zapisi> cq = cb.createQuery(Zapisi.class);
		cq.select(cq.from(Zapisi.class));
		TypedQuery<Zapisi> q = getEntityManager().createQuery(cq);
		q.setMaxResults(range[1] - range[0]);
		q.setFirstResult(range[0]);
		return q.getResultList();
	}

	public int count() {
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		cq.select(cb.count(cq.from(Zapisi.class)));
		return ((Long) getEntityManager().createQuery(cq).getSingleResult()).intValue();
	}

	/**
	 * Zapisuje akciju u tablicu zapisi (kao dnevnik rada)
	 * 
	 * @param korisnickoIme  korisnickoIme prijavljenog korisnika
	 * @param adresaRacunala adresa racunala
	 * @param ipAdresa       ip adresa
	 * @param akcija         opis akcije koja se izvrsila
	 */
	public void zapisiAkciju(String korisnickoIme, String adresaRacunala, String ipAdresa, String akcija) {
		Zapisi zapis = new Zapisi();
		zapis.setKorisnickoime(korisnickoIme);
		zapis.setAdresaracunala(adresaRacunala);
		zapis.setIpadresaracunala(ipAdresa);
		zapis.setOpisrada(akcija);
		zapis.setVrijeme(new Timestamp(System.currentTimeMillis()));

		create(zapis);
	}

	public List<Zapisi> findByKorisnikIVrijeme(String korisnickoIme, long vrijemeOd, long vrijemeDo) {
		CriteriaQuery<Zapisi> cq = cb.createQuery(Zapisi.class);
		Root<Zapisi> zapisi = cq.from(Zapisi.class);

		List<Predicate> predicates = new ArrayList<>();

		if (korisnickoIme != null && !korisnickoIme.trim().isEmpty()) {
			predicates.add(cb.equal(zapisi.get("korisnickoime"), korisnickoIme));
		}

		if (vrijemeOd > 0) {
			predicates.add(cb.greaterThanOrEqualTo(zapisi.get("vrijeme"), new java.sql.Timestamp(vrijemeOd)));
		}

		if (vrijemeDo > 0) {
			predicates.add(cb.lessThanOrEqualTo(zapisi.get("vrijeme"), new java.sql.Timestamp(vrijemeDo)));
		}

		if (!predicates.isEmpty()) {
			cq.where(cb.and(predicates.toArray(new Predicate[0])));
		}

		cq.orderBy(cb.desc(zapisi.get("vrijeme")));

		TypedQuery<Zapisi> query = getEntityManager().createQuery(cq);
		return query.getResultList();
	}

}
