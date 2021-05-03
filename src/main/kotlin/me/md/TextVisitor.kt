package me.md

class TextVisitor : MdVisitor {
    data class LinkDefinition(val label: String, val url: String)

    private val builder = StringBuilder()
    private val links = mutableMapOf<String, LinkDefinition>()
    private var itemLevel = -1

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

    override fun visit(text: Md.Word.Text) {
        builder.append(text.text)
    }

    override fun visit(wrappedText: Md.Word.WrappedText) {
        builder.append(wrappedText.wrap)
        builder.append(wrappedText.text)
        builder.append(wrappedText.wrap)
    }

    override fun visit(link: Md.Word.Link) {
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
        itemLevel += 1
        itemize.elements.forEach {
            when (it) {
                is Md.Sentence -> {
                    builder.append(" ".repeat(itemLevel * 2))
                    builder.append("- ")
                    it.accept(this)
                    builder.appendLine()
                }
                else -> {
                    it.accept(this)
                }
            }
        }
        itemLevel -= 1
    }

    override fun visit(enumerate: Md.Enumerate) {
        itemLevel += 1
        enumerate.elements.forEachIndexed { index, it ->
            when (it) {
                is Md.Sentence -> {
                    builder.append(" ".repeat(itemLevel * 2))
                    builder.append("${index + 1}. ")
                    it.accept(this)
                    builder.appendLine()
                }
                else -> {
                    it.accept(this)
                }
            }
        }
        itemLevel -= 1
    }

    override fun visit(blockquotes: Md.Blockquotes) {
        blockquotes.elements.forEach { sentence ->
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