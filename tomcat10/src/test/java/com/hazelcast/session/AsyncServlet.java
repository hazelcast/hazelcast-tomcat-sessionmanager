package com.hazelcast.session;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebServlet(asyncSupported = true, value = "/AsyncServlet")
public class AsyncServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncServlet.class);
    private static final BlockingQueue<AsyncContext> queue = new ArrayBlockingQueue<>(20000);

    public AsyncServlet() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(AsyncServlet::newEvent, 0, 5, TimeUnit.SECONDS);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        queue.add(request.startAsync());
    }

    private static void newEvent() {
        var clients = new ArrayList<AsyncContext>(queue.size());
        queue.drainTo(clients);
        for (AsyncContext ac : clients) {
            try {
                ac.getResponse().getWriter().write("OK");
            } catch (IOException e) {
                LOGGER.error("Error writing response", e);
            }
            ac.complete();
        }
    }
}
