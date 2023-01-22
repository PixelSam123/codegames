package io.github.pixelsam123.problems.submission.runner;

public enum SubmissionRunStatus {

    ACCEPTED("Accepted"),
    COMPILE_ERROR("CompileError"),
    RUNTIME_ERROR("RuntimeError");

    public final String value;

    SubmissionRunStatus(String value) {
        this.value = value;
    }

}
