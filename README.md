# Crates
Simple and efficient crates plugin.

*Even though example.yml file is generated, it's not loaded as an actual crate. Plugin comes with no crates by default and you have to add them by yourself.*

## Building
To build, run `mvn install` or `mvn clean install`.  
Jar file named `Crates-[VERSION].jar` will be placed in `../git-builds/`

## Permissions
#### Recommended only for admins:
Permission | Description
--- | ---
`skydistrict.command.crates` | Using `/crates` command.
`skydistrict.command.crates.getcrate` | Using `/crates getcrate` command.
`skydistrict.command.crates.give` | Using `/crates give` command.
`skydistrict.command.crates.giveall` | Using `/crates giveall` command.
`skydistrict.command.crates.reload` | Using `/crates reload` command.
