package com.onur.bootcamp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class NoteService {
    @Inject
    EntityManager em;

    private final List<Note> notes = new ArrayList<>();
    //private int nextId = 1;

    public List<Note> getAllNotes() {
        return getAllNotes(1, 10, "urgency,desc", Urgency.LOW, null);
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

        // Sanitize inputs (accept only allowed fields/orders for safety)
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
        /*return notes.stream()
                .filter(note -> note.getId() == id)
                .findAny()
                .orElse(null);*/
        return em.find(Note.class, id);
    }

    @Transactional
    public Note createNote(Note note) {
        /*note.setId(nextId++);
        notes.add(note);*/
        if (note.getUrgency() == null) {
            note.setUrgency(Urgency.LOW);
        }

        em.persist(note);

        return note;
    }

    @Transactional
    public Note updateNote(int id, NoteUpdateRequest incoming) {
        Note n = em.find(Note.class, id); // n is now a managed entity
        if (n == null) return null;
        if (incoming.getContent() != null) n.setContent(incoming.getContent());
        if (incoming.getTitle() != null) n.setTitle(incoming.getTitle());
        if (incoming.getUrgency() != null) n.setUrgency(incoming.getUrgency());
        // No need to call em.persist() or em.merge() for managed entities

        // When the transaction ends (method returns), JPA automatically saves the change!

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
        //return notes.removeIf(note -> note.getId() == id);
    }

    public Note getLatestNote() {
        /*if (notes.isEmpty()) return null;
        return notes.get(notes.size() - 1);*/

        List<Note> notes = em.createQuery("SELECT n FROM Note n ORDER BY n.id DESC", Note.class)
                .setMaxResults(1)
                .getResultList();
        return notes.isEmpty() ? null : notes.get(0);
    }
}