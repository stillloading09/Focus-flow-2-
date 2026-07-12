# FocusFlow

A time-blocking productivity app for Android — Kotlin, Jetpack Compose, Material 3, MVVM, Room, Hilt.

## CI fix (this drop): `Could not find or load main class "-Xmx64m"`

**Root cause:** `gradlew`'s `DEFAULT_JVM_OPTS` was defined as `'"-Xmx64m" "-Xms64m"'` — with the double-quote characters embedded *inside* the string. When that variable is expanded unquoted later in the script (`exec "$JAVACMD" $DEFAULT_JVM_OPTS ...`), POSIX `sh` only performs whitespace word-splitting on unquoted expansions — it does **not** re-interpret quote characters as syntax. So `java` received a literal argument of `"-Xmx64m"`, quotes and all. Since that token doesn't start with `-`, java's launcher treated it as a main-class name instead of a JVM flag — exactly matching `Could not find or load main class "-Xmx64m"`.

**Fix:** rewrote `gradlew` with `DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"` (no embedded quotes). Since neither flag itself contains a space, plain unquoted word-splitting on `$DEFAULT_JVM_OPTS` is sufficient and correct — no `eval` trick needed. Verified by hand: substituting the final `exec` line for an `echo` and running the script shows a clean token list (`-Xmx64m -Xms64m -Dorg.gradle.appname=... -classpath ... org.gradle.wrapper.GradleWrapperMain ...`) with no stray quote characters.

`gradlew.bat` did **not** have this bug — Windows argument parsing strips matching quotes before the target process ever sees them, so `"-Xmx64m" "-Xms64m"` was already arriving at `java.exe` clean. Simplified it to match the Unix script's style anyway, for consistency; it wasn't broken.

`gradle.properties` (`org.gradle.jvmargs=-Xmx2048m`) is unrelated — that flag configures the Gradle *daemon*, not the wrapper launcher, and has no quoting issue.

Added `.github/workflows/android-ci.yml` — a minimal workflow that checks out the repo, sets up JDK 17, and runs `./gradlew assembleDebug`, so this is self-verifying in CI. If you already have your own workflow file, compare against this one or replace it — don't assume this is your only/canonical workflow.

**One gap this fix alone doesn't close:** `gradle/wrapper/gradle-wrapper.jar` — the actual binary `gradlew` invokes — still isn't in this project. I flagged this in the previous repair pass and it's still true: I have no network access in this sandbox, so I can't fetch or compile that jar myself. Without it, `./gradlew assembleDebug` has nothing to execute even with the script logic fixed. The `.github/workflows/android-ci.yml` I added works around this by installing Gradle directly on the runner (`gradle/actions/setup-gradle`) and running `gradle wrapper --gradle-version 8.7` to generate a real wrapper jar before the `./gradlew assembleDebug` step — that part of CI does have network access, so this should close the gap there. For local builds outside CI, you'll still want to run `gradle wrapper` once yourself (any local Gradle/Android Studio install) to commit a permanent wrapper jar to the repo — at that point the extra CI step becomes redundant and you can remove it.



You reported the project wasn't compiling. I audited every file — package declarations, imports, Gradle config, manifest, resources — and found three real, confirmed bugs (not the folder-structure issue described, see note below):

1. **Missing icon dependency.** `material3` doesn't bundle `Icons.Filled.*` — that's a separate artifact. Icons like `Work`, `School`, `FitnessCenter`, `Bedtime`, `DragIndicator` (used in `BlockCategory` and the timeline UI) would fail to resolve. Added `androidx.compose.material:material-icons-core` and `-extended` to `app/build.gradle.kts`, plus an explicit `androidx.compose.foundation:foundation` dependency.
2. **Missing launcher icon.** `AndroidManifest.xml` pointed at `@mipmap/ic_launcher`, but no such resource existed anywhere — a guaranteed AAPT2 resource-linking failure. Added an adaptive icon (`mipmap-anydpi-v26/ic_launcher.xml` + `ic_launcher_round.xml`, backed by simple vector drawables and a color resource). `minSdk` is 26, the same version adaptive icons were introduced, so no legacy PNG fallback is needed.
3. **No Gradle wrapper at all.** There was no `gradlew`, `gradlew.bat`, or `gradle/wrapper/` directory in the project — which is exactly why `./gradlew assembleDebug` couldn't run; the command didn't exist. Added `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.properties` pinned to Gradle 8.7 (the minimum required by AGP 8.5.0).

