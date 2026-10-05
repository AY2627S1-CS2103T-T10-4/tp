package seedu.address.ui;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;

/**
 * Utilities for running UI test actions safely on the JavaFX application thread.
 */
final class JavaFxTestUtils {

    private static final int TIMEOUT_SECONDS = 10;
    private static final AtomicBoolean IS_TOOLKIT_STARTED = new AtomicBoolean();

    private JavaFxTestUtils() {}

    static void startJavaFxToolkit() {
        if (!IS_TOOLKIT_STARTED.compareAndSet(false, true)) {
            return;
        }

        CountDownLatch startupLatch = new CountDownLatch(1);
        try {
            Platform.startup(startupLatch::countDown);
        } catch (IllegalStateException e) {
            startupLatch.countDown();
        }
        await(startupLatch);
    }

    static void runOnFxThread(ThrowingRunnable action) {
        callOnFxThread(() -> {
            action.run();
            return null;
        });
    }

    static <T> T callOnFxThread(Callable<T> action) {
        if (Platform.isFxApplicationThread()) {
            return call(action);
        }

        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        try {
            return task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for the JavaFX application thread", e);
        } catch (ExecutionException e) {
            return rethrow(e.getCause());
        } catch (java.util.concurrent.TimeoutException e) {
            throw new AssertionError("Timed out while waiting for the JavaFX application thread", e);
        }
    }

    private static <T> T call(Callable<T> action) {
        try {
            return action.call();
        } catch (Exception e) {
            throw new AssertionError("JavaFX test action failed", e);
        }
    }

    private static <T> T rethrow(Throwable cause) {
        if (cause instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (cause instanceof Error error) {
            throw error;
        }
        throw new AssertionError("JavaFX test action failed", cause);
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out while starting the JavaFX toolkit");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while starting the JavaFX toolkit", e);
        }
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }
}
