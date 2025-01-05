## Stardew Parser

Below are the supported copy flows.

### Player to Farmhand

Stardew Parser can copy a `player` from another world and add it as a `farmhand` to the destination save. To do so a
processing file written as the example below is required:

```json
{
  "fromFile": "sample/source_save",
  "toFile": "sample/destination_save",
  "clearFarmers": false,
  "characterType": {
    "characterType": "PLAYER",
    "uid": 12345,
    "copyAs": {
      "characterType": "FARMER",
      "replace": {
        "characterType": "FARMER",
        "uid": 45678
      }
    }
  }
}
```

- Identify the source player `UniqueMultiplayerID`. This uid identifies the player across games and links the player to
  the users' computer.
- Create a `copyAs` definition of `characterType` `FARMER`. This tells the parser that we are taking the `player` of uid
  `12345` and copying it over as a `FARMER`.
- Stardew Valley requires each Farmhand to have a house. Prior to copying over the player a house must be built. This
  creates a default empty house with an empty `Farmer` record.
- Create a `replace` definition of `characterType` `FARMER`.
- Define the `uid` to be replaced. When an empty house is built in Stardew it's assigned a placeholder farmhand with a
  defined `UniqueMultiplayerID`. In this case that id is `45678`.
- Finally, this will replace the placeholder farmhand of `uid` `45678` with the `player` of `uid` `12345` converted
  to a farmhand and assigned to the empty house.

### Player to Player

Stardew Parser can copy a single `player` from another world and add it as the `player` in a new save. To do so a
processing file written as the example below is required:

```json
{
  "fromFile": "sample/source_save",
  "toFile": "sample/destination_save",
  "clearFarmers": false,
  "character": {
    "characterType": "PLAYER",
    "uid": 12345,
    "copyAs": {
      "characterType": "PLAYER"
    }
  }
}
```

- Identify the source player `UniqueMultiplayerID`. This uid identifies the player across games and links the player to
  the users' computer.
- Create a `copyAs` definition of `characterType` `PLAYER`. This tells the parser that we are taking the `player` of uid
  `12345` and copying it over as a `PLAYER`.
- Finally, this will replace the `player` in the destination save with the `player` of `uid` `12345`. Stardew can only
  have a single `player` record per save. Any additional players are considered `farmhands`.

### Farmhand to Farmhand

Stardew Parser can copy a `farmhand` from one save to another. To do so a processing file written as the example below
is required.

```json
{
  "fromFile": "sample/source_save",
  "toFile": "sample/destination_save",
  "clearFarmers": false,
  "character": {
    "characterType": "FARMER",
    "uid": 12345,
    "copyAs": {
      "characterType": "FARMER",
      "replace": {
        "characterType": "FARMER",
        "uid": 45678
      }
    }
  }
}
```

- Identify the source Farmer `UniqueMultiplayerID`. This uid identifies the player across games and links the player to
  the users' computer.
- Create a `copyAs` definition of `characterType` `FARMER`. This tells the parser that we are taking the `farmhand` of
  uid
  `12345` and copying it over as a `FARMER`.
- Stardew Valley requires each Farmhand to have a house. Prior to copying over the farmhand a house must be built. This
  creates a default empty house with an empty `Farmer` record.
- Create a `replace` definition of `characterType` `FARMER`.
- Define the `uid` to be replaced. When an empty house is built in Stardew it's assigned a placeholder farmhand with a
  defined `UniqueMultiplayerID`. In this case that id is `45678`.
- Finally, this will replace the placeholder farmhand of `uid` `45678` with the `Farmer` of `uid` `12345`
  and assign it to the empty house.