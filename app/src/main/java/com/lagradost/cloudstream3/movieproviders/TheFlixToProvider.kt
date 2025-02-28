package com.lagradost.cloudstream3.movieproviders

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.getQualityFromName


class TheFlixToProvider : MainAPI() {
    override var name = "TheFlix.to"
    override var mainUrl = "https://theflix.to"
    override val instantLinkLoading = false
    override val hasMainPage = true
    override val supportedTypes = setOf(
        TvType.Movie,
        TvType.TvSeries,
    )

    data class HomeJson (
        @JsonProperty("props") val props         : HomeProps         = HomeProps(),
    )

    data class HomeProps (
        @JsonProperty("pageProps" ) val pageProps : PageProps = PageProps(),
    )

    data class PageProps (
        @JsonProperty("moviesListTrending"     ) val moviesListTrending     : MoviesListTrending    = MoviesListTrending(),
        @JsonProperty("moviesListNewArrivals"  ) val moviesListNewArrivals  : MoviesListNewArrivals = MoviesListNewArrivals(),
        @JsonProperty("tvsListTrending"        ) val tvsListTrending        : TvsListTrending      = TvsListTrending(),
        @JsonProperty("tvsListNewEpisodes"     ) val tvsListNewEpisodes     : TvsListNewEpisodes    = TvsListNewEpisodes(),
    )


    data class MoviesListTrending (
        @JsonProperty("docs"  ) val docs  : ArrayList<Docs> = arrayListOf(),
        @JsonProperty("total" ) val total : Int?            = null,
        @JsonProperty("page"  ) val page  : Int?            = null,
        @JsonProperty("limit" ) val limit : Int?            = null,
        @JsonProperty("pages" ) val pages : Int?            = null,
        @JsonProperty("type"  ) val type  : String?         = null,
    )

    data class MoviesListNewArrivals (
        @JsonProperty("docs"  ) val docs  : ArrayList<Docs> = arrayListOf(),
        @JsonProperty("total" ) val total : Int?            = null,
        @JsonProperty("page"  ) val page  : Int?            = null,
        @JsonProperty("limit" ) val limit : Int?            = null,
        @JsonProperty("pages" ) val pages : Int?            = null,
        @JsonProperty("type"  ) val type  : String?         = null,
    )

    data class TvsListTrending (
        @JsonProperty("docs"  ) val docs  : ArrayList<Docs> = arrayListOf(),
        @JsonProperty("total" ) val total : Int?            = null,
        @JsonProperty("page"  ) val page  : Int?            = null,
        @JsonProperty("limit" ) val limit : Int?            = null,
        @JsonProperty("pages" ) val pages : Int?            = null,
        @JsonProperty("type"  ) val type  : String?         = null,
    )

    data class TvsListNewEpisodes (
        @JsonProperty("docs"  ) val docs  : ArrayList<Docs> = arrayListOf(),
        @JsonProperty("total" ) val total : Int?            = null,
        @JsonProperty("page"  ) val page  : Int?            = null,
        @JsonProperty("limit" ) val limit : Int?            = null,
        @JsonProperty("pages" ) val pages : Int?            = null,
        @JsonProperty("type"  ) val type  : String?         = null,
    )

    data class Docs (
        @JsonProperty("name"             ) val name             : String           = String(),
        @JsonProperty("originalLanguage" ) val originalLanguage : String?           = null,
        @JsonProperty("popularity"       ) val popularity       : Double?           = null,
        @JsonProperty("runtime"          ) val runtime          : Int?              = null,
        @JsonProperty("status"           ) val status           : String?           = null,
        @JsonProperty("voteAverage"      ) val voteAverage      : Double?           = null,
        @JsonProperty("voteCount"        ) val voteCount        : Int?              = null,
        @JsonProperty("cast"             ) val cast             : String?           = null,
        @JsonProperty("director"         ) val director         : String?           = null,
        @JsonProperty("overview"         ) val overview         : String?           = null,
        @JsonProperty("posterUrl"        ) val posterUrl        : String?           = null,
        @JsonProperty("releaseDate"      ) val releaseDate      : String?           = null,
        @JsonProperty("createdAt"        ) val createdAt        : String?           = null,
        @JsonProperty("updatedAt"        ) val updatedAt        : String?           = null,
        @JsonProperty("conversionDate"   ) val conversionDate   : String?           = null,
        @JsonProperty("id"               ) val id               : Int?              = null,
        @JsonProperty("available"        ) val available        : Boolean?          = null,
    )


