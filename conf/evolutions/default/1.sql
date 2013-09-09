# --- !Ups

CREATE TABLE dinners (
  id                        SERIAL PRIMARY KEY,
  title                     VARCHAR(50) NOT NULL,
  event_date                TIMESTAMP NOT NULL,
  description               VARCHAR(255) NOT NULL,
  hosted_by                 VARCHAR(20) NOT NULL,
  contact_phone             VARCHAR(20) NOT NULL,
  address                   VARCHAR(50) NOT NULL,
  country                   VARCHAR(30) NOT NULL,
  latitude                  FLOAT NOT NULL,
  longitude                 FLOAT NOT NULL
);

CREATE TABLE rsvps (
  id                        SERIAL PRIMARY KEY,
  dinner_id                 INT NOT NULL,
  attendee_name             VARCHAR(30) NOT NULL,
  FOREIGN KEY (dinner_id) REFERENCES dinners(id)
);

INSERT INTO dinners(title, event_date, description, hosted_by, contact_phone, address, country, latitude, longitude) values
('.NET Futures','2013-12-06 17:00:00','Come talk about cool things coming with .NET','scottgu','425-555-1212','One Microsoft Way, Redmond WA','USA',4.764312000000000e+001,-1.221306090000000e+002),
('Geek Out','2013-12-06 12:00:00','All things geek allowed','scottha','425-555-1212','One Microsoft Way, Redmond WA','USA',4.764312000000000e+001,-1.221306090000000e+002),
('Fine Wine','2013-12-07 12:00:00','Sample some fine Washington Wine','philha','425-555-1212','One Microsoft Way, Redmond WA','USA',4.763254600000000e+001,-1.222120100000000e+002),
('Surfing Lessons','2013-12-08 12:00:00','Ride the waves with Rob','robcon','425-555-1212','One Microsoft Way, Redmond WA','USA',4.763254600000000e+001,-1.222120100000000e+002),
('Curing Polio','2013-12-09 12:00:00','Discuss how we can eradicate polio around the world','billg','425-555-1212','One Microsoft Way, Redmond WA','USA',4.763254600000000e+001,-1.222120100000000e+002),
('Dinner with Sus','2013-02-28 17:00:00','Fun dinner with the wife','scottgu','425-555-1212','One Microsoft Way, Redmond WA','USA',4.763254600000000e+001,-1.222120100000000e+002),
('XBOX Gaming','2013-03-01 17:00:00','Game Fest with the newest XBox Games!','scottgu','425-555-1212','One Microsoft Way, Redmond WA 98052','USA',4.764312000000000e+001,-1.221306090000000e+002);

# --- !Downs

DROP TABLE IF EXISTS rsvps;
DROP TABLE IF EXISTS dinners;
