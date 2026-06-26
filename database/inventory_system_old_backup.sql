-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: inventory_system
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bill_invoice`
--

DROP TABLE IF EXISTS `bill_invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_invoice` (
  `bill_id` int NOT NULL AUTO_INCREMENT,
  `bill_no` varchar(20) NOT NULL,
  `bill_received_by` varchar(50) NOT NULL,
  `bill_amount` decimal(10,2) NOT NULL,
  `pid` varchar(6) NOT NULL,
  `sid` int NOT NULL,
  `entry_id` int DEFAULT NULL,
  `qty_received` int NOT NULL,
  `bill_status` enum('COMPLETE','INCOMPLETE') NOT NULL DEFAULT 'INCOMPLETE',
  `record_status` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `received_date` date DEFAULT NULL,
  PRIMARY KEY (`bill_id`),
  UNIQUE KEY `bill_no` (`bill_no`),
  KEY `sid` (`sid`),
  KEY `fk_bill_pid` (`pid`),
  KEY `fk_bill_entry` (`entry_id`),
  CONSTRAINT `bill_invoice_ibfk_2` FOREIGN KEY (`sid`) REFERENCES `supplier` (`sid`),
  CONSTRAINT `fk_bill_entry` FOREIGN KEY (`entry_id`) REFERENCES `order_table` (`entry_id`),
  CONSTRAINT `fk_bill_pid` FOREIGN KEY (`pid`) REFERENCES `product` (`pid`),
  CONSTRAINT `bill_invoice_chk_2` CHECK ((`bill_amount` > 0)),
  CONSTRAINT `bill_invoice_chk_qty_received` CHECK ((`qty_received` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bill_invoice`
--

