#Web Crawler

RESTful microservice, powered by Spring Boot, one-button bootstrap

##To run the program:

- Clone this repository

- `./gradlew bootRun`

##Available options:

`-Dserver.port` - port for this application

`-Dcrawler.depth` - depth for this crawler
Amount of nested links for crawler to visit

 `-Xss[g|G|m|M|k|K]` - stack size 
For big numbers of `crawler.depth` (> 1000) 
you might consider increasing stack size
due to recursive implementation

`-Dcrawler.asset_types` - list of assets
`[png,jpf,js,css]`