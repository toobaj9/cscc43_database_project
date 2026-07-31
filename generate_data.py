from __future__ import annotations

from collections import defaultdict
from datetime import date, datetime, time, timedelta
from pathlib import Path
import random

DATA_DIR = Path("data")
DATA_DIR.mkdir(exist_ok=True)

random.seed(43)
TODAY = date.today()
NOW = datetime.now().replace(microsecond=0)

def clean_value(value):
    if value is None:
        return r"\N"
    if isinstance(value, datetime):
        return value.strftime("%Y-%m-%d %H:%M:%S")
    if isinstance(value, date):
        return value.strftime("%Y-%m-%d")
    if isinstance(value, time):
        return value.strftime("%H:%M:%S")
    return str(value).replace("\t", " ").replace("\r", " ").replace("\n", " ")


def write_rows(filename, rows):
    with (DATA_DIR / filename).open("w", encoding="utf-8", newline="") as file:
        for row in rows:
            file.write("\t".join(clean_value(value) for value in row) + "\n")


def dt(days_from_today, hour=19, minute=0):
    return datetime.combine(TODAY + timedelta(days=days_from_today), time(hour, minute))


users, customers, organizers, payment_information = [], [], [], []
venues, sections, ga_sections, seat_rows, seats = [], [], [], [], []
seg_genres, artists, events, features, belongs_to = [], [], [], [], []
performances, price_tiers, assigned_to_tier, blocks = [], [], [], []
orders, tickets, reserve_seats, owns_ticket = [], [], [], []
resale_listings, purchase_listings, reviews = [], [], []
cancels_ticket, cancels_performance = [], []

# Users
organizer_data = [
    ("organizer1@mytix.com", "Maple Entertainment", "100 King Street West, Toronto", "1985-04-12"),
    ("organizer2@mytix.com", "Northern Stage Productions", "50 Queen Street, Ottawa", "1982-09-23"),
    ("organizer3@mytix.com", "Pacific Live Events", "800 Granville Street, Vancouver", "1979-02-15"),
    ("organizer4@mytix.com", "Montreal Arts Group", "1200 Rue Sainte-Catherine, Montreal", "1988-07-05"),
    ("organizer5@mytix.com", "Empire Events", "200 Broadway, New York", "1980-11-18"),
]
for email, name, address, dob in organizer_data:
    users.append((email, name, dob, address))
    organizers.append((email,))

first_names = ["Avery", "Maya", "Noah", "Layla", "Ethan", "Sofia", "Liam", "Amira", "Lucas", "Zoe", "Owen", "Nora", "Adam", "Leah", "Daniel", "Sara", "Ryan", "Hana", "Mason", "Eva"]
last_names = ["Chen", "Singh", "Martin", "Ahmed", "Brown", "Wilson", "Garcia", "Khan", "Lee", "Taylor", "Nguyen", "Patel", "Clark", "Lewis", "Young"]
customer_cities = [("Toronto", "Canada"), ("Ottawa", "Canada"), ("Montreal", "Canada"), ("Vancouver", "Canada"), ("New York", "United States"), ("Chicago", "United States")]

for i in range(1, 101):
    email = f"customer{i:03}@mytix.com"
    name = f"{first_names[(i - 1) % len(first_names)]} {last_names[(i * 3) % len(last_names)]}"
    city, country = customer_cities[(i - 1) % len(customer_cities)]
    address = f"{100 + i} Example Street, {city}, {country}"
    dob = date(1965 + (i % 35), (i % 12) + 1, ((i * 2) % 27) + 1)
    users.append((email, name, dob, address))
    customers.append((email,))
    card_num = f"400000000000{i:04}"
    expiry = date(TODAY.year + 3 + (i % 3), (i % 12) + 1, 1)
    payment_information.append((card_num, name, expiry, f"{100 + i:03}"))

customer_emails = [row[0] for row in customers]
card_by_customer = {customer_emails[i]: payment_information[i][0] for i in range(100)}

