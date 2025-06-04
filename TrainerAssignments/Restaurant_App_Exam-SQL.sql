-------------------PLSQL Assessment - Restaurant Application--------------------
-----------------------Sudharsana I - 04/06/2025--------------------------------

-- Enable DBMS_OUTPUT for displaying messages in SQL*Plus or SQL Developer
SET SERVEROUTPUT ON;

-- 1. Create Table Restaurant
CREATE TABLE Restaurant
(
    restaurantId NUMBER PRIMARY KEY,
    restaurantName VARCHAR2(30),
    city VARCHAR2(30),
    email VARCHAR2(30),
    mobile VARCHAR2(15),
    rating NUMBER(9,2)
);
--1.1 CREATE SEQUENCE restaurant_seq for id
CREATE SEQUENCE restaurant_seq
START WITH 1
INCREMENT BY 1
NOCACHE;

--1.2 Create a Trigger
CREATE OR REPLACE TRIGGER trg_before_insert_restaurant
BEFORE INSERT ON Restaurant
FOR EACH ROW
BEGIN
    IF :NEW.restaurantId IS NULL THEN
        SELECT restaurant_seq.NEXTVAL INTO :NEW.restaurantId FROM dual;
    END IF;
END;

-- Insertion Of Sample Records for Restaurant
INSERT ALL
  INTO Restaurant (restaurantName, city, email, mobile, rating) VALUES ('Tasty Treats', 'Chennai', 'treats@food.com', '9123456789', 4.3)
  INTO Restaurant (restaurantName, city, email, mobile, rating) VALUES ('Spice Hub', 'Madurai', 'spicehub@food.com', '9012345678', 4.6)
  INTO Restaurant (restaurantName, city, email, mobile, rating) VALUES ('Curry Leaves', 'Coimbatore', 'curry@leaves.com', '9876543210', 4.2)
  INTO Restaurant (restaurantName, city, email, mobile, rating) VALUES ('Grill House', 'Trichy', 'grill@house.com', '9998877665', 4.8)
  INTO Restaurant (restaurantName, city, email, mobile, rating) VALUES ('Veg Delight', 'Salem', 'veg@delight.com', '9887766554', 4.1)
SELECT * FROM dual;
COMMIT;


-- 2. Create Table RestaurantBackup
CREATE TABLE RestaurantBackup (
    RbId NUMBER PRIMARY KEY,
    restaurantId NUMBER,
    restaurantName VARCHAR2(30),
    city VARCHAR2(30),
    email VARCHAR2(30),
    mobile VARCHAR2(15),
    rating NUMBER(9,2),
    operation VARCHAR2(30),
    activityOn DATE DEFAULT SYSDATE
);

--2.1 Create Sequence for RbId
CREATE SEQUENCE restaurantbackup_seq
START WITH 1
INCREMENT BY 1
NOCACHE;

--2.2 Create Trigger to Auto-Increment RbId on insert into RestaurantBackup
CREATE OR REPLACE TRIGGER trg_before_insert_restaurantbackup
BEFORE INSERT ON RestaurantBackup
FOR EACH ROW
BEGIN
    IF :NEW.RbId IS NULL THEN
        SELECT restaurantbackup_seq.NEXTVAL INTO :NEW.RbId FROM dual;
    END IF;
END;
/

--2.3.1 After Insert on Restaurant — Log new inserted row

CREATE OR REPLACE TRIGGER trg_after_insert_restaurant
AFTER INSERT ON Restaurant
FOR EACH ROW
BEGIN
    INSERT INTO RestaurantBackup (
        restaurantId, restaurantName, city, email, mobile, rating, operation
    ) VALUES (
        :NEW.restaurantId, :NEW.restaurantName, :NEW.city, :NEW.email, :NEW.mobile, :NEW.rating, 'INSERT'
    );
END;
/

--2.3.2 Before Update on Restaurant — Log old data before update

CREATE OR REPLACE TRIGGER trg_before_update_restaurant
BEFORE UPDATE ON Restaurant
FOR EACH ROW
BEGIN
    INSERT INTO RestaurantBackup (
        restaurantId, restaurantName, city, email, mobile, rating, operation
    ) VALUES (
        :OLD.restaurantId, :OLD.restaurantName, :OLD.city, :OLD.email, :OLD.mobile, :OLD.rating, 'UPDATE'
    );
END;
/

