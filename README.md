# Run
```sh
$ sbt run
```
## Try in comman line
```sh
$ curl localhost:8080/total --data-binary @$(pwd)/lineitems.csv
{"total":1887.08}
```
## Open Swagger UI
[http://localhost:8080/docs](http://localhost:8080/docs)

## Developing in Sbt
```sh
$ sbt
> ~reStart
```
