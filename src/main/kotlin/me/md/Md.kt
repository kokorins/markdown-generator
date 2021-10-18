package me.md

object Md {

    @DslMarker
    annotation class MdMarker

    @MdMarker
    interface MdElement {
        fun accept(visitor: MdVisitor)
    }

    abstract class MdContainer<T : MdElement> : MdElement {
        val elements = mutableListOf<T>()
        fun add(element: T): MdContainer<T> {
            elements.add(element)
            return this
        }

        fun addAll(elements: List<T>): MdContainer<T> {
            this.elements.addAll(elements)
            return this
        }
    }

//    abstract class MdSentenceContainer : MdElement {
//        val sentences = mutableListOf<Sentence>()
//        fun add(element: Sentence): MdElement {
//            sentences.add(element)
//            return this
//        }
//    }

    object HorizontalLine : MdElement {
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }
    }

    sealed class Word : MdElement {
        data class Text(val text: String) : Word() {
            override fun accept(visitor: MdVisitor) {
                visitor.visit(this)
            }
        }

        data class WrappedText(val wrap: String, val text: String) : Word() {
            override fun accept(visitor: MdVisitor) {
                visitor.visit(this)
            }
        }

        data class Link(
            val text: Sentence,
            val url: String,
            val inPlace: Boolean,
            val label: String? = null
        ) : Word() {
            constructor(
                text: String,
                url: String,
                inPlace: Boolean,
                label: String? = null
            ) : this(Sentence().text(text), url, inPlace, label)

            override fun accept(visitor: MdVisitor) {
                visitor.visit(this)
            }
        }
    }

    class Sentence : MdContainer<Word>() {
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }

        fun asText(): String? {
            return if (isText()) {
                val textVisitor = TextVisitor()
                accept(textVisitor)
                textVisitor.render()
            } else {
                null
            }
        }

        fun isText(): Boolean {
            return elements.all {
                return when (it) {
                    is Word.Text -> true
                    is Word.WrappedText -> true
                    is Word.Link -> false
                }
            }
        }

        fun text(text: String): Sentence {
            add(Word.Text(text))
            return this
        }

        fun link(text: String, url: String, label: String? = null): Sentence {
            add(Word.Link(Sentence().text(text), url, false, label))
            return this
        }

        fun image(text: String, url: String, label: String? = null): Sentence {
            add(Word.Link(Sentence().text(text), url, true, label))
            return this
        }

        fun i(text: String): Sentence {
            add(Word.WrappedText("_", text))
            return this
        }

        fun b(text: String): Sentence {
            add(Word.WrappedText("**", text))
            return this
        }

        fun ib(text: String): Sentence {
            add(Word.WrappedText("***", text))
            return this
        }

        operator fun String.unaryPlus(): Sentence {
            return text(this)
        }

        operator fun plus(sentence: Sentence): Sentence {
            if (this != sentence) {
                addAll(sentence.elements)
            }
            return this
        }

        operator fun plus(text: String): Sentence {
            add(Word.Text(text))
            return this
        }
    }

    data class Header(val level: Int, val sentence: Sentence) : MdElement {
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }
    }

    class Paragraph : MdElement {
        var current = Sentence()
        val sentences = mutableListOf<MdElement>(current)
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }

        fun link(url: String, label: String? = null, text: Sentence.() -> Sentence): Sentence {
            current.add(Word.Link(text(Sentence()), url, false, label))
            return current
        }

        fun image(url: String, label: String? = null, text: Sentence.() -> Sentence): Sentence {
            current.add(Word.Link(text(Sentence()), url, true, label))
            return current
        }

        fun link(text: String, url: String, label: String? = null): Sentence {
            current.add(Word.Link(Sentence().text(text), url, false, label))
            return current
        }

        fun labeled(label: String, text: String = label, url: () -> String): Sentence {
            current.add(Word.Link(Sentence().text(text), url(), false, label))
            return current
        }

        fun image(text: String, url: String, label: String? = null): Sentence {
            current.add(Word.Link(Sentence().text(text), url, true, label))
            return current
        }

        fun i(text: String): Sentence {
            current.add(Word.WrappedText("_", text))
            return current
        }

        fun b(text: String): Sentence {
            current.add(Word.WrappedText("**", text))
            return current
        }

        fun ib(text: String): Sentence {
            current.add(Word.WrappedText("***", text))
            return current
        }

        fun br(): Paragraph {
            current = Sentence()
            sentences.add(current)
            return this
        }

        fun Sentence.br(): Paragraph {
            return this@Paragraph.br()
        }

        operator fun String.unaryPlus(): Sentence {
            return text(this)
        }

        fun text(text: String): Sentence {
            current.add(Word.Text(text))
            return current
        }

        fun add(sentence: Sentence): Paragraph {
            current.plus(sentence)
            current = Sentence()
            sentences.add(current)
            return this
        }
    }

    class Itemize : MdContainer<MdElement>() {
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }

        fun item(block: Sentence.() -> Unit): Itemize {
            add(Sentence().apply(block))
            return this
        }

        fun itemize(block: Itemize.() -> Unit): Itemize {
            add(Itemize().apply(block))
            return this
        }

        fun enumerate(block: Enumerate.() -> Unit): Itemize {
            add(Enumerate().apply(block))
            return this
        }
    }

    class Enumerate : MdContainer<MdElement>() {
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }

        fun item(block: Sentence.() -> Unit): Enumerate {
            add(Sentence().apply(block))
            return this
        }

        fun itemize(block: Itemize.() -> Unit): Enumerate {
            add(Itemize().apply(block))
            return this
        }

        fun enumerate(block: Enumerate.() -> Unit): Enumerate {
            add(Enumerate().apply(block))
            return this
        }
    }

    class Blockquotes : MdContainer<Sentence>() {
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }

        fun item(block: Sentence.() -> Unit): Blockquotes {
            add(Sentence().apply(block))
            return this
        }
    }

    class Document : MdContainer<MdElement>() {
        fun line() {
            add(HorizontalLine)
        }

        fun title(block: Sentence.() -> Unit) {
            h(1, block)
        }

        fun h(level: Int, block: Sentence.() -> Unit) {
            add(Header(level, Sentence().apply(block)))
        }

        fun p(block: Paragraph.() -> Unit) {
            add(Paragraph().apply(block))
        }

        fun itemize(block: Itemize.() -> Unit) {
            add(Itemize().apply(block))
        }

        fun enumerate(block: Enumerate.() -> Unit) {
            add(Enumerate().apply(block))
        }

        fun blockquote(block: Blockquotes.() -> Unit) {
            add(Blockquotes().apply(block))
        }

        fun code(language: String = "", listing: () -> String) {
            add(Word.WrappedText("```", "$language\n${listing()}\n"))
        }

        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }

        fun asString(): String {
            val visitor = TextVisitor()
            this.accept(visitor)
            return visitor.render()
        }
    }

    fun generate(doc: Document.() -> Unit): Document {
        val document = Document()
        doc(document)
        return document
    }
}