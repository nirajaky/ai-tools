

# Postgresql Database setup (Docker)

docker run --name nexacorp-postgres \
  -e POSTGRES_DB=nexacorp_knowledge \
  -e POSTGRES_USER=nexa \
  -e POSTGRES_PASSWORD=nexa \
  -p 5432:5432 \
  -d postgres
    

## Verify

`docker ps`

### Because my local db username and password is postgres

`psql -U postgres -d nexacorp_knowledge -f "C:\Users\NIRKUMA\Documents\poc\nexacorp-ai-backend\data\db\schema.sql"`

`psql -U postgres -d nexacorp_knowledge -f "C:\Users\NIRKUMA\Documents\poc\nexacorp-ai-backend\data\db\data.sql"
`
