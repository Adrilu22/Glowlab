#!/usr/bin/env python3
"""
Script para generar tráfico de prueba hacia la API de GlowLab.
Uso: python scripts/generate_traffic.py
Requisitos: pip install requests
"""

import requests
import time
import random

BASE_URL = "http://localhost:8080"

ENDPOINTS = [
    ("GET", "/api/categorias"),
    ("GET", "/api/productos"),
    ("GET", "/api/usuarios"),
    ("GET", "/api/rutinas"),
    ("GET", "/api/productos/1"),
    ("GET", "/api/categorias/1"),
]

COMPRA_PAYLOAD = [{"productoId": 1}, {"productoId": 2}]


def send_request(method, path):
    url = BASE_URL + path
    try:
        if method == "POST":
            resp = requests.post(url, json=COMPRA_PAYLOAD, timeout=5)
        else:
            resp = requests.get(url, timeout=5)
        print(f"[{resp.status_code}] {method} {path}")
        return resp.status_code
    except requests.exceptions.ConnectionError:
        print(f"[ERROR] No se puede conectar a {BASE_URL} — ¿está corriendo la API?")
        return None
    except Exception as e:
        print(f"[ERROR] {path}: {e}")
        return None


def generate_traffic(requests_per_second: int = 3, duration_seconds: int = 120):
    print(f"Generando tráfico: {requests_per_second} req/s durante {duration_seconds}s")
    print(f"API: {BASE_URL}\n")

    interval = 1.0 / requests_per_second
    end_time = time.time() + duration_seconds
    total = 0
    errors = 0

    while time.time() < end_time:
        # ~10% de las peticiones son compras
        if random.random() < 0.1:
            status = send_request("POST", "/api/compras")
        else:
            method, path = random.choice(ENDPOINTS)
            status = send_request(method, path)

        if status is None:
            errors += 1
            time.sleep(2)
            continue

        total += 1
        time.sleep(interval)

    print(f"\nFinalizado. Total: {total} peticiones, errores de conexión: {errors}")


if __name__ == "__main__":
    generate_traffic(requests_per_second=3, duration_seconds=120)
