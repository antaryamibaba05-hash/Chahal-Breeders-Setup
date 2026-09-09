package com.highflyerpro.tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.highflyerpro.tracker.data.local.dao.AchievementDao
import com.highflyerpro.tracker.data.local.dao.BreedingDao
import com.highflyerpro.tracker.data.local.dao.BreedingEventDao
import com.highflyerpro.tracker.data.local.dao.BreedingPairDao
import com.highflyerpro.tracker.data.local.dao.DewormingDao
import com.highflyerpro.tracker.data.local.dao.DiseaseDao
import com.highflyerpro.tracker.data.local.dao.EventDao
import com.highflyerpro.tracker.data.local.dao.FlightDao
import com.highflyerpro.tracker.data.local.dao.GroupDao
import com.highflyerpro.tracker.data.local.dao.HealthDao
import com.highflyerpro.tracker.data.local.dao.MedicationDao
import com.highflyerpro.tracker.data.local.dao.NoteDao
import com.highflyerpro.tracker.data.local.dao.NutritionDao
import com.highflyerpro.tracker.data.local.dao.PhotoDao
import com.highflyerpro.tracker.data.local.dao.PigeonDao
import com.highflyerpro.tracker.data.local.dao.PigeonPhotoDao
import com.highflyerpro.tracker.data.local.dao.ReminderDao
import com.highflyerpro.tracker.data.local.dao.SettingsDao
import com.highflyerpro.tracker.data.local.dao.SupplementDao
import com.highflyerpro.tracker.data.local.dao.TagDao
import com.highflyerpro.tracker.data.local.dao.TournamentDao
import com.highflyerpro.tracker.data.local.dao.WeightDao
import com.highflyerpro.tracker.data.local.entities.AchievementEntity
import com.highflyerpro.tracker.data.local.entities.AppSettingsEntity
import com.highflyerpro.tracker.data.local.entities.BreedingEventEntity
import com.highflyerpro.tracker.data.local.entities.BreedingPairEntity
import com.highflyerpro.tracker.data.local.entities.BreedingRecordEntity
import com.highflyerpro.tracker.data.local.entities.DewormingRecordEntity
import com.highflyerpro.tracker.data.local.entities.DiseaseRecordEntity
import com.highflyerpro.tracker.data.local.entities.FeedingRecordEntity
import com.highflyerpro.tracker.data.local.entities.FlightRecordEntity
import com.highflyerpro.tracker.data.local.entities.FlightSessionEntity
import com.highflyerpro.tracker.data.local.entities.FoodEntity
import com.highflyerpro.tracker.data.local.entities.FoodInventoryEntity
import com.highflyerpro.tracker.data.local.entities.GroupEntity
import com.highflyerpro.tracker.data.local.entities.GroupMemberEntity
import com.highflyerpro.tracker.data.local.entities.HealthRecordEntity
import com.highflyerpro.tracker.data.local.entities.MedicationRecordEntity
import com.highflyerpro.tracker.data.local.entities.NoteEntity
import com.highflyerpro.tracker.data.local.entities.PhotoEntity
import com.highflyerpro.tracker.data.local.entities.PigeonEntity
import com.highflyerpro.tracker.data.local.entities.PigeonEventEntity
import com.highflyerpro.tracker.data.local.entities.PigeonPhoto
import com.highflyerpro.tracker.data.local.entities.PigeonTagEntity
import com.highflyerpro.tracker.data.local.entities.PointRuleEntity
import com.highflyerpro.tracker.data.local.entities.ReminderEntity
import com.highflyerpro.tracker.data.local.entities.SeasonEntity
import com.highflyerpro.tracker.data.local.entities.SupplementRecordEntity
import com.highflyerpro.tracker.data.local.entities.TagEntity
import com.highflyerpro.tracker.data.local.entities.TournamentEntity
import com.highflyerpro.tracker.data.local.entities.TournamentParticipantEntity
import com.highflyerpro.tracker.data.local.entities.TournamentResultEntity
import com.highflyerpro.tracker.data.local.entities.WeightRecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PigeonEntity::class,
        FlightSessionEntity::class,
        FlightRecordEntity::class,
        PigeonEventEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        HealthRecordEntity::class,
        BreedingRecordEntity::class,
        PhotoEntity::class,
        PigeonPhoto::class,
        NoteEntity::class,
        AchievementEntity::class,
        SeasonEntity::class,
        TournamentEntity::class,
        TournamentParticipantEntity::class,
        TournamentResultEntity::class,
        PointRuleEntity::class,
        AppSettingsEntity::class,
        // LoftMaster Pro Extensions
        BreedingPairEntity::class,
        BreedingEventEntity::class,
        MedicationRecordEntity::class,
        DewormingRecordEntity::class,
        SupplementRecordEntity::class,
        FoodEntity::class,
        FeedingRecordEntity::class,
        FoodInventoryEntity::class,
        DiseaseRecordEntity::class,
        WeightRecordEntity::class,
        ReminderEntity::class,
        TagEntity::class,
        PigeonTagEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pigeonDao(): PigeonDao
    abstract fun flightDao(): FlightDao
    abstract fun groupDao(): GroupDao
    abstract fun eventDao(): EventDao
    abstract fun healthDao(): HealthDao
    abstract fun breedingDao(): BreedingDao
    abstract fun photoDao(): PhotoDao
    abstract fun pigeonPhotoDao(): PigeonPhotoDao
    abstract fun noteDao(): NoteDao
    abstract fun achievementDao(): AchievementDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun settingsDao(): SettingsDao
    // New DAOs
    abstract fun breedingPairDao(): BreedingPairDao
    abstract fun breedingEventDao(): BreedingEventDao
    abstract fun medicationDao(): MedicationDao
    abstract fun dewormingDao(): DewormingDao
    abstract fun supplementDao(): SupplementDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun diseaseDao(): DiseaseDao
    abstract fun weightDao(): WeightDao
    abstract fun reminderDao(): ReminderDao
    abstract fun tagDao(): TagDao

    companion object {
        const val DATABASE_NAME = "high_flyer_pro.db"
        const val DATABASE_VERSION = 8

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun closeInstance() {
            synchronized(this) {
                INSTANCE?.let { db ->
                    if (db.isOpen) {
                        db.close()
                    }
                }
                INSTANCE = null
            }
        }

        private fun addColumnIfNotExists(db: SupportSQLiteDatabase, tableName: String, columnName: String, columnDef: String) {
            var exists = false
            db.query("PRAGMA table_info($tableName)").use { cursor ->
                val nameIdx = cursor.getColumnIndex("name")
                if (nameIdx != -1) {
                    while (cursor.moveToNext()) {
                        if (cursor.getString(nameIdx).equals(columnName, ignoreCase = true)) {
                            exists = true
                            break
                        }
                    }
                }
            }
            if (!exists) {
                db.execSQL("ALTER TABLE $tableName ADD COLUMN $columnName $columnDef")
            }
        }

        private fun isTableExists(db: SupportSQLiteDatabase, tableName: String): Boolean {
            db.query("SELECT name FROM sqlite_master WHERE type='table' AND name=?", arrayOf(tableName)).use { cursor ->
                return cursor.count > 0
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "photos", "filePath", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "photos", "thumbnailPath", "TEXT DEFAULT NULL")
                addColumnIfNotExists(db, "photos", "dateAdded", "INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE photos SET dateAdded = createdAt WHERE dateAdded = 0")
                db.execSQL("UPDATE photos SET filePath = photoUri WHERE filePath = ''")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_photos_isProfilePhoto ON photos(isProfilePhoto)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_photos_dateAdded ON photos(dateAdded)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS pigeon_photos (
                        photoId TEXT PRIMARY KEY NOT NULL,
                        pigeonId TEXT NOT NULL,
                        filePath TEXT NOT NULL,
                        category TEXT NOT NULL DEFAULT 'Profile',
                        caption TEXT,
                        dateAdded INTEGER NOT NULL DEFAULT 0,
                        isProfilePhoto INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(pigeonId) REFERENCES pigeons(pigeonId) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pigeon_photos_pigeonId ON pigeon_photos(pigeonId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pigeon_photos_isProfilePhoto ON pigeon_photos(isProfilePhoto)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pigeon_photos_dateAdded ON pigeon_photos(dateAdded)")

                if (isTableExists(db, "photos")) {
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO pigeon_photos (photoId, pigeonId, filePath, category, caption, dateAdded, isProfilePhoto)
                        SELECT CAST(id AS TEXT), pigeonId, CASE WHEN filePath != '' THEN filePath ELSE photoUri END, category, caption, dateAdded, isProfilePhoto
                        FROM photos
                        """.trimIndent()
                    )
                }
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS pigeon_photos_new (
                        photoId TEXT PRIMARY KEY NOT NULL,
                        pigeonId TEXT NOT NULL,
                        filePath TEXT NOT NULL,
                        category TEXT NOT NULL DEFAULT 'Profile',
                        caption TEXT,
                        dateAdded INTEGER NOT NULL DEFAULT 0,
                        isProfilePhoto INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(pigeonId) REFERENCES pigeons(pigeonId) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                if (isTableExists(db, "pigeon_photos")) {
                    db.execSQL(
                        """
                        INSERT OR IGNORE INTO pigeon_photos_new (photoId, pigeonId, filePath, category, caption, dateAdded, isProfilePhoto)
                        SELECT CAST(photoId AS TEXT), pigeonId, filePath, category, caption, dateAdded, isProfilePhoto
                        FROM pigeon_photos
                        """.trimIndent()
                    )
                    db.execSQL("DROP TABLE IF EXISTS pigeon_photos")
                }

                db.execSQL("ALTER TABLE pigeon_photos_new RENAME TO pigeon_photos")

                db.execSQL("CREATE INDEX IF NOT EXISTS index_pigeon_photos_pigeonId ON pigeon_photos(pigeonId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pigeon_photos_isProfilePhoto ON pigeon_photos(isProfilePhoto)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pigeon_photos_dateAdded ON pigeon_photos(dateAdded)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Breeding Pairs
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS breeding_pairs (
                        pairId TEXT PRIMARY KEY NOT NULL,
                        maleId TEXT NOT NULL,
                        femaleId TEXT NOT NULL,
                        pairName TEXT NOT NULL DEFAULT '',
                        startDate INTEGER NOT NULL DEFAULT 0,
                        endDate INTEGER,
                        status TEXT NOT NULL DEFAULT 'Active',
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_breeding_pairs_pairId ON breeding_pairs(pairId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_breeding_pairs_maleId ON breeding_pairs(maleId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_breeding_pairs_femaleId ON breeding_pairs(femaleId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_breeding_pairs_status ON breeding_pairs(status)")

                // 2. Breeding Events
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS breeding_events (
                        eventId TEXT PRIMARY KEY NOT NULL,
                        pairId TEXT NOT NULL,
                        maleId TEXT NOT NULL,
                        femaleId TEXT NOT NULL,
                        date INTEGER NOT NULL DEFAULT 0,
                        nestNumber TEXT NOT NULL DEFAULT '',
                        egg1Date INTEGER,
                        egg2Date INTEGER,
                        expectedHatchDate INTEGER,
                        actualHatchDate INTEGER,
                        eggStatus TEXT NOT NULL DEFAULT 'Fertile',
                        youngPigeonId1 TEXT,
                        youngPigeonId2 TEXT,
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_breeding_events_eventId ON breeding_events(eventId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_breeding_events_pairId ON breeding_events(pairId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_breeding_events_maleId ON breeding_events(maleId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_breeding_events_femaleId ON breeding_events(femaleId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_breeding_events_date ON breeding_events(date)")

                // 3. Medication Records
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS medication_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        pigeonId TEXT NOT NULL,
                        medicineName TEXT NOT NULL,
                        genericName TEXT NOT NULL DEFAULT '',
                        purpose TEXT NOT NULL DEFAULT '',
                        dosage TEXT NOT NULL DEFAULT '',
                        unit TEXT NOT NULL DEFAULT 'ml',
                        administrationMethod TEXT NOT NULL DEFAULT 'Drinking Water',
                        startDate INTEGER NOT NULL DEFAULT 0,
                        endDate INTEGER,
                        frequency TEXT NOT NULL DEFAULT 'Once Daily',
                        reason TEXT NOT NULL DEFAULT '',
                        prescribedBy TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        status TEXT NOT NULL DEFAULT 'Active',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_medication_records_pigeonId ON medication_records(pigeonId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_medication_records_status ON medication_records(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_medication_records_startDate ON medication_records(startDate)")

                // 4. Deworming Records
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS deworming_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        pigeonId TEXT NOT NULL,
                        date INTEGER NOT NULL DEFAULT 0,
                        medicineUsed TEXT NOT NULL,
                        dosage TEXT NOT NULL DEFAULT '',
                        nextDueDate INTEGER NOT NULL DEFAULT 0,
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_deworming_records_pigeonId ON deworming_records(pigeonId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_deworming_records_date ON deworming_records(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_deworming_records_nextDueDate ON deworming_records(nextDueDate)")

                // 5. Supplement Records
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS supplement_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        pigeonId TEXT NOT NULL,
                        supplementName TEXT NOT NULL,
                        category TEXT NOT NULL DEFAULT 'Multivitamin',
                        dosage TEXT NOT NULL DEFAULT '',
                        unit TEXT NOT NULL DEFAULT 'g/L',
                        frequency TEXT NOT NULL DEFAULT 'Daily',
                        startDate INTEGER NOT NULL DEFAULT 0,
                        endDate INTEGER,
                        reason TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_supplement_records_pigeonId ON supplement_records(pigeonId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_supplement_records_category ON supplement_records(category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_supplement_records_startDate ON supplement_records(startDate)")

                // 6. Food Mixtures
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS food_mixtures (
                        foodId TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        ingredients TEXT NOT NULL DEFAULT '',
                        proteinPercentage REAL,
                        energyKcal REAL,
                        purpose TEXT NOT NULL DEFAULT 'Maintenance',
                        season TEXT NOT NULL DEFAULT 'All Season',
                        suitableFor TEXT NOT NULL DEFAULT 'All Birds',
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_food_mixtures_foodId ON food_mixtures(foodId)")

                // 7. Feeding Records
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS feeding_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        foodId TEXT NOT NULL,
                        foodName TEXT NOT NULL,
                        quantityGrams REAL NOT NULL,
                        date INTEGER NOT NULL DEFAULT 0,
                        timeSlot TEXT NOT NULL DEFAULT 'Morning',
                        targetType TEXT NOT NULL DEFAULT 'Whole Loft',
                        targetId TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_feeding_records_foodId ON feeding_records(foodId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_feeding_records_date ON feeding_records(date)")

                // 8. Food Inventory
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS food_inventory (
                        inventoryId TEXT PRIMARY KEY NOT NULL,
                        foodName TEXT NOT NULL,
                        currentQuantity REAL NOT NULL,
                        unit TEXT NOT NULL DEFAULT 'kg',
                        minimumStock REAL NOT NULL DEFAULT 5.0,
                        purchaseDate INTEGER,
                        expiryDate INTEGER,
                        supplier TEXT NOT NULL DEFAULT '',
                        cost REAL,
                        notes TEXT NOT NULL DEFAULT '',
                        updatedAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_food_inventory_inventoryId ON food_inventory(inventoryId)")

                // 9. Disease Records
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS disease_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        pigeonId TEXT NOT NULL,
                        diseaseName TEXT NOT NULL,
                        symptoms TEXT NOT NULL DEFAULT '',
                        dateDetected INTEGER NOT NULL DEFAULT 0,
                        severity TEXT NOT NULL DEFAULT 'Mild',
                        affectedGroup TEXT NOT NULL DEFAULT '',
                        diagnosis TEXT NOT NULL DEFAULT '',
                        treatment TEXT NOT NULL DEFAULT '',
                        medication TEXT NOT NULL DEFAULT '',
                        recoveryDate INTEGER,
                        outcome TEXT NOT NULL DEFAULT 'Under Observation',
                        notes TEXT NOT NULL DEFAULT '',
                        status TEXT NOT NULL DEFAULT 'Active',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_disease_records_pigeonId ON disease_records(pigeonId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_disease_records_status ON disease_records(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_disease_records_dateDetected ON disease_records(dateDetected)")

                // 10. Weight Records
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS weight_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        pigeonId TEXT NOT NULL,
                        date INTEGER NOT NULL DEFAULT 0,
                        weightGrams REAL NOT NULL,
                        condition TEXT NOT NULL DEFAULT 'Prime',
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_weight_records_pigeonId ON weight_records(pigeonId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_weight_records_date ON weight_records(date)")

                // 11. Reminders
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS reminders (
                        reminderId TEXT PRIMARY KEY NOT NULL,
                        pigeonId TEXT,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL DEFAULT '',
                        category TEXT NOT NULL DEFAULT 'General',
                        dueDate INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_reminders_reminderId ON reminders(reminderId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_pigeonId ON reminders(pigeonId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_dueDate ON reminders(dueDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_isCompleted ON reminders(isCompleted)")

                // 12. Tags
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tags (
                        name TEXT PRIMARY KEY NOT NULL,
                        colorHex TEXT NOT NULL DEFAULT '#1E88E5',
                        isSystem INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_tags_name ON tags(name)")

                // 13. Pigeon Tags
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS pigeon_tags (
                        pigeonId TEXT NOT NULL,
                        tagName TEXT NOT NULL,
                        PRIMARY KEY(pigeonId, tagName)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pigeon_tags_pigeonId ON pigeon_tags(pigeonId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pigeon_tags_tagName ON pigeon_tags(tagName)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "pigeons", "ringYear", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "pigeons", "markings", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "pigeons", "breeder", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "pigeons", "source", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "pigeons", "purchasePrice", "REAL DEFAULT NULL")
                addColumnIfNotExists(db, "pigeons", "currentValue", "REAL DEFAULT NULL")
                addColumnIfNotExists(db, "pigeons", "retirementType", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "pigeons", "isFavorite", "INTEGER NOT NULL DEFAULT 0")
                addColumnIfNotExists(db, "pigeons", "isPinned", "INTEGER NOT NULL DEFAULT 0")
                addColumnIfNotExists(db, "pigeons", "inTrash", "INTEGER NOT NULL DEFAULT 0")
                addColumnIfNotExists(db, "pigeons", "trashDate", "INTEGER DEFAULT NULL")
                addColumnIfNotExists(db, "pigeons", "isAchieved", "INTEGER NOT NULL DEFAULT 0")

                db.execSQL("CREATE INDEX IF NOT EXISTS index_pigeons_inTrash ON pigeons(inTrash)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pigeons_isFavorite ON pigeons(isFavorite)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pigeons_isPinned ON pigeons(isPinned)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfNotExists(db, "flight_sessions", "eventType", "TEXT NOT NULL DEFAULT 'Training'")
                addColumnIfNotExists(db, "flight_sessions", "location", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "flight_sessions", "rules", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "flight_sessions", "description", "TEXT NOT NULL DEFAULT ''")
                addColumnIfNotExists(db, "flight_sessions", "isArchived", "INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `flight_records_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` TEXT,
                        `pigeonId` TEXT NOT NULL,
                        `pigeonName` TEXT NOT NULL,
                        `pigeonRingNumber` TEXT NOT NULL,
                        `releaseTimestamp` INTEGER NOT NULL,
                        `landingTimestamp` INTEGER,
                        `durationMillis` INTEGER,
                        `landingPosition` INTEGER,
                        `status` TEXT NOT NULL DEFAULT 'FLYING',
                        `notes` TEXT NOT NULL DEFAULT '',
                        `createdAt` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )

                if (isTableExists(db, "flight_records")) {
                    db.execSQL(
                        """
                        INSERT INTO flight_records_new (id, sessionId, pigeonId, pigeonName, pigeonRingNumber, releaseTimestamp, landingTimestamp, durationMillis, landingPosition, status, notes, createdAt)
                        SELECT id, sessionId, pigeonId, pigeonName, pigeonRingNumber, releaseTimestamp, landingTimestamp, durationMillis, landingPosition, status, notes, createdAt
                        FROM flight_records
                        """.trimIndent()
                    )
                    db.execSQL("DROP TABLE IF EXISTS flight_records")
                }
                db.execSQL("ALTER TABLE flight_records_new RENAME TO flight_records")

                db.execSQL("CREATE INDEX IF NOT EXISTS index_flight_records_sessionId ON flight_records(sessionId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_flight_records_pigeonId ON flight_records(pigeonId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_flight_records_status ON flight_records(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_flight_records_landingTimestamp ON flight_records(landingTimestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_flight_records_landingPosition ON flight_records(landingPosition)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        prepopulateDefaults(database)
                    }
                }
            }
        }

        suspend fun prepopulateDefaults(database: AppDatabase) {
            // Clean database initialization without pre-seeded default records
        }
    }
}
