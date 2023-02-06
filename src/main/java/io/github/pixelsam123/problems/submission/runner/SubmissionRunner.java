package io.github.pixelsam123.problems.submission.runner;

import io.smallrye.mutiny.Uni;

public interface SubmissionRunner {

    Uni<SubmissionRunResult> run(String jsCode);

}
