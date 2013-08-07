package benchmark

import edu.knowitall.ollie.Ollie
import edu.knowitall.ollie.OllieExtraction
import edu.knowitall.tool.parse.StanfordParser
import edu.knowitall.ollie.OllieExtractionInstance
import edu.knowitall.tool.postag.StanfordPostagger
import edu.knowitall.chunkedextractor.ReVerb
import edu.knowitall.tool.chunk.OpenNlpChunker
import edu.knowitall.tool.postag.OpenNlpPostagger
import edu.knowitall.tool.sentence.OpenNlpSentencer
import edu.knowitall.tool.tokenize.OpenNlpTokenizer

object CollectExtractions {
  
  
  
  def main(args: Array[String]){
    val ollie = new Ollie()
    val reverb = new ReVerb()
    val parser = new StanfordParser(new StanfordPostagger())
    val chunker =  new OpenNlpChunker()
    val sentence = new OpenNlpSentencer()
    val tokenizer = new OpenNlpTokenizer()

    
    val lines = scala.io.Source.fromFile("sentences")(scala.io.Codec.UTF8).getLines.toList
    var sentenceTupleList = List[(String,String,String)]()
    for(l <- lines){
      val vals = l.split("\t")
      val sentence = vals(0)
      val docId = vals(1)
      val fillerIds = vals(2)
      sentenceTupleList = sentenceTupleList :+ (sentence,docId,fillerIds)
    }

    
    var listWithExtractions = List[(String,String,String,List[Extraction])]()
    sentenceTupleList.foreach(p => {
      try{
        val graph = parser.dependencyGraph(p._1)
        var extractions = List[Extraction]()
        for (e <- ollie.extract(graph)){
          
          extractions = extractions :+ new Extraction(e.extr.arg1.text,e.extr.rel.text,e.extr.arg2.text)
        }
        for(e <- reverb.extract(chunker.chunkTokenized(tokenizer.tokenize(p._1)))){
          
          extractions = extractions :+ new Extraction(e.extr.arg1.text,e.extr.rel.text,e.extr.arg2.text)
        }
        extractions = extractions.toSet.toList
        listWithExtractions = listWithExtractions :+ (p._1,p._2,p._3,extractions)
      }
      catch{
        case e: Exception => {}
      }
    })
    
    val pw = new java.io.PrintWriter(new java.io.File("extractions.txt"))
    for( a <- listWithExtractions){
      pw.write(a._1 + "\t" + a._2 + "\t" + a._3 + "\n")
      for( e <- a._4){
        pw.write("\t"+e+"\n")
      }
    }
    pw.close()
  }
  
  
  def findSentence(fillerId: Int, docId: String, begOffset: Int, entityName:String):String = {
    
    SolrHelper.getSentenceFromDocWithParagraphFormat(docId, begOffset, entityName).getOrElse({""})
  }
  
  private case class Extraction(val arg1:String, val rel: String, val arg2: String){
    override def toString(): String = {
      Iterator(arg1,rel,arg2).mkString("(",",",")")
    }
  }

}