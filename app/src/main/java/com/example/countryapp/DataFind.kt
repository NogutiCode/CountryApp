package com.example.countryapp
//Capital
// Currency
//Neighbours
//population

data class Country(
    val name: Name?, //done
    val capital: Any?, //done
    val flags: Flags?,
    val currencies: Map<String, Currency>?,
    val population: Int?,
    //val borders: Map<Int, Any>?,
    //val gini: Any?,
)
data class Currency(
    val name: String?,
    )

data class Flags(
    val png: String?,
    val svg: String?
)

data class Name(
    val common: String?,
    val official: String?,
    val nativeName: Map<String, Any>?
)
