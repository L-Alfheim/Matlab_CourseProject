CREATE TABLE Person (
    id INTEGER PRIMARY KEY NOT NULL,
    Name TEXT NOT NULL UNIQUE,
    phoneNumber TEXT,
    Address TEXT,
    Birthday TEXT,
    recordTime DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE FaceRecord (
    id INTEGER PRIMARY KEY NOT NULL,
    personId INTEGER NOT NULL,
    imageURL TEXT UNIQUE,
    embedding BLOB,
    FOREIGN KEY (personId) REFERENCES Person(id) ON DELETE CASCADE
);

INSERT INTO Person (Name, phoneNumber, Address) VALUES ('张三', '12345678901', '天津市津南区');
INSERT INTO Person (Name, phoneNumber, Address) VALUES ('李四', '19876543210', '上海市浦东新区');
INSERT INTO Person (Name, phoneNumber, Address) VALUES ('许五', '+8613712345678', '广东省广州市番禺区');
INSERT INTO Person (Name, phoneNumber, Address) VALUES ('王六', '010-12345678', '北京市海淀区');

INSERT INTO FaceRecord (personId, imageURL) VALUES (1, 'images/zhangsan1.jpg');