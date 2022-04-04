create database dbmovie_test;
create user 'movie'@'localhost' identified by 'password';
grant all privileges on dbmovie_test.* to 'movie'@'localhost';
flush privileges;
