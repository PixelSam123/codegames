package io.github.pixelsam123.user;

import io.agroal.api.AgroalDataSource;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("/user/v1")
public class UserResource {

    private final AgroalDataSource dataSource;

    public UserResource(AgroalDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getAllPreviews() {
        return Uni.createFrom().item(() -> {
            try (PreparedStatement userSelection = dataSource.getConnection().prepareStatement("""
                SELECT
                    name,
                    (SELECT count() FROM submission WHERE status='Accepted' AND user_pk=name)
                        AS accepted_submission_count
                FROM user
                """)) {
                ResultSet res = userSelection.executeQuery();

                List<UserPreview> userPreviews = new ArrayList<>();
                while (res.next()) {
                    userPreviews.add(new UserPreview(
                        res.getString("name"),
                        res.getInt("accepted_submission_count")
                    ));
                }

                return Response.ok(userPreviews).build();
            } catch (SQLException err) {
                return Response.serverError().entity(Map.ofEntries(
                    Map.entry("content", err.toString())
                )).build();
            }
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> postOne(UserCreationView user) {
        return Uni.createFrom().item(() -> {
            try (PreparedStatement userInsertion = dataSource.getConnection().prepareStatement(
                "INSERT INTO user VALUES (?,?,?)"
            )) {
                userInsertion.setString(1, user.name());
                userInsertion.setString(2, BcryptUtil.bcryptHash(user.password()));
                userInsertion.setString(3, "user");

                userInsertion.executeUpdate();
            } catch (SQLException err) {
                return Response.serverError().entity(Map.ofEntries(
                    Map.entry("content", err.toString())
                )).build();
            }

            return Response.created(URI.create("/user/" + user.name())).build();
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

}
