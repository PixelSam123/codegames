package io.github.pixelsam123.problems.submission.runner;

public record SubmissionRunResult(
    SubmissionRunStatus status,
    String stdout
) {
}
