package io.github.pixelsam123.problems.submission.runner;

import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.engine.IJavetEngine;
import com.caoccao.javet.interop.engine.IJavetEnginePool;
import com.caoccao.javet.interop.engine.JavetEnginePool;
import com.caoccao.javet.values.V8Value;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JavetSubmissionRunner implements ISubmissionRunner {

    private final IJavetEnginePool<V8Runtime> javetEnginePool;

    public JavetSubmissionRunner(IJavetEnginePool<V8Runtime> javetEnginePool) {
        this.javetEnginePool = javetEnginePool;
    }

    @Override
    public Uni<SubmissionRunResult> run(String jsCode) {
        return Uni.createFrom().item(() -> {
            try (IJavetEngine<V8Runtime> javetEngine = javetEnginePool.getEngine()) {
                V8Runtime runtime = javetEngine.getV8Runtime();

                try (V8Value result = runtime.getExecutor(jsCode).execute()) {
                    return new SubmissionRunResult(0, result.toString());
                }
            } catch (JavetException err) {
                return new SubmissionRunResult(1, err.toString());
            }
        });
    }

    public static void main(String[] args) {
        new JavetSubmissionRunner(new JavetEnginePool<>())
            .run("console.log('hi')")
            .subscribe()
            .with(
                res -> System.out.println(res),
                err -> err.printStackTrace()
            );
    }

}
