The nn-project's folder contains the following:

1 - resources folder :
  1.1 - train set and test set named dataBaseTrainTrain.txt and dataBaseTrainTrain.txt.
        set format- vector1
  1.2 - train set and test set named mnist_train.csv mnist_test.csv.
        set format - MNIST
  1.3 - train set and test set named mnist_train_3000.csv mnist_test_3000.csv.
        set format - MNIST
        same sets as in subsection 1.2 with 3000 random example as opposed to the entire set.
  1.4 - out.csv
        an example for the output.
        
        
        
2 - NearestNeighbour folder : the project's source code.



3 - jar folder : 
  1.1 - fileReduce.jar.
        use this jar if you want to craete a file with n random examples from m examples in the original file (n<=m)
        How to use:
        Run the jar file named fileReduce.jar using the terminal from the jar's folder.
        
        java -jar fileReduce.jar <input-file-name-and-path> -o <output-file-name-and-path> -n <number-of-examples-to-keep>        
        
        example: 
        java -jar fileReduce.jar mnist_train.csv -o mnist_train_1500.csv -n mnist_train_1500 
        
        notes:
        - the output file name is not mendatory (run the command without -o <output-file-name-and-path>).
          in this case the output will be in the file "out.csv" in the jar's folder.

  1.2 - METRIC_TO_FORMAT.txt
        This file is a mapping from a metric to the formats it supports.
        
        
  1.3 - nn-gen.jar
        this is the jar that runs the classifier.
  
  
  
How to run the  classifier:

Run the jar file named nn-gen.jar using the terminal from the jar's folder.
Use this command:

java -jar nn-gen.jar <training-set-file-name> <training-test-file-name> <format> <metric> <delta> -s <user-input-scale> -d <divisor> > <output-file-name>


notes-
 - *very important*
 
    for now when u run nn-gen.jar, the file METRIC_TO_FORMAT.txt should be in the same folder as nn-gen.jar.
 
 - -s <user-input-scale> is not mendatory.
    in case of not providing a scale the algorithm will calculate the scale.
 - -d <divisor>, this is how much wi reduce the scale each time we try a nwe scale.
    this argument is not mendatory.
    in case it is not provided it will be 2.
 
 
 
The formats currenty supported are "vector1" which is the format we got as part of the instructions for the project
example: 

3,2
1,4,22
4,9,0
1,5,-13

the set for the example is:
vec 1: (1 4 2)
vec 2: (4 9 0)
vec 3: (1 5 -13)

The second format is the format in the MNIST files.
this format is the vectors without any additional information.
exampe:

4,22,1
9,1,2
1,2,3

the set for the example is:
vec 1: (4 22 1)
vec 2: (9 1 2)
vec 3: (1 2 3)

The metric currenty supported "euclidian".

example:

java -jar nn-gen.jar /users/studs/bsc/2015/oronm/Downloads/NearestNeighbour-master/mnist_train.csv /users/studs/bsc/2015/oronm/Downloads/NearestNeighbour-master/mnist_test.csv "MNIST" "euclidian" 0.5 -s 100.5 -d 8