    override suspend fun getMainPage(): HomePageResponse {
        val items = ArrayList<HomePageList>()
        val doc = app.get(mainUrl).document
        val scriptText = doc.selectFirst("script[type=application/json]").data()
        if (scriptText.contains("moviesListTrending")) {
            val json = parseJson<HomeJson>(scriptText)
            val homePageProps = json.props.pageProps
            listOf(
                Triple(homePageProps.moviesListNewArrivals.docs, homePageProps.moviesListNewArrivals.type, "New Movie arrivals"),
                Triple(homePageProps.moviesListTrending.docs, homePageProps.moviesListTrending.type, "Trending Movies"),
                Triple(homePageProps.tvsListTrending.docs, homePageProps.tvsListTrending.type, "Trending TV Series"),
                Triple(homePageProps.tvsListNewEpisodes.docs, homePageProps.tvsListNewEpisodes.type, "New Episodes")
            ).map { (docs, type, homename) ->
                val home = docs.map { info ->
                    val title = info.name
                    val poster = info.posterUrl
                    val typeinfo = if (type?.contains("TV") == true) TvType.TvSeries else TvType.Movie
                    val link = if (typeinfo == TvType.Movie) "$mainUrl/movie/${info.id}-${cleanTitle(title)}"
                    else "$mainUrl/tv-show/${info.id}-${cleanTitle(title)}/season-1/episode-1"
                    TvSeriesSearchResponse(
                        title,
                        link,
                        this.name,
                        typeinfo,
                        poster,
                        null,
                        null,
                    )
                }
                items.add(HomePageList(homename, home))
            }

        }

        if (items.size <= 0) throw ErrorLoadingException()
        return HomePageResponse(items)
    }

    data class SearchJson (
        @JsonProperty("props"         ) val props         : SearchProps         = SearchProps(),
    )

    data class SearchProps (
        @JsonProperty("pageProps" ) val pageProps : SearchPageProps = SearchPageProps(),
    )

    data class SearchPageProps (
        @JsonProperty("mainList"            ) val mainList            : SearchMainList             = SearchMainList(),
    )

    data class SearchMainList (
        @JsonProperty("docs"  ) val docs  : ArrayList<Docs> = arrayListOf(),
        @JsonProperty("total" ) val total : Int?            = null,
        @JsonProperty("page"  ) val page  : Int?            = null,
        @JsonProperty("limit" ) val limit : Int?            = null,
        @JsonProperty("pages" ) val pages : Int?            = null,
        @JsonProperty("type"  ) val type  : String?         = null,
    )


    override suspend fun search(query: String): List<SearchResponse> {
        val search = ArrayList<SearchResponse>()
        val urls = listOf(
            "$mainUrl/movies/trending?search=$query",
            "$mainUrl/tv-shows/trending?search=$query"
        )
        urls.apmap { url ->
            val doc = app.get(url).document
            val scriptText = doc.selectFirst("script[type=application/json]").data()
            if (scriptText.contains("pageProps")) {
                val json = parseJson<SearchJson>(scriptText)
                val searchPageProps = json.props.pageProps.mainList
                val pair = listOf(Pair(searchPageProps.docs, searchPageProps.type))
                pair.map { (docs, type) ->
                    docs.map { info ->
                        val title = info.name
                        val poster = info.posterUrl
                        val typeinfo = if (type?.contains("TV") == true) TvType.TvSeries else TvType.Movie
                        val link = if (typeinfo == TvType.Movie) "$mainUrl/movie/${info.id}-${cleanTitle(title)}"
                        else "$mainUrl/tv-show/${info.id}-${cleanTitle(title)}/season-1/episode-1"
                        if (typeinfo == TvType.Movie) {
                            search.add(
                                MovieSearchResponse(
                                    title,
                                    link,
                                    this.name,
                                    TvType.Movie,
                                    poster,
                                    null
                                )
                            )
                        } else {
                            search.add(
                                TvSeriesSearchResponse(
                                    title,
                                    link,
                                    this.name,
                                    TvType.TvSeries,
                                    poster,
                                    null,
                                    null
                                )
                            )
                        }
                    }
                }
            }
        }
        return search
    }


