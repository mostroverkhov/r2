package com.github.mostroverkhov.r2.example;

import com.github.mostroverkhov.r2.core.contract.RequestChannel;
import com.github.mostroverkhov.r2.core.contract.RequestResponse;
import com.github.mostroverkhov.r2.core.contract.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Contract {

  public static class ControlUnit {

    @Service("controlUnit")
    interface Svc {

      @RequestResponse("power")
      Mono<Response> power();
    }

    public static class Response {
      float availablePower;

      public Response(float availablePower) {
        this.availablePower = availablePower;
      }

      public Response() {
      }

      public float getAvailablePower() {
        return availablePower;
      }

      public void setAvailablePower(float availablePower) {
        this.availablePower = availablePower;
      }

      @Override
      public String toString() {
        return "Response{" +
            "availablePower=" + availablePower +
            '}';
      }
    }
  }

  public static class AssemblyLines {

    @Service("assemblyLines")
    public interface Svc {

      @RequestChannel("control")
      Flux<Response> control(Flux<Request> requests);
    }

    public static class Request {
      private int activeAssemblies;

      public Request(int activeAssemblies) {
        this.activeAssemblies = activeAssemblies;
      }

      public Request() {
      }

      public int getActiveAssemblies() {
        return activeAssemblies;
      }

      public void setActiveAssemblies(int activeAssemblies) {
        this.activeAssemblies = activeAssemblies;
      }

      @Override
      public String toString() {
        return "Request{" +
            "activeAssemblies=" + activeAssemblies +
            '}';
      }
    }

    public static class Response {
      private float temperature;
      private float humidity;
      private float particles;

      public Response(float temperature, float humidity, float particles) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.particles = particles;
      }

      public Response() {
      }

      public float getTemperature() {
        return temperature;
      }

      public void setTemperature(float temperature) {
        this.temperature = temperature;
      }

      public float getHumidity() {
        return humidity;
      }

      public void setHumidity(float humidity) {
        this.humidity = humidity;
      }

      public float getParticles() {
        return particles;
      }

      public void setParticles(float particles) {
        this.particles = particles;
      }

      @Override
      public String toString() {
        return "Assembly Line{" +
            "temperature=" + temperature +
            ", humidity=" + humidity +
            ", particles=" + particles +
            '}';
      }
    }
  }
}
