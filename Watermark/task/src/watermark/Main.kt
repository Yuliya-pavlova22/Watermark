package watermark
import org.cef.CefSettings.ColorType
import org.w3c.dom.css.RGBColor
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Color
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.system.exitProcess
import kotlin.io.path.Path
import kotlin.io.path.fileSize

public  class Image {
    var file = ""
    var width = 0
    var height = 0
    var numCom = 0
    var numColCom = 0
    var bits = 0
    var tranc = ""
}
public val trnsprenc = mapOf<Int, String>(1 to "OPAQUE", 2 to "BITMASK", 3 to "TRANSLUCENT")

fun main() = try {
    val myImage: BufferedImage = CreateInputImg()
    val myWatermark: BufferedImage = CreateWaterImg()

//проверяем размеры изображений
    СompSize(myImage,myWatermark)

    // использовать альфа-канал?
    var isTrans = false
    // a transparency color?
    var isTransColor = false

    if (trnsprenc.get(myWatermark.transparency).toString() == "TRANSLUCENT") {
        println("Do you want to use the watermark's Alpha channel?")
        isTrans = readln().lowercase() == "yes"
    } else {
        println("Do you want to set a transparency color?")
        isTransColor = readln().lowercase() == "yes"
    }
    //
    val trColor: Color = CreatetrColor(isTransColor)

    val trancPerc = CreatePercent()

    val singMetod = IsSingleMetod()

    var positionList: List<String> = listOf("","")
    if (singMetod) {
        positionList = Position(myImage,myWatermark)
        //println(positionList)
    }


    // создание файла  и наложение водяного знака
    val myImageOut: BufferedImage = CreateOutImg(myImage, myWatermark, isTrans, isTransColor, trancPerc, trColor, positionList, singMetod)

}
catch (ex: RuntimeException) {
    println(ex.message)
}

fun Position(myImage: BufferedImage, myWatermark: BufferedImage): List<String> {
    val difX = myImage.width - myWatermark.width
    val difY = myImage.height - myWatermark.height
    println("Input the watermark position ([x 0-$difX] [y 0-$difY]):")
    var strMetod = readln()!!.trim().split(" ")
    if (strMetod[0].toIntOrNull() != null && strMetod[1].toIntOrNull() != null ) {
        if (strMetod[0].toInt() !in 0..difX || strMetod[1].toInt() !in 0..difY) {
            throw RuntimeException("The position input is out of range.")

        } else {
            val posX = strMetod[0].toInt()
            val posy = strMetod[1].toInt()
        }
    } else
        throw RuntimeException("The position input is invalid.")

    return strMetod
}

// true - single
// false - grid
fun IsSingleMetod(): Boolean {
    println("Choose the position method (single, grid):")
    val strPos = readln()!!
    var pos =  if (strPos.lowercase() == "single") true
    else
        if (strPos.lowercase() == "grid") false
        else {
        throw RuntimeException("The position method input is invalid.")
        }
    return pos
}

fun СompSize(myImage: BufferedImage, myWatermark: BufferedImage) {
    if (myWatermark.width <= myImage.width && myWatermark.height <= myImage.height){
        return
    } else {
        throw RuntimeException("The watermark's dimensions are larger.")
    }

}

fun CreateOutImg(myImage: BufferedImage,
                 myWatermark: BufferedImage,
                 isTrans: Boolean,
                 isTransColor: Boolean,
                 trancPerc: Int,
                 trColor: Color,
                 positionList: List<String>,
                 singMetod : Boolean): BufferedImage {
    println("Input the output image filename (jpg or png extension):")
    val outImg = readln()!!
    var formatImg = ""
    if (checkFormat(outImg)) {
        formatImg = outImg.substring(outImg.indexOf('.') + 1)
        // println(formatImg)
    } else {
        throw RuntimeException("The output file extension isn't \"jpg\" or \"png\".")

    }
    val myImageOutf: BufferedImage = BufferedImage(myImage.width, myImage.height, BufferedImage.TYPE_INT_RGB)


    for (x in 0 until myImageOutf.width) {
        for (y in 0 until myImageOutf.height) {
            var w: Color = Color(myImage.getRGB(x, y), isTrans)
            var i: Color = Color(myImage.getRGB(x, y), isTrans)
            if (singMetod) {
                val posX = positionList[0].toInt()
                val posY = positionList[1].toInt()
                if (x in posX until posX + myWatermark.width && y in posY until posY + myWatermark.height)
                w = Color(myWatermark.getRGB(x - posX, y - posY ), isTrans)
            } else {
               // w = Color(myWatermark.getRGB(x % myWatermark.width, y % myWatermark.height))
                w = Color(myWatermark.getRGB(x % myWatermark.width , y % myWatermark.height), isTrans)
            }

            val color =
                if (w == trColor && isTransColor) {
                    Color(
                        i.red,
                        i.green,
                        i.blue
                    )
                } else
                    Color(
                        (trancPerc * w.red + (100 - trancPerc) * i.red) / 100,
                        (trancPerc * w.green + (100 - trancPerc) * i.green) / 100,
                        (trancPerc * w.blue + (100 - trancPerc) * i.blue) / 100
                    )
            myImageOutf.setRGB(x, y, (if (isTrans && w.alpha == 0) i else color).rgb)
        }
    }


    val outputFileJpg = File(outImg)  // Output the file
    ImageIO.write(myImageOutf, formatImg, outputFileJpg)
    println("The watermarked image $outImg has been created.")

    return  myImageOutf
}


