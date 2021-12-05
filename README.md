# Crates
Simple and efficient crates plugin.

*Even though example.json file is generated, it's not loaded as an actual crate. Plugin comes with no crates by default and you have to add them by yourself.*

## Running
### Supported server software
Server | Version
--- | ---
[Paper](https://github.com/PaperMC/Paper) (and forks...) | 1.18 | https://papermc.io/downloads

### External dependencies
Depdency | Required
--- | ---
[Indigo](https://github.com/Grabsky/Indigo) | Yes

## Building
To build, run `gradle build` or `gradle clean build`.

## Permissions
#### Recommended only for admins:
Permission | Description | Admin*
--- | --- | ---
`crates.command.crates` | Using `/crates` command. | Yes
`crates.command.crates.getcrate` | Using `/crates getcrate` command. | Yes
`crates.command.crates.give` | Using `/crates give` command. | Yes
`crates.command.crates.giveall` | Using `/crates giveall` command. | Yes
`crates.command.crates.reload` | Using `/crates reload` command. | Yes

\* Recommended only for players with admin rights
