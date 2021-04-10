package me.md

interface MdVisitor {
    fun visit(horizontalLine: Md.HorizontalLine)
    fun visit(text: Md.Text)
    fun visit(wrappedText: Md.WrappedText)
    fun visit(link: Md.Link)
    fun visit(sentence: Md.Sentence)
    fun visit(header: Md.Header)
    fun visit(paragraph: Md.Paragraph)
    fun visit(itemize: Md.Itemize)
    fun visit(enumerate: Md.Enumerate)
    fun visit(blockquotes: Md.Blockquotes)
    fun visit(document: Md.Document)
}