fun CreatePercent(): Int {
    println("Input the watermark transparency percentage (Integer 0-100):")
    val strPerc = readln()!!
    if (!isDig(strPerc)) {
        throw RuntimeException("The transparency percentage isn't an integer number.")

    }

    val trancPercf = strPerc.toInt()
    if (trancPercf !in 0..100) {
        throw RuntimeException("The transparency percentage is out of range.")
    }
    return  trancPercf
}

fun CreatetrColor(isTransColor: Boolean): Color {
    var trColorf = Color(0,0,0)
    if (isTransColor) {
        println("Input a transparency color ([Red] [Green] [Blue]):")
        val strTrColor = readln()!!

        if (checkTrColor(strTrColor)) {
            var trColorstr = strTrColor.split(" ").map { it.toInt() }
            // println(trColorstr)
            trColorf = Color(trColorstr[0], trColorstr[1], trColorstr[2])
        } else throw RuntimeException("The transparency color input is invalid.")

    }
    return  trColorf
}

fun CreateWaterImg(): BufferedImage {
    println("Input the watermark image filename:")
    val str2 = readln()!!
    val inputFileWater = File(str2)


    if (!inputFileWater.exists()) {
        throw RuntimeException("The file $str2 doesn't exist.")
    }
    val myWatermarkf: BufferedImage = ImageIO.read(inputFileWater)

    if (checkNumCol(myWatermarkf)) {
        throw RuntimeException("The number of watermark color components isn't 3.")

    }
    // создаем экземпляр класса для изображения водяного знака
    var watermark = Image()
    watermark = properties(watermark, myWatermarkf, str2)

    if (checkBits(myWatermarkf)) {
        throw RuntimeException("The watermark isn't 24 or 32-bit.")

    }
    return myWatermarkf

}

fun CreateInputImg(): BufferedImage {
    println("Input the image filename:")
    var str = readln()!!
    var inputFile: File = File(str)

    if (!inputFile.exists()){
        throw RuntimeException("The file $str doesn't exist.")
    }
    var myImagef: BufferedImage
    myImagef= ImageIO.read(inputFile)
    // создаем экземпляр класса для входящего изображения
    var inputImg = Image()
    inputImg = properties(inputImg, myImagef, str)

    if (checkNumCol(myImagef)) {
        throw RuntimeException("The number of image color components isn't 3.")
    }

    if (checkBits(myImagef)) {
        throw RuntimeException("The image isn't 24 or 32-bit.")
    }
    return  myImagef
}


//проверка на цвета
fun checkTrColor(str: String): Boolean {
    var rezult = true
    str.trim()
    val rgb = str.split(" ")
   // println(rgb.joinToString())
    if (rgb.lastIndex == 2) {
    for (i in rgb) {
        rezult = rezult && if (isDig(i)) {
            if (i.toInt() in 0..255) rezult
            else false
        } else false
    }
    } else rezult = false
    return  rezult
}

// присвоение свойств изображения для экземпляра класса
fun properties(img: Image, imgFile: BufferedImage, str : String): Image {
    var trnsprenc = mapOf<Int, String>(1 to "OPAQUE", 2 to "BITMASK", 3 to "TRANSLUCENT")
    img.file = str
    img.width = imgFile.width
    img.height = imgFile.height
    img.numCom = imgFile.colorModel.numComponents
    img.numColCom = imgFile.colorModel.numColorComponents
    img.bits = imgFile.colorModel.pixelSize
    img.tranc = trnsprenc.get(imgFile.transparency).toString()
    return  img
}

// проверка на 3-х цветные компоненты
fun checkNumCol(inputImg : BufferedImage): Boolean {
    return  inputImg.colorModel.numColorComponents != 3
}
//не является 24-битным или 32-битным
fun checkBits(inputImg : BufferedImage): Boolean {
    return  inputImg.colorModel.pixelSize != 24 && inputImg.colorModel.pixelSize != 32
}
//проверка на числа
fun isDig(str: String) : Boolean {
    var rezult = true
    str.forEach { rezult = rezult && it.isDigit() }
    return  rezult
}

fun checkFormat(str: String): Boolean {
    if ( ".jpg" !in str || ".png" !in str) {
        val index = str.indexOf('.')
        val dif = str.lastIndex - index
        return dif == 3 && (str.substring(index + 1) == "jpg" || str.substring(index + 1) == "png")
    } else return false
}

fun printInfo(filename: String, bi: BufferedImage) {
    println("Image file: $filename")

    println("Width: ${bi.width}")
    println("Height: ${bi.height}")
    println("Number of components: ${bi.colorModel.numComponents}")
    println("Number of color components: ${bi.colorModel.numColorComponents}")
    println("Bits per pixel: ${bi.colorModel.pixelSize}")
   // println("Transparency: ${getTransparency(bi.transparency)}")

}
