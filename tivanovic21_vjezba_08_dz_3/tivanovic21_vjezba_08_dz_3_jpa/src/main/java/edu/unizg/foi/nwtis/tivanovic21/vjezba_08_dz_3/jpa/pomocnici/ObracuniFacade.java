package edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.pomocnici;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.tivanovic21.vjezba_08_dz_3.jpa.entiteti.Obracuni;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Stateless
public class ObracuniFacade extends EntityManagerProducer implements Serializable {
	private static final long serialVersionUID = 3595041789282895885L;

	private CriteriaBuilder cb;

	@PostConstruct
	private void init() {
		cb = getEntityManager().getCriteriaBuilder();
	}

	public void create(Obracuni obracuni) {
		getEntityManager().persist(obracuni);
	}

	public void edit(Obracuni obracuni) {
		getEntityManager().merge(obracuni);
	}

	public void remove(Obracuni obracuni) {
		getEntityManager().remove(getEntityManager().merge(obracuni));
	}

	public Obracuni find(Object id) {
		return getEntityManager().find(Obracuni.class, id);
	}

	public List<Obracuni> findAll() {
		CriteriaQuery<Obracuni> cq = cb.createQuery(Obracuni.class);
		cq.select(cq.from(Obracuni.class));
		return getEntityManager().createQuery(cq).getResultList();
	}

	public List<Obracuni> findRange(int[] range) {
		CriteriaQuery<Obracuni> cq = cb.createQuery(Obracuni.class);
		cq.select(cq.from(Obracuni.class));
		TypedQuery<Obracuni> q = getEntityManager().createQuery(cq);
		q.setMaxResults(range[1] - range[0]);
		q.setFirstResult(range[0]);
		return q.getResultList();
	}

	public int count() {
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		cq.select(cb.count(cq.from(Obracuni.class)));
		return ((Long) getEntityManager().createQuery(cq).getSingleResult()).intValue();
	}

	public Obracun pretvori(Obracuni o) {
		if (o == null) {
			return null;
		}
		var oObjekt = new Obracun(o.getPartneri().getId(), o.getId(), o.getJelo(), (float) o.getKolicina(),
				(float) o.getCijena(), o.getVrijeme().getTime());

		return oObjekt;
	}

	public List<Obracun> pretvori(List<Obracuni> obracuniE) {
		List<Obracun> obracuni = new ArrayList<>();
		for (Obracuni oEntitet : obracuniE) {
			var oObjekt = pretvori(oEntitet);

			obracuni.add(oObjekt);
		}

		return obracuni;
	}

	public List<Obracuni> filterPoPartneruIVremenu(int partnerId, long vrijemeOd, long vrijemeDo) {
		CriteriaQuery<Obracuni> cq = cb.createQuery(Obracuni.class);
		Root<Obracuni> obracuni = cq.from(Obracuni.class);

		List<Predicate> predicates = new ArrayList<>();

		predicates.add(cb.equal(obracuni.get("partneri").get("id"), partnerId));

		if (vrijemeOd > 0) {
			predicates.add(cb.greaterThanOrEqualTo(obracuni.get("vrijeme"), new java.sql.Timestamp(vrijemeOd)));
		}

		if (vrijemeDo > 0) {
			predicates.add(cb.lessThanOrEqualTo(obracuni.get("vrijeme"), new java.sql.Timestamp(vrijemeDo)));
		}

		cq.where(cb.and(predicates.toArray(new Predicate[0])));
		cq.orderBy(cb.desc(obracuni.get("vrijeme")));

		TypedQuery<Obracuni> query = getEntityManager().createQuery(cq);
		return query.getResultList();
	}

}