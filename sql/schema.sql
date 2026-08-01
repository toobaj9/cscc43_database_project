DROP TABLE IF EXISTS cancelsPerformance;
DROP TABLE IF EXISTS cancelsTicket;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS ownsTicket;
DROP TABLE IF EXISTS purchaseListing;
DROP TABLE IF EXISTS reserveSeat;
DROP TABLE IF EXISTS blocks;
DROP TABLE IF EXISTS assignedToTier;
DROP TABLE IF EXISTS belongsTo;
DROP TABLE IF EXISTS features;
DROP TABLE IF EXISTS resaleListing;
DROP TABLE IF EXISTS ticket;
DROP TABLE IF EXISTS customerOrder;
DROP TABLE IF EXISTS paymentInformation;
DROP TABLE IF EXISTS seat;
DROP TABLE IF EXISTS seatRow;
DROP TABLE IF EXISTS generalAdmissionSection;
DROP TABLE IF EXISTS section;
DROP TABLE IF EXISTS priceTier;
DROP TABLE IF EXISTS performance;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS organizer;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS segGenre;
DROP TABLE IF EXISTS artist;
DROP TABLE IF EXISTS venue;

CREATE TABLE users (
  email                  VARCHAR(255)    PRIMARY KEY,
  name                   VARCHAR(100)    NOT NULL,
  dob                    DATE            NOT NULL,
  address                VARCHAR(500)    NOT NULL
);

CREATE TABLE customer (
  email                  VARCHAR(255)    PRIMARY KEY,
  FOREIGN KEY (email) REFERENCES users(email)
);

CREATE TABLE organizer (
  email                  VARCHAR(255)    PRIMARY KEY,
  FOREIGN KEY (email) REFERENCES users(email)
);

CREATE TABLE events (
  event_id               INT             AUTO_INCREMENT PRIMARY KEY,
  organizer_email        VARCHAR(255)    NOT NULL,
  event_name             VARCHAR(100)    NOT NULL,
  resale_cap             DECIMAL(4, 2)   NOT NULL,
  FOREIGN KEY (organizer_email) REFERENCES organizer(email)
);

CREATE TABLE segGenre (
  seg_name               VARCHAR(100)    NOT NULL,
  genre_name             VARCHAR(100)    NOT NULL,
  PRIMARY KEY (seg_name, genre_name)
);

CREATE TABLE artist (
  artist_id              INT             AUTO_INCREMENT PRIMARY KEY,
  artist_name            VARCHAR(100)    NOT NULL,
  artist_type            VARCHAR(10)     NOT NULL,
  CHECK (artist_type = 'artist' or artist_type = 'team')
);

CREATE TABLE venue (
  venue_id               INT             AUTO_INCREMENT PRIMARY KEY,
  venue_name             VARCHAR(100)    NOT NULL,
  latitude               DECIMAL(9, 6)   NOT NULL,
  longitude              DECIMAL(9, 6)   NOT NULL,
  street_address         VARCHAR(255)    NOT NULL,
  postal_code            VARCHAR(10)     NOT NULL,
  city                   VARCHAR(100)    NOT NULL,
  country                VARCHAR(100)    NOT NULL
);

CREATE TABLE performance (
  performance_id         INT             AUTO_INCREMENT PRIMARY KEY,
  performance_date       DATE            NOT NULL,
  performance_time       TIME            NOT NULL,
  performance_status     VARCHAR(50)     NOT NULL,
  event_id               INT             NOT NULL,
  venue_id               INT             NOT NULL,
  FOREIGN KEY (event_id) REFERENCES events(event_id),
  FOREIGN KEY (venue_id) REFERENCES venue(venue_id),
  CHECK (performance_status = 'scheduled' or performance_status = 'cancelled' or performance_status = 'completed')
);

CREATE TABLE priceTier (
  performance_id         INT             NOT NULL,
  tier_name              VARCHAR(10)     NOT NULL,
  price                  DECIMAL(6, 2)   NOT NULL,
  PRIMARY KEY (performance_id, tier_name),
  FOREIGN KEY (performance_id) REFERENCES performance(performance_id)
);

-- added this sectiontype here to classify general and reserved otherwise reserved would be just same as section
CREATE TABLE section (
  venue_id               INT             NOT NULL,
  section_name           VARCHAR(100)    NOT NULL,
  section_type           VARCHAR(10)     NOT NULL,
  PRIMARY KEY (venue_id, section_name),
  FOREIGN KEY (venue_id) REFERENCES venue(venue_id),
  CHECK (section_type = 'general' or section_type = 'reserved')
);