# Venues and seating
venues.extend([
    (1, "Harbourfront Arena", 43.6435, -79.3791, "40 Bay Street", "M5J 2X2", "Toronto", "Canada"),
    (2, "Downtown Concert Hall", 43.6467, -79.3859, "60 Simcoe Street", "M5J 2H5", "Toronto", "Canada"),
    (3, "Queen Street Theatre", 43.6508, -79.3902, "250 Queen Street West", "M5V 1Z7", "Toronto", "Canada"),
    (4, "Ottawa Civic Theatre", 45.4215, -75.6972, "100 Elgin Street", "K1P 5K8", "Ottawa", "Canada"),
    (5, "Montreal Arts Centre", 45.5088, -73.5878, "175 Rue Sainte-Catherine", "H2X 1Z8", "Montreal", "Canada"),
    (6, "Pacific Performance Hall", 49.2827, -123.1207, "800 Granville Street", "V6Z 1K3", "Vancouver", "Canada"),
    (7, "Manhattan Events Hall", 40.7580, -73.9855, "1500 Broadway", "10036", "New York", "United States"),
    (8, "Chicago Lakeside Theatre", 41.8781, -87.6298, "200 North State Street", "60601", "Chicago", "United States"),
])
venue_layouts = {
    1: {"Orchestra": (5, 12), "Balcony": (4, 10), "Lawn": 60},
    2: {"Main Floor": (6, 14), "Mezzanine": (4, 12), "Terrace": 80},
    3: {"Orchestra": (4, 10), "Balcony": (3, 8), "Atrium": 35},
    4: {"Main Floor": (6, 12), "Upper Level": (5, 10), "Pavilion": 70},
    5: {"Parterre": (7, 14), "Balcon": (5, 12), "Courtyard": 90},
    6: {"Orchestra": (8, 16), "Balcony": (6, 14), "Plaza": 120},
    7: {"Floor": (10, 18), "Upper Bowl": (8, 16), "Concourse": 150},
    8: {"Main Floor": (7, 15), "Gallery": (5, 12), "Promenade": 100},
}
reserved_section_names = defaultdict(list)
ga_section_name = {}
seat_inventory = defaultdict(list)
section_capacity = {}
for venue_id, layout in venue_layouts.items():
    for section_name, layout_value in layout.items():
        if isinstance(layout_value, tuple):
            # reserved section
            sections.append((venue_id, section_name, "reserved"))

            row_count, seats_per_row = layout_value
            reserved_section_names[venue_id].append(section_name)
            section_capacity[(venue_id, section_name)] = row_count * seats_per_row
            for row_index in range(row_count):
                row_name = chr(ord("A") + row_index)
                seat_rows.append((venue_id, section_name, row_name))
                for seat_num in range(1, seats_per_row + 1):
                    seats.append((venue_id, section_name, row_name, seat_num))
                    seat_inventory[(venue_id, section_name)].append((row_name, seat_num))
        else:
            sections.append((venue_id, section_name, "general"))
            ga_sections.append((venue_id, section_name, layout_value))
            ga_section_name[venue_id] = section_name
            section_capacity[(venue_id, section_name)] = layout_value

