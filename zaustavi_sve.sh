#!/usr/bin/env bash
set -e

echo "Zaustavljam kontejnere (ako su pokrenuti)…"
docker stop tvrtka_tivanovic21 partner_tivanovic21_nopart partner_tivanovic21 servis_tivanovic21 2>/dev/null || true

echo "Brišem kontejnere…"
docker rm tvrtka_tivanovic21 partner_tivanovic21_nopart partner_tivanovic21 servis_tivanovic21 2>/dev/null || true

echo "Brišem slike..."
docker rmi tvrtka_tivanovic21 partner_tivanovic21_nopart partner_tivanovic21 servis_tivanovic21 2>/dev/null || true

echo "Sve obrisano."
