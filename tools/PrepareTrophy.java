import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Fit an already cut-out PNG into the game's native trophy canvas. */
public class PrepareTrophy {
    public static void main(String[] args)throws Exception {
        BufferedImage source=ImageIO.read(Paths.get(args[0]).toFile());
        int left=source.getWidth(),top=source.getHeight(),right=-1,bottom=-1;
        for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++)
            if((source.getRGB(x,y)>>>24)>16){left=Math.min(left,x);top=Math.min(top,y);right=Math.max(right,x);bottom=Math.max(bottom,y);}
        if(right<left)throw new IllegalArgumentException("Empty trophy");
        int width=right-left+1,height=bottom-top+1;
        double scale=Math.min(80.0/width,120.0/height);
        int w=(int)Math.round(width*scale),h=(int)Math.round(height*scale);
        BufferedImage target=new BufferedImage(84,124,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=target.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(source,(84-w)/2,(124-h)/2,(84+w)/2,(124+h)/2,left,top,right+1,bottom+1,null);g.dispose();
        Path path=Paths.get(args[1]);Files.createDirectories(path.getParent());ImageIO.write(target,"png",path.toFile());
    }
}
