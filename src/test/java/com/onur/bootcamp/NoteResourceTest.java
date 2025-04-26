package com.onur.bootcamp;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@QuarkusTest
public class NoteResourceTest {
    @Inject
    EntityManager em;

    @BeforeEach
    @Transactional
    public void cleanDb() {
        em.createQuery("DELETE FROM Note").executeUpdate();
    }

    // --- POST Tests ---

    @Test
    public void testPostNote_Success() {
        given()
                .contentType("application/json")
                .body("{\"content\":\"foo content\",\"title\":\"foo title\"}")
                .when().post("/notes")
                .then()
                .statusCode(201)
                .body("content", is("foo content"))
                .body("title", is("foo title"));
    }

    @Test
    public void testPostNote_BlankContent() {
        given()
                .contentType("application/json")
                .body("{\"content\":\"\"}")
                .when().post("/notes")
                .then()
                .statusCode(400);
    }

    @Test
    public void testPostNote_InvalidInputs() {
        // Bad urgency string
        given()
                .contentType("application/json")
                .body("{\"content\":\"alpha\",\"title\":\"bravo\",\"urgency\":\"INVALID\"}")
                .when().post("/notes")
                .then()
                .statusCode(400);

        // Missing content
        given()
                .contentType("application/json")
                .body("{\"title\":\"bravo\",\"urgency\":\"LOW\"}")
                .when().post("/notes")
                .then()
                .statusCode(400);

        // Missing title
        given()
                .contentType("application/json")
                .body("{\"content\":\"alpha\",\"urgency\":\"LOW\"}")
                .when().post("/notes")
                .then()
                .statusCode(400);
    }

    // --- GET Tests ---

