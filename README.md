[![Maintainability](https://api.codeclimate.com/v1/badges/d102d5cdb2b5a641fa6d/maintainability)](https://codeclimate.com/github/alexbalonperin/zio_experiments/maintainability)

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
