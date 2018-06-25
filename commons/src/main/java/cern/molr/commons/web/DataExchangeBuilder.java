package cern.molr.commons.web;

import cern.molr.commons.api.type.trye.Failure;
import cern.molr.commons.api.type.trye.Success;
import cern.molr.commons.api.type.trye.Try;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.function.Function;

/**
 * A data exchange builder between the client and the server. It builds a publisher of string messages which is
 * generated from one element received from a Flux of strings
 *
 * @author yassine-kr
 */
public class DataExchangeBuilder<Input, Output> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataExchangeBuilder.class);

    /**
     * The mapper used for serializing output and deserializing input
     */
    private ObjectMapper mapper;
    private Class<Input> inputType;
    private Flux<String> preInput;
    private ThrowingFunction<Input, Publisher<Output>> generator;
    private Function<Throwable, Output> generatorExceptionHandler;

    public DataExchangeBuilder(Class<Input> inputType) {
        this.inputType = inputType;

        mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /**
     * Set the received Flux of messages
     *
     * @param preInput received flux of messages
     *
     * @return this builder to chain other methods
     */
    public DataExchangeBuilder<Input, Output> setPreInput(Flux<String> preInput) {
        this.preInput = preInput;
        return this;
    }

    /**
     * Set the generator which generates the output flux from the received input data
     *
     * @param generator
     *
     * @return this builder to chain other methods
     */
    public DataExchangeBuilder<Input, Output> setGenerator(ThrowingFunction<Input, Publisher<Output>> generator) {
        this.generator = generator;
        return this;
    }

    /**
     * Set the handler of the exception thrown by the generator
     *
     * @param function the returned output will be serialized and wrapped in a publisher of one element
     *
     * @return this builder to chain other methods
     */
    public DataExchangeBuilder<Input, Output> setGeneratorExceptionHandler(Function<Throwable, Output> function) {
        this.generatorExceptionHandler = function;
        return this;
    }

    /**
     * builds the publisher
     *
     * @return the publisher
     */
    public Flux<String> build() {
        return preInput.take(1).<Try<Input>>map((data) -> {
            try {
                return new Success<>(mapper.readValue(data, inputType));
            } catch (IOException error) {
                return new Failure<>(error);
            }
        }).concatMap((tryInput) -> tryInput.match((error) -> {
            LOGGER.error("unable to deserialize the input data", error);
            return Mono.empty();
        }, (input) -> {
            try {
                return Flux.from(generator.apply(input)).map((output -> {
                    try {
                        return mapper.writeValueAsString(output);
                    } catch (JsonProcessingException error) {
                        return null;
                    }
                }));
            } catch (Exception error) {
                try {
                    LOGGER.error("exception in getting the result stream", error);
                    return Mono.just(mapper.writeValueAsString(generatorExceptionHandler.apply(error)));
                } catch (JsonProcessingException error1) {
                    LOGGER.error("unable to serialize an output data", error1);
                    return Mono.empty();
                }
            }
        }));
    }

    /**
     * A function which can throw an exception
     *
     * @param <T> parameter type
     * @param <R> return type
     */
    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }


}