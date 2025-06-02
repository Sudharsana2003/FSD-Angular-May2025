---------------------------------------COZY HAVEN STAY APP - HOTEL BOOKING MANAGEMENT APPLICATION (SUDHARSANA I - BATCH 1)-----------------------------------

--1.Users Table

CREATE TABLE USERS (
    USER_ID             NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    USERNAME            VARCHAR2(100) NOT NULL UNIQUE,
    EMAIL               VARCHAR2(255) NOT NULL UNIQUE,
    PASSWORD_HASH       VARCHAR2(512) NOT NULL,
    FIRST_NAME          VARCHAR2(100) NOT NULL,
    LAST_NAME           VARCHAR2(100) NOT NULL,
    FULL_NAME           VARCHAR2(255) GENERATED ALWAYS AS (FIRST_NAME || ' ' || LAST_NAME) VIRTUAL,
    --virtual means seperate table without space if needed on fly it takes up - calculated automatically from other columns whenever you query it.
    GENDER              VARCHAR2(10),
    COUNTRY_CODE        VARCHAR2(5) NOT NULL,
    LOCAL_PHONE_NUMBER  VARCHAR2(20) NOT NULL UNIQUE,
    ADDRESS             VARCHAR2(500),
    USER_TYPE           VARCHAR2(20) NOT NULL,
    REGISTRATION_DATE   TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    LAST_LOGIN_DATE     TIMESTAMP,
    IS_ACTIVE           NUMBER(1) DEFAULT 1 NOT NULL,

    -- Constraints
    CONSTRAINT chk_gender CHECK (GENDER IN ('Male', 'Female', 'Other')),
    CONSTRAINT chk_user_type CHECK (USER_TYPE IN ('GUEST', 'HOTEL_OWNER', 'ADMIN')),
    CONSTRAINT chk_local_phone_format CHECK (REGEXP_LIKE(LOCAL_PHONE_NUMBER, '^\d{7,15}$')),
    CONSTRAINT chk_country_code_format CHECK (REGEXP_LIKE(COUNTRY_CODE, '^\+\d{1,4}$')),
    CONSTRAINT chk_email_format CHECK (REGEXP_LIKE(EMAIL, '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'))
);

----------------------1. USERS RECORDS INSERTION--------------------------------
INSERT INTO USERS (USERNAME, EMAIL, PASSWORD_HASH, FIRST_NAME, LAST_NAME, GENDER, COUNTRY_CODE, LOCAL_PHONE_NUMBER, ADDRESS, USER_TYPE) VALUES
('rahul.kumar', 'rahul.kumar@example.in', 'rahul123', 'Rahul', 'Kumar', 'Male', '+91', '9876543210', '123 MG Road, Bangalore, Karnataka', 'GUEST'),

('anita.sharma', 'anita.sharma@example.in', 'anita123', 'Anita', 'Sharma', 'Female', '+91', '9123456789', '45 Sector 17, Chandigarh, Punjab', 'HOTEL_OWNER'),

('arjun.patel', 'arjun.patel@example.in', 'arjun123', 'Arjun', 'Patel', 'Male', '+91', '9988776655', '89 Marine Drive, Mumbai, Maharashtra', 'GUEST'),

('meera.nair', 'meera.nair@example.in', 'meera123', 'Meera', 'Nair', 'Female', '+91', '9012345678', '23 MG Road, Kochi, Kerala', 'ADMIN'),

('sanjay.singh', 'sanjay.singh@example.in', 'sanjay123', 'Sanjay', 'Singh', 'Male', '+91', '9876123456', '56 Park Street, Kolkata, West Bengal', 'HOTEL_OWNER'),

('oliver.smith', 'oliver.smith@example.co.uk', 'oliver123', 'Oliver', 'Smith', 'Male', '+44', '7700123456', '12 Baker Street, London, England', 'GUEST'),

('emily.jones', 'emily.jones@example.co.uk', 'emily123', 'Emily', 'Jones', 'Female', '+44', '7911123456', '45 Queen Road, Manchester, England', 'HOTEL_OWNER'),