# Segments, artists, events
seg_genres.extend([
    ("Concerts", "Pop"), ("Concerts", "Rock"), ("Concerts", "Classical"),
    ("Arts, Theatre & Comedy", "Musical"), ("Arts, Theatre & Comedy", "Comedy"),
    ("Arts, Theatre & Comedy", "Classical"), ("Sports", "Basketball"), ("Sports", "Hockey"),
])
artists.extend([
    (1, "Nova Lane", "artist"),
    (2, "The Northern Lights", "team"),
    (3, "Maya Rivers", "artist"),
    (4, "Atlas Echo", "team"),
    (5, "Toronto Chamber Ensemble", "team"),
    (6, "Lena Brooks", "artist"),
    (7, "Samir Patel", "artist"),
    (8, "The Crescent Players", "team"),
    (9, "Metro Dance Collective", "team"),
    (10, "Blue Horizon", "team"),
    (11, "Aria Chen", "artist"),
    (12, "Redwood Avenue", "team"),
    (13, "City Hoops", "team"),
    (14, "Northern Blades", "team"),
    (15, "Violet Sky", "artist"),
    (16, "The Grand Quartet", "team"),
    (17, "Jordan Miles", "artist"),
    (18, "West Coast Pulse", "team"),
])
event_specs = [
    (1, "organizer1@mytix.com", "Neon Nights Tour", 1.25, "Concerts", "Pop", [1, 3, 15]),
    (2, "organizer1@mytix.com", "Northern Lights Live", 1.20, "Concerts", "Rock", [2, 12]),
    (3, "organizer2@mytix.com", "The Clockmaker", 1.15, "Arts, Theatre & Comedy", "Musical", [8, 9]),
    (4, "organizer2@mytix.com", "Late Night Laughs", 1.30, "Arts, Theatre & Comedy", "Comedy", [6, 7, 17]),
    (5, "organizer3@mytix.com", "Pacific Sound Festival", 1.25, "Concerts", "Rock", [4, 10, 18]),
    (6, "organizer4@mytix.com", "Mozart by Candlelight", 1.10, "Concerts", "Classical", [5, 11]),
    (7, "organizer5@mytix.com", "City Hoops Showcase", 1.20, "Sports", "Basketball", [13]),
    (8, "organizer5@mytix.com", "Northern Blades Classic", 1.20, "Sports", "Hockey", [14]),
    (9, "organizer1@mytix.com", "Violet Sky Acoustic", 1.15, "Concerts", "Pop", [15]),
    (10, "organizer2@mytix.com", "Comedy Across Canada", 1.25, "Arts, Theatre & Comedy", "Comedy", [6, 17]),
    (11, "organizer3@mytix.com", "Blue Horizon Reunion", 1.30, "Concerts", "Rock", [10]),
    (12, "organizer4@mytix.com", "The Grand Quartet", 1.10, "Concerts", "Classical", [16]),
    (13, "organizer2@mytix.com", "Moonlit Steps", 1.15, "Arts, Theatre & Comedy", "Musical", [9, 8]),
    (14, "organizer5@mytix.com", "Downtown Basketball Cup", 1.25, "Sports", "Basketball", [13]),
    (15, "organizer1@mytix.com", "Pop Rising", 1.30, "Concerts", "Pop", [3, 11, 1]),
    (16, "organizer3@mytix.com", "West Coast Pulse Live", 1.25, "Concerts", "Rock", [18]),
    (17, "organizer4@mytix.com", "Classics at the Theatre", 1.10, "Arts, Theatre & Comedy", "Classical", [5, 16]),
    (18, "organizer2@mytix.com", "Jordan Miles: New Material", 1.20, "Arts, Theatre & Comedy", "Comedy", [17]),
    (19, "organizer5@mytix.com", "Winter Hockey Exhibition", 1.20, "Sports", "Hockey", [14]),
    (20, "organizer3@mytix.com", "Atlas Echo World Tour", 1.30, "Concerts", "Rock", [4, 12]),
]
event_by_id = {}
for event_id, organizer_email, event_name, resale_cap, segment, genre, event_artists in event_specs:
    events.append((event_id, organizer_email, event_name, resale_cap))
    event_by_id[event_id] = {"organizer": organizer_email, "name": event_name, "cap": resale_cap}
    belongs_to.append((event_id, segment, genre))
    labels = ["Headliner", "Special Guest", "Opening Act"]
    for index, artist_id in enumerate(event_artists):
        features.append((event_id, artist_id, labels[min(index, 2)]))

# Performances
SPECIAL = {"sold_out": 1, "low_sales": 2, "availability_showcase": 31, "under_7_days": 32, "cancelled_1": 8, "cancelled_2": 19, "unsold_tier": 33}
performance_specs = []
for index, offset in enumerate([-220, -200, -180, -160, -140, -120, -100, -80, 20, 35]):
    performance_specs.append((3, 3, offset, 19 if index % 2 == 0 else 14))
for venue_id, offset in zip([1, 4, 5, 6, 7, 8], [-250, -170, -90, 25, 55, 85]):
    performance_specs.append((1, venue_id, offset, 20))
offset_pool = [-330, -310, -290, -270, -240, -210, -190, -150, -130, -110, -70, -55, -40, -25, -15, -5, 3, 6, 10, 14, 18, 28, 40, 50, 65, 75, 95, 110, 130, 150, 180, 210]
venue_cycle = [2, 4, 5, 6, 7, 8, 1, 3]
for event_id in [2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]:
    for repetition in range(3):
        performance_specs.append((event_id, venue_cycle[(event_id + repetition) % 8], offset_pool[(event_id * 3 + repetition) % len(offset_pool)], 13 if repetition == 0 else (16 if repetition == 1 else 20)))
