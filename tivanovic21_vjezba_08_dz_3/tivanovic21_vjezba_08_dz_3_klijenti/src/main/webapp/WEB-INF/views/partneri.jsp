<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, edu.unizg.foi.nwtis.podaci.Partner" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>REST MVC - Pregled partnera</title>
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
        </style>
    </head>
    <body>
        <h1>Pregled partnera/restorana</h1>

        <div class="navigation">
            <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a>
        </div>

        <div>
            <%
            if(request.getAttribute("status") != null && (Integer)request.getAttribute("status") == 200) {
                List<Partner> partneri = (List<Partner>) request.getAttribute("partneri");
                if(partneri != null && !partneri.isEmpty()) {
            %>
                <p>Ukupno partnera: <strong><%= partneri.size() %></strong></p>

                <table>
                    <thead>
                        <tr>
                            <th>R.br.</th>
                            <th>ID</th>
                            <th>Naziv</th>
                            <th>Adresa</th>
                            <th>Mrežna vrata</th>
                            <th>Vrata za kraj</th>
                            <th>Admin kod</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                        int i = 0;
                        for(Partner p : partneri) {
                            i++;
                        %>
                        <tr>
                            <td><%= i %></td>
                            <td><%= p.id() %></td>
                            <td>
                                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/partner/<%= p.id() %>">
                                    <%= p.naziv() %>
                                </a>
                            </td>
                            <td><%= p.adresa() %></td>
                            <td><%= p.mreznaVrata() %></td>
                            <td><%= p.mreznaVrataKraj() %></td>
                            <td><%= p.adminKod() %></td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            <%
                } else {
            %>
                <div class="message">Trenutno nema dostupnih partnera.</div>
            <%
                }
            } else {
            %>
                <div class="message error">
                    Greška pri dohvaćanju partnera.
                    <% if(request.getAttribute("status") != null) { %>
                        Status kod: <%= request.getAttribute("status") %>
                    <% } %>
                </div>
            <%
            }
            %>
        </div>
    </body>
</html>
