package cc.colorcat.vangogh;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Created by cxx on 2017/7/10.
 * xx.ch@outlook.com
 */
class Dispatcher {
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final ExecutorService executor;
    private final Deque<Task> tasks = new LinkedList<>();
    private final Deque<RealCall> waiting = new LinkedList<>();
    private final Set<RealCall> running = new HashSet<>();

    private final VanGogh vanGogh;
    private volatile boolean pause = false;

    Dispatcher(VanGogh vanGogh, ExecutorService executor) {
        this.vanGogh = vanGogh;
        this.executor = executor;
    }

    void pause() {
        pause = true;
    }

    void resume() {
        pause = false;
        synchronized (waiting) {
            promoteTask();
        }
    }

    void clear() {
        Utils.checkMain();
        synchronized (waiting) {
            waiting.clear();
            tasks.clear();
        }
    }

    void enqueue(Task task) {
        Utils.checkMain();
        if (!tasks.contains(task) && tasks.offer(task)) {
            task.onPreExecute();
            RealCall call = new RealCall(vanGogh, task);
            synchronized (waiting) {
                if (!waiting.contains(call) && waiting.offer(call)) {
                    promoteTask();
                }
            }
        }
    }

    private void promoteTask() {
        RealCall call;
        while (!pause && running.size() < vanGogh.maxRunning() && (call = waiting.pollLast()) != null) {
            if (running.add(call)) {
                executor.submit(new AsyncCall(call));
            }
        }
//        LogUtils.i("Dispatcher", "waiting tasks = " + tasks.size()
//                + "\n waiting calls = " + waiting.size()
//                + "\n running calls = " + running.size());
    }

    private void completeCall(final RealCall call, final Result result, final Exception cause) {
        if ((result != null) == (cause != null)) {
            throw new IllegalStateException("dispatcher reporting error.");
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                String stableKey = call.task().stableKey();
                Iterator<Task> iterator = tasks.descendingIterator();
                while (iterator.hasNext()) {
                    Task task = iterator.next();
                    if (stableKey.equals(task.stableKey())) {
                        task.onPostResult(result, cause);
                        iterator.remove();
                    }
                }
            }
        });
    }

    private class AsyncCall implements Runnable {
        private RealCall call;

        private AsyncCall(RealCall call) {
            this.call = call;
        }

        @Override
        public void run() {
            Result result = null;
            Exception cause = null;
            try {
                result = call.execute();
            } catch (IOException e) {
                LogUtils.e(e);
                cause = e;
            } catch (IndexOutOfBoundsException e) {
                LogUtils.e(e);
                cause = new UnsupportedOperationException("unsupported uri: " + call.task().uri());
            } finally {
                synchronized (waiting) {
                    running.remove(call);
                    if (result != null || call.getAndIncrement() >= vanGogh.retryCount()) {
                        completeCall(call, result, cause);
                    } else if (!waiting.contains(call)) {
                        waiting.offer(call);
                    }
                    promoteTask();
                }
            }
        }
    }
}
