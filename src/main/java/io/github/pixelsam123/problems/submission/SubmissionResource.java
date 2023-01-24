package io.github.pixelsam123.problems.submission;

import io.agroal.api.AgroalDataSource;
import io.github.pixelsam123.problems.ProblemService;
import io.github.pixelsam123.problems.submission.runner.ISubmissionRunner;
import io.github.pixelsam123.problems.submission.runner.JavetSubmissionRunner;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.URI;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    @GET
    @Path("/{idx}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getAllForProblem(int idx) {
        return Uni.createFrom().item(() -> {
            try (
                Connection c = dataSource.getConnection();
                PreparedStatement submissionSelection = c.prepareStatement(
                    "SELECT * FROM submission WHERE problem_idx=?"
                )
            ) {
                submissionSelection.setInt(1, idx);

                ResultSet res = submissionSelection.executeQuery();

                List<Submission> submissions = new ArrayList<>();
                while (res.next()) {
                    submissions.add(new Submission(
                        res.getString("user_pk"),
                        res.getString("content"),
                        res.getString("status")
                    ));
                }

                return Response.ok(submissions).build();
            } catch (SQLException err) {
                return Response.serverError().entity(err.toString()).build();
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
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

                        try (
                            Connection c = dataSource.getConnection();
                            PreparedStatement submissionInsertion = c.prepareStatement(
                                "INSERT INTO submission VALUES (?,?,?,?)"
                            )
                        ) {
                            submissionInsertion.setString(1, loggedInUser.getName());
                            submissionInsertion.setInt(2, idx);
                            submissionInsertion.setString(3, code);
                            submissionInsertion.setString(4, result.status().value);

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
