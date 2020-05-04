package users.telemetry

import io.jaegertracing.Configuration
import io.jaegertracing.internal.JaegerTracer
import io.jaegertracing.internal.samplers.ConstSampler
import io.jaegertracing.zipkin.ZipkinV2Reporter
import io.opentracing.mock.MockTracer
import org.apache.http.client.utils.URIBuilder
import zipkin2.reporter.AsyncReporter
import zipkin2.reporter.okhttp3.OkHttpSender

object Tracer {

  def jaeger(
      host: String,
      serviceName: String
  ): JaegerTracer = {
    val url =
      new URIBuilder().setScheme("http").setHost(host).setPath("/api/v2/spans").build.toString
    val senderBuilder = OkHttpSender.newBuilder.compressionEnabled(true).endpoint(url)

    new Configuration(serviceName).getTracerBuilder
      .withSampler(new ConstSampler(true))
      .withReporter(new ZipkinV2Reporter(AsyncReporter.create(senderBuilder.build)))
      .build
  }

  val mock = new MockTracer
}