CREATE TABLE generalAdmissionSection (
  venue_id               INT             NOT NULL,
  section_name           VARCHAR(100)    NOT NULL,
  capacity               INT             NOT NULL,
  PRIMARY KEY (venue_id, section_name),
  FOREIGN KEY (venue_id, section_name) REFERENCES section(venue_id, section_name)
);

CREATE TABLE seatRow (
  venue_id               INT             NOT NULL,
  section_name           VARCHAR(100)    NOT NULL,
  row_name               VARCHAR(100)    NOT NULL,
  PRIMARY KEY (venue_id, section_name, row_name),
  FOREIGN KEY (venue_id, section_name) REFERENCES section(venue_id, section_name)
);

CREATE TABLE seat (
  venue_id               INT             NOT NULL,
  section_name           VARCHAR(100)    NOT NULL,
  row_name               VARCHAR(100)    NOT NULL,
  seat_num               INT             NOT NULL,
  PRIMARY KEY (venue_id, section_name, row_name, seat_num),
  FOREIGN KEY (venue_id, section_name, row_name) REFERENCES seatRow(venue_id, section_name, row_name)
);

CREATE TABLE paymentInformation (
  card_num               VARCHAR(16)     PRIMARY KEY,
  cardholder_name        VARCHAR(100)    NOT NULL,
  card_expiry            DATE            NOT NULL,
  cvv                    VARCHAR(3)      NOT NULL
);

CREATE TABLE customerOrder (
  order_id               INT             AUTO_INCREMENT PRIMARY KEY,
  order_date             DATETIME        NOT NULL DEFAULT (NOW()),
  card_num               VARCHAR(16)     NOT NULL,
  performance_id         INT             NOT NULL,
  customer_email         VARCHAR(255)    NOT NULL,
  FOREIGN KEY (performance_id) REFERENCES performance(performance_id),
  FOREIGN KEY (customer_email) REFERENCES customer(email),
  FOREIGN KEY (card_num) REFERENCES paymentInformation(card_num)
);

CREATE TABLE ticket (
  ticket_id              INT             AUTO_INCREMENT PRIMARY KEY,
  face_value             DECIMAL(6, 2)   NOT NULL,
  ticket_status          VARCHAR(50)     NOT NULL,
  order_id               INT             NOT NULL,
  venue_id               INT             NOT NULL,
  section_name           VARCHAR(100)    NOT NULL,
  FOREIGN KEY (venue_id, section_name) REFERENCES section(venue_id, section_name),
  FOREIGN KEY (order_id) REFERENCES customerOrder(order_id),
  CHECK (ticket_status = 'active' or ticket_status = 'cancelled' or ticket_status = 'refunded')
);

CREATE TABLE resaleListing (
  listing_id             INT             AUTO_INCREMENT PRIMARY KEY,
  listing_price          DECIMAL(6,2)    NOT NULL,
  listed_at              DATETIME        NOT NULL DEFAULT (NOW()),
  listing_status         VARCHAR(50)     NOT NULL,
  ticket_id              INT             NOT NULL,
  seller_email           VARCHAR(255)    NOT NULL,
  FOREIGN KEY (ticket_id) REFERENCES ticket(ticket_id),
  FOREIGN KEY (seller_email) REFERENCES customer(email),
  CHECK (listing_status = 'active' or listing_status = 'withdrawn' or listing_status = 'sold')
);

CREATE TABLE features (
  event_id               INT             NOT NULL,
  artist_id              INT             NOT NULL,
  billing_order          VARCHAR(100)    NOT NULL,
  PRIMARY KEY (event_id, artist_id),
  FOREIGN KEY (event_id) REFERENCES events(event_id),
  FOREIGN KEY (artist_id) REFERENCES artist(artist_id),
  CHECK (billing_order = 'Headliner' or billing_order = 'Special Guest' or billing_order = 'Opening Act')
);

CREATE TABLE belongsTo (
  event_id               INT             NOT NULL,
  seg_name               VARCHAR(100)    NOT NULL,
  genre_name             VARCHAR(100)    NOT NULL,
  PRIMARY KEY (event_id),
  FOREIGN KEY (event_id) REFERENCES events(event_id),
  FOREIGN KEY (seg_name, genre_name) REFERENCES segGenre(seg_name, genre_name)
);

