# java-filmorate

Получить все фильмы с жанрами.
```sql
SELECT f.name, 
    f.release_date, 
    g.name AS genre
FROM film AS f
JOIN film_genre AS fg ON f.film_id = fg.film_id
JOIN genre AS g ON fg.genre_id = g.genre_id;
```
Топ‑5 популярных фильмов.
```sql
SELECT f.name, 
    AVG(fr.rating) AS avg_rating, 
    COUNT(fr.user_id) AS votes
FROM film f
JOIN film_rating AS fr ON f.film_id = fr.film_id
GROUP BY f.film_id, 
    f.name
ORDER BY avg_rating DESC, 
    votes DESC
LIMIT 5;
```
Список пользователей.
```sql
SELECT user_id, 
    name, 
    email, 
    login, 
    birthday 
FROM user;
```
Проверка статуса дружбы между пользователями
```sql
SELECT u.name, 
    s.name AS status
FROM friendship AS fr
JOIN user AS u ON fr.friend_id = u.user_id
JOIN status AS s USING (status_id)
WHERE fr.user_id = 1;
```
Получение списка жанров для конкретного фильма
```sql
SELECT g.name
FROM genre AS g
JOIN film_genre USING (genre_id)
WHERE film_id = 1;
```