('jack.brown', 'jack.brown@example.co.uk', 'jack123', 'Jack', 'Brown', 'Male', '+44', '7800123456', '78 Princes Street, Edinburgh, Scotland', 'GUEST'),

('sophia.wilson', 'sophia.wilson@example.co.uk', 'sophia123', 'Sophia', 'Wilson', 'Female', '+44', '7722123456', '34 Castle Road, Cardiff, Wales', 'ADMIN'),

('harry.taylor', 'harry.taylor@example.co.uk', 'harry123', 'Harry', 'Taylor', 'Male', '+44', '7733123456', '89 King Street, Belfast, Northern Ireland', 'HOTEL_OWNER');






--2.Hotels Table

CREATE TABLE HOTELS (
    HOTEL_ID                     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
    HOTEL_NAME                   VARCHAR2(255) NOT NULL,
    LOCATION                     VARCHAR2(255) NOT NULL,
    ADDRESS                      VARCHAR2(500) NOT NULL,
    DESCRIPTION                  CLOB,
    OWNER_USER_ID                NUMBER NOT NULL, 
    CONTACT_COUNTRY_CODE         VARCHAR2(5) NOT NULL,
    CONTACT_LOCAL_PHONE_NUMBER   VARCHAR2(20) NOT NULL,
    CONTACT_EMAIL                VARCHAR2(255),
    IS_ACTIVE                    NUMBER(1) DEFAULT 1 NOT NULL,
    CREATED_AT                   TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATED_AT                   TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL, 

    CONSTRAINT fk_owner FOREIGN KEY (OWNER_USER_ID) REFERENCES USERS(USER_ID),
    CONSTRAINT uq_hotel_name_loc UNIQUE (HOTEL_NAME, LOCATION),
    CONSTRAINT chk_hotel_contact_cc CHECK (REGEXP_LIKE(CONTACT_COUNTRY_CODE, '^\+\d{1,4}$')),
    CONSTRAINT chk_hotel_contact_phone CHECK (REGEXP_LIKE(CONTACT_LOCAL_PHONE_NUMBER, '^\d{7,15}$')),
    CONSTRAINT chk_hotel_contact_email CHECK (CONTACT_EMAIL IS NULL OR REGEXP_LIKE(CONTACT_EMAIL, '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'))
);

-- Trigger to automatically update the UPDATED_AT timestamp for HOTELS
CREATE OR REPLACE TRIGGER trg_hotels_upd_date
BEFORE UPDATE ON HOTELS
FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := SYSTIMESTAMP;
END;
/

----------------------1. HOTELS RECORDS INSERTION--------------------------------
INSERT INTO HOTELS (HOTEL_NAME, LOCATION, ADDRESS, DESCRIPTION, OWNER_USER_ID, CONTACT_COUNTRY_CODE, CONTACT_LOCAL_PHONE_NUMBER, CONTACT_EMAIL)
VALUES
('The Grand Palace', 'Mumbai', '123 Marine Drive, Mumbai, Maharashtra', 'Luxury hotel with sea view rooms and rooftop restaurant.', 2, '+91', '9988776655', 'contact@grandpalace.in'),

('Royal Residency', 'Bangalore', '45 MG Road, Bangalore, Karnataka', 'Comfortable stay in the heart of the city.', 2, '+91', '9123456789', 'info@royalresidency.in'),

('Sunset Inn', 'London', '12 Baker Street, London, England', 'Cozy inn close to major attractions.', 6, '+44', '7700123456', 'contact@sunsetinn.co.uk'),

('Highland Retreat', 'Edinburgh', '78 Princes Street, Edinburgh, Scotland', 'A peaceful retreat in the scenic highlands.', 7, '+44', '7800123456', 'stay@highlandretreat.co.uk'),

('City Lights Hotel', 'Chennai', '89 Anna Salai, Chennai, Tamil Nadu', 'Modern amenities with great city views.', 2, '+91', '9876543210', 'reservations@citylights.in'),

('Seaside Resort', 'Goa', '23 Beach Road, Panaji, Goa', 'Relaxing resort near the beach.', 3, '+91', '9876123456', 'contact@seasideresort.in'),