performance_specs = performance_specs[:60]
for performance_id, (event_id, venue_id, offset, hour) in enumerate(performance_specs, 1):
    perf_dt = dt(offset, hour)
    status = "completed" if perf_dt < NOW else "scheduled"
    if performance_id in (SPECIAL["cancelled_1"], SPECIAL["cancelled_2"]):
        status = "cancelled"
    performances.append((performance_id, perf_dt.date(), perf_dt.time(), status, event_id, venue_id))

def replace_performance(performance_id, event_id, venue_id, days_from_today, hour, status):
    perf_dt = dt(days_from_today, hour)
    performances[performance_id - 1] = (performance_id, perf_dt.date(), perf_dt.time(), status, event_id, venue_id)
replace_performance(1, 3, 3, -220, 19, "completed")
replace_performance(2, 3, 3, -180, 14, "completed")
replace_performance(8, 1, 4, -90, 20, "cancelled")
replace_performance(19, 7, 7, -45, 19, "cancelled")
replace_performance(31, 15, 1, 30, 20, "scheduled")
replace_performance(32, 4, 2, 4, 20, "scheduled")
replace_performance(33, 20, 6, 60, 20, "scheduled")
performance_info = {r[0]: {"date": r[1], "time": r[2], "status": r[3], "event_id": r[4], "venue_id": r[5]} for r in performances}

tier_price_by_perf, section_tier_by_perf = {}, {}
for performance_id, perf_date, perf_time, status, event_id, venue_id in performances:
    base = 35 + event_id * 3 + venue_id * 2
    prices = {"Premium": round(base * 1.7, 2), "Standard": round(base, 2), "General": round(base * 0.8, 2)}
    if performance_id == 31:
        prices = {"Premium": 145.00, "Standard": 82.00, "General": 60.00}
    price_tiers.extend((performance_id, name, price) for name, price in prices.items())
    tier_price_by_perf[performance_id] = prices
    rs = reserved_section_names[venue_id]
    mapping = {rs[0]: "Premium", rs[1]: "Standard", ga_section_name[venue_id]: "General"}
    if venue_id == 1 and performance_id != 31:
        mapping = {rs[0]: "Standard", rs[1]: "Premium", ga_section_name[venue_id]: "General"}
    section_tier_by_perf[performance_id] = mapping
    for section_name, tier_name in mapping.items():
        assigned_to_tier.append((performance_id, venue_id, section_name, tier_name))

blocked_seats_by_perf = defaultdict(set)
for performance_id in [5, 12, 31, 40, 44]:
    venue_id = performance_info[performance_id]["venue_id"]
    section_name = reserved_section_names[venue_id][0]
    for index, (row_name, seat_num) in enumerate(seat_inventory[(venue_id, section_name)][:3]):
        blocked_seats_by_perf[performance_id].add((section_name, row_name, seat_num))
        data = (performance_id, section_name, venue_id, row_name, seat_num, dt(-20 + index, 10), ["Sightline obstruction", "Technical equipment", "Venue maintenance"][index])
        blocks.append(data)

# Orders and tickets
next_order_id = 1
next_ticket_id = 1
sold_reserved, sold_ga_count = defaultdict(set), defaultdict(int)
tickets_by_performance, tickets_by_customer = defaultdict(list), defaultdict(list)
ticket_data = {}

def perf_dt(pid):
    i = performance_info[pid]
    return datetime.combine(i["date"], i["time"])

def section_price(pid, section_name):
    return tier_price_by_perf[pid][section_tier_by_perf[pid][section_name]]

def available_reserved(pid, section_name):
    venue_id = performance_info[pid]["venue_id"]
    blocked = {(r, n) for s, r, n in blocked_seats_by_perf[pid] if s == section_name}
    return [(r, n) for r, n in seat_inventory[(venue_id, section_name)] if (r, n) not in sold_reserved[(pid, section_name)] and (r, n) not in blocked]

