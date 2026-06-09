# Repository Guidelines

## Project Structure & Module Organization

This is a Gradle Java project for Marketi, an API for normalizing and querying Kosovo market price data. Production code
lives under `src/main/java/com/pelajtech/marketi`, grouped by responsibility:

- `item`: item records and units.
- `heuristics`: name, group-id, scoring, and unit-conversion helpers.
- `pipeline`: pipeline interfaces and local implementations.
- `event` and `log`: event and logging support.

Tests live in matching packages under `src/test/java`. Shared test fixtures belong in test helpers such as
`src/test/java/com/pelajtech/marketi/item/ItemHelpers.java`. Resources, when needed, should go in `src/main/resources`
or `src/test/resources`.

## Build, Test, and Development Commands

- `./gradlew test`: compile production/test code and run the JUnit suite.
- `./gradlew build`: run tests and produce the project jar under `build/libs`.
- `./gradlew clean`: remove Gradle build outputs.

Use the checked-in Gradle wrapper rather than a system Gradle install.

## Coding Style & Naming Conventions

Use Java with 4-space indentation and package names under `com.pelajtech.marketi`. Prefer small, focused classes and
package-private helpers when behavior is internal to a package. Public utilities should have clear names such as
`HeuristicScorer`, `NameHeuristics`, or `UnitConverter`.

Records are used for immutable data carriers. Tests use package-private classes and descriptive method names, for
example `reduceEmitsItemWithHighestScoringNameAcrossGroups`. Keep item examples realistic for Kosovo markets; Albanian
product names are preferred in tests.

Avoid nulls in project code when possible. Use `Optional` for absent values; only use null when an external library or
API requires it. Keep shared date/time behavior in `com.pelajtech.marketi.utils.DateUtils`, using its UTC clock instead
of local system timezone clocks.

Prefer direct, readable code over unnecessary indirection. Do not add custom factory interfaces or tiny one-line helper
methods unless they are reused or remove real complexity. For JSON, use typed Jackson records when the payload shape is
known instead of manually walking JSON trees. Keep external DTO records in a separate package-local class when they
would crowd a downloader or service.

## Testing Guidelines

The project uses JUnit Jupiter via `org.junit:junit-bom:6.0.0`. Add tests beside the code’s package in `src/test/java`.
Use `@ParameterizedTest` with `@CsvSource` for normalization tables and market-name permutations.

Run `./gradlew test` before submitting changes. Cover edge cases for heuristics, especially unit aliases, decimal
separators, Albanian diacritics, and ordering-sensitive reducer behavior. Avoid duplicating test object construction;
use `ItemHelpers.item(...)` for `ShoppingItem` fixtures.

For market downloaders, prefer real HTTP integration tests over mocked HTTP clients so downloader behavior is checked
against the actual market response shape. Keep those tests bounded with options such as `maxPages` or per-page limits.

## Commit & Pull Request Guidelines

Recent commits use short imperative or descriptive messages, such as `add unit converter` and
`add local item log and reducer`. Keep commits focused and mention the affected area when useful.

Pull requests should include a concise description, test results such as `./gradlew test`, and any behavior changes in
normalization, grouping, or reduction. Link related issues when applicable. Screenshots are not normally required for
this backend Java project.

## Agent-Specific Instructions

Do not commit generated build outputs from `build/` or local IDE metadata. Preserve existing user changes in the working
tree. When adding heuristics, prefer tests that document real market input variants before broadening regexes or
conversion maps.
