<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Vježba 8 - zadaća 3 - Nadzorna konzola Tvrtka</title>
<style>
.status-radi {
	color: green;
	font-weight: bold;
}

.status-ne-radi {
	color: red;
	font-weight: bold;
}

.console-section {
	margin: 20px 0;
	padding: 10px;
	border: 1px solid #ccc;
}

.form-group {
	margin: 10px 0;
}
</style>
</head>
<body>
	<h1>Vježba 8 - zadaća 3 - Nadzorna konzola Tvrtka</h1>
	<ul>
		<li><a
			href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna
				stranica Tvrtka</a></li>
		<li><a
			href="${pageContext.servletContext.contextPath}/index.xhtml">Početna
				stranica Partner</a></li>
	</ul>

	<div class="console-section">
		<h3>Status poslužitelja Tvrtka</h3>
		<p>
			Status rada: <span id="statusRada"
				class="${not empty statusT and statusT == 200 ? 'status-radi' : 'status-ne-radi'}">
				${not empty statusT and statusT == 200 ? "RADI" : "NE RADI"} </span>
		</p>



		<p>
			Broj primljenih obračuna: <span id="brojObracuna">${globalniPodaci.brojObracuna}</span>

		</p>
	</div>

	<div class="console-section">
		<h3>Upravljanje poslužiteljem</h3>
		<ul>
			<li><a
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/kraj">Kraj
					rada poslužitelja Tvrtka</a></li>
			<li><a
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/status">Status
					poslužitelja Tvrtka</a></li>
			<li><a
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/start/1">Start
					poslužitelja Tvrtka - registracija</a></li>
			<li><a
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/pauza/1">Pauza
					poslužitelja Tvrtka - registracija</a></li>
			<li><a
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/start/2">Start
					poslužitelja Tvrtka - za partnere</a></li>
			<li><a
				href="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/pauza/2">Pauza
					poslužitelja Tvrtka - za partnere</a></li>
		</ul>
	</div>

	<div class="console-section">
		<h3>Interna poruka</h3>
		<div id="internaPorukaDisplay"
			style="min-height: 50px; background: #f5f5f5; padding: 10px; border: 1px solid #ddd;">
			<em>Nema poruke</em>
		</div>
	</div>

	<div class="console-section">
		<h3>Slanje interne poruke</h3>
		<form onsubmit="posaljiInternuPoruku(event)">
			<div class="form-group">
				<label for="porukaText">Poruka:</label><br>
				<textarea id="porukaText" name="poruka" rows="3" cols="50" required></textarea>
			</div>
			<div class="form-group">
				<button type="submit">Pošalji poruku</button>
			</div>
		</form>
	</div>

	<script type="text/javascript">
		let wsocket;
		let socketReady = false;

		function connect() {
			let adresa = window.location.pathname;
			let dijelovi = adresa.split("/");
			adresa = "ws://" + window.location.hostname + ":"
					+ window.location.port + "/" + dijelovi[1] + "/ws/tvrtka";

			if ('WebSocket' in window) {
				wsocket = new WebSocket(adresa);
			} else if ('MozWebSocket' in window) {
				wsocket = new MozWebSocket(adresa);
			} else {
				alert('WebSocket nije podržan od web preglednika.');
				return;
			}

			wsocket.onmessage = onMessage;

			wsocket.onopen = function() {
				console.log("WebSocket veza uspostavljena");
				socketReady = true;

				document.getElementById("porukaText").disabled = false;
				document.querySelector("button[type='submit']").disabled = false;
			};

			wsocket.onerror = function(error) {
				console.log("WebSocket greška: ", error);
				socketReady = false;
			};
		}

		function onMessage(evt) {
			const poruka = evt.data;
			console.log("Primljena poruka: " + poruka);

			const dijelovi = poruka.split(';');
			if (dijelovi.length >= 3) {
				const status = dijelovi[0];
				const brojObracuna = dijelovi[1];
				const internaPorukaText = dijelovi[2];

				const statusElem = document.getElementById("statusRada");
				if (status === "RADI") {
					statusElem.textContent = "RADI";
					statusElem.className = "status-radi";
				} else {
					statusElem.textContent = "NE RADI";
					statusElem.className = "status-ne-radi";
				}

				document.getElementById("brojObracuna").textContent = brojObracuna;

				const internaPorukaDiv = document
						.getElementById("internaPorukaDisplay");
				if (internaPorukaText && internaPorukaText.trim() !== "") {
					internaPorukaDiv.innerHTML = "<strong>Poruka:</strong> "
							+ internaPorukaText;
				} else {
					internaPorukaDiv.innerHTML = "<em>Nema poruke</em>";
				}
			}
		}

		function posaljiInternuPoruku(event) {
			event.preventDefault();
			const porukaText = document.getElementById("porukaText").value
					.trim();

			if (!porukaText) {
				alert("Unesite poruku.");
				return;
			}

			if (!wsocket || !socketReady
					|| wsocket.readyState !== WebSocket.OPEN) {
				alert("WebSocket veza nije još aktivna");
				return;
			}

			const brojObracuna = document.getElementById("brojObracuna").textContent;
			const poruka = "RADI;" + brojObracuna + ";" + porukaText;

			wsocket.send(poruka);
			document.getElementById("porukaText").value = "";
			console.log("Poslana poruka: " + poruka);
		}

		window.addEventListener("load", function() {

			document.getElementById("porukaText").disabled = true;
			document.querySelector("button[type='submit']").disabled = true;

			connect();
		});
	</script>


</body>
</html>