    @Test
    public void testGetAllNotes() {
        given()
            .contentType("application/json")
            .body("{\"content\":\"foo bar\",\"title\":\"foo bar\"}")
            .when().post("/notes").then().statusCode(201);

        given()
                .when().get("/notes")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    public void testGetNotes_WithFilter() {
        given()
                .contentType("application/json")
                .body("{\"content\":\"lorem ipsum\",\"title\":\"lorem ipsum\",\"urgency\":\"HIGH\"}")
                .when().post("/notes")
                .then()
                .statusCode(201);

        given()
                .when().get("/notes?title=lorem&urgency=high")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("title", hasItem("lorem ipsum"));
    }

    @Test
    public void testGetNotes_WithSizeLimit() {
        given()
                .contentType("application/json")
                .body("{\"content\":\"gamma\",\"title\":\"gamma\",\"urgency\":\"HIGH\"}")
                .when().post("/notes")
                .then()
                .statusCode(201);

        given()
                .contentType("application/json")
                .body("{\"content\":\"delta\",\"title\":\"delta\",\"urgency\":\"HIGH\"}")
                .when().post("/notes")
                .then()
                .statusCode(201);

        given()
                .when().get("/notes?size=1")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1));
    }

    @Test
    public void testGetNotes_Pagination() {
        given()
                .contentType("application/json")
                .body("{\"content\":\"apple\",\"title\":\"apple\",\"urgency\":\"LOW\"}")
                .when().post("/notes")
                .then()
                .statusCode(201);

        given()
                .contentType("application/json")
                .body("{\"content\":\"banana\",\"title\":\"banana\",\"urgency\":\"LOW\"}")
                .when().post("/notes")
                .then()
                .statusCode(201);

        String firstTitle = given()
                .when().get("/notes?size=1&page=1&sort=title,asc")
                .then()
                .statusCode(200)
                .extract()
                .body().jsonPath().getString("title[0]");

        String secondTitle = given()
                .when().get("/notes?size=1&page=2&sort=title,asc")
                .then()
                .statusCode(200)
                .extract()
                .body().jsonPath().getString("title[0]");

        assertNotEquals(firstTitle, secondTitle);
    }

    @Test
    public void testGetNotes_OutOfBounds() {
        given()
                .when().get("/notes?size=10&page=9999")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    public void testGetNotes_Sorting() {
        // Use alphabetically clear values
        given().contentType("application/json")
                .body("{\"content\":\"alpha\",\"title\":\"alpha\",\"urgency\":\"LOW\"}")
                .when().post("/notes").then().statusCode(201);
        given().contentType("application/json")
                .body("{\"content\":\"omega\",\"title\":\"omega\",\"urgency\":\"HIGH\"}")
                .when().post("/notes").then().statusCode(201);
        given().contentType("application/json")
                .body("{\"content\":\"gamma\",\"title\":\"gamma\",\"urgency\":\"MEDIUM\"}")
                .when().post("/notes").then().statusCode(201);

        given().when().get("/notes?sort=urgency,desc&size=1")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("urgency", hasItem("HIGH"));

        given().when().get("/notes?sort=urgency,asc&size=1")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("urgency", hasItem("LOW"));

        given().when().get("/notes?sort=title,asc&size=1")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("title", hasItem("alpha"));
        given().when().get("/notes?sort=title,desc&size=1")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("title", hasItem("omega"));

        given().when().get("/notes?sort=content,asc&size=1")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("content", hasItem("alpha"));
        given().when().get("/notes?sort=content,desc&size=1")
                .then().statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("content", hasItem("omega"));
    }

    // --- PUT Tests (now split out) ---

    @Test
    public void testPutNote_UpdateContentAndTitle() {
        int noteId = given()
                .contentType("application/json")
                .body("{\"title\":\"pre-update\",\"content\":\"pre-update\",\"urgency\":\"HIGH\"}")
                .when().post("/notes")
                .then()
                .statusCode(201)
                .extract()
                .body().jsonPath().getInt("id");

        given()
                .contentType("application/json")
                .body("{\"title\":\"updated title\",\"content\":\"updated content\"}")
                .when().put("/notes/" + noteId)
                .then().statusCode(200);

        given()
                .when().get("/notes/" + noteId)
                .then().statusCode(200)
                .body("title", equalTo("updated title"))
                .body("content", equalTo("updated content"))
                .body("urgency", equalTo("HIGH"));
    }

    @Test
    public void testPutNote_UpdateUrgency() {
        int noteId = given()
                .contentType("application/json")
                .body("{\"title\":\"update urgency\",\"content\":\"update urgency\",\"urgency\":\"MEDIUM\"}")
                .when().post("/notes")
                .then()
                .statusCode(201)
                .extract()
                .body().jsonPath().getInt("id");

        given()
                .contentType("application/json")
                .body("{\"urgency\":\"LOW\"}")
                .when().put("/notes/" + noteId)
                .then().statusCode(200);

        given()
                .when().get("/notes/" + noteId)
                .then().statusCode(200)
                .body("urgency", equalTo("LOW"));
    }

    @Test
    public void testPutNote_EmptyFieldsAndInvalidInputs() {
        int noteId = given()
                .contentType("application/json")
                .body("{\"title\":\"bad input test\",\"content\":\"bad input test\",\"urgency\":\"LOW\"}")
                .when().post("/notes")
                .then()
                .statusCode(201)
                .extract()
                .body().jsonPath().getInt("id");

        // Empty urgency
        given().contentType("application/json").body("{\"urgency\":\"\"}").when().put("/notes/" + noteId).then().statusCode(400);

        // Invalid urgency
        given().contentType("application/json").body("{\"urgency\":\"INVALID\"}").when().put("/notes/" + noteId).then().statusCode(400);

        // Integer urgency
        given().contentType("application/json").body("{\"urgency\":1}").when().put("/notes/" + noteId).then().statusCode(400);

        // Empty title/content
        given().contentType("application/json").body("{\"title\":\"\"}").when().put("/notes/" + noteId).then().statusCode(400);
        given().contentType("application/json").body("{\"content\":\"\"}").when().put("/notes/" + noteId).then().statusCode(400);
        given().contentType("application/json").body("{\"title\":\"\",\"content\":\"\"}").when().put("/notes/" + noteId).then().statusCode(400);
        // Empty payload
        given().contentType("application/json").body("{}").when().put("/notes/" + noteId).then().statusCode(400);
    }

    @Test
    public void testPutNote_NonExistentIdAndUnknownFields() {
        int noteId = given()
                .contentType("application/json")
                .body("{\"title\":\"nonexistent test\",\"content\":\"nonexistent test\",\"urgency\":\"HIGH\"}")
                .when().post("/notes")
                .then()
                .statusCode(201)
                .extract()
                .body().jsonPath().getInt("id");

        // Non-existent ID
        given().contentType("application/json").body("{\"title\":\"foo\"}").when().put("/notes/99999999").then().statusCode(404);

        // Unknown field "foo" should be ignored, "title" should update
        given().contentType("application/json").body("{\"title\":\"final update\",\"foo\":\"bar\"}").when().put("/notes/" + noteId).then().statusCode(200);
        given().when().get("/notes/" + noteId).then().statusCode(200).body("title", equalTo("final update")).body("$", not(hasKey("foo")));

        // Immutable id field in body is ignored
        given().contentType("application/json").body("{\"id\":123456,\"title\":\"should work\"}").when().put("/notes/" + noteId).then().statusCode(200);
        given().when().get("/notes/" + noteId).then().statusCode(200).body("id", equalTo(noteId));
    }

    // --- DELETE Tests ---

    @Test
    public void testDeleteNote() {
        String uniqueTitle = "delete me " + System.currentTimeMillis();
        int noteId = given()
                .contentType("application/json")
                .body("{\"title\":\"" + uniqueTitle + "\",\"content\":\"delete content\"}")
                .when().post("/notes")
                .then()
                .statusCode(201)
                .extract()
                .body().jsonPath().getInt("id");

        given().when().delete("/notes/" + noteId).then().statusCode(204);
        given().when().get("/notes/" + noteId).then().statusCode(404);
        given().when().delete("/notes/" + noteId).then().statusCode(404);
        given().when().delete("/notes/987654321").then().statusCode(404);

        // Should not see deleted note in the list
        given().when().get("/notes?size=10000")
                .then()
                .statusCode(200)
                .body("title", not(hasItem(uniqueTitle)));
    }
}
