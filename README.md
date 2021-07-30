# Markdown Generator in Kotlin

[![Java CI with Gradle](https://github.com/kokorins/markdown-generator/actions/workflows/gradle.yml/badge.svg)](https://github.com/kokorins/markdown-generator/actions/workflows/gradle.yml)
[![](https://jitpack.io/v/kokorins/markdown-generator.svg)](https://jitpack.io/#kokorins/markdown-generator)

Simple library to generate markdown from kotlin.

It allows transform code like:

```kotlin
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
    code("language") {
        """
                    fun someCode(): String {
                        Just an example of code
                    }
                """.trimIndent()
    }
}

doc.asString()
```

into

```markdown
# My Markdown Document

## Introduction

This is a small test of how it looks like.

- Just an item [link 1](url1)

1. Just an enumerated item ![link 2]

> Blockquote line [link 3][link 2]
> Another Blockquote line

```language
fun someCode(): String {
    Just an example of code
}
```

[link 2]: url2
            
```

## Installation

```kotlin
dependencies {
    implementation("com.github.kokorins:markdown-generator:0.1.0")
}
```

