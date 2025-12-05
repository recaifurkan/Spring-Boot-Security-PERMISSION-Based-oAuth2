#!/bin/bash

echo "===> Access token alınıyor..."




#curl -s -X POST "http://localhost:9000/oauth2/token" \
#  -H "Content-Type: application/x-www-form-urlencoded" \
#  -d "grant_type=client_credentials" \
#  -d "scope=SCOPE_product.read" \
#  -d "client_id=ahmet" \
#  -d "client_secret=12345"

TOKEN=$(curl -s -X POST "http://localhost:9000/oauth2/token" \
  -u "ahmet:12345" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "scope=product.read" \
  | jq -r '.access_token')

echo "===> Token alındı:"
echo "$TOKEN"
echo ""

echo "===> /products endpoint'ine istek atılıyor..."

curl -X GET \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  http://localhost:8081/products -v

echo ""
echo "===> İşlem tamam"

#WWW-Authenticate: Bearer error="insufficient_scope", error_description="The request requires higher privileges than provided by the access token.", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"

