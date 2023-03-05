package io.github.pixelsam123;

import com.caoccao.javet.enums.JSRuntimeType;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.engine.IJavetEnginePool;
import com.caoccao.javet.interop.engine.JavetEngineConfig;
import com.caoccao.javet.interop.engine.JavetEnginePool;
import io.agroal.api.AgroalDataSource;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;

@QuarkusMain
public class Codegames implements QuarkusApplication {

    private final AgroalDataSource dataSource;

    public Codegames(AgroalDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @ApplicationScoped
    public IJavetEnginePool<V8Runtime> javetEnginePool() {
        JavetEngineConfig config = new JavetEngineConfig();
        config.setJSRuntimeType(JSRuntimeType.V8);

        return new JavetEnginePool<>();
    }

    @Override
    public int run(String... args) throws Exception {
        if (!new File("codegames.db").exists()) {
            try (InputStream resource = Thread
                .currentThread()
                .getContextClassLoader()
                .getResourceAsStream("codegames_init.sql")) {
                assert resource != null;

                StringBuilder databaseInitStr = new StringBuilder();
                try (Scanner databaseInitFile = new Scanner(resource)) {
                    while (databaseInitFile.hasNextLine()) {
                        databaseInitStr.append(databaseInitFile.nextLine());
                    }
                }

                String[] databaseInitCodes = databaseInitStr.toString().split(";");

                try (Connection c = dataSource.getConnection()) {
                    for (String initCode : databaseInitCodes) {
                        try (PreparedStatement initStatement = c.prepareStatement(initCode)) {
                            boolean isInitSuccessful = initStatement.execute();
                            if (isInitSuccessful) {
                                throw new Exception(
                                    "Failed to create database at step:\n" + initCode
                                );
                            }
                        }
                    }
                }
            }
        }

        Quarkus.waitForExit();
        return 0;
    }

}
