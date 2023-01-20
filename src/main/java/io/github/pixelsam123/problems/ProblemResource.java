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

    private static final List<Problem> problems = List.of(
        new Problem(
            "Clamp Trolls",
            """
                Streamers are becoming really angry! They keep getting donation messages with repeated symbols like this:
                ```
                @@@@@@@@@@@@@@@@@@@ :):):):):):):)
                ``` \s
                The text-to-speech engines will read them out like 'at symbol at symbol at symbol at symbol..., smiley face smiley face'... you get the point.
                Your task is to create a function that will clamp down the trolls. It should handle:
                1. Obvious examples using symbols
                ```
                clampTrolls('@@@@@@@@@@@@@@@@@@@ :):):):):):):)', 4) // '@@@@ :):):):)'
                clampTrolls(':):):):):)', 2) // ':):)'
                clampTrolls('In-between normal @@@@@@@@@@ text', 3) // 'In-between normal @@@ text'
                ```
                2. Manual repetition of substrings
                ```
                clampTrolls('your smiley face smiley face smiley face smiley face', 2) // 'your smiley face smiley face'
                ```
                Please help them before they throw their phones in rage!
                """.trim(),
            "const clampTrolls = (str, maxLimit) => {}"
        )
    );

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<ProblemPreview>> getAllPreviews() {
        return Uni.createFrom().item(() -> problems
            .stream()
            .map(problem -> new ProblemPreview(problem.title(), problem.description()))
            .toList());
    }

    @GET
    @Path("/{idx}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getOneByIdx(int idx) {
        return Uni.createFrom().item(() -> {
            int maxIdx = problems.size() - 1;

            if (idx < 0 || idx > maxIdx) {
                return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(Map.ofEntries(
                        Map.entry(
                            "content",
                            "Invalid index. Valid index is 0-" + maxIdx + " inclusive"
                        )
                    ))
                    .build();
            }

            return Response.ok(problems.get(idx)).build();
        });
    }

}