--2.3.3 Before Delete on Restaurant — Log old data before delete
CREATE OR REPLACE TRIGGER trg_before_delete_restaurant
BEFORE DELETE ON Restaurant
FOR EACH ROW
BEGIN
    INSERT INTO RestaurantBackup (
        restaurantId, restaurantName, city, email, mobile, rating, operation
    ) VALUES (
        :OLD.restaurantId, :OLD.restaurantName, :OLD.city, :OLD.email, :OLD.mobile, :OLD.rating, 'DELETE'
    );
END;
/

-- Insertion Of Sample Records to RestaurantBackup
INSERT ALL
  INTO RestaurantBackup (restaurantId, restaurantName, city, email, mobile, rating, operation) 
    VALUES (1, 'Tasty Treats', 'Chennai', 'treats@food.com', '9123456789', 4.3, 'INSERT')
  INTO RestaurantBackup (restaurantId, restaurantName, city, email, mobile, rating, operation) 
    VALUES (2, 'Spice Hub', 'Madurai', 'spicehub@food.com', '9012345678', 4.6, 'INSERT')
  INTO RestaurantBackup (restaurantId, restaurantName, city, email, mobile, rating, operation) 
    VALUES (3, 'Curry Leaves', 'Coimbatore', 'curry@leaves.com', '9876543210', 4.2, 'INSERT')
  INTO RestaurantBackup (restaurantId, restaurantName, city, email, mobile, rating, operation) 
    VALUES (4, 'Grill House', 'Trichy', 'grill@house.com', '9998877665', 4.8, 'INSERT')
  INTO RestaurantBackup (restaurantId, restaurantName, city, email, mobile, rating, operation) 
    VALUES (5, 'Veg Delight', 'Salem', 'veg@delight.com', '9887766554', 4.1, 'INSERT')
SELECT * FROM dual;
COMMIT;


-----------------TASKS-------------------

--1.Implement Procedures to AddRestaurant, SearchByRestaurantId, UpdateRestaurant, DeleteRestaurantById

--i) AddRestaurant
CREATE OR REPLACE PROCEDURE AddRestaurant(
    p_name IN VARCHAR2,
    p_city IN VARCHAR2,
    p_email IN VARCHAR2,
    p_mobile IN VARCHAR2,
    p_rating IN NUMBER
)
IS
BEGIN
    INSERT INTO Restaurant (restaurantName, city, email, mobile, rating)
    VALUES (p_name, p_city, p_email, p_mobile, p_rating);
    DBMS_OUTPUT.PUT_LINE('Restaurant Added Successfully');
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        DBMS_OUTPUT.PUT_LINE('A duplicate value was found for a unique constraint (e.g., email).');
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error adding restaurant: ' || SQLERRM);
END;
/


--ii) SearchByRestaurantId

CREATE OR REPLACE PROCEDURE SearchByRestaurantId(p_id IN NUMBER)
IS
    v_restaurant Restaurant%ROWTYPE;
BEGIN
    SELECT * INTO v_restaurant FROM Restaurant WHERE restaurantId = p_id;
    DBMS_OUTPUT.PUT_LINE('ID: ' || v_restaurant.restaurantId || ', Name: ' || v_restaurant.restaurantName || 
                         ', City: ' || v_restaurant.city || ', Email: ' || v_restaurant.email || 
                         ', Mobile: ' || v_restaurant.mobile || ', Rating: ' || v_restaurant.rating);
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Restaurant not found.');
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;

--iii) UpdateRestaurant

