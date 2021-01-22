/*
 * Copyright 2019 DLSC Software & Consulting GmbH.
 */
package com.dlsc.gemsfx.util;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Worker;
import retrofit2.Response;

/**
 * A utility class used to invoke backend services via Retrofit on a separate thread and to
 * handle the response on the UI thread. This class makes it extremely easy to do this and
 * using it results in very compact and nicely structured code (also thanks to a fluent API).
 * <p>
 * <h3>Example 1:</h3>
 * In this example we assume that "myService.loadData()" is a Retrofit call. Executing it returns
 * a Retrofit "Response" object. Hence, this lambda implements the "ServiceSupplier" interface
 * required by the service invocation.
 * <pre>
 *         ServiceInvocation.create("Load data ...", () -> myService.loadData().execute())
 *              .onSuccess(data -> listView.getItems().setAll(data)).execute();
 *     </pre>
 * <p>
 * One can see that in this fluent API the last call goes to the service invocation's
 * execute() method. However, it is advised to execute service invocations in a central place
 * within the application's user interface codebase. This makes it easier to register default
 * error handlers and to provide feedback to the user regarding the status of the execution of
 * a service invocation. Example 2 shows how this is done.
 * </p>
 * <h3>Example 2:</h3>
 * <pre>
 *         ui.execute(ServiceInvocation.create("Load data ...", () -> myService.loadData().execute())
 *              .onSuccess(data -> listView.getItems().setAll(data)));
 *     </pre>
 * </p>
 * Various consumers can be registered with a service invocation before it gets executed. Some
 * of these consumers have a second version with the postfix "default" to their name, e.g. "onFailure" and
 * "onFailureDefault". The idea behind this is that an application might provide a central place
 * where service invocations are being executed (e.g. Window.execute(si) or Workbench.execute(si))
 * and wants to set "default" handlers in that location. At the same time each specific occurrence
 * of a service invocation might require its own / more specific handlers.
 *
 * @param <T> the type of the result object wrapped inside the retrofit response
 */
public final class ServiceInvocation<T> implements Worker<T> {

    private static final Logger LOG = Logger.getLogger(ServiceInvocation.class.getName());
    
