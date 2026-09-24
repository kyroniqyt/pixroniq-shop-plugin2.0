# PixroniqShop

Coins, a cosmetics shop (colored tab names, prefixes, kill messages, hub pets), and command
hooks so your duels plugin can pay out rewards.

## Building

```
mvn package
```

Jar comes out at `target/PixroniqShop.jar`. Drop it in `plugins/` and restart.

## Before you set anything up: one thing to decide

Your original list showed **one price per category** (Colored Names — 1,000 coins, Prefixes
— 2,000, etc.) next to a *list* of options within each category. That's ambiguous between two
different shop designs, and it changes the economy a lot:

- **A: pay per option** — buying Cyan costs 1,000, buying Pink is a *separate* 1,000, owning
  all 8 colors costs 8,000 total. This is what's built below, since it's the simpler, more
  standard "shop item" model.
- **B: pay once per category** — 1,000 coins unlocks "colored names" as a feature, and every
  color in the list is then free to switch between with `/shop use`.

The commands below set it up as **A**. If you actually meant **B**, tell me and I'll add a
"category unlock" mode instead — it's a real code change, not just different commands.

## Setting up your actual shop items

Every item needs two commands: `additem` to create it, then `setvalue` to say what it does.

### Colored Names (COLOR)

```
/shop additem color cyan 1000 &bCyan
/shop setvalue cyan &b

/shop additem color pink 1000 &dPink
/shop setvalue pink &d

/shop additem color black 1000 &0Black
/shop setvalue black &0

/shop additem color yellow 1000 &eYellow
/shop setvalue yellow &e

/shop additem color blue 1000 &9Blue
/shop setvalue blue &9
```

I left out "YN", "Slice", and "Kyron Blue" from your list — not sure what colors those are
meant to be (they're not standard Minecraft color names). Tell me what they should map to
and I'll give you the exact commands, or just copy the pattern above yourself: pick any of
BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE,
GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE (or the matching `&` code letter).

### Prefixes (PREFIX)

```
/shop additem prefix legend 2000 &6Legend
/shop setvalue legend &6[Legend] 

/shop additem prefix bedwarsmaster 2000 &aBedWars Master
/shop setvalue bedwarsmaster &a[BedWars Master] 

/shop additem prefix skywarsgod 2000 &bSkyWars God
/shop setvalue skywarsgod &b[SkyWars God] 

/shop additem prefix duelsmonster 2000 &cDuels Monster
/shop setvalue duelsmonster &c[Duels Monster] 

/shop additem prefix killingmachine 2000 &4Killing Machine
/shop setvalue killingmachine &4[Killing Machine] 

/shop additem prefix immortal 2000 &5Immortal
/shop setvalue immortal &5[Immortal] 

/shop additem prefix touchgrass 2000 &2Touch Grass
/shop setvalue touchgrass &2[Touch Grass] 

/shop additem prefix ekitten 2000 &dE-Kitten
/shop setvalue ekitten &d[E-Kitten] 
```

(Trailing space after each prefix on purpose, so it doesn't run into the player's name.)

### Hub Pets (PET)

```
/shop additem pet dog 4000 &fDog
/shop setvalue dog DOG

/shop additem pet cat 4000 &fCat
/shop setvalue cat CAT

/shop additem pet capybara 4000 &fCapybara
/shop setvalue capybara CAPYBARA

/shop additem pet parrot 4000 &fParrot
/shop setvalue parrot PARROT
```

Remember: Capybara currently renders as a Turtle (see the note in `PetType.java`) since
there's no real capybara mob in Minecraft. Dog/Cat/Parrot are the real mobs and will look
correct on both Java and Bedrock.

### Kill Messages (KILLMESSAGE)

Each of your six lines becomes its own purchasable item:

```
/shop additem killmessage deleted 1250 Deleted
/shop setvalue deleted {killer} deleted {victim}.

/shop additem killmessage clipped 1250 Clipped
/shop setvalue clipped {killer} clipped {victim}.

/shop additem killmessage comboed 1250 Comboed
/shop setvalue comboed {victim} got comboed by {killer}.

/shop additem killmessage ragequit 1250 Rage Quit
/shop setvalue ragequit {killer} made {victim} rage quit.

/shop additem killmessage obliterated 1250 Obliterated
/shop setvalue obliterated {killer} obliterated {victim}.

/shop additem killmessage lobby 1250 Sent to Lobby
/shop setvalue lobby {killer} sent {victim} back to the lobby.
```

Your default/base message ("{killer} won the battle against {victim}") is what's shown when
a killer has never bought or equipped a kill message — it's built in as the fallback, nothing
to set up for it.

## Wiring this into your duels plugin

This plugin doesn't know anything about how your duels plugin decides who won, lost, or got
a kill — that logic lives entirely in your duels plugin. What it gives you is a **command
that plugin can call** when those things happen. Almost every duels/kit plugin has a "reward
commands" section in its config for exactly this. Point it at:

```
On win:  coins give %player% 50
On loss: coins give %player% 10
On kill: coins give %player% 5
         killmsg trigger %player% %victim%
```

Replace `%player%` / `%victim%` with whatever placeholder names your specific duels plugin
actually uses (check its config/wiki — the name varies by plugin). If your duels plugin
doesn't support reward commands at all, tell me which plugin it is and I'll find another way
to hook in.

## Player-facing commands (available to everyone, no permission needed)

```
/shop list                 - see everything for sale
/shop list color            - see just one category
/shop buy <id>               - buy it (auto-equips)
/shop use <id>                - switch to something you already own, for free
/shop owned                    - see what you've bought
/coins                           - check your own balance
/coins <player>                   - check someone else's balance
```

## Admin commands (op / pixroniqshop.admin)

```
/shop additem <category> <id> <price> <display name>
/shop setvalue <id> <value>
/shop removeitem <id>
/coins give|take|set <player> <amount>
/killmsg trigger <killer> <victim>   (meant to be called by your duels plugin, not typed by hand)
```

## Data storage

`plugins/PixroniqShop/shop.yml` — the item catalog (safe to hand-edit offline).
`plugins/PixroniqShop/playerdata.yml` — balances, ownership, and equipped selections.
