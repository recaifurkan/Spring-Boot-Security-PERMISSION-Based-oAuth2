#!/bin/bash

echo "===> Access token alınıyor..."

TOKEN=$(curl -s -X POST "http://localhost:9000/oauth2/token" \
  -u "my-client:my-secret" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "username=ahmet" \
  -d "password=12345" \
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

