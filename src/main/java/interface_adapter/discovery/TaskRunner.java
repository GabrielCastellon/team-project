package interface_adapter.discovery;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface TaskRunner<T> {
    void run(Callable<T> task, BiConsumer<T, Throwable> onComplete);
}
