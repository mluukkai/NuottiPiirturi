import scala.io.Source
import scala.collection.mutable.Buffer
import scala.io.StdIn._
import java.io._

class TiedostonLukeminen {

  var inputFromFile = Buffer[String]()       // kaikki input, paitsi tyhjät rivit
  var nuottiDataRiveina = Buffer[String]()  
  var nuottiAlkiot = Array[String]()         // splitattuna yllä oleva data
  var nuottiDatanRivinumerot = Buffer[Int]() // syötetiedoston nuottidatarivit muistiin, ei tyhjiä rivejä
  val lyriikkadata = Buffer[String]()        // biisin sanat

  var MIDIPatch = ""
  var tahtilaji = "4"
  var kappaleenNimi = ""
  var tiedostonNimi = ""
  val inputhakemistonNimi =  "./input_virheita/"
  val inputhakemisto = new File(inputhakemistonNimi)
  
  var rekursioCounter = 0

  
 
  helppiTeksti()
  listaaTiedostot()
 
  do {
    tiedostonNimi = readLine("\n\nMinkä nimisen tiedoston haluat nuoteiksi? Valitse ylläolevista. ")
  } while (!onkoListalla(tiedostonNimi))

 

 
  
 ///// F U N K T I O T: ///////////////////////////////////////////////////////////////////////// 
  
