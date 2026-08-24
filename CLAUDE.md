# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Teaching/demo project (UCO talk): an algebraic design for a bank domain model in Scala 3 + Cats Effect, exposed over an http4s Ember server. Root package is `co.edu.uco.xebia.bank`.

## Commands

Requires JDK 21 (`.java-version`) and sbt.

```sh
sbt validate            # alias: scalafmtCheckAll; compile; test  (same command CI runs)
sbt server/run          # run the app (forked JVM); serves on 0.0.0.0:8080
sbt domain/compile      # compile a single module
sbt scalafmtAll         # apply formatting
sbt "testOnly co.edu.uco.xebia.bank.SomeSpec"   # single test (no test framework wired up yet)
```

There are currently **no test sources and no test framework dependency**. Adding tests means adding the framework (e.g. munit-cats-effect / weaver) to `build.sbt` first.

`sbt-tpolecat` is enabled, so scalac is strict. The only global escape hatch is in `build.sbt`: `-Wconf:any:verbose,id=E198:silent` (silences "unused explicit parameter").

## Architecture

Two sbt modules; `server` depends on `domain`. `domain` has no server dependencies (it does pull http4s-client for the FX gateway).

### Algebra pattern

Every capability follows the same shape and this convention should be preserved for new capabilities:

- a `trait` naming the operations, all returning `IO[...]`
- a companion `object` with a `make` factory returning either the value directly (`Accounts.make`, `MoneyCalculator.make`, `Payments.make`) or `Resource[IO, A]` when it owns resources (`CurrencyConverter.make`, `FrankfurterApi.make`, `InMemoryCache.make`)
- the implementation is an anonymous class inside `make`; private helpers live there, closing over the injected dependencies

Wiring happens exactly once, in `Main.run`: a `Resource` for-comprehension builds persistence → converter → calculator → accounts → payments, then `.use` runs the demo scenario and starts the server.

### Bounded contexts

`domain/.../accounts` and `domain/.../payments`, each split into `algebras/` (behavior) and `models/` (data, errors, HTTP request DTOs). `shared/` holds cross-context value types (`Money`, `Currency`, `Rate`, `NonEmptyString`). Payments depends on accounts (`AccountId`), not the reverse.

### Type discipline

- **Opaque subtypes** for identifiers and names: `opaque type AccountId <: UUID = UUID` etc. The `<: UUID`/`<: String` upper bound means they are transparently usable as the underlying type (and so `Encoder[UUID].contramap(identity)` works) but cannot be constructed implicitly.
- **Iron refinements** for numerics: `MoneyAmount` (`Positive0`) and `Rate` (`Positive`) extend `RefinedSubtype`. Use `.either`/`.from` at boundaries; `applyUnsafe` only where the arithmetic provably preserves the constraint — every existing call site carries a comment justifying it. Follow that.
- **Errors are enums extending `RuntimeException` with `NoStackTrace`**, raised via `IO.raiseError` — not `Either` in the algebra signatures. `AccountError`, `PaymentError`, `MoneyError`, `ConversionError`. Lower-level errors are translated at the boundary with `adaptError` (see `Payments.deposit` mapping `MoneyError` → `PaymentError`), and HTTP status mapping happens in the routes' `handleXError` recover blocks.

### State and effects

- `BlockingInMemoryBankPersistence` is a type alias for `AtomicMap[IO, AccountId, Option[Account]]`. Mutations go through `.apply(key).evalModifyValueIfSet { ... }`, which returns `Option` — `None` means "no such account" and is turned into the domain's not-found error. This gives per-key atomicity; don't bypass it with get-then-set.
- `InMemoryCache` is a `MapRef` plus a `Supervisor` that schedules TTL eviction as a background fiber.
- `FrankfurterApi` calls `https://api.frankfurter.dev/v2/rate/{base}/{quote}` with exponential backoff (3 retries, 100ms doubling) and collapses failure to `IO.none`; `CurrencyConverter` tries the cache first, falls back to the API, caches for 1 hour, and raises `ConversionError.RateUnavailable` if both miss. Running the app therefore hits the network.

### HTTP layer (`modules/server`)

- Routes are `object`s with `routes(algebra): HttpRoutes[IO]`, combined in `Main` with `<+>` (SemigroupK) and served via `.orNotFound`.
- `codecs/JsonCodecs` is hand-rolled: explicit `given` codecs for every opaque/refined type, string codecs for enums, and tagged-union codecs (`"type"` discriminator) for `AccountStatus` and `Transaction`. Adding a domain type means adding its codec here; `deriveEncoder`/`deriveDecoder` only works once the leaf `given`s exist. Import with `import co.edu.uco.xebia.bank.codecs.JsonCodecs.given`.
- Endpoints: `GET/POST /account`, `POST /account/{id}/freeze|close`, `POST /deposit|withdraw|transfer`.

## Known rough edges

`Main.runServer` and `Main.run` both construct an unused `DummyAccountImpl` (a stub `Accounts` returning canned data, used during the talk). `runServer` uses `.allocated` and drops the finalizer, so the server is never cleanly shut down. Don't treat these as intentional patterns.

## Style

`.scalafmt.conf`: scala3 dialect, `maxColumn = 120`, sorted imports, redundant braces removed. Existing code uses significant-indentation Scala 3 syntax (`enum`, `given`, brace-less `object`/`trait` bodies) in `domain`; parts of `server` use braces. Match the file you're editing, and run `sbt scalafmtAll` before finishing.
