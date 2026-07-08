-- Create admin user if not exists
-- Password is BCrypt hashed for "admin" (strength 10)
INSERT INTO users (username, email, pass, geo_location)
SELECT 'admin', 'admin@gmail.com', '$2a$10$MutQnXK4JTYbRuzkjaWj5uqaAVpajUb9DPe/SZvX6Z4bzO0.mfNTW', ST_SetSRID(ST_MakePoint(-70.6845, -33.4489), 4326)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@gmail.com');

-- Add ADMIN role
INSERT INTO user_roles (user_entity_id, roles)
SELECT u.id, 'ADMIN'
FROM users u
WHERE u.email = 'admin@gmail.com'
  AND NOT EXISTS (SELECT 1 FROM user_roles r WHERE r.user_entity_id = u.id AND r.roles = 'ADMIN');
