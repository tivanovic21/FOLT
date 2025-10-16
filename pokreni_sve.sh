#!/usr/bin/env bash
set -e  # exit on any error

echo "mvn clean install"
mvn clean install


# 1) Build & run “tvrtka”
pushd tivanovic21_vjezba_07_dz_2_tvrtka
echo "Building tvrtka_tivanovic21..."
docker build -t tvrtka_tivanovic21 -f Dockerfile .
echo "Running tvrtka_tivanovic21..."
docker run -d -it \
  --network=mreza_tivanovic21 \
  --ip=20.24.5.2 \
  --name=tvrtka_tivanovic21 \
  --hostname=tvrtka_tivanovic21 \
  --mount source=svezak_tivanovic21,target=/usr/app/podaci \
    -p 8000:8000 -p 8001:8001 -p 8002:8002 \
  tvrtka_tivanovic21:latest
popd

# 2) Build & run “partner” (no PARTNER)
pushd tivanovic21_vjezba_07_dz_2_partner
echo "Building partner_tivanovic21_nopart..."
docker build -t partner_tivanovic21_nopart -f Dockerfile.noPartner .
echo "Running partner_tivanovic21_nopart..."
docker run -d -it \
  --network=mreza_tivanovic21 \
  --ip=20.24.5.3 \
  --name=partner_tivanovic21_nopart \
  --hostname=partner_tivanovic21 \
  --mount source=svezak_tivanovic21,target=/usr/app/podaci \
  -p 8010:8010 -p 8011:8011 \
  partner_tivanovic21_nopart:latest
popd

# 3) Build & run “partner” (with PARTNER)
pushd tivanovic21_vjezba_07_dz_2_partner
echo "Building partner_tivanovic21 (with PARTNER)..."
docker build -t partner_tivanovic21 -f Dockerfile .
echo "Running partner_tivanovic21 (with PARTNER)..."
docker run -d -it \
  --network=mreza_tivanovic21 \
  --ip=20.24.5.3 \
  --name=partner_tivanovic21 \
  --hostname=partner_tivanovic21 \
  --mount source=svezak_tivanovic21,target=/usr/app/podaci \
  -p 8010:8010 -p 8011:8011 \
  partner_tivanovic21:latest
popd

# 4) Build & run “servisi”
pushd tivanovic21_vjezba_07_dz_2_servisi
echo "Building servis_tivanovic21..."
docker build -t servis_tivanovic21 -f Dockerfile .
echo "Running servis_tivanovic21..."
docker run -d -it \
  --network=mreza_tivanovic21 \
  --ip=20.24.5.20 \
  --name=servis_tivanovic21 \
  --hostname=servis_tivanovic21 \
  servis_tivanovic21:latest
popd

echo
echo "=== ALL CONTAINERS STARTED ==="
