<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Aktiviranje spavanja</title>
<style>
body {
	font-family: sans-serif;
	margin: 20px;
}

.poruka {
	color: red;
}

.uspjeh {
	color: green;
}

table td {
	padding: 5px;
}

.info {
	margin: 15px 0;
	padding: 10px;
}

.info {
	background: #eef;
}

</style>
</head>
<body>

	<h1>Aktiviranje spavanja</h1>

	<ul>
		<li><a
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
				stranica</a></li>
	</ul>

	<%
	String uspjehParam = request.getParameter("uspjeh");
	String vrijemeParam = request.getParameter("vrijeme");
	if ("true".equals(uspjehParam) && vrijemeParam != null) {
	%>
	<p class="uspjeh">
		Spavanje je aktivirano na
		<%=vrijemeParam%>
		sekundi.
	</p>
	<%
	}
	%>

	<%
	String poruka = (String) request.getAttribute("poruka");
	Boolean pogreska = (Boolean) request.getAttribute("pogreska");
	if (poruka != null && !poruka.isEmpty()) {
	%>
	<p class="<%=Boolean.TRUE.equals(pogreska) ? "poruka" : "uspjeh"%>"><%=poruka%></p>
	<%
	}
	%>

	<div class="info">
		<p>Spavanje pauzira rad poslužitelja. Vrijednost se postavlja u sekundama!</p>
	</div>

	<form method="post"
		action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/spavanje">
		<table>
			<tr>
				<td>Vrijeme spavanja (sekunde):</td>
				<td><input type="number" name="vrijeme" value="${vrijeme}"
					min="1" max="3600" required /> <input type="hidden"
					name="${mvc.csrf.name}" value="${mvc.csrf.token}" /></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="submit" value="Aktiviraj spavanje" /></td>
			</tr>
		</table>
	</form>

</body>
</html>
