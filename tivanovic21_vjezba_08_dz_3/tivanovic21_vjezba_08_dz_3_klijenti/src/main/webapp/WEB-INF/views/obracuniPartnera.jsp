<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="edu.unizg.foi.nwtis.podaci.Obracun"%>
<%@page import="java.time.Instant, java.time.LocalDateTime, java.time.ZoneId, java.time.format.DateTimeFormatter"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Privatni dio - Obračuni partnera</title>
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
    </style>
</head>
<body>

<h1>Obračuni partnera ID: ${partnerId}</h1>

<div class="section navigation">
    <ul>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/privatno/obracuni">Svi obračuni</a></li>
        <li><a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner">Partneri</a></li>
    </ul>
</div>

<div class="section">
    <h2>Filtri</h2>
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
            Filter: Od: ${od != null ? od : 'početak'} |
            Do: ${do != null ? do : 'kraj'} |
            Tip: ${tip != null ? tip : 'sve'}
        </div>
    <% } %>
</div>

<div class="section">
    <% 
    if (request.getAttribute("obracuni") != null) {
        List<Obracun> obracuni = (List<Obracun>) request.getAttribute("obracuni");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        int shownCount = 0;
    %>
        <h2>Obračuni partnera</h2>

        <% if (!obracuni.isEmpty()) { %>
            <table>
                <thead>
                    <tr><th>ID</th><th>Tip</th><th>Ukupno (€)</th><th>Vrijeme (DateTime | timestamp)</th></tr>
                </thead>
                <tbody>
                    <% for (Obracun o : obracuni) {
                        boolean isJelo = o.jelo();
                        String currentTip = isJelo ? "jelo" : "pice";
                        String selectedTip = request.getParameter("tip");

                        if (selectedTip != null && !"sve".equals(selectedTip) && !selectedTip.equals(currentTip)) {
                            continue;
                        }

                        double ukupno = o.kolicina() * o.cijena();
                        LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(o.vrijeme()), ZoneId.systemDefault());
                        shownCount++;
                    %>
                        <tr class="<%= isJelo ? "jelo" : "pice" %>">
                            <td><%= o.id() %></td>
                            <td><%= isJelo ? "Jelo" : "Piće" %></td>
                            <td><%= String.format("%.2f", ukupno) %></td>
                            <td><%= dt.format(formatter) %> (<%= o.vrijeme() %>)</td>
                        </tr>
                    <% } %>
                </tbody>
            </table>

            <% if (shownCount == 0) { %>
                <div class="message">Nema obračuna za zadani filter tipa.</div>
            <% } %>
        <% } else { %>
            <div class="message">Nema obračuna za zadane kriterije.</div>
        <% } %>

    <% } else if (request.getAttribute("greska") != null) { %>
        <div class="message">Greška: ${greska}</div>
    <% } else { %>
        <div class="message">Koristite filtere za pretraživanje obračuna partnera.</div>
    <% } %>
</div>

</body>
</html>
