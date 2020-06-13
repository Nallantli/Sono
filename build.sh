echo "Compiling Main class files..."
javac -cp . main/Main.java -encoding utf8 -d bin
cd bin
echo "Creating SonoLang.jar..."
jar cvfe SonoLang.jar main.Main .
cd ..
echo "Done."