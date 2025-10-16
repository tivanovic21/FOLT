<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, edu.unizg.foi.nwtis.podaci.Korisnik" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>REST MVC - Pregled korisnika</title>
        <style type="text/css">
table, th, td {
  border: 1px solid;
}       
th {
	text-align: center;
	font-weight: bold;
} 
.desno {
	text-align: right;
}
        </style>
    </head>
    <body>
        <h1>REST MVC - Pregled korisnika</h1>
       <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/korisnici/pocetak">Početna stranica</a>
            </li>
            </ul>
            <br/>       
        <table>
        <tr><th>R.br. <th>Korisnik</th><th>Ime</th><th>Prezime</th><th>Email</th></tr>
	<%
	int i=0;
	List<Korisnik> korisnici = (List<Korisnik>) request.getAttribute("korisnici");
	for(Korisnik k: korisnici) {
	  i++;
	  %>
       <tr><td class="desno"><%= i %></td><td><%= k.getKorisnik() %></td><td><%= k.getIme() %></td><td><%= k.getPrezime() %></td><td><%= k.getEmail() %></td></tr>	  
	  <%
	}
	%>	
        </table>	        
    </body>
</html>
