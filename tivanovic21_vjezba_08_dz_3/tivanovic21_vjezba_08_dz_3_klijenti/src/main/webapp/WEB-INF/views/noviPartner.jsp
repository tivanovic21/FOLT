<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Dodavanje partnera</title>
    <style type="text/css">
        .poruka { color: red; }
        .uspjeh { color: green; }
    </style>
</head>
<body>
    <h1>Dodavanje partnera</h1>

    <ul>
        <li>
            <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a>
        </li>
        <li>
        	<a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner">Pregled partnera/restorana</a>
        </li>

        <%
        if (request.getAttribute("poruka") != null) {
            String poruka = (String) request.getAttribute("poruka");
            Object oPogreska = request.getAttribute("pogreska");
            boolean pogreska = oPogreska != null && (Boolean) oPogreska;
            if (!poruka.isEmpty()) {
        %>
        <li>
            <p class="<%= pogreska ? "poruka" : "uspjeh" %>"><%= poruka %></p>
        </li>
        <% } } %>

        <li>
            <p>Podaci partnera:</p>
            <form method="post" autocomplete="off" action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/noviPartner">
                <table>
                    <tr>
                        <td>Naziv:</td>
                        <td>
                            <input type="text" name="naziv" size="40" value="${naziv}" required />
                            <input type="hidden" name="${mvc.csrf.name}" value="${mvc.csrf.token}" />
                        </td>
                    </tr>
                    <tr>
                        <td>Vrsta kuhinje:</td>
                        <td><input type="text" name="vrstaKuhinje" size="30" value="${vrstaKuhinje}" /></td>
                    </tr>
                    <tr>
                        <td>Adresa:</td>
                        <td><input type="text" name="adresa" size="50" value="${adresa}" required /></td>
                    </tr>
                    <tr>
                        <td>Mrežna vrata:</td>
                        <td><input type="number" name="mreznaVrata" value="${mreznaVrata}" /></td>
                    </tr>
                    <tr>
                        <td>Vrata za kraj:</td>
                        <td><input type="number" name="mreznaVrataKraj" value="${mreznaVrataKraj}" /></td>
                    </tr>
                    <tr>
                        <td>GPS širina:</td>
                        <td><input type="number" name="gpsSirina" value="${gpsSirina}" step="0.000001" /></td>
                    </tr>
                    <tr>
                        <td>GPS dužina:</td>
                        <td><input type="number" name="gpsDuzina" value="${gpsDuzina}" step="0.000001" autocomplete="nope"/></td>
                    </tr>
                    <tr>
                        <td>Sigurnosni kod:</td>
                        <td><input type="text" name="sigurnosniKod" size="20" value="${sigurnosniKod}" autocomplete="nope"/></td>
                    </tr>
                    <tr>
                        <td>Admin kod:</td>
                        <td><input type="text" name="adminKod" size="20" value="${adminKod}" autocomplete="nope"/></td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td><input type="submit" value=" Dodaj partnera "></td>
                    </tr>
                </table>
            </form>
        </li>
    </ul>
</body>
</html>