    data class LoadMain (
        @JsonProperty("props") val props : LoadProps = LoadProps(),
    )
    data class LoadProps (
        @JsonProperty("pageProps" ) val pageProps : LoadPageProps = LoadPageProps(),
    )

    data class LoadPageProps (
        @JsonProperty("selectedTv"          ) val selectedTv          : TheFlixMetadata?          = TheFlixMetadata(),
        @JsonProperty("movie") val movie               : TheFlixMetadata?               = TheFlixMetadata(),
        @JsonProperty("videoUrl") val videoUrl            : String?              = null,
        @JsonProperty("recommendationsList") val recommendationsList : RecommendationsList? = RecommendationsList(),
    )

    data class Genres (
        @JsonProperty("name" ) val name : String,
        @JsonProperty("id"   ) val id   : Int?    = null

    )

    data class Seasons (
        @JsonProperty("name"             ) val name             : String?             = null,
        @JsonProperty("numberOfEpisodes" ) val numberOfEpisodes : Int?                = null,
        @JsonProperty("seasonNumber"     ) val seasonNumber     : Int?                = null,
        @JsonProperty("overview"         ) val overview         : String?             = null,
        @JsonProperty("posterUrl"        ) val posterUrl        : String?             = null,
        @JsonProperty("releaseDate"      ) val releaseDate      : String?             = null,
        @JsonProperty("createdAt"        ) val createdAt        : String?             = null,
        @JsonProperty("updatedAt"        ) val updatedAt        : String?             = null,
        @JsonProperty("id"               ) val id               : Int?                = null,
        @JsonProperty("episodes"         ) val episodes         : ArrayList<Episodes> = arrayListOf()
    )

    data class Episodes (
        @JsonProperty("episodeNumber" ) val episodeNumber : Int?              = null,
        @JsonProperty("name"          ) val name          : String?           = null,
        @JsonProperty("seasonNumber"  ) val seasonNumber  : Int?              = null,
        @JsonProperty("voteAverage"   ) val voteAverage   : Double?           = null,
        @JsonProperty("voteCount"     ) val voteCount     : Int?              = null,
        @JsonProperty("overview"      ) val overview      : String?           = null,
        @JsonProperty("releaseDate"   ) val releaseDate   : String?           = null,
        @JsonProperty("createdAt"     ) val createdAt     : String?           = null,
        @JsonProperty("updatedAt"     ) val updatedAt     : String?           = null,
        @JsonProperty("id"            ) val id            : Int?              = null,
        @JsonProperty("videos"        ) val videos        : ArrayList<Videos> = arrayListOf()
    )

    data class Videos (
        @JsonProperty("language" ) val language : String? = null,
        @JsonProperty("name"     ) val name     : String? = null,
        @JsonProperty("id"       ) val id       : Int?    = null
    )

    data class RecommendationsList (
        @JsonProperty("docs"  ) val docs  : ArrayList<LoadDocs> = arrayListOf(),
        @JsonProperty("total" ) val total : Int?            = null,
        @JsonProperty("page"  ) val page  : Int?            = null,
        @JsonProperty("limit" ) val limit : Int?            = null,
        @JsonProperty("pages" ) val pages : Int?            = null,
        @JsonProperty("type"  ) val type  : String?         = null,
    )