**One thing I could not fix here and want to be upfront about:** `gradle/wrapper/gradle-wrapper.jar` — the actual binary the wrapper scripts invoke — is not included. This sandbox has no network access and no local Gradle install, so I have no way to fetch or generate that jar. This is the one remaining manual step, and it's a one-time, no-typing fix: open the project in Android Studio and it will detect the missing wrapper jar and offer to regenerate it automatically (or run `gradle wrapper` once from any machine with Gradle installed). After that, `./gradlew assembleDebug` should run cleanly.

I also checked and did **not** find the malformed `{ui/theme,ui/timeline,...}` literal-brace directories described — that was leftover debris from an earlier `mkdir` command in my own working folder here, not something that shipped in the ZIP I gave you previously; it contained no files and I've deleted it regardless. If the copy you have does contain that literal directory, delete it — it's empty and unused, not the actual source of any compile error.

Verified by hand (no build tool available in this environment to run automatically, see below):
- Every `.kt` file's package declaration matches its folder path.
- No import points at a class/package that doesn't exist in the dependency set (after fix #1).
- Every `@mipmap/@drawable/@color/@style` reference in code or XML resolves to an actual resource (after fix #2).
- Navigation routes/arguments match the `SavedStateHandle` keys read in `AddEditBlockViewModel`.
- No duplicate or conflicting Gradle dependency versions; AGP/Kotlin/KSP/Hilt/Compose-compiler versions are a known-compatible combination.

**Important limitation:** this sandbox has no Android SDK, no Gradle binary, and no network access, so I was not able to literally execute `./gradlew assembleDebug` and watch it succeed — I did a full manual static audit instead. Please run the build on your end and send me the exact error output if anything still fails; I'll fix it from there.

## Milestone 1: project skeleton + core timeline

- **Project setup** — Gradle Kotlin DSL, Hilt + KSP wired up.
- **Data layer** — `TimeBlockEntity` (Room), `Converters` for `LocalDate`/`LocalTime`/enum, `TimeBlockDao`, `FocusFlowDatabase`.
- **Domain layer** — `TimeBlock` model with `isActiveAt()` / `progressAt()` helpers.
- **Repository** — `TimeBlockRepository` / `TimeBlockRepositoryImpl`, exposed as Kotlin `Flow`.
- **DI** — Hilt modules providing the database, DAO, and repository as singletons.
- **UI** — `TimelineScreen` + `TimelineViewModel`: a live-updating (1s tick) vertical list of blocks for the selected day, with the currently-active block visually distinguished by a soft animated glow.
- **Theme** — Material 3 color schemes for Light / Dark / AMOLED.
- **Navigation** — `FocusFlowNavGraph`.

## Milestone 2: Add/Edit block screen + drag-to-reorder + resize

- **Add/Edit screen** (`AddEditBlockScreen` + `AddEditBlockViewModel`) — create a new block or edit an existing one: title, category picker, start/end time pickers, a duration stepper, notes, and a recurring toggle.
- **Delete** — confirmation dialog, then removes the block and pops back.
- **Duplicate** — clones the block immediately after its own end time with the same duration.
- **Drag-to-reorder** — long-press the drag handle to pick a block up; on drop, every block in the day is reflowed sequentially, preserving each block's own duration.
- **Drag-to-resize** — a handle at the bottom edge of each card adjusts that block's end time.
- **Navigation** — real `add_edit_block?blockId={id}&date={date}` route.

### Known limitation to revisit
The reorder drag reads list indices from live Compose state while a `saveBlocks` write is in flight; on a slow write this could momentarily desync the dragged item from the pointer. Fine for typical use, worth hardening later.

## Not yet built (future milestones, in planned order)

1. Focus Mode full-screen (progress ring, pause/complete/skip/extend)
2. Notifications (WorkManager-scheduled, pre-start/start/end)
3. Custom categories
4. Calendar / Agenda / Week views
5. Statistics & heatmap (charts)
6. Habits & recurrence rules (the `isRecurring` flag exists but recurrence isn't expanded into future days yet)
7. Templates
8. Goals
9. Home screen widget (Glance)
10. Gamification (XP, streaks, badges)
11. Search & filters
12. Backup/export (JSON/CSV, Drive)
13. Accessibility pass, animation polish (shared-element transitions, parallax)

## Building

1. Open in Android Studio (Koala+).
2. If prompted about a missing Gradle wrapper jar, let Android Studio regenerate it (or run `gradle wrapper` once with any local Gradle install).
3. Let Gradle sync pull dependencies.
4. Run `./gradlew assembleDebug`, or run on a device/emulator with API 26+ directly from Android Studio.


