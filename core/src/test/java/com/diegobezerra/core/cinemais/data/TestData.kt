package com.diegobezerra.core.cinemais.data

import com.diegobezerra.core.cinemais.domain.model.Movie
import com.diegobezerra.core.cinemais.domain.model.Posters
import java.text.SimpleDateFormat

val movie = Movie(
    id = 7128,
    title = "Capitã Marvel",
    originalTitle = "Captain Marvel",
    posters = Posters(
        small = "http://www.claquete.com/fotos/filmes/poster/7128_pequeno.jpg",
        medium = "http://www.claquete.com/fotos/filmes/poster/7128_medio.jpg",
        large = "http://www.claquete.com/fotos/filmes/poster/7128_grande.jpg"
    ),
    htmlUrl = "http://www.cinemais.com.br/filmes/filme.php?cf=7128",
    synopsis = "Ambientado nos anos 1990, Capitã Marvel da Marvel Studios é uma aventura " +
        "totalmente nova de um período nunca visto na história do Universo Cinematográfico " +
        "da Marvel que acompanha a jornada de Carol Danvers, conforme ela se torna uma das " +
        "personagens mais poderosas do universo. Enquanto uma guerra galáctica entre duas raças" +
        " alienígenas chega à Terra, Danvers se vê junto a um pequeno grupo de aliados bem no " +
        "meio da ação.",
    cast = listOf(
        "Brie Larson", "Gemma Chan", "Mckenna Grace", "Lee Pace", "Ben Mendelsohn",
        "Samuel L. Jackson", "Jude Law", "Djimon Hounsou"
    ),
    screenplay = listOf("Anna Boden", "Ryan Fleck", "Geneva Robertson-Dworet"),
    executiveProduction = listOf("Victoria Alonso", "Louis D´Esposito", "Stan Lee",
        "Jonathan Schwartz", "Patricia Whitcher"),
    production = listOf("Kevin Feige"),
    direction = listOf("Anna Boden", "Ryan Fleck"),
    rating = 12,
    countries = listOf("EUA"),
    genres = listOf("Ação", "Aventura", "Ficção-científica"),
    runtime = 124,
    releaseDate = SimpleDateFormat("dd/MM/yyyy").parse("07/03/2019"),
    distributor = "Walt Disney Studios"
)
