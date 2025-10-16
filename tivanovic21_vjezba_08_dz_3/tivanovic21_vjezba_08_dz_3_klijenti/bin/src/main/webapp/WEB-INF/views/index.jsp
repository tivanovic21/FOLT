<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Vježba 8 - Zadaća 3 - Početna stranica</title>
    </head>
    <body>
        <h1>Vježba 8 - Zadaća 3 - Početna stranica</h1>
        <ul>     
        	<li>
        		<a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a>
        	</li>  
 	   	    <li>
        		<a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/status">Status poslužitelja tvrtka</a>
        	</li> 
	 	   	<li>
        		<a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/start/1">Start poslužitelja tvrtka 1</a>
        	</li> 
 	 	   	<li>
        		<a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pauza/1">Pauza poslužitelja tvrtka 1</a>
        	</li> 
        	<li>
        		<a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/start/2">Start poslužitelja tvrtka 2</a>
        	</li> 
 	 	   	<li>
        		<a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pauza/2">Pauza poslužitelja tvrtka 2</a>
        	</li> 
   	        <li>
        		<a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/kraj">Kraj rada poslužitelja tvrtka</a>
        	</li>                 
        </ul>          
    </body>
</html>
