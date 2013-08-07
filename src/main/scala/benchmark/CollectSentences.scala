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
import scala.util.Random


object CollectSentences {
  
  
  
  def main(args: Array[String]){
    val ollie = new Ollie()
    val parser = new StanfordParser(new StanfordPostagger())


    
    val lines = scala.io.Source.fromFile("slotanswers")(scala.io.Codec.UTF8).getLines.toList
    var idSentenceTupleList = List[(Int,String,String)]()
    for(l <- lines.drop(1)){
      val vals = l.split("\t")
      val fillerId = vals(0).toInt
      val docId = vals(4)
      val begOffset = vals(5).toInt
      val entityName = vals(7)
      idSentenceTupleList = idSentenceTupleList :+ (fillerId,docId,findSentence(fillerId,docId,begOffset,entityName))
    }
    
    
    //filter out empty sentences and collapse equal sentences
    var sentenceListIdMap = Map[String,(String,List[Int])]()
    for(t <- idSentenceTupleList){
      if(t._3 != "" && !t._3.contains("<")
          && !t._3.contains(">")
          && !t._3.contains("/")
          && !t._3.contains("(")
          && !t._3.contains(")")
          && !t._3.contains("-")
          && !t._3.contains("[")
          && !t._3.contains("]")){
	      if(sentenceListIdMap.contains(t._3)){
	        sentenceListIdMap +=  ((t._3,(t._2, t._1 :: sentenceListIdMap.get(t._3).get._2)))
	      }
	      else{
	        sentenceListIdMap += ((t._3, (t._2, List(t._1))))
	      }
      }
    }
    
    var listWithExtractions = List[(String,List[Extraction],String,List[Int])]()
    //filter if we find any extractions
    sentenceListIdMap.foreach(p => {
      try{
        val graph = parser.dependencyGraph(p._1)
        var extractions = List[Extraction]()
        for (e <- ollie.extract(graph)){
          
          extractions = extractions :+ new Extraction(e.extr.arg1.text,e.extr.rel.text,e.extr.arg2.text)
        }
        listWithExtractions = listWithExtractions :+ (p._1,extractions.toList,p._2._1,p._2._2)
      }
      catch{
        case e: Exception => {}
      }
    })
    
    listWithExtractions = Random.shuffle(listWithExtractions).take(100)
    val pw = new java.io.PrintWriter(new java.io.File("sentences"))
    for( a <- listWithExtractions){
    	pw.write(a._1.replaceAll("\\s+"," ") +"\t" + a._3)
    	pw.write(a._4.toIterator.mkString("\t",",",""))
    	pw.write("\n")
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