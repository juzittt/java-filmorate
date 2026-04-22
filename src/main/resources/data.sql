MERGE INTO mpa_rating (rating_id, name) VALUES (1, 'G');
MERGE INTO mpa_rating (rating_id, name) VALUES (2, 'PG');
MERGE INTO mpa_rating (rating_id, name) VALUES (3, 'PG-13');
MERGE INTO mpa_rating (rating_id, name) VALUES (4, 'R');
MERGE INTO mpa_rating (rating_id, name) VALUES (5, 'NC-17');

MERGE INTO genres (genre_id, name) VALUES (1, 'Комедия');
MERGE INTO genres (genre_id, name) VALUES (2, 'Драма');
MERGE INTO genres (genre_id, name) VALUES (3, 'Мультфильм');
MERGE INTO genres (genre_id, name) VALUES (4, 'Триллер');
MERGE INTO genres (genre_id, name) VALUES (5, 'Документальный');
MERGE INTO genres (genre_id, name) VALUES (6, 'Боевик');

MERGE INTO friendship_status (status_id, name) VALUES(1,'CONFIRMED');
MERGE INTO friendship_status (status_id, name) VALUES(2,'UNCONFIRMED');

MERGE INTO event_type (event_type_id, name) VALUES (1, 'LIKE');
MERGE INTO event_type (event_type_id, name) VALUES (2, 'REVIEW');
MERGE INTO event_type (event_type_id, name) VALUES (3, 'FRIEND');

MERGE INTO operation_type (operation_id, name) VALUES (1, 'ADD');
MERGE INTO operation_type (operation_id, name) VALUES (2, 'UPDATE');
MERGE INTO operation_type (operation_id, name) VALUES (3, 'REMOVE');