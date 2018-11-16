package cern.molr.commons.web;

import cern.molr.commons.api.response.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.function.Function;

/**
 * A data processor builder. It builds a {@link Flux} of {@link String} messages. This stream takes one element from an
 * input {@link Flux}, it deserializes it, generates a {@link Publisher} from it, then each published element is
 * serialized to a {@link String} object
 *
 * @author yassine-kr
 */
public class DataProcessorBuilder<Input, Output> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProcessorBuilder.class);

    /**
     * The mapper used for serializing output and deserializing input
     */
    private ObjectMapper mapper;
    private Class<Input> inputType;
    private Flux<String> preInput;
    private ThrowingFunction<Input, Publisher<Output>> generator;
    private Function<Throwable, Output> generatorExceptionHandler;

    public DataProcessorBuilder(Class<Input> inputType) {
        this.inputType = inputType;

        mapper = SerializationUtils.getMapper();
    }

    /**
     * Set the preInput Flux of messages
     *
     * @param preInput preInput flux of messages
     *
     * @return this builder to chain other methods
     */
    public DataProcessorBuilder<Input, Output> setPreInput(Flux<String> preInput) {
        this.preInput = preInput;
        return this;
    }

    /**
     * Set the handler of the exception thrown by the generator
     *
     * @param function the returned output will be serialized and wrapped in a publisher of one element
     *
     * @return this builder to chain other methods
     */
    public DataProcessorBuilder<Input, Output> setGeneratorExceptionHandler(Function<Throwable, Output> function) {
        this.generatorExceptionHandler = function;
        return this;
    }

    /**
     * Builds the stream
     *
     * @return the built stream
     */
    public Flux<String> build() {
        return preInput.take(1)
                .map(getDeserializer())
                .concatMap((tryInput) -> tryInput.match(getDeserializationErrorHandler(),
                        getGenerator().andThen((tryFlux) -> tryFlux.match(getGenerationErrorHandler(),
                                (flux) -> flux.map(getSerializer()))
                        )));
    }

    /**
     * Returns the function which tries to deserialize the string input
     *
     * @return the deserializer function
     */
    private Function<String, Response<Input>> getDeserializer() {
        return data -> {
            try {
                return new Response<>(mapper.readValue(data, inputType));
            } catch (IOException error) {
                LOGGER.error("unable to deserialize the input data [{}]", data, error);
                return new Response<>(error);
            }
        };
    }

    /**
     * Returns the handler which is called when there is a deserialization error
     *
     * @return the function which returns a {@link String} publisher
     */
    private Function<Throwable, Publisher<String>> getDeserializationErrorHandler() {
        return error -> Mono.empty();
    }

    /**
     * Returns the generator which generates the output {@link Flux} from the input
     *
     * @return the function which returns the output {@link Flux}
     */
    private Function<Input, Response<Flux<Output>>> getGenerator() {
        return input -> {
            try {
                return new Response<>(Flux.from(generator.apply(input)));
            } catch (Exception error) {
                return new Response<>(error);
            }
        };
    }

    /**
     * Set the generator which generates the output {@link Publisher} from the received input data
     *
     * @return this builder to chain other methods
     */
    public DataProcessorBuilder<Input, Output> setGenerator(ThrowingFunction<Input, Publisher<Output>> generator) {
        this.generator = generator;
        return this;
    }

    /**
     * Returns the handler called when there is a problem in the output {@link Publisher} generation
     *
     * @return the function which handles the error
     */
    private Function<Throwable, Publisher<String>> getGenerationErrorHandler() {
        return error -> {
            try {
                LOGGER.error("exception in generating the result stream", error);
                if (generatorExceptionHandler == null) {
                    return Mono.empty();
                }
                return Mono.just(mapper.writeValueAsString(generatorExceptionHandler.apply(error)));
            } catch (JsonProcessingException error1) {
                LOGGER.error("unable to serialize an output data [{}]", generatorExceptionHandler.apply(error), error1);
                return Mono.empty();
            }
        };
    }

    /**
     * Returns the serializer which transforms the output to a {@link String}
     *
     * @return the function which serializes the output
     */
    private Function<Output, String> getSerializer() {
        return output -> {
            try {
                return mapper.writeValueAsString(output);
            } catch (JsonProcessingException error) {
                LOGGER.error("unable to serialize an output data [{}]", output, error);
                return null;
            }
        };
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