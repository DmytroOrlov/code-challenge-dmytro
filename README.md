# code-challenge

Hi

## The Task
A user went on a shopping spree at eyeem. His purchases are collected in a csv that is attached as `lineitems.csv` 
and it is time to calculate the total amount of money he has to pay.

The csv structure is

|photo_id|price|discount_code|
|-|-|-|
|1|1.50|XMAS_SPECIAL|
|2|1.25||

A discount code must be applied to the price.
The structure of a discount code is

````json
{"name":"SMART_XTREME","discount":10}
````

where discount is number between 0 and 100.
Given the price of a photo is 10.00 and the discount is 10, the actual price of the photo is 9.00

The discount codes can be obtained by talking to a microservice.

```
docker-compose up -d
curl localhost:9000/api/discounts/SMART_XTREME
curl localhost:9000/api/discounts/{name}
```

The endpoint might return

 - 200 - OK with the specified json structure
 - 404 - NOT FOUND if no such discount code exists
 - 429 - Too Many Requests
 - 500 - Life is hard

Please be aware, that this service is legacy and might overload or looses contact to its database. Please program accordingly.

## Your assignment

Create a program that takes in the CSV and outputs the final amount the user has to pay.
You are free to choose any libraries and frameworks you need. The only restriction is
 - it needs to be built in scala
 - it should be runnable easily on our developer machines (java,sbt,docker)
 
  
    sbt run ....
    docker run ...
    java -jar ...

You are free to design the program interface and the output format. 
Please provide an instruction, how to run your program.

Have fun
