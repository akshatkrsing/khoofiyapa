use ngdb;
------
create table if not exists secrets(file_id int primary key auto_increment, filename varchar(70), filesecretkey longblob,
fileextension varchar(50), filepath varchar(300) unique, fileencryption varchar(70));
------
create table if not exists histories(h_id int primary key auto_increment, filepath varchar(300), actiontime timestamp,
foreign key (filepath) references secrets(filepath));
------
create table if not exists params(
    paramName varchar(100) not null primary key,
    paramValue varchar(100)
);