USE mytix;

SET FOREIGN_KEY_CHECKS = 0;

-- ===========================
-- Entities
-- ===========================

LOAD DATA LOCAL INFILE 'data/User.txt'
INTO TABLE User
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(email, name, address, dateOfBirth);

LOAD DATA LOCAL INFILE 'data/Customer.txt'
INTO TABLE Customer
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(email);

LOAD DATA LOCAL INFILE 'data/Organizer.txt'
INTO TABLE Organizer
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(email);

LOAD DATA LOCAL INFILE 'data/PaymentInformation.txt'
INTO TABLE PaymentInformation
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(cardNum, cardholderName, expiryDate, CVV);

LOAD DATA LOCAL INFILE 'data/Venue.txt'
INTO TABLE Venue
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
venueID,
venueName,
latitude,
longitude,
streetAddress,
postalCode,
city,
country
);

LOAD DATA LOCAL INFILE 'data/Section.txt'
INTO TABLE Section
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
venueID,
sectionName
);

LOAD DATA LOCAL INFILE 'data/ReservedSection.txt'
INTO TABLE ReservedSection
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
venueID,
sectionName
);

LOAD DATA LOCAL INFILE 'data/GeneralAdmissionSection.txt'
INTO TABLE GeneralAdmissionSection
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
venueID,
sectionName,
capacity
);

LOAD DATA LOCAL INFILE 'data/SeatRow.txt'
INTO TABLE SeatRow
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
venueID,
sectionName,
rowName
);

LOAD DATA LOCAL INFILE 'data/Seat.txt'
INTO TABLE Seat
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
venueID,
sectionName,
rowName,
seatNum
);

LOAD DATA LOCAL INFILE 'data/SegGenre.txt'
INTO TABLE SegGenre
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
segName,
genreName
);

LOAD DATA LOCAL INFILE 'data/Artist.txt'
INTO TABLE Artist
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
artistID,
artistName,
artistType
);

LOAD DATA LOCAL INFILE 'data/Event.txt'
INTO TABLE Event
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
eventID,
organizerEmail,
eventName,
resaleCap
);

LOAD DATA LOCAL INFILE 'data/Performance.txt'
INTO TABLE Performance
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
performanceID,
performanceDate,
performanceTime,
status,
eventID,
venueID
);

LOAD DATA LOCAL INFILE 'data/PriceTier.txt'
INTO TABLE PriceTier
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
performanceID,
tierName,
price
);

LOAD DATA LOCAL INFILE 'data/CustomerOrder.txt'
INTO TABLE CustomerOrder
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
orderID,
orderDate,
cardNum,
performanceID,
customerEmail
);

LOAD DATA LOCAL INFILE 'data/Ticket.txt'
INTO TABLE Ticket
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
ticketID,
faceValue,
status,
orderID,
venueID,
sectionName
);

LOAD DATA LOCAL INFILE 'data/ResaleListing.txt'
INTO TABLE ResaleListing
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
listingID,
listingPrice,
listedAt,
status,
ticketID,
sellerEmail
);

-- ===========================
-- Relationship tables
-- ===========================

LOAD DATA LOCAL INFILE 'data/Features.txt'
INTO TABLE Features
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
eventID,
artistID,
billingOrder
);

LOAD DATA LOCAL INFILE 'data/BelongsTo.txt'
INTO TABLE BelongsTo
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
eventID,
segName,
genreName
);

LOAD DATA LOCAL INFILE 'data/AssignedToTier.txt'
INTO TABLE AssignedToTier
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
performanceID,
sectionName,
tierName
);

LOAD DATA LOCAL INFILE 'data/Blocks.txt'
INTO TABLE Blocks
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
performanceID,
sectionName,
rowName,
seatNum,
blockedAt,
reason
);

LOAD DATA LOCAL INFILE 'data/ReserveSeat.txt'
INTO TABLE ReserveSeat
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
ticketID,
venueID,
sectionName,
rowName,
seatNum
);

LOAD DATA LOCAL INFILE 'data/OwnsTicket.txt'
INTO TABLE OwnsTicket
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
ticketID,
acquiredAt,
email,
endedAt
);

LOAD DATA LOCAL INFILE 'data/PurchaseListing.txt'
INTO TABLE PurchaseListing
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
listingID,
buyerEmail,
purchasedAt,
salePrice
);

LOAD DATA LOCAL INFILE 'data/Reviews.txt'
INTO TABLE Reviews
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
email,
performanceID,
eventRating,
venueRating,
comment,
createdAt
);

LOAD DATA LOCAL INFILE 'data/CancelsTicket.txt'
INTO TABLE CancelsTicket
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
ticketID,
customerEmail,
cancelledAt,
refundAmount,
reason
);

LOAD DATA LOCAL INFILE 'data/CancelsPerformance.txt'
INTO TABLE CancelsPerformance
FIELDS TERMINATED BY '\t'
LINES TERMINATED BY '\n'
(
performanceID,
organizerEmail,
cancelledAt,
reason
);

SET FOREIGN_KEY_CHECKS = 1;