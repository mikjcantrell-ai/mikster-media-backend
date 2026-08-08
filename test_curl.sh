TOKEN=$(curl -s -X POST "https://accounts.spotify.com/api/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=client_credentials&client_id=$(grep spotify.client-id src/main/resources/application.properties | cut -d= -f2 | xargs)&client_secret=$(grep spotify.client-secret src/main/resources/application.properties | cut -d= -f2 | xargs)" | grep -o '"access_token":"[^"]*' | grep -o '[^"]*$')

curl -v -X GET "https://api.spotify.com/v1/search?type=track&limit=50&market=US&q=artist:beyonce" \
     -H "Authorization: Bearer $TOKEN"