def create_order(customer_email, pid, requests, order_date=None):
    global next_order_id, next_ticket_id
    if order_date is None:
        latest = min(NOW - timedelta(days=1), perf_dt(pid) - timedelta(days=1))
        earliest = max(NOW - timedelta(days=365), latest - timedelta(days=180))
        order_date = earliest if latest <= earliest else earliest + timedelta(seconds=random.randint(0, max(1, int((latest - earliest).total_seconds()))))
    order_id = next_order_id
    next_order_id += 1
    orders.append((order_id, order_date, card_by_customer[customer_email], pid, customer_email))
    venue_id = performance_info[pid]["venue_id"]
    made = []
    for request in requests:
        section_name, seat = request["section"], request.get("seat")
        ticket_id = next_ticket_id
        next_ticket_id += 1
        face_value = section_price(pid, section_name)
        tickets.append((ticket_id, face_value, "active", order_id, venue_id, section_name))
        if seat:
            row_name, seat_num = seat
            sold_reserved[(pid, section_name)].add(seat)
            reserve_seats.append((ticket_id, section_name, venue_id, row_name, seat_num))
        else:
            sold_ga_count[(pid, section_name)] += 1
        owns_ticket.append((ticket_id, order_date, customer_email, None))
        tickets_by_performance[pid].append(ticket_id)
        tickets_by_customer[customer_email].append(ticket_id)
        ticket_data[ticket_id] = {"customer": customer_email, "performance_id": pid, "section": section_name, "face_value": face_value, "status": "active", "order_id": order_id}
        made.append(ticket_id)
    return made

def create_reserved_order(customer, pid, section_name, qty, order_date=None):
    avail = available_reserved(pid, section_name)
    return [] if len(avail) < qty else create_order(customer, pid, [{"section": section_name, "seat": seat} for seat in avail[:qty]], order_date)

def create_ga_order(customer, pid, qty, order_date=None):
    venue_id = performance_info[pid]["venue_id"]
    section_name = ga_section_name[venue_id]
    remaining = section_capacity[(venue_id, section_name)] - sold_ga_count[(pid, section_name)]
    qty = min(qty, remaining)
    return [] if qty <= 0 else create_order(customer, pid, [{"section": section_name, "seat": None} for _ in range(qty)], order_date)

# Sold out
pid = 1
venue_id = performance_info[pid]["venue_id"]
for section_name in reserved_section_names[venue_id]:
    while available_reserved(pid, section_name):
        create_reserved_order(random.choice(customer_emails), pid, section_name, min(4, len(available_reserved(pid, section_name))))
ga = ga_section_name[venue_id]
while sold_ga_count[(pid, ga)] < section_capacity[(venue_id, ga)]:
    create_ga_order(random.choice(customer_emails), pid, min(4, section_capacity[(venue_id, ga)] - sold_ga_count[(pid, ga)]))

# Low sales
pid = 2
venue_id = performance_info[pid]["venue_id"]
create_reserved_order(customer_emails[10], pid, reserved_section_names[venue_id][0], 4)
create_reserved_order(customer_emails[11], pid, reserved_section_names[venue_id][1], 3)
create_ga_order(customer_emails[12], pid, 3)

