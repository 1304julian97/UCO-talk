# UCO-talk

A minimal bank DSL in Scala 2.13, using tagless-final algebras over an immutable domain model, with
Cats Effect and fs2.

## Modules

- `modules/domain` — domain model and DSL algebras.
- `modules/server` — runnable entry point (`Main`).

## Development

```sh
sbt compile           # build all modules
sbt test              # run tests
sbt scalafmtAll       # format sources
sbt scalafmtCheckAll  # verify formatting (as CI does)
sbt server/run        # run the server module
```

Requires JDK 21 and sbt.