('The Royal Crown', 'Manchester', '45 Queen Road, Manchester, England', 'Elegant hotel with historic charm.', 8, '+44', '7911123456', 'info@royalcrown.co.uk'),

('Garden View', 'Kolkata', '56 Park Street, Kolkata, West Bengal', 'Boutique hotel surrounded by gardens.', 5, '+91', '9876123456', 'hello@gardenview.in'),

('Lakeview Hotel', 'Cardiff', '34 Castle Road, Cardiff, Wales', 'Hotel with a beautiful lake view.', 9, '+44', '7722123456', 'stay@lakeview.co.uk'),

('Hilltop Inn', 'Belfast', '89 King Street, Belfast, Northern Ireland', 'Cozy inn atop a hill with panoramic views.', 10, '+44', '7733123456', 'info@hilltopinn.co.uk');



--3.Room_Types Table

CREATE TABLE ROOM_TYPES (
    ROOM_TYPE_ID            NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    TYPE_NAME               VARCHAR2(50) NOT NULL UNIQUE,
    DEFAULT_ROOM_SIZE_SQM   NUMBER(10,2),
    DEFAULT_ROOM_SIZE_SQFT  NUMBER(10,2),
    DEFAULT_BED_PREFERENCE  VARCHAR2(50),
    DEFAULT_MAX_ADULTS      NUMBER(2),
    DEFAULT_MAX_CHILDREN    NUMBER(2),
    DEFAULT_BASE_FARE       NUMBER(10,2) NOT NULL,
    DESCRIPTION             VARCHAR2(500),
    
    CONSTRAINT chk_default_bed_preference CHECK (DEFAULT_BED_PREFERENCE IN ('SINGLE', 'DOUBLE', 'KING', 'QUEEN')),
    CONSTRAINT chk_default_fare_positive CHECK (DEFAULT_BASE_FARE > 0)
);


----------------------3. ROOM_TYPES RECORDS INSERTION--------------------------------
INSERT INTO ROOM_TYPES (TYPE_NAME, DEFAULT_ROOM_SIZE_SQM, DEFAULT_ROOM_SIZE_SQFT, DEFAULT_BED_PREFERENCE, DEFAULT_MAX_ADULTS, DEFAULT_MAX_CHILDREN, DEFAULT_BASE_FARE, DESCRIPTION)
VALUES 
('Standard Single', 20.00, 215.28, 'SINGLE', 1, 0, 2000.00, 'Ideal for solo travelers. Compact room with basic amenities.'),

('Standard Double', 25.00, 269.10, 'DOUBLE', 2, 1, 3000.00, 'Comfortable for two adults. Includes TV, AC, and Wi-Fi.'),

('Deluxe King', 35.00, 376.74, 'KING', 2, 2, 4500.00, 'Spacious room with a king-size bed and city view.'),

('Queen Room', 28.00, 301.39, 'QUEEN', 2, 1, 4000.00, 'Queen bed with modern interiors and garden view.'),

('Family Suite', 50.00, 538.20, 'KING', 4, 2, 7000.00, 'Perfect for families. Separate sleeping and living areas.'),

('Luxury Suite', 60.00, 645.83, 'KING', 3, 2, 9000.00, 'Premium suite with bath tub, balcony and minibar.'),

('Economy Room', 18.00, 193.75, 'SINGLE', 1, 1, 1500.00, 'Budget-friendly room for short stays.'),

('Executive Room', 30.00, 322.92, 'DOUBLE', 2, 1, 5000.00, 'For business travelers. Includes work desk and faster Wi-Fi.'),

('Penthouse Suite', 80.00, 861.11, 'KING', 4, 2, 12000.00, 'Top-floor luxury with private terrace and premium service.'),

('Twin Bed Room', 26.00, 279.86, 'DOUBLE', 2, 1, 3200.00, 'Two separate beds ideal for friends or colleagues.');





--4.Rooms Table

