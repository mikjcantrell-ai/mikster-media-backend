-- MySQL dump 10.13  Distrib 9.7.0, for macos15 (arm64)
--
-- Host: localhost    Database: mmai
-- ------------------------------------------------------
-- Server version	9.7.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '102862f0-584c-11f1-8b58-063b17c28861:1-906';

--
-- Table structure for table `app_users`
--

DROP TABLE IF EXISTS `app_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `display_name` varchar(80) DEFAULT NULL,
  `email` varchar(128) DEFAULT NULL,
  `last_login` datetime(6) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` varchar(16) NOT NULL,
  `username` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKspsnwr241e9k9c8p5xl4k45ih` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_users`
--

LOCK TABLES `app_users` WRITE;
/*!40000 ALTER TABLE `app_users` DISABLE KEYS */;
INSERT INTO `app_users` VALUES (1,_binary '','2026-05-20 18:01:41.000000','Platform Admin','admin@mikstermedia.io',NULL,'$2b$10$kUq8wtA8lRRnoCE0RXI1uewpF/2PutUPL0mYlMYmWS.UNTwZj1iUq','ADMIN','admin'),(2,_binary '','2026-06-09 22:09:52.166597','Dan','dlisee@gmail.com',NULL,'$2a$10$PF2iO.euPJZc9K4dEeOLI.zkXuOi.eMRlcJFv5nbpYR/gI3mcWxxi','ADMIN','dlisee');
/*!40000 ALTER TABLE `app_users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `artists`
--

DROP TABLE IF EXISTS `artists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `artists` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ai_tools_used` varchar(255) DEFAULT NULL,
  `bio` text,
  `country` varchar(255) DEFAULT NULL,
  `featured_status` bit(1) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `monthly_listeners` int DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `primary_genre` varchar(255) DEFAULT NULL,
  `profile_url` varchar(255) DEFAULT NULL,
  `display_order` int DEFAULT NULL,
  `featured_since` datetime(6) DEFAULT NULL,
  `website_url` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_artist_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=199 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `artists`
--

LOCK TABLES `artists` WRITE;
/*!40000 ALTER TABLE `artists` DISABLE KEYS */;
INSERT INTO `artists` VALUES (7,'Suno','Where honeysuckle meets heartstring, and every dirt road leads somewhere worth singing about.','United States',_binary '','https://i.scdn.co/image/ab67616d0000b2738545abded79819cd6ab6833f',0,'Marianna Dreams','Country','https://open.spotify.com/artist/0eJgTk6bE7oCiZoczsTvuC',0,NULL,'https://www.mariannadreams.com'),(8,'Spotify Import',NULL,NULL,_binary '','https://i.scdn.co/image/ab67616d0000b2739ba55dbe8fcc9f1392d9bb4c',0,'JAX Cantrell','Pop','https://open.spotify.com/artist/0WuEokx5jjbCdjTq4CLLq5',0,NULL,NULL),(187,'Spotify Import',NULL,NULL,_binary '\0','https://i.scdn.co/image/ab67616d0000b273307e11689b7369426cb87c10',0,'Cross Bone Tears',NULL,NULL,0,NULL,NULL),(188,'Spotify Import',NULL,NULL,_binary '\0','https://i.scdn.co/image/ab67616d0000b273307e11689b7369426cb87c10',0,'AI Generated Music',NULL,NULL,0,NULL,NULL),(189,'Spotify Import',NULL,NULL,_binary '\0','https://i.scdn.co/image/ab67616d0000b2731aa3416797db215e481c934c',0,'MisterLEVIK',NULL,NULL,0,NULL,NULL),(190,'Spotify Import',NULL,NULL,_binary '\0','https://i.scdn.co/image/ab67616d0000b2739dbd5064a23153b9d0b4080a',0,'A.I.M. Artificial Intelligence Music',NULL,NULL,0,NULL,NULL),(191,'Spotify Import',NULL,NULL,_binary '\0','https://i.scdn.co/image/ab67616d0000b27372b2d2e2bbce6799e95d4221',0,'Spike Polite & Sewage',NULL,NULL,0,NULL,NULL),(192,'Spotify Import',NULL,NULL,_binary '\0','https://i.scdn.co/image/ab67616d0000b27317b6eb5893d21d2dda836bbb',0,'Greg “Dr. C” Calliste',NULL,NULL,0,NULL,NULL),(193,'Spotify Import',NULL,NULL,_binary '\0','https://i.scdn.co/image/ab67616d0000b273e786fdb99b7abfb7a056ac42',0,'Gabby Moon',NULL,NULL,0,NULL,NULL),(194,'Spotify Import',NULL,NULL,_binary '\0','https://i.scdn.co/image/ab67616d0000b2732e7da53a76c5ee7012e793e8',0,'Suno AI',NULL,NULL,0,NULL,NULL),(196,'Spotify Import',NULL,NULL,_binary '\0','https://i.scdn.co/image/ab67616d0000b2732e7da53a76c5ee7012e793e8',0,'Thien Phearin',NULL,NULL,0,NULL,NULL),(197,'Spotify Import',NULL,NULL,_binary '\0','https://i.scdn.co/image/ab67616d0000b2732e7da53a76c5ee7012e793e8',0,'GenerativeAI',NULL,NULL,0,NULL,NULL),(198,'Spotify Import',NULL,NULL,_binary '\0','https://i.scdn.co/image/ab67616d0000b2732e7da53a76c5ee7012e793e8',0,'Aussa',NULL,NULL,0,NULL,NULL);
/*!40000 ALTER TABLE `artists` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `collaboration_posts`
--

DROP TABLE IF EXISTS `collaboration_posts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collaboration_posts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `author_name` varchar(255) NOT NULL,
  `collaboration_type` varchar(255) NOT NULL,
  `contact_info` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` text NOT NULL,
  `title` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collaboration_posts`
--

LOCK TABLES `collaboration_posts` WRITE;
/*!40000 ALTER TABLE `collaboration_posts` DISABLE KEYS */;
INSERT INTO `collaboration_posts` VALUES (1,_binary '','Synthwave Steve','Vocals','steve@example.com','2026-05-25 15:54:32.000000','I have a great instrumental track generated in Suno, but I want real human vocals on it. It is a country pop song. Let me know if you are interested!','Need female country vocals for Suno track'),(2,_binary '\0','Test','Vocals','test@test.com','2026-06-01 18:14:48.512246','Test','Test Collab'),(3,_binary '','Michael Cantrell','Prompting','mikjcantrell@gmail.com','2026-06-06 17:23:41.768289','Testing','Testing');
/*!40000 ALTER TABLE `collaboration_posts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `genres`
--

DROP TABLE IF EXISTS `genres`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `genres` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `color_hex` varchar(255) DEFAULT NULL,
  `description` text,
  `icon_emoji` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `track_count` int DEFAULT NULL,
  `display_order` int DEFAULT NULL,
  `featured_since` datetime(6) DEFAULT NULL,
  `featured_status` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_pe1a9woik1k97l87cieguyhh4` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `genres`
--

LOCK TABLES `genres` WRITE;
/*!40000 ALTER TABLE `genres` DISABLE KEYS */;
INSERT INTO `genres` VALUES (1,'#00e5ff','AI-generated electronic music spanning synthwave, techno, and ambient electronica.','🎹','Electronic',3,NULL,NULL,NULL),(2,'#b06dff','Meditative, evolving soundscapes designed for focus, sleep, and creative flow.','🌌','Ambient',2,NULL,NULL,NULL),(3,'#ffd700','AI improvisations and compositions exploring jazz harmony and improvisation.','🎺','Jazz',1,NULL,NULL,NULL),(4,'#ff4da6','Chill beats, warm textures, and nostalgic vibes — all generated by AI.','🎧','Lo-Fi',1,NULL,NULL,NULL),(5,'#39e27a','High-energy dance music with AI-crafted drops, builds, and hooks.','🔥','EDM',1,NULL,NULL,NULL),(6,'#4d79ff','Expansive, cosmic soundscapes inspired by the universe and AI imagination.','🚀','Space Ambient',1,NULL,NULL,NULL),(7,'#ff6b35','Catchy, hook-driven AI pop with indie sensibilities and experimental production.','🎸','Indie Pop',1,NULL,NULL,NULL),(8,'#ff6b9d','Catchy, hook-driven AI pop with mainstream appeal and polished production.','🎤','Pop',0,NULL,NULL,NULL),(9,'#e85d04','Guitar-driven AI compositions spanning classic rock to modern alternative.','🎸','Rock',0,NULL,NULL,NULL),(10,'#6a0dad','AI-generated beats, flows, and production spanning boom-bap to trap aesthetics.','🎤','Hip-Hop',0,NULL,NULL,NULL),(11,'#c77dff','Smooth AI rhythms and soulful melodies rooted in classic rhythm and blues.','🎵','R&B',0,NULL,NULL,NULL),(12,'#f4a261','AI-crafted storytelling with acoustic guitars, fiddles, and heartfelt lyrics.','🤠','Country',0,NULL,'2026-06-09 17:03:56.299611',_binary ''),(13,'#1d3557','Raw, emotive AI guitar and vocal compositions rooted in American blues tradition.','🎸','Blues',0,NULL,NULL,NULL),(14,'#e63946','Deep, emotive AI music celebrating the tradition of classic soul and Motown.','❤️','Soul',0,NULL,NULL,NULL),(15,'#a8dadc','Orchestral and chamber AI compositions influenced by Western classical tradition.','🎻','Classical',0,NULL,NULL,NULL),(16,'#d4a373','Acoustic storytelling AI music rooted in folk and Americana traditions.','🪕','Folk',0,NULL,'2026-06-09 17:51:49.748672',_binary ''),(17,'#f77f00','Four-to-the-floor AI house music with deep basslines and soulful samples.','🏠','House',0,NULL,NULL,NULL),(18,'#023e8a','Relentless, industrial AI techno built for the underground dance floor.','⚙️','Techno',0,NULL,NULL,NULL),(19,'#d00000','High-tempo AI breakbeats and heavy bass lines defining the DnB sound.','🥁','Drum & Bass',0,NULL,NULL,NULL),(20,'#240046','Bass-heavy AI music with signature wobble synths and half-time rhythms.','🔊','Dubstep',0,NULL,NULL,NULL),(21,'#370617','AI trap productions with rolling hi-hats, massive 808s, and dark atmospheres.','🎤','Trap',0,NULL,NULL,NULL),(22,'#7209b7','Retro-futuristic AI synthesizer music inspired by 80s film scores and new wave.','🌆','Synthwave',0,NULL,NULL,NULL),(23,'#74b3ce','Downtempo AI music perfect for relaxation, yoga, and winding down.','😌','Chillout',0,NULL,NULL,_binary '\0'),(24,'#7b2d8b','Independent-spirited AI music that defies mainstream conventions.','🎵','Indie',0,NULL,'2026-06-09 17:03:39.164239',_binary ''),(25,'#457b9d','Genre-blending AI music pushing boundaries of conventional rock and pop.','🎸','Alternative',0,NULL,'2026-06-09 17:51:34.457975',_binary ''),(26,'#495057','Distorted, raw AI guitar music channeling the angst of 90s Seattle grunge.','🎸','Grunge',0,NULL,NULL,NULL),(27,'#ff006e','Fast, aggressive AI music rooted in the DIY ethos and rebellious spirit of punk.','⚡','Punk',0,NULL,NULL,NULL),(28,'#6c757d','Heavy, distorted AI guitar music spanning classic metal to modern extremes.','🤘','Metal',0,NULL,NULL,NULL),(29,'#343a40','AI-powered wall-of-sound metal with crushing riffs and powerful drumming.','🤘','Heavy Metal',0,NULL,NULL,NULL),(30,'#212529','Extreme AI metal with blast beats, down-tuned guitars, and guttural vocals.','💀','Death Metal',0,NULL,NULL,NULL),(31,'#2b2d42','Complex AI compositions exploring extended song structures and odd time signatures.','🎵','Progressive Rock',0,NULL,NULL,NULL),(32,'#40916c','Groove-heavy AI music rooted in Jamaican reggae tradition and social consciousness.','🌿','Reggae',0,NULL,NULL,NULL),(33,'#ff6b35','AI-generated Latin rhythms spanning salsa, bachata, reggaeton, and bossa nova.','💃','Latin',0,NULL,NULL,NULL),(34,'#2d6a4f','High-energy AI African-inspired rhythms blending highlife, fuji, and jùjú.','🥁','Afrobeats',0,NULL,NULL,NULL),(35,'#ff85a1','Polished, energetic AI Korean pop with synchronized production and catchy hooks.','🌸','K-Pop',0,NULL,'2026-06-09 17:51:56.154102',_binary ''),(36,'#ffd60a','Uplifting AI faith-based music rooted in the tradition of gospel and spiritual.','✝️','Gospel',0,NULL,NULL,NULL),(37,'#fb8500','Groove-first AI music built on syncopated bass lines and tight rhythm sections.','🕺','Funk',0,NULL,NULL,NULL),(38,'#ffb703','AI dance music channeling the glittering, four-on-the-floor spirit of the 70s.','🕺','Disco',0,NULL,NULL,NULL),(39,'#e9c46a','AI explorations of global musical traditions, rhythms, and instrumentation.','🌍','World Music',0,NULL,NULL,NULL),(40,'#8a9b68','Acoustic AI string music rooted in Appalachian bluegrass and old-time traditions.','🪕','Bluegrass',0,NULL,'2026-06-09 19:34:48.042971',_binary ''),(41,'#52b788','Gentle, spiritually-inspired AI music designed for meditation and inner peace.','🧘','New Age',0,NULL,NULL,NULL),(42,'#6c757d','Boundary-pushing AI compositions that defy categorisation and challenge convention.','🔬','Experimental',0,NULL,NULL,NULL);
/*!40000 ALTER TABLE `genres` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inquiries`
--

DROP TABLE IF EXISTS `inquiries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inquiries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message_body` text NOT NULL,
  `received_date` datetime(6) NOT NULL,
  `sender_email` varchar(255) NOT NULL,
  `sender_name` varchar(255) NOT NULL,
  `subject` varchar(255) NOT NULL,
  `admin_reply` text,
  `is_read` bit(1) NOT NULL,
  `replied_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inquiries`
--

LOCK TABLES `inquiries` WRITE;
/*!40000 ALTER TABLE `inquiries` DISABLE KEYS */;
INSERT INTO `inquiries` VALUES (1,'000\neereooo','2026-06-08 14:46:24.144173','mikjcantrell@gmail.com','Michael Cantrell','Increase Rating','hdefel',_binary '','2026-06-08 14:50:18.257084'),(2,'Testing the contact form.','2026-06-09 15:56:12.114574','dlisee@gmail.com','Dan Lisee','Testing','Contact form works.  i need to send email but not there yet',_binary '','2026-06-09 17:25:56.554638');
/*!40000 ALTER TABLE `inquiries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `members`
--

DROP TABLE IF EXISTS `members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `members` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT NULL,
  `display_name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `genre_interest` varchar(255) DEFAULT NULL,
  `joined_at` datetime(6) DEFAULT NULL,
  `membership_tier` varchar(20) DEFAULT NULL,
  `newsletter_opt_in` bit(1) DEFAULT NULL,
  `primary_ai_tool` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `auth_provider` varchar(20) DEFAULT NULL,
  `provider_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_9d30a9u1qpg8eou0otgkwrp5d` (`email`),
  UNIQUE KEY `UK_lj4daw762ura5d2y6iu7g5n1i` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `members`
--

LOCK TABLES `members` WRITE;
/*!40000 ALTER TABLE `members` DISABLE KEYS */;
INSERT INTO `members` VALUES (11,_binary '','Dan Lisee','dlisee@gmail.com','Rock','2026-06-09 15:53:38.686669','PRODUCER',_binary '','Suno','@dlisee','$2a$10$wJOY2arYMEuLk0LJ5pVOq.CORRT34KPmsdAcnXMc4zGFtHZ7k3noW','LOCAL',NULL),(15,_binary '','Michael Cantrell','mikjcantrell@gmail.com',NULL,'2026-06-09 16:29:38.029349','PRODUCER',_binary '',NULL,NULL,NULL,'GOOGLE','100896884423601558221'),(16,_binary '','Matt Creator','mattcreator@example.com',NULL,'2026-06-10 18:53:14.535252','LISTENER',_binary '',NULL,NULL,'$2a$10$pz5MYrC6IjKfzZo1VU9WaedbsIOymfs7nK7BrQQSNSHaao1Bmj8PO','LOCAL',NULL);
/*!40000 ALTER TABLE `members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `new_releases`
--

DROP TABLE IF EXISTS `new_releases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `new_releases` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `featured_status` bit(1) DEFAULT NULL,
  `play_count` int DEFAULT NULL,
  `release_date` date NOT NULL,
  `spotlight_text` text,
  `track_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrslfdundt7in4ac8285vgsd4d` (`track_id`),
  CONSTRAINT `FKrslfdundt7in4ac8285vgsd4d` FOREIGN KEY (`track_id`) REFERENCES `tracks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `new_releases`
--

LOCK TABLES `new_releases` WRITE;
/*!40000 ALTER TABLE `new_releases` DISABLE KEYS */;
INSERT INTO `new_releases` VALUES (1,_binary '',0,'2026-05-23','A stunning debut that redefines what AI can do with electronic textures.',11),(2,_binary '',0,'2026-05-20','Haunting and beautiful — AmbientCore proves silence can be engineered.',12),(3,_binary '\0',0,'2026-05-17','Neural_Jazz\'s most ambitious release yet: pure algorithmic improvisation.',13),(4,_binary '\0',0,'2026-05-14','DawnAI captures the feeling of sunrise in a single 3-minute lo-fi track.',14),(5,_binary '\0',0,'2026-05-11','CosmicAI takes us to the edge of the universe with this hypnotic hymn.',15);
/*!40000 ALTER TABLE `new_releases` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pending_submissions`
--

DROP TABLE IF EXISTS `pending_submissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pending_submissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `artist_name` varchar(255) NOT NULL,
  `platform_type` varchar(255) DEFAULT NULL,
  `stream_url` varchar(1024) NOT NULL,
  `submission_date` datetime(6) NOT NULL,
  `submitter_email` varchar(255) NOT NULL,
  `tools_declared` varchar(512) DEFAULT NULL,
  `track_title` varchar(255) NOT NULL,
  `is_priority` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pending_submissions`
--

LOCK TABLES `pending_submissions` WRITE;
/*!40000 ALTER TABLE `pending_submissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `pending_submissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `platform_settings`
--

DROP TABLE IF EXISTS `platform_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `platform_settings` (
  `setting_key` varchar(255) NOT NULL,
  `setting_value` text NOT NULL,
  PRIMARY KEY (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `platform_settings`
--

LOCK TABLES `platform_settings` WRITE;
/*!40000 ALTER TABLE `platform_settings` DISABLE KEYS */;
INSERT INTO `platform_settings` VALUES ('about_mission','We champion AI music creators worldwide — spotlighting generative art built with Suno, Udio, Stable Audio, and beyond.'),('chart_refresh_hour','0'),('contact_email','mikjcantrell@gmail.com'),('featured_artist_days','14'),('featured_genre','Electronic'),('featured_track_days','14'),('GENRE_FEATURED_DAYS','14'),('GENRE_FEATURED_LIMIT','6'),('home_featured_track_limit','14'),('security_access_collab','CREATOR,PRODUCER'),('security_access_guides','CREATOR,PRODUCER'),('security_access_submit','CREATOR,PRODUCER'),('site_tagline','The Future of Music is Generated'),('site_title','Mikster Media AI'),('spotify_refresh_token','AQCqyBDTRyAZ-m2aYe_CDGt8o6lQFCMfB4mt92XRaSp-HRdSfzBNCzIxk2nwvl6M5L2oa8eHf65w8eTUnLMCsbwQyK9P__2ntMvkyNFqX3fK4nCTfFhY4ubtvnp4nPMYl9w');
/*!40000 ALTER TABLE `platform_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tracks`
--

DROP TABLE IF EXISTS `tracks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tracks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ai_source_url` varchar(1000) DEFAULT NULL,
  `ai_tools_used` varchar(512) DEFAULT NULL,
  `chartmetric_score` int DEFAULT NULL,
  `creator` varchar(255) NOT NULL,
  `display_order` int NOT NULL,
  `embed_url` varchar(1024) DEFAULT NULL,
  `featured_status` bit(1) DEFAULT NULL,
  `genre` varchar(255) DEFAULT NULL,
  `image_url` varchar(1024) DEFAULT NULL,
  `last_fm_scrobbles` int DEFAULT NULL,
  `last_week_chartmetric_score` int DEFAULT NULL,
  `last_week_last_fm_scrobbles` int DEFAULT NULL,
  `last_week_spotify_popularity` int DEFAULT NULL,
  `last_week_suno_likes` int DEFAULT NULL,
  `last_week_suno_plays` int DEFAULT NULL,
  `last_week_tiktok_views` int DEFAULT NULL,
  `last_week_udio_likes` int DEFAULT NULL,
  `last_week_udio_plays` int DEFAULT NULL,
  `last_week_youtube_views` bigint DEFAULT NULL,
  `media_url` varchar(1024) NOT NULL,
  `platform_source` varchar(255) NOT NULL,
  `release_date` varchar(255) DEFAULT NULL,
  `spotify_popularity` int DEFAULT NULL,
  `suno_likes` int DEFAULT NULL,
  `suno_plays` int DEFAULT NULL,
  `tiktok_views` int DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `udio_likes` int DEFAULT NULL,
  `udio_plays` int DEFAULT NULL,
  `video_url` varchar(1000) DEFAULT NULL,
  `youtube_views` int DEFAULT NULL,
  `prompt_recipe` text,
  `featured_until` date DEFAULT NULL,
  `chartmetric_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_track_title_creator` (`title`,`creator`)
) ENGINE=InnoDB AUTO_INCREMENT=278 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tracks`
--

LOCK TABLES `tracks` WRITE;
/*!40000 ALTER TABLE `tracks` DISABLE KEYS */;
INSERT INTO `tracks` VALUES (11,'https://suno.com/s/zNjsxPBSEba9Flxa','suno',0,'Marianna Dreams',0,'',_binary '','Country','https://i.scdn.co/image/ab67616d0000b2738545abded79819cd6ab6833f',0,0,0,0,429,4292,0,0,0,54819,'https://open.spotify.com/track/4LvdAmtQev8e3n9pSkXvlu?si=d89f2ea1d1fe4823','Spotify','2026-05-19',0,429,4292,0,'Honeysuckle Summer Breeze',NULL,0,'https://youtu.be/KluO6tDN4yg?si=-Ym4XW2ivmv7iGBi',195372,'','2026-06-16',NULL),(12,'https://suno.com/s/EkJCu6z13wJ4FUFm','',0,'JAX Cantrell',0,'https://open.spotify.com/embed/track/5m7KhFEIWDu9roXL4C93gF',_binary '','Pop','https://i.scdn.co/image/ab67616d0000b2739ba55dbe8fcc9f1392d9bb4c',0,0,0,0,406,4067,0,0,0,0,'https://open.spotify.com/track/5m7KhFEIWDu9roXL4C93gF','Spotify','2026-03-24',0,406,4067,0,'Embrace the Chaos',NULL,0,'',0,'','2026-06-16',NULL),(13,'https://suno.com/s/6aibxqk8pw69sdPo','',0,'JAX Cantrell',0,'https://open.spotify.com/embed/track/5tJ0UwriJB7qAJcctzTLdC',_binary '','Pop','https://i.scdn.co/image/ab67616d0000b2739ba55dbe8fcc9f1392d9bb4c',0,0,0,0,351,3518,0,0,0,0,'https://open.spotify.com/track/5tJ0UwriJB7qAJcctzTLdC','Spotify','2026-03-24',0,351,3518,0,'Go See Do',NULL,NULL,'',0,NULL,'2026-06-16',NULL),(14,'https://suno.com/s/74j7zkTSIJM5CKiJ','',0,'JAX Cantrell',0,'https://open.spotify.com/embed/track/41B9CHuVfLjDv4nXzOGlYG',_binary '\0','Pop','https://i.scdn.co/image/ab67616d0000b2739ba55dbe8fcc9f1392d9bb4c',0,0,0,0,235,2358,0,0,0,0,'https://open.spotify.com/track/41B9CHuVfLjDv4nXzOGlYG','Spotify','2026-03-24',0,235,2358,0,'Legend in the Making',NULL,NULL,'',0,NULL,NULL,NULL),(15,'https://suno.com/s/E81R6KoeNuX1j5F8','',0,'JAX Cantrell',0,'https://open.spotify.com/embed/track/6N4ttBc8XNop91RlvZIl0A',_binary '\0','Pop','https://i.scdn.co/image/ab67616d0000b2739ba55dbe8fcc9f1392d9bb4c',0,0,0,0,830,8306,0,0,0,0,'https://open.spotify.com/track/6N4ttBc8XNop91RlvZIl0A','Spotify','2026-03-24',0,830,8306,0,'I Do Remember',NULL,NULL,'',0,NULL,NULL,NULL),(16,'https://suno.com/s/EOvg1p7CFIhO5cns','',0,'JAX Cantrell',0,'https://open.spotify.com/embed/track/4lzgcfku53slWy1rS9aHoE',_binary '','Pop','https://i.scdn.co/image/ab67616d0000b2739ba55dbe8fcc9f1392d9bb4c',0,0,0,0,900,9005,0,0,0,58652,'https://open.spotify.com/track/4lzgcfku53slWy1rS9aHoE','Spotify','2026-03-24',0,900,9005,0,'Full Time Love',NULL,NULL,'https://youtu.be/sWHRKgQuhc4',58654,NULL,NULL,NULL),(17,'https://suno.com/s/NvEhbRArgI25GDHW','',0,'JAX Cantrell',0,'https://open.spotify.com/embed/track/4496b5e8lQVUyXmloPefhr',_binary '\0','Pop','https://i.scdn.co/image/ab67616d0000b273ffb62e5304c4420514400554',0,0,0,0,896,8967,0,0,0,0,'https://open.spotify.com/track/4496b5e8lQVUyXmloPefhr','Spotify','2026-05-02',0,896,8967,0,'Be Kind',NULL,NULL,'',0,NULL,NULL,NULL),(18,'https://suno.com/s/jokLa77SjagNgoCq','',0,'JAX Cantrell',0,'https://open.spotify.com/embed/track/3wH0Z2Ady6GYEHpQkuVmTM',_binary '','Pop','https://i.scdn.co/image/ab67616d0000b2739ba55dbe8fcc9f1392d9bb4c',0,0,0,0,224,2246,0,0,0,68001,'https://open.spotify.com/track/3wH0Z2Ady6GYEHpQkuVmTM','Spotify','2026-03-24',0,224,2246,0,'Angry, Beautiful World',NULL,0,'https://youtu.be/soYLDEisxDQ',68005,'',NULL,NULL),(19,'https://suno.com/s/HY53NxCUtmLu3Wo4','',0,'JAX Cantrell',0,'https://open.spotify.com/embed/track/4hrUuVMMddWYa1UnQVvzIV',_binary '\0','Pop','https://i.scdn.co/image/ab67616d0000b2739ba55dbe8fcc9f1392d9bb4c',0,0,0,0,839,8392,0,0,0,0,'https://open.spotify.com/track/4hrUuVMMddWYa1UnQVvzIV','Spotify','2026-03-24',0,839,8392,0,'Central Park, Autumn Leaves',NULL,NULL,'',0,NULL,NULL,NULL),(20,'https://suno.com/s/EkJCu6z13wJ4FUFm','',0,'JAX Cantrell',0,'https://open.spotify.com/embed/track/4ZK0P8aRMiauk4sbT1OYDx',_binary '','Pop','https://i.scdn.co/image/ab67616d0000b273a235ff0f1f170716b0a538dd',0,0,0,0,406,4067,0,0,0,140343,'https://open.spotify.com/track/4ZK0P8aRMiauk4sbT1OYDx','Spotify','2026-03-12',0,406,4067,0,'Retro Blonde',NULL,0,'https://youtu.be/qZc-Fbpfa4E',140345,'','2026-06-23',NULL),(21,'https://suno.com/s/N2oRSQbx3nypseN6','',0,'JAX Cantrell',0,'https://open.spotify.com/embed/track/0hQo3aaTLzFSAZWIknDIC2',_binary '\0','Pop','https://i.scdn.co/image/ab67616d0000b2739ba55dbe8fcc9f1392d9bb4c',0,0,0,0,307,3079,0,0,0,0,'https://open.spotify.com/track/0hQo3aaTLzFSAZWIknDIC2','Spotify','2026-03-24',0,307,3079,0,'Bourbon Soothes the Soul',NULL,NULL,'',0,NULL,NULL,NULL),(22,'https://suno.com/s/uihEWzi8Osqs3hFU','',0,'JAX Cantrell',0,'https://open.spotify.com/embed/track/5mXZVqXCnjne0EYNalNkGA',_binary '\0','Pop','https://i.scdn.co/image/ab67616d0000b2739ba55dbe8fcc9f1392d9bb4c',0,0,0,0,416,4167,0,0,0,0,'https://open.spotify.com/track/5mXZVqXCnjne0EYNalNkGA','Spotify','2026-03-24',0,416,4167,0,'Kissing Goodbye',NULL,NULL,'',0,NULL,NULL,NULL),(23,'https://suno.com/s/dB0eKvhAuH39hFEG','',0,'JAX Cantrell',0,'https://open.spotify.com/embed/track/4xBlnSq0fgFaR2igJTlgnp',_binary '\0','Pop','https://i.scdn.co/image/ab67616d0000b2739ba55dbe8fcc9f1392d9bb4c',0,0,0,0,249,2494,0,0,0,0,'https://open.spotify.com/track/4xBlnSq0fgFaR2igJTlgnp','Spotify','2026-03-24',0,249,2494,0,'Miles of Grace',NULL,NULL,'',0,NULL,NULL,NULL),(24,'https://suno.com/s/sPmNMMJ5PeJOUXra','',0,'Marianna Dreams',0,'https://open.spotify.com/embed/track/6iuGWZytfTqKAVKVBlzfIj',_binary '','Country','https://i.scdn.co/image/ab67616d0000b2738545abded79819cd6ab6833f',0,0,0,0,949,9497,0,0,0,0,'https://open.spotify.com/track/6iuGWZytfTqKAVKVBlzfIj','Spotify','2026-05-19',0,949,9497,0,'Leaving Marianna',NULL,0,'',0,NULL,NULL,NULL),(25,'https://suno.com/s/vfCVEZ9YiBTLa2qt','',0,'Marianna Dreams',0,'https://open.spotify.com/embed/track/2Vge8kJ7fKVsgjtu3PHpVj',_binary '\0','Country','https://i.scdn.co/image/ab67616d0000b2738545abded79819cd6ab6833f',0,0,0,0,660,6609,0,0,0,0,'https://open.spotify.com/track/2Vge8kJ7fKVsgjtu3PHpVj','Spotify','2026-05-19',0,660,6609,0,'Roots and Wings',NULL,NULL,'',0,NULL,NULL,NULL),(26,'https://suno.com/s/ux6zPPqcdUyMCfJV','',0,'Marianna Dreams',0,'https://open.spotify.com/embed/track/0szYhZb5JknADWf0SDPIu4',_binary '\0','Country','https://i.scdn.co/image/ab67616d0000b2738545abded79819cd6ab6833f',0,0,0,0,451,4513,0,0,0,0,'https://open.spotify.com/track/0szYhZb5JknADWf0SDPIu4','Spotify','2026-05-19',0,451,4513,0,'Friday Night Forever',NULL,0,'',0,NULL,NULL,NULL),(27,'https://suno.com/s/rYtLizSbYg850HjB','',0,'Marianna Dreams',0,'https://open.spotify.com/embed/track/5H6jc2yr58p1AJGv2RyFlt',_binary '\0','Country','https://i.scdn.co/image/ab67616d0000b2738545abded79819cd6ab6833f',0,0,0,0,256,2564,0,0,0,0,'https://open.spotify.com/track/5H6jc2yr58p1AJGv2RyFlt','Spotify','2026-05-19',0,256,2564,0,'Enough',NULL,0,'',0,NULL,NULL,NULL),(28,'https://suno.com/s/ShYbec1fx8xFeOLe','',0,'Marianna Dreams',0,'https://open.spotify.com/embed/track/3GySTzf94pzmV1S5ebaGNP',_binary '','Country','https://i.scdn.co/image/ab67616d0000b2738545abded79819cd6ab6833f',0,0,0,0,745,7452,0,0,0,0,'https://open.spotify.com/track/3GySTzf94pzmV1S5ebaGNP','Spotify','2026-05-19',0,745,7452,0,'Down the Juke Joint Line',NULL,0,'',0,NULL,'2026-06-16',NULL),(29,'https://suno.com/s/zFd9V7LgNcppFK6Z','',0,'Marianna Dreams',0,'https://open.spotify.com/embed/track/3oVIwsE0s9IJtyM3keTLf2',_binary '','Country','https://i.scdn.co/image/ab67616d0000b2738545abded79819cd6ab6833f',0,0,0,0,326,3269,0,0,0,0,'https://open.spotify.com/track/3oVIwsE0s9IJtyM3keTLf2','Spotify','2026-05-19',0,326,3269,0,'Small Town Saints',NULL,0,'',0,NULL,'2026-06-16',NULL),(30,'https://suno.com/s/NEO5PLT0Dnocdzoj','',0,'Marianna Dreams',0,'https://open.spotify.com/embed/track/5tNhHoY4pzhi4amInYAwJ0',_binary '\0','Country','https://i.scdn.co/image/ab67616d0000b2738545abded79819cd6ab6833f',0,0,0,0,795,7950,0,0,0,0,'https://open.spotify.com/track/5tNhHoY4pzhi4amInYAwJ0','Spotify','2026-05-19',0,795,7950,0,'Where the River Bends',NULL,0,'',0,NULL,NULL,NULL),(31,'https://suno.com/s/S93HZqYpNdFpxKqP','',0,'Marianna Dreams',0,'https://open.spotify.com/embed/track/7gzjoNgZtPcBunZsY44AjX',_binary '\0','Country','https://i.scdn.co/image/ab67616d0000b2738545abded79819cd6ab6833f',0,0,0,0,598,5987,0,0,0,0,'https://open.spotify.com/track/7gzjoNgZtPcBunZsY44AjX','Spotify','2026-05-19',0,598,5987,0,'Porch Light Left On',NULL,0,'',0,NULL,NULL,NULL),(32,'https://suno.com/s/MoNG7ZII7rIYzEqT','',0,'Marianna Dreams',0,'https://open.spotify.com/embed/track/5W1zfkbegTDp94AhHof42m',_binary '\0','Country','https://i.scdn.co/image/ab67616d0000b2738545abded79819cd6ab6833f',0,0,0,0,288,2881,0,0,0,0,'https://open.spotify.com/track/5W1zfkbegTDp94AhHof42m','Spotify','2026-05-19',0,288,2881,0,'I Wanna be Present (with you)',NULL,0,'',0,NULL,NULL,NULL),(33,'https://suno.com/s/6HRqpmWABmGCxiOB','',0,'Marianna Dreams',0,'https://open.spotify.com/embed/track/2iSKFVmFjV51Rqpmm4oW8f',_binary '\0','Country','https://i.scdn.co/image/ab67616d0000b2738545abded79819cd6ab6833f',0,0,0,0,900,9005,0,0,0,0,'https://open.spotify.com/track/2iSKFVmFjV51Rqpmm4oW8f','Spotify','2026-05-19',0,900,9005,0,'Wildflower Mile',NULL,0,'',0,NULL,NULL,NULL),(34,'https://suno.com/s/zQ3ISyOIrmB58VFb','',0,'Marianna Dreams',0,'https://open.spotify.com/embed/track/4hGULOdTSTBXacx9gHdEB2',_binary '\0','Country','https://i.scdn.co/image/ab67616d0000b2738545abded79819cd6ab6833f',0,0,0,0,443,4431,0,0,0,0,'https://open.spotify.com/track/4hGULOdTSTBXacx9gHdEB2','Spotify','2026-05-19',0,443,4431,0,'Faded Blue Jeans',NULL,0,'',0,NULL,NULL,NULL),(35,'https://suno.com/s/0V54BgJ2OCOCRlea','',0,'Marianna Dreams',0,'https://open.spotify.com/embed/track/0DpgXDUfG0Cvr5cqesYcPC',_binary '\0','Country','https://i.scdn.co/image/ab67616d0000b2738545abded79819cd6ab6833f',0,0,0,0,929,9294,0,0,0,0,'https://open.spotify.com/track/0DpgXDUfG0Cvr5cqesYcPC','Spotify','2026-05-19',0,929,9294,0,'Ghosts in the Rearview',NULL,0,'',0,NULL,NULL,NULL),(248,NULL,'',0,'Heart, SunoAI',0,'https://open.spotify.com/embed/track/3Kb0o7Q409LyKNEgjQ1nUR',_binary '\0','','https://i.scdn.co/image/ab67616d0000b273f6b783c5beb8a75f1d01df13',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/3Kb0o7Q409LyKNEgjQ1nUR','Spotify','2026-06-06',0,0,0,0,'Premium Bourbon (2026)',0,0,NULL,0,NULL,NULL,NULL),(249,NULL,'',0,'Cross Bone Tears, AI Generated Music',0,'https://open.spotify.com/embed/track/7uqNxcvTk1illnkRKgnLrB',_binary '\0','','https://i.scdn.co/image/ab67616d0000b273307e11689b7369426cb87c10',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/7uqNxcvTk1illnkRKgnLrB','Spotify','2026-06-09',0,0,0,0,'Jack and Jill continued…',0,0,NULL,0,NULL,NULL,NULL),(250,NULL,'',0,'Cross Bone Tears, AI Generated Music',0,'https://open.spotify.com/embed/track/1I6LbVV2tR0iBZkT5z1Paf',_binary '\0','','https://i.scdn.co/image/ab67616d0000b273307e11689b7369426cb87c10',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/1I6LbVV2tR0iBZkT5z1Paf','Spotify','2026-06-09',0,0,0,0,'Pop Goes the Weasel',0,0,NULL,0,NULL,NULL,NULL),(251,NULL,'',0,'Cross Bone Tears, AI Generated Music',0,'https://open.spotify.com/embed/track/1jogY3R1olXE58emv1IONT',_binary '\0','','https://i.scdn.co/image/ab67616d0000b273307e11689b7369426cb87c10',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/1jogY3R1olXE58emv1IONT','Spotify','2026-06-09',0,0,0,0,'Twinkle Twinkle',0,0,NULL,0,NULL,NULL,NULL),(252,NULL,'',0,'Cross Bone Tears, AI Generated Music',0,'https://open.spotify.com/embed/track/5DHw0alMXFqvaqrwEjqdfH',_binary '\0','','https://i.scdn.co/image/ab67616d0000b273307e11689b7369426cb87c10',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/5DHw0alMXFqvaqrwEjqdfH','Spotify','2026-06-09',0,0,0,0,'3 Blind Mice',0,0,NULL,0,NULL,NULL,NULL),(253,NULL,'',0,'Cross Bone Tears, AI Generated Music',0,'https://open.spotify.com/embed/track/0zqTJzCEaQcEGVtDLlZJyf',_binary '\0','','https://i.scdn.co/image/ab67616d0000b273307e11689b7369426cb87c10',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/0zqTJzCEaQcEGVtDLlZJyf','Spotify','2026-06-09',0,0,0,0,'Peter Cottontail',0,0,NULL,0,NULL,NULL,NULL),(254,NULL,'',0,'Cross Bone Tears, AI Generated Music',0,'https://open.spotify.com/embed/track/4XD6ymHIZdpzQqAO9gPLpP',_binary '\0','','https://i.scdn.co/image/ab67616d0000b273307e11689b7369426cb87c10',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/4XD6ymHIZdpzQqAO9gPLpP','Spotify','2026-06-09',0,0,0,0,'Hush-a-bye Baby',0,0,NULL,0,NULL,NULL,NULL),(255,NULL,'',0,'Cross Bone Tears, AI Generated Music',0,'https://open.spotify.com/embed/track/1GBmp8Ya6i3Yar4mr8g00l',_binary '\0','','https://i.scdn.co/image/ab67616d0000b273307e11689b7369426cb87c10',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/1GBmp8Ya6i3Yar4mr8g00l','Spotify','2026-06-09',0,0,0,0,'Mary Mary',0,0,NULL,0,NULL,NULL,NULL),(256,NULL,'',0,'Cross Bone Tears, AI Generated Music',0,'https://open.spotify.com/embed/track/0SCMmokwT88OqFEaTCl15z',_binary '\0','','https://i.scdn.co/image/ab67616d0000b273307e11689b7369426cb87c10',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/0SCMmokwT88OqFEaTCl15z','Spotify','2026-06-09',0,0,0,0,'Lizzie Borden',0,0,NULL,0,NULL,NULL,NULL),(257,NULL,'',0,'Cross Bone Tears, AI Generated Music',0,'https://open.spotify.com/embed/track/4x8pKripuwP4oTU1UI2L9l',_binary '\0','','https://i.scdn.co/image/ab67616d0000b273307e11689b7369426cb87c10',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/4x8pKripuwP4oTU1UI2L9l','Spotify','2026-06-09',0,0,0,0,'3 Little Pigs',0,0,NULL,0,NULL,NULL,NULL),(258,NULL,'',0,'Cross Bone Tears, AI Generated Music',0,'https://open.spotify.com/embed/track/0l4od0OI3mCCS2aEfv6EVJ',_binary '\0','','https://i.scdn.co/image/ab67616d0000b273307e11689b7369426cb87c10',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/0l4od0OI3mCCS2aEfv6EVJ','Spotify','2026-06-09',0,0,0,0,'The Pied Piper',0,0,NULL,0,NULL,NULL,NULL),(259,NULL,'',0,'A.I.M. Artificial Intelligence Music',0,'https://open.spotify.com/embed/track/4VK2dFfeXmN3QDOzSDwc8W',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2739dbd5064a23153b9d0b4080a',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/4VK2dFfeXmN3QDOzSDwc8W','Spotify','2026-06-13',0,0,0,0,'Donesi te pehar u Bosnu (Zmajevi)',0,0,NULL,0,NULL,NULL,NULL),(260,NULL,'',0,'MisterLEVIK',0,'https://open.spotify.com/embed/track/0MXsZOU2dz0tq3N756oxFw',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2731aa3416797db215e481c934c',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/0MXsZOU2dz0tq3N756oxFw','Spotify','2026-06-04',0,0,0,0,'Montagem Artificial Intelligence - Sped Up',0,0,NULL,0,NULL,NULL,NULL),(261,NULL,'',0,'MisterLEVIK',0,'https://open.spotify.com/embed/track/6rEzlw9dfLGsaT557Rzefg',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2731aa3416797db215e481c934c',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/6rEzlw9dfLGsaT557Rzefg','Spotify','2026-06-04',0,0,0,0,'Montagem Artificial Intelligence - Slowed',0,0,NULL,0,NULL,NULL,NULL),(262,NULL,'',0,'MisterLEVIK',0,'https://open.spotify.com/embed/track/01GwYukOQQcUrLlN8baGdc',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2731aa3416797db215e481c934c',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/01GwYukOQQcUrLlN8baGdc','Spotify','2026-06-04',0,0,0,0,'Montagem Artificial Intelligence - Super Slowed',0,0,NULL,0,NULL,NULL,NULL),(263,NULL,'',0,'Greg “Dr. C” Calliste',0,'https://open.spotify.com/embed/track/3sslO9pdhqngm1e03my10H',_binary '\0','','https://i.scdn.co/image/ab67616d0000b27317b6eb5893d21d2dda836bbb',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/3sslO9pdhqngm1e03my10H','Spotify','2026-05-29',0,0,0,0,'Artificial Intelligence',0,0,NULL,0,NULL,NULL,NULL),(264,NULL,'',0,'Spike Polite & Sewage',0,'https://open.spotify.com/embed/track/5eGpTmKb9gU7kST46tsOPr',_binary '\0','','https://i.scdn.co/image/ab67616d0000b27372b2d2e2bbce6799e95d4221',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/5eGpTmKb9gU7kST46tsOPr','Spotify','2026-06-13',0,0,0,0,'Artificial Intelligence',0,0,NULL,0,NULL,NULL,NULL),(265,NULL,'',0,'Gabby Moon',0,'https://open.spotify.com/embed/track/3G5LBKOovhbJCkZhu08RdA',_binary '\0','','https://i.scdn.co/image/ab67616d0000b273e786fdb99b7abfb7a056ac42',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/3G5LBKOovhbJCkZhu08RdA','Spotify','2026-06-05',0,0,0,0,'New Digital App called “Glitch” Artificial Intelligence',0,0,NULL,0,NULL,NULL,NULL),(266,NULL,'',0,'MisterLEVIK',0,'https://open.spotify.com/embed/track/4kxXDWF26kT8Vc246al3Xh',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2731aa3416797db215e481c934c',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/4kxXDWF26kT8Vc246al3Xh','Spotify','2026-06-04',0,0,0,0,'Montagem Artificial Intelligence - Ultra Slowed',0,0,NULL,0,NULL,NULL,NULL),(267,NULL,'',0,'MisterLEVIK',0,'https://open.spotify.com/embed/track/32k1IWFAIRjuaa1XQNLitj',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2731aa3416797db215e481c934c',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/32k1IWFAIRjuaa1XQNLitj','Spotify','2026-06-04',0,0,0,0,'Montagem Artificial Intelligence',0,0,NULL,0,NULL,NULL,NULL),(268,NULL,'',0,'Suno AI, Thien Phearin, GenerativeAI, Aussa',0,'https://open.spotify.com/embed/track/1u9TFuD0EGwMlx6YoQfkbZ',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2732e7da53a76c5ee7012e793e8',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/1u9TFuD0EGwMlx6YoQfkbZ','Spotify','2026-06-15',0,0,0,0,'Moonlit Lotus Dynasty (Chinese Traditional Instrument)',0,0,NULL,0,NULL,NULL,NULL),(269,NULL,'',0,'Suno AI, Thien Phearin, GenerativeAI, Aussa',0,'https://open.spotify.com/embed/track/1CWoHARRuXiJRNVwzPE9MW',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2732e7da53a76c5ee7012e793e8',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/1CWoHARRuXiJRNVwzPE9MW','Spotify','2026-06-15',0,0,0,0,'Lotus by the Willow Bridge (Chinese Traditional Instrument)',0,0,NULL,0,NULL,NULL,NULL),(270,NULL,'',0,'Suno AI, GenerativeAI, Thien Phearin, Aussa',0,'https://open.spotify.com/embed/track/6RO8kUZJczODC1r56WqBG4',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2732e7da53a76c5ee7012e793e8',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/6RO8kUZJczODC1r56WqBG4','Spotify','2026-06-15',0,0,0,0,'Lotus Harbor Dawn (Chinese Traditional Instrument)',0,0,NULL,0,NULL,NULL,NULL),(271,NULL,'',0,'Suno AI, GenerativeAI, Thien Phearin, Aussa',0,'https://open.spotify.com/embed/track/63UwPQuZkLVQQ5KR8wmanK',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2732e7da53a76c5ee7012e793e8',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/63UwPQuZkLVQQ5KR8wmanK','Spotify','2026-06-15',0,0,0,0,'Lotus in the Jade Garden (Chinese Traditional Instrument)',0,0,NULL,0,NULL,NULL,NULL),(272,NULL,'',0,'Suno AI, Thien Phearin, GenerativeAI, Aussa',0,'https://open.spotify.com/embed/track/7BXbdOCBbnUi9Mf0XdUenC',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2732e7da53a76c5ee7012e793e8',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/7BXbdOCBbnUi9Mf0XdUenC','Spotify','2026-06-15',0,0,0,0,'Lotus Cloud Temple',0,0,NULL,0,NULL,NULL,NULL),(273,NULL,'',0,'Suno AI, Thien Phearin, GenerativeAI, Aussa',0,'https://open.spotify.com/embed/track/2Q9j8wKUNz2ckThmn4Hl40',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2732e7da53a76c5ee7012e793e8',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/2Q9j8wKUNz2ckThmn4Hl40','Spotify','2026-06-15',0,0,0,0,'Jade Valley Lotus (Chinese Traditional Instrument)',0,0,NULL,0,NULL,NULL,NULL),(274,NULL,'',0,'Suno AI, Thien Phearin, GenerativeAI, Aussa',0,'https://open.spotify.com/embed/track/0TRQtdxRi6ZVIEZhlHw5lr',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2732e7da53a76c5ee7012e793e8',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/0TRQtdxRi6ZVIEZhlHw5lr','Spotify','2026-06-15',0,0,0,0,'Lotus River Drift (Chinese Traditional Instrument)',0,0,NULL,0,NULL,NULL,NULL),(275,NULL,'',0,'Suno AI, Thien Phearin, GenerativeAI, Aussa',0,'https://open.spotify.com/embed/track/7McgyF777dNlNrRM5Q2k8P',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2732e7da53a76c5ee7012e793e8',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/7McgyF777dNlNrRM5Q2k8P','Spotify','2026-06-15',0,0,0,0,'Lotus Fragrance on the Breeze (Chinese Traditional Instrument)',0,0,NULL,0,NULL,NULL,NULL),(276,NULL,'',0,'Suno AI, Thien Phearin, GenerativeAI, Aussa',0,'https://open.spotify.com/embed/track/1KD25AcDlJRdmV1GUeuJ52',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2732e7da53a76c5ee7012e793e8',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/1KD25AcDlJRdmV1GUeuJ52','Spotify','2026-06-15',0,0,0,0,'Lotus of the Emerald Lake (Chinese Traditional Instrument)',0,0,NULL,0,NULL,NULL,NULL),(277,NULL,'',0,'Suno AI, Thien Phearin, GenerativeAI, Aussa',0,'https://open.spotify.com/embed/track/4UUj5SXLT0R7vYFnRVmOTu',_binary '\0','','https://i.scdn.co/image/ab67616d0000b2732e7da53a76c5ee7012e793e8',0,0,0,0,0,0,0,0,0,0,'https://open.spotify.com/track/4UUj5SXLT0R7vYFnRVmOTu','Spotify','2026-06-15',0,0,0,0,'Lotus Courtyard Melody (Chinese Traditional Instrument)',0,0,NULL,0,NULL,NULL,NULL);
/*!40000 ALTER TABLE `tracks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `weekly_charts`
--

DROP TABLE IF EXISTS `weekly_charts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_charts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `current_rank` int NOT NULL,
  `previous_rank` int DEFAULT NULL,
  `rank_change` varchar(10) DEFAULT NULL,
  `upvote_count` int NOT NULL,
  `weekly_plays` int NOT NULL,
  `track_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh6dun62d11mqal8340rsmc54s` (`track_id`),
  CONSTRAINT `FKh6dun62d11mqal8340rsmc54s` FOREIGN KEY (`track_id`) REFERENCES `tracks` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `weekly_charts`
--

LOCK TABLES `weekly_charts` WRITE;
/*!40000 ALTER TABLE `weekly_charts` DISABLE KEYS */;
INSERT INTO `weekly_charts` VALUES (100,1,1,'STEADY',0,0,11),(102,2,2,'STEADY',0,0,20),(103,3,3,'STEADY',0,0,16),(104,4,4,'STEADY',0,0,18),(105,5,5,'STEADY',0,0,24),(106,6,6,'STEADY',0,0,35),(107,7,7,'STEADY',0,0,33),(108,8,8,'STEADY',0,0,17),(109,9,9,'STEADY',0,0,30),(110,10,10,'STEADY',0,0,28);
/*!40000 ALTER TABLE `weekly_charts` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-16 12:11:53
