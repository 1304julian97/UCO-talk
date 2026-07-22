# UCO-talk

An algebraic design for a bank domain model in Scala 3 and Cats Effect

## Modules

- `modules/domain` - bounded contexts (starting with `accounts`): rich domain models + behavior traits
- `modules/server` - runnable entry point (`Main`)

## Development

```sh
sbt validate    # scalafmt + compile + test
sbt server/run  # run the app
```

Requires JDK 21
