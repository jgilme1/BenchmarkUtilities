name := "BehncmarkUtilities"

version := "1.0"

scalaVersion := "2.10.1"

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "2.1.0",
  "edu.washington.cs.knowitall.openie" %% "openie-models" % "1.0",
  "edu.washington.cs.knowitall.openie" %% "openie-linker" % "1.0",
  "edu.washington.cs.knowitall.nlptools" %% "nlptools-sentence-opennlp" % "2.4.2",
  "edu.washington.cs.knowitall.ollie" %% "ollie-core" %"1.0.3",
  "edu.stanford.nlp" % "stanford-corenlp" % "1.3.5",
  "edu.washington.cs.knowitall.stanford-corenlp" % "stanford-parse-models" % "1.3.5",
  "edu.washington.cs.knowitall.stanford-corenlp" % "stanford-postag-models" % "1.3.5",
  "edu.washington.cs.knowitall.stanford-corenlp" % "stanford-ner-models" % "1.3.5",
  "edu.washington.cs.knowitall.nlptools" % "nlptools-parse-stanford_2.9.3" % "2.4.2",
  "edu.washington.cs.knowitall.nlptools" % "nlptools-postag-stanford_2.9.3" % "2.4.2",
  "jp.sf.amateras.solr.scala" %% "solr-scala-client" % "0.0.8"
)

resolvers ++= Seq(
  "nicta" at "http://nicta.github.com/scoobi/releases",
  "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "amateras-repo" at "http://amateras.sourceforge.jp/mvn/"
)

