# --- First database schema

# --- !Ups

CREATE TABLE USER (
  id                        SERIAL PRIMARY KEY,
  username                  VARCHAR(255) NOT NULL,
  email                     VARCHAR(255) NOT NULL,
  password                  VARCHAR(255) NOT NULL
);

CREATE TABLE DINNER (
  id                        SERIAL PRIMARY KEY,
  title                     VARCHAR(50) NOT NULL,
  event_date                DATETIME,
  description               VARCHAR(255) NOT NULL,
  hosted_by                 VARCHAR(20) NOT NULL,
  contact_phone             VARCHAR(20) NOT NULL,
  address                   VARCHAR(50) NOT NULL,
  country                   VARCHAR(30) NOT NULL,
  latitude                  FLOAT,
  longitude                 FLOAT
);

CREATE TABLE RSVP (
  id                        SERIAL PRIMARY KEY,
  dinner_id                 INT,
  attendee_name             VARCHAR(30) NOT NULL,
  FOREIGN KEY (dinner_id) REFERENCES DINNER(id)
);

INSERT INTO DINNER(title, event_date, description, hosted_by, contact_phone, address, country, latitude, longitude) values
('.NET Futures', '2012-06-12 12:00:00', 'Come talk about cool things', 'scottgu', '425-985-3648', 'One Microsoft Road', 'USA', 47.64312, -122.130609),
('Geek Out', '2012-06-12 14:30:00', 'All things geek allowed', 'scottha', '425-555-1212', 'One Microsoft Road', 'USA', 47.64312, -122.130609);

# --- !Downs

DROP TABLE IF EXISTS USER;
DROP TABLE IF EXISTS DINNER;
DROP TABLE IF EXISTS RSVP;