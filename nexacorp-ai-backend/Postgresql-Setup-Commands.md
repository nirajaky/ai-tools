

# Postgresql Database setup (Docker)

docker run --name nexacorp-postgres \
  -e POSTGRES_DB=nexacorp_knowledge \
  -e POSTGRES_USER=nexa \
  -e POSTGRES_PASSWORD=nexa \
  -p 5432:5432 \
  -d postgres
    

## Verify

`docker ps`