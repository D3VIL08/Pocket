# Pocket

A native Android app for tracking personal spending and subscriptions. Offline-first: everything
works with no network, and sync is designed as an addition rather than a rewrite.

> **Status — foundation + expense tracking.** This is the first slice: the module graph, build
> tooling, test infrastructure and CI, plus expense tracking working end to end from Room to
> Compose. Subscriptions, the dashboard, the home-screen widget and receipt scanning build on
> the same foundation; see [Roadmap](#roadmap).

## What works today

- **Quick expense entry** — the amount field takes focus with the numeric keypad already up, the
  category is preselected and the date defaults to today, so recording a coffee is a couple of taps.
- **Grouped list** — expenses under day headers with per-day and per-month totals, month-to-month
  navigation, and an empty state that says what would fill it.
- **Edit and delete** — tap a row to edit; swipe it away to delete, with an undo snackbar rather
  than a confirmation dialog.
- **Offline by construction** — Room is the source of truth and the UI reads `Flow`s straight off
  it, so a write anywhere refreshes every observer with no manual invalidation.

## Architecture

Multi-module Clean Architecture, following the [Now in Android][nia] layout. Dependencies point
inward: `:feature:*` and `:app` know `:core:domain`, never Room.

```
:app                    MainActivity, Application, type-safe NavHost
:feature:expenses       list + add/edit screens and their ViewModels
      │
      ▼
:core:domain     (JVM)  repository interfaces + use cases — no Android dependencies
:core:model      (JVM)  Expense, Category, Money, filters
:core:common     (JVM)  Outcome/FailureReason, dispatcher qualifiers
      ▲
:core:data              repository implementations, mappers, DataStore preferences
:core:database          Room entities, DAOs, seed callback, exported schemas
:core:designsystem      Material 3 theme and reusable composables
:core:ui                money/date formatters, category components, preview data
:core:testing    (JVM)  fakes, MainDispatcherRule, test data builders
build-logic/convention  Gradle convention plugins, so no module build file exceeds ~20 lines
```

### Decisions worth knowing

**Money is never a `Double`.** [`Money`][money] holds a `Long` count of minor units (cents, paise)
plus an ISO 4217 code. Binary floating point cannot represent most decimal fractions, so
`0.1 + 0.2 != 0.3` and repeated arithmetic drifts — unacceptable when the numbers are somebody's
spending. Addition is exact and overflow throws rather than wrapping. It maps to one SQLite
`INTEGER` column.

**Room owns the truth.** Repositories expose `Flow` reads directly from DAO queries. The UI is
offline-first because there is no other path, and V2 sync becomes another writer into the same
database rather than a parallel source.

**Validation lives in the domain.** [`ValidateExpenseDraft`][validate] holds the rules, so the add
screen, the edit screen and any future CSV importer cannot drift apart. `Clock` is injected, so
"not in the future" is testable rather than dependent on when the suite runs.

**Deleting a category never loses history.** The expense→category foreign key is `RESTRICT`, and
deletion reassigns affected expenses in a single DAO `@Transaction`. Losing spending records
because a label was removed would be a data-loss bug, not a tidy-up.

**Compose tests run on the JVM.** UI tests execute under Robolectric, so the entire suite runs from
`./gradlew test` with no emulator, and CI has nothing device-shaped to be flaky about.

## Testing

| Module | What it covers |
| --- | --- |
| `:core:model` | `Money` arithmetic, parsing, currency precision, overflow; date ranges |
| `:core:domain` | Use-case rules and failure mapping against fakes |
| `:core:database` | DAOs on real in-memory SQLite — SQL, indices, foreign keys, the seed |
| `:core:data` | Repositories over a real database, verifying entity↔domain mapping |
| `:core:ui` | Locale-aware money and date formatting |
| `:feature:expenses` | ViewModel state transitions (Turbine) and Compose UI under Robolectric |

```bash
./gradlew test          # everything, on the JVM (not testDebugUnitTest -- that skips the JVM modules)
./gradlew detekt        # static analysis (add -PdetektAutoCorrect=true to fix formatting)
./gradlew lintDebug     # Android Lint
./gradlew assembleDebug # debug APK
```

## Building

Requires **JDK 21** and the Android SDK (`compileSdk 36`). Clone and run `./gradlew assembleDebug`;
Gradle resolves everything else. `minSdk` is 26 — that covers the overwhelming majority of active
devices and gives `java.time` natively, so no core-library desugaring is needed.

### A note on toolchain versions

The build pins **AGP 8.13.2 / Kotlin 2.2.21 / compileSdk 36** rather than the newest AGP 9.x line.
AGP 9 moves Kotlin support in-tree, and KSP — which Room and Hilt both require here — is not yet
compatible with it; AGP 9 only accepts the standalone Kotlin plugin behind `android.newDsl=false`,
a flag Google documents as a temporary bypass. Resting a build on an officially unsupported
combination is not a trade worth making, so the AndroidX libraries are pinned to their
`compileSdk 36`-compatible versions. Moving to AGP 9 is a single coordinated bump once KSP
supports built-in Kotlin.

## CI

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) runs detekt, Android Lint, the full unit
test suite and a debug assemble on every push and pull request, uploading test reports (including
on failure) and the debug APK.

## Roadmap

- **Subscriptions** — recurrence rules in `:core:model`, renewal reminders via WorkManager.
- **Dashboard** — charts and a monthly budget. The per-category aggregate query and
  `ObserveMonthlySummary` already exist for it.
- **Home-screen widget** — Glance, reading the same repositories.
- **Receipt scanning** — CameraX + ML Kit text recognition, writing to `Expense.receiptUri`
  (the column is already in the schema).
- **V2** — shared groups for splitting bills, Firebase auth and sync, CSV export.

[nia]: https://github.com/android/nowinandroid
[money]: core/model/src/main/kotlin/dev/pocket/core/model/Money.kt
[validate]: core/domain/src/main/kotlin/dev/pocket/core/domain/usecase/ValidateExpenseDraft.kt
