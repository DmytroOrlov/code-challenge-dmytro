# Run
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
