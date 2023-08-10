package app


data class Country(
    val name: Name?,
    val capital: Any?,
    val flags: Flags?,
    val currencies: Map<String, Currency>?,
    val population: Int?,
    val borders: Any?,
    val cca3: Any?

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


