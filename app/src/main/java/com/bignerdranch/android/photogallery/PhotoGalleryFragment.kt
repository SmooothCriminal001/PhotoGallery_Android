package com.bignerdranch.android.photogallery

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.bignerdranch.android.photogallery.api.FlickrApi
import com.bignerdranch.android.photogallery.api.GalleryItem
import com.bignerdranch.android.photogallery.databinding.FragmentPhotoGalleryBinding
import com.bignerdranch.android.photogallery.databinding.ListItemGalleryBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import java.util.*

private const val TAG = "PhotoGalleryFragment"

class PhotoGalleryFragment: Fragment(){
    
    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels()
    private var searchView: SearchView? = null

    private var _binding: FragmentPhotoGalleryBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPhotoGalleryBinding.inflate(inflater, container, false)
        binding.photoGrid.layoutManager = GridLayoutManager(context, 3)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Things to be done in a coroutine tied to this fragment's lifecycle
        viewLifecycleOwner.lifecycleScope.launch {
        	//Things that have to be done every time a fragment hits a specific lifecycle
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                photoGalleryViewModel.uiState.collect{
                    Log.d(TAG, "Response received: $it")
                    binding.photoGrid.adapter = PhotoListAdapter().apply { submitList(it.images) }
                    searchView?.setQuery(it.query, false)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    	super.onCreateOptionsMenu(menu, inflater)
    	inflater.inflate(R.menu.fragment_photo_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        searchView = searchItem.actionView as? SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        	override fun onQueryTextSubmit(query: String?): Boolean {
        		photoGalleryViewModel.setQuery(query ?: "")
        		return true
        	}

        	override fun onQueryTextChange(newText: String?): Boolean {
        		return false
        	}
        })
    }

    override fun onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu()
        searchView = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
    	return when (item.itemId) {
    		R.id.menu_item_clear -> {
    			photoGalleryViewModel.setQuery("")
    			true
    		}
    		else -> super.onOptionsItemSelected(item)
    	}
    }
    
    private inner class PhotoViewHolder(private val binding: ListItemGalleryBinding) : RecyclerView.ViewHolder(binding.root){
    	
    	//fun bind(galleryItem : GalleryItem, OnClickLambda: (UUID) -> Unit){
        fun bind(galleryItem : GalleryItem){
    		//Assign binding instances with data here

            binding.itemImageView.load(galleryItem.url){
                //optionalLambda
                placeholder(R.drawable.flower_design)
                transformations(CircleCropTransformation())
            }
            /*
    		binding.root.setOnClickListener{
    			OnClickLambda()
    		}

    		 */
    	}
    }
    
    object DiffUtilInstance : DiffUtil.ItemCallback<GalleryItem>() {
    	override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
    		//return oldItem.id == newItem.id		//Or any way to check the items are same
    		return false
    	}
    
    	override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
    		//return (oldItem.toString() == newItem.toString())
    		return false
    	}
    }
    
    //private inner class PhotoListAdapter(private val OnClickLambda: (UUID) -> Unit): ListAdapter<GalleryItem, PhotoViewHolder>(DiffUtilInstance){
    private inner class PhotoListAdapter(): ListAdapter<GalleryItem, PhotoViewHolder>(DiffUtilInstance){
    	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
    		val inflater = LayoutInflater.from(parent.context)
    		val binding = ListItemGalleryBinding.inflate(inflater, parent, false)
    		return PhotoViewHolder(binding)
    	}
    
    	override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
            //holder.bind(getItem(position), OnClickLambda)
            val galleryItem = getItem(position);
            Log.d(TAG, "Id ${galleryItem.id} - ${galleryItem.title} - ${galleryItem.url}")
            holder.bind(galleryItem)
    	}
    
    	override fun getItemViewType(position: Int): Int {
    		
    		return 0
    	}
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}