## Run unit- and integration tests (require Docker)
```sh
$ sbt test
[info] DockerTest:
[info] DummyTest:
[info] DiscountsFailTest:
...
[info] Tests: succeeded 13, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 41 s
```

## Run
```sh
$ docker-compose up -d && \
    sbt run
```
## Try in comman line
```sh
code-challenge-dmytro$ curl localhost:8080/total --data-binary @lineitems.csv
{"total":1887.08}
```
## Open Swagger UI
[http://localhost:8080/docs](http://localhost:8080/docs)

## Developing in Sbt
```sh
$ sbt
> ~reStart
```
