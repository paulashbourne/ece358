default:
	javac src/com/ece358/*.java
clean:
	find . -name "*.class" -exec rm -rf {} \;
	find . -name "*.pyc" -exec rm -rf {} \;
test:
	echo "=== RUNNING test01.py ==="
	python tests/test01.py
	echo "=== RUNNING test02.py ==="
	python tests/test02.py
