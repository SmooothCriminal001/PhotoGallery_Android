package com.bignerdranch.android.photogallery.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class PhotoRepository {

    private val flickrApi: FlickrApi
    
    init {

		val okHttpClient = OkHttpClient.Builder()
		            .addInterceptor(PhotoInterceptor())
		            .build()

    	val retrofit: Retrofit = Retrofit.Builder()
			.baseUrl("https://api.flickr.com")
			.addConverterFactory(MoshiConverterFactory.create())		//Uses Moshi converter on the response object to parse response
			.client(okHttpClient)
			.build()
    	flickrApi = retrofit.create()
    }
    
    suspend fun fetchPhotos() = flickrApi.fetchPhotos().photos.galleryItems

	suspend fun searchPhotos(query: String) = flickrApi.searchPhotos(query).photos.galleryItems
}