    data class LoadDocs (
        @JsonProperty("name"             ) val name             : String           = String(),
        @JsonProperty("originalLanguage" ) val originalLanguage : String?           = null,
        @JsonProperty("popularity"       ) val popularity       : Double?           = null,
        @JsonProperty("runtime"          ) val runtime          : Int?              = null,
        @JsonProperty("status"           ) val status           : String?           = null,
        @JsonProperty("voteAverage"      ) val voteAverage      : Double?           = null,
        @JsonProperty("voteCount"        ) val voteCount        : Int?              = null,
        @JsonProperty("cast"             ) val cast             : String?           = null,
        @JsonProperty("director"         ) val director         : String?           = null,
        @JsonProperty("overview"         ) val overview         : String?           = null,
        @JsonProperty("posterUrl"        ) val posterUrl        : String?           = null,
        @JsonProperty("releaseDate"      ) val releaseDate      : String?           = null,
        @JsonProperty("createdAt"        ) val createdAt        : String?           = null,
        @JsonProperty("updatedAt"        ) val updatedAt        : String?           = null,
        @JsonProperty("id"               ) val id               : Int?              = null,
        @JsonProperty("available"        ) val available        : Boolean?          = null,
    )


    data class TheFlixMetadata (
        @JsonProperty("episodeRuntime"   ) val episodeRuntime   : Int?               = null,
        @JsonProperty("name"             ) val name             : String           = String(),
        @JsonProperty("originalLanguage" ) val originalLanguage : String?           = null,
        @JsonProperty("popularity"       ) val popularity       : Double?           = null,
        @JsonProperty("runtime"          ) val runtime          : Int?              = null,
        @JsonProperty("numberOfSeasons"  ) val numberOfSeasons  : Int?               = null,
        @JsonProperty("numberOfEpisodes" ) val numberOfEpisodes : Int?               = null,
        @JsonProperty("status"           ) val status           : String?           = null,
        @JsonProperty("voteAverage"      ) val voteAverage      : Double?           = null,
        @JsonProperty("voteCount"        ) val voteCount        : Int?              = null,
        @JsonProperty("cast"             ) val cast             : String?           = null,
        @JsonProperty("director"         ) val director         : String?           = null,
        @JsonProperty("overview"         ) val overview         : String?           = null,
        @JsonProperty("posterUrl"        ) val posterUrl        : String?           = null,
        @JsonProperty("releaseDate"      ) val releaseDate      : String?           = null,
        @JsonProperty("conversionDate"   ) val conversionDate   : String?           = null,
        @JsonProperty("createdAt"        ) val createdAt        : String?           = null,
        @JsonProperty("updatedAt"        ) val updatedAt        : String?           = null,
        @JsonProperty("id"               ) val id               : Int?              = null,
        @JsonProperty("available"        ) val available        : Boolean?          = null,
        @JsonProperty("genres"           ) val genres           : ArrayList<Genres> = arrayListOf(),
        @JsonProperty("videos"           ) val videos           : ArrayList<Videos> = arrayListOf(),
        @JsonProperty("seasons"          ) val seasons          : ArrayList<Seasons> = arrayListOf()
    )

    private fun cleanTitle(title: String): String {
        val dotTitle = title.substringBefore("/season")
        if (dotTitle.contains(Regex("\\..\\."))) { //For titles containing more than two dots (S.W.A.T.)
            return (dotTitle.removeSuffix(".")
                .replace(" - ", "-")
                .replace(".", "-").replace(" ", "-")
                .replace("-&", "")
                .replace(Regex("(:|-&)"), "")
                .replace("'", "-")).lowercase()
        }
        return (title
            .replace(" - ","-")
            .replace(" ","-")
            .replace("-&","")
            .replace("/","-")
            .replace(Regex("(:|-&|\\.)"),"")
            .replace("'","-")).lowercase()
    }

