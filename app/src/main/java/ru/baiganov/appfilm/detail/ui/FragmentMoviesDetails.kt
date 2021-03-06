package ru.baiganov.appfilm.detail.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.serialization.ExperimentalSerializationApi
import ru.baiganov.appfilm.R
import ru.baiganov.appfilm.adapter.ActorsAdapter
import ru.baiganov.appfilm.api.ApiFactory
import ru.baiganov.appfilm.database.AppDatabase
import ru.baiganov.appfilm.databinding.FragmentMoviesDetailsBinding
import ru.baiganov.appfilm.pojo.Movie

class FragmentMoviesDetails : Fragment() {

    private lateinit var adapter: ActorsAdapter
    private lateinit var recyclerActors: RecyclerView
    private lateinit var viewModelFactory: MoviesDetailsFactory
    private lateinit var binding: FragmentMoviesDetailsBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_movies_details, container, false)
    }

    @ExperimentalSerializationApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMoviesDetailsBinding.bind(view)
        setUpListeners()
        initRecycler()
        setupViewModel()
    }

    private fun setUpListeners() {
        binding.tvBack.setOnClickListener {
            fragmentManager?.popBackStack()
        }
    }

    @ExperimentalSerializationApi
    private fun setupViewModel() {
        val database = AppDatabase.create(requireContext())
        viewModelFactory = arguments?.getParcelable<Movie>("movie")?.let { movie ->
            MoviesDetailsFactory(
                    movie = movie,
                    apiService = ApiFactory.apiService,
                    actorsDao = database.actorsDao()
            )
        }!!

        val viewModelMovie = ViewModelProvider(
                this,
                viewModelFactory
        ).get(MoviesDetailsViewModel::class.java)

        viewModelMovie.movies.observe(viewLifecycleOwner) {
            bindMovie(it)
        }

        viewModelMovie.actors.observe(viewLifecycleOwner) {
            adapter.bindActors(it)
        }
    }

    private fun initRecycler() {
        recyclerActors = binding.rvActors
        adapter = ActorsAdapter()
        recyclerActors.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerActors.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun bindMovie(movie: Movie) {
        binding.tvName.text = movie.title
        binding.tvReviews.text = movie.voteCount.toString() + " " + requireContext().getString(R.string.reviews)
        binding.tvStoryline.text = movie.overview
        binding.rbStars.rating = movie.ratings / 2
        var temp:String = ""
        movie.genres.forEach {
            temp = if (temp == "") {
                it.name
            } else {
                "$temp,${it.name}"
            }
        }
        binding.tvTag.text = temp
        Glide.with(requireContext())
                .load(movie.backdrop)
                .into(binding.ivPoster)
    }

    companion object {
        fun getNewInstance(movie: Movie): FragmentMoviesDetails {
            val bundle = Bundle()
            bundle.putParcelable("movie", movie)
            val fragment = FragmentMoviesDetails()
            fragment.arguments = bundle
            return fragment
        }
    }
}