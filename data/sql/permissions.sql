-- regular expression to convert Java enum to SQL in emacs:
--   from: ^\s-+\([^(]+\)(\([^)]+\))
--   to: (\2, 0, '\1')

LOCK TABLES `PERMISSION` WRITE;
INSERT INTO `PERMISSION` (ID, VERSION, CLASS_NAME) VALUES
(1, 0, 'ADMINISTRATION'),

(2, 0, 'SPECIMEN_CREATE'),
(3, 0, 'SPECIMEN_READ'),
(4, 0, 'SPECIMEN_UPDATE'),
(5, 0, 'SPECIMEN_DELETE'),
(6, 0, 'SPECIMEN_LINK'),
(7, 0, 'SPECIMEN_ASSIGN'),

(8, 0, 'SITE_CREATE'),
(9, 0, 'SITE_READ'),
(10, 0, 'SITE_UPDATE'),
(11, 0, 'SITE_DELETE'),

(12, 0, 'PATIENT_CREATE'),
(13, 0, 'PATIENT_READ'),
(14, 0, 'PATIENT_UPDATE'),
(15, 0, 'PATIENT_DELETE'),
(16, 0, 'PATIENT_MERGE'),

(17, 0, 'COLLECTION_EVENT_CREATE'),
(18, 0, 'COLLECTION_EVENT_READ'),
(19, 0, 'COLLECTION_EVENT_UPDATE'),
(20, 0, 'COLLECTION_EVENT_DELETE'),

(21, 0, 'PROCESSING_EVENT_CREATE'),
(22, 0, 'PROCESSING_EVENT_READ'),
(23, 0, 'PROCESSING_EVENT_UPDATE'),
(24, 0, 'PROCESSING_EVENT_DELETE'),

(25, 0, 'ORIGIN_INFO_CREATE'),
(26, 0, 'ORIGIN_INFO_READ'),
(27, 0, 'ORIGIN_INFO_UPDATE'),
(28, 0, 'ORIGIN_INFO_DELETE'),

(29, 0, 'DISPATCH_CREATE'),
(30, 0, 'DISPATCH_READ'),
(31, 0, 'DISPATCH_CHANGE_STATE'),
(32, 0, 'DISPATCH_UPDATE'),
(33, 0, 'DISPATCH_DELETE'),

(34, 0, 'RESEARCH_GROUP_CREATE'),
(35, 0, 'RESEARCH_GROUP_READ'),
(36, 0, 'RESEARCH_GROUP_UPDATE'),
(37, 0, 'RESEARCH_GROUP_DELETE'),

(38, 0, 'STUDY_CREATE'),
(39, 0, 'STUDY_READ'),
(40, 0, 'STUDY_UPDATE'),
(41, 0, 'STUDY_DELETE'),

(42, 0, 'REQUEST_CREATE'),
(43, 0, 'REQUEST_READ'),
(44, 0, 'REQUEST_UPDATE'),
(45, 0, 'REQUEST_DELETE'),

(46, 0, 'REQUEST_PROCESS'),

(47, 0, 'CLINIC_CREATE'),
(48, 0, 'CLINIC_READ'),
(49, 0, 'CLINIC_UPDATE'),
(50, 0, 'CLINIC_DELETE'),

(51, 0, 'USER_MANAGEMENT'),

(52, 0, 'CONTAINER_TYPE_CREATE'),
(53, 0, 'CONTAINER_TYPE_READ'),
(54, 0, 'CONTAINER_TYPE_UPDATE'),
(55, 0, 'CONTAINER_TYPE_DELETE'),

(56, 0, 'CONTAINER_CREATE'),
(57, 0, 'CONTAINER_READ'),
(58, 0, 'CONTAINER_UPDATE'),
(59, 0, 'CONTAINER_DELETE'),

(60, 0, 'SPECIMEN_TYPE_CREATE'),
(61, 0, 'SPECIMEN_TYPE_READ'),
(62, 0, 'SPECIMEN_TYPE_UPDATE'),
(63, 0, 'SPECIMEN_TYPE_DELETE'),

(64, 0, 'LOGGING'),
(65, 0, 'REPORTS');

UNLOCK TABLES;