LOCK TABLES `bill_invoice` WRITE;
/*!40000 ALTER TABLE `bill_invoice` DISABLE KEYS */;
INSERT INTO `bill_invoice` VALUES (1,'B5001','Store Clerk',5000.00,'P0001',1,1,60,'INCOMPLETE','ACTIVE','2025-03-15'),(2,'B5002','Store Clerk',20000.00,'P0002',2,2,20,'COMPLETE','ACTIVE','2025-03-16'),(3,'B5003','Store Clerk',150000.00,'P0003',3,3,5,'COMPLETE','ACTIVE','2025-03-17');
/*!40000 ALTER TABLE `bill_invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `issue`
--

DROP TABLE IF EXISTS `issue`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `issue` (
  `issue_id` int NOT NULL AUTO_INCREMENT,
  `pid` varchar(6) DEFAULT NULL,
  `issue_to` varchar(50) NOT NULL,
  `issued_by` varchar(50) NOT NULL,
  `dept_name` varchar(50) NOT NULL,
  `qty_issued` int NOT NULL,
  `reason` varchar(100) DEFAULT NULL,
  `date` date NOT NULL,
  `qty_returned` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`issue_id`),
  KEY `fk_issue_pid` (`pid`),
  CONSTRAINT `fk_issue_pid` FOREIGN KEY (`pid`) REFERENCES `product` (`pid`),
  CONSTRAINT `issue_chk_1` CHECK ((`qty_issued` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `issue`
--

LOCK TABLES `issue` WRITE;
/*!40000 ALTER TABLE `issue` DISABLE KEYS */;
INSERT INTO `issue` VALUES (3,'P0001','Admin Staff','Store','Admin',20,'Office work','2025-02-15',0),(4,'P0002','Exam Cell','Store','Examination',5,'Exam setup','2025-02-16',0);
/*!40000 ALTER TABLE `issue` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `manager_product_type`
--

DROP TABLE IF EXISTS `manager_product_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `manager_product_type` (
  `user_id` varchar(5) NOT NULL,
  `ptype_id` varchar(5) NOT NULL,
  PRIMARY KEY (`user_id`,`ptype_id`),
  KEY `ptype_id` (`ptype_id`),
  CONSTRAINT `manager_product_type_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `manager_product_type_ibfk_2` FOREIGN KEY (`ptype_id`) REFERENCES `product_type` (`ptype_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `manager_product_type`
--

LOCK TABLES `manager_product_type` WRITE;
/*!40000 ALTER TABLE `manager_product_type` DISABLE KEYS */;
INSERT INTO `manager_product_type` VALUES ('M01','Misc'),('M01','STN');
/*!40000 ALTER TABLE `manager_product_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_table`
--

DROP TABLE IF EXISTS `order_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_table` (
  `entry_id` int NOT NULL AUTO_INCREMENT,
  `order_no` varchar(20) NOT NULL,
  `pid` varchar(6) NOT NULL,
  `sid` int NOT NULL,
  `order_date` date NOT NULL,
  `qty_ordered` int NOT NULL,
  `order_status` enum('PAID','IN_PROCESS') NOT NULL DEFAULT 'IN_PROCESS',
  `record_status` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`entry_id`),
  KEY `pid` (`pid`),
  KEY `sid` (`sid`),
  CONSTRAINT `order_table_ibfk_1` FOREIGN KEY (`pid`) REFERENCES `product` (`pid`),
  CONSTRAINT `order_table_ibfk_2` FOREIGN KEY (`sid`) REFERENCES `supplier` (`sid`),
  CONSTRAINT `order_table_chk_1` CHECK ((`qty_ordered` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_table`
--

LOCK TABLES `order_table` WRITE;
/*!40000 ALTER TABLE `order_table` DISABLE KEYS */;
INSERT INTO `order_table` VALUES (1,'ORD201','P0001',1,'2025-03-10',100,'IN_PROCESS','ACTIVE'),(2,'ORD202','P0002',2,'2025-03-11',20,'PAID','ACTIVE'),(3,'ORD203','P0003',3,'2025-03-12',5,'PAID','ACTIVE');
/*!40000 ALTER TABLE `order_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `pid` varchar(6) NOT NULL,
  `product_name` varchar(50) NOT NULL,
  `description` varchar(100) NOT NULL,
  `qty_in_stock` int NOT NULL,
  `min_qty_required` int NOT NULL,
  `ptype_id` varchar(5) NOT NULL,
  PRIMARY KEY (`pid`),
  KEY `ptype_id` (`ptype_id`),
  CONSTRAINT `product_ibfk_1` FOREIGN KEY (`ptype_id`) REFERENCES `product_type` (`ptype_id`),
  CONSTRAINT `product_chk_1` CHECK ((`qty_in_stock` >= 0)),
  CONSTRAINT `product_chk_2` CHECK ((`min_qty_required` >= 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES ('P0001','A4 Paper','A4 size white sheets',200,50,'STN'),('P0002','Office Chair','Revolving chair',25,10,'FUR'),('P0003','Desktop PC','i5 8GB RAM Desktop',10,5,'CS');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_type`
--

DROP TABLE IF EXISTS `product_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_type` (
  `ptype_id` varchar(5) NOT NULL,
  `ptype_name` varchar(30) NOT NULL,
  PRIMARY KEY (`ptype_id`),
  UNIQUE KEY `ptype_name` (`ptype_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_type`
--

LOCK TABLES `product_type` WRITE;
/*!40000 ALTER TABLE `product_type` DISABLE KEYS */;
INSERT INTO `product_type` VALUES ('CS','Computer sets'),('FUR','Furniture'),('Misc','Miscellaneous'),('STN','Stationery');
/*!40000 ALTER TABLE `product_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `return_table`
--

DROP TABLE IF EXISTS `return_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `return_table` (
  `return_id` int NOT NULL AUTO_INCREMENT,
  `ptype_id` varchar(5) NOT NULL,
  `issue_id` int NOT NULL,
  `pid` varchar(6) DEFAULT NULL,
  `quantity` int NOT NULL,
  `return_to` varchar(50) NOT NULL,
  `date` date NOT NULL,
  PRIMARY KEY (`return_id`),
  KEY `ptype_id` (`ptype_id`),
  KEY `issue_id` (`issue_id`),
  KEY `fk_return_pid` (`pid`),
  CONSTRAINT `fk_return_pid` FOREIGN KEY (`pid`) REFERENCES `product` (`pid`),
  CONSTRAINT `return_table_ibfk_1` FOREIGN KEY (`ptype_id`) REFERENCES `product_type` (`ptype_id`),
  CONSTRAINT `return_table_ibfk_2` FOREIGN KEY (`issue_id`) REFERENCES `issue` (`issue_id`),
  CONSTRAINT `return_table_chk_1` CHECK ((`quantity` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `return_table`
--

LOCK TABLES `return_table` WRITE;
/*!40000 ALTER TABLE `return_table` DISABLE KEYS */;
/*!40000 ALTER TABLE `return_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role` (
  `role_id` int NOT NULL,
  `role_name` varchar(20) NOT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES (3,'Dean'),(2,'HOD'),(1,'Manager');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rts_table`
--

DROP TABLE IF EXISTS `rts_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rts_table` (
  `rts_id` int NOT NULL AUTO_INCREMENT,
  `bill_id` int NOT NULL,
  `pid` varchar(6) NOT NULL,
  `sid` int NOT NULL,
  `quantity` int NOT NULL,
  `rts_date` date NOT NULL,
  `record_status` enum('SENT','RECEIVED') NOT NULL DEFAULT 'SENT',
  PRIMARY KEY (`rts_id`),
  KEY `bill_id` (`bill_id`),
  KEY `pid` (`pid`),
  KEY `sid` (`sid`),
  CONSTRAINT `rts_table_ibfk_1` FOREIGN KEY (`bill_id`) REFERENCES `bill_invoice` (`bill_id`),
  CONSTRAINT `rts_table_ibfk_2` FOREIGN KEY (`pid`) REFERENCES `product` (`pid`),
  CONSTRAINT `rts_table_ibfk_3` FOREIGN KEY (`sid`) REFERENCES `supplier` (`sid`),
  CONSTRAINT `rts_table_chk_1` CHECK ((`quantity` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rts_table`
--

LOCK TABLES `rts_table` WRITE;
/*!40000 ALTER TABLE `rts_table` DISABLE KEYS */;
/*!40000 ALTER TABLE `rts_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supplier`
--

DROP TABLE IF EXISTS `supplier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier` (
  `sid` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `email` varchar(50) DEFAULT NULL,
  `contact_no` varchar(15) NOT NULL,
  `address` varchar(100) NOT NULL,
  `ptype_id` varchar(5) NOT NULL,
  PRIMARY KEY (`sid`),
  UNIQUE KEY `email` (`email`),
  KEY `ptype_id` (`ptype_id`),
  CONSTRAINT `supplier_ibfk_1` FOREIGN KEY (`ptype_id`) REFERENCES `product_type` (`ptype_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplier`
--

LOCK TABLES `supplier` WRITE;
/*!40000 ALTER TABLE `supplier` DISABLE KEYS */;
INSERT INTO `supplier` VALUES (1,'Stationery Hub','stat@hub.com','9876543210','Delhi','STN'),(2,'Pen World','pen@world.com','9876543211','Noida','STN'),(3,'Furniture House','furn@house.com','9876543212','Gurgaon','FUR'),(4,'Tech Supplies','tech@sup.com','9876543213','Delhi','CS'),(5,'Office Mart','office@mart.com','9876543214','Noida','Misc');
/*!40000 ALTER TABLE `supplier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `supplies`
--

DROP TABLE IF EXISTS `supplies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplies` (
  `sid` int NOT NULL,
  `pid` varchar(6) NOT NULL,
  PRIMARY KEY (`sid`,`pid`),
  KEY `fk_supplies_pid` (`pid`),
  CONSTRAINT `fk_supplies_pid` FOREIGN KEY (`pid`) REFERENCES `product` (`pid`),
  CONSTRAINT `supplies_ibfk_1` FOREIGN KEY (`sid`) REFERENCES `supplier` (`sid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `supplies`
--

LOCK TABLES `supplies` WRITE;
/*!40000 ALTER TABLE `supplies` DISABLE KEYS */;
INSERT INTO `supplies` VALUES (1,'P0001'),(2,'P0002'),(3,'P0003');
/*!40000 ALTER TABLE `supplies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` varchar(5) NOT NULL,
  `user_name` varchar(30) NOT NULL,
  `password` varchar(30) NOT NULL,
  `role_id` int NOT NULL,
  `secret_question` varchar(255) DEFAULT NULL,
  `secret_answer` varchar(255) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `email` (`email`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES ('D01','CK Jha Sir','pass123',3,'What is your senior secondary school name?','abc','v0890257@gmail.com'),('H01','Rajiv Sir','pass123',2,'What is your senior secondary school name?','def','vanshree1635@gmail.com'),('M01','Himanshu sir','pass1234',1,'What is your senior secondary school name?','xyz','g7447931@gmail.com');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-26 12:44:59
