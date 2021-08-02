package me.md

object Readme {
    fun generate():String {
        val doc = Md.generate {
            title { +"My Markdown Document" }
            h(2) { +"Introduction" }
            p { +"This is a small test of how it looks like." }

            itemize {
                item {
                    +"Just an item " + link("link 1", "url1")
                }
            }

            enumerate {
                item {
                    +"Just an enumerated item " + image("link 2", "url2", "link 2")
                }
            }

            blockquote {
                item {
                    +"Blockquote line " + link("link 3", "url2", "link 2")
                }
                item {
                    +"Another Blockquote line"
                }
            }
            code("kotlin") {
                """
            fun someCode(): String {
                Just an example of code
            }
        """.trimIndent()
            }
        }

        return doc.asString()
    }
}

fun main() {
    println(Readme.generate())
}