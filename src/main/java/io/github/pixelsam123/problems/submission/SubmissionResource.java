package io.github.pixelsam123.problems.submission;

import io.github.pixelsam123.problems.ProblemService;
import io.github.pixelsam123.problems.submission.runner.ISubmissionRunner;
import io.github.pixelsam123.problems.submission.runner.JavetSubmissionRunner;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

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
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> postOne(String code, int idx) {
        return problemService
            .getOneByIdx(idx)
            .onItemOrFailure()
            .transformToUni((problem, error) -> {
                if (error instanceof IndexOutOfBoundsException) {
                    return Uni.createFrom().item(() -> Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(Map.ofEntries(
                            Map.entry("content", "Invalid index")
                        ))
                        .build());
                }

                return submissionRunner
                    .run(code + '\n' + problem.tests())
                    .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                    .map(result -> Response.ok(result).build());
            });
    }

}