  def listaaTiedostot() = {
    var montakoNimeaRiville = 0
    for (tiedosto <- inputhakemisto.listFiles()) {     
       if (tiedosto.isFile) {
          print(tiedosto.getName + '\t')
          montakoNimeaRiville += 1
          if (montakoNimeaRiville == 8){
            println()
            montakoNimeaRiville = 0
          }
       }    
    }
  } 

  
  def onkoListalla(nimi: String): Boolean = {
    for (tiedosto <- inputhakemisto.listFiles())
      if (tiedosto.isFile && tiedosto.getName.toLowerCase() == nimi.toLowerCase().trim())
        return true
    false
  }

  
  def lueTiedosto(): Unit = {  
    
     this.inputFromFile = Buffer[String]()       // pitää nollata, jos tänne tullaan virheidentarkistuksesta
     this.nuottiDataRiveina = Buffer[String]() 
     this.nuottiDatanRivinumerot = Buffer[Int]()
     this.nuottiAlkiot = Array[String]() 
     val kayttajanValitsemaTiedosto = Source.fromFile(inputhakemistonNimi + tiedostonNimi)
      
     try {
       for (rivi <- kayttajanValitsemaTiedosto.getLines) {
          this.inputFromFile += rivi.trim
       }
     } finally {
        kayttajanValitsemaTiedosto.close()
     }
          
     kasitteleTunnisteet(this.inputFromFile) 
     tarkistaVirheet()
  }   
  
  
  def tarkistaVirheet() = {
     // println("nuottiDataRiveina :" + nuottiDataRiveina + " nuottiDatanRivinumerot: " + nuottiDatanRivinumerot)
 
     // splittaus & virheiden tarkistus:  
     var virheitaNolla = true
     var jatketaanko = true
     var korjattuVersio= ""
     for (i <- 0 until nuottiDataRiveina.size) {
        rekursioCounter += 1
        println("rekursioCounter forin alussa" + rekursioCounter)
        if(jatketaanko == true){        // jos löytyi virhe, loppuja alkioita ei haluta talteen
          var splitattuRivi = nuottiDataRiveina(i).split(" ") 
          for (alkio <- splitattuRivi) {
               if (alkio == "" ) {} // ylimääräisiä välilyöntejä ei nuottiAlkiot:hin  
               
               else if (alkio.head == '<'){
                  jatketaanko = tarkistaSoinnunVirheet(alkio, i)
                  println(jatketaanko)
               }   
               else if (oikeellisuusTesti(alkio) == "") {  // ei virhettä alkiossa, tarpeeksi infoa nuotin tekemiseen
                    println("rivi 95")
                    nuottiAlkiot = nuottiAlkiot :+ erikoistapauksetNuotinNimessa(alkio) // alkio sellaisenaan tai fixattuna
                    
               } else {  // virheellinen alkio:
               virheitaNolla =  false  
               korjattuVersio = readLine("\n\n syöte '" + alkio +"' on virheellinen: " + oikeellisuusTesti(alkio) + 
                 "\n Virhe on rivillä " + (nuottiDatanRivinumerot(i)+1)  +
                 "\n Korjaa äsken valitsemaasi tiedostoon ja paina ENTER, kun tiedosto on tallennettu. ")
                 
                 if(korjattuVersio == "")   lueTiedosto() 
               }
          }
     }  
         println("rekursioCounter forin lopussa" + rekursioCounter)
     } // end for nuottiDataRiveina
     
     println("nuottiAlkiot after nuottiDataRiveina-for: " +nuottiAlkiot.size )
     
     if (virheitaNolla) soundiValinta()
     //     for (i <- 0 until nuottiAlkiot.size)
     //        println (nuottiAlkiot(i))   
    
    
       def tarkistaSoinnunVirheet(alkio:String, ind:Int) = {
        
       var jatketaanko = true
         
                if(alkio.last != '>'){
                     jatketaanko = false
                     korjattuVersio = readLine("\n\n syöte '" + alkio +"' on virheellinen:  puuttuu soinnun lopetussymboli '>' tai olet vahingossa laittanut välilyönnin soinnun sisään" + 
                      "\n Virhe on rivillä " + (nuottiDatanRivinumerot(ind)+1)  +
                      "\n Korjaa äsken valitsemaasi tiedostoon ja paina ENTER, kun tiedosto on tallennettu. ")  
                     if(korjattuVersio == "")  lueTiedosto() else {}
                }     
                 
                else {
                   println("tarkistaSoinnunVirheet-  else")
                   var sointu =  alkio.tail.substring(0, alkio.size -2).split(",")  
                   for(i <- 0 until sointu.size) {
                      if (oikeellisuusTesti(sointu(i)) == "") {
                      //   println(erikoistapauksetNuotinNimessa(sointu(i)))
                          sointu(i) = erikoistapauksetNuotinNimessa(sointu(i))  // korvataan soinnun alkiot mahdollisilla fixauksilla
                      }
                      else {
                         virheitaNolla =  false  
                         jatketaanko = false
                         korjattuVersio = readLine("\n\n syöte '" + sointu(i) +"' on virheellinen: " + oikeellisuusTesti(sointu(i)) + 
                         "\n Virhe on rivillä " + (nuottiDatanRivinumerot(ind)+1)  +
                         "\n Korjaa äsken valitsemaasi tiedostoon ja paina ENTER, kun tiedosto on tallennettu. ")
                          if(korjattuVersio == "")  lueTiedosto()
                      }
                   }
                   
                   if(virheitaNolla == true){
                       var korjattuAlkio = "<"
                       for (aani<- sointu) korjattuAlkio += aani + ","
                       korjattuAlkio = korjattuAlkio.substring(0,korjattuAlkio.size-1) // ei vikaa pilkkua
                       korjattuAlkio += ">"
                       
                       println("korjattuAlkio: " + korjattuAlkio)
                       
                       nuottiAlkiot = nuottiAlkiot :+ korjattuAlkio  // alkio = <g1,h1>
                   }
                 
                 }  // end else: ei ole kyse koko alkiosta <....>
    println(" @sointu: " +jatketaanko)          
    jatketaanko 
    }// end tarkistaSoinnunVirheet
   
   println("rekursioCounter tarkistaVirheet lopussa" + rekursioCounter)   
  }  // end tarkistaVirheet

  
  
  
  def erikoistapauksetNuotinNimessa(alkio:String): String=  {
     val alkioPituustietoPois = alkio.filter(_ != '-')
     // case  c2# --> c#2
     if(alkioPituustietoPois.size == 3 && (alkio.tail.contains("#")  || alkio.tail.contains("b")) && !alkioPituustietoPois(2).isDigit) 
          return "" + alkio(0) + alkio(2) +alkio(1) +alkio.substring(3)  
     else if(alkioPituustietoPois == "b#1"  )      // popmuusikot kutsuvat h:ta b:ksi
        return "h#1" 
     else if(alkioPituustietoPois == "b#2" )
        return "h#2"
     alkio             // palautetaan alkio muuttumattomana jos ei tehdä mitään ylläolevista toimenpiteistä
  }
 
  
  def kasitteleTunnisteet(inputFromFile: Buffer[String]) = {  // tekstisyöterivejä

    var seuraavatrivitLyriikkaan = false
    for (i <- 0 until inputFromFile.size) {
      if (inputFromFile(i).trim.size != 0){  
         var kelvollinenSyoteRivi = inputFromFile(i).replaceAll("\t", "")
         
         if (kelvollinenSyoteRivi.head == '#') {    //  T U N N I S T E E T
            if (kelvollinenSyoteRivi.tail.toLowerCase().trim.contains("sanat")){
               seuraavatrivitLyriikkaan = true
              // varaudutaan siihen että joku kirjoittaa sanoja jo samalle riville kuin missä tunniste:
              if(kelvollinenSyoteRivi.tail.trim.substring(6).length != 0)  {
                      lyriikkadata += kelvollinenSyoteRivi.tail.trim.substring(6)
             }   
            } 
            else if (seuraavatrivitLyriikkaan == false) kasitteleKappaleenNimiJaTahtilaji(kelvollinenSyoteRivi, i)  // end lyriikat false
           
        } else if (seuraavatrivitLyriikkaan){    // L Y R I I K K A 
             lyriikkadata += kelvollinenSyoteRivi
        }
         
        else {    // L O P U T   ELI   N U O T I T      
          nuottiDatanRivinumerot += i
          nuottiDataRiveina += kelvollinenSyoteRivi.toLowerCase() 
        }
      }// if   .size != 0 
    } 
  //  println("kappaleenNimi: " + kappaleenNimi + ", tahtilaji" + tahtilaji)
  }
  
  
  def kasitteleKappaleenNimiJaTahtilaji(kelvollinenSyoteRivi:String, ind:Int) ={
    
           if (kelvollinenSyoteRivi.tail.toLowerCase().contains("nimi")) {
                kappaleenNimi = kelvollinenSyoteRivi.tail.substring(5, kelvollinenSyoteRivi.tail.size)
               
           }
           else if ("2345678".contains(kelvollinenSyoteRivi(1))){
                tahtilaji = kelvollinenSyoteRivi(1).toString      // tahtilaji pilalla jos myöhemmässä kommentissa on numero heti #:n jälkeen TODO
                   // varaudutaan siihen että joku kirjoittaa nuotteja jo samalle riville kuin missä tahtilaji-tunniste:
                   if(kelvollinenSyoteRivi.tail.trim.substring(1) != 0)  {
                      nuottiDataRiveina += kelvollinenSyoteRivi.tail.trim.substring(1)   // kaatuu jos käyttäjä on laittanut 5/4 --> /4 on  "nuottidataa"  TODO
                      nuottiDatanRivinumerot += ind
                   }   
           }         
            
  }

  
  def helppiTeksti() = {
    val helpFile = Source.fromFile("help.txt")   

    try {
      for (rivi <- helpFile.getLines) {
        println(rivi)
      }
      println()
    } finally {
      helpFile.close()
    }
  }
  
  
  def soundiValinta() = {
     do {
     MIDIPatch = readLine("\nMillä soundilla haluat kuulla kappaleen?\n" +
      "ENTER= en millään,  1= piano,  2= vibrafoni,  3= rock-urut,  4= syna,  5= akustinen kitara,  6= rokkibändi,  7=music box  ")
     } while (!"1234567".contains(MIDIPatch))
  }

  
  def oikeellisuusTesti(syote: String): String = {    // esim. g#1---
  
    // ALUKSI TUTKITAAN  N U O T T I E N   S Y N T A K SI ,  EI PITUUDET
         val filtteredNote = syote.filter(_ != '-').filter(_ != '.')
         
         if(filtteredNote == "z") {}  // taukojen syntaksi helppo, tehdään pituustesti myöhemmin
         else{
            if(filtteredNote.count(_ == 'z') > 1)
               return "taukojen pituudet merkitään viivoilla, esim puolitauko z--"
            if(!"cdefgahb".contains(filtteredNote.toLowerCase().head.toString()))
               return "nuotin pitää alkaa kirjaimilla c,C, d,D e,E f,F g,G a,A h,H, b tai B"   // väärä teksti jos "zz"
            else if(filtteredNote.size == 1 )   
               return "tarkoititko "+ syote + "1 vai " + syote + "2?"   
            else if(filtteredNote.size == 2 && (filtteredNote.tail.contains("#") || filtteredNote.tail.contains("b")) && !(filtteredNote.tail.contains("1")|| filtteredNote.tail.contains("2")))   
               return "tarkoititko "+ syote + "1 vai " + syote + "2?"      
            else if(filtteredNote.tail.contains("#b") ||  filtteredNote.tail.contains("b#"))    
                return "nuotissa on ylennys- ja alennusmerkki"   
            else if( !(filtteredNote.tail.contains("1")|| filtteredNote.tail.contains("2")))   
               return "sallitut oktaavialat ovat 1 ja 2"   
            else if(filtteredNote.size == 3 && !(filtteredNote.tail.contains("#") || filtteredNote.tail.contains("b")))   
                    return "väärä formaatti. Muistathan syntaksin: esim. alennettu e on Eb, ei es"   
            else if(filtteredNote.size > 3)
                return "liian pitkä nuotin nimi" 
          } // iso else
     
     //  P I T U U D E T  
         val lkm = syote.count(_ == '-')
         if(lkm > 4)
            return "maksimipituus nuotille on 4, eli viivoja korkeintaan ----"
         else if(lkm == 3 && syote.contains("."))    // ohjelmassa ei määritelty pisteellistä pisteellistä puolinuottia
            return "väärä pituus"
         else if(lkm == 4 && syote.contains("."))   // max pituus 4
            return "pisteellistä kokonuottia ei ole määritelty, tee kokonuotti ja 2 taukoa"
         else if(lkm == 0 && syote.contains("."))    // ei pisteellistä kahdeksasosaa
            return "tämä ohjelma ei osaa käsitellä pisteellistä kahdeksasosaa"
    
        else ""     
          
  } // end oikeellisuusTesti

}