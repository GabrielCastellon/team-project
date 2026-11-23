package view;

import interface_adapter.discovery.TaskRunner;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

import javax.swing.SwingWorker;

public final class SwingTaskRunner<T> implements TaskRunner<T> {
    @Override
    public void run(Callable<T> task, BiConsumer<T, Throwable> onComplete) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                T result = null;
                Throwable failure = null;
                try {
                    result = get();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    failure = ex;
                } catch (ExecutionException ex) {
                    failure = ex.getCause();
                }
                onComplete.accept(result, failure);
            }
        }.execute();
    }
}
