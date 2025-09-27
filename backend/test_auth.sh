#!/bin/bash

# Test script for authentication and authorization
BASE_URL="http://localhost:8080"

echo "=== Testing Authentication and Authorization System ==="
echo

# Test 1: Login as admin user
echo "1. Testing admin login..."
ADMIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}')

echo "Admin login response: $ADMIN_RESPONSE"

# Extract admin token
ADMIN_TOKEN=$(echo $ADMIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Admin token: $ADMIN_TOKEN"
echo

# Test 2: Login as regular user
echo "2. Testing user login..."
USER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user123"}')

echo "User login response: $USER_RESPONSE"

# Extract user token
USER_TOKEN=$(echo $USER_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "User token: $USER_TOKEN"
echo

# Test 3: Access public endpoint (should work without token)
echo "3. Testing public endpoint (no auth required)..."
curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "test123", "email": "test@example.com"}'
echo
echo

# Test 4: Access protected endpoint without token (should fail)
echo "4. Testing protected endpoint without token (should fail)..."
curl -s -w "HTTP Status: %{http_code}\n" -X GET "$BASE_URL/api/products"
echo

# Test 5: Access user endpoint with valid user token (should work)
echo "5. Testing user endpoint with valid user token..."
curl -s -w "HTTP Status: %{http_code}\n" -X GET "$BASE_URL/api/products" \
  -H "Authorization: Bearer $USER_TOKEN"
echo

# Test 6: Access admin endpoint with user token (should fail)
echo "6. Testing admin endpoint with user token (should fail)..."
curl -s -w "HTTP Status: %{http_code}\n" -X GET "$BASE_URL/api/admin/users" \
  -H "Authorization: Bearer $USER_TOKEN"
echo

# Test 7: Access admin endpoint with admin token (should work)
echo "7. Testing admin endpoint with admin token..."
curl -s -w "HTTP Status: %{http_code}\n" -X GET "$BASE_URL/api/admin/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
echo

# Test 8: User can only see their own orders
echo "8. Testing user can only see their own orders..."
curl -s -X GET "$BASE_URL/api/orders" \
  -H "Authorization: Bearer $USER_TOKEN"
echo
echo

# Test 9: Admin can see all orders
echo "9. Testing admin can see all orders..."
curl -s -X GET "$BASE_URL/api/orders" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
echo
echo

# Test 10: User cannot create products (should fail)
echo "10. Testing user cannot create products (should fail)..."
curl -s -w "HTTP Status: %{http_code}\n" -X POST "$BASE_URL/api/products" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Product", "price": 99.99, "category": "Test"}'
echo

# Test 11: Admin can create products (should work)
echo "11. Testing admin can create products..."
curl -s -w "HTTP Status: %{http_code}\n" -X POST "$BASE_URL/api/products" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Admin Test Product", "price": 199.99, "category": "Admin Test", "description": "Test product created by admin", "stock": 10}'
echo

echo "=== Authentication and Authorization Tests Complete ==="