    override suspend fun load(url: String): LoadResponse? {
        val soup = app.get(url).document
        val scripttext = soup.selectFirst("script[type=application/json]").data()
        val tvtype = if (url.contains("movie")) TvType.Movie else TvType.TvSeries
        val json = parseJson<LoadMain>(scripttext)
        val episodes = ArrayList<TvSeriesEpisode>()
        val isMovie = tvtype == TvType.Movie
        val pageMain = json.props.pageProps

        val metadata: TheFlixMetadata? = if (isMovie) pageMain.movie else pageMain.selectedTv

        val available = metadata?.available

        val comingsoon = !available!!

        val movieId = metadata.id

        val movietitle = metadata.name

        val poster = metadata.posterUrl

        val description = metadata.overview

        if (!isMovie) {
            metadata.seasons.map { seasons ->
                val seasonPoster = seasons.posterUrl ?: metadata.posterUrl
                seasons.episodes.forEach { epi ->
                    val episodenu = epi.episodeNumber
                    val seasonum = epi.seasonNumber
                    val title = epi.name
                    val epDesc = epi.overview
                    val test = epi.videos
                    val ratinginfo = (epi.voteAverage)?.times(10)?.toInt()
                    val rating = if (ratinginfo?.equals(0) == true) null else ratinginfo
                    val eps = TvSeriesEpisode(
                        title,
                        seasonum,
                        episodenu,
                        "$mainUrl/tv-show/$movieId-${cleanTitle(movietitle)}/season-$seasonum/episode-$episodenu",
                        description = epDesc!!,
                        posterUrl = seasonPoster,
                        rating = rating,
                    )
                    if (test.isNotEmpty()) {
                        episodes.add(eps)
                    } else {
                        //Nothing, will prevent seasons/episodes with no videos to be added
                    }
                }
            }
        }
        val rating = metadata.voteAverage?.toFloat()?.times(1000)?.toInt()

        val tags = metadata.genres.map { it.name }

        val recommendationsitem =  pageMain.recommendationsList?.docs?.map { loadDocs ->
            val title = loadDocs.name
            val posterrec = loadDocs.posterUrl
            val link = if (isMovie) "$mainUrl/movie/${loadDocs.id}-${cleanTitle(title)}"
            else "$mainUrl/tv-show/${loadDocs.id}-${cleanTitle(title)}/season-1/episode-1"
            MovieSearchResponse(
                title,
                link,
                this.name,
                tvtype,
                posterrec,
                year = null
            )
        }

        val year = metadata.releaseDate?.substringBefore("-")

        val runtime = metadata.runtime?.div(60) ?: metadata.episodeRuntime?.div(60)
        val cast = metadata.cast?.split(",")

        return when (tvtype) {
            TvType.TvSeries -> {
                return newTvSeriesLoadResponse(movietitle, url, TvType.TvSeries, episodes) {
                    this.posterUrl = poster
                    this.year = year?.toIntOrNull()
                    this.plot = description
                    this.duration = runtime
                    addActors(cast)
                    this.tags = tags
                    this.recommendations = recommendationsitem
                    this.comingSoon = comingsoon
                    this.rating = rating
                }
            }
            TvType.Movie -> {
                newMovieLoadResponse(movietitle, url, TvType.Movie, url) {
                    this.year = year?.toIntOrNull()
                    this.posterUrl = poster
                    this.plot = description
                    this.duration = runtime
                    addActors(cast)
                    this.tags = tags
                    this.recommendations = recommendationsitem
                    this.comingSoon = comingsoon
                    this.rating = rating
                }
            }
            else -> null
        }
    }


    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document
        val script = doc.selectFirst("script[type=application/json]").data()
        val json = parseJson<LoadMain>(script)
        val extractedLink = json.props.pageProps.videoUrl
        val qualityReg = Regex("(\\d+p)")
        if (extractedLink != null) {
            val quality = qualityReg.find(extractedLink)?.value ?: ""
            callback(
                ExtractorLink(
                    name,
                    "$name $quality",
                    extractedLink,
                    "",
                    getQualityFromName(quality),
                    false
                ))
        }
        return true
    }
}
