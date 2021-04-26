package me.md

class TextVisitor : MdVisitor {
    data class LinkDefinition(val label: String, val url: String)

    private val builder = StringBuilder()
    private val links = mutableMapOf<String, LinkDefinition>()

    fun render(): String {
        if (links.isNotEmpty()) {
            builder.appendLine()
        }
        for (link in links.values) {
            builder.appendLine("[${link.label}]: ${link.url}")
        }
        return builder.toString()
    }

    override fun visit(horizontalLine: Md.HorizontalLine) {
        builder.appendLine("---")
    }

    override fun visit(text: Md.Text) {
        builder.append(text.text)
    }

    override fun visit(wrappedText: Md.WrappedText) {
        builder.append(wrappedText.wrap)
        builder.append(wrappedText.text)
        builder.append(wrappedText.wrap)
    }

    override fun visit(link: Md.Link) {
        if (link.label != null) { // reference
            links[link.label] = LinkDefinition(link.label, link.url)
            val text = link.text.asText()
            if (text == link.label) { // short reference
                if (link.inPlace) {
                    builder.append("!")
                }
                builder.append("[${link.label}]")
            } else {
                if (link.inPlace) {
                    builder.append("!")
                }
                builder.append("[")
                link.text.accept(this)
                builder.append("][${link.label}]")
            }
        } else {
            if (link.inPlace) {
                builder.append("!")
            }
            builder.append("[")
            link.text.accept(this)
            builder.append("](${link.url})")
        }
    }

    override fun visit(sentence: Md.Sentence) {
        sentence.elements.forEach { it.accept(this) }
    }

    override fun visit(header: Md.Header) {
        builder.append("#".repeat(header.level) + " ")
        header.sentence.accept(this)
        builder.appendLine()
    }

    override fun visit(paragraph: Md.Paragraph) {
        paragraph.sentences.forEach {
            it.accept(this)
            builder.appendLine()
        }
    }

    override fun visit(itemize: Md.Itemize) {
        itemize.sentences.forEach { sentence ->
            builder.append("- ")
            sentence.accept(this)
            builder.appendLine()
        }
    }

    override fun visit(enumerate: Md.Enumerate) {
        enumerate.sentences.forEachIndexed { index, sentence ->
            builder.append("${index + 1}. ")
            sentence.accept(this)
            builder.appendLine()
        }
    }

    override fun visit(blockquotes: Md.Blockquotes) {
        blockquotes.sentences.forEach { sentence ->
            builder.append("> ")
            sentence.accept(this)
            builder.appendLine()
        }
    }

    override fun visit(document: Md.Document) {
        document.elements.forEach {
            it.accept(this)
            builder.appendLine()
        }
    }
}