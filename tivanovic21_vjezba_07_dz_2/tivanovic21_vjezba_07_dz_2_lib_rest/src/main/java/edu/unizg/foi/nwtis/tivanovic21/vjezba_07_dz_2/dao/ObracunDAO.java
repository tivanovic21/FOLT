package edu.unizg.foi.nwtis.tivanovic21.vjezba_07_dz_2.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;

public class ObracunDAO {
	private final Connection vezaBP;

	public ObracunDAO(Connection vezaBP) {
		this.vezaBP = vezaBP;
	}

	public Obracun dohvati(String id) {
		String upit = "SELECT partner, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE id = ?";

		try (PreparedStatement ps = vezaBP.prepareStatement(upit)) {
			ps.setString(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int partner = rs.getInt("partner");
					boolean jelo = rs.getBoolean("jelo");
					float kolicina = rs.getFloat("kolicina");
					float cijena = rs.getFloat("cijena");
					long vrijeme = rs.getLong("vrijeme");
					return new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
				}
			}
		} catch (SQLException ex) {
			Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, "Greška pri dohvatu obračuna", ex);
		}
		return null;
	}

	public List<Obracun> dohvatiSve() {
		String upit = "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni ORDER BY vrijeme";

		List<Obracun> lista = new ArrayList<>();
		try (Statement st = vezaBP.createStatement(); ResultSet rs = st.executeQuery(upit)) {

			while (rs.next()) {
				int partner = rs.getInt("partner");
				String id = rs.getString("id");
				boolean jelo = rs.getBoolean("jelo");
				float kolicina = rs.getFloat("kolicina");
				float cijena = rs.getFloat("cijena");
				long vrijeme = rs.getTimestamp("vrijeme").getTime();
				lista.add(new Obracun(partner, id, jelo, kolicina, cijena, vrijeme));
			}
		} catch (SQLException ex) {
			Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, "Greška pri dohvatu svih obračuna", ex);
		}
		return lista;
	}

	public boolean dodaj(Obracun obracun) {
		String upit = "INSERT INTO obracuni (partner, id, jelo, kolicina, cijena, vrijeme) VALUES (?, ?, ?, ?, ?, ?)";

		try (PreparedStatement ps = vezaBP.prepareStatement(upit)) {
			PartnerDAO partnerDAO = new PartnerDAO(vezaBP);
			Partner partner = partnerDAO.dohvati(obracun.partner(), false);

			if (partner == null) {
				Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE,
						"Partner s ID " + obracun.partner() + " ne postoji");
				return false;
			}

			ps.setInt(1, obracun.partner());
			ps.setString(2, obracun.id());
			ps.setBoolean(3, obracun.jelo());
			ps.setFloat(4, obracun.kolicina());
			ps.setFloat(5, obracun.cijena());
			ps.setTimestamp(6, new Timestamp(obracun.vrijeme()));

			int affectedRows = ps.executeUpdate();
			return affectedRows > 0;

		} catch (SQLException ex) {
			Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, "Greška pri dodavanju obračuna", ex);
			return false;
		}
	}

	public boolean azuriraj(Obracun o) {
		String upit = "UPDATE obracuni SET partner = ?, jelo = ?, kolicina = ?, cijena = ?, vrijeme = ? "
				+ "WHERE id = ?";

		try (PreparedStatement ps = vezaBP.prepareStatement(upit)) {
			ps.setInt(1, o.partner());
			ps.setBoolean(2, o.jelo());
			ps.setFloat(3, o.kolicina());
			ps.setFloat(4, o.cijena());
			ps.setLong(5, o.vrijeme());
			ps.setString(6, o.id());
			return ps.executeUpdate() == 1;
		} catch (SQLException ex) {
			Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, "Greška pri ažuriranju obračuna", ex);
		}
		return false;
	}

	public boolean obrisi(String id) {
		String upit = "DELETE FROM obracuni WHERE id = ?";

		try (PreparedStatement ps = vezaBP.prepareStatement(upit)) {
			ps.setString(1, id);
			return ps.executeUpdate() == 1;
		} catch (SQLException ex) {
			Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, "Greška pri brisanju obračuna", ex);
		}
		return false;
	}

	public List<Obracun> dohvatiPoVremenu(Long vrijemeOd, Long vrijemeDo) {
		StringBuilder upit = new StringBuilder("SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni");

		List<Object> parametri = new ArrayList<>();
		boolean imaUvjet = false;

		if (vrijemeOd != null) {
			upit.append(" WHERE vrijeme >= ?");
			parametri.add(new Timestamp(vrijemeOd));
			imaUvjet = true;
		}

		if (vrijemeDo != null) {
			if (imaUvjet) {
				upit.append(" AND vrijeme <= ?");
			} else {
				upit.append(" WHERE vrijeme <= ?");
			}
			parametri.add(new Timestamp(vrijemeDo));
			;
		}

		upit.append(" ORDER BY vrijeme");

		List<Obracun> lista = new ArrayList<>();
		try (PreparedStatement ps = vezaBP.prepareStatement(upit.toString())) {
			for (int i = 0; i < parametri.size(); i++) {
				ps.setObject(i + 1, parametri.get(i));
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int partner = rs.getInt("partner");
					String id = rs.getString("id");
					boolean jelo = rs.getBoolean("jelo");
					float kolicina = rs.getFloat("kolicina");
					float cijena = rs.getFloat("cijena");
					long vrijeme = rs.getTimestamp("vrijeme").getTime();
					lista.add(new Obracun(partner, id, jelo, kolicina, cijena, vrijeme));
				}
			}
		} catch (SQLException ex) {
			Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, "Greška pri dohvatu obračuna po vremenu",
					ex);
		}
		return lista;
	}

	public List<Obracun> dohvatiJelaPoVremenu(Long vrijemeOd, Long vrijemeDo) {
		StringBuilder upit = new StringBuilder(
				"SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE jelo = ?");

		List<Object> parametri = new ArrayList<>();
		parametri.add(true);

		if (vrijemeOd != null) {
			upit.append(" AND vrijeme >= ?");
			parametri.add(new Timestamp(vrijemeOd));
		}

		if (vrijemeDo != null) {
			upit.append(" AND vrijeme <= ?");
			parametri.add(new Timestamp(vrijemeDo));
		}

		upit.append(" ORDER BY vrijeme");

		List<Obracun> lista = new ArrayList<>();
		try (PreparedStatement ps = vezaBP.prepareStatement(upit.toString())) {
			for (int i = 0; i < parametri.size(); i++) {
				ps.setObject(i + 1, parametri.get(i));
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int partner = rs.getInt("partner");
					String id = rs.getString("id");
					boolean jelo = rs.getBoolean("jelo");
					float kolicina = rs.getFloat("kolicina");
					float cijena = rs.getFloat("cijena");
					long vrijeme = rs.getTimestamp("vrijeme").getTime();
					lista.add(new Obracun(partner, id, jelo, kolicina, cijena, vrijeme));
				}
			}
		} catch (SQLException ex) {
			Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE,
					"Greška pri dohvatu obračuna za jela po vremenu", ex);
		}
		return lista;
	}

	public List<Obracun> dohvatiJela() {
		String upit = "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE jelo = ? ORDER BY vrijeme";

		List<Obracun> lista = new ArrayList<>();
		try (PreparedStatement ps = vezaBP.prepareStatement(upit)) {
			ps.setBoolean(1, true);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int partner = rs.getInt("partner");
					String id = rs.getString("id");
					boolean jelo = rs.getBoolean("jelo");
					float kolicina = rs.getFloat("kolicina");
					float cijena = rs.getFloat("cijena");
					long vrijeme = rs.getTimestamp("vrijeme").getTime();
					lista.add(new Obracun(partner, id, jelo, kolicina, cijena, vrijeme));
				}
			}
		} catch (SQLException ex) {
			Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, "Greška pri dohvatu obračuna za jela", ex);
		}
		return lista;
	}

	public List<Obracun> dohvatiPicePoVremenu(Long vrijemeOd, Long vrijemeDo) {
		StringBuilder upit = new StringBuilder(
				"SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE jelo = ?");

		List<Object> parametri = new ArrayList<>();
		parametri.add(false);

		if (vrijemeOd != null) {
			upit.append(" AND vrijeme >= ?");
			parametri.add(new Timestamp(vrijemeOd));
		}

		if (vrijemeDo != null) {
			upit.append(" AND vrijeme <= ?");
			parametri.add(new Timestamp(vrijemeDo));
		}

		upit.append(" ORDER BY vrijeme");

		List<Obracun> lista = new ArrayList<>();
		try (PreparedStatement ps = vezaBP.prepareStatement(upit.toString())) {
			for (int i = 0; i < parametri.size(); i++) {
				ps.setObject(i + 1, parametri.get(i));
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int partner = rs.getInt("partner");
					String id = rs.getString("id");
					boolean jelo = rs.getBoolean("jelo");
					float kolicina = rs.getFloat("kolicina");
					float cijena = rs.getFloat("cijena");
					long vrijeme = rs.getTimestamp("vrijeme").getTime();
					lista.add(new Obracun(partner, id, jelo, kolicina, cijena, vrijeme));
				}
			}
		} catch (SQLException ex) {
			Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE,
					"Greška pri dohvatu obračuna za piće po vremenu", ex);
		}
		return lista;
	}

	public List<Obracun> dohvatiPice() {
		String upit = "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE jelo = ? ORDER BY vrijeme";

		List<Obracun> lista = new ArrayList<>();
		try (PreparedStatement ps = vezaBP.prepareStatement(upit)) {
			ps.setBoolean(1, false);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int partner = rs.getInt("partner");
					String id = rs.getString("id");
					boolean jelo = rs.getBoolean("jelo");
					float kolicina = rs.getFloat("kolicina");
					float cijena = rs.getFloat("cijena");
					long vrijeme = rs.getTimestamp("vrijeme").getTime();
					lista.add(new Obracun(partner, id, jelo, kolicina, cijena, vrijeme));
				}
			}
		} catch (SQLException ex) {
			Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, "Greška pri dohvatu obračuna za piće", ex);
		}
		return lista;
	}

	public List<Obracun> dohvatiPoPartneruIVremenu(int partnerId, Long vrijemeOd, Long vrijemeDo) {
		StringBuilder upit = new StringBuilder(
				"SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE partner = ?");

		List<Object> parametri = new ArrayList<>();
		parametri.add(partnerId);

		if (vrijemeOd != null) {
			upit.append(" AND vrijeme >= ?");
			parametri.add(new Timestamp(vrijemeOd));
		}

		if (vrijemeDo != null) {
			upit.append(" AND vrijeme <= ?");
			parametri.add(new Timestamp(vrijemeDo));
		}

		upit.append(" ORDER BY vrijeme");

		List<Obracun> lista = new ArrayList<>();
		try (PreparedStatement ps = vezaBP.prepareStatement(upit.toString())) {
			for (int i = 0; i < parametri.size(); i++) {
				ps.setObject(i + 1, parametri.get(i));
			}

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int partner = rs.getInt("partner");
					String id = rs.getString("id");
					boolean jelo = rs.getBoolean("jelo");
					float kolicina = rs.getFloat("kolicina");
					float cijena = rs.getFloat("cijena");
					long vrijeme = rs.getTimestamp("vrijeme").getTime();
					lista.add(new Obracun(partner, id, jelo, kolicina, cijena, vrijeme));
				}
			}
		} catch (SQLException ex) {
			Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE,
					"Greška pri dohvatu obračuna za partnera " + partnerId + " po vremenu", ex);
		}
		return lista;
	}

	public List<Obracun> dohvatiPoPartneru(int partnerId) {
		String upit = "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE partner = ? ORDER BY vrijeme";

		List<Obracun> lista = new ArrayList<>();
		try (PreparedStatement ps = vezaBP.prepareStatement(upit)) {
			ps.setInt(1, partnerId);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					int partner = rs.getInt("partner");
					String id = rs.getString("id");
					boolean jelo = rs.getBoolean("jelo");
					float kolicina = rs.getFloat("kolicina");
					float cijena = rs.getFloat("cijena");
					long vrijeme = rs.getTimestamp("vrijeme").getTime();
					lista.add(new Obracun(partner, id, jelo, kolicina, cijena, vrijeme));
				}
			}
		} catch (SQLException ex) {
			Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE,
					"Greška pri dohvatu obračuna za partnera " + partnerId, ex);
		}
		return lista;
	}

}
