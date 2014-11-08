#!/usr/bin/env bash
inkscape -f icon.svg -e ../app/src/main/res/drawable-mdpi/ic_launcher.png -w 48 -h 48
inkscape -f icon.svg -e ../app/src/main/res/drawable-hdpi/ic_launcher.png -w 72 -h 72
inkscape -f icon.svg -e ../app/src/main/res/drawable-xhdpi/ic_launcher.png -w 96 -h 96
inkscape -f icon.svg -e ../app/src/main/res/drawable-xxhdpi/ic_launcher.png -w 144 -h 144

inkscape -f down_chevron.svg -e ../app/src/main/res/drawable/down_chevron.png -w 88 -h 60
inkscape -f up_chevron.svg -e ../app/src/main/res/drawable/up_chevron.png -w 88 -h 60
