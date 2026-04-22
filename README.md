# java-filmorate
<img width="1085" height="732" alt="db-sheme_dev" src="https://github.com/user-attachments/assets/faf7172d-18ed-44b8-a2ac-b1cdd3c3168a" />

Список пользователей
```sql
SELECT user_id, 
    name, 
    email, 
    login, 
    birthday 
FROM user;
```
Получение списка жанров для конкретного фильма
```sql
SELECT g.genre_id, g.name
FROM genres g
JOIN film_genres fg ON g.genre_id = fg.genre_id
WHERE fg.film_id = ?
ORDER BY g.genre_id
```
Получение списка всех режисеров
```sql
SELECT director_id, name
FROM directors
ORDER BY director_id
```
Получение фильмов по режисеру отсортированных по году
```sql
SELECT f.film_id, f.title, f.description, f.release_date, f.duration,
    f.rating_id, mr.name AS mpa_name
FROM films f
LEFT JOIN mpa_rating mr ON f.rating_id = mr.rating_id
JOIN film_directors fd ON f.film_id = fd.film_id
WHERE fd.director_id = ?
ORDER BY f.release_date ASC, f.film_id ASC
```
Получение ленты новостей
```sql
SELECT event_id, timestamp, user_id, event_type, operation, entity_id
FROM events
WHERE user_id = ?
ORDER BY timestamp ASC
```
Получение всех отзывов к фильму
```sql
SELECT r.*, COALESCE(
    SUM(CASE WHEN rl.user_id IS NOT NULL THEN 1 ELSE 0 END) -
    SUM(CASE WHEN rd.user_id IS NOT NULL THEN 1 ELSE 0 END), 0) AS useful
FROM reviews r
LEFT JOIN review_likes rl ON r.review_id = rl.review_id
LEFT JOIN review_dislikes rd ON r.review_id = rd.review_id
WHERE r.film_id = ?
GROUP BY r.review_id
ORDER BY useful DESC
LIMIT ?
```
