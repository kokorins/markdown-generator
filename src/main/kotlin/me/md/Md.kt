package me.md

object Md {

    @DslMarker
    annotation class MdMarker

    @MdMarker
    interface MdElement {
        fun accept(visitor: MdVisitor)
    }

    abstract class MdContainer : MdElement {
        val elements = mutableListOf<MdElement>()
        fun add(element: MdElement): MdElement {
            elements.add(element)
            return this
        }
    }

    abstract class MdSentenceContainer : MdElement {
        val sentences = mutableListOf<Sentence>()
        fun add(element: Sentence): MdElement {
            sentences.add(element)
            return this
        }
    }

    data class Text(val text: String) : MdElement {
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }
    }

    data class WrappedText(val wrap: String, val text: String) : MdElement {
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }
    }

    object HorizontalLine : MdElement {
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }
    }

   class Sentence : MdContainer() {
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }

        fun text(text: String): Sentence {
            add(Text(text))
            return this
        }

        fun link(text: String, url: String, label: String? = null): Sentence {
            add(Link(text, url, false, label))
            return this
        }

        fun image(text: String, url: String, label: String? = null): Sentence {
            add(Link(text, url, true, label))
            return this
        }

        fun i(text: String): Sentence {
            add(WrappedText("_", text))
            return this
        }

        fun b(text: String): Sentence {
            add(WrappedText("**", text))
            return this
        }

        fun ib(text: String): Sentence {
            add(WrappedText("***", text))
            return this
        }

        operator fun String.unaryPlus(): Sentence {
            return text(this)
        }

        operator fun plus(sentence: Sentence): Sentence {
            if (this != sentence) {
                add(sentence)
            }
            return this
        }

        operator fun plus(text: String): Sentence {
            add(Text(text))
            return this
        }
    }

    data class Link(
            val text: String,
            val url: String,
            val inPlace: Boolean,
            val label: String? = null
    ) : MdElement {
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
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

        fun link(text: String, url: String, label: String? = null): Sentence {
            current.add(Link(text, url, false, label))
            return current
        }

        fun image(text: String, url: String, label: String? = null): Sentence {
            current.add(Link(text, url, true, label))
            return current
        }

        fun i(text: String): Sentence {
            current.add(WrappedText("_", text))
            return current
        }

        fun b(text: String): Sentence {
            current.add(WrappedText("**", text))
            return current
        }

        fun ib(text: String): Sentence {
            current.add(WrappedText("***", text))
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
            current.add(Text(text))
            return current
        }

        fun add(sentence: Sentence): Paragraph {
            current.plus(sentence)
            current = Sentence()
            sentences.add(current)
            return this
        }
    }

    class Itemize : MdSentenceContainer() {
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }

        fun item(block: Sentence.() -> Unit): Itemize {
            add(Sentence().apply(block))
            return this
        }
    }

    class Enumerate : MdSentenceContainer() {
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }

        fun item(block: Sentence.() -> Unit): Enumerate {
            add(Sentence().apply(block))
            return this
        }
    }

    class Blockquotes : MdSentenceContainer() {
        override fun accept(visitor: MdVisitor) {
            visitor.visit(this)
        }

        fun item(block: Sentence.() -> Unit): Blockquotes {
            add(Sentence().apply(block))
            return this
        }
    }

    class Document : MdContainer() {
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
            add(WrappedText("```", "$language\n${listing()}\n"))
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