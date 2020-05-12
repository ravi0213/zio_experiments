package io.abp.users.telemetry

import io.abp.users.config.TelemetryConfig
import io.abp.users.config.TelemetryConfig.TracerConfig
import io.jaegertracing.Configuration
import io.jaegertracing.internal.JaegerTracer
import io.jaegertracing.internal.samplers.ConstSampler
import io.jaegertracing.zipkin.ZipkinV2Reporter
import io.opentracing.mock.MockTracer
import org.apache.http.client.utils.URIBuilder
import zio._
import zio.clock._
import zio.telemetry.opentracing.OpenTracing
import zipkin2.reporter.AsyncReporter
import zipkin2.reporter.okhttp3.OkHttpSender

object Tracer {

  def apply(config: TelemetryConfig): ULayer[OpenTracing] = {
    val tracer = config.tracerConfig match {
      case TracerConfig.Mock                            => Tracer.mock
      case TracerConfig.JaegerConfig(host, serviceName) => Tracer.jaeger(host, serviceName)
    }
    (Clock.live >>> OpenTracing.live(tracer))
  }

  private def jaeger(
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

  private val mock = new MockTracer
}
