# Stardew Parser

A Java tool for migrating characters between Stardew Valley save files. Supports copying players and farmhands across saves via an interactive terminal UI or a JSON instruction file.

## Requirements

- Java 17+
- Maven 3.x

## Build

```bash
mvn clean package
```

This produces an executable fat JAR in `target/`. Modified save files are written to the `generated/` directory.

## Usage

### Through UI Tool

Launch the JAR and follow the interactive terminal prompts:

```bash
java -jar target/stardew-parser-*.jar
```

1. Select a copy flow from the menu (Player → Player or Player → Farmhand).
2. Paste or type the path to your source and destination save files.
3. The tool auto-detects player UIDs where possible, or prompts you to select them.
4. For farmhand copies, choose an available empty farmhand slot from the list.
5. The modified save is written to the `generated/` directory.

---

### Through JSON Instruction File

Create a JSON instruction file and pass it to the parser. The supported copy flows are described below.

#### Player to Farmhand

Copies a `player` from one save and inserts them as a `farmhand` in another. A cabin (empty house) must already exist in the destination save before running this.

```json
{
  "fromFile": "sample/source_save",
  "toFile": "sample/destination_save",
  "clearFarmers": false,
  "character": {
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

- `uid` — the `UniqueMultiplayerID` of the source player.
- `copyAs.characterType: FARMER` — converts the player into a farmhand.
- `replace.uid` — the `UniqueMultiplayerID` of the placeholder farmhand assigned to the empty cabin. The source player replaces this placeholder.

#### Player to Player

Copies a `player` from one save and replaces the `player` in another. Only one player record can exist per save; all others are farmhands.

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

- `uid` — the `UniqueMultiplayerID` of the source player.
- `copyAs.characterType: PLAYER` — keeps the character as a player in the destination save.

#### Farmhand to Farmhand

Copies a `farmhand` from one save into an empty cabin in another. A cabin must already exist in the destination save before running this.

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

- `uid` — the `UniqueMultiplayerID` of the source farmhand.
- `copyAs.characterType: FARMER` — keeps the character as a farmhand.
- `replace.uid` — the `UniqueMultiplayerID` of the placeholder farmhand in the destination cabin. The source farmhand replaces this placeholder.
