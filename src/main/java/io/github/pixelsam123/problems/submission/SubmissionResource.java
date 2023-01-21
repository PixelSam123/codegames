package io.github.pixelsam123.problems.submission;

import io.github.pixelsam123.problems.ProblemService;
import io.github.pixelsam123.problems.submission.runner.ISubmissionRunner;
import io.github.pixelsam123.problems.submission.runner.JavetSubmissionRunner;
import io.github.pixelsam123.problems.submission.runner.SubmissionRunResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/problems/v1/submission")
public class SubmissionResource {

    private final ProblemService problemService;
    private final ISubmissionRunner submissionRunner;

    public SubmissionResource(
        ProblemService problemService,
        JavetSubmissionRunner submissionRunner
    ) {
        this.problemService = problemService;
        this.submissionRunner = submissionRunner;
    }

    @POST
    @Path("/{idx}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> postOne(int idx) {
        Uni<SubmissionRunResult> result = submissionRunner
            .run("")
            .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());

        return Uni.createFrom().item(Response.ok("yes").build());
    }

}
