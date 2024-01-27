import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
object titanic extends App {
  val spark = SparkSession.builder
    .appName("titanic")
    .master("local")
    .getOrCreate()

  val csvFilePath = "C:/Users/RandyShaw/Desktop/train.csv"

  val df: DataFrame = spark.read
    .option("header", "true")
    .option("inferSchema", "true")
    .csv(csvFilePath)

  //df.show()
/*
  //1. What is the average ticket fare for each Ticket class?
  val avgFareByClass = df.groupBy("Pclass")
    .agg(avg("Fare").alias("AverageFare"))

  avgFareByClass.show()
*/
/*

  //2. What is the survival percentage for each Ticket class? Which class has the highest survival rate?

  val totalPassengersByClass = df.groupBy("Pclass").count()

  val survivorsByClass = df.filter("Survived = 1").groupBy("Pclass").agg(count("*").alias("SurvivorCount"))

  val survivalStatsByClass = totalPassengersByClass
    .join(survivorsByClass, "Pclass")
    .withColumn("SurvivalPercentage", col("SurvivorCount") / col("count"))


  survivalStatsByClass.show()

  val highestSurvivalClass = survivalStatsByClass
    .orderBy(desc("SurvivalPercentage"))
    .select("Pclass")
    .first()

  println(s"The class with the highest survival rate is: ${highestSurvivalClass.getInt(0)}")
*/
/*

  //3. Find Rose
  val conditions = (col("Pclass") === 1) &&
    (col("Sex") === "female") &&
    (col("Age") === 17) &&
    (col("Sibsp") === 0) &&
    (col("Parch") === 1)

  val matchingRecords = df.filter(conditions)

  if (!matchingRecords.isEmpty) {
    val matchingRecord = matchingRecords.first()

    println("Matching Record:")
    println(matchingRecord)
  } else {
    println("No matching records found.")
  }

*/
/*
  //4. Find Jack
  val conditions = (col("Age").isin(19, 20)) &&
    (col("Survived") === 0) &&
    (col("Pclass") === 3) &&
    (col("Sex") === "male") &&
    (col("Parch") === 0) &&
    (col("SibSp") === 0)

  val matchingRecords = df.filter(conditions).collect()

  if (matchingRecords.nonEmpty) {
    println("Matching Records:")
    matchingRecords.foreach(println)
  } else {
    println("No matching records found.")
  }
*/


  //5. Split the age for every 10 years. 1-10 as one age group, 11- 20 as another etc.
  val ageGroups = Seq((0, 10), (10, 20), (20, 30), (30, 40), (40, 50), (50, 60), (60, 70), (70, 80))

  val ageGroupExpr = ageGroups
    .map { case (lower, upper) => s"when (Age > $lower and Age <= $upper) then '$lower-$upper'" }
    .mkString(" ")

  val dfWithAgeGroup = df.withColumn("AgeGroup", expr(s"case $ageGroupExpr else 'Unknown' end"))

  val survivalByAgeGroup = dfWithAgeGroup.groupBy("AgeGroup")
    .agg(
      sum("Survived").alias("Survived"),
      count("*").alias("TotalPassengers")
    )
    .withColumn("SurvivalRate", col("Survived") / col("TotalPassengers"))
    .orderBy("AgeGroup")

  println("Survival Rate per Age Group:")
  survivalByAgeGroup.show()

  val highestSurvivalAgeGroup = survivalByAgeGroup
    .orderBy(desc("SurvivalRate"))
    .select("AgeGroup")
    .first()

  println(s"The age group most likely to survive is: ${highestSurvivalAgeGroup.getString(0)}")


}
