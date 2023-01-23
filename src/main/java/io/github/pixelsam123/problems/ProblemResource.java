package io.github.pixelsam123.problems;

import io.smallrye.mutiny.Uni;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/problems/v1/problem")
public class ProblemResource {

    private final ProblemService problemService;

    public ProblemResource(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<ProblemPreview>> getAllPreviews() {
        return problemService
            .getAll()
            .map(problems -> problems
                .stream()
                .map(problem -> new ProblemPreview(problem.title(), problem.description()))
                .toList());
    }

    @GET
    @Path("/{idx}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getOneByIdx(int idx) {
        return problemService
            .getOneByIdx(idx)
            .onItemOrFailure()
            .transform((problem, error) -> {
                if (error != null) {
                    if (error instanceof IndexOutOfBoundsException) {
                        return Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(Map.ofEntries(
                                Map.entry("content", "Invalid index")
                            ))
                            .build();
                    }

                    return Response
                        .serverError()
                        .entity(Map.ofEntries(
                            Map.entry("content", error.getClass().getSimpleName())
                        ))
                        .build();
                }

                return Response
                    .ok(new ProblemDetailedView(
                        problem.title(),
                        problem.description(),
                        problem.initialCode()
                    ))
                    .build();
            });
    }

}
