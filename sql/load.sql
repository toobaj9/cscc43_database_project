USE mytix;

SET FOREIGN_KEY_CHECKS = 0;

-- ===========================
-- Entities
-- ===========================

LOAD DATA LOCAL INFILE 'data/User.txt'
INTO TABLE users
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(email, name, dob, address);

LOAD DATA LOCAL INFILE 'data/Customer.txt'
INTO TABLE customer
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(email);

LOAD DATA LOCAL INFILE 'data/Organizer.txt'
INTO TABLE organizer
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(email);

LOAD DATA LOCAL INFILE 'data/PaymentInformation.txt'
INTO TABLE paymentInformation
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(card_num, cardholder_name, card_expiry, cvv);

LOAD DATA LOCAL INFILE 'data/Venue.txt'
INTO TABLE venue
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
venue_id,
venue_name,
latitude,
longitude,
street_address,
postal_code,
city,
country
);

LOAD DATA LOCAL INFILE 'data/Section.txt'
INTO TABLE section
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
venue_id,
section_name,
section_type
);

LOAD DATA LOCAL INFILE 'data/GeneralAdmissionSection.txt'
INTO TABLE generalAdmissionSection
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
venue_id,
section_name,
capacity
);

LOAD DATA LOCAL INFILE 'data/SeatRow.txt'
INTO TABLE seatRow
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
venue_id,
section_name,
row_name
);

LOAD DATA LOCAL INFILE 'data/Seat.txt'
INTO TABLE seat
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
venue_id,
section_name,
row_name,
seat_num
);

LOAD DATA LOCAL INFILE 'data/SegGenre.txt'
INTO TABLE segGenre
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
seg_name,
genre_name
);

LOAD DATA LOCAL INFILE 'data/Artist.txt'
INTO TABLE artist
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
artist_id,
artist_name,
artist_type
);

LOAD DATA LOCAL INFILE 'data/Event.txt'
INTO TABLE events
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
event_id,
organizer_email,
event_name,
resale_cap
);

LOAD DATA LOCAL INFILE 'data/Performance.txt'
INTO TABLE performance
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
performance_id,
performance_date,
performance_time,
performance_status,
event_id,
venue_id
);

LOAD DATA LOCAL INFILE 'data/PriceTier.txt'
INTO TABLE priceTier
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
performance_id,
tier_name,
price
);

LOAD DATA LOCAL INFILE 'data/CustomerOrder.txt'
INTO TABLE customerOrder
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
order_id,
order_date,
card_num,
performance_id,
customer_email
);

LOAD DATA LOCAL INFILE 'data/Ticket.txt'
INTO TABLE ticket
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
ticket_id,
face_value,
ticket_status,
order_id,
venue_id,
section_name
);

LOAD DATA LOCAL INFILE 'data/ResaleListing.txt'
INTO TABLE resaleListing
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
listing_id,
listing_price,
listed_at,
listing_status,
ticket_id,
seller_email
);

-- ===========================
-- Relationship tables
-- ===========================

LOAD DATA LOCAL INFILE 'data/Features.txt'
INTO TABLE features
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
event_id,
artist_id,
billing_order
);

LOAD DATA LOCAL INFILE 'data/BelongsTo.txt'
INTO TABLE belongsTo
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
event_id,
seg_name,
genre_name
);

LOAD DATA LOCAL INFILE 'data/AssignedToTier.txt'
INTO TABLE assignedToTier
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
performance_id,
venue_id,
section_name,
tier_name
);

LOAD DATA LOCAL INFILE 'data/Blocks.txt'
INTO TABLE blocks
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
performance_id,
section_name,
venue_id,
row_name,
seat_num,
blocked_at,
reason
);

LOAD DATA LOCAL INFILE 'data/ReserveSeat.txt'
INTO TABLE reserveSeat
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
ticket_id,
section_name,
venue_id,
row_name,
seat_num
);

LOAD DATA LOCAL INFILE 'data/OwnsTicket.txt'
INTO TABLE ownsTicket
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
ticket_id,
acquired_at,
email,
ended_at
);

LOAD DATA LOCAL INFILE 'data/PurchaseListing.txt'
INTO TABLE purchaseListing
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
listing_id,
buyer_email,
purchased_at,
sale_price
);

LOAD DATA LOCAL INFILE 'data/Reviews.txt'
INTO TABLE reviews
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
email,
performance_id,
event_rating,
venue_rating,
comment,
created_at
);

LOAD DATA LOCAL INFILE 'data/CancelsTicket.txt'
INTO TABLE cancelsTicket
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
ticket_id,
customer_email,
refund_amount,
reason,
cancelled_at
);

LOAD DATA LOCAL INFILE 'data/CancelsPerformance.txt'
INTO TABLE cancelsPerformance
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
performance_id,
organizer_email,
reason,
cancelled_at
);

SET FOREIGN_KEY_CHECKS = 1;