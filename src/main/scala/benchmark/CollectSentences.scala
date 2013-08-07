package benchmark

import edu.knowitall.ollie.Ollie
import edu.knowitall.ollie.OllieExtraction
import edu.knowitall.tool.parse.StanfordParser
import edu.knowitall.ollie.OllieExtractionInstance
import edu.knowitall.tool.postag.StanfordPostagger;




object CollectSentences {
  
  
  
  def main(args: Array[String]){
    val ollie = new Ollie()
    val parser = new StanfordParser(new StanfordPostagger());

    
    val lines = scala.io.Source.fromFile("slotanswers")(scala.io.Codec.UTF8).getLines.toList
    var idSentenceTupleList = List[(Int,String)]()
    for(l <- lines.drop(1)){
      val vals = l.split("\t")
      val fillerId = vals(0).toInt
      val docId = vals(4)
      val begOffset = vals(5).toInt
      val entityName = vals(7)
      idSentenceTupleList = idSentenceTupleList :+ (fillerId,findSentence(fillerId,docId,begOffset,entityName))
    }
    
    //filter out empty sentences and collapse equal sentences
    var sentenceListIdMap = Map[String,List[Int]]()
    for(t <- idSentenceTupleList){
      if(t._2 != ""){
	      if(sentenceListIdMap.contains(t._2)){
	        sentenceListIdMap += ((t._2, t._1 :: sentenceListIdMap.get(t._2).get))
	      }
	      else{
	        sentenceListIdMap += ((t._2, List(t._1)))
	      }
      }
    }
    
    var listWithExtractions = List[(String,List[Extraction],List[Int])]()
    //filter if ollie finds any extractions
    sentenceListIdMap.foreach(p => {
      try{
        val graph = parser.dependencyGraph(p._1)
        var extractions = List[Extraction]()
        for (e <- ollie.extract(graph)){
          
          extractions = extractions :+ new Extraction(e.extr.arg1.text,e.extr.rel.text,e.extr.arg2.text)
        }
        listWithExtractions = listWithExtractions :+ (p._1,extractions.toList,p._2)
      }
      catch{
        case e: Exception => {}
      }
    })
    
    val pw = new java.io.PrintWriter(new java.io.File("sentenceExtractions.out"))
    for( a <- listWithExtractions){
      pw.write(a._1.replaceAll("\\s+", " "))
      for( e <- a._2){
        pw.write("\t"+e)
      }
      for( fId <- a._3){
        pw.write("\tfillerId:"+fId)
      }
      pw.write("\n")
    }
    pw.close()
    
    
    
  }
  
  
  def findSentence(fillerId: Int, docId: String, begOffset: Int, entityName:String):String = {
    
    SolrHelper.getSentenceFromDocWithParagraphFormat(docId, begOffset, entityName).getOrElse({""})
  }
  
  private class Extraction(val arg1:String, val rel: String, val arg2: String){
    
    override def toString(): String = {
      Iterator(arg1,rel,arg2).mkString("(",",",")")
    }
    
  }

}