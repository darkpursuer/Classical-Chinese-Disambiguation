CREATE SCHEMA `yz3940_nlp` DEFAULT CHARACTER SET utf8 COLLATE utf8_bin ;

use `yz3940_nlp`;

CREATE TABLE `yz3940_nlp`.`wordsrcs` (
  `No` INT NOT NULL AUTO_INCREMENT,
  `word` MEDIUMTEXT NOT NULL,
  `sense` MEDIUMTEXT NOT NULL,
  `context` MEDIUMTEXT NOT NULL,
  `index` MEDIUMTEXT NOT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

CREATE TABLE `yz3940_nlp`.`features` (
  `index` MEDIUMTEXT NOT NULL,
  `word` MEDIUMTEXT NOT NULL,
  `feature_str` MEDIUMTEXT NOT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

CREATE TABLE `yz3940_nlp`.`parameters` (
  `word` MEDIUMTEXT NOT NULL,
  `sense` MEDIUMTEXT NOT NULL,
  `parameter` INT NOT NULL,
  `feature` MEDIUMTEXT NOT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

CREATE TABLE `yz3940_nlp`.`sensechars` (
  `word` MEDIUMTEXT NOT NULL,
  `sense` MEDIUMTEXT NOT NULL,
  `char` MEDIUMTEXT NOT NULL,
  `PrVj` MEDIUMTEXT NOT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

CREATE TABLE `yz3940_nlp`.`wordempties` (
  `word` MEDIUMTEXT NOT NULL,
  `PrEmpty` MEDIUMTEXT NOT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

CREATE TABLE `yz3940_nlp`.`wordsenses` (
  `word` MEDIUMTEXT NOT NULL,
  `sense` MEDIUMTEXT NOT NULL,
  `PrSk` MEDIUMTEXT NOT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

CREATE TABLE `yz3940_nlp`.`testfiles` (
  `no` MEDIUMTEXT NOT NULL,
  `date` MEDIUMTEXT NOT NULL,
  `isCompleted` MEDIUMTEXT NOT NULL,
  `progress` MEDIUMTEXT NOT NULL,
  `result` LONGTEXT NOT NULL,
  `entryNum` MEDIUMTEXT NOT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

CREATE TABLE `yz3940_nlp`.`testentries` (
  `id` MEDIUMTEXT NOT NULL,
  `testNo` MEDIUMTEXT NOT NULL,
  `word` MEDIUMTEXT NOT NULL,
  `context` MEDIUMTEXT NOT NULL,
  `index` MEDIUMTEXT NOT NULL,
  `realSense` MEDIUMTEXT NOT NULL,
  `testSense` MEDIUMTEXT NOT NULL,
  `confidence` MEDIUMTEXT NOT NULL,
  `isRetrained` MEDIUMTEXT NOT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

CREATE TABLE `yz3940_nlp`.`configs` (
  `key` MEDIUMTEXT NOT NULL,
  `value` MEDIUMTEXT NOT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;