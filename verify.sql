SELECT COUNT(*) AS Users FROM User;
SELECT COUNT(*) AS Customers FROM Customer;
SELECT COUNT(*) AS Organizers FROM Organizer;

SELECT COUNT(*) AS Venues FROM Venue;
SELECT COUNT(*) AS Events FROM Event;
SELECT COUNT(*) AS Performances FROM Performance;
SELECT COUNT(*) AS Orders FROM CustomerOrder;
SELECT COUNT(*) AS Tickets FROM Ticket;
SELECT COUNT(*) AS Reviews FROM Reviews;
SELECT COUNT(*) AS Listings FROM ResaleListing;

SELECT COUNT(DISTINCT city) AS Cities
FROM Venue;

SELECT COUNT(DISTINCT country) AS Countries
FROM Venue;


SELECT
SUM(performanceDate < CURDATE()) AS Past,
SUM(performanceDate >= CURDATE()) AS Future
FROM Performance;

SELECT performanceID
FROM PriceTier
GROUP BY performanceID
HAVING COUNT(*) < 2;


SELECT
performanceID,
COUNT(*) AS TicketsSold
FROM Ticket
JOIN CustomerOrder USING(orderID)
GROUP BY performanceID
ORDER BY TicketsSold DESC;


SELECT
customerEmail,
COUNT(*) AS TicketsBought
FROM CustomerOrder
JOIN Ticket USING(orderID)
GROUP BY customerEmail
ORDER BY TicketsBought DESC
LIMIT 10;

SELECT status, COUNT(*)
FROM ResaleListing
GROUP BY status;

SELECT *
FROM CancelsPerformance;


SELECT *
FROM CancelsTicket;

SELECT
COUNT(*) AS Reviews
FROM Reviews;


SELECT
COUNT(DISTINCT performanceID)
FROM Reviews;

SELECT COUNT(*)
FROM Ticket t
LEFT JOIN CustomerOrder o
ON t.orderID=o.orderID
WHERE o.orderID IS NULL;

SELECT COUNT(*)
FROM CustomerOrder o
LEFT JOIN Customer c
ON o.customerEmail=c.email
WHERE c.email IS NULL;

SELECT COUNT(*)
FROM Performance p
LEFT JOIN Event e
ON p.eventID=e.eventID
WHERE e.eventID IS NULL;