# Availability pattern
pid = 31
venue_id = performance_info[pid]["venue_id"]
orch = reserved_section_names[venue_id][0]
balc = reserved_section_names[venue_id][1]
row_a = [s for s in seat_inventory[(venue_id, orch)] if s[0] == "A" and s[1] not in {5, 6, 7, 8}]
for k in range(0, len(row_a), 4):
    create_order(customer_emails[(k // 4) % 20], pid, [{"section": orch, "seat": s} for s in row_a[k:k + 4]], NOW - timedelta(days=10))
row_b_even = [s for s in seat_inventory[(venue_id, orch)] if s[0] == "B" and s[1] % 2 == 0]
for k in range(0, len(row_b_even), 4):
    create_order(customer_emails[20 + (k // 4) % 20], pid, [{"section": orch, "seat": s} for s in row_b_even[k:k + 4]], NOW - timedelta(days=8))
create_reserved_order(customer_emails[1], pid, balc, 4, NOW - timedelta(days=7))
create_ga_order(customer_emails[2], pid, 8, NOW - timedelta(days=6))

# Less than seven days
pid = 32
venue_id = performance_info[pid]["venue_id"]
create_reserved_order(customer_emails[3], pid, reserved_section_names[venue_id][0], 4)
create_ga_order(customer_emails[4], pid, 5)

# Unsold tiers: only Premium sold
pid = 33
venue_id = performance_info[pid]["venue_id"]
create_reserved_order(customer_emails[5], pid, reserved_section_names[venue_id][0], 4)

# Two heavy buyers
for customer in customer_emails[:2]:
    for pid in [31, 32, 34, 35, 36, 37]:
        if len(tickets_by_customer[customer]) >= 12:
            break
        venue_id = performance_info[pid]["venue_id"]
        if not create_reserved_order(customer, pid, reserved_section_names[venue_id][0], 2):
            create_ga_order(customer, pid, 2)

eligible = [pid for pid, info in performance_info.items() if info["status"] != "cancelled"]
attempts = 0
while len(orders) < 300 or len(tickets) < 820:
    attempts += 1
    if attempts > 20000:
        raise RuntimeError("Could not generate enough orders and tickets.")
    pid = random.choice(eligible)
    venue_id = performance_info[pid]["venue_id"]
    customer = random.choice(customer_emails)
    qty = random.choice([1, 2, 2, 3, 3, 4])
    if random.random() < 0.75:
        section_name = random.choice(reserved_section_names[venue_id])
        if not create_reserved_order(customer, pid, section_name, qty):
            create_ga_order(customer, pid, qty)
    else:
        create_ga_order(customer, pid, qty)

# Cancellations

# Add sold tickets to the performances that organizers will later cancel.
# The purchases occurred before the organizer cancellation dates.
for pid in (8, 19):
    venue_id = performance_info[pid]["venue_id"]
    purchase_date = perf_dt(pid) - timedelta(days=45)

    create_reserved_order(
        customer_emails[25 + pid],
        pid,
        reserved_section_names[venue_id][0],
        3,
        purchase_date,
    )

    create_ga_order(
        customer_emails[35 + pid],
        pid,
        2,
        purchase_date,
    )


cancel_reasons = [
    "Travel plans changed.",
    "Scheduling conflict.",
    "Wrong performance date.",
    "Family commitment.",
    "Transportation issue.",
    "Duplicate purchase.",
]


def release_ticket_inventory(ticket_id):
    """Make a customer-cancelled seat available for purchase again."""
    info = ticket_data[ticket_id]
    pid = info["performance_id"]
    venue_id = performance_info[pid]["venue_id"]
    section_name = info["section"]

    reserved_row = next(
        (row for row in reserve_seats if row[0] == ticket_id),
        None,
    )

    if reserved_row is not None:
        _, _, _, row_name, seat_num = reserved_row
        sold_reserved[(pid, section_name)].discard((row_name, seat_num))
    else:
        sold_ga_count[(pid, section_name)] = max(
            0,
            sold_ga_count[(pid, section_name)] - 1,
        )


# Customer cancellations more than seven days before the performance:
# full refund.
refund_candidates = [
    ticket_id
    for ticket_id, info in ticket_data.items()
    if info["status"] == "active"
    and perf_dt(info["performance_id"]) > NOW + timedelta(days=7)
    and performance_info[info["performance_id"]]["status"] != "cancelled"
]

for index, ticket_id in enumerate(refund_candidates[:3]):
    info = ticket_data[ticket_id]
    cancelled_at = NOW - timedelta(days=3 - index)

    cancels_ticket.append(
        (
            ticket_id,
            info["customer"],
            info["face_value"],
            cancel_reasons[index],
            cancelled_at,
        )
    )

    info["status"] = "refunded"
    release_ticket_inventory(ticket_id)


# Customer cancellations within seven days:
# cancelled without a refund.
non_refund_candidates = [
    ticket_id
    for ticket_id, info in ticket_data.items()
    if info["status"] == "active"
    and NOW < perf_dt(info["performance_id"]) <= NOW + timedelta(days=7)
    and performance_info[info["performance_id"]]["status"] != "cancelled"
]

for index, ticket_id in enumerate(non_refund_candidates[:3]):
    info = ticket_data[ticket_id]
    cancelled_at = NOW - timedelta(hours=index + 1)

    cancels_ticket.append(
        (
            ticket_id,
            info["customer"],
            0.00,
            cancel_reasons[index + 3],
            cancelled_at,
        )
    )

    info["status"] = "cancelled"
    release_ticket_inventory(ticket_id)


# Organizer-cancelled performances:
# every sold ticket for the performance receives a full refund.
for pid in (8, 19):
    event_id = performance_info[pid]["event_id"]
    cancelled_at = perf_dt(pid) - timedelta(days=20)

    cancels_performance.append(
        (
            pid,
            event_by_id[event_id]["organizer"],
            "Cancelled by the organizer due to operational circumstances.",
            cancelled_at,
        )
    )

    for ticket_id in tickets_by_performance[pid]:
        if ticket_data[ticket_id]["status"] == "active":
            ticket_data[ticket_id]["status"] = "refunded"


# Update the actual Ticket rows
index_by_ticket = {
    row[0]: index
    for index, row in enumerate(tickets)
}

for ticket_id, info in ticket_data.items():
    if info["status"] in {"cancelled", "refunded"}:
        index = index_by_ticket[ticket_id]
        old = tickets[index]

        tickets[index] = (
            old[0],          # ticket_id
            old[1],          # face_value
            info["status"],  # cancelled or refunded
            old[3],          # order_id
            old[4],          # venue_id
            old[5],          # section_name
        )

# Resale and ownership
next_listing_id = 1

def close_owner(ticket_id, ended_at):
    for i in range(len(owns_ticket) - 1, -1, -1):
        row = owns_ticket[i]
        if row[0] == ticket_id and row[3] is None:
            owns_ticket[i] = (row[0], row[1], row[2], ended_at)
            return
    raise RuntimeError("No current owner found")

def create_listing(ticket_id, seller, status, listed_at, price, buyer=None, purchased_at=None):
    global next_listing_id
    listing_id = next_listing_id
    next_listing_id += 1
    resale_listings.append((listing_id, price, listed_at, status, ticket_id, seller))
    if status == "sold":
        purchase_listings.append((listing_id, buyer, purchased_at, price))
        close_owner(ticket_id, purchased_at)
        owns_ticket.append((ticket_id, purchased_at, buyer, None))
        ticket_data[ticket_id]["customer"] = buyer
    return listing_id

for heavy in customer_emails[:2]:
    owned = list(tickets_by_customer[heavy])[:12]
    for index, ticket_id in enumerate(owned[:7]):
        info = ticket_data[ticket_id]
        event_id = performance_info[info["performance_id"]]["event_id"]
        price = round(info["face_value"] * event_by_id[event_id]["cap"], 2) if index == 0 else round(info["face_value"] * 1.05, 2)
        listed_at = NOW - timedelta(days=40 - index)
        if index < 3:
            buyer = customer_emails[40 + index + (0 if heavy == customer_emails[0] else 5)]
            create_listing(ticket_id, heavy, "sold", listed_at, price, buyer, listed_at + timedelta(days=2))
        elif index < 5:
            create_listing(ticket_id, heavy, "withdrawn", listed_at, price)
        else:
            create_listing(ticket_id, heavy, "active", listed_at, price)

# One ticket changes owners twice
twice_ticket = next(t for t in tickets_by_customer[customer_emails[0]] if ticket_data[t]["status"] == "active")
first_seller = ticket_data[twice_ticket]["customer"]
event_id = performance_info[ticket_data[twice_ticket]["performance_id"]]["event_id"]
cap_price = round(ticket_data[twice_ticket]["face_value"] * event_by_id[event_id]["cap"], 2)
create_listing(twice_ticket, first_seller, "sold", NOW - timedelta(days=20), cap_price, customer_emails[60], NOW - timedelta(days=18))
create_listing(twice_ticket, customer_emails[60], "sold", NOW - timedelta(days=10), round(ticket_data[twice_ticket]["face_value"] * 1.02, 2), customer_emails[61], NOW - timedelta(days=7))

extra = [t for t, info in ticket_data.items() if info["status"] == "active" and perf_dt(info["performance_id"]) > NOW][:20]
for index, ticket_id in enumerate(extra):
    seller = ticket_data[ticket_id]["customer"]
    listed_at = NOW - timedelta(days=15 - index % 10)
    status = ["active", "withdrawn", "sold"][index % 3]
    price = round(ticket_data[ticket_id]["face_value"] * 1.08, 2)
    if status == "sold":
        buyer = customer_emails[70 + index % 20]
        if buyer == seller:
            buyer = customer_emails[(71 + index) % 100]
        create_listing(ticket_id, seller, status, listed_at, price, buyer, listed_at + timedelta(days=1))
    else:
        create_listing(ticket_id, seller, status, listed_at, price)

# Reviews
comments = [
    "The performance had excellent pacing and the lead artist connected well with the audience. The sound balance was clear throughout the evening.",
    "The venue staff were organized and helpful from entry to departure. The seating area was comfortable and the sightlines were better than expected.",
    "The show was energetic and the supporting artists added real variety. The final set was the strongest part of the performance.",
    "The production design was creative and the lighting supported the mood of each scene. The venue could improve the concession lines.",
    "The event was enjoyable and professionally managed. The audience atmosphere made the experience feel lively without becoming disruptive.",
    "The performers delivered a polished show with several memorable moments. The venue was easy to reach and the entrances were clearly marked.",
]
events_reviewed, reviewed_pairs = set(), set()
for pid in [p for p, info in performance_info.items() if perf_dt(p) < NOW and info["status"] == "completed"]:
    event_id = performance_info[pid]["event_id"]
    if len(events_reviewed) >= 10 and event_id not in events_reviewed:
        continue
    reviewers = []
    for ticket_id in tickets_by_performance[pid]:
        reviewer = ticket_data[ticket_id]["customer"]
        if (reviewer, pid) not in reviewed_pairs and reviewer not in reviewers:
            reviewers.append(reviewer)
        if len(reviewers) == 3:
            break
    for index, reviewer in enumerate(reviewers):
        reviews.append((reviewer, pid, 3 + ((pid + index) % 3), 3 + ((pid + index + 1) % 3), comments[(pid + index) % len(comments)], perf_dt(pid) + timedelta(days=2 + index, hours=3)))
        reviewed_pairs.add((reviewer, pid))
    if reviewers:
        events_reviewed.add(event_id)
    if len(events_reviewed) >= 10 and len(reviews) >= 30:
        break

# Validation
ticket_statuses = {row[2] for row in tickets}

assert {"active", "cancelled", "refunded"} <= ticket_statuses
assert len(refund_candidates) >= 3
assert len(non_refund_candidates) >= 3

for pid in (8, 19):
    assert len(tickets_by_performance[pid]) > 0
    assert all(
        ticket_data[ticket_id]["status"] == "refunded"
        for ticket_id in tickets_by_performance[pid]
    )
assert sum(1 for row in tickets if row[2] == "refunded") >= 3
assert len(venues) >= 8 and len({v[6] for v in venues}) >= 4 and len({v[7] for v in venues}) >= 2
assert len(events) >= 20 and len({e[1] for e in events}) >= 5 and len(artists) >= 15
assert len(performances) >= 60 and len(customers) >= 100 and len(orders) >= 300 and len(tickets) >= 800
assert len(cancels_ticket) >= 4 and len(cancels_performance) >= 2
assert {r[3] for r in resale_listings} >= {"sold", "withdrawn", "active"}
assert sum(1 for r in owns_ticket if r[0] == twice_ticket) >= 3
assert len(events_reviewed) >= 10 and len(reviews) >= 20

# Output
write_rows("User.txt", users)
write_rows("Customer.txt", customers)
write_rows("Organizer.txt", organizers)
write_rows("PaymentInformation.txt", payment_information)
write_rows("Venue.txt", venues)
write_rows("Section.txt", sections)
write_rows("GeneralAdmissionSection.txt", ga_sections)
write_rows("SeatRow.txt", seat_rows)
write_rows("Seat.txt", seats)
write_rows("SegGenre.txt", seg_genres)
write_rows("Artist.txt", artists)
write_rows("Event.txt", events)
write_rows("Features.txt", features)
write_rows("BelongsTo.txt", belongs_to)
write_rows("Performance.txt", performances)
write_rows("PriceTier.txt", price_tiers)
write_rows("AssignedToTier.txt", assigned_to_tier)
write_rows("Blocks.txt", blocks)
write_rows("CustomerOrder.txt", orders)
write_rows("Ticket.txt", tickets)
write_rows("ReserveSeat.txt", reserve_seats)
write_rows("OwnsTicket.txt", owns_ticket)
write_rows("ResaleListing.txt", resale_listings)
write_rows("PurchaseListing.txt", purchase_listings)
write_rows("Reviews.txt", reviews)
write_rows("CancelsTicket.txt", cancels_ticket)
write_rows("CancelsPerformance.txt", cancels_performance)

print("Validation passed.")
print(f"Generated {len(users)} users, {len(events)} events, {len(performances)} performances, {len(orders)} orders, and {len(tickets)} tickets.")
print(f"Files written to: {DATA_DIR.resolve()}")