-- ============================================================
-- PostgreSQL Seed Script: Populate customer table with 30 profiles
-- Requires: CREATE EXTENSION IF NOT EXISTS "pgcrypto";
-- Author: Joshua Nkweti-Nyong
-- ============================================================

-- Ensure UUID generation support
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Optional: Drop table if you want to reset
-- DROP TABLE IF EXISTS customer CASCADE;

-- Insert sample data
INSERT INTO customer (id, first_name, last_name, address, email, birth_date, register_date)
VALUES
    (gen_random_uuid(), 'Joshua', 'Nkweti', '123 Elm Street', 'joshua.nkweti@example.com', '2002-11-23', CURRENT_DATE - INTERVAL '10 days'),
    (gen_random_uuid(), 'Emma', 'Thompson', '45 Lake View Ave', 'emma.thompson@example.com', '1998-06-14', CURRENT_DATE - INTERVAL '9 days'),
    (gen_random_uuid(), 'Liam', 'Johnson', '221 Baker Street', 'liam.johnson@example.com', '1995-03-11', CURRENT_DATE - INTERVAL '8 days'),
    (gen_random_uuid(), 'Olivia', 'Martinez', '12 River Road', 'olivia.martinez@example.com', '1993-12-22', CURRENT_DATE - INTERVAL '7 days'),
    (gen_random_uuid(), 'Noah', 'Anderson', '7 Maple Lane', 'noah.anderson@example.com', '1990-09-30', CURRENT_DATE - INTERVAL '6 days'),
    (gen_random_uuid(), 'Ava', 'Brown', '89 Hillcrest Drive', 'ava.brown@example.com', '2001-04-17', CURRENT_DATE - INTERVAL '5 days'),
    (gen_random_uuid(), 'Ethan', 'Clark', '56 Cedar Avenue', 'ethan.clark@example.com', '1999-02-10', CURRENT_DATE - INTERVAL '4 days'),
    (gen_random_uuid(), 'Sophia', 'Lewis', '303 Ocean Blvd', 'sophia.lewis@example.com', '1994-08-08', CURRENT_DATE - INTERVAL '3 days'),
    (gen_random_uuid(), 'Mason', 'Walker', '88 Forest Trail', 'mason.walker@example.com', '1996-11-21', CURRENT_DATE - INTERVAL '2 days'),
    (gen_random_uuid(), 'Isabella', 'Harris', '17 Pine Street', 'isabella.harris@example.com', '2000-01-15', CURRENT_DATE - INTERVAL '1 day'),
    (gen_random_uuid(), 'Elijah', 'Young', '23 Oak Lane', 'elijah.young@example.com', '1997-05-07', CURRENT_DATE - INTERVAL '12 days'),
    (gen_random_uuid(), 'Amelia', 'King', '15 Sunset Blvd', 'amelia.king@example.com', '1993-10-03', CURRENT_DATE - INTERVAL '11 days'),
    (gen_random_uuid(), 'James', 'Scott', '8 Meadow View', 'james.scott@example.com', '1992-02-20', CURRENT_DATE - INTERVAL '14 days'),
    (gen_random_uuid(), 'Mia', 'Adams', '102 Birch Road', 'mia.adams@example.com', '1999-07-19', CURRENT_DATE - INTERVAL '13 days'),
    (gen_random_uuid(), 'Benjamin', 'Nelson', '200 Broad Street', 'benjamin.nelson@example.com', '1988-06-06', CURRENT_DATE - INTERVAL '16 days'),
    (gen_random_uuid(), 'Charlotte', 'Perez', '34 Garden Way', 'charlotte.perez@example.com', '1995-10-28', CURRENT_DATE - INTERVAL '15 days'),
    (gen_random_uuid(), 'Lucas', 'Turner', '66 Riverbend Drive', 'lucas.turner@example.com', '1994-03-13', CURRENT_DATE - INTERVAL '18 days'),
    (gen_random_uuid(), 'Harper', 'Hill', '9 Greenfield Road', 'harper.hill@example.com', '1996-09-24', CURRENT_DATE - INTERVAL '17 days'),
    (gen_random_uuid(), 'Henry', 'Carter', '55 Cherry Lane', 'henry.carter@example.com', '1990-12-11', CURRENT_DATE - INTERVAL '19 days'),
    (gen_random_uuid(), 'Evelyn', 'Mitchell', '78 Willow Street', 'evelyn.mitchell@example.com', '1991-04-05', CURRENT_DATE - INTERVAL '20 days'),
    (gen_random_uuid(), 'Alexander', 'Roberts', '22 River Avenue', 'alexander.roberts@example.com', '1989-08-09', CURRENT_DATE - INTERVAL '21 days'),
    (gen_random_uuid(), 'Ella', 'Evans', '101 Park Road', 'ella.evans@example.com', '1997-01-03', CURRENT_DATE - INTERVAL '22 days'),
    (gen_random_uuid(), 'Michael', 'Collins', '303 Highland Blvd', 'michael.collins@example.com', '1998-09-27', CURRENT_DATE - INTERVAL '23 days'),
    (gen_random_uuid(), 'Abigail', 'Stewart', '88 Valley View', 'abigail.stewart@example.com', '1995-05-16', CURRENT_DATE - INTERVAL '24 days'),
    (gen_random_uuid(), 'Daniel', 'Sanchez', '12 Grove Street', 'daniel.sanchez@example.com', '1993-02-28', CURRENT_DATE - INTERVAL '25 days'),
    (gen_random_uuid(), 'Grace', 'Morris', '45 Evergreen Lane', 'grace.morris@example.com', '1999-11-01', CURRENT_DATE - INTERVAL '26 days'),
    (gen_random_uuid(), 'William', 'Rogers', '99 Summit Ave', 'william.rogers@example.com', '1987-07-07', CURRENT_DATE - INTERVAL '27 days'),
    (gen_random_uuid(), 'Victoria', 'Reed', '75 Bay Street', 'victoria.reed@example.com', '1992-10-14', CURRENT_DATE - INTERVAL '28 days'),
    (gen_random_uuid(), 'Samuel', 'Cook', '10 North Avenue', 'samuel.cook@example.com', '1994-04-25', CURRENT_DATE - INTERVAL '29 days'),
    (gen_random_uuid(), 'Scarlett', 'Bailey', '44 Cypress Blvd', 'scarlett.bailey@example.com', '1996-03-19', CURRENT_DATE - INTERVAL '30 days');

-- Verify inserted data
SELECT COUNT(*) AS total_customers FROM customer;