CREATE TABLE assignedToTier (
  performance_id         INT             NOT NULL,
  venue_id               INT             NOT NULL,
  section_name           VARCHAR(100)    NOT NULL,
  tier_name              VARCHAR(10)     NOT NULL,
  PRIMARY KEY (performance_id, section_name),
  FOREIGN KEY (performance_id, tier_name) REFERENCES priceTier(performance_id, tier_name),
  FOREIGN KEY (venue_id, section_name) REFERENCES section(venue_id, section_name)
);

CREATE TABLE blocks (
  performance_id         INT             NOT NULL,
  section_name           VARCHAR(100)    NOT NULL,
  venue_id               INT             NOT NULL,
  row_name               VARCHAR(100)    NOT NULL,
  seat_num               INT             NOT NULL,
  blocked_at             DATETIME        NOT NULL DEFAULT (NOW()),
  reason                 VARCHAR(500)    NOT NULL,
  PRIMARY KEY (performance_id, section_name, row_name, seat_num),
  FOREIGN KEY (performance_id) REFERENCES performance(performance_id),
  FOREIGN KEY (venue_id, section_name, row_name, seat_num) REFERENCES seat(venue_id, section_name, row_name, seat_num)
);

CREATE TABLE reserveSeat (
  ticket_id              INT             NOT NULL,
  section_name           VARCHAR(100)    NOT NULL,
  venue_id               INT             NOT NULL,
  row_name               VARCHAR(100)    NOT NULL,
  seat_num               INT             NOT NULL,
  PRIMARY KEY (ticket_id),
  FOREIGN KEY (ticket_id) REFERENCES ticket(ticket_id),
  FOREIGN KEY (venue_id, section_name, row_name, seat_num) REFERENCES seat(venue_id, section_name, row_name, seat_num)
);

CREATE TABLE purchaseListing (
  listing_id             INT             NOT NULL,
  buyer_email            VARCHAR(255)    NOT NULL,
  purchased_at           DATETIME        NOT NULL DEFAULT (NOW()),
  sale_price             DECIMAL(6, 2)   NOT NULL,
  PRIMARY KEY (listing_id),
  FOREIGN KEY (listing_id) REFERENCES resaleListing(listing_id),
  FOREIGN KEY (buyer_email) REFERENCES customer(email)
);

CREATE TABLE ownsTicket (
  ticket_id              INT             NOT NULL,
  acquired_at            DATETIME        NOT NULL DEFAULT (NOW()),
  email                  VARCHAR(255)    NOT NULL,
  ended_at               DATETIME        DEFAULT NULL,
  PRIMARY KEY (ticket_id, acquired_at),
  FOREIGN KEY (ticket_id) REFERENCES ticket(ticket_id),
  FOREIGN KEY (email) REFERENCES customer(email)
);

CREATE TABLE reviews (
  email                  VARCHAR(255)    NOT NULL,
  performance_id         INT             NOT NULL,
  event_rating           INT             NOT NULL,
  venue_rating           INT             NOT NULL,
  comment                VARCHAR(1000)   NOT NULL,
  created_at             DATETIME        NOT NULL DEFAULT (NOW()),
  PRIMARY KEY (email, performance_id),
  FOREIGN KEY (performance_id) REFERENCES performance(performance_id),
  FOREIGN KEY (email) REFERENCES customer(email),
  CHECK (event_rating >= 1 and event_rating <= 5),
  CHECK (venue_rating >= 1 and venue_rating <= 5)
);

CREATE TABLE cancelsTicket (
  ticket_id              INT             NOT NULL,
  customer_email         VARCHAR(255)    NOT NULL,
  refund_amount          DECIMAL(6, 2)   NOT NULL,
  reason                 VARCHAR(500)    NOT NULL,
  cancelled_at           DATETIME        NOT NULL DEFAULT (NOW()),
  PRIMARY KEY (ticket_id),
  FOREIGN KEY (ticket_id) REFERENCES ticket(ticket_id),
  FOREIGN KEY (customer_email) REFERENCES customer(email),
  CHECK (refund_amount >= 0)
);

CREATE TABLE cancelsPerformance (
  performance_id         INT             NOT NULL,
  organizer_email        VARCHAR(255)    NOT NULL,
  reason                 VARCHAR(500)    NOT NULL,
  cancelled_at           DATETIME        NOT NULL DEFAULT (NOW()),
  PRIMARY KEY (performance_id),
  FOREIGN KEY (performance_id) REFERENCES performance(performance_id),
  FOREIGN KEY (organizer_email) REFERENCES organizer(email)
);