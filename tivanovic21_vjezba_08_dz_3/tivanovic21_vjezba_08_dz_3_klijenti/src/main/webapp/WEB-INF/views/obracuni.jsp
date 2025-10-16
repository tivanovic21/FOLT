<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="edu.unizg.foi.nwtis.podaci.Obracun"%>
<%@page import="java.time.Instant, java.time.LocalDateTime, java.time.ZoneId, java.time.format.DateTimeFormatter"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Privatni dio - Pregled obračuna</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        h1, h2 { margin-bottom: 15px; }
        .section { margin-bottom: 25px; }
        .navigation ul { padding-left: 20px; }
        .navigation li { display: inline; margin-right: 15px; }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: center; }
        th { background-color: #f0f0f0; }
        .message { margin-top: 20px; padding: 10px; border: 1px solid #ccc; background-color: #f9f9f9; }
        .highlight { background-color: #eef; padding: 8px; margin-top: 10px; }
        .jelo { background-color: #e9fbe5; }
        .pice { background-color: #fff3cd; }
        .number { text-align: right; }
        .link-style { text-decoration: underline; color: blue; cursor: pointer; }
    </style>
</head>
<body>

<h1>Privatni dio - Pregled obračuna</h1>

<div class="section navigation">
    <ul>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner">Partneri</a></li>
    </ul>
</div>

<div class="section">
    <h2>Filtri za pretraživanje</h2>
    <form method="get">
        <label>Vrijeme od (timestamp): <input type="number" name="od" value="${od}" /></label><br><br>
        <label>Vrijeme do (timestamp): <input type="number" name="do" value="${do}" /></label><br><br>
        <label>Tip obračuna:
            <select name="tip">
                <option value="sve" ${tip == 'sve' || tip == null ? 'selected' : ''}>Sve</option>
                <option value="jelo" ${tip == 'jelo' ? 'selected' : ''}>Jelo</option>
                <option value="pice" ${tip == 'pice' ? 'selected' : ''}>Piće</option>
            </select>
        </label><br><br>
        <button type="submit">Filtriraj</button>
    </form>

    <% if (request.getAttribute("od") != null || request.getAttribute("do") != null || request.getAttribute("tip") != null) { %>
        <div class="highlight">
            Filter: Od: ${od != null ? od : 'početak'} | Do: ${do != null ? do : 'kraj'} | Tip: ${tip != null ? tip : 'sve'}
        </div>
    <% } %>
</div>

<div class="section">
    <% if (request.getAttribute("obracuni") != null) {
        List<Obracun> obracuni = (List<Obracun>) request.getAttribute("obracuni");
        double ukupno = 0.0;
        int brojJela = 0;
        int brojPica = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    %>
        <h2>Rezultati (<%= obracuni.size() %> stavki)</h2>
        <% if (!obracuni.isEmpty()) { %>
            <table>
                <thead>
                    <tr>
                        <th>ID</th><th>Partner ID</th><th>Tip</th><th>Količina</th><th>Cijena</th><th>Ukupno</th><th>Vrijeme (DateTime | timestamp)</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Obracun o : obracuni) {
                        double stavka = o.kolicina() * o.cijena();
                        ukupno += stavka;
                        if (o.jelo()) brojJela++; else brojPica++;
                        LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(o.vrijeme()), ZoneId.systemDefault());
                    %>
                        <tr class="<%= o.jelo() ? "jelo" : "pice" %>">
                            <td><%= o.id() %></td>
                            <td>
                                <a class="link-style" href="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracuni/partner/<%= o.partner() %>">
                                    <%= o.partner() %>
                                </a>
                            </td>
                            <td><%= o.jelo() ? "Jelo" : "Piće" %></td>
                            <td class="number"><%= String.format("%.1f", o.kolicina()) %></td>
                            <td class="number"><%= String.format("%.2f", o.cijena()) %></td>
                            <td class="number"><%= String.format("%.2f", stavka) %></td>
                            <td><%= dt.format(formatter) %> (<%= o.vrijeme() %>)</td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
            <div class="message">
                Ukupno: €<%= String.format("%.2f", ukupno) %> | Jela: <%= brojJela %> | Pića: <%= brojPica %> | Stavki: <%= obracuni.size() %>
            </div>
        <% } else { %>
            <div class="message">Nema obračuna za zadane kriterije.</div>
        <% } %>
    <% } else if (request.getAttribute("greska") != null) { %>
        <div class="message">Greška: ${greska}</div>
    <% } else { %>
        <div class="message">Koristite filtere za pretraživanje obračuna.</div>
    <% } %>
</div>

</body>
</html>