CREATE OR REPLACE PROCEDURE UpdateRestaurant(
    p_id IN NUMBER,
    p_name IN VARCHAR2,
    p_city IN VARCHAR2,
    p_email IN VARCHAR2,
    p_mobile IN VARCHAR2,
    p_rating IN NUMBER
)
IS
BEGIN
    UPDATE Restaurant 
    SET restaurantName = p_name, city = p_city, email = p_email, mobile = p_mobile, rating = p_rating
    WHERE restaurantId = p_id;

    IF SQL%ROWCOUNT = 0 THEN
        DBMS_OUTPUT.PUT_LINE('Restaurant not found.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('Restaurant updated.');
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;


--iv)DeleteRestaurantById
CREATE OR REPLACE PROCEDURE DeleteRestaurantById(p_id IN NUMBER)
IS
BEGIN
    DELETE FROM Restaurant WHERE restaurantId = p_id;
    IF SQL%ROWCOUNT = 0 THEN
        DBMS_OUTPUT.PUT_LINE('Restaurant not found.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('Restaurant deleted.');
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;

-- 2.  Write cursor to print all restaurant Details

DECLARE
    CURSOR rest_cursor IS SELECT * FROM Restaurant;
    v_restaurant Restaurant%ROWTYPE;
BEGIN
    OPEN rest_cursor;
    LOOP
        FETCH rest_cursor INTO v_restaurant;
        EXIT WHEN rest_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE('ID: ' || v_restaurant.restaurantId || ', Name: ' || v_restaurant.restaurantName ||
                             ', City: ' || v_restaurant.city || ', Email: ' || v_restaurant.email || 
                             ', Mobile: ' || v_restaurant.mobile || ', Rating: ' || v_restaurant.rating);
    END LOOP;
    CLOSE rest_cursor;
END;


--3.Write a procedure with output parameters to searchRestaurantById

CREATE OR REPLACE PROCEDURE searchRestaurantById(
    p_id IN NUMBER,
    p_name OUT VARCHAR2,
    p_city OUT VARCHAR2,
    p_email OUT VARCHAR2,
    p_mobile OUT VARCHAR2,
    p_rating OUT NUMBER
)
IS
BEGIN
    SELECT restaurantName, city, email, mobile, rating
    INTO p_name, p_city, p_email, p_mobile, p_rating
    FROM Restaurant
    WHERE restaurantId = p_id;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Restaurant not found.');
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;


--Write 3 Triggers for Insert, Update and Delete as you need to store the records in backup table and write operation as "INSERT","DELETE","UPDATE"
--1.If update old values to be stored in Backup Table
--2.if delete old values to be stored in Backup Table
--3.if insert new values to be stored in backup table                          --Completed at the time of table creation itself



-----------------------SAMPLE INPUTS AND OUPUTS---------------------------------

SET SERVEROUTPUT ON;


--1) Test AddRestaurant
EXEC AddRestaurant('Ocean Bites', 'Pondicherry', 'ocean@bites.com', '9876543211', 4.5);

--2) Test SearchByRestaurantId 
    --Searching for ID 1 (existing)
    EXEC SearchByRestaurantId(1);
    --Searching for ID not present (e.g., 999)
    EXEC SearchByRestaurantId(999);

--3) Test UpdateRestaurant
EXEC UpdateRestaurant(2, 'Spice Hub Deluxe', 'Madurai', 'spicehub@newmail.com', '9012345678', 4.7);

    --3.1 Non Exsistent ID test
    EXEC UpdateRestaurant(999, 'No Name', 'Nowhere', 'no@mail.com', '0000000000', 0);



--4) Test DeleteRestaurantById
EXEC DeleteRestaurantById(3);

    --4.1 Deletion of Non Exsistent ID test
    EXEC DeleteRestaurantById(999);

--5) Test Cursor block to print all restaurants
DECLARE
    CURSOR rest_cursor IS SELECT * FROM Restaurant;
    v_restaurant Restaurant%ROWTYPE;
BEGIN
    OPEN rest_cursor;
    LOOP
        FETCH rest_cursor INTO v_restaurant;
        EXIT WHEN rest_cursor%NOTFOUND;
        DBMS_OUTPUT.PUT_LINE('ID: ' || v_restaurant.restaurantId || ', Name: ' || v_restaurant.restaurantName ||
                             ', City: ' || v_restaurant.city || ', Email: ' || v_restaurant.email || 
                             ', Mobile: ' || v_restaurant.mobile || ', Rating: ' || v_restaurant.rating);
    END LOOP;
    CLOSE rest_cursor;
END;
/

--6 Test searchRestaurantById with output params
DECLARE
    v_name VARCHAR2(30);
    v_city VARCHAR2(30);
    v_email VARCHAR2(30);
    v_mobile VARCHAR2(15);
    v_rating NUMBER(9,2);
BEGIN
    searchRestaurantById(1, v_name, v_city, v_email, v_mobile, v_rating);
    DBMS_OUTPUT.PUT_LINE('Name: ' || v_name);
    DBMS_OUTPUT.PUT_LINE('City: ' || v_city);
    DBMS_OUTPUT.PUT_LINE('Email: ' || v_email);
    DBMS_OUTPUT.PUT_LINE('Mobile: ' || v_mobile);
    DBMS_OUTPUT.PUT_LINE('Rating: ' || v_rating);
END;
/


--7 Verifying Triggers working by checking RestaurantBackup

SELECT RbId, restaurantId, restaurantName, operation, activityOn FROM RestaurantBackup ORDER BY RbId;










