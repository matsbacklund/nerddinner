# --- First database schema

# --- !Ups

CREATE TABLE User (
  UserId                    SERIAL PRIMARY KEY,
  Username                  VARCHAR(128) NOT NULL,
  Email                     VARCHAR(255) NOT NULL,
  Password                  VARCHAR(64) NOT NULL
);

CREATE TABLE Dinner (
  DinnerId                  SERIAL PRIMARY KEY,
  Title                     VARCHAR(50) NOT NULL,
  EventDate                 DATETIME NOT NULL,
  Description               VARCHAR(255) NOT NULL,
  HostedBy                  VARCHAR(20) NOT NULL,
  ContactPhone              VARCHAR(20) NOT NULL,
  Address                   VARCHAR(50) NOT NULL,
  Country                   VARCHAR(30) NOT NULL,
  Latitude                  FLOAT NOT NULL,
  Longitude                 FLOAT NOT NULL
);

CREATE TABLE Rsvp (
  RsvpId                    SERIAL PRIMARY KEY,
  DinnerId                  INT NOT NULL,
  AttendeeName              VARCHAR(30) NOT NULL,
  FOREIGN KEY (DinnerId) REFERENCES Dinner(DinnerId)
);

INSERT INTO DINNER(Title, EventDate, Description, HostedBy, ContactPhone, Address, Country, Latitude, Longitude) values
('.NET Futures','2013-12-06 17:00:00','Come talk about cool things coming with .NET','scottgu','425-555-1212','One Microsoft Way, Redmond WA','USA',4.764312000000000e+001,-1.221306090000000e+002),
('Geek Out','2013-12-06 12:00:00','All things geek allowed','scottha','425-555-1212','One Microsoft Way, Redmond WA','USA',4.764312000000000e+001,-1.221306090000000e+002),
('Fine Wine','2013-12-07 12:00:00','Sample some fine Washington Wine','philha','425-555-1212','One Microsoft Way, Redmond WA','USA',4.763254600000000e+001,-1.222120100000000e+002),
('Surfing Lessons','2013-12-08 12:00:00','Ride the waves with Rob','robcon','425-555-1212','One Microsoft Way, Redmond WA','USA',4.763254600000000e+001,-1.222120100000000e+002),
('Curing Polio','2013-12-09 12:00:00','Discuss how we can eradicate polio around the world','billg','425-555-1212','One Microsoft Way, Redmond WA','USA',4.763254600000000e+001,-1.222120100000000e+002),
('Dinner with Sus','2013-02-28 17:00:00','Fun dinner with the wife','scottgu','425-555-1212','One Microsoft Way, Redmond WA','USA',4.763254600000000e+001,-1.222120100000000e+002),
('XBOX Gaming','2013-03-01 17:00:00','Game Fest with the newest XBox Games!','scottgu','425-555-1212','One Microsoft Way, Redmond WA 98052','USA',4.764312000000000e+001,-1.221306090000000e+002);

# --- !Downs

DROP TABLE IF EXISTS USER;
DROP TABLE IF EXISTS DINNER;
DROP TABLE IF EXISTS RSVP;