CREATE TABLE ROOMS (
    ROOM_ID                     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
    HOTEL_ID                    NUMBER NOT NULL, 
    ROOM_NUMBER                 VARCHAR2(20) NOT NULL,
    ROOM_TYPE_ID                NUMBER NOT NULL, 
    ROOM_SIZE_SQM               NUMBER(10,2),
    ROOM_SIZE_SQFT              NUMBER(10,2),
    BED_PREFERENCE              VARCHAR2(50) NOT NULL,
    MAX_PEOPLE                  NUMBER(2) NOT NULL,
    BASE_FARE_PER_NIGHT         NUMBER(10,2) NOT NULL,
    IS_AC                       NUMBER(1) DEFAULT 0 NOT NULL,
    IS_AVAILABLE                NUMBER(1) DEFAULT 1 NOT NULL,
    
    CONSTRAINT fk_room_hotel FOREIGN KEY (HOTEL_ID) REFERENCES HOTELS(HOTEL_ID),
    CONSTRAINT fk_room_type FOREIGN KEY (ROOM_TYPE_ID) REFERENCES ROOM_TYPES(ROOM_TYPE_ID),
    
    CONSTRAINT uq_hotel_room_number UNIQUE (HOTEL_ID, ROOM_NUMBER),
    CONSTRAINT chk_room_bed_preference CHECK (BED_PREFERENCE IN ('SINGLE', 'DOUBLE', 'KING', 'QUEEN')),
    CONSTRAINT chk_room_fare_positive CHECK (BASE_FARE_PER_NIGHT >= 0)

);

----------------------4. ROOMS RECORDS INSERTION--------------------------------
INSERT INTO ROOMS (HOTEL_ID, ROOM_NUMBER, ROOM_TYPE_ID, ROOM_SIZE_SQM, ROOM_SIZE_SQFT, BED_PREFERENCE, MAX_PEOPLE, BASE_FARE_PER_NIGHT, IS_AC, IS_AVAILABLE) VALUES
(1, '101', 1, 20.00, 215.28, 'SINGLE', 1, 1500.00, 1, 1),
(1, '102', 2, 28.00, 301.00, 'DOUBLE', 2, 2500.00, 1, 1),
(1, '103', 3, 35.00, 376.74, 'KING', 3, 4000.00, 1, 0),
(2, '201', 4, 25.00, 269.10, 'QUEEN', 3, 3000.00, 1, 1),
(2, '202', 5, 40.00, 430.56, 'KING', 4, 5000.00, 1, 1),
(3, '301', 6, 50.00, 538.20, 'KING', 4, 6000.00, 1, 0),
(3, '302', 7, 18.00, 193.75, 'SINGLE', 1, 1800.00, 0, 1),
(4, '401', 8, 30.00, 322.92, 'DOUBLE', 3, 3500.00, 1, 1),
(5, '501', 9, 60.00, 645.83, 'KING', 5, 7000.00, 1, 1),
(5, '502', 10, 26.00, 279.86, 'DOUBLE', 3, 3200.00, 0, 1);




--5.Bookings Table

CREATE TABLE BOOKINGS (
    BOOKING_ID            NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    USER_ID               NUMBER NOT NULL,
    HOTEL_ID              NUMBER NOT NULL, 
    CHECK_IN_DATE         DATE NOT NULL,
    CHECK_OUT_DATE        DATE NOT NULL,
    NUMBER_OF_ADULTS      NUMBER(2) DEFAULT 1 NOT NULL,
    NUMBER_OF_CHILDREN    NUMBER(2) DEFAULT 0 NOT NULL,
    TOTAL_GUESTS          NUMBER(2) NOT NULL, 
    TOTAL_FARE            NUMBER(12,2) NOT NULL,
    BOOKING_DATE          TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    BOOKING_STATUS        VARCHAR2(20) DEFAULT 'CONFIRMED' NOT NULL,
    PAYMENT_ID            VARCHAR2(36) UNIQUE, 
    CANCELLATION_DATE     TIMESTAMP,
    REFUND_AMOUNT         NUMBER(12,2),
    CREATED_AT            TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATED_AT            TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL, 

    CONSTRAINT fk_booking_user FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID),
    CONSTRAINT fk_booking_hotel FOREIGN KEY (HOTEL_ID) REFERENCES HOTELS(HOTEL_ID),

    CONSTRAINT chk_booking_dates CHECK (CHECK_OUT_DATE > CHECK_IN_DATE),
    CONSTRAINT chk_booking_status CHECK (BOOKING_STATUS IN ('CONFIRMED', 'CANCELLED', 'COMPLETED', 'PENDING_PAYMENT')),
    CONSTRAINT chk_guests_positive CHECK (NUMBER_OF_ADULTS >= 0 AND NUMBER_OF_CHILDREN >= 0)
);

