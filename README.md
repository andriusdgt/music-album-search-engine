# music-album-search-engine

It's a backend app which lets users query for artists, mark the favorite ones and check their top albums. It queries iTunes API to fetch artist and album data, HSQLDB to persist queried data and minimize queries made to external API and it also uses Redis cache to further improve performance.

Prerequisites:
* JDK 11 installed

### Launching the server

To run the app just use this command:

```
./gradlew run
```

Optionally to separately compile the project use command:

```
./gradlew clean build
```

Or to launch unit and integration tests use command:

```
./gradlew clean test
```

### API

| Method | Path                                                                | Purpose                         |
|--------|---------------------------------------------------------------------|---------------------------------|
| GET    | http://localhost:8080/artist/name/{artistName}                      | Searches artists by name        |
| GET    | http://localhost:8080/artist/{artistAmgId}/album/top                | Gets artist top albums          |
| POST   | http://localhost:8080/user                                          | Creates a new user              |
| PUT    | http://localhost:8080/artist/favorite/{artistAmgId}?userId={userId} | Saves a favorite artist         |
| GET    | http://localhost:8080/artist/favorite?userId={userId}               | Gets all saved favorite artists |
