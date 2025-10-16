<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Vježba 8 - zadaća 3 - Nadzorna konzola Tvrtka</title>
<style>
body {
	font-family: Arial, sans-serif;
	margin: 20px;
}

h1 {
	font-size: 22px;
	margin-bottom: 20px;
}

h2 {
	margin-top: 30px;
	font-size: 18px;
	border-bottom: 1px solid #ccc;
	padding-bottom: 5px;
}

.section {
	margin-bottom: 25px;
}

ul {
	margin: 10px 0;
	padding-left: 20px;
}

li {
	margin-bottom: 5px;
}

table {
	margin-top: 10px;
}

td {
	padding: 4px 8px;
}
</style>
</head>
<body>

	<h1>Vježba 8 - zadaća 3 - Nadzorna konzola Tvrtka</h1>

	<div class="section">
		<h2>Javni dio</h2>
		<ul>
			<li><a
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
					stranica Tvrtka</a></li>
			<li><a
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner">Pregled
					partnera/restorana</a></li>
			<li><a
				href="${pageContext.servletContext.contextPath}/index.xhtml">Početna
					stranica Partner (Jakarta Faces)</a></li>
		</ul>
	</div>

	<div class="section">
		<h2>Privatni dio (potrebna prijava)</h2>
		<ul>
			<li><a
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracuni">
					Pregled obračuna (sve/jelo/piće)</a></li>
			<li><a
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracuni/partner/1">
					Obračuni partnera (primjer: Partner ID 1)</a></li>
		</ul>
	</div>

	<div class="section">
		<h2>Administracijski dio (samo admin)</h2>
		<ul>
			<li><a
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/nadzornaKonzolaTvrtka">Nadzorna
					konzola Tvrtka</a></li>
			<li><a
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/noviPartner">Dodavanje
					novog partnera</a></li>
			<li><a
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/spavanje">Aktiviranje
					spavanja</a></li>
		</ul>
	</div>

</body>
</html>
