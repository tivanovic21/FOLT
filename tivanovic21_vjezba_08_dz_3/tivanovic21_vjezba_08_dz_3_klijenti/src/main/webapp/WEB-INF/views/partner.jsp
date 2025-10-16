<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="edu.unizg.foi.nwtis.podaci.Partner"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>REST MVC - Detalji partnera</title>
    <style>
        body {
            font-family: sans-serif;
            margin: 20px;
        }
        table, th, td {
            border: 1px solid #000;
            border-collapse: collapse;
        }
        th, td {
            padding: 6px;
            text-align: left;
        }
        .navigation a {
            margin-right: 10px;
        }
        .message {
            margin-top: 15px;
            font-style: italic;
        }
        .error {
            color: red;
        }
        .partner-name {
            font-weight: bold;
            margin-bottom: 10px;
        }
    </style>
</head>
<body>
    <h1>Detalji partnera</h1>

    <div class="navigation">
        <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a>
        <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner">Povratak na popis partnera</a>
    </div>

    <div>
        <%
        if(request.getAttribute("partner") != null) {
            Partner partner = (Partner) request.getAttribute("partner");
        %>
            <div class="partner-name"><%= partner.naziv() %></div>

            <table>
                <tr>
                    <th>Partner ID</th>
                    <td><%= partner.id() %></td>
                </tr>
                <tr>
                    <th>Naziv partnera</th>
                    <td><%= partner.naziv() %></td>
                </tr>
                <tr>
                    <th>Adresa</th>
                    <td><%= partner.adresa() %></td>
                </tr>
                <tr>
                    <th>Mrežna vrata</th>
                    <td><%= partner.mreznaVrata() %></td>
                </tr>
                <tr>
                    <th>Vrata za kraj</th>
                    <td><%= partner.mreznaVrataKraj() %></td>
                </tr>
                <tr>
                    <th>Admin kod</th>
                    <td><%= partner.adminKod() %></td>
                </tr>
            </table>
        <%
        } else if(request.getAttribute("greska") != null) {
        %>
            <div class="message error">
                Greška: <%= request.getAttribute("greska") %>
            </div>
        <%
        } else {
        %>
            <div class="message error">
                Partner nije pronađen.
            </div>
        <%
        }
        %>
    </div>
</body>
</html>