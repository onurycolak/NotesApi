package com.onur.bootcamp;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;


@Path("/notes")
public class NoteResource {
    @Inject
    NoteService noteService;

    @GET
    @APIResponse(responseCode = "200", description = "All notes returned according to given parameters if there are any.")
    @Operation(
            summary = "List notes",
            description = "Returns paginated, sorted, and filtered list of notes."
    )
    public Response getAllNote(
            @Parameter(
                    description = "Page number (1-based)"
            ) @QueryParam("page") @DefaultValue("1") int page,
            @Parameter(
                    description = "Page size"
            ) @QueryParam("size") @DefaultValue("10") int size,
            @Parameter(
                    description = "Comma separated sort configuration"
            ) @QueryParam("sort") @DefaultValue("urgency,desc") String sort,
            @Parameter(
                    description = "Urgency filter: one of LOW, MEDIUM, HIGH.",
                    schema = @Schema(implementation = Urgency.class)
            ) @QueryParam("urgency") Urgency urgency,
            @Parameter(
                    description = "Title filter, partial match available"
            ) @QueryParam("title") String title

    ) {
        List<Note> allNotes = noteService.getAllNotes(page, size, sort, urgency, title);

        return Response.ok(allNotes).build();
    }

    @GET
    @Operation(
            summary = "Get the last note",
            description = "Returns latest added note."
    )
    @APIResponse(responseCode = "200", description = "Latest added note is returned.")
    @APIResponse(
            responseCode = "404",
            description = "Note not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "NotFound",
                                    summary = "Note not found",
                                    value = "{\"error\": \"Note with the given ID not found.\", \"status\": 404}"
                            )
                    }
            )
    )
    @Path("/latest")
    public Response getLatestNote() {
        Note latest = noteService.getLatestNote();

        if (latest == null) {
            throw new WebApplicationException("Note not found", Response.Status.NOT_FOUND);
        }

        return Response.ok(latest).build();
    }

    @GET
    @Operation(
            summary = "Gets the note by ID.",
            description = "Returns the note matching the provided ID."
    )
    @APIResponse(responseCode = "200", description = "Note with given ID returned.")
    @APIResponse(
            responseCode = "400",
            description = "Invalid ID provided.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "BadRequest",
                                    summary = "Invalid ID",
                                    value = "{\"error\": \"Invalid ID provided.\", \"status\": 400}"
                            )
                    }
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Note with the given ID not found.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "NotFound",
                                    summary = "Note not found",
                                    value = "{\"error\": \"Note with the given ID not found.\", \"status\": 404}"
                            )
                    }
            )
    )
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNoteWithId(
        @Parameter(
            description = "Target note ID"
        ) @PathParam("id") int id
    ) {
        if (id <= 0) {
            throw new WebApplicationException("Id must be greater than 0", Response.Status.BAD_REQUEST);
        }

        Note note = noteService.getNoteById(id);

        if (note != null) return Response.ok(note).build();

        throw new WebApplicationException("Note not found", Response.Status.NOT_FOUND);
    }

    @PUT
    @Operation(
            summary = "Update the target note.",
            description = "Update the note matching with provided ID."
    )
    @APIResponse(responseCode = "200", description = "Note with given ID returned.")
    @APIResponse(
            responseCode = "400",
            description = "Invalid data provided.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "BadRequest",
                                    summary = "Invalid ID",
                                    value = "{\"error\": \"Invalid ID provided.\", \"status\": 400}"
                            )
                    }
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Note not found.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "NotFound",
                                    summary = "Note not found",
                                    value = "{\"error\": \"Note with the given ID not found.\", \"status\": 404}"
                            )
                    }
            )
    )
    @Parameter(
            description = "API key for authentication",
            in = ParameterIn.HEADER,
            required = true,
            name = "X-API-Key"
    )
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateNote(
        @Parameter(
            description = "Target note ID"
        ) @PathParam("id") int id,
        @RequestBody(
                description = "Note data to update, at least one of the content, title or urgency must be provided, given data must be non-empty."
        ) NoteUpdateRequest update
    ) {
        try {
            update.validate();
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e.getMessage(), 400);
        }

        if (id <= 0) {
            throw new WebApplicationException("Id must be greater than 0", Response.Status.BAD_REQUEST);
        }

        Note updated = noteService.updateNote(id, update);
        if (updated == null) {
            throw new WebApplicationException("Note not found", 404);
        }
        return Response.ok(updated).build();
    }

    @POST
    @Operation(
            summary = "Create a new note",
            description = "Creates a note. Requires non-empty title and content. Returns created note with status 201."
    )
    @APIResponse(responseCode = "201", description = "Note created")
    @APIResponse(
        responseCode = "400",
        description = "Invalid note",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                        @ExampleObject(
                                name = "BadRequest",
                                summary = "Invalid note.",
                                value = "{\"error\": \"Invalid note.\", \"status\": 400}"
                        )
                }
        )
    )
    @Parameter(
            description = "API key for authentication",
            in = ParameterIn.HEADER,
            required = true,
            name = "X-API-Key"
    )
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNote(
            @RequestBody(
                    description = "Note containing title, content and urgency which is an enum value of Low, Medium and High."
            ) @Valid Note note,
            @Context UriInfo uriInfo
    ) {
        noteService.createNote(note);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(note.getId()))
                .build();

        return Response
                .created(location)
                .entity(note)
                .build();
    }

    @DELETE
    @Operation(
            summary = "Delete note.",
            description = "Deletes the note matching the ID."
    )
    @APIResponse(responseCode = "204", description = "Note deleted.")
    @APIResponse(
        responseCode = "400",
        description = "Invalid ID provided.",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                        @ExampleObject(
                                name = "BadRequest",
                                summary = "Invalid ID",
                                value = "{\"error\": \"Invalid ID provided.\", \"status\": 400}"
                        )
                }
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Note with the given ID not found.",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                        @ExampleObject(
                                name = "NotFound",
                                summary = "Note not found",
                                value = "{\"error\": \"Note with the given ID not found.\", \"status\": 404}"
                        )
                }
        )
    )
    @Parameter(
            description = "API key for authentication",
            in = ParameterIn.HEADER,
            required = true,
            name = "X-API-Key"
    )
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteNoteById(
        @Parameter(
                description = "Target note ID"
        )
        @PathParam("id") int id
    ) {
        if (id <= 0) {
            throw new WebApplicationException("Id must be greater than 0", Response.Status.BAD_REQUEST);
        }

        boolean deleted = noteService.deleteNoteById(id);

        if (deleted) {
            return Response.noContent().build(); // 204 No Content, common for deletes
        } else {
            throw new WebApplicationException("Note not found", Response.Status.NOT_FOUND);
        }
    }
}