-- Trigger to automatically calculate TOTAL_GUESTS for BOOKINGS
CREATE OR REPLACE TRIGGER trg_booking_total_guests
BEFORE INSERT OR UPDATE ON BOOKINGS
FOR EACH ROW
BEGIN
    :NEW.TOTAL_GUESTS := NVL(:NEW.NUMBER_OF_ADULTS, 0) + NVL(:NEW.NUMBER_OF_CHILDREN, 0);
END;
/

-- Trigger to automatically update the UPDATED_AT timestamp for BOOKINGS
CREATE OR REPLACE TRIGGER trg_bookings_upd_date
BEFORE UPDATE ON BOOKINGS
FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := SYSTIMESTAMP;
END;
/

---------------------------------5. BOOKINGS-----------------------------------

INSERT INTO BOOKINGS (USER_ID, HOTEL_ID, CHECK_IN_DATE, CHECK_OUT_DATE, NUMBER_OF_ADULTS, NUMBER_OF_CHILDREN, TOTAL_GUESTS, TOTAL_FARE, PAYMENT_ID, BOOKING_STATUS)
VALUES
(1, 1, DATE '2025-06-10', DATE '2025-06-12', 2, 1, 3, 4800.00, 'PAY123456A', 'CONFIRMED'),

(2, 2, DATE '2025-07-01', DATE '2025-07-05', 1, 0, 1, 7000.00, 'PAY223456B', 'CONFIRMED'),

(3, 1, DATE '2025-06-20', DATE '2025-06-21', 2, 2, 4, 3200.00, 'PAY323456C', 'PENDING_PAYMENT'),

(4, 3, DATE '2025-06-15', DATE '2025-06-18', 3, 1, 4, 10200.00, 'PAY423456D', 'COMPLETED'),

(5, 4, DATE '2025-06-05', DATE '2025-06-06', 1, 0, 1, 1500.00, 'PAY523456E', 'CANCELLED'),

(1, 2, DATE '2025-08-01', DATE '2025-08-04', 2, 1, 3, 7200.00, 'PAY623456F', 'CONFIRMED'),

(2, 3, DATE '2025-06-25', DATE '2025-06-26', 1, 1, 2, 2800.00, 'PAY723456G', 'COMPLETED'),

(3, 5, DATE '2025-09-10', DATE '2025-09-12', 2, 2, 4, 9500.00, 'PAY823456H', 'CONFIRMED'),

(4, 5, DATE '2025-07-15', DATE '2025-07-16', 1, 0, 1, 1800.00, 'PAY923456I', 'CONFIRMED'),

(5, 1, DATE '2025-06-22', DATE '2025-06-23', 2, 0, 2, 3600.00, 'PAY103456J', 'CANCELLED');





--6.Booked_Room_Details Table

--Junction for Multi-Room Bookings
CREATE TABLE BOOKED_ROOM_DETAILS (
    BOOKED_ROOM_ID        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    BOOKING_ID            NUMBER NOT NULL, 
    ROOM_ID               NUMBER NOT NULL, 
    HOTEL_ID              NUMBER NOT NULL, 
    FARE_AT_BOOKING       NUMBER(10,2) NOT NULL, 
    CONSTRAINT fk_booked_room_booking FOREIGN KEY (BOOKING_ID) REFERENCES BOOKINGS(BOOKING_ID),
    CONSTRAINT fk_booked_room_room FOREIGN KEY (ROOM_ID) REFERENCES ROOMS(ROOM_ID),
    CONSTRAINT fk_booked_room_hotel FOREIGN KEY (HOTEL_ID) REFERENCES HOTELS(HOTEL_ID),

    CONSTRAINT uq_booking_room UNIQUE (BOOKING_ID, ROOM_ID) 
);


