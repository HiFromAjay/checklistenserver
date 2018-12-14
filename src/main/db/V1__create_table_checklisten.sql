CREATE TABLE checklisten.checklisten (
	`ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
	`KUERZEL` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
	`NAME` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
	`TYP` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
	`DATEN` LONGTEXT,
	`VERSION` int(10) DEFAULT 0,
	`DATE_MODIFIED` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`ID`),
  	UNIQUE KEY `uk_checklisten_1` (`KUERZEL`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

