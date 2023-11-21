CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    about VARCHAR(255),
    roles VARCHAR(255),
    languages VARCHAR(255),
    skills VARCHAR(255),
    projects_experiences VARCHAR(255),
    assignments VARCHAR(255),
    profile_pic VARCHAR(255)
);
