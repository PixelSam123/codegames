package io.github.pixelsam123.problems.submission.runner;

import com.caoccao.javet.exceptions.JavetCompilationException;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.exceptions.JavetExecutionException;
import com.caoccao.javet.exceptions.JavetTerminatedException;
import com.caoccao.javet.interception.logging.BaseJavetConsoleInterceptor;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.engine.IJavetEngine;
import com.caoccao.javet.interop.engine.IJavetEnginePool;
import com.caoccao.javet.values.V8Value;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;

@ApplicationScoped
public class JavetSubmissionRunner implements SubmissionRunner {

    private static final long runTimeoutDuration = 5L;

    private final IJavetEnginePool<V8Runtime> javetEnginePool;

    public JavetSubmissionRunner(IJavetEnginePool<V8Runtime> javetEnginePool) {
        this.javetEnginePool = javetEnginePool;
    }

    @Override
    public Uni<SubmissionRunResult> run(String jsCode) {
        return Uni.createFrom().item(() -> {
            StringBuilder stdout = new StringBuilder();

            try (
                IJavetEngine<V8Runtime> javetEngine = javetEnginePool.getEngine();
                V8Runtime runtime = javetEngine.getV8Runtime()
            ) {
                runtime.resetContext();

                BaseJavetConsoleInterceptor consoleInterceptor = new BaseJavetConsoleInterceptor(
                    runtime
                ) {
                    @Override
                    public void consoleDebug(V8Value... v8Values) {
                        for (V8Value value : v8Values) {
                            stdout.append(value.toString()).append('\n');
                        }
                    }

                    @Override
                    public void consoleError(V8Value... v8Values) {
                        for (V8Value value : v8Values) {
                            stdout.append(value.toString()).append('\n');
                        }
                    }

                    @Override
                    public void consoleInfo(V8Value... v8Values) {
                        for (V8Value value : v8Values) {
                            stdout.append(value.toString()).append('\n');
                        }
                    }

                    @Override
                    public void consoleLog(V8Value... v8Values) {
                        for (V8Value value : v8Values) {
                            stdout.append(value.toString()).append('\n');
                        }
                    }

                    @Override
                    public void consoleTrace(V8Value... v8Values) {
                        for (V8Value value : v8Values) {
                            stdout.append(value.toString()).append('\n');
                        }
                    }

                    @Override
                    public void consoleWarn(V8Value... v8Values) {
                        for (V8Value value : v8Values) {
                            stdout.append(value.toString()).append('\n');
                        }
                    }
                };

                Uni
                    .createFrom()
                    .voidItem()
                    .onItem()
                    .delayIt()
                    .by(Duration.ofSeconds(runTimeoutDuration))
                    .subscribe()
                    .with(unused -> runtime.terminateExecution());

                consoleInterceptor.register(runtime.getGlobalObject());

                try {
                    runtime.getExecutor(jsCode).executeVoid();
                } catch (JavetCompilationException err) {
                    stdout.append(err.getScriptingError().toString()).append('\n');
                    return new SubmissionRunResult(
                        SubmissionRunStatus.COMPILE_ERROR, stdout.toString()
                    );
                } catch (JavetExecutionException err) {
                    stdout.append(err.getScriptingError().toString()).append('\n');
                    return new SubmissionRunResult(
                        SubmissionRunStatus.RUNTIME_ERROR, stdout.toString()
                    );
                } catch (JavetTerminatedException err) {
                    stdout.append("Timed out. Details: ").append(err.getMessage()).append('\n');
                    return new SubmissionRunResult(
                        SubmissionRunStatus.RUNTIME_ERROR, stdout.toString()
                    );
                } finally {
                    consoleInterceptor.unregister(runtime.getGlobalObject());
                }

                return new SubmissionRunResult(SubmissionRunStatus.ACCEPTED, stdout.toString());
            } catch (JavetException err) {
                return new SubmissionRunResult(SubmissionRunStatus.RUNTIME_ERROR, err.toString());
            }
        });
    }

}