---------------------------6. BOOKED_ROOM_DETAILS-------------------------------

INSERT INTO BOOKED_ROOM_DETAILS (BOOKING_ID, ROOM_ID, HOTEL_ID, FARE_AT_BOOKING) VALUES
(1, 1, 1, 2400.00),
(1, 2, 1, 2400.00),
(2, 3, 2, 7000.00),
(3, 4, 1, 3200.00),
(4, 5, 3, 5100.00),
(4, 6, 3, 5100.00),
(5, 7, 4, 1500.00),
(6, 8, 2, 3600.00),
(6, 9, 2, 3600.00),
(7, 10, 3, 2800.00);



--7.Amenities Table

CREATE TABLE AMENITIES (
    AMENITY_ID     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    AMENITY_NAME   VARCHAR2(100) NOT NULL UNIQUE,
    DESCRIPTION    VARCHAR2(500)
);

---------------------------7. AMENITIES-----------------------------------------
INSERT INTO AMENITIES (AMENITY_NAME, DESCRIPTION) VALUES
('Free Wi-Fi', 'High-speed wireless internet access throughout the hotel'),
('Swimming Pool', 'Outdoor/Indoor pool available for guest use'),
('Fitness Center', 'Gym with cardio and weight training equipment'),
('Airport Shuttle', 'Complimentary shuttle service to and from the airport'),
('Room Service', '24/7 in-room food and beverage service'),
('Spa Services', 'Massage and wellness treatments available'),
('Parking', 'Free on-site or valet parking for guests'),
('Business Center', 'Workstations, printers, and conference rooms'),
('Pet Friendly', 'Hotel allows guests to bring pets'),
('Restaurant', 'On-site dining with breakfast, lunch, and dinner options');



--8.Hotel_Amenities Table

-- Junction Table Many-to-many relationship between HOTELS and AMENITIES
CREATE TABLE HOTEL_AMENITIES (
    HOTEL_ID      NUMBER NOT NULL,
    AMENITY_ID    NUMBER NOT NULL,

    PRIMARY KEY (HOTEL_ID, AMENITY_ID),

    CONSTRAINT fk_ha_hotel FOREIGN KEY (HOTEL_ID) REFERENCES HOTELS(HOTEL_ID),
    CONSTRAINT fk_ha_amenity FOREIGN KEY (AMENITY_ID) REFERENCES AMENITIES(AMENITY_ID)
);


---------------------------8. HOTEL_AMENITIES-----------------------------------

INSERT INTO HOTEL_AMENITIES (HOTEL_ID, AMENITY_ID) VALUES (1, 1);
INSERT INTO HOTEL_AMENITIES (HOTEL_ID, AMENITY_ID) VALUES (1, 7);
INSERT INTO HOTEL_AMENITIES (HOTEL_ID, AMENITY_ID) VALUES (1, 10);
INSERT INTO HOTEL_AMENITIES (HOTEL_ID, AMENITY_ID) VALUES (2, 1);
INSERT INTO HOTEL_AMENITIES (HOTEL_ID, AMENITY_ID) VALUES (2, 2);
INSERT INTO HOTEL_AMENITIES (HOTEL_ID, AMENITY_ID) VALUES (2, 3);
INSERT INTO HOTEL_AMENITIES (HOTEL_ID, AMENITY_ID) VALUES (2, 6);
INSERT INTO HOTEL_AMENITIES (HOTEL_ID, AMENITY_ID) VALUES (3, 1);
INSERT INTO HOTEL_AMENITIES (HOTEL_ID, AMENITY_ID) VALUES (3, 5);
INSERT INTO HOTEL_AMENITIES (HOTEL_ID, AMENITY_ID) VALUES (3, 9);


--9.Payments Table


