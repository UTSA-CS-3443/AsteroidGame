# AsteroidGame

Repo for team: Undecided

## Description

Shoot asteroids, acquire upgrades, dodge downgrades. And also dodge asteroids too, I guess.

### Features

Short list:

#### Spaceship
- [x] Ability to shoot things
- [x] Explodes on collision with asteroid (unless you have extra lives)
- [ ] Mouse/keyboard control (basic functionality in main menu, but still need to allow arbitrary keys instead of hard-coded WASD)

#### Asteroids
- [x] Angular momentum
- [x] Procedurally generated shape
- [x] Surface normal for lighting

#### Upgrades
- [ ] Auto Aim
- [ ] Extra Life (works, but need to make the game *not* give you this upgrade when you already have 3 lives)
- [x] Fire Power + associated downgrade
- [x] Fire Rate + associated downgrade
- [x] Ghost Mode
- [ ] Laser Beam
- [ ] Lucky Dice
- [x] Speed + associated downgrade
- [ ] Time Warp (works, but need to make the game *not* give you this upgrade when time is already slow)
- [x] Wide Spread

#### Misc
- [ ] Difficulty setting (depends on settings menu)
- [x] Game can be paused and resumed by pressing escape
- [x] Game can end, and can be restarted
- [ ] Points (internally tracked, but not displayed)
- [ ] Settings menu (mouse/keyboard control is in main menu)
- [ ] Space-y background (still need to add stars for decoration)
- [ ] Stats are displayed at the top of the window (except for points)
- [ ] Upgrade purchasing menu

Longer list: [Here](https://github.com/UTSA-CS-3443/AsteroidGame/blob/main/todo.txt)

## Dependencies

Java 9.0.4 (Later versions might work, but are not tested.)

JavaFX (I can't find the version number in eclipse. Good luck.)

## Running

Nothing special. Just import the project into eclipse, make sure JavaFX is on the class path, and use `game.Main` as the main class.
