[![Maintainability](https://api.codeclimate.com/v1/badges/d102d5cdb2b5a641fa6d/maintainability)](https://codeclimate.com/github/alexbalonperin/zio_experiments/maintainability)
[![Build Status](https://travis-ci.org/alexbalonperin/zio_experiments.svg?branch=master)](https://travis-ci.org/alexbalonperin/zio_experiments)
[![codecov](https://codecov.io/gh/alexbalonperin/zio_experiments/branch/master/graph/badge.svg)](https://codecov.io/gh/alexbalonperin/zio_experiments)
[![Mergify Status][mergify-status]][mergify]
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

[mergify]: https://mergify.io
[mergify-status]: https://img.shields.io/endpoint.svg?url=https://gh.mergify.io/badges/alexbalonperin/zio_experiments&style=flat


# ZIO Experiments

Trying out ZIO

## Local development

To use telemetry locally, start [Jaeger][jaeger] by running following command:
```bash
docker run -d --name jaeger \
  -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
  -p 5775:5775/udp \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 14268:14268 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.6
```

To check if it's running properly visit [Jaeger UI](http://localhost:16686/).
More info can be found [here][jaeger-docker].