CREATE TABLE PAYMENTS (
    PAYMENT_ID              NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
    BOOKING_ID              NUMBER NOT NULL,                                 
    USER_ID                 NUMBER NOT NULL,                                 
    AMOUNT                  NUMBER(12,2) NOT NULL,                           
    CURRENCY                VARCHAR2(5) DEFAULT 'INR' NOT NULL,              
    PAYMENT_METHOD          VARCHAR2(50) NOT NULL,                           
    GATEWAY_TRANSACTION_ID  VARCHAR2(100) UNIQUE,                            
    PAYMENT_STATUS          VARCHAR2(20) NOT NULL,                           
    PAYMENT_DATE            TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,         
    CARD_LAST_FOUR_DIGITS   VARCHAR2(4),                                     
    REFUNDED_AMOUNT         NUMBER(12,2) DEFAULT 0.0,                        
    CREATED_AT              TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,         
    UPDATED_AT              TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,        

    CONSTRAINT fk_payment_booking FOREIGN KEY (BOOKING_ID) REFERENCES BOOKINGS(BOOKING_ID),
    CONSTRAINT fk_payment_user FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID), -- RE-ADDED FK

    CONSTRAINT chk_payment_status CHECK (PAYMENT_STATUS IN ('SUCCESS', 'FAILED', 'PENDING', 'REFUNDED', 'AUTHORIZED', 'VOIDED')), -- CORRECTED: More statuses
    CONSTRAINT chk_payment_amount_positive CHECK (AMOUNT > 0), -- RE-ADDED
    CONSTRAINT chk_refund_amount_valid CHECK (REFUNDED_AMOUNT >= 0 AND REFUNDED_AMOUNT <= AMOUNT) -- RE-ADDED
);

-- Trigger to automatically update the UPDATED_AT timestamp for PAYMENTS
CREATE OR REPLACE TRIGGER trg_payments_upd_date
BEFORE UPDATE ON PAYMENTS
FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := SYSTIMESTAMP;
END;
/

-----------------------------9. PAYMENTS ---------------------------------------

INSERT INTO PAYMENTS (
    BOOKING_ID, USER_ID, AMOUNT, CURRENCY, PAYMENT_METHOD,
    GATEWAY_TRANSACTION_ID, PAYMENT_STATUS, CARD_LAST_FOUR_DIGITS, REFUNDED_AMOUNT) VALUES
    (1, 1, 4500.00, 'INR', 'Credit Card', 'TXN1234567890', 'SUCCESS', '1234', 0.00),
    (2, 2, 7200.00, 'INR', 'Debit Card', 'TXN1234567891', 'SUCCESS', '5678', 0.00),
    (3, 3, 3500.00, 'INR', 'UPI', 'TXN1234567892', 'SUCCESS', NULL, 0.00),
    (4, 4, 5200.00, 'INR', 'Credit Card', 'TXN1234567893', 'FAILED', '4321', 0.00),
    (5, 5, 6000.00, 'INR', 'Net Banking', 'TXN1234567894', 'PENDING', NULL, 0.00),
    (6, 6, 4800.00, 'INR', 'Credit Card', 'TXN1234567895', 'REFUNDED', '7890', 4800.00),
    (7, 7, 7000.00, 'INR', 'Debit Card', 'TXN1234567896', 'AUTHORIZED', '3456', 0.00),
    (8, 8, 3900.00, 'INR', 'UPI', 'TXN1234567897', 'VOIDED', NULL, 0.00),
    (9, 9, 4500.00, 'INR', 'Credit Card', 'TXN1234567898', 'SUCCESS', '6543', 0.00),
    (10,10, 5600.00, 'INR', 'Net Banking', 'TXN1234567899', 'SUCCESS', NULL, 0.00);


--10.Reviews Table

CREATE TABLE REVIEWS (
    REVIEW_ID       NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    USER_ID         NUMBER NOT NULL, 
    HOTEL_ID        NUMBER NOT NULL, 
    BOOKING_ID      NUMBER,          
    RATING          NUMBER(1) NOT NULL,
    COMMENT_TEXT    CLOB,
    REVIEW_DATE     TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    IS_ACTIVE       NUMBER(1) DEFAULT 1 NOT NULL,
    CREATED_AT      TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATED_AT      TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

    CONSTRAINT fk_review_user FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID),
    CONSTRAINT fk_review_hotel FOREIGN KEY (HOTEL_ID) REFERENCES HOTELS(HOTEL_ID),
    CONSTRAINT fk_review_booking FOREIGN KEY (BOOKING_ID) REFERENCES BOOKINGS(BOOKING_ID),

    CONSTRAINT chk_review_rating CHECK (RATING BETWEEN 1 AND 5),
    CONSTRAINT uq_user_booking_review UNIQUE (USER_ID, BOOKING_ID)
);


