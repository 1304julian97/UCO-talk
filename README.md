# UCO-talk

- A minimal internal bank DSL in Scala 3

## Modules

- `modules/core` - the DSL: semantic model (`bank.models`), fluent syntax (`bank.dsl`), interpreter (`bank.interpreter`)
- `modules/server` - a runnable `Main` demo.

## Development

```sh
sbt compile           # build all modules
sbt test              # run tests
sbt scalafmtAll       # format sources
sbt scalafmtCheckAll  # verify formatting (as CI does)
sbt server/run        # run the server module
```

Requires JDK 21 and sbt
