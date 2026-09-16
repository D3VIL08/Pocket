# Working in this repository

Conventions here are enforced by `detekt` and the convention plugins, not by review alone. Run
`./gradlew detekt test` before considering a change done.

## Module rules

- Dependencies point inward. `:feature:*` and `:app` depend on `:core:domain`; **nothing outside
  `:core:database` may import `androidx.room`**. If a feature seems to need a transaction, the
  transaction belongs in a DAO.
- `:core:model`, `:core:domain`, `:core:common` and `:core:testing` are **pure JVM**. Adding an
  Android dependency to any of them is a design error, not a build fix — `:core:testing` is JVM
  specifically so the JVM-only `:core:domain` can consume it.
- A new feature module gets `alias(libs.plugins.pocket.android.feature)` and nothing else; the
  convention plugin supplies Compose, Hilt and the standard `:core:*` wiring.
- All versions live in `gradle/libs.versions.toml`. Never write a version literal in a module
  build file.

## Code conventions

- **Money is `Money`, never `Double` or `BigDecimal` in storage.** Minor units as a `Long`.
- **Dates**: `LocalDate` for "what day did this happen" (a calendar question), `Instant` only for
  ordering. Inject `Clock`; never call `LocalDate.now()` with no argument in production code.
- **Validation belongs in use cases**, not ViewModels — the rules must hold for every caller.
- **ViewModels** expose a sealed `UiState` via `StateFlow` and one-shot effects via a
  `Channel`-backed `Flow`. Collect with `collectAsStateWithLifecycle`.
- **Screens split in two**: a stateful `XRoute` that takes the ViewModel, and a stateless
  `XScreen` that takes plain values. Previews and tests drive the stateless half.
- **Repository reads return `Flow`**; writes are `suspend` and go through the injected IO
  dispatcher.

## Testing conventions

- Prefer hand-written fakes in `:core:testing` over mocks — assert what the user ends up seeing,
  not which method was called.
- DAO and repository tests run against real in-memory Room, not a faked DAO; a wrong column
  mapping is invisible otherwise.
- Compose tests run under Robolectric on the JVM (`@RunWith(AndroidJUnit4::class)`). Do not add
  emulator-based `androidTest` sources — library modules have that variant disabled on purpose.
- Pin time with `TestData.fixedClock`. Do not assert on CLDR month abbreviations ("Sep" vs
  "Sept" varies by JDK); assert the contract instead.

## Gotchas

- `ImportOrdering` depends on `ij_kotlin_imports_layout` in `.editorconfig`. Without it, detekt's
  bundled ktlint check and its own formatter disagree. Use
  `./gradlew detekt -PdetektAutoCorrect=true` to fix formatting.
- Room's exported schemas in `core/database/schemas/` are checked in. A schema change needs a
  migration and an updated JSON; `fallbackToDestructiveMigration` is deliberately not used because
  spending history is not disposable.
- Adding a `CategoryIcon` enum entry is safe; renaming or removing one needs a migration, because
  the enum's `name` is what the database stores.
