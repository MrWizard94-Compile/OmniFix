package org.omnifix.client;

import net.minecraftforge.fml.loading.ImmediateWindowHandler;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * Keeps the early Forge splash/window rendering while long register events run on the main thread
 * by moving the GL context to a helper thread for {@link ImmediateWindowHandler#renderTick()}.
 */
public final class AsyncLoadingScreen extends Thread implements AutoCloseable {

    private static int splashThreadNum = 1;
    private static GLCapabilities caps;

    private final long theWindow;
    private final AtomicBoolean keepRunning = new AtomicBoolean(true);

    public AsyncLoadingScreen() {
        this.setName("OmniFix splash thread " + splashThreadNum++);
        this.theWindow = GLFW.glfwGetCurrentContext();
        if (caps == null) {
            caps = GL.createCapabilities();
        }
        if (this.theWindow == 0) {
            throw new IllegalStateException("No GLFW context found for async loading screen");
        }
        this.start();
    }

    @Override
    public synchronized void start() {
        GLFW.glfwMakeContextCurrent(0);
        super.start();
    }

    @Override
    public void run() {
        GLFW.glfwMakeContextCurrent(theWindow);
        GL.setCapabilities(caps);
        while (keepRunning.get()) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(50));
            ImmediateWindowHandler.renderTick();
        }
        GLFW.glfwMakeContextCurrent(0);
    }

    @Override
    public void close() {
        keepRunning.set(false);
        try {
            this.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        GLFW.glfwMakeContextCurrent(theWindow);
    }
}
