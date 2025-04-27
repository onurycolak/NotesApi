package com.onur.bootcamp;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class NoteService {
    @Inject
    EntityManager em;

    public List<Note> getAllNotes() {
        return getAllNotes(1, 10, "urgency,desc", null, null);
    }

    public List<Note> getAllNotes(int page, int size, String sort, Urgency urgency, String title) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        String sortOrder = (sortParts.length > 1) ? sortParts[1].toUpperCase() : "DESC";

        String jpql = "SELECT n FROM Note n WHERE 1=1";

        if (title != null && !title.isBlank()) {
            jpql += " AND LOWER(n.title) like :title";
        }

        if (urgency != null) {
            jpql += " AND n.urgency = :urgency";
        }

        if (!sortField.matches("id|title|urgency|content")) sortField = "urgency";
        if (!sortOrder.matches("ASC|DESC")) sortOrder = "DESC";

        String orderBy;
        if ("title".equals(sortField) || "content".equals(sortField)) {
            orderBy = " ORDER BY LOWER(n." + sortField + ") " + sortOrder;
        } else {
            orderBy = " ORDER BY n." + sortField + " " + sortOrder;
        }

        TypedQuery<Note> query = em.createQuery(jpql + orderBy, Note.class);;

        if(title != null && !title.isBlank()) {
            query.setParameter("title", "%" + title.toLowerCase() + "%");
        }

        if (urgency != null) {
            query.setParameter("urgency", urgency);
        }

        return query
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Note getNoteById(int id) {
        return em.find(Note.class, id);
    }

    @Transactional
    public void createNote(Note note) {
        if (note.getUrgency() == null) {
            note.setUrgency(Urgency.LOW);
        }

        em.persist(note);

    }

    @Transactional
    public Note updateNote(int id, NoteUpdateRequest incoming) {
        Note n = em.find(Note.class, id); // n is now a managed entity
        if (n == null) return null;
        if (incoming.getContent() != null) n.setContent(incoming.getContent());
        if (incoming.getTitle() != null) n.setTitle(incoming.getTitle());
        if (incoming.getUrgency() != null) n.setUrgency(incoming.getUrgency());

        return n;
    }

    @Transactional
    public boolean deleteNoteById(int id) {
        Note n = em.find(Note.class, id);
        if (n != null) {
            em.remove(n);
            return true;
        }
        return false;
    }

    public Note getLatestNote() {
        List<Note> notes = em.createQuery("SELECT n FROM Note n ORDER BY n.id DESC", Note.class)
                .setMaxResults(1)
                .getResultList();
        return notes.isEmpty() ? null : notes.get(0);
    }
}