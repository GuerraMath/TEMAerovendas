// Caminho: app/src/main/java/com/temaerovendas/di/AppModule.kt
package com.temaerovendas.di

import android.content.Context
import androidx.room.Room
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.temaerovendas.R
import com.temaerovendas.data.local.AerovendasDatabase
import com.temaerovendas.data.local.dao.FavoriteDao
import com.temaerovendas.data.repository.AircraftRepositoryImpl
import com.temaerovendas.data.repository.AuthRepositoryImpl
import com.temaerovendas.data.repository.HangarRepositoryImpl
import com.temaerovendas.domain.repository.AircraftRepository
import com.temaerovendas.domain.repository.AuthRepository
import com.temaerovendas.domain.repository.HangarRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Firebase
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideGoogleSignInClient(@ApplicationContext context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    // Room Database
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AerovendasDatabase =
        Room.databaseBuilder(
            context,
            AerovendasDatabase::class.java,
            "aerovendas_db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideFavoriteDao(database: AerovendasDatabase): FavoriteDao =
        database.favoriteDao()

    // Repositórios
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        googleSignInClient: GoogleSignInClient
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth, firestore, googleSignInClient)

    @Provides
    @Singleton
    fun provideAircraftRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
        favoriteDao: FavoriteDao,
        firebaseAuth: FirebaseAuth
    ): AircraftRepository = AircraftRepositoryImpl(firestore, storage, favoriteDao, firebaseAuth)

    @Provides
    @Singleton
    fun provideHangarRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): HangarRepository = HangarRepositoryImpl(firestore, storage)
}
