# Guide to the Fish Index

The fish index uses the datapack system so that other mods and datapacks can
easily implement their own entries that work nicely with the preexisting
entries.

The directory to target is `data/[namespace]/fishing_index/fishing_index.json`.

The default fishing index can be found [here](./src/main/resources/data/fishingoverhaul/fishing_index/fishing_index.json)

## Properties

The Fishing Index is made of two parts, a list of specific entries (`"entries"`) 
and a default entry (`"default"`).

Each element in `"entries"` should look like the following

```json
{
  "item": "minecraft:salmon",
  "catch_chance": 0.1,
  "variability": 0.05,
  "crit_chance": 0.3,
  "speed_multiplier": 1.0
}
```

- `catch_chance`: The portion of the circle that is taken up by the catch zone
- `variability`: The catch chance is centered around `catch_chance` and varies 
  by a maximum of this amount
- `crit_chance`: The portion of the catch zone that is taken up by the crit zone
  (ie. 0.3 `catch_chance` and 0.1 `crit_chance` leads to 
  0.03 of the circle taken up by the crit zone)
- `speed_multiplier`: A multiplier to the fish speed

The following properties are optional and, if omitted, will default to the above values:  

- `"crit_chance"`
- `"speed_multiplier"`
