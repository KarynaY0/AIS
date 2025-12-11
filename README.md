Here is also a db code just in case:
-- Recreate Database (THIS DELETES ALL EXISTING DATA!)
DROP DATABASE IF EXISTS AIS;
CREATE DATABASE AIS CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE AIS;

-----------------------------------------------------
-- USER TABLE
-----------------------------------------------------
CREATE TABLE user (
    userid INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-----------------------------------------------------
-- ADMIN TABLE
-----------------------------------------------------
CREATE TABLE admin (
    pk INT PRIMARY KEY AUTO_INCREMENT,
    userid INT NOT NULL UNIQUE,
    FOREIGN KEY (userid) REFERENCES user(userid)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-----------------------------------------------------
-- GROUP TABLE
-----------------------------------------------------
CREATE TABLE `group` (
    groupid INT PRIMARY KEY AUTO_INCREMENT,
    group_code VARCHAR(10) UNIQUE NOT NULL,
    program_initials VARCHAR(3) NOT NULL,
    start_year INT NOT NULL,
    language_code VARCHAR(1),
    CONSTRAINT chk_program_initials CHECK (CHAR_LENGTH(program_initials) BETWEEN 2 AND 3),
    CONSTRAINT chk_start_year CHECK (start_year BETWEEN 0 AND 99),
    CONSTRAINT chk_language_code CHECK (language_code IS NULL OR CHAR_LENGTH(language_code) = 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-----------------------------------------------------
-- STUDENT TABLE
-----------------------------------------------------
CREATE TABLE student (
    pk INT PRIMARY KEY AUTO_INCREMENT,
    userid INT NOT NULL UNIQUE,
    groupid INT,
    FOREIGN KEY (userid) REFERENCES user(userid)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (groupid) REFERENCES `group`(groupid)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-----------------------------------------------------
-- TEACHER TABLE
-----------------------------------------------------
CREATE TABLE teacher (
    pk INT PRIMARY KEY AUTO_INCREMENT,
    userid INT NOT NULL UNIQUE,
    department VARCHAR(100),
    FOREIGN KEY (userid) REFERENCES user(userid)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-----------------------------------------------------
-- SUBJECT TABLE
-----------------------------------------------------
CREATE TABLE subject (
    subjectid INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) UNIQUE NOT NULL,
    credits INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-----------------------------------------------------
-- GRADE TABLE
-----------------------------------------------------
CREATE TABLE grade (
    gradeid INT PRIMARY KEY AUTO_INCREMENT,
    userid INT NOT NULL,
    subjectid INT NOT NULL,
    grade_value DECIMAL(5,2) NOT NULL DEFAULT 0,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    comment TEXT,
    FOREIGN KEY (userid) REFERENCES user(userid)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (subjectid) REFERENCES subject(subjectid)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_userid (userid),
    INDEX idx_subjectid (subjectid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-----------------------------------------------------
-- TEACHER_SUBJECT TABLE
-----------------------------------------------------
CREATE TABLE teacher_subject (
    id INT PRIMARY KEY AUTO_INCREMENT,
    userid INT NOT NULL,
    subjectid INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE KEY unique_teacher_subject (userid, subjectid),
    FOREIGN KEY (userid) REFERENCES user(userid)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (subjectid) REFERENCES subject(subjectid)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-----------------------------------------------------
-- GROUP_SUBJECT TABLE
-----------------------------------------------------
CREATE TABLE group_subject (
    id INT PRIMARY KEY AUTO_INCREMENT,
    groupid INT NOT NULL,
    subjectid INT NOT NULL,
    academic_semester VARCHAR(20),
    UNIQUE KEY unique_group_subject (groupid, subjectid),
    FOREIGN KEY (groupid) REFERENCES `group`(groupid)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (subjectid) REFERENCES subject(subjectid)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-----------------------------------------------------
-- INSERT ADMIN USER
-----------------------------------------------------
INSERT INTO user (username, password)
VALUES ('Admin', 'Admin');

INSERT INTO admin (userid)
VALUES (LAST_INSERT_ID());

-----------------------------------------------------
-- INSERT SAMPLE TEACHER
-----------------------------------------------------
INSERT INTO user (username, password)
VALUES ('teacher1', 'password');

INSERT INTO teacher (userid, department)
VALUES (LAST_INSERT_ID(), 'Computer Science');

-----------------------------------------------------
-- INSERT SAMPLE STUDENTS
-----------------------------------------------------
INSERT INTO user (username, password)
VALUES 
    ('student1', 'password'),
    ('student2', 'password'),
    ('student3', 'password'),
    ('student4', 'password');

-- Now group creation
INSERT INTO `group` (group_code, program_initials, start_year, language_code)
VALUES 
    ('PI24E', 'PI', 24, 'E'),
    ('CS23', 'CS', 23, NULL),
    ('BA24L', 'BA', 24, 'L');

-- Assign students (userid starts at 3+)
INSERT INTO student (userid, groupid)
VALUES 
    (3, 1),  -- student1
    (4, 1),  -- student2
    (5, 2),  -- student3
    (6, 2);  -- student4

-----------------------------------------------------
-- INSERT SAMPLE SUBJECTS
-----------------------------------------------------
INSERT INTO subject (code, credits)
VALUES 
    ('MATH101', 5),
    ('CS101', 6),
    ('ENG101', 3);

-----------------------------------------------------
-- ASSIGN TEACHER TO SUBJECTS
-----------------------------------------------------
INSERT INTO teacher_subject (userid, subjectid, is_active)
VALUES 
    (2, 1, TRUE),  -- teacher1 teaches MATH101
    (2, 2, TRUE);  -- teacher1 teaches CS101

-----------------------------------------------------
-- ASSIGN SUBJECTS TO GROUPS
-----------------------------------------------------
INSERT INTO group_subject (groupid, subjectid, academic_semester)
VALUES 
    (1, 1, '2024 Fall'),
    (1, 2, '2024 Fall'),
    (2, 1, '2024 Fall'),
    (2, 2, '2024 Fall');

-----------------------------------------------------
-- INSERT SAMPLE GRADES
-----------------------------------------------------
INSERT INTO grade (comment, grade_value, userid, subjectid)
VALUES 
    ('Excellent work', 95, 3, 1),
    ('Good progress', 85, 4, 1),
    ('Needs improvement', 72, 5, 1),
    ('Outstanding', 98, 6, 2);

SELECT 'AIS database created successfully!' AS Status;
