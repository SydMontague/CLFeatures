package de.craftlancer.clfeatures.painter;

import java.awt.*;

public class HSLColor {
    private Color rgb;
    private float[] hsl;
    private float alpha;
    
    public HSLColor(Color rgb) {
        this.rgb = rgb;
        hsl = fromRGB(rgb);
        alpha = rgb.getAlpha() / 255.0f;
    }
    
    /**
     * Create a RGB Color object based on this HSLColor with a different
     * Shade. Changing the shade will return a darker color. The percent
     * specified is a relative value.
     *
     * @param percent - the value between 0 - 100
     * @return the RGB Color object
     */
    public Color adjustShade(float percent) {
        float multiplier = (100.0f - percent) / 100.0f;
        float l = Math.max(0.0f, hsl[2] * multiplier);
        
        return toRGB(hsl[0], hsl[1], l, alpha);
    }
    
    /**
     * Create a RGB Color object based on this HSLColor with a different
     * Tone. Changing the tone will return a lighter color. The percent
     * specified is a relative value.
     *
     * @param percent - the value between 0 - 100
     * @return the RGB Color object
     */
    public Color adjustTone(float percent) {
        float multiplier = (100.0f + percent) / 100.0f;
        float l = Math.min(100.0f, hsl[2] * multiplier);
        
        return toRGB(hsl[0], hsl[1], l, alpha);
    }
    
    /**
     * Convert a RGB Color to it corresponding HSL values.
     *
     * @return an array containing the 3 HSL values.
     */
    public static float[] fromRGB(Color color) {
        //  Get RGB values in the range 0 - 1
        
        float[] rgb = color.getRGBColorComponents(null);
        float r = rgb[0];
        float g = rgb[1];
        float b = rgb[2];
        
        //	Minimum and Maximum RGB values are used in the HSL calculations
        
        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));
        
        //  Calculate the Hue
        
        float h = 0;
        
        if (max == min)
            h = 0;
        else if (max == r)
            h = ((60 * (g - b) / (max - min)) + 360) % 360;
        else if (max == g)
            h = (60 * (b - r) / (max - min)) + 120;
        else if (max == b)
            h = (60 * (r - g) / (max - min)) + 240;
        
        //  Calculate the Luminance
        
        float l = (max + min) / 2;
        
        //  Calculate the Saturation
        
        float s = 0;
        
        if (max == min)
            s = 0;
        else if (l <= .5f)
            s = (max - min) / (max + min);
        else
            s = (max - min) / (2 - max - min);
        
        return new float[]{h, s * 100, l * 100};
    }
    
    /**
     * Convert HSL values to a RGB Color.
     *
     * @param h     Hue is specified as degrees in the range 0 - 360.
     * @param s     Saturation is specified as a percentage in the range 1 - 100.
     * @param l     Lumanance is specified as a percentage in the range 1 - 100.
     * @param alpha the alpha value between 0 - 1
     * @returns the RGB Color object
     */
    public static Color toRGB(float h, float s, float l, float alpha) {
        if (s < 0.0f || s > 100.0f) {
            String message = "Color parameter outside of expected range - Saturation";
            throw new IllegalArgumentException(message);
        }
        
        if (l < 0.0f || l > 100.0f) {
            String message = "Color parameter outside of expected range - Luminance";
            throw new IllegalArgumentException(message);
        }
        
        if (alpha < 0.0f || alpha > 1.0f) {
            String message = "Color parameter outside of expected range - Alpha";
            throw new IllegalArgumentException(message);
        }
        
        //  Formula needs all values between 0 - 1.
        
        h = h % 360.0f;
        h /= 360f;
        s /= 100f;
        l /= 100f;
        
        float q = 0;
        
        if (l < 0.5)
            q = l * (1 + s);
        else
            q = (l + s) - (s * l);
        
        float p = 2 * l - q;
        
        float r = Math.max(0, HueToRGB(p, q, h + (1.0f / 3.0f)));
        float g = Math.max(0, HueToRGB(p, q, h));
        float b = Math.max(0, HueToRGB(p, q, h - (1.0f / 3.0f)));
        
        r = Math.min(r, 1.0f);
        g = Math.min(g, 1.0f);
        b = Math.min(b, 1.0f);
        
        return new Color(r, g, b, alpha);
    }
    
    private static float HueToRGB(float p, float q, float h) {
        if (h < 0) h += 1;
        
        if (h > 1) h -= 1;
        
        if (6 * h < 1) {
            return p + ((q - p) * 6 * h);
        }
        
        if (2 * h < 1) {
            return q;
        }
        
        if (3 * h < 2) {
            return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));
        }
        
        return p;
    }
}