    private static final Executor EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r);
        thread.setName("Service Invocation Thread");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    });

    private final String name;
    private final ServiceSupplier<T> service;

    private Consumer<String> onStart;
    private Consumer<T> onSuccess;
    private Consumer<Response<T>> onSuccessDetailed;

    private Runnable onFinally;

    private BiConsumer<String, String> onFailure;
    private BiConsumer<String, Response> onFailureDetailed;
    private BiConsumer<String, Exception> onException;

    private BiConsumer<String, String> onFailureDefault;
    private BiConsumer<String, Response> onFailureDetailedDefault;
    private BiConsumer<String, Exception> onExceptionDefault;

    private Map<HttpStatusCode, BiConsumer<String, String>> onStatusCode;
    private Map<HttpStatusCode, BiConsumer<String, String>> onStatusCodeDefault;

    private boolean simulatingFailure;

    private long delay;

    private ServiceInvocation(String name, ServiceSupplier<T> service) {
        this.name = Objects.requireNonNull(name, "service invocation name can not be null");
        this.service = Objects.requireNonNull(service, "service can not be null");

        title.set(name);
    }

    /**
     * Creates a new service invocation instance.
     *
     * @param name     the name of this invocation
     * @param supplier the supplier returning the service for invocation
     * @param <T>      the type of the response object
     * @return a service invocation
     */
    public static <T> ServiceInvocation<T> create(String name, ServiceSupplier<T> supplier) {
        return new ServiceInvocation(name, supplier);
    }

    private BiConsumer<String, String> getOnFailure() {
        return onFailure != null ? onFailure : onFailureDefault;
    }

    private BiConsumer<String, Response> getOnFailureDetailed() {
        return onFailureDetailed != null ? onFailureDetailed : onFailureDetailedDefault;
    }

    private BiConsumer<String, Exception> getOnException() {
        return onException != null ? onException : onExceptionDefault;
    }

    private BiConsumer<String, String> getOnStatusCode(HttpStatusCode code) {
        BiConsumer<String, String> handler = null;

        if (onStatusCode != null) {
            handler = onStatusCode.get(code);
        }

        if (handler == null && onStatusCodeDefault != null) {
            handler = onStatusCodeDefault.get(code);
        }

        return handler;
    }

    /**
     * Executes the service invocation with the default executor.
     *
     * @return a completable future object usable for chaining
     */
    public CompletableFuture<Void> execute() {
        return execute(EXECUTOR);
    }

    /**
     * Executes the service invocation with the given executor.
     *
     * @return a completable future object usable for chaining
     */
    public CompletableFuture<Void> execute(Executor executor) {
        Objects.requireNonNull(executor, "executor can not be null");

        CompletableFuture<Void> result = new CompletableFuture<>();

        state.set(State.RUNNING);
        totalWork.set(1);
        running.set(true);
        progress.set(0);

        executor.execute(() -> {

            if (onStart != null) {
                Platform.runLater(() -> onStart.accept(getName()));
            }


            LOG.fine("executing service invocation with name: " + getName());
            try {

                if (delay > 0) {
                    delay();
                }

                Instant startTime = Instant.now();

                Platform.runLater(() -> message.set("Calling service"));

                Response<T> response = service.get();

                if (LOG.isLoggable(Level.FINE)) {
                    Duration duration = Duration.between(startTime, Instant.now());
                    LOG.fine("server side call duration: " + duration.toMillis() + "ms");
                }

                if (response.isSuccessful() && !isSimulatingFailure()) {
                    success(response);
                } else {
                    failure(response);
                }

                result.complete(null);

            } catch (Exception t) {
                exception(result, t);
            } finally {
                doFinally();
            }
        });

        return result;
    }

    private void delay() throws InterruptedException {
        LOG.finer("delaying service call, millis = " + delay);
        Thread.sleep(delay);
    }

    private void success(Response<T> response) throws ExecutionException, InterruptedException {
        Platform.runLater(() -> {
            message.set("Call was successful");
            state.set(State.SUCCEEDED);
        });

        if (onSuccess != null) {
            LOG.finer("invoking onSuccess handler");
            runAndWait(() -> onSuccess.accept(response.body()));
        } else if (onSuccessDetailed != null) {
            LOG.finer("invoking onSuccessDetailed handler");
            runAndWait(() -> onSuccessDetailed.accept(response));
        }
    }

    private void failure(Response<T> response) throws IOException, ExecutionException, InterruptedException {
        Platform.runLater(() -> {
            message.set("Call was not successful");
            state.set(State.FAILED);
        });

        String errorMessage = simulatingFailure ? "Simulated failure" : (response.errorBody() != null ? response.errorBody().string() : "");

        LOG.log(Level.SEVERE, "service call was not successful: " + errorMessage);

        if (onStatusCode != null || onStatusCodeDefault != null) {

            BiConsumer<String, String> statusCodeConsumer = getOnStatusCode(HttpStatusCode.fromStatusCode(response.code()));

            if (statusCodeConsumer != null) {

                LOG.finer("invoking onStatusCode handler for status code " + response.code());
                Platform.runLater(() -> statusCodeConsumer.accept(name, errorMessage));
            }
        }

        BiConsumer<String, String> onFailureHandler = getOnFailure();

        if (onFailureHandler != null) {
            runAndWait(() -> {
                LOG.finer("invoking onFailure handler");
                onFailureHandler.accept(name, errorMessage);
            });
        } else {

            BiConsumer<String, Response> onFailureDetailedHandler = getOnFailureDetailed();

            if (onFailureDetailedHandler != null) {
                LOG.finer("invoking onFailureDetailed handler");
                runAndWait(() -> onFailureDetailed.accept(name, response));
            }
        }
    }

    private void exception(CompletableFuture<Void> result, Exception t) {
        LOG.log(Level.SEVERE, "error when trying to invoke the service: " + getName(), t);

        Platform.runLater(() -> {
            state.set(State.FAILED);
            exception.set(t);
            message.set("Server-side error");
        });

        BiConsumer<String, Exception> onExceptionHandler = getOnException();

        if (onExceptionHandler != null) {
            try {
                LOG.finer("invoking onException handler");
                runAndWait(() -> onExceptionHandler.accept(name, t));
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "error when trying to propagate error message from service invocation: " + getName(), e);
            }
        }

        result.completeExceptionally(t);
    }

    private void doFinally() {
        Platform.runLater(() -> {
            running.set(false);
            progress.set(1);
        });

        if (onFinally != null) {
            try {
                LOG.finer("invoking onFinally handler");
                runAndWait(onFinally);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "error when trying to execute ‘on finally' of service invocation: " + getName(), e);
            }
        }
    }

    /**
     * Returns the name of the service invocation.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Introduces an artificial delay for the invocation. The call to the backend will happen
     * after the delay time has passed.
     *
     * @param delay the delay in milliseconds
     * @return the service invocation
     */
    public ServiceInvocation<T> withDelay(long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay millis can not be negative but was " + delay);
        }
        this.delay = delay;
        return this;
    }

    /**
     * An easy way to explicitly make the service invocation fail in order to test the handling of
     * failures by the application.
     *
     * @param failure enables / disables the failure
     * @return the service invocation
     */
    public ServiceInvocation<T> withSimulatingFailure(boolean failure) {
        simulatingFailure = failure;
        return this;
    }

    /**
     * Returns true if the service invocation will intentionally fail (see also {@link #withSimulatingFailure(boolean)}).
     *
     * @return true if the invocation will fail
     */
    public boolean isSimulatingFailure() {
        return simulatingFailure;
    }

    /**
     * A consumer that will be invoked first when the {@link #execute()} method gets invoked.
     *
     * @param onStart the "on start" handler.
     * @return the service invocation
     */
    public ServiceInvocation<T> onStart(Consumer<String> onStart) {
        this.onStart = onStart;
        return this;
    }

    /**
     * A consumer that will be invoked when the backend service invocation was successful.
     * The consumer will receive the expected result object.
     *
     * @param onSuccess the "on success" handler.
     * @return the service invocation
     */
    public ServiceInvocation<T> onSuccess(Consumer<T> onSuccess) {
        this.onSuccess = onSuccess;
        return this;
    }

    /**
     * A consumer that will be invoked when the backend service invocation was successful.
     * The consumer will receive the response object wrapping the expected result object.
     *
     * @param onSuccessDetailed the "on success detailed" handler.
     * @return the service invocation
     */
    public ServiceInvocation<T> onSuccessDetailed(Consumer<Response<T>> onSuccessDetailed) {
        this.onSuccessDetailed = onSuccessDetailed;
        return this;
    }

    /**
     * A consumer that will be invoked when the backend service invocation was not successful.
     * The consumer will receive the name of the service invocation and an error message.
     *
     * @param onFailure the "on failure" handler.
     * @return the service invocation
     */
    public ServiceInvocation<T> onFailure(BiConsumer<String, String> onFailure) {
        this.onFailure = onFailure;
        return this;
    }

    /**
     * A default consumer that will be invoked when the backend service invocation was not successful.
     * The consumer will receive the name of the service invocation and an error message.
     *
     * @param onFailureDefault the "on failure default" handler.
     * @return the service invocation
     */
    public ServiceInvocation<T> onFailureDefault(BiConsumer<String, String> onFailureDefault) {
        this.onFailureDefault = onFailureDefault;
        return this;
    }

    /**
     * A consumer that will be invoked when the backend service invocation was not successful.
     * The consumer will receive the name of the service invocation and the full response object.
     *
     * @param onFailureDetailed the "on failure detailed" handler.
     * @return the service invocation
     */
    public ServiceInvocation<T> onFailureDetailed(BiConsumer<String, Response> onFailureDetailed) {
        this.onFailureDetailed = onFailureDetailed;
        return this;
    }

    /**
     * A default consumer that will be invoked when the backend service invocation was not successful.
     * The consumer will receive the name of the service invocation and the full response object.
     *
     * @param onFailureDetailedDefault the "on failure detailed default" handler.
     * @return the service invocation
     */
    public ServiceInvocation<T> onFailureDetailedDefault(BiConsumer<String, Response> onFailureDetailedDefault) {
        this.onFailureDetailedDefault = onFailureDetailedDefault;
        return this;
    }

    /**
     * A consumer that will be invoked when the backend returns the given HTTP status code. The consumer
     * receives the name of the service invocation and the status message.
     *
     * @param code the status code for which the consumer will be registered
     * @param onStatusCode the "on status code" handler.
     * @return the service invocation
     */
    public ServiceInvocation<T> onStatusCode(HttpStatusCode code, BiConsumer<String, String> onStatusCode) {
        if (this.onStatusCode == null) {
            this.onStatusCode = new HashMap<>();
        }
        this.onStatusCode.put(code, onStatusCode);
        return this;
    }

    /**
     * A default consumer that will be invoked when the backend returns the given HTTP status code. The consumer
     * receives the name of the service invocation and the status message.
     *
     * @param code the status code for which the consumer will be registered
     * @param onStatusCodeDefault the "on status code" handler.
     *
     * @return the service invocation
     */
    public ServiceInvocation<T> onStatusCodeDefault(HttpStatusCode code, BiConsumer<String, String> onStatusCodeDefault) {
        if (this.onStatusCodeDefault == null) {
            this.onStatusCodeDefault = new HashMap<>();
        }
        this.onStatusCodeDefault.put(code, onStatusCodeDefault);
        return this;
    }

    /**
     * A consumer that will be invoked in case of an exception during the execution of the
     * service invocation. The consumer will receive the name of the service invocation and
     * the exception object.
     *
     * @param onException the consumer
     * @return the service invocation
     */
    public ServiceInvocation<T> onException(BiConsumer<String, Exception> onException) {
        this.onException = onException;
        return this;
    }

    /**
     * A default consumer that will be invoked in case of an exception during the execution of the
     * service invocation. The consumer will receive the name of the service invocation and
     * the exception object.
     *
     * @param onExceptionDefault the consumer
     * @return the service invocation
     */
    public ServiceInvocation<T> onExceptionDefault(BiConsumer<String, Exception> onExceptionDefault) {
        this.onExceptionDefault = onExceptionDefault;
        return this;
    }

    /**
     * A runnable that will be invoked after the service invocation has completed. No matter if
     * an exception occurred or not.
     *
     * @param onFinally the runnable
     * @return the service invocation
     */
    public ServiceInvocation<T> onFinally(Runnable onFinally) {
        this.onFinally = onFinally;
        return this;
    }

    private void runAndWait(Runnable runnable) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                runnable.run();
                result.complete(null);
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        });

        result.get();
    }

    // state

    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>(this, "state", State.READY);

    /**
     * Returns the worker state of the service invocation (running, succeeded, ...).
     *
     * @return the state of the service invocation according to the {@link Worker} interface
     */
    @Override
    public State getState() {
        return state.get();
    }

    /**
     * Returns a read-only property for observing the state of the service invocation.
     *
     * @return the state property
     */
    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    // value

    private final ReadOnlyObjectWrapper<T> value = new ReadOnlyObjectWrapper<>(this, "value");

    @Override
    public T getValue() {
        return value.get();
    }

    @Override
    public ReadOnlyObjectProperty<T> valueProperty() {
        return value;
    }

    // exceptions

    private final ReadOnlyObjectWrapper<Throwable> exception = new ReadOnlyObjectWrapper<>(this, "exception");

    @Override
    public Throwable getException() {
        return exception.get();
    }

    @Override
    public ReadOnlyObjectProperty<Throwable> exceptionProperty() {
        return exception.getReadOnlyProperty();
    }

    // work done

    private final ReadOnlyDoubleWrapper workDone = new ReadOnlyDoubleWrapper(this, "workDone");

    @Override
    public double getWorkDone() {
        return workDone.get();
    }

    @Override
    public ReadOnlyDoubleProperty workDoneProperty() {
        return workDone.getReadOnlyProperty();
    }

    // total work

    private final ReadOnlyDoubleWrapper totalWork = new ReadOnlyDoubleWrapper(this, "totalWork");

    @Override
    public double getTotalWork() {
        return totalWork.get();
    }

    @Override
    public ReadOnlyDoubleProperty totalWorkProperty() {
        return totalWork;
    }

    // progress

    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, "progress");

    @Override
    public double getProgress() {
        return progress.get();
    }

    @Override
    public ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }

    // running

    private final ReadOnlyBooleanWrapper running = new ReadOnlyBooleanWrapper(this, "running");

    @Override
    public ReadOnlyBooleanProperty runningProperty() {
        return running.getReadOnlyProperty();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    // message

    private final ReadOnlyStringWrapper message = new ReadOnlyStringWrapper(this, "message");

    @Override
    public String getMessage() {
        return message.get();
    }

    @Override
    public ReadOnlyStringProperty messageProperty() {
        return message;
    }

    // title

    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(this, "title");

    @Override
    public String getTitle() {
        return title.get();
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return title;
    }

    // cancel

    @Override
    public boolean cancel() {
        throw new UnsupportedOperationException();
    }

    /**
     * A functional supplier interface used for providing the response object of
     * a retrofit call.
     *
     * @param <T> the result type
     */
    @FunctionalInterface
    public interface ServiceSupplier<T> {

        Response<T> get() throws Exception;
    }
}
