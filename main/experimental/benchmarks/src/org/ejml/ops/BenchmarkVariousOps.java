/*
 * Copyright (c) 2009-2017, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.ops;

import org.ejml.data.DMatrixRow_F64;

import java.util.Arrays;
import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkVariousOps {

    static Random rand = new Random(0xffff);

    static int TRIALS_TRANSPOSE = 20000000;
    static int TRIALS_SCALE = 30000000;
    static int TRIALS_NORM = 10000000;
    static int TRIALS_DETERMINANT = 20000000;

    public static long transposeEml(DMatrixRow_F64 mat , int numTrials) {
        long prev = System.currentTimeMillis();

        DMatrixRow_F64 tran = new DMatrixRow_F64(mat.numCols,mat.numRows);

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.transpose(mat,tran);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

//    public static long transposeMtj( DMatrixRow_F64 orig , int numTrials) {
//        DenseMatrix mat = UtilMatrixToolkitsJava.convertToMtj(orig);
//
//        long prev = System.currentTimeMillis();
//
//        DenseMatrix tran = new DenseMatrix(mat.numColumns(),mat.numRows());
//
//        for( int i = 0; i < numTrials; i++ ) {
//            mat.transpose(tran);
//        }
//
//        long curr = System.currentTimeMillis();
//        return curr-prev;
//    }

    public static long scale(DMatrixRow_F64 mat , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.scale(10,mat);
            CommonOps_R64.scale(0.1,mat);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long scale2(DMatrixRow_F64 mat , int numTrials) {
        DMatrixRow_F64 result = mat.copy();

        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.scale(10,mat,result);
            CommonOps_R64.scale(0.1,mat,result);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

//    public static long scaleMtj( DMatrixRow_F64 orig , int numTrials) {
//        DenseMatrix mat = UtilMatrixToolkitsJava.convertToMtj(orig);
//
//        long prev = System.currentTimeMillis();
//
//        for( int i = 0; i < numTrials; i++ ) {
//            mat.scale(10);
//            mat.scale(0.1);
//        }
//
//        long curr = System.currentTimeMillis();
//        return curr-prev;
//    }

    public static long normEml(DMatrixRow_F64 mat , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            NormOps_R64.normF(mat);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long determinant(DMatrixRow_F64 mat , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            CommonOps_R64.det(mat);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long fillManual(DMatrixRow_F64 mat , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            final int size = mat.getNumElements();

            for( int j = 0; j < size; j++ ) {
                mat.set( j , 2 );
            }
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

    public static long fillArrays(DMatrixRow_F64 mat , int numTrials) {
        long prev = System.currentTimeMillis();

        for( int i = 0; i < numTrials; i++ ) {
            Arrays.fill(mat.data,0,mat.getNumElements(),2);
        }

        long curr = System.currentTimeMillis();
        return curr-prev;
    }

//    public static long normMtj( DMatrixRow_F64 orig , int numTrials) {
//        DenseMatrix mat = UtilMatrixToolkitsJava.convertToMtj(orig);
//
//        long prev = System.currentTimeMillis();
//
//        for( int i = 0; i < numTrials; i++ ) {
//            mat.norm(Matrix.Norm.Frobenius);
//        }
//
//        long curr = System.currentTimeMillis();
//        return curr-prev;
//    }

    public static void main( String args[] ) {
        System.out.println("Small Matrix Results:") ;
        DMatrixRow_F64 mat = RandomMatrices_R64.createRandom(4,4,rand);

//        System.out.printf("Transpose:         eml = %10d\n",
//                transposeEml(mat,TRIALS_TRANSPOSE));
//        System.out.printf("Scale:             eml = %10d\n",
//                scale(mat,TRIALS_SCALE));
//        System.out.printf("Scale2:            eml = %10d\n",
//                scale2(mat,TRIALS_SCALE));
//        System.out.printf("Norm:              eml = %10d\n",
//                normEml(mat,TRIALS_NORM));
//        System.out.printf("Determinant:       eml = %10d\n",
//                determinant(mat,TRIALS_DETERMINANT));
        System.out.printf("FillManual:        eml = %10d\n",
                fillManual(mat,TRIALS_SCALE));
        System.out.printf("FillArrays:        eml = %10d\n",
                fillArrays(mat,TRIALS_SCALE));

        System.out.println();
        System.out.println("Large Matrix Results:") ;
        mat = RandomMatrices_R64.createRandom(2000,2000,rand);
//        System.out.printf("Transpose:         eml = %10d\n",
//                transposeEml(mat,100));
//        System.out.printf("Scale:             eml = %10d\n",
//                scaleEml(mat,100));
//        System.out.printf("Norm:              eml = %10d\n",
//                normEml(mat,100));
//        System.out.printf("Determinant:       eml = %10d\n",
//                determinant(mat,1));
        System.out.printf("FillManual:        eml = %10d\n",
                fillManual(mat,1000));
        System.out.printf("FillArrays:        eml = %10d\n",
                fillArrays(mat,1000));
    }
}
