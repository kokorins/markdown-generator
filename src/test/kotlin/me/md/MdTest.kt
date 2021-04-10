package me.md

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MdTest : FunSpec({
    test("sentence check") {
        val sentence = Md.Sentence().text("t").b("b").i("i").ib("ib").link("l", "l").image("i", "i")
        val visitor = TextVisitor()
        sentence.accept(visitor)
        visitor.render() shouldBe "t**b**_i_***ib***[l](l)![i](i)"
    }

    test("sentence infix check") {
        val sentence = Md.Sentence().apply {
            +"t" + "t" + b("b") + i("i") + ib("ib") + link("l", "l") + image("i", "i")
        }
        val visitor = TextVisitor()
        sentence.accept(visitor)
        visitor.render() shouldBe "tt**b**_i_***ib***[l](l)![i](i)"
    }

    test("sum of sentences") {
        val sentence = Md.Sentence().apply {
            text("Hello").plus(Md.Sentence().text(" World"))
        }
        val visitor = TextVisitor()
        sentence.accept(visitor)
        visitor.render() shouldBe "Hello World"
    }

    test("paragraph") {
        val paragraph = Md.Paragraph().apply {
            (+"t").br()
            text("t").br()
            b("b"); br()
            i("i"); br()
            ib("ib"); br()
            image("i", "i"); br()
            link("l", "l")
        }
        val visitor = TextVisitor()
        paragraph.accept(visitor)
        visitor.render() shouldBe """
            t
            t
            **b**
            _i_
            ***ib***
            ![i](i)
            [l](l)
            
        """.trimIndent()
    }

    test("link") {
        val link = Md.Link("text", "url", false)
        val visitor = TextVisitor()
        link.accept(visitor)
        visitor.render() shouldBe """
            [text](url)
        """.trimIndent()
    }

    test("link-full-reference") {
        val link = Md.Link("text", "url", false, "label")
        val visitor = TextVisitor()
        link.accept(visitor)
        visitor.render() shouldBe """
            [text][label]
            [label]: url
            
        """.trimIndent()
    }

    test("link-short-reference") {
        val link = Md.Link("label", "url", false, "label")
        val visitor = TextVisitor()
        link.accept(visitor)
        visitor.render() shouldBe """
            [label]
            [label]: url
            
        """.trimIndent()
    }

    test("image") {
        val link = Md.Link("text", "url", true)
        val visitor = TextVisitor()
        link.accept(visitor)
        visitor.render() shouldBe """
            ![text](url)
        """.trimIndent()
    }
    
    test("image-short-link") {
        val link = Md.Link("text", "url", true, "text")
        val visitor = TextVisitor()
        link.accept(visitor)
        visitor.render() shouldBe """
            ![text]
            [text]: url
            
        """.trimIndent()
    }
    
    test("image-full-link") {
        val link = Md.Link("text", "url", true, "1")
        val visitor = TextVisitor()
        link.accept(visitor)
        visitor.render() shouldBe """
            ![text][1]
            [1]: url
            
        """.trimIndent()
    }

    test("itemize") {
        val itemize = Md.Itemize()
        itemize.item { +"item 1" }
        itemize.item { +"item 2" }
        val visitor = TextVisitor()
        itemize.accept(visitor)
        visitor.render() shouldBe """
            - item 1
            - item 2
            
        """.trimIndent()
    }

    test("enumerate") {
        val enumerate = Md.Enumerate()
        enumerate.item { +"item 1" }
        enumerate.item { +"item 2" }
        val visitor = TextVisitor()
        enumerate.accept(visitor)
        visitor.render() shouldBe """
            1. item 1
            2. item 2
            
        """.trimIndent()
    }

    test("blockquotes") {
        val blockquotes = Md.Blockquotes()
        blockquotes.item { +"line 1" }
        blockquotes.item { +"line 2" }
        val visitor = TextVisitor()
        blockquotes.accept(visitor)
        visitor.render() shouldBe """
            > line 1
            > line 2
            
        """.trimIndent()
    }

    test("horizontal line") {
        val line = Md.HorizontalLine
        val visitor = TextVisitor()
        line.accept(visitor)
        visitor.render() shouldBe """
            ---
            
        """.trimIndent()
    }

    test("paragraph-adds") {
        val paragraph = Md.Paragraph().apply {
            add(Md.Sentence().text("text"))
        }
        val visitor = TextVisitor()
        paragraph.accept(visitor)
        visitor.render() shouldBe """
            text
            
            
        """.trimIndent()
    }

    test("paragraph-with-full-link-reference") {
        val paragraph = Md.Paragraph().apply {
            link("text", "url", "label")
        }
        val visitor = TextVisitor()
        paragraph.accept(visitor)
        visitor.render() shouldBe """
            [text][label]
            
            [label]: url
            
        """.trimIndent()
    }

    test("code block") {
        val doc = Md.generate {
            code("kotlin") {
            """
            fun main() {
                println("Hello world")
            }
            """.trimIndent()
            }
        }
        doc.asString() shouldBe """
            ```kotlin
            fun main() {
                println("Hello world")
            }
            ```
            
        """.trimIndent()
    }

    test("code without language") {
        val doc = Md.generate {
            code {
            """
            some code
            """.trimIndent()
            }
        }
        doc.asString() shouldBe """
            ```
            some code
            ```
            
        """.trimIndent()
    }


    test("doc") {
        val doc = Md.generate {
            title {
                +"My Markdown Document"
            }
            h(2) {
                +"Introduction"
            }
            p {
                +"This is a small test of how it looks like."
            }

            line()

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
        }

        doc.asString() shouldBe """
            # My Markdown Document
            
            ## Introduction
            
            This is a small test of how it looks like.
            
            ---
            
            - Just an item [link 1](url1)
            
            1. Just an enumerated item ![link 2]
            
            > Blockquote line [link 3][link 2]
            > Another Blockquote line
            
            
            [link 2]: url2
            
        """.trimIndent()
    }
})
