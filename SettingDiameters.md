# Introduction #

Every encoder has an inner and outer diameter setting so the encoder can be customized to fit your robot's wheels and encoder sensor array.

The picture above shows the inner and outer diameters of a wheel encoder.

Set the inner diameter by entering a number into the Inner Diameter field.  The inner diameter will visibly change after you tab out of the field or click elsewhere.

Likewise, set the outer diameter by entering a number into the Outer Diameter field. The outer diameter won't visibly change because the preview automatically scales the image to fit in the preview area. The inner diameter will change to show the change in proportions of your encoder.

# Details #

## Resolution ##

The size of the encoder IR sensor and the disc diameter limits resolution. The IR sensor needs a stripe of a certain minimum width.  Only so many stripes of a given width can fit on an encoder disc.

Bigger diameter encoder discs permit higher resolution patterns.  Suppose your encoder sensor can see an encoder stripe that is 5mm wide or wider.  A 50mm encoder disc with stripes that are 5mm wide at the edge will not have as many stripes, total, as a much larger 500mm encoder disc.

Your printer resolution and the size of the encoder disc limit the resolution of the disc.  With high resolution printers, say 600dpi, even if small stripes can be printed, they may be too small for an IR sensor to detect reliably.

## Wheel Mounted Encoder ##

If the encoder is to be mounted on the robot's wheel, the outer diameter has to be less than that of the wheel.  If the encoder is mounted on the gear train, inaccuracy due to gear lash will be introduced.  This is particularly evident when driving the motor in one direction, then the other direction.

## A single IR detector ##

A single track

## Multiple IR Detectors ##

The layout of the detectors, and the number of detectors, determines the inner and outer diameters that can be used.

Add your content here.  Format your content with:
  * Text in **bold** or _italic_
  * Headings, paragraphs, and lists
  * Automatic links to other wiki pages