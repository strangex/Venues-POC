# Buy Venues
This project is a simple proof-of-concept application that allows adding `venues` and buying them for money.

## Usage
The application can be started by executing `sbt run`.

### Creating/updating a venue
You can use `PUT` with a json containing id, name, and price to create/update a value.
```
> curl -XPUT -H "Content-Type: application/json" http://localhost:9000/venues -d '{
    "id" : "687e8292-1afd-4cf7-87db-ec49a3ed93b1",
    "name" : "Rynek Główny",
    "price" : 1000
  }'
{"status":200,"message":"Venue 687e8292-1afd-4cf7-87db-ec49a3ed93b1 created successfully!!"}
```

### Getting all venues
```
> curl http://localhost:9000/venues
{
    "status" : "200",
    "data" : [
        {
            "id": "687e8292-1afd-4cf7-87db-ec49a3ed93b1",
            "name": "Rynek Główny",
            "price": 1000
        }
    ] 
}
```

### Deleting venues
```
> curl -XDELETE "http://localhost:9000/venues/687e8292-1afd-4cf7-87db-ec49a3ed93b1"
{"status":200,"message":"Venue 687e8292-1afd-4cf7-87db-ec49a3ed93b1 has been deleted!!"}
```
Note that in case the provided Venue ID does not exist, the following response is received :
```
{"status":400,"message":"Venue 687e8292-1afd-4cf7-87db-ec49a3ed93b1 does not exist !!"}
```

### Hardcoded players
For now, two players are hardcoded and they are:
- `id=player1`, `money=500`
- `id=player2`, `money=2000`

Each restart of the application resets the state to the above.

### Buying a venue

#### Scenario 1: Buying a venue when player does not exist
```
> curl -XPOST -H "Content-Type: application/json" http://localhost:9000/venues/purchase -d '{
  "playerID" : "player0",
  "venueID" : "687e8292-1afd-4cf7-87db-ec49a3ed93b1"
}'
{"status" : 400,"message":"Player player0 doesn't exist!!"}
```

#### Scenario 2: Buying a venue when venue does not exist
```
> curl -XPOST -H "Content-Type: application/json" http://localhost:9000/venues/purchase -d '{
  "playerID" : "player1",
  "venueID" : "217e8292-1afd-4cf7-87db-ec49a3ed93b1"
}'
{"status":400,"message":"Venue 217e8292-1afd-4cf7-87db-ec49a3ed93b1 doesn't exist!!"}
```

#### Scenario 3: Buying a venue when player cannot afford it
```
> curl -XPOST -H "Content-Type: application/json" http://localhost:9000/venues/purchase -d '{
  "playerID" : "player1",
  "venueID" : "687e8292-1afd-4cf7-87db-ec49a3ed93b1"
}'
{"status":400,"message":"Player player1 can't afford Rynek Główny!!"}h
```

#### Scenario 4: Buying a venue when player can afford it
```
> curl -XPOST -H "Content-Type: application/json" http://localhost:9000/venues/purchase -d '{
  "playerID": "player2",
  "venueID" : "687e8292-1afd-4cf7-87db-ec49a3ed93b1"
}'
{"status":200,"message":"Rynek Główny was bought by player2 for 1000. Player player2 has 1000 left."}
```

```
> curl http://localhost:9000/venues
{
  "status" : "200",
   "data"[
  {
    "id": "687e8292-1afd-4cf7-87db-ec49a3ed93b1",
    "name": "Rynek Główny",
    "price": 1000,
    "owner:" "player2"
  }
]
```