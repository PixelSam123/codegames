package io.github.pixelsam123.problems.submission;

import io.agroal.api.AgroalDataSource;
import io.github.pixelsam123.problems.ProblemService;
import io.github.pixelsam123.problems.submission.runner.ISubmissionRunner;
import io.github.pixelsam123.problems.submission.runner.JavetSubmissionRunner;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.URI;
import java.security.Principal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

@Path("/problems/v1/submission")
public class SubmissionResource {

    private final ProblemService problemService;
    private final ISubmissionRunner submissionRunner;
    private final AgroalDataSource dataSource;

    public SubmissionResource(
        ProblemService problemService,
        JavetSubmissionRunner submissionRunner,
        AgroalDataSource dataSource
    ) {
        this.problemService = problemService;
        this.submissionRunner = submissionRunner;
        this.dataSource = dataSource;
    }

    @POST
    @Path("/{idx}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> postOne(@Context SecurityContext securityCtx, String code, int idx) {
        return problemService
            .getOneByIdx(idx)
            .onItemOrFailure()
            .transformToUni((problem, error) -> {
                if (error instanceof IndexOutOfBoundsException) {
                    return Uni
                        .createFrom()
                        .item(() -> Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(Map.ofEntries(Map.entry("content", "Invalid index")))
                            .build());
                }

                return submissionRunner
                    .run(code + '\n' + problem.tests())
                    .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                    .map(result -> {
                        Principal loggedInUser = securityCtx.getUserPrincipal();

                        if (loggedInUser == null) {
                            return Response.ok(result).build();
                        }

                        try (PreparedStatement submissionInsertion = dataSource
                            .getConnection()
                            .prepareStatement("INSERT INTO submission VALUES (?,?,?)")) {
                            submissionInsertion.setString(1, loggedInUser.getName());
                            submissionInsertion.setString(2, code);
                            submissionInsertion.setString(3, result.status().value);

                            submissionInsertion.executeUpdate();
                        } catch (SQLException err) {
                            return Response.serverError().entity(Map.ofEntries(
                                Map.entry("content", err.toString())
                            )).build();
                        }

                        return Response
                            .created(URI.create("/problems/v1/submission/" + idx))
                            .entity(result)
                            .build();
                    })
                    .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
            });
    }

}
