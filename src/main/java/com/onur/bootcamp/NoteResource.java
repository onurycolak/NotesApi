package com.onur.bootcamp;

import jakarta.inject.Inject;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;


@Path("/notes")
public class NoteResource {
    @Inject
    NoteService noteService;

    @GET
    public List<Note> getAllNote(
            @QueryParam("page") @DefaultValue("1") @Min(1) int page,
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("sort") @DefaultValue("urgency,desc") String sort,
            @QueryParam("urgency") Urgency urgency,
            @QueryParam("title") String title

    ) {
        return noteService.getAllNotes(page, size, sort, urgency, title);
    }

    @GET
    @Path("/latest")
    public Note getLatestNote() {
        if (noteService.getAllNotes().isEmpty()) {
            throw new WebApplicationException("No notes available", Response.Status.NOT_FOUND);
        }

        return noteService.getLatestNote();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Note getNoteWithId(@PathParam("id") int id) {
        if (id <= 0) {
            throw new WebApplicationException("Id must be greater than 0", Response.Status.BAD_REQUEST);
        }

        Note n = noteService.getNoteById(id);

        if (n != null) return n;

        throw new WebApplicationException("Note not found", Response.Status.NOT_FOUND);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Note updateNote(@PathParam("id") int id, NoteUpdateRequest update) {
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
        return updated;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNote(@Valid Note note, @Context UriInfo uriInfo) {
        noteService.createNote(note);

        // Build URI for the new note, e.g., /notes/3
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(note.getId()))
                .build();

        return Response
                .created(location) // Sets status 201 and Location header
                .entity(note)
                .build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteNoteById(@PathParam("id") int id) {
        System.out.println(id);
        boolean deleted = noteService.deleteNoteById(id);

        if (id <= 0) {
            throw new WebApplicationException("Id must be greater than 0", Response.Status.BAD_REQUEST);
        }

        if (deleted) {
            return Response.noContent().build(); // 204 No Content, common for deletes
        } else {
            throw new WebApplicationException("Note not found", Response.Status.NOT_FOUND);
        }
    }
}