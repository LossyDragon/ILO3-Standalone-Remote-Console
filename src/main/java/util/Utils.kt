package util

import java.awt.Image
import java.awt.Toolkit
import javax.swing.JApplet

@Suppress("DEPRECATION")
class Utils {
    companion object {
        @JvmStatic
        fun getResourceImage(applet: JApplet, image: String): Image {
            val resource = applet.javaClass.classLoader.getResource(image)
            return Toolkit.getDefaultToolkit().getImage(resource)
        }
    }
}