-- Trigger to automatically update the UPDATED_AT timestamp for REVIEWS
CREATE OR REPLACE TRIGGER trg_reviews_upd_date
BEFORE UPDATE ON REVIEWS
FOR EACH ROW
BEGIN
    :NEW.UPDATED_AT := SYSTIMESTAMP;
END;
/


-----------------------------10. REVIEWS ---------------------------------------

INSERT INTO REVIEWS (
    USER_ID, HOTEL_ID, BOOKING_ID, RATING, COMMENT_TEXT, IS_ACTIVE
) VALUES
    (1, 1, 1, 5, 'Excellent stay, very comfortable and clean.', 1),
    (2, 2, 2, 4, 'Good service, but room was a bit small.', 1),
    (3, 1, 3, 3, 'Average experience, expected better amenities.', 1),
    (4, 3, 4, 2, 'Not satisfied with the cleanliness.', 1),
    (5, 2, 5, 5, 'Great location and friendly staff.', 1),
    (6, 4, 6, 4, 'Comfortable beds but noisy neighborhood.', 1),
    (7, 5, 7, 5, 'Excellent food and room service.', 1),
    (8, 3, 8, 3, 'Decent stay, but needs improvement in WiFi.', 1),
    (9, 4, 9, 4, 'Good overall, would stay again.', 1),
    (10, 5, 10, 5, 'Amazing experience, highly recommend.', 1);



-----------------------INDEXES CREATION FOR COZY HAVEN STAY APP----------------------------

-- Indexes for USERS table
CREATE INDEX idx_users_username ON USERS(USERNAME);
CREATE INDEX idx_users_email ON USERS(EMAIL);
CREATE INDEX idx_users_user_type ON USERS(USER_TYPE);

-- Indexes for HOTELS table
CREATE INDEX idx_hotels_location ON HOTELS(LOCATION);
CREATE INDEX idx_hotels_owner ON HOTELS(OWNER_USER_ID);

-- Indexes for ROOMS table
CREATE INDEX idx_rooms_hotel ON ROOMS(HOTEL_ID);
CREATE INDEX idx_rooms_type ON ROOMS(ROOM_TYPE_ID);
CREATE INDEX idx_rooms_availability ON ROOMS(IS_AVAILABLE);

-- Indexes for BOOKINGS table
CREATE INDEX idx_bookings_user ON BOOKINGS(USER_ID);
CREATE INDEX idx_bookings_hotel ON BOOKINGS(HOTEL_ID);
CREATE INDEX idx_bookings_status ON BOOKINGS(BOOKING_STATUS);
CREATE INDEX idx_bookings_check_dates ON BOOKINGS(CHECK_IN_DATE, CHECK_OUT_DATE);

-- Indexes for BOOKED_ROOM_DETAILS table
CREATE INDEX idx_booked_details_booking ON BOOKED_ROOM_DETAILS(BOOKING_ID);
CREATE INDEX idx_booked_details_room ON BOOKED_ROOM_DETAILS(ROOM_ID);
CREATE INDEX idx_booked_details_hotel ON BOOKED_ROOM_DETAILS(HOTEL_ID); 

-- Indexes for PAYMENTS table
CREATE INDEX idx_payments_booking ON PAYMENTS(BOOKING_ID);
CREATE INDEX idx_payments_gateway_txn ON PAYMENTS(GATEWAY_TRANSACTION_ID);
CREATE INDEX idx_payments_user ON PAYMENTS(USER_ID);
CREATE INDEX idx_payments_status ON PAYMENTS(PAYMENT_STATUS);

-- Indexes for REVIEWS table
CREATE INDEX idx_reviews_user ON REVIEWS(USER_ID);
CREATE INDEX idx_reviews_hotel ON REVIEWS(HOTEL_ID);
CREATE INDEX idx_reviews_booking ON REVIEWS(BOOKING_ID);
