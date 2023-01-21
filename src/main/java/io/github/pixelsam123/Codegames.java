package io.github.pixelsam123;

import com.caoccao.javet.enums.JSRuntimeType;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.engine.IJavetEnginePool;
import com.caoccao.javet.interop.engine.JavetEngineConfig;
import com.caoccao.javet.interop.engine.JavetEnginePool;

import javax.enterprise.context.ApplicationScoped;

public class Codegames {

    @ApplicationScoped
    public IJavetEnginePool<V8Runtime> javetEnginePool() {
        JavetEngineConfig config = new JavetEngineConfig();
        config.setJSRuntimeType(JSRuntimeType.V8);

        return new JavetEnginePool<>();
    }

}
